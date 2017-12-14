/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  AbstractCacheAdapter.java   
 * @Package cn.com.higinet.tms.common.cache.adapter   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-7-26 10:20:26   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.cache.adapter;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.common.cache.Cache;
import cn.com.higinet.tms.common.cache.KV;

/**
 * 缓存适配器的基类，除了close方法，其他所有方法均有实现，目前仅仅是抛出UnsupportedOperationException异常.
 * 这些方法都是要各自缓存适配器自己去适配的方法。
 *
 * @ClassName:  AbstractCacheAdapter
 * @author: 王兴
 * @date:   2017-7-26 10:20:28
 * @since:  v4.3
 */
public abstract class AbstractCacheAdapter implements Cache {

	protected Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * @see cn.com.higinet.tms.common.cache.Cache#get(cn.com.higinet.tms.common.cache.KV)
	 */
	@Override
	public KV get(KV kv) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.Cache#get(java.util.List)
	 */
	@Override
	public List<KV> get(List<KV> kvs) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.Cache#set(cn.com.higinet.tms.common.cache.KV)
	 */
	@Override
	public void set(KV kv) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.Cache#set(java.util.List)
	 */
	@Override
	public void set(List<KV> kvs) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.Cache#delete(cn.com.higinet.tms.common.cache.KV)
	 */
	@Override
	public void delete(KV kv) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.Cache#delete(java.util.List)
	 */
	@Override
	public void delete(List<KV> kvs) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.Cache#exists(cn.com.higinet.tms.common.cache.KV)
	 */
	@Override
	public boolean exists(KV kv) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.Cache#exists(java.util.List)
	 */
	@Override
	public Map<KV, Boolean> exists(List<KV> kvs) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.Cache#deleteAll()
	 */
	public void deleteAll() {
		throw new UnsupportedOperationException();
	}
}
