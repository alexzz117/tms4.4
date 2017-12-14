package cn.com.higinet.tms.manager.modules.common.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/*
 * author yanghui
 */
public class ExceptionUtil {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static String getStackTrace(Throwable e) {
		String str = "";
		if (e == null) return str;
		
		StackTraceElement[] t = e.getStackTrace();
		int count = 0;
		for (int i = 0; t != null && i < t.length; i++) {
			StackTraceElement tem = t[i];
			if(tem.getClassName().indexOf("cn.com.higinet") == -1) continue;
			
			str = str + (count + 1) + " = 异常类信息:" + tem.toString() + "\r\n";
			count++;
		}
		str = str + "主要原因是:" + e.toString();
		return str;
	}
	
	/**
	 * 
	 * 创建人：杨葵
	 * 方法说明:
	 * @see #getStackTrace 去掉文字,字符修饰,只要追踪到的异常堆栈
	 * @param e
	 * @return String
	 * 返回值说明:
	 */
	public static String getStackTraceCleanVersion(final Throwable e) {
		String str = "";
		if (e == null) 
			return str;
		StackTraceElement[] t = e.getStackTrace();
		
		for (int i = 0; t != null && i < t.length; i++) {
			
			StackTraceElement tem = t[i];
			str += tem.toString() + "\r\n";
		}
		return str;
	}
	
	/**
	 * 
	 * 创建人：杨葵
	 * 方法说明:
	 * 获取异常信息
	 * @param e
	 * @return String
	 * 返回值说明:
	 */
	public static  String getErrorInfoAsString(final Throwable e){
		OutputStream ostr = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(ostr));
		return ostr.toString();
	}
}
