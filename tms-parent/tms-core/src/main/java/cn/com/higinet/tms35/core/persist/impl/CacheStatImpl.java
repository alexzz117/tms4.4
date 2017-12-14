/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  CacheStatImpl.java   
 * @Package cn.com.higinet.tms35.core.persist.impl   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-8-21 14:12:31   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms35.core.persist.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.common.cache.Cache;
import cn.com.higinet.tms.common.cache.CacheManager;
import cn.com.higinet.tms.common.cache.KV;
import cn.com.higinet.tms.common.util.StringUtils;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.dao.stat_value;
import cn.com.higinet.tms35.core.persist.Stat;
import cn.com.higinet.tms35.stat.stat_number_encode;
import cn.com.higinet.tms35.stat.stat_row;
import cn.com.higinet.tms35.stat.serv.stat_serv;
import cn.com.higinet.tms35.stat.serv.stat_serv_dispatch_local;

/**
 * 统计值的持久化接口实现类，本实现类是直接缓存实现
 *
 * @ClassName:  CacheStatImpl
 * @author: 王兴
 * @date:   2017-8-21 14:12:31
 * @since:  v4.3
 */
public class CacheStatImpl implements Stat {
	private static final Logger log = LoggerFactory.getLogger(CacheStatImpl.class);

	public static final String STAT_CACHE_GROUP = "stat";

	private CacheManager cacheManager = bean.get(CacheManager.class);

	@Override
	public List<stat_row> queryList(List<stat_row> rows) throws Exception {
		List<KV> kvs = new ArrayList<KV>();
		Cache cache = null;
		try {
			cache = cacheManager.getMainProvider().getCache();
			for (int i = 0, len = rows.size(); i < len; i++) {
				//构造查询KV列表
				stat_row row = rows.get(i);
				kvs.add(new KV(STAT_CACHE_GROUP, row.getUniqID()));
			}
			kvs = cache.get(kvs);
			Map<String, String> resMap = new HashMap<String, String>();
			for (int i = 0, len = kvs.size(); i < len; i++) { //构造结果map，遍历结果时候会用到
				KV kv = kvs.get(i);
				resMap.put(kv.getKey(), kv.getString());
			}
			for (int i = 0, len = rows.size(); i < len; i++) {
				stat_row row = rows.get(i);
				row.set_value(resMap.get(row.getUniqID()));
				if (row.is_query()) {
					row.dec_batch(); //这里补刀，如果m_batch不为空，则会执行latch的递减操作，目前只有查询统计有效
				}
			}
			return rows;
		} catch (Exception e) {
			for (int i = 0, len = rows.size(); i < len; i++) {
				stat_row row = rows.get(i);
				row.set_error();
			}
			throw e;
		} finally {
			if (cache != null)
				cache.close();
		}
	}

	@Override
	public boolean delete(stat_row row) throws Exception {
		Cache cache = null;
		try {
			cache = cacheManager.getMainProvider().getCache();
			cache.delete(new KV(STAT_CACHE_GROUP, row.getUniqID()));
			return true;
		} catch (Exception e) {
			throw e;
		} finally {
			if (cache != null)
				cache.close();
		}
	}

	@Override
	public void saveList(List<stat_row> rows) throws Exception {
		Cache cache = null;
		try {
			cache = cacheManager.getMainProvider().getCache();
			List<KV> deleteList = new ArrayList<KV>();
			List<KV> updateList = new ArrayList<KV>();
			for (int i = 0, len = rows.size(); i < len; i++) {
				stat_row row = rows.get(i);
				String value = row.get_value();
				if (StringUtils.notNull(value)) {
					updateList.add(new KV(STAT_CACHE_GROUP, row.getUniqID(), value.getBytes()));
				} else {
					deleteList.add(new KV(STAT_CACHE_GROUP, row.getUniqID())); //如果一个stat_row为空，那么从缓存中删除
				}
			}
			if (!deleteList.isEmpty()) {
				cache.delete(deleteList);
			}
			if (!updateList.isEmpty()) {
				cache.set(updateList);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (cache != null)
				cache.close();
		}
	}

	@Override
	public void backup(List<stat_row> rows) throws Exception {
		if (rows != null && !rows.isEmpty()) {
			List<stat_value> svs = new ArrayList<stat_value>();
			for (int i = 0, len = rows.size(); i < len; i++) {
				stat_row row = rows.get(i);
				if (StringUtils.isEmpty(row.get_value())) {//去掉值为null的stat_row
					continue;
				}
				String value = stat_number_encode.encode(row.m_stat_id) + ":" + row.get_value() + "\n"; //构造原来stat_value格式存储，否则解析会出问题
				svs.add(new stat_value(row.getUniqID(), value));
			}
			((stat_serv_dispatch_local) stat_serv.inst_eval).inst_back.request(svs);//目前依然是备份到数据库中
		}
	}

}
