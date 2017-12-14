package cn.com.higinet.tms.core.common;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public final class date_tool
{
	public static final String FMT_DTS="yyyy-MM-dd HH:mm:ss.SSS";
	public static final String FMT_DT="yyyy-MM-dd HH:mm:ss";
	public static final String FMT_D="yyyy-MM-dd";
	public static final String FMT_T="HH:mm:ss";
	
	public static final int raw_offset=java.util.TimeZone.getDefault().getRawOffset();
	public static  String now(String fmt)
	{
		return 
		new java.text.SimpleDateFormat(fmt)
		.format(java.util.Calendar.getInstance().getTime()); 
	}
	
	public static  String now()
	{
		return now(FMT_DT);
	}

	public static  String now_date()
	{
		return now(FMT_D);
	}
	public static  String now_time()
	{
		return now(FMT_T);
	}
	
	public static  java.util.Date parse(String str,String fmt)
	{
		if(str==null)
			return null;
		try
        {
	        return 
	        new java.text.SimpleDateFormat(fmt).parse(str);
        }
        catch (ParseException e)
        {
	        e.printStackTrace();
        }
		
        return  null;
 	}
	
	static final Pattern pattern=Pattern.compile("(\\d{1,4}-\\d{1,2}-\\d{1,2})?(?:\\s+)?(\\d{1,2}:\\d{1,2}:\\d{1,2})?(\\.\\d{1,3})?"); 
	public static  java.util.Date parse(String str)
	{
		if(str==null)
			return null;
		
		java.util.regex.Matcher m=pattern.matcher(str);
		if(!m.matches())
			return null;
		String ymd=m.group(1);
		String hms=m.group(2);
		String mil=m.group(3);
		
		String fmt=null;
		if(ymd!=null && hms!=null)
			fmt=mil==null?date_tool.FMT_DT:date_tool.FMT_DTS;
		else if(ymd==null)
			fmt=date_tool.FMT_T;
		else
			fmt=date_tool.FMT_D;
		
		return date_tool.parse(str, fmt);
	}
	
	public static  java.util.Date parse_date(String str)
	{
		return parse(str,FMT_D);
	}
	
	public static  String format(String str,String formatStr)
	{
		if(str==null)
			return "";
		return format(parse(str),formatStr);
	}
	
	public static  String format(Date d,String formatStr)
	{
		if(d==null)
			return "";
		
		return new SimpleDateFormat(formatStr)
		 .format(d);
	}
	
	public static  String format(Date d)
	{
		if(((d.getTime()+raw_offset)%(1000*24*60*60))==0)
			return format(d,FMT_D);
		else 
		if(((d.getTime()+raw_offset)%1000)==0)
			return format(d,FMT_DT);
		else
			return format(d,FMT_DTS);
	}
	
	public static void main(String[] arg)
	{
//		System.out.println(date_tool.parse("2000-1-1"));
//		System.out.println(date_tool.parse("2000-1-1 18:00:22"));
		System.out.println(date_tool.parse("2000-1-32 25:00:22.11"));
//		System.out.println(date_tool.parse("18:00:22"));
		
	}
}
