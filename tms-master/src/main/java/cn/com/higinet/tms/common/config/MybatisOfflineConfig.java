package cn.com.higinet.tms.common.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@MapperScan(basePackages = {
		"cn.com.higinet.xxx"
}, sqlSessionFactoryRef = "offlineSessionFactory")
public class MybatisOfflineConfig {

	@Autowired
	@Qualifier("offlineDataSource")
	private DataSource offlineDataSource;

	@Primary
	@Bean("offlineSessionFactory")
	public SqlSessionFactory offlineSessionFactory() throws Exception {
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource( offlineDataSource );
		return factoryBean.getObject();
	}

}
