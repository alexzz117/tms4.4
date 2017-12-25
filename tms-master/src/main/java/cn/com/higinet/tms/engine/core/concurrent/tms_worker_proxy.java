package cn.com.higinet.tms.engine.core.concurrent;


@SuppressWarnings("unchecked")
public class tms_worker_proxy<E> extends tms_worker_base<E>
{
	protected tms_worker<E>[] m_worker_pool;

	public tms_worker_proxy(String name, int deque_size, int thread_cnt)
	{
		super(name, deque_size);
		m_worker_pool = new tms_worker[thread_cnt];
	}

	public tms_worker_proxy(String name, work_deque<E> request, int thread_cnt)
	{
		super(name, request);
		m_worker_pool = new tms_worker[thread_cnt];
	}
	
	public boolean request(int thread_idx, E e)
	{
		if (thread_idx >= m_worker_pool.length)
		{
			return false;
		}
		tms_worker<E> worker = m_worker_pool[thread_idx];
		return worker.request(e);
	}

	@Override
	public void shutdown(boolean abort)
	{
		log.info("begin shutdown " + name());
		for (tms_worker<E> w : m_worker_pool)
			w.shutdown(abort);
		for (tms_worker<E> w : m_worker_pool)
			w.join();
		log.info("shutdown " + name() + " over.");
	}
	
	protected void start_this()
	{
		super.start();
	}

	@Override
	public void start()
	{
		for (tms_worker<E> w : m_worker_pool)
			w.start();
	}

	@Override
	public void setup(String[] string)
	{
		for (tms_worker<E> w : m_worker_pool)
			w.setup(string);
	}
}
