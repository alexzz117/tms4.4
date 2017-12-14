/*
 * Copyright © 2012 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common.util;

import java.util.UUID;
/**
 * 
 * @author Administrator
 * @version 2.2.0
 * @date 2011-10-21
 * @description 通用工具包中有关字符串处理的工具类
 *
 */
public class CmcStringUtil {
    public static String objToString(Object obj) {
    	return obj == null?"":obj.toString();
    }
    public static int objToInt(Object obj) {
    	return obj == null?0:Integer.parseInt(obj.toString());
    }
    public static String nullToEmpty(String strIn) {
    	return strIn == null?"":strIn;
    }
    public static String arrToStr(String[] arr,String split) {
    	if(arr==null||arr.length==0){
    		return "";
    	}
    	StringBuffer strBuf = new StringBuffer();
    	for(String s:arr){
    		strBuf.append(s).append(split);
    	}
    	return strBuf.substring(0, strBuf.length()-1);
    }
	public static boolean isBlank(String value) {
		return value==null||"".equals(value.trim());
	}
	public static String encode(String content)
	{
		StringBuffer tmpStr = new StringBuffer();
		for (int i = 0; i < content.length(); i++)
		{
			if (content.charAt(i) == '<')
			{
				tmpStr.append("&lt");
			} else if (content.charAt(i) == '>'){
				tmpStr.append("&gt");
			} else if (content.charAt(i) == '\n'){
				tmpStr.append("<br>");
			} else if (content.charAt(i) == ' '){
				tmpStr.append("&nbsp;");
			} else {
				tmpStr.append(content.charAt(i));
			}
		}
		return tmpStr.toString();
	}
	
	public static StringBuffer appendXmlMessage(String key,String value) {
		StringBuffer bf = new StringBuffer();
		if(!isBlank(value)){
			bf.append("<").append(key).append(">").append(value).append("</").append(key).append(">");
		}
		return bf;
	}
	
	public static StringBuffer appendXmlMessage(String key,Object value) {
		StringBuffer bf = new StringBuffer();
		if(value!=null){
			bf.append("<").append(key).append(">").append(value).append("</").append(key).append(">");
		}
		return bf;
	}
	
	public static String randomUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
    /**
	 * 如果字符串为空返回
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str){
		return (str == null || "".equals(str));
	}
	
	/**
	 * 获得新的字符串
	 * @param strs
	 * @param length
	 * @param charset
	 * @return
	 */
	public static String getStrs(String strs,int length,String charset){
		String str = "";
		String newStr = "";
		for(int i=0;i<strs.length();i++){
			char s = strs.charAt(i);
			if((""+s).getBytes().length>(""+s).length()){
				if("GBK".equals(charset)){
					str += s+"**";
				}else{
					str += s+"***";
				}
			}
			newStr+=s;
			if(str.length()>=length){
				break;
			}
		}
		
		return newStr;
	}
	
	public static void main(String [] args){
		System.out.println("作".hashCode());
	}
	
}
