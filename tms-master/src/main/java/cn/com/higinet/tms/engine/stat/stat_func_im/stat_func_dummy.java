package cn.com.higinet.tms.engine.stat.stat_func_im;

import cn.com.higinet.tms.engine.core.cache.db_stat;
import cn.com.higinet.tms.engine.core.cache.db_userpattern;
import cn.com.higinet.tms.engine.stat.stat_func;
import cn.com.higinet.tms.engine.stat.stat_row;

public class stat_func_dummy extends stat_func
{
	String name;

	public stat_func_dummy(String name)
	{
		this.name = name;

	}

	@Override
	public String type(db_stat st)
	{
		return null;
	}

	@Override
	public String name()
	{
		return name;
	}
	
	@Override
	public String union_(String d1, String d2, db_stat stat)
	{
		return null;
	}

	@Override
	public Object union_item(Object w1, Object w2)
	{
		return null;
	}

	

	@Override
	public String set(String d, db_stat stat, int cur_minute, Object cur_value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(String data, db_stat stat, int cur_minute, Object cur_value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean need_curval_when_get()
	{
		return false;
	}

	@Override
	public boolean need_curval_when_set()
	{
		return true;
	}

	@Override
	public Object getAll(String data, db_stat stat, int cur_minute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPatternValue(db_stat st, stat_row sd, db_userpattern up,
			long txntime, int txn_minute, Object curVal) {
		// TODO Auto-generated method stub
		return null;
	}
}
