package cn.com.higinet.tms35.manage.query.common.function.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms35.manage.common.util.CalendarUtil;
import cn.com.higinet.tms35.manage.query.common.function.Function;

/**
 * 转换函数
 * 时间字符转换成格林时间
 */
@Service("DateFormatToMillisFunction")
public class DateFormatToMillisFunction implements Function {
	private static final Log log = LogFactory.getLog(DateFormatToMillisFunction.class);

	public Object execute(Object... args) {
		String value = String.valueOf(args[0]);
		try {
			String[] params = (String[]) args[1];
			String param = params[0];//value的格式
			return CalendarUtil.parseStringToTimeMillis(value, param);
		}catch (Exception e) {
			log.error("Date Format To Millis error.", e);
			return value;
		}
	}

}
