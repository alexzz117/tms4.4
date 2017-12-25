package cn.com.higinet.tms.engine.comm;

import java.util.concurrent.atomic.AtomicInteger;

public class reference_count
{
	java.util.concurrent.atomic.AtomicInteger m_ref;

	public reference_count()
	{
		m_ref = new AtomicInteger(1);
	}

	public int inc_ref()
	{
		return m_ref.incrementAndGet();
	}

	public int release()
	{
		return m_ref.decrementAndGet();
	}
}
