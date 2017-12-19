package cn.com.higinet.tms.engine.comm;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

public class ref_cache
{
	ConcurrentMap<String, List<String>> m_delegate;
	List<String> m_name_list;
	String m_tab_name;

	// static class wei implements Weigher<tm_stamp>
	// {
	//
	// public int weightOf(tm_stamp arg0)
	// {
	// return (int)(tm_tool.lctm_ms()-arg0.timestamp());
	// }
	// }

	public ref_cache(String tab_name, List<String> name_list, int max_size)
	{
		m_delegate = new ConcurrentLinkedHashMap.Builder<String, List<String>>()//
				.initialCapacity(max_size)//
				.maximumWeightedCapacity(max_size).build();
		m_tab_name = tab_name;
		name_list = m_name_list;
	}

	public final List<String> get(String k)
	{
		return m_delegate.get(k);
	}

	public final void put(String k, List<String> v)
	{
		m_delegate.put(k, v);
	}
	
	public final String get_v(List<String> buf, String fd_name)
	{
		if (buf == null)
			return null;
		int id = Collections.binarySearch(this.m_name_list, fd_name);
		if (id == -1)
			return null;
		synchronized (buf)
		{
			return buf.get(id);
		}
	}

	public final void put_v(List<String> buf, String fd_name, String value)
	{
		if (buf == null)
			return;
		int id = Collections.binarySearch(this.m_name_list, fd_name);
		if (id == -1)
			return;
		synchronized (buf)
		{
			buf.set(id, value);
		}
	}
}
