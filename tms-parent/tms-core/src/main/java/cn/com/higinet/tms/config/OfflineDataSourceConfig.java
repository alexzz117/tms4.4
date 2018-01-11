package cn.com.higinet.tms.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import cn.com.higinet.c3p0.Base64Util;

@Configuration
public class OfflineDataSourceConfig {

	@Bean(name = "tmpDataSource")
	@Qualifier("tmpDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.tmp")
	public DataSource onlineDataSource(@Value("${spring.datasource.tmp.password}") String passwd) {
		return DataSourceBuilder.create().password(Base64Util.base64Decode(passwd)).build();
	}

	@Bean(name = "tmpJdbcTemplate")
	public JdbcTemplate tmpJdbcTemplate(@Qualifier("tmpDataSource") DataSource tmpDataSource) {
		return new JdbcTemplate(tmpDataSource);
	}

}
