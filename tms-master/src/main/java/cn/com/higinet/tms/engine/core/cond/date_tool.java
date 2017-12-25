package cn.com.higinet.tms.engine.core.cond;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class date_tool
{
	static Logger log=LoggerFactory.getLogger(date_tool.class);
	public static final String FMT_DTS="yyyy-MM-dd HH:mm:ss.SSS";
	public static final String FMT_DT="yyyy-MM-dd HH:mm:ss";
	public static final String FMT_DT0="yyyy-MM-dd HH:mm";
	public static final String FMT_D="yyyy-MM-dd";
	public static final String FMT_T="HH:mm:ss";
	public static final String FMT_T0="HH:mm";
	
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
        	log.error(null,e);
        }
		
        return  null;
 	}
	
	static final Pattern pattern=Pattern.compile("(\\d{1,4}-\\d{1,2}-\\d{1,2})?(?:\\s+)?(\\d{1,2}\\:\\d{1,2}(\\:\\d{1,2}(\\.\\d{1,3})?)?)?"); 
	public static  java.util.Date parse(String str)
	{
		if(str==null)
			return null;
		
		java.util.regex.Matcher m=pattern.matcher(str);
		if(!m.matches())
			return null;
		String ymd=m.group(1);
		String hm=m.group(2);
		String sec=m.group(3);
		String mil=m.group(4);
		
		String fmt=null;
		if(ymd!=null && hm!=null && sec!=null && mil!=null)
			fmt=date_tool.FMT_DTS;
		else if(ymd!=null && hm!=null && sec!=null)
			fmt=date_tool.FMT_DT;
		else if(ymd!=null && hm!=null)
			fmt=date_tool.FMT_DT0;
		else if(ymd!=null)
			fmt=date_tool.FMT_D;
		else if(hm!=null && sec!=null)
			fmt=date_tool.FMT_T;
		else
			fmt=date_tool.FMT_T0;
		
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
		//由于夏令时原因，为了对准日期，使用如下实现，如想修改，请测试[1986,1991]年之间的夏令时时间是否正确
		String ret=format(d,FMT_DTS);
		if(ret.endsWith(" 00:00:00.000"))
			return ret.substring(0,10);

		if(ret.endsWith(".000"))
			return ret.substring(0,19);

		return ret;
	}
	
	public static long tm_date(long d)
	{
		d=raw_offset+d;
		return d/(1000*24*60*60)*(1000*24*60*60)-raw_offset;
	}
	
	public static long tm_time(long d)
	{
		return (d-raw_offset)-tm_date(d);
	}

	public static void test(String d)
	{
		System.out.print(date_tool.format(date_tool.parse(d)));
		System.out.print("\t");
		System.out.println(date_tool.parse(d).getTime());
	}
	
	public static void test(long ld)
	{
		String d=date_tool.format(new Date(ld));
		System.out.print(date_tool.format(date_tool.parse(d)));
		System.out.print("\t");
		System.out.println(date_tool.parse(d).getTime());
	}
	
	public static void main(String[] arg)
	{
		for(int i=1000;i<2012;i++)
			test(i+"-06-28");
	}
}
