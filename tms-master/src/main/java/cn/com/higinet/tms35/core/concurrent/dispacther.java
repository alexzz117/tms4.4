package cn.com.higinet.tms35.core.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.comm.str_tool;

public class dispacther
{
	
	private static final Logger log = LoggerFactory.getLogger( dispacther.class );

	// 实现原则：
	// 1）如果一个用户现有交易已经分发到某线程并且还未处理完成，依旧分发到这个线程
	// 2）否则，调度给一个交易最少的线程

	static class task_info implements Comparable<task_info>
	{
		public task_info(int thread_id, int queued_count)
		{
			this.thread_id = thread_id;
			this.queued_count = queued_count;
		}

		void inc_count()
		{
			++queued_count;
		}

		int dec_count()
		{
			return --queued_count;
		}

		int thread_id; // 交易目前所在的线程ID
		int queued_count; // 目前排队的交易数量

		@Override
		public int compareTo(task_info o)
		{
			int c = queued_count - o.queued_count;

			if (c != 0)
				return c;

			return thread_id - o.thread_id;
		}

		public String toString()
		{
			return "{" + thread_id + "," + queued_count + "}";
		}
	}

	int m_thread_count; // 线程池的工作线程的数量

	TreeSet<task_info> m_count_index; // 以每个线程队列在排队的交易数量排序的线程处理信息
	task_info[] m_thread_index;// 以线程序号为索引的数组，存储了每个线程正在排队的交易数量
	Map<String, task_info> m_user_task; // 存储每个用户在排队的交易数量

	public dispacther(int thread_count)
	{
		this.m_thread_count = thread_count;
		this.m_user_task = new HashMap<String, task_info>();
		this.m_count_index = new TreeSet<task_info>();
		this.m_thread_index = new task_info[this.m_thread_count];
		for (int i = 0; i < this.m_thread_count; i++)
		{
			m_thread_index[i] = new task_info(i, 0);
			m_count_index.add(m_thread_index[i]);
		}
	}

	synchronized public int clac_worker_id(String disp)
	{
		if (str_tool.is_empty(disp))
		{
			disp = "no-disp";
		}

		task_info ti = m_user_task.get(disp);
		if (ti == null)
		{
			ti = m_count_index.pollFirst();

			ti.inc_count();
			m_user_task.put(disp, new task_info(ti.thread_id, 1));

			this.m_count_index.add(ti);
		}
		else
		{
			ti.inc_count();

			ti = m_thread_index[ti.thread_id];

			this.m_count_index.remove(ti);
			ti.inc_count();
			this.m_count_index.add(ti);
		}

		return ti.thread_id;
	}

	synchronized public void notify_trans_over(String disp)
	{
		task_info ti = m_user_task.get(disp);
		if (ti == null)
		{
			log.error(String.format("当前处理队列中无该用户(%s)的交易", disp));
			return;
		}

		if (ti.dec_count() == 0)
		{
			m_user_task.remove(disp);
		}

		ti = m_thread_index[ti.thread_id];
		m_count_index.remove(ti);
		ti.dec_count();
		m_count_index.add(ti);
	}
}
