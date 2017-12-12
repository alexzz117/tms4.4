package cn.com.higinet.tms35.manage.query.common.function.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms35.manage.query.common.function.Function;

@Service("getCurrentOperator")
public class CurrentOperatorFunction implements Function {

	@Autowired
	private  HttpServletRequest request;
	
	@Override
	@SuppressWarnings("unchecked")
	public Object execute(Object... args) {
		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute("OPERATOR");
		if (operator != null)
			return operator.get("OPERATOR_ID");
		return null;
	}
}