package cn.com.higinet.tms35.core.concurrent;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import cn.com.higinet.tms35.comm.clock;

public final class work_deque<E>
{
	Comparator<E> comp;
	zcircle<E> m_buff, m_buff_0;

	mutex lock = mutex.get_mutex_j5(false);
	cond cond_not_empty = lock.cond();
	cond cond_not_full = lock.cond();
	cond cond_no_batch_reader = lock.cond();
	cond cond_no_batch_writer = lock.cond();
	int want_read_count = 0, want_write_count = 0;
	long batch_reader = -1, batch_writer = -1;

	public work_deque(int max_buf_size)
	{
		this(max_buf_size, null);
	}

	public work_deque(int max_buf_size, Comparator<E> comp)
	{
		this.comp = comp;
		m_buff = new zcircle<E>(max_buf_size, comp);
		m_buff_0 = new zcircle<E>(0, comp);
	}

	@Override
	public String toString()
	{
		return m_buff.toString();
	}

	void enter_batch_read()
	{
		while (batch_reader != -1)
			cond_no_batch_reader.await();
		batch_reader = Thread.currentThread().getId();
	}

	void leave_batch_read()
	{
		batch_reader = -1;
		cond_no_batch_reader.signal();
	}

	void enter_batch_write()
	{
		while (batch_writer != -1)
			cond_no_batch_writer.await();
		batch_writer = Thread.currentThread().getId();
	}

	void leave_batch_write()
	{
		batch_writer = -1;
		cond_no_batch_writer.signal();
	}

	void wait_read(int tmout)
	{
		want_read_count++;
		cond_not_empty.await(tmout);
		want_read_count--;
	}

	void wait_read()
	{
		wait_read(Integer.MAX_VALUE);
	}

	void wait_write(int tmout)
	{
		want_write_count++;
		cond_not_full.await(tmout);
		want_write_count--;
	}

	void wait_write()
	{
		wait_write(Integer.MAX_VALUE);
	}

	void signal()
	{
		if (want_read_count > 0 && !m_buff.isempty())
		{
			cond_not_empty.signal();
		}

		if (want_write_count > 0 && !m_buff.isfull())
		{
			cond_not_full.signal();
		}
	}

	public void clear()
	{
		this.lock.lock();
		if (m_buff.size() > 0)
		{
			m_buff.clear();
			cond_not_full.signal();
		}
		this.signal();
		this.lock.unlock();
	}

	public int drainTo(Collection<E> c, int count)
	{
		lock.lock();
		try
		{
			while (m_buff.isempty())
				this.wait_read();

			return m_buff.get(c, count == 0 ? m_buff.size : count);
		}
		finally
		{
			this.signal();
			lock.unlock();
		}
	}

	public int drainTo(Collection<E> c, int count, int timeoutMs)
	{
		clock timer = new clock();
		lock.lock();
		try
		{
			this.enter_batch_read();
			int ret = 0, left = 0;
			while (ret < count && timer.left(timeoutMs) > 0)
			{
				while (m_buff.isempty() && (left = timer.left(timeoutMs)) > 0)
					this.wait_read(left);

				ret += m_buff.get(c, count - ret);
			}

			return ret;
		}
		finally
		{
			this.signal();
			this.leave_batch_read();
			lock.unlock();
		}
	}

	public E take(int timeoutMs)
	{
		clock c = new clock();
		lock.lock();
		try
		{
			int left = 0;
			while (this.size() == 0 && (left = c.left(timeoutMs)) > 0)
				this.wait_read(left);

			return m_buff_0.isempty() ? m_buff.get() : m_buff_0.get();
		}
		finally
		{
			this.signal();
			lock.unlock();
		}
	}

	public boolean offer(E e, int timeoutMs)
	{
		clock c = new clock();
		lock.lock();
		try
		{
			int left = 0;
			while (m_buff.isfull() && (left = c.left(timeoutMs)) > 0)
				this.wait_write(left);

			return m_buff.add(e) == 0;
		}
		finally
		{
			this.signal();
			lock.unlock();
		}
	}

	public boolean offer(E e)
	{
		return offer(e, Integer.MAX_VALUE);
	}

	public boolean offer_first(E e)
	{
		lock.lock();
		try
		{
			if (m_buff_0.max_size()==0)
				m_buff_0 = new zcircle<E>(m_buff.max_size(), m_buff._compare);

			while (m_buff_0.isfull())
				this.wait_write();

			return m_buff_0.add_first(e) == 0;
		}
		finally
		{
			this.signal();
			lock.unlock();
		}
	}

	public boolean offer(List<E> c, int b, int e)
	{
		return comp == null ? offer_no_sort(c, b, e) : offer_sort(c, b, e);
	}

	private boolean offer_sort(List<E> c, int b, int e)
	{
		if (b >= e)
			return false;

		int hi = b;
		E h = c.get(b);
		try
		{
			lock.lock();
			while ((e - b) + m_buff.size() > m_buff.max_size())
				cond_not_full.await(200);
			for (int i = b + 1; i < e; i++)
			{
				if (comp.compare(h, c.get(i)) == 0)
					continue;
				m_buff.add(c, hi, i);
				hi = i;
				h = c.get(hi);
			}
			m_buff.add(c, hi, e);
		}
		finally
		{
			cond_not_empty.signal();
			lock.unlock();
		}

		return true;
	}

	private boolean offer_no_sort(List<E> c, int b, int e)
	{
		if (b >= e)
			return false;

		lock.lock();
		this.enter_batch_write();

		try
		{
			while (m_buff.size() + (e - b) > m_buff.max_size())
				this.wait_write();

			return m_buff.add(c, b, e) == 0;
		}
		finally
		{
			this.signal();
			this.leave_batch_write();
			lock.unlock();
		}
	}

	public int size()
	{
		lock.lock();
		try
		{
			return m_buff.size + m_buff_0.size;
		}
		finally
		{
			lock.unlock();
		}
	}

	public boolean is_empty()
	{
		return 0 == size();
	}
}
