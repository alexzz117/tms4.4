package cn.com.higinet.tms.base.util;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Moneyz {

	private final static Logger logger = LoggerFactory.getLogger( Moneyz.class );

	public static BigDecimal liToYuan( Long org ) {
		if( org == null ) return null;
		try {
			BigDecimal value = new BigDecimal( org );
			return value.divide( new BigDecimal( 1000 ), 2, BigDecimal.ROUND_HALF_UP );
		}
		catch( Exception e ) {
			logger.error( e.getMessage(), e );
			return null;
		}
	}
	
	public static BigDecimal fenToYuan( Long org ) {
		if( org == null ) return null;
		try {
			BigDecimal value = new BigDecimal( org );
			return value.divide( new BigDecimal( 100 ), 2, BigDecimal.ROUND_HALF_UP );
		}
		catch( Exception e ) {
			logger.error( e.getMessage(), e );
			return null;
		}
	}
}
