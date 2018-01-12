package cn.com.higinet.tms.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.com.higinet.tms.StatBootstrap;
import cn.com.higinet.tms.common.cache.EnvInitializer;
import cn.com.higinet.tms.common.cache.initializer.ConfigInitializer;

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
	
	@Bean
	@Qualifier("EnvInitializer")
	public EnvInitializer envInitializer() {
		ConfigInitializer ei = new ConfigInitializer();
		ei.setCachePrefix("tms.svc.cache.");
		ei.setConfigPath("/statConfig.properties");
		return ei;
	}
}
