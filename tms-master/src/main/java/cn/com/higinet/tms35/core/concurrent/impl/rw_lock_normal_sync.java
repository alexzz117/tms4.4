package cn.com.higinet.tms35.core.concurrent.impl;

import cn.com.higinet.tms35.comm.tm_tool;
import cn.com.higinet.tms35.core.concurrent.rw_lock;

public class rw_lock_normal_sync extends rw_lock
{
	public rw_lock_normal_sync(Object coreObj)
	{
		super(coreObj);
	}

	public boolean wait_read(long tmout)
	{
		synchronized (core)
		{
			long start = tm_tool.lctm_ms();
			Thread self = Thread.currentThread();
			long tmwait = tmout;
			for (; num_writer > 0;)
			{
				if (self == writer)
					break;
				try
				{
					core.wait(tmwait);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
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

				try
				{
					core.wait();
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (++num_reader == 1)
				writer = self;
		}
	}

	public void done_read()
	{
		synchronized (core)
		{
			--num_reader;

			if (num_writer == 0 && num_reader == 0)
				writer = null;

			core.notifyAll();
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			writer = self;
			num_writer++;
		}
	}

	public boolean wait_write(long tmout)
	{
		synchronized (core)
		{
			long start = tm_tool.lctm_ms();
			Thread self = Thread.currentThread();
			long tmwait = tmout;
			for (; num_writer > 0 || num_reader > 0;)
			{
				if (self == writer)
					break;

				try
				{
					core.wait(tmwait);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
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

	public void done_write()
	{
		synchronized (core)
		{
			--num_writer;

			if (num_writer == 0 && num_reader == 0)
				writer = null;

			core.notifyAll();
		}
	}

	public static void main(String[] argv)
	{
		class mythread extends Thread
		{
			public rw_lock lock;

			public void run()
			{
				while (true)
					try
					{
						Thread.sleep(100);
						lock.wait_write();
						lock.wait_write();
						Thread.sleep(50);
						System.out.println(this);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					finally
					{
						lock.done_write();
						lock.done_write();
					}
			}

		}
		;
		rw_lock lock = new rw_lock_normal_sync(null);

		for (int i = 0; i < 5; i++)
		{
			mythread t = new mythread();
			t.lock = lock;
			t.start();

		}

		try
		{
			Thread.sleep(10000000);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
