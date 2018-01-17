package cn.com.higinet.tms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
//@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 1800)
@EnableDiscoveryClient
//@EnableZuulProxy
//@EnableElasticSearch
//@EnableKafka
public class TmsMasterApplication {
	private static final Logger logger = LoggerFactory.getLogger( TmsMasterApplication.class );

	public static void main( String[] args ) {
		//System.getProperties().put( "server.ip", InetAddress.getLocalHost().getHostAddress() );
		SpringApplication.run( TmsMasterApplication.class, args );
	}

}