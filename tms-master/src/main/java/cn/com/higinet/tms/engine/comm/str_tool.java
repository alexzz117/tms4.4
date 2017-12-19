package cn.com.higinet.tms.engine.comm;

import java.security.MessageDigest;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class str_tool
{
	public static final String MAX_VALUE = new String(new char[]{0xFFFF,0xFFFF,0xFFFF,0xFFFF,0xFFFF,0xFFFF,0xFFFF,0xFFFF,0xFFFF,0xFFFF,0xFFFF});
	static char[] lower = new char[256];
	static char[] upper = new char[256];
	static
	{
		for (int i = 0; i < 256; i++)
		{
			lower[i] = Character.toLowerCase((char) i);
			upper[i] = Character.toUpperCase((char) i);
		}
	}

	final public static boolean is_empty(String s)
	{
		return s == null || s.length() == 0;
	}

	final public static String upper_case(String s)
	{
		if (is_empty(s))
			return s;

		char[] buf = s.toCharArray();
		for (int i = 0, len = buf.length; i < len; i++)
		{
			if (buf[i] < 255)
				buf[i] = upper[buf[i]];
		}

		return String.copyValueOf(buf);
	}

	final public static String lower_case(String s)
	{
		if (is_empty(s))
			return s;

		char[] buf = s.toCharArray();
		for (int i = 0, len = buf.length; i < len; i++)
		{
			if (buf[i] < 255)
				buf[i] = lower[buf[i]];
		}

		return String.copyValueOf(buf);
	}

	final public static String to_str(Object val)
	{
		if(val==null)
			return null;
		return val.toString();
	}

	final public static String trim(String value)
	{
		if(value==null)
			return value;
		
		return value.trim();
	}
	
	final public static String xml_decode(String str)
	{
		str = str.replaceAll("(?u)&amp;", "&");
		str = str.replaceAll("(?u)&lt;", "<");
		str = str.replaceAll("(?u)&gt;", ">");
		str = str.replaceAll("(?u)&apos;", "'");
		str = str.replaceAll("(?u)&quot;", "\"");
		return str;
	}
	
	final public static boolean is_number(String str)
	{
        Pattern pattern = Pattern.compile("^[0-9]\\d*$");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ) {
             return false;
        }
        return true;
	}
	
	final public static String randomUUID() {
	    return UUID.randomUUID().toString().replace("-", "");
	}
	
	public final static String MD5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};       
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
