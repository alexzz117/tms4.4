package cn.com.higinet.tms35.manage.query.common.function.impl;

import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms35.manage.common.util.CalendarUtil;
import cn.com.higinet.tms35.manage.query.common.function.Function;

/**
 * 转换函数
 * 日期格式转换函数实现类
 */
@Service("dateFormatFunction")
public class DateFormatFunction implements Function {
	
	private static final Log log = LogFactory.getLog(DateFormatFunction.class);
	
	public Object execute(Object... args) {
		
		String value = (String) args[0];
		try {
			String[] params = (String[]) args[1];
			String pattern1 = params[0];
			String pattern2 = params[1];
			
			String newValue = CalendarUtil.transFormFormatToAnother(value, 
					new SimpleDateFormat(pattern1), new SimpleDateFormat(pattern2));
			return newValue;
		} catch (Exception e) {
			log.error("Date Format error.", e);
			return value;
		}
	}

}
