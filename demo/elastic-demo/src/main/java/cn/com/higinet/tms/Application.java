package cn.com.higinet.tms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@PropertySource("classpath:/application.properties")
@SpringBootApplication
@EnableScheduling
//@EnableAdminServer
@EnableTransactionManagement
@EnableCaching
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class,DataSourceAutoConfiguration.class})
public class Application {
	public static void main(String[] args) {
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "20");

        ApplicationContext ctx = SpringApplication.run(Application.class, args);
//        String[] activeProfiles = ctx.getEnvironment().getActiveProfiles();
//        for (String profile : activeProfiles) {
//        }
	}
}
