package cn.com.higinet.tms.engine.core.cond;

import java.util.regex.Pattern;

public class ip_tool
{
	static Pattern pattern_ip = Pattern
	.compile("(0|(?:[1-9][0-9]{0,2}))\\.(0|(?:[1-9][0-9]{0,2}))\\.(0|(?:[1-9][0-9]{0,2}))\\.(0|(?:[1-9][0-9]{0,2}))");

	public static boolean is_valid(String ip)
	{
		java.util.regex.Matcher m= pattern_ip.matcher(ip);
		if(!m.matches())
			return false;
		for(int i=1;i<=4;i++)
		{
			if(Long.parseLong(m.group(i))>255)
				return false;
		}
		
		return true;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		debug(".9.1.1");
		debug("00.9.1.1");
		debug("10.9.1.1");
		debug("90.9.1.1");
		debug("999.9.1.1");
		debug("10.9.11");
		debug("1220.9.1.01");
		debug("255.9.1.01");
		debug("120.9.1.256");
	}


	private static void debug(String string)
	{
		System.out.println(string + " is " + (is_valid(string)?"valid":"invalid"));
	}

}
