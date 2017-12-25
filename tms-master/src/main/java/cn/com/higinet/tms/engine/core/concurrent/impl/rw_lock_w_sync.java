package cn.com.higinet.tms.engine.core.concurrent.impl;

import cn.com.higinet.tms.engine.comm.tm_tool;

public class rw_lock_w_sync extends rw_lock_normal_sync
{
	public rw_lock_w_sync(Object coreObj)
	{
		super(coreObj);
	}

	int num_writer_wait = 0;

	public boolean wait_read(long tmout)
	{
		long start = tm_tool.lctm_ms();
		Thread self = Thread.currentThread();
		long tmwait = tmout;
		synchronized (core)
		{
			for (; num_writer > 0 || num_writer_wait > 0;)
			{
				if (self == writer)
					break;

				try
				{
					core.wait(tmwait);
				}
				catch (InterruptedException e)
				{
					return false;
				}

				tmwait = tmout - (tm_tool.lctm_ms() - start);
				if (tmwait <= 0)
					return false;
			}

			if (++num_reader == 1)
				writer = self;

			return true;
		}
	}

	public void wait_read()
	{
		Thread self = Thread.currentThread();
		synchronized (core)
		{
			for (; num_writer > 0 || num_writer_wait > 0;)
			{
				if (self == writer)
					break;

				try
				{
					core.wait();
				}
				catch (InterruptedException e)
				{
					return;
				}
			}

			if (++num_reader == 1)
				writer = self;
		}
	}

	public void wait_write()
	{
		Thread self = Thread.currentThread();
		synchronized (core)
		{
			for (; num_writer > 0 || num_reader > 0;)
			{
				if (self == writer)
					break;
				try
				{
					core.wait();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
					return;
				}
				finally
				{
					num_writer_wait--;
				}
			}

			writer = self;
			num_writer++;
		}
	}

	public boolean wait_write(long tmout)
	{
		long start = tm_tool.lctm_ms();
		Thread self = Thread.currentThread();
		long tmwait = tmout;
		synchronized (core)
		{
			for (; num_writer > 0 || num_reader > 0;)
			{
				if (self == writer)
					break;

				num_writer_wait++;
				try
				{
					core.wait(tmwait);
				}
				catch (InterruptedException e)
				{
					return false;
				}
				finally
				{
					num_writer_wait--;
				}

				tmwait = tmout - (System.currentTimeMillis() - start);
				if (tmwait <= 0)
					return false;
			}

			++num_writer;
			writer = self;

			return true;
		}
	}
}