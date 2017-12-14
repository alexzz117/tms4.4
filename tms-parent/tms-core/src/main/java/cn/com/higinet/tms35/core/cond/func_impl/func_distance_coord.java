package cn.com.higinet.tms35.core.cond.func_impl;

import cn.com.higinet.tms35.core.cond.func_map;

/**
 * 通过经纬度(坐标)计算两地距离
 * @author lining
 */
public class func_distance_coord extends func_distance {

	@Override
	public Object exec(Object[] p, int n) {
		if(func_map.has_nothing(p, n, 4))
			return func_map.NOTHING;
		double lat1 = (Double) p[n];// 纬度1
		double lng1 = (Double) p[n + 1];// 经度1
		double lat2 = (Double) p[n + 2];// 纬度2
		double lng2 = (Double) p[n + 3];// 经度2
		
		double dist = GetDistance(lat1, lng1, lat2, lng2);
		if (log.isDebugEnabled()) {
			log.debug(String.format("latitude1=%f,longitude1=%f,latitude2=%f," +
					"longitude2=%f,dist=%f", lat1, lng1, lat2, lng2, dist));
		}
		return dist;
	}
}