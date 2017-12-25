package cn.com.higinet.tms.engine.core.cond.func_impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.cond.func;
import cn.com.higinet.tms.engine.core.cond.func_map;

public class func_speed implements func
{
	static final Logger log = LoggerFactory.getLogger(func_speed.class);

	/**
	 * n:lat1
	 * n+1:lng1
	 * n+2:lat2
	 * n+3:lng2
	 * n+4:ip1
	 * n+5:ip2
	 * n+6:time1
	 * n+7:time2
	 */
	public Object exec(Object[] p, int n)
	{
		// time1 or time2 is null
		if (func_map.has_nothing(p, n + 6, 2))
			return func_map.NOTHING;
		
		Object dist = null;
		int dist_flag = -1;// 距离计算方法标识：0-IP, 1-经纬度
		if (func_map.has_nothing(p, n, 4))
		{
			// (lat1 or lng1 or lat2 or lng2) is null
			if (func_map.has_nothing(p, n + 4, 2))
			{
				// ip1 or ip2 is null
				return func_map.NOTHING;
			}
			else
			{
				// 通过IP计算飞行速度
				dist = new func_distance().exec(p, n + 4);
				dist_flag = 0;
			}
		}
		else
		{
			// 通过经纬度计算飞行速度
			dist = new func_distance_coord().exec(p, n);
			dist_flag = 1;
		}
		
		if (func_map.is_nothing(dist))
			return func_map.NOTHING;
		
		long time_span = Math.abs((Long) p[n + 6] - (Long) p[n + 7]);
		if (time_span == 0) {
			log.debug("飞行速度被0除.");
			return func_map.NOTHING;
			//throw new tms_exception("被0除.");
		}
		                    

		double span_in_hour = 1.D * time_span / 1000 / 3600;
		double speed = (Double) dist / span_in_hour;

		if (log.isDebugEnabled())
		{
			if (dist_flag == 0)
			{
				log.debug(String.format(
						"IP1:%s,IP2:%s,TM1:%d,TM2:%d,TMSPAN=%5.4fh,abs=%5.4fkm,speed=%5.4fkm/h",
							p[n + 4], p[n + 5], p[n + 6], p[n + 7], span_in_hour, dist, speed));
			}
			else if (dist_flag == 1)
			{
				log.debug(String.format(
						"LATITUDE1=%f,LONGITUDE1=%f,LATITUDE2=%f,LONGITUDE2=%f,TM1:%d,TM2:%d," +
								"TMSPAN=%5.4fh,abs=%5.4fkm,speed=%5.4fkm/h", p[n], p[n + 1], p[n + 2],
									p[n + 3], p[n + 6], p[n + 7], span_in_hour, dist, speed));
			}
		}
		return new Double(speed);
	}
}
