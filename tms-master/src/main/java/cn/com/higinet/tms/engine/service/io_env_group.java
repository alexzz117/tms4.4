package cn.com.higinet.tms.engine.service;

import java.util.Map;
import java.util.TreeMap;

import cn.com.higinet.tms.engine.comm.Response;
import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.core.concurrent.counter;

public class io_env_group extends io_env
{
	Map<String, Object> m_req_map, m_res_map;
	counter m_counter;

	public io_env_group(Map<String, Object> m, Response res,counter c)
	{
		super(null, res);
		m_req_map = m;
		m_res_map = new TreeMap<String, Object>();
		m_counter=c;
		if(c!=null)
			c.inc();
	}

	@Override
	public void done()
	{
		done(null);
	}

	@Override
	public void done(Exception ex)
	{
		if(m_counter!=null)
			m_counter.dec();
	}

	@Override
	public void setData(String name, Object value)
	{
		m_res_map.put(name, value);
	}
	
	public String getResData(String name)
	{
		return str_tool.to_str(m_res_map.get(name));
	}

	String return_code;

	@Override
	public void setReturnCode(String code)
	{
		return_code = code;
	}

	public TreeMap<String, String> getParameterMap()
	{
		return map_cast(this.m_req_map);
	}

	public String getReturnCode() {
		return return_code;
	}
}
