package cn.com.higinet.tms35.comm;

//主要是防止数值溢出
public class comp_tool
{
	public final static int comp(int left, int right)
	{
		return left < right ? -1 : left == right ? 0 : 1;
	}

	public static int comp(double left, double right)
	{
		return left < right ? -1 : left == right ? 0 : 1;
	}

	public static int comp(long left, long right)
	{
		return left < right ? -1 : left == right ? 0 : 1;
	}
}
