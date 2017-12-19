package cn.com.higinet.tms.engine.stat.stat_func_im;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.core.cache.db_stat;
import cn.com.higinet.tms.engine.core.cache.db_userpattern;
import cn.com.higinet.tms.engine.core.cache.db_userpattern.Pattern;
import cn.com.higinet.tms.engine.stat.stat_func;
import cn.com.higinet.tms.engine.stat.stat_row;

public class stat_func_bindist_rang extends stat_func
{

	@Override
	public String type(db_stat st)
	{
		return "DOUBLE";
	}

	@Override
	public String name()
	{
		return "RANG_BIN_DIST";
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
	
	// [tw:range_index:c]
	@Override
	public String set(String d, db_stat stat, int cur_minute, Object cur_value)
	{
		if (d == null)
			d = "";

		if (str_tool.is_empty(str_tool.to_str(cur_value)))
			return d;

		if (!(cur_value instanceof Number))
			cur_value = Double.parseDouble(str_tool.to_str(cur_value));

		int index = stat.range_index((Number) cur_value);
		if (index < 0)
			return d;

		return new stat_func_bindist().set(d, stat, cur_minute, new Integer(index));
	}

	@Override
	public Object get(String data, db_stat stat, int cur_minute, Object cur_value)
	{
		if (is_empty(data) || str_tool.is_empty(str_tool.to_str(cur_value)))
			return new Double(0);

		if (!(cur_value instanceof Number))
			cur_value = Double.parseDouble(str_tool.to_str(cur_value));
		int index = stat.range_index((Number) cur_value);
		if (index < 0)
			return new Double(0);

		return new stat_func_bindist().get(data, stat, cur_minute, new Integer(index));
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
	public Object getAll(String data, db_stat stat, int cur_minute)
	{
		// TODO Auto-generated method stub
		return new stat_func_bindist().getAll(data, stat, cur_minute);
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
	public Object getPatternValue(db_stat st, stat_row sd, db_userpattern up, long txntime,
			int txn_minute, Object curVal)
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
				if (curVal == null || curVal.equals(""))
					continue;

				if (!(curVal instanceof Number))
					curVal = Double.parseDouble(str_tool.to_str(curVal));

				int index = st.range_index((Number) curVal);

				if (p.pattern_value.equals(str_tool.to_str(index)))
				{
					return 1;
				}
			}
		}
		return null;
	}

}
