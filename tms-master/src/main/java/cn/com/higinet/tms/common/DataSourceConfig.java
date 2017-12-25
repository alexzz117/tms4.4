package cn.com.higinet.tms.common;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.google.common.collect.Maps;

import cn.com.higinet.tms.manager.common.DSType;
import cn.com.higinet.tms.manager.common.DynamicDataSource;


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
	
	/**
	 * 动态数据源
	 */
	@Bean("dynamicDataSource")
	public DynamicDataSource dynamicDataSource() {
		DynamicDataSource dynamicDataSource = new DynamicDataSource();

		Map<Object, Object> map = Maps.newHashMap();
		map.put( DSType.DS_OFFICIAL, onlineDataSource() );
		map.put( DSType.DS_TEMP, offlineDataSource() );

		dynamicDataSource.setTargetDataSources( map );
		dynamicDataSource.setDefaultTargetDataSource( offlineDataSource() );
		return dynamicDataSource;
	}

}
