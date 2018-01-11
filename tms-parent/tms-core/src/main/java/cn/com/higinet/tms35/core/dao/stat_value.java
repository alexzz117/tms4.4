package cn.com.higinet.tms35.core.dao;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.core.concurrent.counter;
import cn.com.higinet.tms35.stat.stat_number_encode;
import cn.com.higinet.tms35.stat.stat_win_time;

//对应一条tms_run_stat数据库记录
public class stat_value
{
	private static Logger logger = LoggerFactory.getLogger(stat_value.class);
	static int g_min_save_span = tmsapp.get_config("tms.stat.min_save_span", 0);;// 分钟,比该值小的窗口*窗口数将不再存储到数据库
	public String m_param, m_value;
	Map<Integer, String> m_map = null;

	boolean m_is_indb;

	public stat_value(String param)
	{
		m_is_indb = false;
		m_param = param;
		m_map = new TreeMap<Integer, String>();
	}

	public stat_value(String param, String v)
	{
		m_param = param;
		if (m_map == null)
			m_map = new TreeMap<Integer, String>();
		set_data(m_value = v);
	}

	public Iterator<Map.Entry<Integer, String>> Iterator()
	{
		return m_map.entrySet().iterator();
	}

	public void set_map(Map<Integer, String> m)
	{
		m_map = m;
		this.set_dirty(true);
	}

	public Map<Integer, String> getM_map() {
		return m_map;
	}

	final int skip_(char[] cc, int p, char ch)
	{
		while (p < cc.length && cc[p] != ch)
			p++;
		return p;
	}

	synchronized public void set_data(String v)
	{
		try
		{
			m_map.clear();
			m_value = v;
			if (v == null)
				return;
	
			char[] cc = v.toCharArray();
			int i = 0, p = 0, s = 0;
			for (; p < cc.length;)
			{
				i = p;
				s = skip_(cc, i, ':');
				p = skip_(cc, s + 1, '\n');
				m_map.put(stat_number_encode.decode_int(String.copyValueOf(cc, i, s - i)),
						String.copyValueOf(cc, s + 1, p - s - 1));
				p++;
			}
		}
		catch (Exception e)
		{
			logger.error("stat value string: " + v + ", decode error.", e);
			throw new tms_exception(e);
		}
	}

	synchronized public String get(int id)
	{
		return m_map.get(id);
	}

	boolean m_dirty;

	public boolean is_dirty()
	{
		return m_dirty;
	}

	synchronized public void del(int statId)
	{
		this.set_dirty();
		m_map.remove(statId);
	}

	synchronized public void set(int id, String v)
	{
		String v1 = m_map.get(id);
		if (v1 == v || v1 != null && v1.equals(v))
			return;

		this.set_dirty();
		m_map.put(id, v);
	}

	synchronized public String toString()
	{
		return toString(false);
	}
	
	synchronized public String toString(boolean hasNull)
	{
		if (!is_dirty())
			return this.m_value;

		db_stat.cache dc = db_cache.get().stat();
		StringBuffer sb = new StringBuffer(1024);
		for (Iterator<Entry<Integer, String>> it = m_map.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry<Integer, String> me = it.next();
			if (str_tool.is_empty(me.getValue()) && !hasNull)
				continue;

			db_stat ds = dc.get_by_statid(me.getKey().intValue());
			if (ds == null || ds.stat_num_unit * stat_win_time.minute_per_win(ds.stat_unit_min) < g_min_save_span)
				continue;

			sb.append(stat_number_encode.encode(me.getKey().intValue()));
			sb.append(':');
			sb.append((me.getValue() == null ? "" : me.getValue()));
			sb.append('\n');
		}

		return m_value = sb.toString();
	}

	public void set_param(String mParam)
	{
		this.m_param = mParam;
	}

	synchronized public boolean is_empty()
	{
		return this.m_map.isEmpty();
	}

	public void set_indb()
	{
		m_is_indb = true;
	}

	public boolean is_indb()
	{
		return m_is_indb;
	}

	public counter m_c;

	public void dec_query_counter()
	{
		if (m_c != null)
			m_c.dec();
	}

	//
	// public static stat_value load_from(bin_stream bs)
	// {
	// stat_value sv = new stat_value(bs.load_string(), bs.load_string());
	// sv.set_indb();
	// return sv;
	// }
	//
	// public void save_to(bin_stream bs)
	// {
	// bs.save(m_param).save(toString());
	// }

	public void set_dirty(boolean b)
	{
		if (m_dirty && !b)
			m_value = toString();

		m_dirty = b;
	}

	public void set_dirty()
	{
		set_dirty(true);
	}
}
