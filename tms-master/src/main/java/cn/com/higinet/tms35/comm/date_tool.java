package cn.com.higinet.tms35.comm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;


public class date_tool {
	
	public static long get_before_time(String format, long time, int before) {
		String unit = format.substring(format.length() - 1);
		int field = -1;
		if ("y".equals(unit)) {
			field = GregorianCalendar.YEAR;
		} else if ("M".equals(unit)) {
			field = GregorianCalendar.MONTH;
		} else if ("d".equals(unit)) {
			field = GregorianCalendar.DATE;
		} else if ("H".equals(unit)) {
			field = GregorianCalendar.HOUR_OF_DAY;
		} else if ("m".equals(unit)) {
			field = GregorianCalendar.MINUTE;
		} else if ("s".equals(unit)) {
			field = GregorianCalendar.SECOND;
		} else if ("S".equals(unit)) {
			field = GregorianCalendar.MILLISECOND;
		}
		if (field == -1) return 0;
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(time);
		gc.add(field, -before);
		return time - gc.getTimeInMillis();
	}

	public static long get_before_time(String format, int before) {
		return get_before_time(format, java.util.Calendar.getInstance().getTimeInMillis(), before);
	}
	
	public static String get_date_time(String format, long time) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(time);
	}
	
	public static long get_date_time(String format, String date) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			Date _date = sdf.parse(date);
			return _date.getTime();
		} catch (ParseException e) {
			throw new tms_exception(e);
		}
	}
}
