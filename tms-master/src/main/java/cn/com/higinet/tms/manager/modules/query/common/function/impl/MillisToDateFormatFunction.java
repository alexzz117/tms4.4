package cn.com.higinet.tms.manager.modules.query.common.function.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.query.common.function.Function;

/**
 * 转换函数
 * 格林时间转换成时间字符
 */
@Service("MillisToDateFormatFunction")
public class MillisToDateFormatFunction implements Function {
	private static final Log log = LogFactory.getLog(MillisToDateFormatFunction.class);
	
	public Object execute(Object... args) {
		String value = String.valueOf(args[0]);
		try {
			if (CmcStringUtil.isBlank(value)) {
				return "";
			}
			String[] params = (String[]) args[1];
			String param = params[0];//value的格式
			return CalendarUtil.parseTimeMillisToDateTime(Long.valueOf(value), param);
		}catch (Exception e) {
			log.error("Millis To Date Format error.", e);
			return value;
		}
	}

}
