/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.util;

import java.util.UUID;

/**
 * 
 * @author Administrator
 * @version 2.2.0
 * @date 2011-10-21
 * @description 通用工具包中有关字符串处理的工具类
 *
 */
public class StringUtil {
	public static String objToString(Object obj) {
		return obj == null ? "" : obj.toString();
	}

	public static int objToInt(Object obj) {
		return obj == null ? 0 : Integer.parseInt(obj.toString());
	}

	public static String nullToEmpty(String strIn) {
		return strIn == null ? "" : strIn;
	}

	public static String arrToStr(String[] arr, String split) {
		if (arr == null || arr.length == 0) {
			return "";
		}
		StringBuffer strBuf = new StringBuffer();
		for (String s : arr) {
			strBuf.append(s).append(split);
		}
		return strBuf.substring(0, strBuf.length() - 1);
	}

	public static boolean isBlank(String value) {
		return value == null || "".equals(value.trim());
	}

	public static String encode(String content) {
		StringBuffer tmpStr = new StringBuffer();
		for (int i = 0; i < content.length(); i++) {
			if (content.charAt(i) == '<') {
				tmpStr.append("&lt");
			} else if (content.charAt(i) == '>') {
				tmpStr.append("&gt");
			} else if (content.charAt(i) == '\n') {
				tmpStr.append("<br>");
			} else if (content.charAt(i) == ' ') {
				tmpStr.append("&nbsp;");
			} else {
				tmpStr.append(content.charAt(i));
			}
		}
		return tmpStr.toString();
	}

	public static StringBuffer appendXmlMessage(String key, String value) {
		StringBuffer bf = new StringBuffer();
		if (!isBlank(value)) {
			if ("DEVICEFINGER".equals(key)) {
				bf.append("<").append(key).append("><![CDATA[").append(value).append("]]></").append(key).append(">");
			} else {
				bf.append("<").append(key).append(">").append(value).append("</").append(key).append(">");
			}
		}
		return bf;
	}

	public static StringBuffer appendNullNode(String key, String value) {
		StringBuffer bf = new StringBuffer();
		if (!isBlank(value)) {
			bf.append("<").append(key).append(">").append(value).append("</").append(key).append(">");
		} else {
			bf.append("<").append(key).append(">").append("</").append(key).append(">");
		}
		return bf;
	}

	public static StringBuffer appendXmlMessage(String key, Object value) {
		StringBuffer bf = new StringBuffer();
		if (value != null) {
			bf.append("<").append(key).append(">").append(value).append("</").append(key).append(">");
		}
		return bf;
	}

	public static String randomUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public static String specialValueEcode(String value) {
		if (value == null) {
			return null;
		}
		if (value.indexOf("&") > -1) {
			value = value.replace("&", "|");
		}
		if (value.indexOf("<") > -1) {
			value = value.replace("<", "_");
		}
		if (value.indexOf(">") > -1) {
			value = value.replace(">", "_");
		}
		return value;
	}

	public static String specialKeyEcode(String key) {
		if (key == null) {
			return null;
		}
		if (key.indexOf("") > -1) {
			key = key.replace(".", "_");
		}
		return key;
	}
	  public static String getNodeText(String xmlString, String nodeName)
	  {
	    String beginName = "<" + nodeName + ">";
	    String endName = "</" + nodeName + ">";
	    int beginIndex = xmlString.indexOf(beginName);
	    if (beginIndex == -1) {
	      return "";
	    }
	    int endIndex = xmlString.indexOf(endName);
	    if (endIndex == -1) {
	      return "";
	    }
	    String nodeValue = xmlString.substring(beginIndex + beginName.length(), endIndex);
	    return nodeValue;
	  }
}
