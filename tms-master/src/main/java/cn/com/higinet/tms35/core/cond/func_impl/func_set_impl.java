package cn.com.higinet.tms35.core.cond.func_impl;

import cn.com.higinet.tms35.core.cond.func;
import cn.com.higinet.tms35.run.run_env;

/**
 * 给交易属性赋值
 * @author lining@higinet.com.cn
 * @version 4.0
 * @since 2014-8-29 下午3:41:13
 * @description
 */
public class func_set_impl implements func {

	@Override
	public Object exec(Object[] p, int n) {
		int field_id = ((Number) p[n]).intValue();
		Object set_value = p[n + 1];
		run_env re = (run_env) p[n - 1];
		re.get_fd_value().set_fd(field_id, set_value);
		return new Long(1);
	}
}