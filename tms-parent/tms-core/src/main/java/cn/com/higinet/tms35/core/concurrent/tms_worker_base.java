package cn.com.higinet.tms35.core.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.comm.delay_tool;

public abstract class tms_worker_base<E> extends tms_worker<E> implements Runnable
{
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	private String m_name;
	protected AtomicBoolean m_break_event, m_running;
	protected work_deque<E> m_request;

	public tms_worker_base(String name, work_deque<E> shared_request)
	{
		m_name = name;
		m_request = shared_request;
		m_break_event = new AtomicBoolean(false);
		m_running = new AtomicBoolean(false);
	}

	public tms_worker_base(String name, int requst_max_size)
	{
		this(name, new work_deque<E>(requst_max_size));
	}

	public void set_name(String n)
	{
		m_name = n;
	}

	@Override
	public String name()
	{
		return m_name;
	}

	@Override
	public boolean request(E e)
	{
		return m_request.offer(e);
	}

	@Override
	final public boolean request(List<E> el)
	{
		return request(el, 0, el.size());
	}

	@Override
	public boolean request(List<E> el, int b, int e)
	{
		return m_request.offer(el, b, e);
		// for (; b < e; b++)
		// request(el.get(b));
		// return true;
	}

	public void run()
	{
		pre_run();
		while (!m_break_event.get())
		{
			try
			{
				run_once();
			}
			catch (Throwable e)
			{
				log.error(null, e);
			}
		}
		post_run();
		m_running.set(false);
		log.info("shutdown " + name() + " over.");
	}

	protected void run_once()
	{
	}

	protected void pre_run()
	{
	}

	protected void post_run()
	{
	}

	@Override
	public void start()
	{
		Thread th = new Thread(this);
		if (m_name != null)
			th.setName(m_name);
		th.start();
		m_running.set(true);
	}

	@Override
	public void shutdown(boolean abort)
	{
		log.info("shutdown " + name());
		m_break_event.set(true);
	}

	@Override
	public void join()
	{
		m_break_event.set(true);
		while (m_running.get())
			delay_tool.delay(10);
	}

	@Override
	public void setup(String[] string)
	{
	}

	public int drainTo(Collection<E> c, int count, int timeoutMs)
	{
		return m_request.drainTo(c, count, timeoutMs);
	}

	public int drainToBatch(Collection<E> c, int count, int timeoutMs)
	{
		return m_request.drainTo(c, count, timeoutMs);
	}

	public int drainTo(Collection<E> c, int count)
	{
		return m_request.drainTo(c, count);
	}

	public int drainTo(Collection<E> c)
	{
		return m_request.drainTo(c, Integer.MAX_VALUE);
	}

	public boolean is_empty()
	{
		return m_request.is_empty();
	}

	public boolean offer(E e, int timeoutMs)
	{
		return m_request.offer(e, timeoutMs);
	}

	public boolean offer(E e)
	{
		return m_request.offer(e);
	}

	public boolean offer_first(E e)
	{
		return m_request.offer_first(e);
	}

	public int size()
	{
		return m_request.size();
	}

	public E take(int timeoutMs)
	{
		return m_request.take(timeoutMs);
	}
}
