package cn.com.higinet.c3p0;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Base64Util {

	public static String base64Encode(String s) {
		BASE64Encoder base64 = new BASE64Encoder();
		return base64.encode(s.getBytes());
	}

	public static String base64Decode(String s) {
		BASE64Decoder base64 = new BASE64Decoder();
		try {
			return new String(base64.decodeBuffer(s));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		String pwd = "tmst";
		System.out.println("原文：" + pwd);
		String en = Base64Util.base64Encode(pwd);
		System.out.println("加密后：" + en);
	}

}
