package cn.com.higinet.tms35.stat.stat_func_im;

import java.util.ArrayList;
import java.util.List;

import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.core.cache.db_userpattern;
import cn.com.higinet.tms35.core.cache.db_userpattern.Pattern;
import cn.com.higinet.tms35.stat.stat_func;
import cn.com.higinet.tms35.stat.stat_row;
import cn.com.higinet.tms35.stat.stat_win_fac;

public class stat_func_sum extends stat_func
{
	@Override
	public String type(db_stat st)
	{
		return "DOUBLE";
	}

	@Override
	public String name()
	{
		return "SUM";
	}

	@Override
	public String union_(String d1, String d2, db_stat stat)
	{
		return union_impl(d1, d2, stat, stat_win_D._fac);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object union_item(Object w1, Object w2)
	{
		stat_win_D i1 = ((List<stat_win_D>) w1).get(0);
		stat_win_D i2 = ((List<stat_win_D>) w2).get(0);

		List<stat_win_D> ret = new ArrayList<stat_win_D>(1);
		ret.add(new stat_win_D(i1.win_time, i1.dV+i2.dV));
		return ret;
	}

	@Override
	public String set(String d, db_stat stat, int cur_minute, Object cur_value)
	{
		if (d == null)
			d = "";

		// Object o=this.get(d, stat, cur_minute, cur_value);

		if (str_tool.is_empty(str_tool.to_str(cur_value)))
			return d;

		if (!(cur_value instanceof Number))
			cur_value = Double.parseDouble(str_tool.to_str(cur_value));

		double value = ((Number) cur_value).doubleValue();
		int min_win_time = stat.min_win_time(cur_minute);
		int cur_win_time = stat.cur_win_time(cur_minute);

		stat_win_fac<stat_win_D> fac = new stat_win_fac<stat_win_D>(d, min_win_time, stat
				.dec_win_when_set(), stat_win_D._fac);
		stat_win_fac<stat_win_D>.FI fi = fac.find_r(cur_win_time);
		if (fi.flag > 0)
		{
			return fac.toString() + new stat_win_D(cur_win_time, value).toString(fac.base_time());
		}
		else if (fi.flag == 0)
		{
			fi.e.dV += value;
		}
		else
		{
			fac.insert(fac.rindex() + 1, new stat_win_D(cur_win_time, value));
		}

		return fac.toString();
	}

	@Override
	public Object get(String data, db_stat stat, int cur_minute, Object cur_value)
	{
		if (is_empty(data))
			return null;

		int min_win_time = stat.min_win_time(cur_minute);
		stat_win_fac<stat_win_D> fac = new stat_win_fac<stat_win_D>(data, min_win_time,
				stat_win_D._fac);

		stat_win_D w = fac.next();
		if (w == null)
			return null;

		double c = 0;
		for (; w != null; w = fac.next())
			c += w.dV;

		return c;
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

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.stat.stat_func#getAll(java.lang.String, cn.com.higinet.tms35.core.cache.db_stat, int)
	 */
	@Override
	public Object getAll(String data, db_stat stat, int cur_minute) {
		// TODO Auto-generated method stub
		return this.get(data, stat, cur_minute, null);
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.stat.stat_func#getPatternValue(cn.com.higinet.tms35.core.cache.db_stat, cn.com.higinet.tms35.stat.stat_row, cn.com.higinet.tms35.core.cache.db_userpattern, long, int, java.lang.Object)
	 */
	@Override
	public Object getPatternValue(db_stat st, stat_row sd, db_userpattern up,
			long txntime, int txn_minute, Object curVal) {
		
		if (up == null || up.pattern_m == null)
			return null;
			
		//取该统计的行为习惯
		List<Pattern> p_l = up.pattern_m.get(st.stat_id);
		if (p_l == null)
			return null;

		for (Pattern p : p_l)
		{
			// 交易时间在行为习惯的有效期内
			if (p.is_enable(txntime))
			{
				return Double.parseDouble(p.pattern_value);
			}
		}
		
		return null;
	}
}
