package cn.com.higinet.tms.common.demo.quartz;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.quartz.Scheduler;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@Configuration
@PropertySource("classpath:application.properties")
public class SchedulerConfig {

	@Autowired
	Environment env;

	@Bean(name = "SchedulerFactory")
	public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
		SchedulerFactoryBean factory = new SchedulerFactoryBean();
		factory.setQuartzProperties( quartzProperties() );
		return factory;
	}

	@SuppressWarnings("unchecked")
	@Bean
	public Properties quartzProperties() throws IOException {
		Properties properties = new Properties();

		//将Environment转化为JSON对象
		JSONObject json = JSON.parseObject( JSON.toJSONString( env ) );
		//获取propertySources节点list
		List<JSONObject> itmes = (List<JSONObject>) json.get( "propertySources" );
		for( JSONObject item : itmes ) {
			//获取一个节点下的propertyNames
			List<String> keys = (List<String>) item.get( "propertyNames" );
			if( keys == null ) continue;
			//循环propertyNames拿到系统加载的参数名称，再通过Environment获取参数值
			for( String key : keys ) {
				if( key.startsWith( "org.quartz" ) ) {
					//将参数名和参数值写入入Properties
					properties.setProperty( key, env.getProperty( key ) );
				}
			}
		}
		return properties;
	}

	/*
	 * quartz初始化监听器
	 */
	@Bean
	public QuartzInitializerListener executorListener() {
		return new QuartzInitializerListener();
	}

	/*
	 * 通过SchedulerFactoryBean获取Scheduler的实例
	 */
	@Bean(name = "Scheduler")
	public Scheduler scheduler() throws IOException {
		return schedulerFactoryBean().getScheduler();
	}
}
