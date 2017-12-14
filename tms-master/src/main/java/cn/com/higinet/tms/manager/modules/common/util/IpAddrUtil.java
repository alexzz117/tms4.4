package cn.com.higinet.tms.manager.modules.common.util;

/**
 * IP地址操作工具类
 * 
 * @author lining
 * 
 */
public class IpAddrUtil {
	/**
	 * IP地址转Long值
	 * @param string
	 * @return
	 */
	public static long ip2long(String string) {
		String[] ip = string.split("\\.");
		long ret = Long.parseLong(ip[0]);
		ret = (ret << 8) | Long.parseLong(ip[1]);
		ret = (ret << 8) | Long.parseLong(ip[2]);
		ret = (ret << 8) | Long.parseLong(ip[3]);
		return ret;
	}

	/**
	 * Long值转IP地址
	 * @param ip
	 * @return
	 */
	public static String long2ip(long ip) {
		String[] s = new String[4];
		s[0] = Integer.toString((int) ((ip >> 24) & 0xff));
		s[1] = Integer.toString((int) ((ip >> 16) & 0xff));
		s[2] = Integer.toString((int) ((ip >> 8) & 0xff));
		s[3] = Integer.toString((int) (ip & 0xff));
		return StringUtil.ArrayToStr(s, ".");
	}
}