/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms35.manage.rule.service;

import java.util.List;
import java.util.Map;

/**
 * 功能/模块:
 * @author 张立群
 * @version 1.0  May 22, 2013
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
public interface RuleService {

	/**
	* 方法描述:初始化规则列表页面
	* @param rule
	* @return
	 */
	public List<Map<String,Object>> listRule(Map<String, Object> rule);
	
	/**
	* 方法描述:保存规则
	* @param formList
	* @return
	*/
	public Map<String, Object> saveRule(Map<String, List<Map<String, ?>>> formList);
	

	
	/**********************下面的方法在这一版中不使用*********************/

	/**

	/**
	* 方法描述:查询规则信息
	* @param reqs
	*/
	public Map<String, Object> getRule(Map<String, Object> reqs);

	/**
	* 方法描述:查询规则ID
	* @param reqs
	* @return
	*/
	public String getRuleId();


	/**
	 * 同步规则
	 * @param string
	 * @param string2
	 * @param b
	 */
		
	public void auditRule(String ruleId, String operate, boolean b);
	
	/**
	 * 同步路由
	 * @param lineId
	 * @param operate
	 * @param result
	 */
	public void auditLine(String lineId, String operate, boolean result);

	/**
	* 方法描述:
	* @param reqs
	*/
	public void check(Map<String, Object> reqs);
	
	public void checkRef(String txn_id, String rule_name, String ruleShortdesc, String oper);

	/**
	* 方法描述:
	* @param rule_ids
	* @param oper
	*/
	
	public void batchCheckRef(String[] rule_ids, String oper);

}
