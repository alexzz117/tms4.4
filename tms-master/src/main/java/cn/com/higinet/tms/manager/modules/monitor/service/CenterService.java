package cn.com.higinet.tms.manager.modules.monitor.service;

import java.util.Map;

/**
 * 监控中心业务层接口
 * @author mayj
 *
 */
public interface CenterService {
	/**
	 * 获取当前时间范围
	 */
	String getTimeScope(Map<String, String> conds);
}
