package cn.com.higinet.tms.engine.core.concurrent.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cn.com.higinet.tms.engine.core.concurrent.rw_lock;

public class rw_lock_j5 extends rw_lock
{
	java.util.concurrent.locks.ReadWriteLock lock;

	public rw_lock_j5(Object coreObj)
	{
		super(coreObj);
		lock=new ReentrantReadWriteLock();
	}

	@Override
	public void done_read()
	{
		lock.readLock().unlock();
	}

	@Override
	public void done_write()
	{
		lock.writeLock().unlock();
	}

	@Override
	public void wait_read()
	{
		lock.readLock().lock();
	}

	@Override
	public boolean wait_read(long tmout)
	{
		try
		{
			return lock.readLock().tryLock(tmout, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void wait_write()
	{
		lock.writeLock().lock();
	}

	@Override
	public boolean wait_write(long tmout)
	{
		try
		{
			return lock.writeLock().tryLock(tmout, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
