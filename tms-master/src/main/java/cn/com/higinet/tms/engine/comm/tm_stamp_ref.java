package cn.com.higinet.tms.engine.comm;



public class tm_stamp_ref extends tm_stamp
{
	private int ref;
	public tm_stamp_ref()
	{
		ref=1;
	}
	
	public tm_stamp_ref(long tm)
	{
		super(tm);
		ref=1;
	}
	
	final synchronized public int add_ref()
	{
		return ++ref;
	}

	final synchronized public int release()
	{
		return --ref;
	}
}
