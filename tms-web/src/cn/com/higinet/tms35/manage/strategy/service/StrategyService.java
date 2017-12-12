/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms35.manage.strategy.service;

import java.util.List;
import java.util.Map;

/**
 * 功能/模块:策略服务类
 * @author zlq
 * @version 1.0  Mar 13, 2015
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
public interface StrategyService {

	/**
	* 方法描述:初始化策略列表页面
	* @param rule
	* @return
	 */
	public List<Map<String,Object>> listStrategy(Map<String, Object> strategy);
	/**
	 * 方法描述:初始化规则评估策略列表页面
	 * @param rule
	 * @return
	 */
	public List<Map<String,Object>> listRuleEvalStrategy(Map<String, Object> evalStrategy);
	/**
	* 方法描述:保存策略
	* @param formList
	* @return
	*/
	public Map<String, Object> saveStrategy(Map<String, List<Map<String, ?>>> formList);
	/**
	* 方法描述:策略下的规则
	* @param reqs
	* @return
	*/
	public List<Map<String,Object>> listStrategyRule(Map<String, Object> reqs);
	/**
	* 方法描述:保存策略规则
	* @param formList
	*/
	public Map<String, Object> saveStrategyRule(Map<String, List<Map<String, String>>> formList);
	
	/**
	* 方法描述:通过规则ID查询被引用的策略
	* @param input
	* @return
	 */
	public List<Map<String,Object>> listStrategyByRuleid(Map<String, Object> input);
	
}
