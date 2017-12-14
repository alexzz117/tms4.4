package cn.com.higinet.tms35.stat.stat_func_im;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.core.cache.db_userpattern;
import cn.com.higinet.tms35.core.cache.db_userpattern.Pattern;
import cn.com.higinet.tms35.stat.stat_func;
import cn.com.higinet.tms35.stat.stat_row;
import cn.com.higinet.tms35.stat.stat_win_fac;

public class stat_func_bindist extends stat_func
{

	@Override
	public String type(db_stat st)
	{
		return "DOUBLE";
	}

	@Override
	public String name()
	{
		return "BIN_DIST";
	}

	@Override
	public String union_(String d1, String d2, db_stat stat)
	{
		return union_impl(d1, d2, stat, stat_win_LS._fac);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object union_item(Object w1, Object w2)
	{
		List<stat_win_LS> l1 = (List<stat_win_LS>) w1;
		List<stat_win_LS> l2 = (List<stat_win_LS>) w2;

		Map<String, stat_win_LS> map = new TreeMap<String, stat_win_LS>();

		for (int i = 0, c = l1.size(); i < c; i++)
		{
			stat_win_LS v = l1.get(i);
			stat_win_LS p = map.get(v.sV);
			if (p == null)
			{
				map.put(v.sV, new stat_win_LS(v.win_time, v.lV, v.sV));
				continue;
			}

			p.lV += v.lV;
		}

		for (int i = 0, c = l2.size(); i < c; i++)
		{
			stat_win_LS v = l2.get(i);
			stat_win_LS p = map.get(v.sV);
			if (p == null)
			{
				map.put(v.sV, new stat_win_LS(v.win_time, v.lV, v.sV));
				continue;
			}

			p.lV += v.lV;
		}

		return new ArrayList<stat_win_LS>(map.values());
	}

	// [tw1:count:obj1][tw1:count:obj2][tw2:count:obj1][tw2:count:obj3]
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

		stat_win_fac<stat_win_LS> fac = new stat_win_fac<stat_win_LS>(d, min_win_time, stat.dec_win_when_set(),
				stat_win_LS._fac);

		stat_win_fac<stat_win_LS>.FI fi = fac.find_r(cur_win_time);
		if (fi.flag > 0)// 无相等时间窗
		{
			return fac.toString() + new stat_win_LS(cur_win_time, 1, value).toString(fac.base_time());
		}
		else if (fi.flag == 0)// 有相等的时间窗，但是需要比较对象
		{
			stat_win_LS e = fi.e;
			int rindex = fac.rindex();
			for (; e != null && e.win_time == cur_win_time; e = fac.next_r())
			{
				if (e.sV.equals(value))
				{
					e.lV++;
					return fac.toString();
				}
			}
			fac.insert(rindex + 2, new stat_win_LS(cur_win_time, 1, value));
		}
		else
		{
			fac.insert(fac.rindex() + 1, new stat_win_LS(cur_win_time, 1, value));
		}

		return min_len_string(fac);
		// return fac.toString();
	}

	public String min_len_string(stat_win_fac<stat_win_LS> fac)
	{
		final int max_count = stat_func.max_win_count;
		fac.reset_rindex();

		// System.out.println(fac);

		Set<String> set = new TreeSet<String>();
		for (stat_win_LS i = fac.next_r(); i != null; i = fac.next_r())
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
		String value = null;
		if (is_empty(data) || str_tool.is_empty(value = str_tool.to_str(cur_value)))
			return new Double(0);

		int min_win_time = stat.min_win_time(cur_minute);
		long c = 0;
		long vc = 0;

		stat_win_fac<stat_win_LS> fac = new stat_win_fac<stat_win_LS>(data, min_win_time, stat_win_LS._fac);

		for (stat_win_LS w = fac.next(); w != null; w = fac.next())
		{
			c += w.lV;
			if (w.sV.equals(value))
				vc += w.lV;
		}

		if (c == 0)
			return new Double(0);

		return new Double(1.d * vc / c);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.stat.stat_func#getAll(java.lang.String,
	 * cn.com.higinet.tms35.core.cache.db_stat, int)
	 */
	@Override
	public Map<String, Object> getAll(String data, db_stat stat, int cur_minute)
	{
		if (is_empty(data))
			return null;

		int min_win_time = stat.min_win_time(cur_minute);

		stat_win_fac<stat_win_LS> fac = new stat_win_fac<stat_win_LS>(data, min_win_time, stat_win_LS._fac);

		Map<String, Object> o = new HashMap<String, Object>();
		for (stat_win_LS w = fac.next(); w != null; w = fac.next())
		{

			if (!o.containsKey(w.sV))
			{
				o.put(w.sV, w.lV);
			}
			else
			{
				o.put(w.sV, Double.valueOf(String.valueOf(o.get(w.sV))) + w.lV);
			}
		}

		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cn.com.higinet.tms35.stat.stat_func#getPatternValue(cn.com.higinet.tms35
	 * .core.cache.db_stat, cn.com.higinet.tms35.stat.stat_row,
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
			if (p.is_enable(txntime) && p.pattern_value.equals(curVal))
				return 1;
		}

		return null;
	}

}
