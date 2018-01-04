package cn.com.higinet.tms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
//@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 1800)
@EnableDiscoveryClient
//@EnableZuulProxy
public class TmsMasterApplication {

	public static void main( String[] args ) {
		SpringApplication.run( TmsMasterApplication.class, args );
	}
}