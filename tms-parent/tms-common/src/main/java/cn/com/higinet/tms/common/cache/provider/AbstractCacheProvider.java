/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  AbstractCacheProvider.java   
 * @Package cn.com.higinet.tms.common.cache.provider   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-7-25 14:04:05   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.cache.provider;

import cn.com.higinet.tms.common.cache.Cache;
import cn.com.higinet.tms.common.cache.CacheEnv;
import cn.com.higinet.tms.common.cache.CacheProvider;
import cn.com.higinet.tms.common.lifecycle.Service;

/**
 * CacheProvider类的基类，提供一些基础操作，具体实现由各自缓存自己实现
 *
 * @ClassName: AbstractCacheProvider
 * @author: 王兴
 * @date: 2017-7-25 14:04:05
 * @since: v4.3
 */
public abstract class AbstractCacheProvider extends Service implements CacheProvider {

	/** cache配置 */
	protected CacheEnv cacheEnv;

	/**
	 * @see cn.com.higinet.tms.common.cache.CacheProvider#getEnv()
	 */
	@Override
	public CacheEnv getEnv() {
		return cacheEnv;
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.CacheProvider#setEnv(cn.com.higinet.tms.common.cache.CacheEnv)
	 */
	@Override
	public void setEnv(CacheEnv env) {
		cacheEnv = env;
	}

	/**
	 * @see cn.com.higinet.tms.common.cache.CacheProvider#getCache()
	 */
	@Override
	public abstract Cache getCache() throws Exception;

}
