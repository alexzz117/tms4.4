/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  Beans.java   
 * @Package cn.com.higinet.tms.config   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-11 13:51:30   
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.com.higinet.rapid.base.dao.SqlMap;
import cn.com.higinet.tms.common.cache.CacheManager;
import cn.com.higinet.tms.common.cache.EnvInitializer;
import cn.com.higinet.tms.common.cache.initializer.ConfigInitializer;
import cn.com.higinet.tms35.core.persist.Stat;
import cn.com.higinet.tms35.core.persist.impl.CacheStatImpl;

@Configuration
public class PublicBeans {

	@Value("${tms.dbtype:oracle}")
	private String dbtype;

	@Bean
	public CacheManager cacheManager(@Qualifier("EnvInitializer") EnvInitializer initializer) {
		CacheManager cm = new CacheManager();
		cm.setDescription("缓存服务");
		cm.setEnvInitializer(initializer);
		return cm;
	}

	/**
	 * 统计的持久化接口注入.
	 *
	 * @return the stat
	 */
	@Bean
	public Stat stat() {
		return new CacheStatImpl();
	}

	/**
	 * sql配置文件
	 *
	 * @return the sql map
	 */
	@Bean(name = "tmsSqlMap")
	public SqlMap tmsSqlMap() {
		SqlMap map = new SqlMap();
		map.setDbtype(dbtype);
		map.setBasenames(new String[] { "classpath:/tms-sql" });
		return map;
	}
	
}
