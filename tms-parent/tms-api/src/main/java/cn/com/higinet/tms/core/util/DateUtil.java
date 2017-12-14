/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 ** @author Administrator
 * @date 2011-4-26
 * @description 系统日期类
 */
public class DateUtil {
	/**
	 * 根据月份得到相应季度
	 * 
	 * @param month
	 * @return
	 */
	public static int curSeason(int month) {
		int season = 0;
		if (month % 3 != 0) {
			season = month / 3 + 1;
		} else {
			season = month / 3;
		}
		return season;
	}



	// 日期字符串转为时间戳
	public static Timestamp parseToTimestamp(String date) {
		Timestamp ts = new Timestamp(new Date().getTime());
		try {
			if (date != null && !(date.trim().equals(""))) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date d = sdf.parse(date);
				ts = new Timestamp(d.getTime());
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ts;
	}

	/**
	 * 下一天
	 * 
	 * @param today
	 * @return
	 */
	public static Timestamp nextDay(Timestamp today) {
		Timestamp t = new Timestamp(today.getTime() + 1000 * 60 * 60 * 24);
		return t;
	}
	
	/**
	 * 把一种格式的日期转为另一种格式
	 * 
	 * @param date
	 * @param pattern1
	 * @param pattern2
	 * @return
	 */
	public static String dateConvert(String date, String pattern1, String pattern2) {
		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat(pattern1);
			SimpleDateFormat sdf2 = new SimpleDateFormat(pattern2);
			return sdf2.format(sdf1.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 把一种格式的日期转为另一种格式
	 * 
	 * @param date
	 * @param pattern1
	 * @param pattern2
	 * @return
	 */
	public static String dateConvert(Date date, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}
	
	public static Timestamp yesterday(Date today){
		return new Timestamp(today.getTime()-24*60*60*1000);
	}
	
	public static Timestamp stringToTimestamp(String timestampStr, String format) {
		if (timestampStr == null || timestampStr.trim().equals(" ")) {
			return null;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		Date date = new Date();
		try {
			date = dateFormat.parse(timestampStr);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return new Timestamp(date.getTime());
	}
	
	
	
	/**
	 * 字符串转日期
	 * @param str
	 * @return
	 */
	public static Date StrToDate(String str) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			if(str.indexOf(":") == -1) {
				str = str + " 00:00:00";
			}
			date = format.parse(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;
	}
	
	/**
	 * 字符串转日期
	 * @param str
	 * @return
	 */
	public static Date StrToDate(String str, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		Date date = null;
		try {
			if(str.indexOf(":") == -1) {
				str = str + " 00:00:00";
			}
			date = formatter.parse(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;
	}
	
	/**
	 * 日期转成字符串
	 * @param myDate
	 * @param format yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String fotmatDate(Date myDate, String format) {
		if (myDate == null) {
			return "";
		} else {
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			String dateStr = formatter.format(myDate);
			return dateStr;
		}
	}
	
	/**
	 * 日期转成字符串
	 * @param myDate
	 * @param format yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String fotmatDateFinal(Date myDate) {
		if (myDate == null) {
			return "";
		} else {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateStr = formatter.format(myDate);
			return dateStr;
		}
	}
	
	/**
	 * 日期转成数值型
	 * @param myDate
	 * @param format yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static Long dateToLong (Date myDate){
		long time = 0l;
		if (myDate == null){
			return time;
		}else {
			SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				time = df.parse(df.format(myDate)).getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return time;
		}
	}
	


	
	
	public static void main(String[] args) {
		DateUtil sys = new DateUtil();
		Timestamp tm = sys.stringToTimestamp("2011-04-27", "yyyy-MM-dd");
		Timestamp ps = sys.parseToTimestamp("2011-04-27");
		System.out.println(ps);
	}

}
