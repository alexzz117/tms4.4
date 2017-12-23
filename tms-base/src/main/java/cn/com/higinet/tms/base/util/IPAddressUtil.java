package cn.com.higinet.tms.base.util;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;

public class IPAddressUtil {

	public static int getTomcatPort() {
		try {
			MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
			Set<ObjectName> objectNames = beanServer.queryNames( new ObjectName( "*:type=Connector,*" ), Query.match( Query.attr( "protocol" ), Query.value( "HTTP/1.1" ) ) );
			String port = objectNames.iterator().next().getKeyProperty( "port" );
			return Integer.valueOf( port );
		}
		catch( Exception e ) {
			e.printStackTrace();
			return 8080;
		}

	}

	public static String getHostName() {
		String host = null;
		try {
			host = InetAddress.getLocalHost().getHostName();
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
		return host;
	}
	
	public static String getHostAddress() {
		String ip = null;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
		return ip;
	}
}
