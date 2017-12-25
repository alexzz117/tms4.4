package cn.com.higinet.tms.engine.core.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;

import cn.com.higinet.tms.engine.comm.clock;

public class counter
{
	int i;
	public counter()
	{
	}

	synchronized public void set_error(int e)
	{
		if (i < 0)
			return;

		i = e;
		this.notify();
	}

	synchronized public int dec()
	{
		if (i > 0)
			--i;

		this.notify();
		return i;
	}

	synchronized public void inc()
	{
		if (i < 0)
			return;
		++i;
	}

	synchronized public int wait_gt_0()
	{
		while (i > 0)
		{
			try
			{
				this.wait(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return i;
	}
	
	synchronized public int wait_gt(int timeoutMs)
	{
		clock c = new clock();
		int left = 0;
		while (i > 0 && (left = c.left(timeoutMs))>0)
		{
			try
			{
				if(left > 10){
					this.wait(10);
				}else{
					this.wait(left);
				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return i;
	}
	
	synchronized public int wait_gt(int c, long wait)
	{
		while (i > c)
		{
			try
			{
				this.wait(wait);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return i;
	}

	synchronized public int wait_ge(int c, AtomicBoolean cancel_flag)
	{
		while (i >= c && !cancel_flag.get())
		{
			try
			{
				this.wait(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return i;
	}

	synchronized public int get()
	{
		return i;
	}

//	synchronized public int get_max()
//	{
//		return max;
//	}
}
