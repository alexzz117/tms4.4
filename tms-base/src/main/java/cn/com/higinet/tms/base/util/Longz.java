package cn.com.higinet.tms.base.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Longz {

	public final static Logger logger = LoggerFactory.getLogger( Longz.class );

	public static Long valueOf( String arg ) {
		try {
			if( Stringz.isEmpty( arg ) ) return null;
			if( !Stringz.isNumeric( arg ) ) return null;
			return Long.valueOf( arg );
		}
		catch( Exception e ) {
			logger.error( e.getMessage(), e );
			return null;
		}

	}

	public static Long valueOf( Integer arg ) {
		try {
			if( arg == null ) return null;
			return Long.valueOf( arg );
		}
		catch( Exception e ) {
			logger.error( e.getMessage(), e );
			return null;
		}

	}

	public static Long valueOf( Object arg ) {
		try {
			if( arg == null ) return null;
			if( Stringz.isEmpty( arg.toString() ) ) return null;
			return Long.valueOf( arg.toString() );
		}
		catch( Exception e ) {
			logger.error( e.getMessage(), e );
			return null;
		}

	}

}
