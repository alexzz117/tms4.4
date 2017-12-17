package cn.com.higinet.tms.manager.dao;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import cn.com.higinet.tms.manager.dao.impl.MysqlSimpleDaoImpl;

/**
 * SimpleDao配置
 * @author zhang.lei
 */

@Configuration
public class SimpleDaoConfig {

	@Autowired
	@Qualifier("onlineDataSource")
	DataSource onlineDataSource;

	@Autowired
	@Qualifier("offlineDataSource")
	DataSource offlineDataSource;

	@Autowired
	@Qualifier("dynamicDataSource")
	DataSource dynamicDataSource;

	@Bean("cmcSimpleDao")
	public SimpleDao cmcSimpleDao() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource( offlineDataSource );

		MysqlSimpleDaoImpl simpleDao = new MysqlSimpleDaoImpl();
		simpleDao.setJdbcTemplate( jdbcTemplate );
		return simpleDao;
	}

	@Primary
	@Bean("offlineSimpleDao")
	public SimpleDao offlineSimpleDao() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource( offlineDataSource );

		MysqlSimpleDaoImpl simpleDao = new MysqlSimpleDaoImpl();
		simpleDao.setJdbcTemplate( jdbcTemplate );
		return simpleDao;
	}

	@Bean("dynamicSimpleDao")
	public SimpleDao dynamicSimpleDao() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource( dynamicDataSource );

		MysqlSimpleDaoImpl simpleDao = new MysqlSimpleDaoImpl();
		simpleDao.setJdbcTemplate( jdbcTemplate );
		return simpleDao;
	}

	@Bean("onlineSimpleDao")
	public SimpleDao onlineSimpleDao() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource( onlineDataSource );

		MysqlSimpleDaoImpl simpleDao = new MysqlSimpleDaoImpl();
		simpleDao.setJdbcTemplate( jdbcTemplate );
		return simpleDao;
	}

	@Bean("transactionManager")
	public DataSourceTransactionManager transactionManager() {
		return new DataSourceTransactionManager( dynamicDataSource );
	}

	@Bean("onlineTransactionManager")
	public DataSourceTransactionManager onlineTransactionManager() {
		return new DataSourceTransactionManager( onlineDataSource );
	}

	@Bean("cmcTransactionManager")
	public DataSourceTransactionManager cmcTransactionManager() {
		return new DataSourceTransactionManager( offlineDataSource );
	}

	@Primary
	@Bean("offlineTransactionManager")
	public DataSourceTransactionManager offlineTransactionManager() {
		return new DataSourceTransactionManager( offlineDataSource );
	}

	@Bean("tmsSqlMap")
	public SqlMap tmsSqlMap() {
		SqlMap sqlmap = new SqlMap();
		sqlmap.setDbtype( "mysql" );
		sqlmap.setBasenames( new String[] {
				"classpath:/dbsql/tmsweb-sql"
		} );
		return sqlmap;
	}
}
