package cn.com.higinet.tms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.com.higinet.tms.StatBootstrap;

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
}
