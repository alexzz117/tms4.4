package cn.com.higinet.tms35.manage.aml.function.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Service;

import cn.com.higinet.tms35.manage.aml.function.Function;

@Service("dateFormat")
public class DataFormat implements Function {

	@Override
	public Object execute(Object srcData, Object... args) {
		SimpleDateFormat sdf = new SimpleDateFormat(String.valueOf(args[0]).trim());
		return sdf.format(new Date(Long.parseLong(String.valueOf(srcData))));
	}
}
