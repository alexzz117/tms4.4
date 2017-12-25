package cn.com.higinet.tms.engine.comm;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
* @author hyc
* @since 2015年7月8日
*/
public class MD5Util {

	private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		
	/**
	 * MD5 digest.
	 */
	private static MessageDigest md5Digest = null;
	
	public static byte[] getMD5(byte[] b){
		MessageDigest md5 = getMd5Digest();
		md5.reset();
		byte[] hash = null;
		md5.update(b);
		hash = md5.digest();
		return hash;
	}
	public static byte[] getMD5(String content,String charsetName){
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
			md5.reset();
			byte[] hash = null;
			try {
				md5.update(content.getBytes(charsetName));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			hash = md5.digest();
			return hash;
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//getMd5Digest();
		return null;
	}
	public static final String charsetName = "UTF-8";
	public static byte[] getMD5(String content){
		return getMD5(content, charsetName);
	}
	public static String getMD5Hex16(String content){
//		return bytesToHexString(getMD5(content));
		return bytes2hex(getMD5(content));
	}
	/**
	 * Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。   
	 * @param src byte[] data   
	 * @return hex string   
	 */      
	public static String bytesToHexString(byte[] src){   
	    StringBuilder stringBuilder = new StringBuilder(64);   
	    if (src == null || src.length <= 0) {   
	        return null;   
	    }   
	    int len = src.length;
	    for (int i = 0; i < len; i++) {   
	        int v = src[i] & 0xFF;   
	        String hv = Integer.toHexString(v);   
	        if (hv.length() < 2) {   
	            stringBuilder.append(0);   
	        }   
	        stringBuilder.append(hv);   
	    }   
	    return stringBuilder.toString();   
	} 
	/**
	 * byte数组转为16进制ASCII字符串
	 * @param b
	 * @param offset
	 * @param len
	 * @return
	 */
	static String bytes2hex(byte[] b) {
		if(b ==null)
			return "";
		StringBuffer sb = new StringBuffer(64);
	    for(int i = 0; i < b.length; i++){
	    	sb.append(HEX[(b[i]&0xf0)>>4]).append(HEX[b[i] & 0x0f]);
	    }
		return sb.toString();
	}
	/**
	 * Returns a MD5 digest.
	 * 
	 * @return MessageDigest object
	 */
	protected static MessageDigest getMd5Digest() {
		if (md5Digest == null)
			try {
				md5Digest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException nsae) {
				throw new RuntimeException("md5 digest not available", nsae);
			}
		return md5Digest;
	}
}
