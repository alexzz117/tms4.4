package cn.com.higinet.tms.engine.core.concurrent.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import cn.com.higinet.tms.engine.core.concurrent.cond;

public class cond_j5 extends cond
{
	Condition cond_;
	public cond_j5(Condition newCondition)
	{
		cond_=newCondition;
	}

	@Override
	public void signal()
	{
		cond_.signal();
	}

	@Override
	public void signal_all()
	{
		cond_.signalAll();
	}

	@Override
	public boolean await(int timeMs)
	{
		try
		{
			return cond_.await(timeMs,TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void await()
	{
		try
		{
			cond_.await();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

}
