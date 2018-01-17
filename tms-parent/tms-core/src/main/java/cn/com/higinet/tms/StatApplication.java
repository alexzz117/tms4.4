package cn.com.higinet.tms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;

import cn.com.higinet.tms.config.OfflineDataSourceConfig;
import cn.com.higinet.tms.config.RiskBeans;

@SpringBootApplication
@PropertySource("classpath:statConfig.properties")
@ComponentScan(basePackages = { "cn.com.higinet.tms*" }, excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class , RiskBootstrap.class, OfflineDataSourceConfig.class, RiskBeans.class, RiskApplication.class }))
public class StatApplication {
	public static void main(String[] args) {
		SpringApplication.run(StatApplication.class, args);
	}
}
