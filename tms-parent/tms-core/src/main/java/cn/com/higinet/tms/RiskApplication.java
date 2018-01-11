package cn.com.higinet.tms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;

import cn.com.higinet.tms.config.StatBeans;

import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource(locations = { "classpath:service-context.xml" })
@ComponentScan(basePackages = { "cn.com.higinet.tms*" }, excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { StatApplication.class, StatBootstrap.class, StatBeans.class }))
public class RiskApplication {
	public static void main(String[] args) {
		SpringApplication.run(RiskApplication.class, args);
	}
}
