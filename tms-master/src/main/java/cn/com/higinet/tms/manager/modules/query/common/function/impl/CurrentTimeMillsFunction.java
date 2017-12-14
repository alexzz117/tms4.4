package cn.com.higinet.tms.manager.modules.query.common.function.impl;

import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.modules.query.common.function.Function;

@Service("getCurrentTimeMills")
public class CurrentTimeMillsFunction implements Function {

	@Override
	public Object execute(Object... args) {
		return String.valueOf(System.currentTimeMillis());
	}
}