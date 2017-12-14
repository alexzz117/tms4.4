package cn.com.higinet.tms.base.util;

import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public class StringUtil extends StringUtils {

	

	public static boolean isNumeric( String str ) {
		if( StringUtil.isEmpty( str ) ) return false;
		Pattern pattern = Pattern.compile( "[0-9]*" );
		return pattern.matcher( str ).matches();
	}
}
