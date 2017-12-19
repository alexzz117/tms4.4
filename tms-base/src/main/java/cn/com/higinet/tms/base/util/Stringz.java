package cn.com.higinet.tms.base.util;

import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public class Stringz extends StringUtils {

	public static boolean isNotEmpty( Object str ) {
		return !isEmpty( str );
	}

	public static boolean isNotEmpty( Object... strings ) {
		return !isEmpty( strings );
	}

	public static boolean isEmpty( Object... strings ) {
		for( Object str : strings ) {
			if( isEmpty( str ) ) return true;
		}
		return false;
	}

	public static String randomUUID() {
		return UUID.randomUUID().toString().replace( "-", "" );
	}

	public static boolean isNumeric( String str ) {
		if( isEmpty( str ) ) return false;
		Pattern pattern = Pattern.compile( "[0-9]*" );
		return pattern.matcher( str ).matches();
	}

	public static String valueOf( Object arg ) {
		if( arg == null ) return null;

		if( arg instanceof String ) {
			return (String) arg;
		}
		else if( arg instanceof Long ) {
			return ((Long) arg).toString();
		}
		else if( arg instanceof Integer ) {
			return ((Integer) arg).toString();
		}

		return null;
	}

}