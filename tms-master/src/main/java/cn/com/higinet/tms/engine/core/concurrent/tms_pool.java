package cn.com.higinet.tms.engine.core.concurrent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.delay_tool;

public class tms_pool
{
	static Logger log=LoggerFactory.getLogger(tms_pool.class);
	
	static class rejected implements RejectedExecutionHandler
	{
		mutex mutex_ = mutex.get_mutex_j5(false);
		cond cond_ = mutex_.cond();
		int accesser, wait_count;

		rejected()
		{
			accesser = 0;
			wait_count = 0;
		}

		public int wait_count()
		{
			try
			{
				mutex_.lock();
				return wait_count;
			}
			finally
			{
				mutex_.unlock();
			}
		}

		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
		{
			try
			{
				mutex_.lock();
				wait_count++;
				while (accesser > 0)
					cond_.await();
				wait_count--;
				accesser++;
				executor.getQueue().put(r);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			finally
			{
				accesser--;
				mutex_.cond().signal();
				mutex_.unlock();
			}
		}
	}

	static class executor extends ThreadPoolExecutor
	{
		rejected m_rejected;
		public executor(int corePoolSize, int maximumPoolSize, int QueueSize)
		{
			super(corePoolSize, maximumPoolSize, 60 * 5, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>(QueueSize), new rejected());
			m_rejected=(rejected)this.getRejectedExecutionHandler();
		}

		public void shutdown()
		{
			while (m_rejected.wait_count()>0 || this.getQueue().size() > 0)
				delay_tool.delay(100);

			super.shutdown();
		}
	}

	private executor m_pool;
	String m_name;
	public tms_pool(int core_thr_count,int max_thr_count,int deque_size,String name)
	{
		m_name=name;
		m_pool = new executor(core_thr_count,max_thr_count,deque_size);
	}

	public void execute(Runnable command)
	{
		m_pool.execute(command);
	}

	public void shutdown()
	{
		log.info("begin shutdown tms_thread_pool["+m_name+"]");
		m_pool.shutdown();
		log.info("shutdown tms_thread_pool["+m_name+"] over.");
	}
}
