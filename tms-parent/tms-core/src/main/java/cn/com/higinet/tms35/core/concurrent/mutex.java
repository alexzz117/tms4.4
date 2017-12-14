package cn.com.higinet.tms35.core.concurrent;

import cn.com.higinet.tms35.core.concurrent.impl.mutex_j5;
import cn.com.higinet.tms35.core.concurrent.impl.mutex_sync;

public abstract class mutex
{
	public static mutex get_mutex_sync()
	{
		return new mutex_sync();
	}
	
	public static mutex get_mutex_j5(boolean f)
	{
		return new mutex_j5(f);
	}
	
	public abstract boolean lock(long tmout);

	public abstract boolean lock();

	public abstract void unlock();
	
	public cond cond()
	{
		return null;
	}
}