package cn.com.higinet.tms35.core.cache;

import java.util.ArrayList;
import java.util.List;

import cn.com.higinet.tms35.comm.bin_stream;
import cn.com.higinet.tms35.comm.maped_row;
import cn.com.higinet.tms35.core.dao.row_in_db_impl;

public class db_ref_row extends row_in_db_impl
{
	public db_ref_row(linear<db_fd> fd_def)
	{
		data=new maped_row(fd_def);
	}

	public db_ref_row(String user_id,maped_row d)
	{
		key = user_id;
		data=d;
	}
	
	String key;
	maped_row data;
	public void set_key(String s)
	{
		key=s;
	}
	public List<String> values()
	{
		return data.m_value_list;
	}
	
	public Object[] toArray()
	{
		List<String> ret=new ArrayList<String>(data.m_value_list.size()+1);
		ret.addAll(data.m_value_list);
		ret.add(key);
		return ret.toArray();
	}
	public final String get(String refName)
	{
		return data.get(refName);
	}

	public final void put(String refName, String value)
	{
		data.put(refName, value);
	}

	public String get_key()
	{
		return this.key;
	}

	public static db_ref_row load_from(bin_stream bs)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void save_to(bin_stream bs)
	{
		bs.save(this.key)
		.save(this.is_indb())
		.save(this.data.m_value_list.size());
	}

}
