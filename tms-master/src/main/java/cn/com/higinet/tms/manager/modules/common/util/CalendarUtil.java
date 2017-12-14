package cn.com.higinet.tms.manager.modules.common.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.com.higinet.tms.manager.modules.exception.TmsMgrServiceException;

/**
 * 当前系统日期各种格式处理帮助类
 * 
 * @author lifx
 * 
 */
public class CalendarUtil {
	private static final Log log = LogFactory.getLog(CalendarUtil.class);
    public static SimpleDateFormat FORMAT1 = new SimpleDateFormat("yyyy");

    public static SimpleDateFormat FORMAT2 = new SimpleDateFormat("MM");

    public static SimpleDateFormat FORMAT3 = new SimpleDateFormat("dd");

    public static SimpleDateFormat FORMAT4 = new SimpleDateFormat("HH");

    public static SimpleDateFormat FORMAT5 = new SimpleDateFormat("mm");

    public static SimpleDateFormat FORMAT6 = new SimpleDateFormat("s");

    public static SimpleDateFormat FORMAT7 = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    public static SimpleDateFormat FORMAT8 = new SimpleDateFormat(
            "yyyy/MM/dd");

    public static SimpleDateFormat FORMAT9 = new SimpleDateFormat(
            "yyyyMMddHHmmss");
    
    public static SimpleDateFormat FORMAT21 = new SimpleDateFormat(
    "HHmmssSS");

    public static SimpleDateFormat FORMAT10 = new SimpleDateFormat(
            "yyyyMMddHHmmss");

    public static SimpleDateFormat FORMAT11 = new SimpleDateFormat(
            "yyyy-MM-dd");

    public static SimpleDateFormat FORMAT12 = new SimpleDateFormat(
            "yyyyMMddHHmmssSSS");

    public static SimpleDateFormat FORMAT13 = new SimpleDateFormat(
            "yyyyMMddHHmmssSS");
    
    public static SimpleDateFormat FORMAT14 = new SimpleDateFormat(
    "yyyy-MM-dd HH:mm:ss");
    
    public static SimpleDateFormat FORMAT15 = new SimpleDateFormat("yyyy年MM月dd日");
    
    public static SimpleDateFormat FORMAT16 = new SimpleDateFormat(
    "yyyy-MM-dd HH:mm:ss.M");
    
    public static SimpleDateFormat FORMAT17 = new SimpleDateFormat(
    "yyyyMMdd");
    public static SimpleDateFormat FORMAT18 = new SimpleDateFormat(
    "yyyy-MM-dd HH:mm:ss SS");
    
    public static SimpleDateFormat FORMAT19 = new SimpleDateFormat(
    "yyyyMMddHH");    
    public static SimpleDateFormat FORMAT20 = new SimpleDateFormat(
    "yyyyMMddHHmm");
    
