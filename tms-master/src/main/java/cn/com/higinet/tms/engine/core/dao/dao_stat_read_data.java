package cn.com.higinet.tms.engine.core.dao;

import java.util.List;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import cn.com.higinet.tms.engine.comm.tm_tool;
import cn.com.higinet.tms.engine.core.bean;
import cn.com.higinet.tms.engine.stat.stat_data;

/*
 * 统计：本地更新的数据
 * 取值：全部数据库中获取
 * */

public final class dao_stat_read_data 
{
	static private dao_stat_read_data _inst;

	static public dao_stat_read_data Instance()
	{
		if (_inst != null)
			return _inst;

		synchronized (dao_stat_read_data.class)
		{
			if (_inst != null)
				return _inst;

			return _inst = new dao_stat_read_data();
		}
	}

	dao_stat_read_data()
	{
		
	}

	public List<stat_data> read_from_db(List<stat_data> list)
	{
		return list.size() < 10 ? db_read1(list) : db_read2(list);
	}

	public List<stat_data> db_read2(List<stat_data> list)
	{
		return null;
//		dao_stat_batch_query dao_query=new dao_stat_batch_query();
//		try
//		{
//			dao_query.query(list);
//		}
//		catch (SQLException e)
//		{
//			e.printStackTrace();
//		}
//		finally
//		{
//			dao_query.close();
//		}
//		
//		return list;
	}

	final String _s = "select %d,STAT_VALUE from TMS_RUN_STAT where ";

	public List<stat_data> db_read1(List<stat_data> list)
	{
		if (list.isEmpty())
			return null;

		StringBuffer sql = new StringBuffer(1024);
		StringBuffer sb = new StringBuffer(128);
		int pos = sb.length();
		for (int i = 0, len = list.size(); i < len; i++)
		{
			stat_data d = list.get(i);
			if (d == null)
				continue;

			sb.append(String.format(_s, i));
			sb.append("STAT_ID=").append(d.get_id()).append(" and STAT_PARAM='").append(d.get_param()).append('\'');

			sql.append(sb.toString());
			sql.append("\nunion all\n");
			sb.setLength(pos);
		}

		if (sql.length() == 0)
			return list;
		
		sql.setLength(sql.length()-"union all\n".length());
		SqlRowSet rs = bean.get(dao_base.class).query(sql.toString());

		for (int j = 0; rs.next(); j++)
		{
			stat_data d = list.get(rs.getInt(1));
			d.set_value(rs.getString(3));
		}

		return list;
	}

	final public Object[] toArray(stat_data d)
	{
		Integer time = (int)tm_tool.lctm_min();
		return new Object[] { d.get_id(), d.get_param(), d.get_value(), time };
	}

	public String name()
	{
		return "dao_stat_data_read";
	}
}
