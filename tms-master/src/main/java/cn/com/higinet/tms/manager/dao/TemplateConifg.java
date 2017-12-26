package cn.com.higinet.tms.manager.dao;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import cn.com.higinet.tms.manager.common.DynamicDataSource;

/**
 * @author zhang.lei
 * */
@Configuration
public class TemplateConifg {
	

	@Autowired
	@Qualifier("onlineDataSource")
	DataSource onlineDataSource;

	@Autowired
	@Qualifier("offlineDataSource")
	DataSource offlineDataSource;

	@Autowired
	@Qualifier("dynamicDataSource")
	DynamicDataSource dynamicDataSource;
	
	
	
	@Primary
	@Bean("tmsJdbcTemplate")
	public JdbcTemplate tmsJdbcTemplate() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource( dynamicDataSource );
		return jdbcTemplate;
	}
	
	@Bean("officialJdbcTemplate")
	public JdbcTemplate onlineJdbcTemplate() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource( onlineDataSource );
		return jdbcTemplate;
	}
	
	@Bean("tmpJdbcTemplate")
	public JdbcTemplate tmpJdbcTemplate() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource( offlineDataSource );
		return jdbcTemplate;
	}

}
