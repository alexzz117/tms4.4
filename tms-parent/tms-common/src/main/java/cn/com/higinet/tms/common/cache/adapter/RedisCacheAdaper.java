/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  RedisCacheAdaper.java   
 * @Package cn.com.higinet.tms.common.cache.adapter   
 * @Description: ( redis适配器 ：数据入redis缓存、获取redis缓存数据，以及删除redis缓存数据操作)   
 * @author: 刘晓春
 * @date:   2017-7-26 16:27:58   
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

import cn.com.higinet.tms.common.cache.KV;
import cn.com.higinet.tms.common.exception.NullParamterException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * redis适配器，用于操作redis中存储的数据，包括：单个和批量的增删改查操作。<br/>
 * 
 * @ClassName: RedisCacheAdaper
 * @author: 刘晓春
 * @date: 2017-7-26 16:31:15
 * @since: v4.3
 */
public class RedisCacheAdaper extends AbstractCacheAdapter {

	/** 常量 SEPARATOR. */
	private static final String SEPARATOR = "_";

	/** redis 客户端 */
	private Jedis jedis;

	public Jedis getJedis() {
		return jedis;
	}

	public void setJedis(Jedis jedis) {
		this.jedis = jedis;
	}

	/**
	 * 获得组合过后的key值
	 *
	 * @param kv
	 *            the kv
	 * @return composed key 属性
	 */
	private String getComposedKey(KV kv) {
		return new StringBuilder(kv.getGroup()).append(SEPARATOR).append(kv.getKey()).toString();
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.Cache#get(cn.com.higinet.tms.common.cache.KV)
	 */
	@Override
	public KV get(KV kv) {
		if (kv == null)
			throw new NullParamterException("When the value is read in redis, the parameter cannot be null");
		kv.setValue(jedis.get(getComposedKey(kv).getBytes()));
		return kv;
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.Cache#get(java.util.List)
	 */
	@Override
	public List<KV> get(List<KV> kvs) {
		if (kvs == null)
			throw new NullParamterException("When reading values in batch redis, parameters cannot be null");
		if (!kvs.isEmpty()) {
			Pipeline p = jedis.pipelined();
			Map<String, Response<byte[]>> newMap = new HashMap<String, Response<byte[]>>();
			KV kv = null;
			for (int i = 0, len = kvs.size(); i < len; i++) {
				kv = kvs.get(i);
				newMap.put(kv.getKey(), p.get(getComposedKey(kv).getBytes()));
			}
			p.sync();
			for (int i = 0; i < kvs.size(); i++) {
				kvs.get(i).setValue(newMap.get(kvs.get(i).getKey()).get());
			}
		}
		return kvs;
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.Cache#set(cn.com.higinet.tms.common.cache.KV)
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
		jedis.set(getComposedKey(kv).getBytes(), value);
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.Cache#set(java.util.List)
	 */
	@Override
	public void set(List<KV> kvs) {
		if (kvs == null)
			throw new NullParamterException("When the values is added to redis, parameters cannot be null");
		if (!kvs.isEmpty()) {
			Pipeline p = jedis.pipelined();
			KV kv = null;
			for (int i = 0, len = kvs.size(); i < len; i++) {
				kv = kvs.get(i);
				byte[] value = kv.getValue();
				if (value == null) {
					log.warn("Value of KV[group is \"{}\",key is \"{}\"] which will be cached can not bt null.This KV object will be ignored.", kv.getGroup(), kv.getKey());
					continue;
				}
				p.set(getComposedKey(kv).getBytes(), value);
			}
			p.sync();
		}
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.Cache#delete(cn.com.higinet.tms.common.cache.KV)
	 */
	@Override
	public void delete(KV kv) {
		if (kv == null)
			throw new NullParamterException("When the redis delete value, parameter cannot be null");
		jedis.del(getComposedKey(kv).getBytes());
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.Cache#delete(java.util.List)
	 */
	@Override
	public void delete(List<KV> kvs) {
		if (kvs == null)
			throw new NullParamterException("When redis batch delete value, parameters cannot be null");
		if (!kvs.isEmpty()) {
			Pipeline p = jedis.pipelined();
			for (int i = 0, len = kvs.size(); i < len; i++) {
				p.del(getComposedKey(kvs.get(i)).getBytes());
			}
			p.sync();
		}
	}

	@Override
	public boolean exists(KV kv) {
		return jedis.exists(getComposedKey(kv));
	}

	@Override
	public Map<KV, Boolean> exists(List<KV> kvs) {
		if (kvs == null)
			throw new NullParamterException("When reading values in batch redis, parameters cannot be null");
		Map<KV, Boolean> resMap = new HashMap<KV, Boolean>(kvs.size() * 2);
		if (!kvs.isEmpty()) {
			Pipeline p = jedis.pipelined();
			Map<String, Response<Boolean>> newMap = new HashMap<String, Response<Boolean>>();
			KV kv = null;
			for (int i = 0, len = kvs.size(); i < len; i++) {
				kv = kvs.get(i);
				newMap.put(kv.getKey(), p.exists(getComposedKey(kv)));
			}
			p.sync();
			for (int i = 0; i < kvs.size(); i++) {
				kv = kvs.get(i);
				resMap.put(kv, newMap.get(kv.getKey()).get());
			}
		}
		return resMap;
	}

	public void deleteAll() {
		jedis.flushDB(); //仅清除当前选择的DB
	}

	/*
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		if (jedis != null) {
			jedis.close();
		}
	}

}
