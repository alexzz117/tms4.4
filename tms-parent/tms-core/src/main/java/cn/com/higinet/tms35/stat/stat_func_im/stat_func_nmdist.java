package cn.com.higinet.tms35.stat.stat_func_im;

import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.core.cache.db_userpattern;
import cn.com.higinet.tms35.stat.stat_func;
import cn.com.higinet.tms35.stat.stat_row;

public class stat_func_nmdist extends stat_func
{

	@Override
	public String type(db_stat st)
	{
		return "DOUBLE";
	}

	@Override
	public String name()
	{
		return "NM_DIST";
	}

	@Override
	public String set(String d, db_stat stat, int cur_minute, Object cur_value)
	{
		return null;
	}

	@Override
	public Object get(String data, db_stat stat, int cur_minute, Object cur_value)
	{
		return null;
	}

	@Override
	public boolean need_curval_when_get()
	{
		return true;
	}

	@Override
	public boolean need_curval_when_set()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.stat.stat_func#getAll(java.lang.String, cn.com.higinet.tms35.core.cache.db_stat, int)
	 */
	@Override
	public Object getAll(String data, db_stat stat, int cur_minute) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.stat.stat_func#getPatternValue(cn.com.higinet.tms35.core.cache.db_stat, cn.com.higinet.tms35.stat.stat_row, cn.com.higinet.tms35.core.cache.db_userpattern, long, int, java.lang.Object)
	 */
	@Override
	public Object getPatternValue(db_stat st, stat_row sd, db_userpattern up,
			long txntime, int txn_minute, Object curVal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String union_(String d1, String d2, db_stat stat)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object union_item(Object w1, Object w2)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
