package cn.com.higinet.tms.base.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public class Base64Util {

	public static String base64Encode( String s ) {
		BASE64Encoder base64 = new BASE64Encoder();
		return base64.encode( s.getBytes() );
	}

	public static String base64Decode( String s ) {
		BASE64Decoder base64 = new BASE64Decoder();
		try {
			return new String( base64.decodeBuffer( s ) );
		}
		catch( Exception e ) {
			throw new RuntimeException( e );
		}
	}

}
