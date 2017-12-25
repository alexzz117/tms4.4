package cn.com.higinet.tms.engine.core.cond.func_impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.cache.ip_cache;
import cn.com.higinet.tms.engine.core.cond.func;
import cn.com.higinet.tms.engine.core.cond.func_map;

public class func_distance implements func
{
	static final Logger log=LoggerFactory.getLogger(func_distance.class);
	static final double EARTH_RADIUS = 6378.137;

	final static double rad(double dig)
	{
		return Math.PI * dig / 180;
	}

	final static double GetDistance(double lat1, double lng1, double lat2, double lng2)
	{
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}

	public Object exec(Object[] p, int n)
	{
		if(func_map.has_nothing(p, n, 2))
			return func_map.NOTHING;
			
		String ip1 = (String) p[n];
		String ip2 = (String) p[n + 1];
		
		ip_cache.loc.entry le1=ip_cache.Instance().find_loc_entry(ip_cache.LOCATE_TYPE_IP, ip1);
		ip_cache.loc.entry le2=ip_cache.Instance().find_loc_entry(ip_cache.LOCATE_TYPE_IP, ip2);
		
		if(le1 == null || le2 == null)
			return null;
		
		double dist = GetDistance(le1.latitude, le1.longitude, le2.latitude, le2.longitude);
		if (log.isDebugEnabled())
			log.debug(String.format("ip1=%s,ip2=%s,dist=%f", p[n], p[n+1], dist));
		return dist;
	}
	
//	public static void main(String []a)
//	{
//		System.out.print(new func_distance().GetDistance(0, 90, 0, 0));
//	}
}
