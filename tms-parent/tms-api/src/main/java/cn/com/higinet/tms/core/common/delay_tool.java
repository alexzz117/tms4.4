package cn.com.higinet.tms.core.common;

public class delay_tool
{
	static final public void delay_us(long us)
	{
		delay_us(us, null);
	}

	static public void delay(long ms)
	{
		delay_us(ms * 1000, null);
	}

	static final void sleep_us(long us) throws InterruptedException
	{
		if(us<1000)
		{
			long end_time = tm_tool.lctm_us() + us;
			while (tm_tool.lctm_us() < end_time)
				Thread.yield();
		}
		else
			Thread.sleep(us/1000);
	}
	static final int[] sleep_win=new int[]{100000,10000,5000,2000,1000,500,200,100,10,1,0};
	static final public <E> boolean delay_us(long us, func_if<E> when)
	{
		long end_time = tm_tool.lctm_us() + us;
		
		try
		{
			for(int i=0;i<sleep_win.length-1;i++)
			{
				while (tm_tool.lctm_us() < end_time-sleep_win[i])
				{
					if (when != null && when._if())
						return false;
					sleep_us(sleep_win[i+1]);
				}
			}
		}
		catch (InterruptedException e)
		{
			return false;
		}
		return true;
	}

	static final public <E> boolean delay_when(long ms, func_if<E> when)
	{
		return delay_us(ms * 1000, when);
	}

	public static void main(String[] v)
	{
//		clock c = new clock();
//			
//		delay_us(10);
//		c.pin("when delay 10us");
//		delay_us(100);
//		c.pin("when delay 100us");
//		delay_us(1000);
//		c.pin("when delay 1000us");
//		delay(1);
//		c.pin("when delay 1ms");
//		delay(10);
//		c.pin("when delay 10ms");
//		delay(100);
//		c.pin("when delay 100ms");
//		delay(12345);
//		c.pin("when delay 100second");
	}
}
