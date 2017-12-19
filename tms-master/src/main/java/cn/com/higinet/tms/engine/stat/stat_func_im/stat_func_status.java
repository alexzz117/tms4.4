package cn.com.higinet.tms.engine.stat.stat_func_im;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.cache.db_stat;
import cn.com.higinet.tms.engine.core.cache.db_userpattern;
import cn.com.higinet.tms.engine.core.cache.db_userpattern.Pattern;
import cn.com.higinet.tms.engine.stat.stat_func;
import cn.com.higinet.tms.engine.stat.stat_row;
import cn.com.higinet.tms.engine.stat.stat_win_fac;

public class stat_func_status extends stat_func
{
	static Logger log = LoggerFactory.getLogger(stat_func_status.class);

	@Override
	public String name()
	{
		return "STATUS";
	}

	@Override
	public String type(db_stat st)
	{
		return "INT";
	}

	@Override
	public String union_(String d1, String d2, db_stat stat)
	{
		stat_win_fac<stat_win_S> fac1 = new stat_win_fac<stat_win_S>(d1, 0, stat.dec_win_when_set(), stat_win_S._fac);
		stat_win_fac<stat_win_S> fac2 = new stat_win_fac<stat_win_S>(d2, 0, stat.dec_win_when_set(), stat_win_S._fac);

		stat_win_fac<stat_win_S> ret = fac1.union(fac2, stat, this);
//		ret.del_before(0);
		return ret.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object union_item(Object w1, Object w2)
	{
		return new ArrayList<stat_win_S>((ArrayList<stat_win_S>)w2);
	}

	// [wintime:tmstamp:value]
	@Override
	public String set(String d, db_stat stat, int cur_minute, Object cur_value)
	{
		if (d == null)
			d = "";

		int cur_win_time = stat.cur_win_time(cur_minute);
		return new stat_win_S(cur_win_time, "1").toString(0);
	}

	@Override
	public Object get(String data, db_stat stat, int cur_minute, Object cur_value)
	{
		if (is_empty(data))
			return new Long(0);

		int min_win_time = stat.min_win_time(cur_minute);
		stat_win_fac<stat_win_S> fac = new stat_win_fac<stat_win_S>(data, min_win_time, stat_win_S._fac);
		stat_win_S last = fac.next_r();

		if (last == null)
			return new Long(0);

		return new Long(1);
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
	 * @see cn.com.higinet.tms35.stat.stat_func#getAll(java.lang.String,
	 * cn.com.higinet.tms35.core.cache.db_stat, int)
	 */
	@Override
	public Object getAll(String data, db_stat stat, int cur_minute)
	{
		// TODO Auto-generated method stub
		return this.get(data, stat, cur_minute, null);
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
