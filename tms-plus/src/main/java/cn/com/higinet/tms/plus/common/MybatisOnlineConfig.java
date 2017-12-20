package cn.com.higinet.tms.plus.common;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = {
		"cn.com.higinet.xxx"
}, sqlSessionFactoryRef = "onlineSessionFactory")
public class MybatisOnlineConfig {

	@Autowired
	@Qualifier("onlineDataSource")
	private DataSource onlineDataSource;

	@Bean("onlineSessionFactory")
	public SqlSessionFactory onlineSessionFactory() throws Exception {
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource( onlineDataSource );
		return factoryBean.getObject();
	}

}
