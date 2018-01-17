/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  CacheTrafficImpl.java   
 * @Package cn.com.higinet.tms35.core.persist.impl   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-8-26 10:59:59   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms35.core.persist.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.com.higinet.tms.common.cache.Cache;
import cn.com.higinet.tms.common.cache.CacheManager;
import cn.com.higinet.tms.common.cache.CacheProvider;
import cn.com.higinet.tms.common.cache.KV;
import cn.com.higinet.tms.common.event.EventBus;
import cn.com.higinet.tms.common.event.EventContext;
import cn.com.higinet.tms.common.util.ByteUtils;
import cn.com.higinet.tms.event.Params;
import cn.com.higinet.tms.event.Topics;
import cn.com.higinet.tms.event.modules.kafka.KafkaTopics;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.cache.db_fd;
import cn.com.higinet.tms35.core.cache.db_tab;
import cn.com.higinet.tms35.core.cache.linear;
import cn.com.higinet.tms35.core.cache.str_id;
import cn.com.higinet.tms35.core.dao.dao_trafficdata_read;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.core.persist.Filter;
import cn.com.higinet.tms35.core.persist.Traffic;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.run.run_txn_values;

/**
 * 交易流水的持久化类
 *
 * @ClassName:  CacheTrafficImpl
 * @author: 王兴
 * @date:   2017-8-26 10:59:59
 * @since:  v4.3
 */
public class CacheTrafficImpl implements Traffic {

	private static final Logger log = LoggerFactory.getLogger(CacheTrafficImpl.class);

	/** ds. */
	private data_source ds;

	/** reader. */
	private dao_trafficdata_read reader;

	/** traffic provider. */
	private CacheProvider trafficProvider;

	/** 常量 TRANSACTION_GROUP. */
	private static final String TRANSACTION_GROUP = "trans"; //用于缓存分组

	private static final String CACHE_ID = "temp";//缓存ID

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private EventBus eventBus;

	private linear<db_fd> trafficFields;

	@Override
	public void initialize() {
		trafficProvider = cacheManager.getProvider(CACHE_ID);
		ds = new data_source((DataSource) bean.get("tmsDataSource"));
		reader = new dao_trafficdata_read(db_cache.get().table(), db_cache.get().field(), ds);
	}

	/**
	 * @see cn.com.higinet.tms35.core.persist.Traffic#save(java.lang.String, cn.com.higinet.tms35.run.run_txn_values, cn.com.higinet.tms35.core.persist.Filter)
	 */
	@Override
	public run_txn_values save(run_txn_values value, Filter<run_txn_values> filter) throws Exception {
		run_txn_values existsValues = null;
		Cache cache = null;
		try {
			String key = value.get_txn_code();
			KV existsKv = new KV(TRANSACTION_GROUP, key);
			cache = trafficProvider.getCache();
			cache.get(existsKv);
			byte[] data = existsKv.getValue();
			if (data != null) {
				existsValues = bytesToValues(data);
			}
			if (filter != null) {
				if (!filter.filter(existsValues)) {//如果filter返回false，则直接返回，不继续往下
					log.warn("Save transaction abort because filter return false.Values:{}", existsValues.m_txn_data.toString());
					return existsValues;
				}
			}
			//********************接下来往缓存中写入数据，这块逻辑是4.2版本的cache_pending_data********************
			if (existsValues == null) {
				existsValues = new run_txn_values();
				existsValues.set_indb(value.is_indb()); //由于cache可以采用LRU算法淘汰一部分流水，因此value可能是从数据库查询出来的
			} else {
				if (!value.is_indb() && existsValues.is_indb()) {
					// 交易in_db=false and 缓存in_db=true, 说明数据已经入库, 将交易in_db设置为true
					value.set_indb(true);
				} else {
					existsValues.set_indb(value.is_indb());
				}
				value.extend(existsValues);
			}
			existsValues.m_txn_data.clear();
			existsValues.m_txn_data.addAll(value.m_txn_data);
			cache.set(new KV(TRANSACTION_GROUP, value.get_txn_code(), valuesToBytes(existsValues)));//这里是应答客户端之前的同步写库，让流水先进缓存
			//***************************************************************************************************
			EventContext event = new EventContext(Topics.TO_KAFKA);
			event.setData(Params.KAFKA_TOPIC, KafkaTopics.TRAFFIC);
			event.setData(Params.KAFKA_DATA, list2map(value));
			event.setData(Params.KAFKA_USE_PARTITION, true);
			event.setData(Params.KAFKA_PARTITION_KEY, value.m_env.get_dispatch());
			eventBus.publish(event);
		} finally {
			if (cache != null) {
				cache.close();
			}
		}
		return existsValues;
	}

