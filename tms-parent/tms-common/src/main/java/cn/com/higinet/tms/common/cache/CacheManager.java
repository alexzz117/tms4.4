/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  CacheManager.java   
 * @Package cn.com.higinet.tms.common.cache   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-7-24 15:48:12   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.higinet.tms.common.exception.UnsupportedParameterException;
import cn.com.higinet.tms.common.lifecycle.Service;
import cn.com.higinet.tms.common.lifecycle.StartServiceException;
import cn.com.higinet.tms.common.util.ObjectUtils;
import cn.com.higinet.tms.common.util.StringUtils;

/**
 * 缓存服务，支持多种类型的缓存操作，这里提供统一的缓存管理，此对象为单例，全局只应该存在一个对象。
 * 缓存配置由前缀+cacheid+attribute组成，可以支持多个缓存源配置，只要id不同即可。<br/>
 * {@code
 *  #缓存ID为test1的配置如下
 * 	tms.svc.cache.test1.url=192.168.10.189:6379
 *  tms.svc.cache.test1.providerClass=cn.com.higinet.tms.common.cache.provider.RedisCacheProvider
 *  #其他配置
 *  xxxxxxxxxxxxxxx
 * }<br/>
 * {@code
 *  //代码中获取test1的provider方式如下
 * 	CacheProvicer provider=CacheManager.getProvider("test1"); //test1建议定义到常量类中
 *  Cache cache=protiver.getCache(); //进一步获取Cache对象
 * }<br/>
 * 如果想知道每个provider具体的配置，可以查看每个provider的类注释。
 * 
 * @ClassName: CacheManager
 * @author: 王兴
 * @date: 2017-7-24 15:48:12
 * @since: v4.3
 */
public class CacheManager extends Service {

	/** 默认的主cache. */
	private static final String MAIN_CACHE = "main";

	/** 一个hashmap，存放了cacheid对应的CacheProvider，CacheManager主要就是提供CacheProvider. */
	private Map<String, CacheProvider> providers;

	private EnvInitializer envInitializer; //CacheEnv初始化器

	public EnvInitializer getEnvInitializer() {
		return envInitializer;
	}

	public void setEnvInitializer(EnvInitializer envInitializer) {
		this.envInitializer = envInitializer;
	}

	/**
	 * 返回cacheId对应的CacheProvider，这个CacheProvider已经被初始化了，直接使用。
	 *
	 * @param cacheId
	 *            the cache id
	 * @return provider 属性
	 */
	public CacheProvider getProvider(String cacheId) throws ProviderNotFoundException {
		CacheProvider provider = providers.get(cacheId);
		if (provider == null) {
			throw new ProviderNotFoundException("Can't find \"%s\" cache provider,please ensure that there is a cache provider named \"%s\" in your configuration.", cacheId, cacheId);
		}
		return provider;
	}

	/**
	 * 返回 main provider，默认是name属性为main的cache.
	 *
	 * @return main provider 属性
	 */
	public CacheProvider getMainProvider() {
		return getProvider(MAIN_CACHE);
	}

	/**
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStart()
	 */
	@Override
	protected void doStart() throws Throwable {
		if (envInitializer == null) {
			throw new StartServiceException("CacheEnv initializer of CacheManager can not be null.");
		}
		envInitializer.initialize();
		providers = new HashMap<String, CacheProvider>();
		List<CacheEnv> envs = envInitializer.initCacheEnv();
		if (envs != null && !envs.isEmpty()) {
			String cacheId;
			for (int i = 0, len = envs.size(); i < len; i++) {
				CacheEnv env = envs.get(i);
				cacheId = env.getCacheId();
				String providerClass = env.getString(Constants.BASE_CONFIG_PROVIDER, null);// provider的class，此参数是必须配置
				if (StringUtils.isNull(providerClass)) {
					throw new UnsupportedParameterException("The cache attribute \"providerClass\" can not be null for cache id=\"%s\"", cacheId);
				}
				CacheProvider cacheProvider = (CacheProvider) ObjectUtils.createObject(providerClass);
				if (cacheProvider instanceof Service) {
					((Service) cacheProvider).setName("CacheProvider[" + cacheId + "]");
				}
				cacheProvider.setEnv(env);
				boolean success = cacheProvider.start(); //及时启动，如果有问题，则立即抛出
				if (!success) {
					throw new StartServiceException("");
				}
				try {
					cacheProvider.getCache().get(new KV("test", "test")); //此处用于测试缓存服务是否正常
				} catch (Exception e) {
					throw new StartServiceException(StringUtils.format("Cache provider is \"%s\"", cacheId), e);
				}
				providers.put(cacheId, cacheProvider);
			}
		}
	}

	/**
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStop()
	 */
	@Override
	protected void doStop() throws Throwable {
		Collection<CacheProvider> cps = providers.values();
		for (CacheProvider cp : cps) {
			try {
				cp.stop();
			} catch (Exception e) {
				logger.error(StringUtils.format("Exception occurred when stop CacheProvider\"%s\".", cp.getEnv().getCacheId()), e);
			}
		}
		providers.clear();
		providers = null;
		envInitializer.destroy();
	}

}
