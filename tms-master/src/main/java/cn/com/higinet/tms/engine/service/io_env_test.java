package cn.com.higinet.tms.engine.service;

import java.util.Map;
import java.util.TreeMap;

import cn.com.higinet.tms.engine.core.concurrent.counter;

public class io_env_test extends io_env
{
	TreeMap<String,Object> m_map;
	TreeMap<String,Object> r_map;// 返回数据
	String r_code;// 返回码
	counter m_counter;
	
	public io_env_test(TreeMap<String,Object> m)
	{
		super(null,null);
		m_map=m;
		r_map = new TreeMap<String,Object>();
	}

	public io_env_test(TreeMap<String, Object> m, counter c)
	{
		super(null, null);
		m_map=m;
		r_map = new TreeMap<String,Object>();
		m_counter=c;
		if(c!=null)
			c.inc();
	}
	
	public Map<String, Object> getHeadMap()
	{
		return null;
	}

	@SuppressWarnings("unchecked")
	public TreeMap<String, String> getParameterMap()
	{
		return (TreeMap<String, String>)m_map.clone();
	}

	public Map<String, Object> getParameterOriMap()
	{
		return null;
	}

	public void done()
	{
		done(null);
	}
	
	public void done(Exception ex)
	{
		if(m_counter!=null)
			m_counter.dec();
	}

	public void setData(String name, Object value)
	{
		r_map.put(name, value);
	}

	public void setReturnCode(String code)
	{
		r_code = code;
	}

	public TreeMap<String, Object> getResultData() {
		return r_map;
	}

	public String getResultCode() {
		return r_code;
	}
	
}
