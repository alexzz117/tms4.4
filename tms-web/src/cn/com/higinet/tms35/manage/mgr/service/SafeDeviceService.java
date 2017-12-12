package cn.com.higinet.tms35.manage.mgr.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.com.higinet.rapid.base.dao.Page;

public interface SafeDeviceService {

	Page<Map<String, Object>> getSafeDeviceList(Map<String, String> reqs);
	
	void addSafeDeviceAction(Map<String, String> reqs,HttpServletRequest request);
	
	Map<String, Object> getTableList(String tableName,String cond,String value);
	
	void updateSafeDeviceByUuid(Map<String, String> reqs,HttpServletRequest request);
}