    public static SimpleDateFormat FORMAT22 = new SimpleDateFormat("ss");//秒
    public static SimpleDateFormat FORMAT23 = new SimpleDateFormat("mmss");//分秒
    public static SimpleDateFormat FORMAT24 = new SimpleDateFormat("HHmmss");//时分秒
    public static SimpleDateFormat FORMAT25 = new SimpleDateFormat("ddHHmmss");//天时分秒
    public static SimpleDateFormat FORMAT26 = new SimpleDateFormat("yyyyMM");//年月
    /**
     * 转换英文日期到中文日期 2007年7月20日
     */
    public static String changeENCalendarToCNCalendar(String enCalendar){
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT15.toPattern());
        return sdf.format(Timestamp.valueOf(enCalendar+" 00:00:00"));
    }
    
    /**
     * 转换英文日期到中文日期 2007年7月20日
     */
    public static String changeENCalendarToCNCalendar(Timestamp enCalendar){
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT15.toPattern());
        return sdf.format(enCalendar);
    } 
    
    /**
     * 转换英文日期到中文日期 2007年7月20日
     */
    public static String changeCalendar(SimpleDateFormat format,Timestamp enCalendar){
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(format.toPattern());
        return sdf.format(enCalendar);
    } 
    
    /**
     * 取到当前的年份 2007年7月20日
     */
    public static String getCNCalendarByFormat(){
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT15.toPattern());
        return sdf.format(getCurrentTime());
    }
    /**
     * 取到当前的年份
     */
    public static String getCurrYear() {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT1.toPattern());
        return sdf.format(getCurrentTime());
    }

    /**
     * 取到当前的月份
     */
    public static String getCurrMonth() {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT2.toPattern());
        return sdf.format(getCurrentTime());
    }

    /**
     * 取到当前的日
     */
    public static String getCurrDay() {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT3.toPattern());
        return sdf.format(getCurrentTime());
    }

    /**
     * 取到当前的时间的小时
     */
    public static String getCurrHour() {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT4.toPattern());
        return sdf.format(getCurrentTime());
    }
    
    public static String getDefHour(Timestamp t) {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT4.toPattern());
        return sdf.format(t);
    }

    /**
     * 取到当前的时间的分钟
     */
    public static String getCurrMinute() {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT5.toPattern());
        return sdf.format(getCurrentTime());
    }
    public static String getDefMinute(Timestamp t) {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT5.toPattern());
        return sdf.format(t);
    }
    /**
     * 取到当前的时间的秒
     */
    public static String getCurrSecond() {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT6.toPattern());
        return sdf.format(getCurrentTime());
    }
    
    /**
     * 取到当前的时间的年月日
     */
    public static String getCurrYearMonthDay() {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT11.toPattern());
        return sdf.format(getCurrentTime());
    }

    /**
     * 根据格式化类型取到当前的系统时间
     */
    public static String getCalendarByFormat(SimpleDateFormat sdf) {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf1 = new SimpleDateFormat(sdf.toPattern());
        return sdf1.format(getCurrentTime());
    }

    /**
     * 根据格式化类型取到当前的系统时间
     */
    public static String getSynchronizedCalendarByFormat(
            SimpleDateFormat sdf) {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf1 = new SimpleDateFormat(sdf.toPattern());
        return sdf1.format(getCurrentTime());
    }

    /**
     * 根据格式化类型取到当前的系统时间
     */
    public static String getCalendarByFormat(String str) {
        SimpleDateFormat format = new SimpleDateFormat(str);
        return format.format(getCurrentTime());
    }


    /**
     * 两个时间间隔
     */
    public static long timeDistance(String start,String end) {
    	return Timestamp.valueOf(end).getTime() - Timestamp.valueOf(start).getTime();
    }

    /**
     * 两个时间间隔
     */
    public static long timeDistance(String start,String end,SimpleDateFormat sdf) {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf1 = new SimpleDateFormat(sdf.toPattern());
    	SimpleDateFormat sdf2 = new SimpleDateFormat(sdf.toPattern());
    	try {
			return sdf1.parse(end).getTime() - sdf2.parse(start).getTime();
		} catch (ParseException e) {
			throw new TmsMgrServiceException("日期String转Date异常！",e);
		}
    }
    /**
     * 系统当前时间
     * 
     * @return
     */
    public static String getCurrentDateTime() {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf1 = new SimpleDateFormat(FORMAT7.toPattern());
        return sdf1.format(getCurrentTime());
    }
    public static Timestamp getCurrentTime() {
        Timestamp t = new Timestamp(System.currentTimeMillis());
        return t;
    }
    /**
     * 格林时间转换成时间戳
     * @param timeMillis	格林时间
     * @return
     */
    public static Timestamp parseTimeMillisToStamp(long timeMillis){
    	Timestamp t = new Timestamp(timeMillis);
        return t;
    }
    
    /**
     * 时间戳转换成时间格式
     * @param time	时间戳
     * @param sdf	格式
     * @return
     */
    public static String getSynchronizedDateTimeByTimeStamp(Timestamp time, SimpleDateFormat sdf){
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf1 = new SimpleDateFormat(sdf.toPattern());
        return sdf1.format(time);
    }
    
    /**
     * 时间戳转换成时间格式
     * @param time	时间戳
     * @param str	格式字符
     * @return
     */
    public static String getDateTimeByTimeStamp(Timestamp time, String str){
    	SimpleDateFormat format = new SimpleDateFormat(str);
        return format.format(time);
    }
    
    /**
     * 格林时间转换成时间字符
     * @param time	格林时间
     * @param str	格式字符
     * @return
     */
    public static String parseTimeMillisToDateTime(long time, String str){
    	return getDateTimeByTimeStamp(parseTimeMillisToStamp(time), str);
    }
    
    /**
     * 字段时间转换成格林时间
     * @param date		时间字符
     * @param str		格式字符
     * @return
     */
    public static long parseStringToTimeMillis(String date, String str){
    	SimpleDateFormat sdf = new SimpleDateFormat(str);
    	try {
			Date _date = sdf.parse(date);
			return _date.getTime();
		} catch (ParseException e) {
			throw new TmsMgrServiceException("日期String转Millis异常！",e);
		}
    }
    
    /**
     * 字段时间转换成格林时间
     * @param str		时间字符
     * @param format	字符格式
     * @return
     */
    public static long parseSynchronizedStringToTimeMillis(String str, SimpleDateFormat format){
    	SimpleDateFormat sdf = new SimpleDateFormat(format.toPattern());
    	try {
			Date date = sdf.parse(str);
			return date.getTime();
		} catch (ParseException e) {
			throw new TmsMgrServiceException("日期String转Millis异常！",e);
		}
    }

    public static long nextDay(long date,int days) {
    	Calendar   c   =   Calendar.getInstance();
		c.setTimeInMillis(date);
		c.add(Calendar.DAY_OF_MONTH, days);
		return c.getTime().getTime();
    }
    /**
     * 当前日期的前多少秒时间
     * 
     * @param second
     * @return
     */
    public static String getBeforeDateBySecond(
            SimpleDateFormat sdf, 
            long second) {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf1 = new SimpleDateFormat(sdf.toPattern());
        Timestamp t = new Timestamp(System.currentTimeMillis() - second * 1000);
        return sdf1.format(t);
    }

    /**
     * 当前日期的前多少秒时间
     * 
     * @param second
     * @return
     */
    public static String getBeforeDateBySecond(String format, long second) {
        Timestamp t = new Timestamp(System.currentTimeMillis() - second * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(t);
    }

    /**
     * 某个日期的前多少秒时间
     * 
     * @param second
     * @return
     */
    public static String getBeforeDateBySecond(
            SimpleDateFormat sdf, String timeStr,
            long second) {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf1 = new SimpleDateFormat(sdf.toPattern());
    	Timestamp time = Timestamp.valueOf(timeStr);
        Timestamp t = new Timestamp(time.getTime() - second * 1000);
        return sdf1.format(t);
    }

    /**
     * 当前日期的后多少秒时间
     * 
     * @param second
     * @return
     */
    public static String getAfterDateBySecond(
            SimpleDateFormat sdf, 
            long second) {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf1 = new SimpleDateFormat(sdf.toPattern());
        Timestamp t = new Timestamp(System.currentTimeMillis() + second * 1000);
        return sdf1.format(t);
    }

    /**
     * 当前日期的后多少秒时间
     * 
     * @param second
     * @return
     */
    public static String getAfterDateBySecond(String format, long second) {
        Timestamp t = new Timestamp(System.currentTimeMillis() + second * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(t);
    }

    /**
     * 当前日期的后多少秒时间
     * 
     * @param second
     * @return
     */
    public static String getAfterDateBySecond(
            SimpleDateFormat sdf, String timeStr,
            long second) {
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf1 = new SimpleDateFormat(sdf.toPattern());
    	Timestamp time = Timestamp.valueOf(timeStr);
        Timestamp t = new Timestamp(time.getTime() + second * 1000);
        return sdf1.format(t);
    }
    
    /**
     * 转换英文日期到中文日期 2007年7月20日
     */
    public static String changeCalendarByFormat(Timestamp timesamp,SimpleDateFormat format){
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(format.toPattern());
        return sdf.format(timesamp);
    }
    
    public static Date parseStringToDate(String date,SimpleDateFormat format){
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(format.toPattern());
    	try {
			return sdf.parse(date);
		} catch (ParseException e) {
			throw new TmsMgrServiceException("日期String转Date异常！",e);
		}
    }
    
    public static Date parseStringToDate(String date){
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT11.toPattern());
    	try {
			return sdf.parse(date);
		} catch (ParseException e) {
			throw new TmsMgrServiceException("日期String转Date异常！",e);
		}
    }
    
    public static String transFormFormatToAnother(String time,SimpleDateFormat format1,SimpleDateFormat format2){
    	//压力测试下SimpleDateFormat非线程安全，需要重新创建SimpleDateFormat
    	SimpleDateFormat sdf1 = new SimpleDateFormat(format1.toPattern());
    	SimpleDateFormat sdf2 = new SimpleDateFormat(format2.toPattern());
    	try {
    		return sdf2.format(sdf1.parse(time));
		} catch (ParseException e) {
			throw new TmsMgrServiceException("日期String转Date异常！",e);
		}
    }
    
    public static int getDayOfWeek(Date date) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	int week = calendar.get(Calendar.DAY_OF_WEEK);
    	if(week == 1) {
    		week = 7;
    	}else {
    		week -=1;
    	}
    	return week;
    }
    
    public static String modDateByHour(String dateStr, String h) {
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	SimpleDateFormat format14 = new SimpleDateFormat(FORMAT14.toPattern());
		Date date = null;
		try {
			date = format.parse(dateStr);
			Calendar calendar = Calendar.getInstance();
	    	calendar.setTime(date);
	    	calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(h));  
	    	calendar.set(Calendar.MINUTE, 0);
	    	calendar.set(Calendar.SECOND, 0);
	    	return (format14.format(calendar.getTime()));
		} catch (Exception e) {
			log.error("CalendarUtil execute modDateByHour error!", e);
		}
		return dateStr;
    }
    
	/**
	 * 取某时刻下个月开始第一天 参数为null取当前时刻 maxiao
	 */  
    public static Date getFirstdayOfNextmonth(Timestamp currentTime) {
    	if(currentTime == null)currentTime = getCurrentTime();
		GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(currentTime);
        // 月加1
        gc.add(GregorianCalendar.MONTH, +1);
        // 日期变成1
        gc.set(GregorianCalendar.DAY_OF_MONTH, 1);

		return gc.getTime();
	}
	
	/**
	 * 取某时刻所在月最后一天 参数为null取当前时刻 maxiao
	 */ 
    public static Date getLastdayOfThismonth(Timestamp currentTime) {
    	if(currentTime == null)currentTime = getCurrentTime();
		GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(currentTime);
        // 月加1
        gc.add(GregorianCalendar.MONTH, +1);
        // 日期变成1
        gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
        // 日期减1
        gc.add(GregorianCalendar.DAY_OF_MONTH, -1);

		return gc.getTime();
	}

	//向前N个时间
	public static String getBeforeTime(String date,int days,int field,SimpleDateFormat format){
		if("0".equals(days)){
			return date;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(parseStringToDate(date,format));
		c.set(field, c.get(field) - days);
    	SimpleDateFormat format7 = new SimpleDateFormat(format.toPattern());
		return format7.format(c.getTime());
	}
    
	//向前N个自然日
	public static String getBeforeNaturalDay(String date,int days,SimpleDateFormat format){
		if("0".equals(days)){
			return date;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(parseStringToDate(date,format));
		c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) - days);
    	SimpleDateFormat format7 = new SimpleDateFormat(format.toPattern());
		return format7.format(c.getTime());
	}
	
	//往前n个自然月
	public static String getBeforeNaturalMonth(String date, int month,SimpleDateFormat format) {
		
		if("0".equals(month)){
			return date;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(parseStringToDate(date,format));
		c.set(Calendar.MONTH, c.get(Calendar.MONTH) - month);
    	SimpleDateFormat format7 = new SimpleDateFormat(format.toPattern());
		return format7.format(c.getTime());
	}	
	
	//向前N个年
	public static String getBeforeNaturalYear(String date,int years,SimpleDateFormat format){
		if("0".equals(years)){
			return date;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(parseStringToDate(date,format));
		c.set(Calendar.YEAR, c.get(Calendar.YEAR) - years);
    	SimpleDateFormat format7 = new SimpleDateFormat(format.toPattern());
		return format7.format(c.getTime());
	}
    
	//向前N个自然日
	public static String getBeforeNaturalDay(String date,int days){
		return getBeforeNaturalDay(date,days,FORMAT7);
	}
	
	//往前n个自然月
	public static String getBeforeNaturalMonth(String date, int month) {
		return getBeforeNaturalMonth(date,month,FORMAT7);
	}	
	
	//向前N个年
	public static String getBeforeNaturalYear(String date,int years){
		return getBeforeNaturalYear(date,years,FORMAT7);
	}
	
	// 通过出生日期计算年龄
	public static int getAge(Date birthDay) throws Exception {
        Calendar cal = Calendar.getInstance();

        if (cal.before(birthDay)) {
            throw new IllegalArgumentException(
                "出生时间大于当前时间!");
        }

        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH) + 1;
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(birthDay);

        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;

        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) {
                    age--;
                } 
            } else {
                age--;
            }
        } 

        return age;
    }

	
     public static void main(String[] aa){
//    	Calendar c1 = Calendar.getInstance();
//		c1.setTime(CalendarUtil.parseStringToDate("2011-08-29 10:10:10", CalendarUtil.FORMAT7));
		//Long.valueOf(timeMax) < (timeDistance/1000*60*60*24.0)
    	//System.out.println(getBeforeTime("2011-08-31 10:10:10",2,Calendar.MINUTE,FORMAT8));
    	System.out.println(parseTimeMillisToDateTime(1354892521000L, FORMAT18.toPattern()));
//    	String dateStr = "20110714150150";
//    	String ss = "abcdabcdabcd";
//    	String er = "abcdabcdabcd";
//    	
//    	long l = Long.MAX_VALUE;
//    	
//    	System.out.println(ss.compareTo(er));
//    	
//    	System.out.println(new BigInteger(ss.getBytes()).longValue());
//    	System.out.println(new BigInteger(er.getBytes()).longValue());
//    	System.out.println(Long.MAX_VALUE);
    }
}
