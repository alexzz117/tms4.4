/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  DateUtils.java   
 * @Package com.ydtf.bdp.base.utils   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-7 16:03:01   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */

package cn.com.higinet.tms.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// TODO: Auto-generated Javadoc
/**
 * 日期工具类.
 *
 * @author 王兴
 * @since v1.0
 * @date Aug 7, 2015
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

	/** 常量 DEFAULT_FORMAT_DATE. */
	public static final String DEFAULT_FORMAT_DATE = "yyyy-MM-dd";

	/** 常量 DEFAULT_FORMAT_TIME. */
	public static final String DEFAULT_FORMAT_TIME = "HH:mm:ss.SSS";

	/** 常量 DEFAULT_FORMAT_TIMESTAMP. */
	public static final String DEFAULT_FORMAT_TIMESTAMP = "yyyy-MM-dd HH:mm:ss.SSS";

	/** 常量 DEFAULT_FORMAT. */
	public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 采用默认的"yyyy-MM-dd HH:mm:ss"格式化时间戳成字符串.
	 *
	 * @param time the time
	 * @return the string
	 */
	public static String formatDate(long time) {
		return formatDate(time, DEFAULT_FORMAT);
	}
	
	
	/**
	 *采用默认的"yyyy-MM-dd HH:mm:ss"格式化时间戳成Date类型.
	 *
	 * @param time the time
	 * @return the date
	 */
	public static Date parseDate(String time) {
		return parseDate(time, DEFAULT_FORMAT);
	}
	
	

	/**
	 * 采用指定的format格式化时间戳成Date类型.
	 *
	 * @param time the time
	 * @param format the format
	 * @return the date
	 */
	public static Date parseDate(String time, String format) {
		return parseDate(time, format, Locale.getDefault());
	}

	/**
	 * 采用指定的format、locale格式化时间字符串成Date类型.
	 *
	 * @param time the time
	 * @param format the format
	 * @param locale 地区
	 * @return the date
	 */
	public static Date parseDate(String time, String format, Locale locale) {
		if (format == null) {
			format = DEFAULT_FORMAT;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
		try {
			return sdf.parse(time);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 采用指定的format格式化时间戳成String类型.
	 *
	 * @param time the time
	 * @param format the format
	 * @return the string
	 */
	public static String formatDate(long time, String format) {
		return formatDate(time, format, Locale.getDefault());
	}
	
	
	/**
	 * 采用指定的format格式化指定时间成String类型.
	 *
	 * @param d the d
	 * @param formatStr the format str
	 * @return the string
	 */
	public static  String format(Date d,String formatStr) {
		if(d==null) return "";
		return new SimpleDateFormat(formatStr).format(d);
	}


	/**
	 * 采用指定的format、locale格式化时间字符串成String类型.
	 *
	 * @param time the time
	 * @param format the format
	 * @param locale the locale
	 * @return the string
	 */
	public static String formatDate(long time, String format, Locale locale) {
		Date date = new Date(time);
		if (format == null) {
			format = DEFAULT_FORMAT;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
		return sdf.format(date); 
	}

	/**
	 * 返回本周的周一，返回格式为"yyyy-MM-dd"的字符串.
	 *
	 * @return monday of this week 属性
	 */
	public static String getMondayOfThisWeek() {
		Calendar c = Calendar.getInstance();
		int dayofweek = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (dayofweek == 0)
			dayofweek = 7;
		c.add(Calendar.DATE, -dayofweek + 1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(c.getTime());
	}
	/**
	 * 返回指定日期的周一，返回格式为"yyyy-MM-dd"的字符串.
	 * 
	 * @param day 指定日期
	 * @return monday of this week 属性
	 */
	public static String getMondayOfWeek(Date day) {
		Calendar c = Calendar.getInstance();
		c.setTime(day);
		int dayofweek = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (dayofweek == 0)
			dayofweek = 7;
		c.add(Calendar.DATE, -dayofweek + 1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(c.getTime());
	}
	/**
	 * 返回本周的周末，返回格式为"yyyy-MM-dd"的字符串.
	 *
	 * @return sunday of this week 属性
	 */
	public static String getSundayOfThisWeek() {
		Calendar c = Calendar.getInstance();
		int dayofweek = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (dayofweek == 0)
			dayofweek = 7;
		c.add(Calendar.DATE, -dayofweek + 7);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(c.getTime());
	}
	/**
	 * 返回本周的周末，返回格式为"yyyy-MM-dd"的字符串.
	 *
	 * @return sunday of this week 属性
	 */
	public static String getSundayOfThisWeek(Date day) {
		Calendar c = Calendar.getInstance();
		c.setTime(day);
		int dayofweek = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (dayofweek == 0)
			dayofweek = 7;
		c.add(Calendar.DATE, -dayofweek + 7);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(c.getTime());
	}
	/**
	 * 返回当前月份的最后一天，返回格式为"yyyy-MM-dd"的字符串.
	 *
	 * @return last date of month 属性
	 */
	public static String getLastDateOfMonth() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date dt = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH, days);
		String result = format.format(cal.getTime());
		return result;
	}

	/**
	 * 返回当前月份的第一天，返回格式为"yyyy-MM-dd"的字符串.
	 *
	 * @return frist date of month 属性
	 */
	public static String getFristDateOfMonth() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date dt = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		int days = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH, days);
		String result = format.format(cal.getTime());
		return result;
	}
	
	/**
	 * 主函数.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){
	}
}