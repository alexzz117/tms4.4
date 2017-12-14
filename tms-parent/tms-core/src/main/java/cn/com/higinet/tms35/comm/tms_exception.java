package cn.com.higinet.tms35.comm;

public class tms_exception extends RuntimeException
{
	private static final long serialVersionUID = 5710139688715102359L;

	public tms_exception(Object... what)
	{
		super(make_what(what));
	}

	static String make_what(Object... what)
	{
		StringBuffer sb = new StringBuffer(256);
		for (int i = 0, len = what.length; i < len; i++)
			sb.append(what[i]);

		return sb.toString();
	}
}
