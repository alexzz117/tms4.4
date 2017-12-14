package cn.com.higinet.tms35.core.concurrent.impl;

import cn.com.higinet.tms35.core.concurrent.rw_lock;

public class rw_lock_sync extends rw_lock
{
	private rw_lock lock;

	public rw_lock_sync(Object coreObj)
	{
		this(coreObj, priority.none);
	}

	public rw_lock_sync(Object coreObj, priority pri)
	{
		super(coreObj);
		if (pri == priority.none)
			lock = new rw_lock_normal_sync(coreObj);
		else if (pri == priority.write)
			lock = new rw_lock_w_sync(coreObj);
		else
			lock = new rw_lock_r_sync(coreObj);
	}

	@Override
	public void done_read()
	{
		lock.done_read();
	}

	@Override
	public void done_write()
	{
		lock.done_write();
	}

	@Override
	public void wait_read()
	{
		lock.wait_write();
	}

	@Override
	public boolean wait_read(long tmout)
	{
		return lock.wait_read(tmout);
	}

	@Override
	public void wait_write()
	{
		lock.wait_write();
	}

	@Override
	public boolean wait_write(long tmout)
	{
		return lock.wait_write(tmout);
	}

}
