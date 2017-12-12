package cn.com.higinet.tms35.manage.mgr.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.rapid.base.dao.Page;

public interface AssignAutoService {
	/**
	* 方法描述:
	* @param reqs
	* @return
	*/
	
	Page<Map<String,Object>> dealerInformationPage(Map<String, String> reqs);
	
	Page<Map<String,Object>> eventTypePage(Map<String, String> reqs);
	
	void updateUserAssign(Map<String, String> reqs);
	
	void addUserAssign(Map<String, String> reqs);
	
	void modStatusUserAssign(Map<String, String> reqs);
	
	void modAutoAssignSwitch(Map<String, String> reqs);
	
	void delUserAssign(Map<String,List<Map<String,String>>> batchMap);
	
	Page<Map<String, Object>> getAllUserAssign(Map<String, String> reqs);
	
	String getAssignSwitchStatus(Map<String, String> reqs);
	
	Page<Map<String,Object>> getAllPaySuspendList(Map<String, String> reqs);
}
