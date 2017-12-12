package cn.com.higinet.tms35.manage.mgr.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.com.higinet.rapid.base.dao.Page;

/**
 * 评级管理服务类
 * @author zlq
 */
public interface RateService {
	
   /**查询评级设定列表
	* @param conds
	* @return
	*/
	List<Map<String, Object>> rateList(Map<String, String> conds);

	/**
	* 方法描述:修改评价设定表
	* @param reqs
	*/
	void rateMod(Map<String, String> reqs);

	/**
	* 方法描述:查询评级列表
	* @param reqs
	* @return
	*/
	Page<Map<String,Object>> levelPage(Map<String, String> reqs);
    
	/**
	* 方法描述:查询评级历史列表
	* @param reqs
	* @return
	*/
	Page<Map<String,Object>> rateHistoryList(Map<String, String> reqs);
	/**
	* 方法描述:单个评级
	* @param reqs
	*/
	void singleRate(Map<String, Object> reqs,String signal);
   
	/**
	* 方法描述:全部评级
	* @param reqs
	*/
	void allRate(Map<String, Object> reqs,Map<Integer,Map<String,Object>> ser_map);
	
	public double completePercent(Map<String, Object> reqs) ;
	
	 /**
	 修改风险等级
	  */
	public void updateRiskLevel(Map<String, String> reqs);
	
	/**
	 * 查询评级规则
	 * @param txnCode
	 * @param txnType
	 * @return
	 */
	public List<Map<String, Object>> getTransHitRuleList(String txnCode, String txnType);
	
	/**
	  根据查询条件进行批量评级
	 *@param reqs
	 * 
	 */
	 void  queryRate(Map<String, Object> reqs,Map<Integer,Map<String,Object>> ser_map);
}
 
