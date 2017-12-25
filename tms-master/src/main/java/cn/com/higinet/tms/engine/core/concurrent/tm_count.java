package cn.com.higinet.tms.engine.core.concurrent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import cn.com.higinet.tms.engine.comm.delay_tool;
import cn.com.higinet.tms.engine.comm.tm_tool;

public class tm_count
{
	static class period_count
	{
		@Override
		public String toString()
		{
			return "["+index+","+count+"]";
		}
		public period_count(long nowIndex, int count2)
		{
			index = nowIndex;
			count = count2;
		}

		long index;
		int count;
	}

	LinkedList<period_count> m_pc = null;
	int m_period; // 间隔毫秒数

	public tm_count(int period_ms, int max_time_ms)
	{
		m_period = period_ms;
		m_pc = new LinkedList<period_count>();
	}

	public int size()
	{
		return m_pc.size();
	}

	final private long index(long now)
	{
		return  (now / m_period);
	}

	public void add_now(long now, int count)
	{
		long now_index = index(now);
		if (m_pc.isEmpty() || m_pc.getLast().index != now_index)
		{
			m_pc.addLast(new period_count(now_index, count));
			return;
		}

		m_pc.getLast().count += count;
	}

	public int get_time_span()
	{
		int size = size();
		if (size == 0)
			return 0;
		if (size == 1)
			return this.m_period;

		return (int) (m_pc.getLast().index - m_pc.getFirst().index + 1) * m_period;
	}

	public int get_count()
	{
		int c = 0;
		for (period_count pc : m_pc)
			c += pc.count;

		return c;
	}

	public float get_tps()
	{
		return (int) (10000 * get_tpms()) / 10.f;
	}

	public float get_tpms()
	{
		int span = get_time_span();
		if (span == 0)
			return -1f;
		return (int) (10000.f * get_count() / span) / 10000.f;
	}

	public long remove(int lave_count)
	{
		if (lave_count <= 0||m_pc.isEmpty())
			return -1;

		period_count pc=null;
		ListIterator<period_count> it = m_pc.listIterator(m_pc.indexOf(m_pc.getLast()));
		while (lave_count > 0 && it.hasPrevious())
		{
			pc=it.previous();
			lave_count -= pc.count;
		}

		if (lave_count > 0)
			return -1;
		
		if(it.hasPrevious())
			pc=it.previous();

		while(pc.index!=m_pc.getFirst().index)
			m_pc.removeFirst();
		
		return pc.index*m_period;
	}

	public static void main(String[] args)
	{
		Random r=new Random();
		 tm_count tc = new tm_count(200, 100000);
		 for (int i = 0; i < 100000; i++)
		 {
			 tc.add_now(tm_tool.lctm_ms(), 200);
			 delay_tool.delay(r.nextInt(100));
			 tc.remove(8000);
			 System.out.println(tc.get_count());
		 }

		List<Integer> cc=new ArrayList<Integer>(); 
		ArrayBlockingQueue<Integer> d = new ArrayBlockingQueue<Integer>(100, false);
		d.offer(0);
		d.peek();
		d.poll();
		d.drainTo(cc, 10);

		// zfifo_tm<Integer> fifo=new zfifo_tm<Integer>(30000,1000);
		// for(int i=0;i<1000000;i++)
		// {
		// }
		//		
	}

}
