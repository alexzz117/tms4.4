/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  AerospikeCacheAdapter.java   
 * @Package cn.com.higinet.tms.common.cache.adapter   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 司福林
 * @date:   2017-7-26 11:37:23   
 * @version V4.3 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.cache.adapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.policy.WritePolicy;

import cn.com.higinet.tms.common.cache.KV;
import cn.com.higinet.tms.common.exception.NullParamterException;

/**
 * Aerospike适配器，可对aerospike数据库进行增删改查以及批量增删改查操作。<p/>
 * 数据库操作需要获取数据库客户端{@link cn.com.higinet.tms.common.cache.provider.AerospikeCacheProvider.doStart()}。
 * @ClassName:  AerospikeCacheAdapter
 * @author: 司福林
 * @date:   2017-7-26 11:37:29
 * @since:  v4.3
 */
public class AerospikeCacheAdapter extends AbstractCacheAdapter {

	/** 每个服务对象有自己的日志打印。 */
	protected final Logger logger = LoggerFactory.getLogger(AerospikeCacheAdapter.class);

	/** 内存数据库列的名字 */
	private static final String BIN_NAME = "";

	/** aerospike 客户端 */
	private AerospikeClient client;

	/** aerospike 数据库名字 */
	private String namespace;

	public AerospikeClient getClient() {
		return client;
	}

	public void setClient(AerospikeClient client) {
		this.client = client;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/** 
	 * @see cn.com.higinet.tms.common.cache.adapter.AbstractCacheAdapter#get(cn.com.higinet.tms.common.cache.KV)
	 */
	@Override
	public KV get(KV kv) {
		if (kv == null)
			throw new NullParamterException("param kv is musts be inited");

		Key key = new Key(namespace, kv.getGroup(), kv.getKey());
		Record record = client.get(null, key);
		Object val = null;
		if (record != null)
			val = record.getValue(BIN_NAME);
		kv.setValue(val);
		return kv;
	}

	/** 
	 * @see cn.com.higinet.tms.common.cache.adapter.AbstractCacheAdapter#get(java.util.List)
	 */
	@Override
	public List<KV> get(List<KV> kvs) {
		if (kvs == null || kvs.size() == 0)
			throw new NullParamterException("param kvs is musts be inited");

		Key[] keys = new Key[kvs.size()];
		int i;
		KV kv = null;
		for (i = 0; i < kvs.size(); i++) {
			kv = kvs.get(i);
			keys[i] = new Key(namespace, kv.getGroup(), kv.getKey());
		}
		Record[] records = client.get(null, keys);
		for (i = 0; i < kvs.size(); i++) {
			kv = kvs.get(i);
			kv.setValue(records[i] == null ? null : records[i].getValue(BIN_NAME));
		}
		return kvs;
	}

	/** 
	 * @see cn.com.higinet.tms.common.cache.adapter.AbstractCacheAdapter#set(cn.com.higinet.tms.common.cache.KV)
	 */
	@Override
	public void set(KV kv) {
		if (kv == null)
			throw new NullParamterException("Parameter kv can not be null.");
		byte[] value = kv.getValue();
		if (value == null) {
			log.warn("Value of KV[group is \"{}\",key is \"{}\"] which will be cached can not bt null.This KV object will be ignored.", kv.getGroup(), kv.getKey());
			return;
		}
		WritePolicy policy = null;
		long expireTime = kv.getExpireTimestamp();
		if (expireTime > 0) {
			policy = new WritePolicy();
			if (kv.isUseTTL()) {
				policy.expiration = (int) kv.getExpireTimestamp();
			} else {
				long time = expireTime - System.currentTimeMillis();
				if (time < 0) {
					policy.expiration = 1;//1秒
				} else {
					policy.expiration = (int)time;
				}

			}
		}
		Key key = new Key(namespace, kv.getGroup(), kv.getKey());
		Bin bin = new Bin(BIN_NAME, value);
		client.put(policy, key, bin);
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.adapter.AbstractCacheAdapter#set(java.util.List)
	 */
	@Override
	public void set(List<KV> kvs) {
		if (kvs == null || kvs.size() == 0)
			throw new NullParamterException("param kvs is musts be inited");
		for (int i = 0; i < kvs.size(); i++) {
			try {
				set(kvs.get(i));
			} catch (RuntimeException e) {
				logger.error("aerospike delete the key {} occured exception ", kvs.get(i).getKey(), e);
			}
		}

	}

	/**
	 * @see cn.com.higinet.tms.common.cache.adapter.AbstractCacheAdapter#delete(cn.com.higinet.tms.common.cache.KV)
	 */
	@Override
	public void delete(KV kv) {
		if (kv == null)
			throw new NullParamterException("param kv is musts be inited");
		Key key = new Key(namespace, kv.getGroup(), kv.getKey());
		client.delete(null, key);
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.adapter.AbstractCacheAdapter#delete(java.util.List)
	 */
	@Override
	public void delete(List<KV> kvs) {
		if (kvs == null || kvs.size() == 0)
			throw new NullParamterException("param kvs is musts be inited");
		for (int i = 0; i < kvs.size(); i++) {
			try {
				delete(kvs.get(i));
			} catch (RuntimeException e) {
				logger.error("aerospike delete the key {} occured exception ", kvs.get(i).getKey(), e);
			}
		}
	}

	@Override
	public boolean exists(KV kv) {
		Key key = new Key(namespace, kv.getGroup(), kv.getKey());
		return client.exists(null, key);
	}

	@Override
	public Map<KV, Boolean> exists(List<KV> kvs) {
		Map<KV, Boolean> resMap = new HashMap<KV, Boolean>(kvs.size() * 2);
		int size = kvs.size();
		Key[] keys = new Key[size];
		KV kv;
		for (int i = 0; i < size; i++) {
			kv = kvs.get(i);
			keys[i] = new Key(namespace, kv.getGroup(), kv.getKey());
		}
		boolean[] res = client.exists(null, keys);
		for (int i = 0; i < size; i++) {
			kv = kvs.get(i);
			resMap.put(kv, res[i]);
		}
		return resMap;
	}

	public void deleteAll() {
		//目前Aerospike无法实现清空整个namespace
	}

	/**
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {

	}

}