	private Map<String, Object> list2map(run_txn_values rf) {
		if (trafficFields == null) {
			synchronized (CacheTrafficImpl.class) {
				db_tab.cache tabCache = db_cache.get().table();
				db_tab tab = tabCache.get("T");
				db_tab base_tab = tabCache.get(tab.base_tab);
				trafficFields = db_cache.get().field().get_tab_fields(base_tab.tab_name);
			}
		}
		Map<String, Object> res = new HashMap<>();
		db_fd.cache dfc = rf.m_env.get_txn().g_dc.field();
		linear<str_id> fdid_list = dfc.get_fdname_localid(rf.id());
		db_fd bd;
		str_id si;
		si = fdid_list.get_uncheck(0);

		for (int b = 0, f = 0, flen = fdid_list.size(); b < trafficFields.size(); b++)
		{
			bd = trafficFields.get_uncheck(b);
			while (str_tool.is_empty(si.s))
			{
				if (++f >= flen)
					break;
				si = fdid_list.get_uncheck(f);
			}
			if (si.s.equals(bd.fd_name))
			{
				 res.put(bd.fd_name, rf.get_fd(si.id));
				if (++f >= flen)
					break;
				si = fdid_list.get_uncheck(f);
			}
		}
		return res;
	}

	/**
	 * @see cn.com.higinet.tms35.core.persist.Traffic#getById(java.lang.String, cn.com.higinet.tms35.run.run_env)
	 */
	@Override
	public run_txn_values getById(String key, run_env env) throws Exception {
		run_txn_values values = null;
		Cache cache = null;
		try {
			//1、先从集中缓存中找
			cache = trafficProvider.getCache();
			KV kv = new KV(TRANSACTION_GROUP, key);
			kv = cache.get(kv);
			byte[] data = kv.getValue();
			if (data != null) {
				return bytesToValues(data);
			}
			//2、应志军要求，缓存中不存在交易流水的时候，依然从数据库中读取，以防万一，
			values = reader.read(env, key);
			if (values != null && values.is_indb()) {
				//确实发现数据库中有，缓存中没有时，可以往缓存中增加记录，可能重复确认时候会出现这个情况
				kv.setValue(valuesToBytes(values));
				if (!cache.exists(kv)) { //当cache中不存在key的时候，才往cache中增加
					cache.set(kv);
				}
			}
			return values;
		} finally {
			if (cache != null) {
				cache.close();
			}
		}
	}

	private byte[] valuesToBytes(run_txn_values values) throws IOException {
		List<Object> data = new ArrayList<Object>(values.m_txn_data);
		data.add(values.is_indb()); //末尾加上is_indb
		return ByteUtils.object2KryoBytes(data);
	}

	@SuppressWarnings("unchecked")
	private run_txn_values bytesToValues(byte[] data) throws IOException {
		List<Object> _values = (List<Object>) ByteUtils.KryoBytes2Object(ArrayList.class, data);
		int len = _values.size();
		run_txn_values values = new run_txn_values();
		Object isIndb = _values.remove(len - 1);
		values.set_indb(Boolean.valueOf(isIndb.toString()));
		values.m_txn_data = _values;
		return values;
	}

	@Override
	public void destroy() {
		if (reader != null)
			reader.close();
		ds = null;
	}

	/**
	 * 这一步操作是异步的
	 * @see cn.com.higinet.tms35.core.persist.Traffic#setInDB(cn.com.higinet.tms35.run.run_txn_values)
	 */
	@Override
	public void setInDB(run_txn_values newValue) throws Exception {
		Cache cache = null;
		try {
			cache = trafficProvider.getCache();
			newValue.set_indb(true);
			cache.set(new KV(TRANSACTION_GROUP, newValue.get_txn_code(), valuesToBytes(newValue)));
		} finally {
			if (cache != null) {
				cache.close();
			}
		}
	}

	@Override
	public run_txn_values getCacheById(String key, run_env env) throws Exception {
		Cache cache = null;
		try {
			//1、先从集中缓存中找
			cache = trafficProvider.getCache();
			KV kv = new KV(TRANSACTION_GROUP, key);
			kv = cache.get(kv);
			byte[] data = kv.getValue();
			if (data != null) {
				return bytesToValues(data);
			}
			return new run_txn_values(env);
		} finally {
			if (cache != null) {
				cache.close();
			}
		}
	}

}
