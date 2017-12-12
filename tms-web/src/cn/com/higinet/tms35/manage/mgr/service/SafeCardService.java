package cn.com.higinet.tms35.manage.mgr.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.com.higinet.rapid.base.dao.Page;

public interface SafeCardService {

	Page<Map<String, Object>> getSafeCardList(Map<String, String> reqs);
	
	void addSafeCardAction(Map<String, String> reqs,HttpServletRequest request);
	
	Map<String, Object> getTableList(String tableName,String cond,String value);
	
	void updateSafeCardByUuid(Map<String, String> reqs,HttpServletRequest request);
}
