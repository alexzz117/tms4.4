package cn.com.higinet.tms.engine.stat.stat_func_im;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cn.com.higinet.tms.engine.core.cache.db_fd;
import cn.com.higinet.tms.engine.core.cache.db_stat;
import cn.com.higinet.tms.engine.core.cache.db_userpattern;
import cn.com.higinet.tms.engine.core.cache.db_userpattern.Pattern;
import cn.com.higinet.tms.engine.core.cond.date_tool;
import cn.com.higinet.tms.engine.core.cond.op;
import cn.com.higinet.tms.engine.stat.stat_func;
import cn.com.higinet.tms.engine.stat.stat_row;
import cn.com.higinet.tms.engine.stat.stat_win_fac;

public class stat_func_snapshot extends stat_func
{
	@Override
	public String name()
	{
		return "SNAPSHOT";
	}

	@Override
	public String type(db_stat st)
	{
		db_fd fd = st.datafd();
		return fd.type;
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

	// [wintime:value]
	@Override
	public String set(String d, db_stat stat, int cur_minute, Object cur_value)
	{
		if (d == null)
			d = "";

		if (cur_value == null)
			return d;

		int cur_win_time = stat.cur_win_time(cur_minute);
		return new stat_win_S(cur_win_time, to_string(cur_value)).toString(0);
	}

	@Override
	public Object get(String data, db_stat stat, int cur_minute, Object cur_value)
	{
		if (is_empty(data))
			return null;

		int min_win_time = stat.min_win_time(cur_minute);
		stat_win_fac<stat_win_S> fac = new stat_win_fac<stat_win_S>(data, min_win_time, stat_win_S._fac);

		stat_win_S last = fac.next_r();
		if (last == null)
			return null;

		switch (op.name2type(type(stat)))
		{
		case op.time_:
		case op.datetime_:
		case op.long_:
			return new Long(last.sV);
		case op.double_:
			return new Double(last.sV);
		case op.string_:
		default:
			return last.sV;
		}
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
		return true;
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException
	{
		{
			String s = "100:20:100|2:10:100.0|3:10:200.0|";
			stat_win_fac<stat_win_LD> fac = new stat_win_fac<stat_win_LD>(s, -2, new stat_win_LD());

			for (stat_win_LD w = null; null != (w = fac.next());)
				System.out.println(w);

			System.out.println(fac.toString());
		}

		{
			String s = "100:20:100|2:10:100.0|3:10:200.0|";
			stat_win_fac<stat_win_LD> fac = new stat_win_fac<stat_win_LD>(s, 0xFF, new stat_win_LD());

			for (stat_win_LD w = null; null != (w = fac.next());)
				System.out.println(w);
			System.out.println(fac.toString());
		}
		{
			String s = "100:20:100|2:10:100.0|3:10:200.0|";
			stat_win_fac<stat_win_LD> fac = new stat_win_fac<stat_win_LD>(s, 0x101, new stat_win_LD());

			for (stat_win_LD w = null; null != (w = fac.next());)
				System.out.println(w);
			System.out.println(fac.toString());
		}

		{
			String s = "100:20:100|2:10:100.0|3:10:200.0|";
			stat_win_fac<stat_win_LD> fac = new stat_win_fac<stat_win_LD>(s, 0x103, new stat_win_LD());

			for (stat_win_LD w = null; null != (w = fac.next());)
				System.out.println(w);
			System.out.println(fac.toString());
		}

		{
			String s = "100:20:100|2:10:100.0|3:10:200.0|";
			stat_win_fac<stat_win_LD> fac = new stat_win_fac<stat_win_LD>(s, 0x104, new stat_win_LD());

			for (stat_win_LD w = null; null != (w = fac.next());)
				System.out.println(w);
			System.out.println(fac.toString());
		}

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
				String pattern_value = p.pattern_value;
				String data_type = type(st);

				// 日期时间类型的交易属性，需要把日期时间转成long型比较
				if ("DATETIME".equals(data_type))
				{
					pattern_value = String.valueOf(date_tool.parse(pattern_value).getTime());
				}

				switch (op.name2type(data_type))
				{
				case op.time_:
				case op.datetime_:
				case op.long_:
					return new Long(pattern_value);
				case op.double_:
					return new Double(pattern_value);
				case op.string_:
				default:
					return pattern_value;
				}
			}
		}
		return null;
	}

}
