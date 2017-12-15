package cn.com.higinet.tms.manager.modules.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesUtil {
	private static final Logger log = LoggerFactory.getLogger( PropertiesUtil.class );

	private String path = "config/conf.properties";
	private static PropertiesUtil instance = null;
	private Properties prop = null;

	public PropertiesUtil() {}

	public static synchronized PropertiesUtil getPropInstance() {
		if( instance == null ) {
			instance = new PropertiesUtil();
		}
		return instance;
	}

	public synchronized void load() {
		if( prop != null ) {
			return;
		}
		prop = new Properties();
		try {
			InputStream in = this.getClass().getClassLoader().getResourceAsStream( path );
			prop.load( in );
			in.close();
		}
		catch( IOException e ) {
			log.error( "PropertiesUtil load error", e );
		}
	}

	public String get( String key ) {
		if( prop == null ) {
			this.load();
		}
		String value = "";
		try {
			value = prop.getProperty( key );
		}
		catch( Exception e ) {
			log.error( "PropertiesUtil get error", e );
		}
		return value;
	}

	public String get( String key, String default_value ) {
		String value = get( key );
		if( value == null || value.length() == 0 ) {
			value = default_value;
		}
		return value;
	}

	public static void main( String[] args ) {
		log.debug( PropertiesUtil.getPropInstance().get( "createPattern" ) );
	}
}
