package cn.com.higinet.tms.engine.stat.stat_func_im;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.core.cache.db_stat;
import cn.com.higinet.tms.engine.core.cache.db_userpattern;
import cn.com.higinet.tms.engine.core.cache.db_userpattern.Pattern;
import cn.com.higinet.tms.engine.stat.stat_func;
import cn.com.higinet.tms.engine.stat.stat_row;
import cn.com.higinet.tms.engine.stat.stat_win_fac;

public class stat_func_count_uniq extends stat_func
{
	@Override
	public String type(db_stat st)
	{
		return "INT";
	}

	@Override
	public String name()
	{
		return "COUNT_UNIQ";
	}

	@Override
	public String union_(String d1, String d2, db_stat stat)
	{
		return union_impl(d1, d2, stat, stat_win_S._fac);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object union_item(Object w1, Object w2)
	{
		List<stat_win_S> l1 = (List<stat_win_S>) w1;
		List<stat_win_S> l2 = (List<stat_win_S>) w2;

		Set<String> set = new TreeSet<String>();
		List<stat_win_S> ret = new ArrayList<stat_win_S>(l1.size() + l2.size());
		for (int i = 0, c = l1.size(); i < c; i++)
		{
			stat_win_S item = l1.get(i);
			if (set.add(item.sV))
				ret.add(item);
		}

		for (int i = 0, c = l2.size(); i < c; i++)
		{
			stat_win_S item = l2.get(i);
			if (set.add(item.sV))
				ret.add(item);
		}
		
		return ret;
	}

	@Override
	public String set(String d, db_stat stat, int cur_minute, Object cur_value)
	{
		if (d == null)
			d = "";

		if (str_tool.is_empty(str_tool.to_str(cur_value)))
			return d;

		int min_win_time = stat.min_win_time(cur_minute);
		int cur_win_time = stat.cur_win_time(cur_minute);
		String value = str_tool.to_str(cur_value);

		stat_win_fac<stat_win_S> fac = new stat_win_fac<stat_win_S>(d, min_win_time, stat.dec_win_when_set(),
				stat_win_S._fac);

		stat_win_fac<stat_win_S>.FI fi = fac.find_r(cur_win_time);

		if (fi.flag > 0)// 无相等时间窗
		{
			fac.insert(fac.size(), new stat_win_S(cur_win_time, value));
		}
		else if (fi.flag == 0)// 有相等的时间窗，但是需要比较对象
		{
			stat_win_S e = fi.e;
			int rindex = fac.rindex();
			for (; e != null && e.win_time == cur_win_time; e = fac.next_r())
			{
				if (e.sV.equals(value))
					return fac.toString();
			}
			fac.insert(rindex + 2, new stat_win_S(cur_win_time, value));
		}
		else
		{
			fac.insert(fac.rindex() + 1, new stat_win_S(cur_win_time, value));
		}

		return min_len_string(fac);
	}

	public String min_len_string(stat_win_fac<stat_win_S> fac)
	{
		final int max_count = stat_func.max_win_count;
		fac.reset_rindex();

		// System.out.println(fac);

		Set<String> set = new TreeSet<String>();
		for (stat_win_S i = fac.next_r(); i != null; i = fac.next_r())
		{
			set.add(i.sV);
			if (set.size() >= max_count)
				break;
		}

		fac.del_before(fac.rindex());

		return fac.toString();
	}

	@Override
	public Object get(String data, db_stat stat, int cur_minute, Object cur_value)
	{
		if (is_empty(data))
			return new Long(0);

		int min_win_time = stat.min_win_time(cur_minute);

		stat_win_fac<stat_win_S> fac = new stat_win_fac<stat_win_S>(data, min_win_time, stat_win_S._fac);

		Set<String> set = new TreeSet<String>();
		for (stat_win_S i = fac.next(); i != null; i = fac.next())
			set.add(i.sV);

		return new Long(set.size());
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
	public Object getAll(String data, db_stat stat, int cur_minute)
	{
		if (is_empty(data))
			return null;

		int min_win_time = stat.min_win_time(cur_minute);

		stat_win_fac<stat_win_S> fac = new stat_win_fac<stat_win_S>(data, min_win_time, stat_win_S._fac);

		Set<String> set = new TreeSet<String>();
		for (stat_win_S i = fac.next(); i != null; i = fac.next())
			set.add(i.sV);

		return new Long(set.size());
	}

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
