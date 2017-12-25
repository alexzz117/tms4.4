package cn.com.higinet.tms35.core.concurrent.impl;

import cn.com.higinet.tms35.comm.tm_tool;
import cn.com.higinet.tms35.core.concurrent.mutex;

public class mutex_sync extends mutex
{
	private Thread owner=null;
	private int    ref=0;

	public synchronized boolean  lock(long tmout)
	{
		long start=tm_tool.lctm_ms();
		Thread self=Thread.currentThread();
		long tmwait=tmout;
		for(;owner!=null && !owner.equals(self);)
		{
			try
			{
				this.wait(tmwait);
			}
			catch (InterruptedException e)
			{
				return false;
			}

			tmwait=tmout-(tm_tool.lctm_ms()-start);
			if(tmwait<=0)
				return false;
		}

		owner=self;
		ref++;
		
		return true;		
	}
	

	public synchronized boolean  lock()
	{
		Thread self=Thread.currentThread();
		for(;owner!=null && !owner.equals(self);)
		{
			try
			{
				this.wait();
			}
			catch (InterruptedException e)
			{
				return false;
			}
		}

		owner=self;
		ref++;
		
		return true;		
	}


	public synchronized void unlock()
	{
		if(owner==null || !Thread.currentThread().equals(owner))
		{
			System.out.print("释放未占用的mutex");
			return;
		}
		
		if(--ref==0)
		{
			owner=null;
			this.notifyAll();
		}
	}
}
