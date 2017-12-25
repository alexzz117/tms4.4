package cn.com.higinet.tms.engine.stat;

import java.util.ArrayList;
import java.util.List;

import cn.com.higinet.tms.engine.comm.array_tool;
import cn.com.higinet.tms.engine.core.cache.db_stat;

/*
 * 统计字符串分析
 * */
@SuppressWarnings("unchecked")
public class stat_win_fac<E extends stat_win_base>
{
	static final char win_split = '|', item_split = ':';
	E fact;
	List<E> win_items;
	int base_time, pos, pos_r;

	public stat_win_fac()
	{
		win_items = new ArrayList<E>(32);
	}

	public stat_win_fac(String buf, int min_valid_time, E fac)
	{
		this(buf, min_valid_time, 0, fac);
	}

	public stat_win_fac(String buf, int min_valid_time, int dec, E fac)
	{
		fact = fac;
		pos = base_time = 0;
		win_items = new ArrayList<E>(32);
		if (buf == null || buf.length() == 0)
		{
			pos_r = -1;
		}
		else
		{
			init_items(buf, min_valid_time);
			pos_r = win_items.size() - 1;
		}

		for (E e : win_items)
			e.win_time -= dec;

		clac_base_time();
	}

	final public stat_win_fac<E> union(stat_win_fac<E> swf, db_stat stat, stat_func st_func)
	{
		stat_win_fac<E> ret = new stat_win_fac<E>();

		int min_time = stat.min_win_time(stat.st2txntime((Math.max(last().win_time, swf.last().win_time))));

		E p1 = this.next(), p2 = swf.next();

		for (; p1 != null && p1.win_time < min_time; p1 = next())
			;
		for (; p2 != null && p2.win_time < min_time; p2 = swf.next())
			;

		for (; p1 != null && p2 != null;)
		{
			int time_diff = p1.win_time - p2.win_time;

			if (time_diff == 0)
			{
				List<E> l1 = new ArrayList<E>(32), l2 = new ArrayList<E>(32);

				for (int time = p1.win_time; p1 != null && p1.win_time == time; p1 = next())
					l1.add(p1);
				for (int time = p2.win_time; p2 != null && p2.win_time == time; p2 = swf.next())
					l2.add(p2);

				ret.win_items.addAll((List<E>) st_func.union_item(l1, l2));
			}
			else if (time_diff < 0)
			{
				ret.win_items.add(p1);
				p1 = this.next();
			}
			else
			{
				ret.win_items.add(p2);
				p2 = swf.next();
			}
		}

		for (; p1 != null; p1 = next())
			ret.win_items.add(p1);

		for (; p2 != null; p2 = swf.next())
			ret.win_items.add(p2);

		ret.clac_base_time();
		return ret;
	}

	final void clac_base_time()
	{
		if (!this.win_items.isEmpty())
			base_time = win_items.get(0).win_time;
		else
			base_time = 0;
	}

	final int skip_(String cc, int p, char ch)
	{
		return cc.indexOf(ch, p);
	}

	final int skip_r(String cc, int p, char ch)
	{
		return cc.lastIndexOf(ch, p);
	}

	void init_items(String cc, int min_valid_time)
	{
		List<E> list = win_items;
		int p = 0, i = 0, time = 0;
		int len = cc.length();
		for (; p < len;)
		{
			i = p;
			p = skip_(cc, i, item_split);
			time = stat_number_encode.decode_int(cc.substring(i, p)) + base_time;
			if (i == 0)
				base_time = time;

			if (time < min_valid_time)
			{
				p = skip_(cc, p, win_split) + 1;
				continue;
			}
			p = i;
			break;
		}

		for (; p < len;)
		{
			i = skip_(cc, p, win_split);
			list.add((E) fact.from(items(cc, p, i), (stat_win_fac<stat_win_base>) this, p == 0 ? 0 : base_time));
			p = i + 1;
		}

		// if (min_valid_time < 0 && list.size() > -min_valid_time)//
		// 对于以交易为单位的统计，返回最后的交易数量
		// list = list.subList(list.size() + min_valid_time, list.size());

		base_time = list.isEmpty() ? 0 : list.get(0).win_time;
	}

	String[] items(String cc, int s, int e)
	{
		String[] ret = new String[5];
		int len = 0;
		for (; s < e;)
		{
			int i = skip_(cc, s, item_split);
			if (i < 0)
				i = e;
			ret[len++] = cc.substring(s, i >= e ? e : i);
			s = i + 1;
		}

		return array_tool.copyOf(ret, len);
	}

	final public E next()
	{
		if (pos >= win_items.size())
			return null;
		return this.win_items.get(pos++);
	}

	public E next_r()
	{
		if (pos_r < 0 || pos_r >= win_items.size())
			return null;
		return this.win_items.get(pos_r--);
	}

	public class FI
	{
		public int flag;// -1 业务时间早于最早的，0，业务时间等于某窗口，1业务时间大于最大窗口或无窗口
		public E e;

		public FI(int flag, E e)
		{
			this.flag = flag;
			this.e = e;
		}

		public FI(int flag)
		{
			this.flag = flag;
		}
	}

	public FI find_r(int cur_win_time)
	{
		E n = next_r();
		if (n == null || n.win_time < cur_win_time)// 当前统计为空，或者交易时间比统计中记录的时间往后，此时需要新建
			return new FI(1);

		// 否则，查找最后一个相等的时间窗
		for (; n != null; n = next_r())
		{
			if (n.win_time == cur_win_time)
				return new FI(0, n);

			if (n.win_time < cur_win_time)
			{
				pos_r++;
				return new FI(-1);
			}
		}

		// 没有找到，当前时间不在所有的时间窗中，返回第一个
		return new FI(-1);
	}

	public String toString()
	{
		if (win_items.isEmpty())
			return "";

		StringBuffer sb = new StringBuffer(512);
		win_items.get(0).appendTo(sb, 0);

		for (int i = 1, len = win_items.size(); i < len; i++)
			win_items.get(i).appendTo(sb, this.base_time);

		return sb.toString();
	}

	final public int base_time()
	{
		return this.base_time;
	}

	public int rindex()
	{
		return this.pos_r;
	}

	public int index()
	{
		return this.pos;
	}

	public void insert(int i, E e)
	{
		win_items.add(i, e);
		if (e.win_time < base_time)
			base_time = e.win_time;
	}

	public void reset_rindex()
	{
		this.pos_r = win_items.size() - 1;
	}

	public void del_before(int rindex)
	{
		if (rindex < 0)
			return;

		win_items = new ArrayList<E>(win_items.subList(rindex + 1, win_items.size()));
		this.pos_r = win_items.size() - 1;
		this.pos = 0;
		if (win_items.size() > 0)
			this.base_time = win_items.get(0).win_time;
	}

	E last()
	{
		return win_items.get(win_items.size() - 1);
	}

	public int size()
	{
		return win_items.size();
	}
}
