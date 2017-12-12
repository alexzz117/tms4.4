package cn.com.higinet.tms35.manage.mgr.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.com.higinet.tms35.manage.exception.TmsMgrWebException;

/**
 * 数据库时间（毫秒数）和页面上时间（字符串）转换
 * @author wangsch
 *
 */
public class MgrDateConvertUtil {
	/** 形如：2013-06-05 16:55:07 */
	public static final SimpleDateFormat FORMATE1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/** 形如：2013-06-05 */
	public static final SimpleDateFormat FORMATE2 = new SimpleDateFormat("yyyy-MM-dd");
	
	/**
	 * 将日期字符串转换为毫秒数
	 * @param date 将日期字符串
	 * @return 毫秒数，当将日期字符串为null或空字符串时返回0
	 */
	public static long convert2Millisr(String date,SimpleDateFormat formater){
		Calendar calendar = Calendar.getInstance();
		try {
			if(date!=null && !date.equals("")){
				Date newDate = formater.parse(date);
				calendar.setTime(newDate);
				return calendar.getTimeInMillis();
			}
		} catch (ParseException e) {
			e.printStackTrace();
			throw new TmsMgrWebException("将日期字符串转换为毫秒数时出错", e);
		}
		return 0;
	}
	
	/**
	 * 将毫秒数转换为日期字符串
	 * @param millis 毫秒数
	 * @return
	 */
	public static String convert2String(long millis,SimpleDateFormat formater){
		return formater.format(new Date(millis));
	}
	
//	public static void main(String[] args) {
//		System.out.println(MgrDateConvertUtil.convert2Millisr("2013-06-05 16:54:22",MgrDateConvertUtil.FORMATE1));
//		System.out.println(MgrDateConvertUtil.convert2Millisr("2013-06-05",MgrDateConvertUtil.FORMATE2));
//		System.out.println(convert2String(1002341134,FORMATE1));
//		System.out.println(convert2String(1002349458,FORMATE2));
//	}
}