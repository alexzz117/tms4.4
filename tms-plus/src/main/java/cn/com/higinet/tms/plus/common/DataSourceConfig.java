package cn.com.higinet.tms.plus.common;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 数据源配置
 * @author zhang.lei
 */

@Configuration
public class DataSourceConfig {

	/**
	 * 在线库数据源配置
	 */
	@Bean(name = "onlineDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.online")
	public DataSource onlineDataSource() {
		return DataSourceBuilder.create().build();
	}

	/**
	 * 离线库数据源配置
	 */
	@Primary
	@Bean(name = "offlineDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.offline")
	public DataSource offlineDataSource() {
		return DataSourceBuilder.create().build();
	}

}
