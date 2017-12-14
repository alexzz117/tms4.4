package cn.com.higinet.tms.manager.modules.monitor.service;

import java.util.List;
import java.util.Map;

/**
 * 告警事件监控业务层接口
 * @author yangk
 *
 */
public interface AlarmService {
	List<Map<String, Object>> listAllCountry();
	List<Map<String, Object>> listRegionByCountry(String country);
	List<Map<String, Object>> listCityByRegion(String region);
}
