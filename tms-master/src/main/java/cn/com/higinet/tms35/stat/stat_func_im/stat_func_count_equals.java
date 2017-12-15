package cn.com.higinet.tms35.stat.stat_func_im;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.core.cache.db_userpattern;
import cn.com.higinet.tms35.core.cache.db_userpattern.Pattern;
import cn.com.higinet.tms35.stat.stat_row;
import cn.com.higinet.tms35.stat.stat_win_fac;

/**
 * 统计函数: 相同计数(套用二项分布的数据组织方式)
 * 
 * @author lining@higinet.com.cn
 * @version 4.0
 * @since 2014-8-27 上午11:55:28
 * @description
 */
public class stat_func_count_equals extends stat_func_bindist
{

	@Override
	public String name()
	{
		return "COUNT_EQUALS";
	}

	@Override
	public String type(db_stat st)
	{
		return "INT";
	}

	// [tw1:count:obj1][tw1:count:obj2][tw2:count:obj1][tw2:count:obj3]
	@Override
	public Object get(String data, db_stat stat, int cur_minute, Object cur_value)
	{
		String value = null;
		if (is_empty(data) || str_tool.is_empty(value = str_tool.to_str(cur_value)))
			return new Long(0);
		int min_win_time = stat.min_win_time(cur_minute);
		long vc = 0;
		stat_win_fac<stat_win_LS> fac = new stat_win_fac<stat_win_LS>(data, min_win_time, stat_win_LS._fac);
		for (stat_win_LS w = fac.next(); w != null; w = fac.next())
		{
			if (w.sV.equals(value))
				vc += w.lV;
		}
		return new Long(vc);
	}

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
				o.put(w.sV, Integer.valueOf(String.valueOf(o.get(w.sV))) + w.lV);
			}
		}
		return o;
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
			if (p.is_enable(txntime))
			{
				return Integer.parseInt(p.pattern_value);
			}
		}
		return null;
	}
}