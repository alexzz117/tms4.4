package cn.com.higinet.tms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

import cn.com.higinet.tms.config.OfflineDataSourceConfig;
import cn.com.higinet.tms.config.RiskBeans;

@SpringBootApplication
@ComponentScan(basePackages = { "cn.com.higinet.tms*" }, excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { RiskBootstrap.class, OfflineDataSourceConfig.class, RiskBeans.class, RiskApplication.class }))
public class StatApplication {
	public static void main(String[] args) {
		SpringApplication.run(StatApplication.class, args);
	}
}
