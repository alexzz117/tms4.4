package cn.com.higinet.tms35.stat;

import java.util.Arrays;

import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.core.cache.db_userpattern;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_avg;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_bindist;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_bindist_rang;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_count;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_count_equals;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_count_uniq;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_dummy;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_expr_calculat;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_max;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_min;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_nmdist;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_snapshot;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_status;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_sum;

public abstract class stat_func implements Comparable<stat_func>
{
	static stat_func[] g_func = new stat_func[] // 使用名称排序的有序表
	{ new stat_func_avg(), new stat_func_bindist_rang(), new stat_func_bindist(), new stat_func_count_uniq(),
			new stat_func_count(), new stat_func_count_equals(), new stat_func_expr_calculat(), new stat_func_max(),
			new stat_func_min(), new stat_func_nmdist(), new stat_func_sum(), new stat_func_status(),
			new stat_func_snapshot() };

	public static int max_win_count = 15;
	static
	{
		max_win_count = tmsapp.get_config("tms.stat.maxwincount", 15);
		Arrays.sort(g_func);
	}

	static final int get_local_id(String name)
	{
		return Arrays.binarySearch(g_func, new stat_func_dummy(name));
	}

	public final static stat_func get(String name)
	{
		if (name == null)
			return null;

		int index = get_local_id(name);
		if (index < 0)
			return null;
		return g_func[index];
	}

	protected final boolean is_empty(String d)
	{
		return d == null || d.length() == 0;
	}

	protected final String to_string(Object o)
	{
		return o == null ? "" : o.toString();
	}

	final public int compareTo(stat_func o)
	{
		return name().toLowerCase().compareTo(o.name().toLowerCase());
	}

	public abstract String name();

	public abstract String type(db_stat st);

	public abstract Object get(String data, db_stat stat, int cur_minute, Object cur_value);

	public abstract Object getPatternValue(db_stat st, stat_row sd, db_userpattern up, long txntime, int txn_minute,
			Object curVal);

	public abstract String set(String d, db_stat stat, int cur_minute, Object cur_value);

	final public String union(String d1, String d2, db_stat stat)
	{
		if (d1 == null)
			return d2;

		if (d2 == null)
			return d1;

		return union_(d1, d2, stat);
	}

	final public <E extends stat_win_base> String union_impl(String d1, String d2, db_stat stat, E fac)
	{
		stat_win_fac<E> fac1 = new stat_win_fac<E>(d1, 0, stat.dec_win_when_set(), fac);
		stat_win_fac<E> fac2 = new stat_win_fac<E>(d2, 0, stat.dec_win_when_set(), fac);

		stat_win_fac<E> ret = fac1.union(fac2, stat, this);

		return ret.toString();
	}

	protected abstract String union_(String d1, String d2, db_stat stat);

	public abstract Object union_item(Object w1, Object w2);

	public final Object get_online(String data, db_stat stat, int cur_minute, Object cur_value)
	{
		String tmp = set(data, stat, cur_minute, cur_value);
		return get(tmp, stat, cur_minute, cur_value);
	}

	public abstract boolean need_curval_when_get();

	public abstract boolean need_curval_when_set();

	public abstract Object getAll(String data, db_stat stat, int cur_minute);
}
