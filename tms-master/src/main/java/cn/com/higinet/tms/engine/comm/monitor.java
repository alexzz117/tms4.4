package cn.com.higinet.tms.engine.comm;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class monitor
{
	static Logger log = LoggerFactory.getLogger(monitor.class);
	final static boolean isLogOn = tmsapp.get_config("tms.monitor.log.isOn", 0) == 1;

	static private class inst
	{
		static int m_base_time = (int) tm_tool.lctm_sec() / 3600 * 3600;
		static Map<String, mon_item> m_mon_items = new LinkedHashMap<String, mon_item>();
	}

	static public class mon_thread implements Runnable
	{
		public void run()
		{
			long d = 0;
			for (long next = tm_tool.lctm_ms() / 1000 * 1000 + 1000;; next += 1000)
			{
				d = next - tm_tool.lctm_ms();
				delay_tool.delay(d);
				print_all();
				reset_all((int) tm_tool.lctm_sec());
			}
		}

		private void reset_all(int now)
		{
			synchronized (monitor.class)
			{
				Iterator<mon_item> it = inst.m_mon_items.values().iterator();
				while (it.hasNext())
				{
					it.next().reset(now);
				}
			}
		}

		private void print_all()
		{
			StringBuffer sb_value = new StringBuffer(inst.m_mon_items.size() << 4);
			synchronized (monitor.class)
			{
				Iterator<mon_item> it = inst.m_mon_items.values().iterator();
				while (it.hasNext())
				{
					mon_item mi = it.next();
					sb_value.append(mi.m_name).append(':').append(mi.value()).append(',');
				}
			}
			if (sb_value.length() > 0)
			{
				sb_value.setLength(sb_value.length() - 1);
				log.info(sb_value.toString());
			}
		}

		static public void start()
		{
			Thread thr = new Thread(new mon_thread());
			thr.setName("mon_thread");
			thr.setDaemon(true);
			thr.start();
		}
	}

	static mon_item empty_item = new mon_empty_item();

	synchronized static mon_item item(String name)
	{
		return isLogOn ? inst.m_mon_items.get(name) : empty_item;
	}

	// 计数
	static public mon_item new_counter(String name)
	{
		return new_counter(name, Integer.MAX_VALUE);
	}

	synchronized static public mon_item new_counter(String name, int interval)
	{
		mon_item mi = item(name);
		if (mi == null)
		{
			inst.m_mon_items.put(name, mi = new counter(name, interval));
		}

		return mi;
	}

	// 均值
	static public mon_item new_average(String name)
	{
		return new_average(name, Integer.MAX_VALUE);
	}

	synchronized static public mon_item new_average(String name, int interval)
	{
		mon_item mi = item(name);
		if (mi == null)
		{
			inst.m_mon_items.put(name, mi = new average(name, interval));
		}

		return mi;
	}

	static public mon_item new_max(String name)
	{
		return new_max(name, Integer.MAX_VALUE);
	}

	// 均值
	synchronized static public mon_item new_max(String name, int interval)
	{
		mon_item mi = item(name);
		if (mi == null)
		{
			inst.m_mon_items.put(name, mi = new max(name, interval));
		}

		return mi;
	}

	synchronized static public mon_item new_percent(String name, int interval, func_if<Integer> fi)
	{
		mon_item mi = item(name);
		if (mi == null)
		{
			inst.m_mon_items.put(name, mi = new percent_if(name, interval, fi));
		}

		return mi;
	}

	// 条件占比 >=value
	synchronized static public mon_item new_percent_ge(String name, int value)
	{
		return new_percent_ge(name, Integer.MAX_VALUE, value);
	}

	synchronized static public mon_item new_percent_ge(String name, int interval, int value)
	{
		mon_item mi = item(name);
		if (mi == null)
		{
			inst.m_mon_items.put(name, mi = new percent_ge(name, interval, value));
		}

		return mi;
	}

	static public mon_item make_group(mon_item... mi)
	{
		return new mon_item_group(mi);
	}

	public static abstract class mon_item
	{
		String m_name;
		double m_value;
		int m_time;
		int m_interval;
		boolean m_reseted;

		mon_item(String name, double value, int interval)
		{
			m_name = name;
			m_time = monitor.inst.m_base_time;
			m_interval = interval;
			m_value = value;
			m_reseted = false;
		}

		final boolean set(double v)
		{
			if (Math.ceil(v * 1000) == Math.ceil(m_value * 1000))
				return false;

			m_value = v;
			return true;
		}

		String value()
		{
			if (Math.ceil(m_value * 1000) == Math.ceil(m_value) * 1000)
				return String.format("%d", (int) m_value);

			return String.format("%.3f", m_value);
		}

		boolean reset(int now)
		{
			if (m_interval == Integer.MAX_VALUE)
			{
				peek();
				return true;
			}

			if (m_time > now)
				return false;

			while (m_time <= now)
				m_time += m_interval;

			return reset();
		}

		protected boolean reset()
		{
			return false;
		}

		protected boolean peek()
		{
			return false;
		}

		abstract public void fire(int i);

	}

	private static class mon_empty_item extends mon_item
	{
		mon_empty_item()
		{
			super("empty", 0, 0);
		}

		boolean reset(int now)
		{
			return false;
		}

		@Override
		public void fire(int i)
		{
		}
	}

	private static class mon_item_group extends mon_item
	{
		mon_item[] m_mi = new mon_item[] {};

		mon_item_group(mon_item[] mi)
		{
			super("group", 0, 0);
			m_mi = mi;
		}

		public void fire(int i)
		{
			for (mon_item mi : m_mi)
				mi.fire(i);
		}
	}

	private static class counter extends mon_item
	{
		AtomicLong m_total = new AtomicLong(0);

		public counter(String name, int interval)
		{
			super(name, 0, interval);
		}

		public void fire(int i)
		{
			m_total.addAndGet(i);
		}

		protected boolean peek()
		{
			return set(m_total.doubleValue());
		}

		protected boolean reset()
		{
			return set(m_total.getAndSet(0));
		}
	}

	private static class average extends mon_item
	{
		double m_total = 0;
		long m_count = 0;

		average(String name, int interval)
		{
			super(name, 0, interval);
		}

		synchronized public void fire(int i)
		{
			m_total += i;
			m_count++;
		}

		synchronized protected boolean peek()
		{
			return set(m_count == 0 ? 0 : m_total / m_count);
		}

		synchronized protected boolean reset()
		{
			boolean changed = peek();
			m_total = m_count = 0;

			return changed;
		}
	}

	private static class max extends mon_item
	{
		AtomicInteger m_max = new AtomicInteger(0);

		max(String name, int interval)
		{
			super(name, 0, interval);
		}

		public void fire(int i)
		{
			if (m_max.intValue() < i)
				m_max.set(i);
		}

		protected boolean peek()
		{
			return set(m_max.intValue());
		}

		protected boolean reset()
		{
			return set(m_max.getAndSet(0));
		}
	}

	private static class percent_if extends mon_item
	{
		long m_total = 0;
		long m_count = 0;

		func_if<Integer> m_if;

		percent_if(String name, int interval, func_if<Integer> fi)
		{
			super(name, 0, interval);
			m_if = fi;
		}

		synchronized public void fire(int i)
		{
			++m_total;
			if (m_if._if(new Integer(i)))
				++m_count;
		}

		synchronized protected boolean peek()
		{
			return set(m_total == 0 ? 0 : 1. * m_count / m_total);
		}

		synchronized protected boolean reset()
		{
			boolean changed = peek();
			m_total = m_count = 0;
			return changed;
		}
	}

	private static class percent_ge extends percent_if
	{
		percent_ge(String name, int interval, final int value)
		{
			super(name, interval, new func_if<Integer>()
			{
				public boolean _if(Integer e)
				{
					return e.intValue() >= value;
				}
			});
		}
	}

	public static class mon_params
	{
		static int risk_init_thread_cnt = tmsapp.get_config("tms.eval.workerCount", 8);
		static int risk_eval_thread_cnt = tmsapp.get_config("tms.eval.process.workerCount", 8);
		static int stat_query_thread_cnt = tmsapp.get_config("tms.stat.query.clientCount", 2);
		static int stat_eval_thread_cnt = tmsapp.get_config("tms.stat.eval.clientCount", 2);
		public static mon_item io_count, txn_count, txn_avg_resp_time, query_avg_resp_time;
		public static mon_item[] risk_init_deque_blocked_max, risk_init_request_count,
				risk_init_repstime_avg, risk_init_repstime_max, risk_eval_deque_blocked_max,
				risk_eval_request_count, risk_eval_repstime_avg, risk_eval_repstime_max;
		public static mon_item[] stat_query_deque_blocked_max, stat_query_request_max,
				stat_query_resptime_max;
		public static mon_item[] stat_eval_deque_blocked_max, stat_eval_request_max,
				stat_eval_resptime_max;

		public static void init()
		{
			io_count = monitor.make_group(monitor.new_counter("IO_COUNT"), monitor.new_counter(
					"IO_COUNT_1S", 1));
			txn_count = monitor.make_group(monitor.new_counter("TXN_COUNT"), monitor.new_counter(
					"TXN_COUNT_1S", 1));
			txn_avg_resp_time = monitor.make_group(//
					monitor.new_average("TXN_AVG_RESP_TIME"), //
					monitor.new_average("TXN_AVG_RESP_TIME_1S", 1), monitor.new_percent_ge(
							"TXN_TIMEOUT_500MS", 500),//
					monitor.new_percent_ge("TXN_TIMEOUT_1000MS", 1000), //
					monitor.new_percent_ge("TXN_TIMEOUT_2000MS", 2000), //
					monitor.new_percent_ge("TXN_TIMEOUT_3000MS", 3000));
			query_avg_resp_time = monitor.make_group(//
					monitor.new_average("QUERY_AVG_RESP_TIME"), //
					monitor.new_average("QUERY_AVG_RESP_TIME_1S", 1), //
					monitor.new_percent_ge("QUERY_TIMEOUT_100MS", Integer.MAX_VALUE, 100), //
					monitor.new_percent_ge("QUERY_TIMEOUT_200MS", Integer.MAX_VALUE, 200),//
					monitor.new_percent_ge("QUERY_TIMEOUT_300MS", Integer.MAX_VALUE, 300), //
					monitor.new_percent_ge("QUERY_TIMEOUT_400MS", Integer.MAX_VALUE, 400), //
					monitor.new_percent_ge("QUERY_TIMEOUT_500MS", Integer.MAX_VALUE, 500));
			init_risk_thread();
			init_stat_thread();
		}

		private static void init_risk_thread()
		{
			risk_init_deque_blocked_max = new mon_item[risk_init_thread_cnt];
			risk_init_request_count = new mon_item[risk_init_thread_cnt];
			risk_init_repstime_avg = new mon_item[risk_init_thread_cnt];
			risk_init_repstime_max = new mon_item[risk_init_thread_cnt];
			for (int i = 0, len = risk_init_thread_cnt; i < len; i++)
			{
				risk_init_deque_blocked_max[i] = monitor.new_max("RISK-INIT-DEQUE_BLOCK_MAX-" + i,
						1);
				risk_init_request_count[i] = monitor.new_counter("RISK-INIT-REQUEST_COUNT-" + i, 1);
				risk_init_repstime_avg[i] = monitor.new_average("RISK_INIT_REPSTIME_AVG-" + i, 1);
				risk_init_repstime_max[i] = monitor.new_average("RISK_INIT_REPSTIME_MAX-" + i, 1);
			}
			risk_eval_deque_blocked_max = new mon_item[risk_eval_thread_cnt];
			risk_eval_request_count = new mon_item[risk_eval_thread_cnt];
			risk_eval_repstime_avg = new mon_item[risk_eval_thread_cnt];
			risk_eval_repstime_max = new mon_item[risk_eval_thread_cnt];
			for (int i = 0, len = risk_eval_thread_cnt; i < len; i++)
			{
				risk_eval_deque_blocked_max[i] = monitor.new_max("RISK-EVAL-DEQUE_BLOCK_MAX-" + i,
						1);
				risk_eval_request_count[i] = monitor.new_counter("RISK-EVAL-REQUEST_COUNT-" + i, 1);
				risk_eval_repstime_avg[i] = monitor.new_average("RISK_EVAL_REPSTIME_AVG-" + i, 1);
				risk_eval_repstime_max[i] = monitor.new_average("RISK_EVAL_REPSTIME_MAX-" + i, 1);
			}
		}

		private static void init_stat_thread()
		{
			int stat_query_deque_size = (stat_query_thread_cnt + 3) / 4;
			int stat_eval_deque_size = (stat_eval_thread_cnt + 3) / 4;
			stat_query_deque_blocked_max = new mon_item[stat_query_deque_size];
			stat_query_request_max = new mon_item[stat_query_deque_size];
			stat_query_resptime_max = new mon_item[stat_query_deque_size];
			for (int i = 0, len = stat_query_deque_size; i < len; i++)
			{
				stat_query_deque_blocked_max[i] = monitor.new_max("STAT-QUERY_DEQUE_BLOCKED_MAX-"
						+ i, 1);
				stat_query_request_max[i] = monitor.new_max("STAT-QUERY_REQUEST_MAX-" + i, 1);
				stat_query_resptime_max[i] = monitor.new_max("STAT-QUERY_RESPTIME_MAX-" + i, 1);
			}
			stat_eval_deque_blocked_max = new mon_item[stat_eval_deque_size];
			stat_eval_request_max = new mon_item[stat_eval_deque_size];
			stat_eval_resptime_max = new mon_item[stat_eval_deque_size];
			for (int i = 0, len = stat_eval_deque_size; i < len; i++)
			{
				stat_eval_deque_blocked_max[i] = monitor.new_max(
						"STAT-EVAL_DEQUE_BLOCKED_MAX-" + i, 1);
				stat_eval_request_max[i] = monitor.new_max("STAT-EVAL_REQUEST_MAX-" + i, 1);
				stat_eval_resptime_max[i] = monitor.new_max("STAT-EVAL_RESPTIME_MAX-" + i, 1);
			}
		}
	}

	// public static class top_n extends mon_item
	// {
	// java.util.TreeMap<Integer, String> m_map = new TreeMap<Integer,
	// String>();
	// int m_max_count;
	//
	// top_n(int max_count, int interval)
	// {
	// super("", interval);
	// m_max_count = max_count;
	// }
	//
	// synchronized void fire(int i, Object name)
	// {
	// if (name == null)
	// return;
	//
	// m_map.put(i, name.toString());
	//
	// if (m_map.size() > m_max_count)
	// m_map.pollFirstEntry();
	// }
	//
	// synchronized void fire(int i)
	// {
	// }
	//
	// synchronized protected void reset()
	// {
	// m_value.append(m_map.toString());
	// m_map.clear();
	// }
	// }

	public static void main(String v[])
	{
		mon_item mi_c = monitor.make_group(//
				monitor.new_counter("TC_ALL"), //
				monitor.new_counter("TC_1S", 1), //
				monitor.new_counter("TC_30S", 30) //
				);
		mon_item mi_avg = monitor.make_group(//
				monitor.new_max("MAX-1S", 1), //
				monitor.new_max("MAX-60S", 60), //
				monitor.new_average("A-1S", 1), //
				monitor.new_percent_ge("GE-50-1S", 1, 50), //
				monitor.new_percent_ge("GE-50-60S", 60, 50)//
				);

		for (int i = 0; i < 100000; i++)
		{
			delay_tool.delay(100);

			mi_c.fire(1);
			mi_avg.fire((int) (Math.random() * 100 % 100));
		}

	}

}
