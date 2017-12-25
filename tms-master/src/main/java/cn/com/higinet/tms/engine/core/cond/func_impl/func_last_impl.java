package cn.com.higinet.tms.engine.core.cond.func_impl;



import cn.com.higinet.tms.engine.core.cond.func;
import cn.com.higinet.tms.engine.run.run_env;

public class func_last_impl implements func
{
	public Object exec(Object[] p, int n)
	{
		int field_id = ((Number) p[n]).intValue();
		run_env re = (run_env) p[n - 1];

		return re.last_field(field_id);
	}
}
