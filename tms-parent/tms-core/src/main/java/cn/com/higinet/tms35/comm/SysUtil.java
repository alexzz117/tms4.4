package cn.com.higinet.tms35.comm;

import java.security.MessageDigest;
import java.util.List;

public class SysUtil {
	public static String toString(Object value){
		return value == null ? null : value.toString();
	}
	public static boolean isEmpty(String value){
		return value == null || "".equals(value.trim()); 
	}
	public static String cutStrFromEnd(String value,int length){
		if(value == null){
			return null;
		}
		if(value.length() < length){
			return "";
		}
		return value.substring(0, value.length() - length);
	}
	public static String getProjectPath(){
		return System.getProperty("user.dir").replaceAll("\\\\", "/");
	}
	
	public static String strJoin(String[] strs,String joinStr){
		if(strs == null || strs.length == 0){
			return "";
		}
		if(strs.length == 1){
			return strs[0];
		}
		StringBuffer sb= new StringBuffer(strs[0]);
		for (int i = 1; i < strs.length; i++) {
			sb.append(joinStr).append(strs[i]);
		}
		return sb.toString();
	}
	
	public static String strRepeat(String str,int num){
		StringBuffer sb= new StringBuffer();
		for (int i = 0; i < num; i++) {
			sb.append(str);
		}
		return sb.toString();
	}
	
	public static boolean isEmpty(Object value){
		if(value == null){
			return true;
		}
		return value.toString().trim().isEmpty();
	}
	
	public static boolean strEqualsIgnoreCase(Object o1,Object o2){
		if(o1 == null && o2 == null){
			return true;
		}
		if(o1 == null || o2 == null){
			return false;
		}
		return toString(o1).trim().equalsIgnoreCase(toString(o2).trim());
	}
	
	public static <T> T list0(List<T> list){
		if(list == null || list.isEmpty()){
			return null;
		}
		return list.get(0);
	}
	
	public static String md5(Object value){
		String s;
		if(value == null){
			s = null;
		}else{
			s = value.toString();
		}
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
