package cn.com.higinet.tms35.core.concurrent;

import cn.com.higinet.tms35.core.concurrent.impl.rw_lock_j5;
import cn.com.higinet.tms35.core.concurrent.impl.rw_lock_sync;

public abstract class rw_lock
{
	public static enum priority
	{
		none, write, read
	}
	
	public static enum type
	{
		jdk_sync,j5lock
	}

	
	public static rw_lock get_sync_rw_lock(Object coreObj,priority pri)
	{
		return new rw_lock_sync(coreObj,pri);
	}

	public static rw_lock get_sync_rw_lock(Object coreObj)
	{
		return new rw_lock_sync(coreObj,priority.none);
	}

	public static rw_lock get_j5_rw_lock()
	{
		return new rw_lock_j5(null);
	}
	
	
	protected Object core=null;
	protected Thread writer=null;
	protected int    num_writer=0,num_reader=0;
	
	protected rw_lock(Object core_obj)
	{
		core=core_obj;
		if(core==null)
			core=this;
	}

	public abstract void wait_read();
	public abstract boolean wait_read(long tmout);

	public abstract void done_read();

	public abstract void wait_write();
	public abstract boolean wait_write(long tmout);
	public abstract void done_write();
}
