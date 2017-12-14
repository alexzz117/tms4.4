package cn.com.higinet.tms35.core.concurrent.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.com.higinet.tms35.core.concurrent.mutex;

public class mutex_j5 extends mutex
{
	Lock lock;

	public mutex_j5(boolean f)
	{
		lock = new ReentrantLock(f);
	}

	@Override
	public boolean lock(long tmout)
	{
		try
		{
			return lock.tryLock(tmout, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e)
		{
			return false;
		}
	}

	@Override
	public boolean lock()
	{
		lock.lock();
		return true;
	}

	@Override
	public void unlock()
	{
		lock.unlock();
	}

	@Override
	public cn.com.higinet.tms35.core.concurrent.cond cond()
	{
		return new cond_j5(lock.newCondition());
	}

}
