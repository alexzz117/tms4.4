package cn.com.higinet.tms35.comm;

import java.util.ArrayList;
import java.util.List;

import cn.com.higinet.tms35.core.cache.db_fd;
import cn.com.higinet.tms35.core.cache.linear;

public class maped_row
{
	linear<db_fd> m_fd_def;
	public List<String> m_value_list;

	public maped_row(linear<db_fd> fd_def, List<String> value_list)
	{
		m_fd_def = fd_def;
		m_value_list = value_list;
	}

	public maped_row(linear<db_fd> fd_def)
	{
		this(fd_def, new ArrayList<String>(fd_def.size()));
	}

	public final String get(String ref_name)
	{
		int id = this.m_fd_def.index(new db_fd(m_fd_def.get(0).tab_name, ref_name));
		synchronized (m_value_list)
		{
			if (id < 0 || id >= m_value_list.size())
				return null;
			return m_value_list.get(id);
		}
	}

	public final void put(String ref_name, String value)
	{
		int id = this.m_fd_def.index(new db_fd(m_fd_def.get(0).tab_name, ref_name));
		if (id < 0)
			return;
		synchronized (m_value_list)
		{
			while (id >= m_value_list.size())
				m_value_list.add(null);

			m_value_list.set(id, value);
		}
	}
}
