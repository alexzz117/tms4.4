/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  RiskBeans.java   
 * @Package cn.com.higinet.tms.config   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-11 17:12:59   
 * @version V1.0 
 * @Copyright: 2018 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import cn.com.higinet.tms.RiskBootstrap;
import cn.com.higinet.tms.common.cache.CacheManager;
import cn.com.higinet.tms.common.repository.CachedDataRepository;
import cn.com.higinet.tms.common.repository.PersistedDataRepository;
import cn.com.higinet.tms.common.repository.RepositoryInitializer;
import cn.com.higinet.tms.common.repository.RepositoryManager;
import cn.com.higinet.tms35.core.persist.TMSDBInitializer;
import cn.com.higinet.tms35.core.persist.TMSPersistence;
import cn.com.higinet.tms35.core.persist.Traffic;
import cn.com.higinet.tms35.core.persist.impl.CacheTrafficImpl;
import cn.com.higinet.tms35.service.service_cache_refresh;

@Configuration
public class RiskBeans {
	/**
	 * Risk引导类.
	 *
	 * @return the bootstrap
	 */
	@Bean(initMethod = "start", destroyMethod = "stop")
	public RiskBootstrap riskBootstrap() {
		return new RiskBootstrap();
	}

	/**
	 * 交易流水入库持久化类.
	 *
	 * @return the traffic
	 */
	@Bean(initMethod = "initialize", destroyMethod = "destroy")
	public Traffic traffic() {
		return new CacheTrafficImpl();
	}

	/**
	 * 基于缓存的数据仓
	 *
	 * @param cacheManager the cache manager
	 * @return the cached data repository
	 */
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public CachedDataRepository cachedDataRepository(@Qualifier("cacheManager") CacheManager cacheManager) {
		CachedDataRepository cd = new CachedDataRepository();
		cd.setCacheManager(cacheManager);
		return cd;
	}

	/**
	 * Tms 持久化接口实现，仅供基于持久化的数据仓使用.
	 *
	 * @return the TMS persistence
	 */
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public TMSPersistence tmsPersistence() {
		TMSPersistence tp = new TMSPersistence();
		return tp;
	}

	/**
	 * 基于持久化的数据仓
	 *
	 * @param cacheManager the cache manager
	 * @param tmsPersistence the tms persistence
	 * @return the persisted data repository
	 */
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public PersistedDataRepository persistedDataRepository(@Qualifier("cacheManager") CacheManager cacheManager, @Qualifier("tmsPersistence") TMSPersistence tmsPersistence) {
		PersistedDataRepository pdr = new PersistedDataRepository();
		pdr.setCacheManager(cacheManager);
		pdr.setPersistence(tmsPersistence);
		return pdr;
	}

	/**
	 * 数据仓管理器的初始化器.
	 *
	 * @return the TMSDB initializer
	 */
	@Bean
	public TMSDBInitializer initializer() {
		return new TMSDBInitializer();
	}

	/**
	 * 数据仓管理器
	 *
	 * @return the repository manager
	 */
	@Bean(initMethod = "start", destroyMethod = "stop")
	public RepositoryManager repositoryManager(RepositoryInitializer initializer) {
		RepositoryManager rm = new RepositoryManager();
		rm.setDescription("数据仓管理");
		rm.setInitializer(initializer);
		return rm;
	}
	
	@Bean(name="cacheRefreshService")
	public service_cache_refresh cacheRefreshService(){
		return new service_cache_refresh();
	}
	
}
