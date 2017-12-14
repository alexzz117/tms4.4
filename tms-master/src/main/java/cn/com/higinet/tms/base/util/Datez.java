package cn.com.higinet.tms.base.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Datez extends DateUtil {
	
	/**
	 * 返回XX天后的时间字符串
	 * 
	 * @param date 时间字符串
	 * @param format 输入与输出时间格式
	 * @param day 增加的天数
	 * 
	 * */
	public static String addDay( String date, String format, int day ) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat( format );
		Date _date = sdf.parse( date );
		Calendar c = Calendar.getInstance();
		c.setTime( _date );
		c.add( Calendar.DATE, day );
		return sdf.format( c.getTime() );
	}
}
