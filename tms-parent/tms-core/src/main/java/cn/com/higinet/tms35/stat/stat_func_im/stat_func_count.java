package cn.com.higinet.tms35.stat.stat_func_im;

import java.util.ArrayList;
import java.util.List;

import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.core.cache.db_userpattern;
import cn.com.higinet.tms35.core.cache.db_userpattern.Pattern;
import cn.com.higinet.tms35.stat.stat_func;
import cn.com.higinet.tms35.stat.stat_row;
import cn.com.higinet.tms35.stat.stat_win_fac;

public class stat_func_count extends stat_func
{
	@Override
	public String type(db_stat st)
	{
		return "INT";
	}

	@Override
	public String name()
	{
		return "COUNT";
	}

	@Override
	public String union_(String d1, String d2, db_stat stat)
	{
		return union_impl(d1, d2, stat, stat_win_L._fac);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object union_item(Object w1, Object w2)
	{
		stat_win_L i1 = ((List<stat_win_L>) w1).get(0);
		stat_win_L i2 = ((List<stat_win_L>) w2).get(0);

		List<stat_win_L> ret = new ArrayList<stat_win_L>(1);
		ret.add(new stat_win_L(i1.win_time, i1.lV + i2.lV));
		return ret;
	}

	@Override
	public String set(String d, db_stat stat, int cur_minute, Object cur_value)
	{
		if (d == null)
			d = "";

		int min_win_time = stat.min_win_time(cur_minute);
		int cur_win_time = stat.cur_win_time(cur_minute);

		stat_win_fac<stat_win_L> fac = new stat_win_fac<stat_win_L>(d, min_win_time, stat.dec_win_when_set(),
				stat_win_L._fac);
		stat_win_fac<stat_win_L>.FI fi = fac.find_r(cur_win_time);
		if (fi.flag > 0)
		{
			return fac.toString() + new stat_win_L(cur_win_time, 1).toString(fac.base_time());
		}
		else if (fi.flag == 0)
		{
			fi.e.lV++;
		}
		else
		{
			fac.insert(fac.rindex() + 1, new stat_win_L(cur_win_time, 1));
		}

		return fac.toString();
	}

	@Override
	public Object get(String data, db_stat stat, int cur_minute, Object cur_value)
	{
		if (is_empty(data))
			return new Long(0);

		int min_win_time = stat.min_win_time(cur_minute);

		stat_win_fac<stat_win_L> fac = null;
		try
		{
			fac = new stat_win_fac<stat_win_L>(data, min_win_time, stat_win_L._fac);
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
		}

		long c = 0;
		for (stat_win_L w; null != (w = fac.next());)
			c += w.lV;

		return c;
	}

	@Override
	public Object getAll(String data, db_stat stat, int cur_minute)
	{
		return this.get(data, stat, cur_minute, null);
	}

	@Override
	public boolean need_curval_when_get()
	{
		return false;
	}

	@Override
	public boolean need_curval_when_set()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cn.com.higinet.tms35.stat.stat_func#getPatternValue(cn.com.higinet.tms35.
	 * core.cache.db_stat, cn.com.higinet.tms35.stat.stat_row,
	 * cn.com.higinet.tms35.core.cache.db_userpattern, long, int,
	 * java.lang.Object)
	 */
	@Override
	public Object getPatternValue(db_stat st, stat_row sd, db_userpattern up, long txntime, int txn_minute,
			Object curVal)
	{
		if (up == null || up.pattern_m == null)
			return null;

		// 取该统计的行为习惯
		List<Pattern> p_l = up.pattern_m.get(st.stat_id);
		if (p_l == null)
			return null;

		for (Pattern p : p_l)
		{
			// 交易时间在行为习惯的有效期内
			if (p.is_enable(txntime))
			{
				return Integer.parseInt(p.pattern_value);
			}
		}
		return null;
	}
}
