package cn.com.higinet.tms35.manage.dataanalysic.service;


import java.util.List;
import java.util.Map;

import cn.com.higinet.rapid.base.dao.Page;



public interface UserStatValueService {
	//查询统计值
	Page<Map<String,Object>> statPage(Map<String, String> reqs);
	
	/**
	 * 查询交易树
	 * @return
	 */
	public List<Map<String, Object>> queryTransBranches();
	
	/**
	 * 查询交易统计
	 * @param model
	 * @return
	 */
	public List<Map<String, Object>> queryTransStatFunc(String txnId);
}
