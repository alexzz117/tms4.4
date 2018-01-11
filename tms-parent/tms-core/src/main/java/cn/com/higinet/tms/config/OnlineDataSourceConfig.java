package cn.com.higinet.tms.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import cn.com.higinet.c3p0.Base64Util;
import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.rapid.base.dao.impl.OracleSimpleDaoImpl;

@Configuration
public class OnlineDataSourceConfig {

	@Bean(name = "tmsDataSource")
	@Qualifier("tmsDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.tms")
	@Primary
	public DataSource tmsDataSource(@Value("${spring.datasource.tms.password}") String passwd) {
		return DataSourceBuilder.create().password(Base64Util.base64Decode(passwd)).build();
	}

	@Bean(name = "tmsJdbcTemplate")
	public JdbcTemplate tmsJdbcTemplate(@Qualifier("tmsDataSource") DataSource tmsDataSource) {
		return new JdbcTemplate(tmsDataSource);
	}

	@Bean(name = "tmsSimpleDao")
	public SimpleDao tmsSimpleDao(@Qualifier("tmsJdbcTemplate") JdbcTemplate tmsJdbcTemplate) {
		OracleSimpleDaoImpl dao = new OracleSimpleDaoImpl();
		dao.setJdbcTemplate(tmsJdbcTemplate);
		return dao;
	}
}
