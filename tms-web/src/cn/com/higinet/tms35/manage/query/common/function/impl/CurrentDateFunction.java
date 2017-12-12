package cn.com.higinet.tms35.manage.query.common.function.impl;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms35.manage.common.util.CalendarUtil;
import cn.com.higinet.tms35.manage.query.common.function.Function;

/**
 * 取值函数
 * 获取系统时间函数实现类
 */
@Service("getDateFunction")
public class CurrentDateFunction implements Function {

	private static final Log log = LogFactory.getLog(DateFormatFunction.class);

	@SuppressWarnings("unused")
	public Object execute(Object... args) {
		
		String value = (String) args[0];
		try {
			if(args.length == 1) return CalendarUtil.getCurrentDateTime();//当前时间
			
			String[] params = (String[]) args[1];
			if(params.length == 1){
				return CalendarUtil.getCalendarByFormat(params[0]);//自定义格式的时间
			}else{
				String pattern1 = params[1];//前后
				if("before".equals(pattern1)){//前多少表
					return CalendarUtil.getBeforeDateBySecond(params[0], Long.valueOf(params[2]));
				}else if("after".equals(pattern1)){//后多少表
					return CalendarUtil.getAfterDateBySecond(params[0], Long.valueOf(params[2]));
				}else if("startDay".equals(pattern1)){
					return CalendarUtil.parseTimeMillisToDateTime(getTimesmorning(), params[0]);
				}else if("endDay".equals(pattern1)){
					return CalendarUtil.parseTimeMillisToDateTime(getTimesnight() - 1, params[0]);
				}
			}
			return CalendarUtil.getCurrentDateTime();
		} catch (Exception e) {
			log.error("get Date error.", e);
			return CalendarUtil.getCurrentDateTime();
		}
	}
	
	public static long getTimesmorning(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
		//获得当天24点时间
	public static long getTimesnight(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 24);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
}
