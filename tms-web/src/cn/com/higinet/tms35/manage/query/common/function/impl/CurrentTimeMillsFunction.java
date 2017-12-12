package cn.com.higinet.tms35.manage.query.common.function.impl;

import org.springframework.stereotype.Service;

import cn.com.higinet.tms35.manage.query.common.function.Function;

@Service("getCurrentTimeMills")
public class CurrentTimeMillsFunction implements Function {

	@Override
	public Object execute(Object... args) {
		return String.valueOf(System.currentTimeMillis());
	}
}