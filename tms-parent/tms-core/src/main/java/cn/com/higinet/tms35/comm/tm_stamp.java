package cn.com.higinet.tms35.comm;

public class tm_stamp
{
	private long timestamp;
	public tm_stamp()
	{
	}
	
	public tm_stamp(long tm)
	{
		reset(tm);
	}
	
	final public void reset()
	{
		reset(tm_tool.lctm_ms());
	}
	
	final public void reset(long now)
	{
		timestamp=now;
	}
	
	final public long timestamp()
	{
		return timestamp;
	}

	public boolean is_timeout(long time)
	{
		return timestamp<=time;
	}
}
