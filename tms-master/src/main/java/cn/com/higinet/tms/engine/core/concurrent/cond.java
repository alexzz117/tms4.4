package cn.com.higinet.tms.engine.core.concurrent;

public abstract class cond
{
	public abstract boolean await(int timeMs);
	public abstract void await();
	public abstract void signal();
	public abstract void signal_all();
}
