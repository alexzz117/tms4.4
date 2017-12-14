package cn.com.higinet.tms35.core.concurrent.impl;

import cn.com.higinet.tms35.comm.tm_tool;

public class rw_lock_r_sync extends rw_lock_normal_sync
{
	int num_reader_wait = 0;

	public rw_lock_r_sync(Object coreObj)
	{
		super(coreObj);
	}

	public boolean wait_read(long tmout)
	{
		long start = tm_tool.lctm_ms();
		Thread self = Thread.currentThread();
		long tmwait = tmout;
		synchronized (core)
		{
			for (; num_writer > 0;)
			{
				if (self == writer)
					break;

				num_reader_wait++;
				try
				{
					core.wait();
				}
				catch (InterruptedException e)
				{
					return false;
				}
				finally
				{
					num_reader_wait--;
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
			for (; num_writer > 0;)
			{
				if (self == writer)
					break;

				num_reader_wait++;
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
					num_reader_wait--;
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
			for (; num_writer > 0 || num_reader > 0 || num_reader_wait > 0;)
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
			for (; num_writer > 0 || num_reader > 0 || num_reader_wait > 0;)
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

			++num_writer;
			writer = self;

			return true;
		}
	}
}