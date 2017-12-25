package cn.com.higinet.tms.engine.stat;

/*
 * 统计窗口初始化抽象
 * 
 * */
public abstract class stat_win_base
{
	public stat_win_fac<stat_win_base> fac;
	public int win_time;
	
	final public String toString(int baseTime)
	{
		StringBuffer sb = new StringBuffer(64);
		appendTo(sb,baseTime);
		return sb.toString();
	}

	final public String toString()
	{
		StringBuffer sb = new StringBuffer(64);
		appendTo(sb,0);
		return sb.toString();
	}
	
	public abstract void appendTo(StringBuffer sb, int base_time);
	public abstract Object from(String[] items, stat_win_fac<stat_win_base> buff, int base_time);
	public abstract Object[]Array();
}
