/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  StatBeans.java   
 * @Package cn.com.higinet.tms.config   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-16 15:42:20   
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.com.higinet.tms.StatBootstrap;
import cn.com.higinet.tms.common.cache.EnvInitializer;
import cn.com.higinet.tms.common.cache.initializer.ConfigInitializer;

/**
 * StatEngine专用bean定义类
 *
 * @ClassName:  StatBeans
 * @author: 王兴
 * @date:   2018-1-16 15:42:20
 * @since:  v4.4
 */
@Configuration
public class StatBeans {
	/**
	 * Stat引导类.
	 *
	 * @return the bootstrap
	 */
	@Bean(initMethod = "start", destroyMethod = "stop")
	public StatBootstrap statBootstrap() {
		StatBootstrap boot = new StatBootstrap();
		boot.setDescription("Stat Bootstrap");
		boot.setDaemon(false);
		return boot;
	}
	
	/**
	 * Env initializer.
	 *
	 * @return the env initializer
	 */
	@Bean
	@Qualifier("EnvInitializer")
	public EnvInitializer envInitializer() {
		ConfigInitializer ei = new ConfigInitializer();
		ei.setCachePrefix("tms.svc.cache.");
		ei.setConfigPath("/statConfig.properties");
		return ei;
	}
}
