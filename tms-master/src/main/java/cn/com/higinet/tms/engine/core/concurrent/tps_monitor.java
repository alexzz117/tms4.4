package cn.com.higinet.tms.engine.core.concurrent;

import java.util.concurrent.atomic.AtomicLong;

public class tps_monitor
{
	private static class mon_clock implements Runnable
	{
		private volatile long m_clock;

		mon_clock()
		{
			m_clock = System.currentTimeMillis();

			Thread t = new Thread(this);
			t.setDaemon(true);
			t.setName(this.getClass().getSimpleName());
			t.start();
		}

		@Override
		public void run()
		{
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			for (;;)
			{
				m_clock = System.currentTimeMillis() / 100;
				try
				{
					Thread.sleep(10);
				}
				catch (InterruptedException e)
				{
				}
			}
		}

		public long clock()
		{
			return m_clock;
		}
	}

	static private mon_clock m_hz = new mon_clock();

	private static class tps_counter
	{
		AtomicLong m_sum, m_tm_res;
		long m_last_clock;
		long m_mask;
		long m_size;
		int[] m_counter;
		long[] m_timer;

		public tps_counter(int num_second)
		{
			num_second *= 10;
			m_size = 64;
			while (m_size < num_second)
				m_size <<= 1;

			m_sum = new AtomicLong(0);
			m_tm_res = new AtomicLong(0);

			m_mask = m_size - 1;
			m_counter = new int[(int) m_size];
			m_timer = new long[(int) m_size];
			m_last_clock = m_hz.clock();
		}

		private int index(long pos)
		{
			return (int) (pos & m_mask);
		}

		private void clean_to(long now)
		{
			final long d = now - m_last_clock;
			if (d == 0)
				return;

			if (d < m_size)
			{
				for (long i = m_last_clock + 1; i <= now; i++)
				{
					int j = index(i);
					m_counter[j] = 0;
					m_timer[j] = 0;
				}
			}
			else
			{
				for (int i = 0; i < m_size; i++)
				{
					m_counter[i] = 0;
					m_timer[i] = 0;
				}
			}
		}

		public void mark_end(int tm_respone)
		{
			final long now = m_hz.clock();
			synchronized (this)
			{
				clean_to(now);
				int j = index(m_last_clock = now);
				++m_counter[j];
				m_timer[j] += tm_respone;
			}

			m_tm_res.addAndGet(tm_respone);
			m_sum.incrementAndGet();
		}

		public int count(int second)
		{
			final long now = m_hz.clock();
			long rc = 0, b = now - Math.min(m_size, second * 10) + 1;
			synchronized (this)
			{
				for (; b <= m_last_clock; b++)
					rc += m_counter[index(b)];
			}
			return (int) rc;
		}

		public long sum()
		{
			return m_sum.longValue();
		}

		public int art(int second)
		{
			final long now = m_hz.clock();
			long count = 0, b = now - Math.min(m_size, second * 10) + 1;
			long tm_all = 0;
			synchronized (this)
			{
				for (; b <= m_last_clock; b++)
				{
					int j = index(b);
					count += m_counter[j];
					tm_all += m_timer[j];
				}
			}

			return count > 0 ? (int) (tm_all / count) : 0;
		}

		public long art_all()
		{
			long sum = m_sum.get();
			return sum > 0 ? m_tm_res.get() / sum : 0;
		}
	}

	tps_counter m_tc;

	public tps_monitor(int seconds)
	{
		m_tc = new tps_counter(seconds);
	}

	public tps_monitor()
	{
		this(10);
	}

	final public void mark(int tm_mill_sec)
	{
		m_tc.mark_end(tm_mill_sec);
	}

	final public int count(int second)// 最近N秒的交易量
	{
		return m_tc.count(second);
	}

	final public int art(int second)// 最近N秒平均响应时间
	{
		return m_tc.art(second);
	}

	final public long sum_all()// 组件启动以来的总交易量
	{
		return m_tc.sum();
	}

	final public long art_all()// 组件启动以来的总交易量
	{
		return m_tc.art_all();
	}

	public static void main(String argv[])
	{
		final tps_monitor tc = new tps_monitor(10);// 10秒
		for (int i = 0; i < 200; i++)
		{
			Thread x = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					for (int i = 0; i < 100000; i++)
					{
						try
						{
							Thread.sleep(9, 990000);
						}
						catch (InterruptedException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						tc.mark(10);
					}
				}
			});
			x.setDaemon(true);
			x.start();
		}
		AtomicLong t=new AtomicLong(0);

		for (;;)
		{
			System.out.println(String.format(
					"count_10=%d, count_1=%d,sum=%d,art_10=%d,art_1=%d",
					tc.count(10), tc.count(1), tc.sum_all(), tc.art(10),
					tc.art(1)));
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		
		
	}
}
