package cn.com.higinet.tms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
//@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 1800)
@EnableDiscoveryClient
//@EnableZuulProxy
//@EnableKafka
public class TmsMasterApplication {

	public static void main( String[] args ) {
		/*try {
			System.getProperties().put( "server.ip", InetAddress.getLocalHost().getHostAddress() );
		}
		catch( UnknownHostException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		SpringApplication.run( TmsMasterApplication.class, args );
	}
}