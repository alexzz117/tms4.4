/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.stat.service;

import java.util.List;
import java.util.Map;

/**
 * 功能/模块:统计
 * @author 张立群
 * @version 1.0  Apr 26, 2013
 * 类描述:统计配置服务类
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
public interface StatService {
	/**
	* 方法描述:查询统计配置信息
	* @param conds查询条件
	* @return 统计配置列表
	 */
	public List<Map<String,Object>> statList(Map<String,Object> conds);
	
	/**
	* 方法描述:通过交易ID查询交易属性
	* @param txnId
	* @return
	 */
	public List<Map<String,Object>> txnFeatureList(String txnId);
	
	/**
	* 方法描述:通过ID获取一条统计信息
	* @param statId
	* @return
	 */
	public Map<String,Object> getOneStat(String statId);
	
	/**
	* 方法描述:查询代码级
	* @param category_id
	* @param conds
	* @return
	 */
	public List<Map<String,Object>> codeList(String category_id,String[] conds);
	
	/**
	* 方法描述:更新统计信息的有效性
	* @param stat_id
	 */
	public void updateValidStat(String[] stat_id,String stat_status);
	
	/**
	* 方法描述:删除统计信息
	* @param stat_id
	 */
	public void delStat(String[] stat_id);

	/**
	* 方法描述:
	* @param statId
	* @return
	*/
	public Map<String,Object> getOneStatForCond(String statId);

	/**
	* 方法描述:更新统计条件
	* @param input
	*/
	public void updateStatCond(Map<String, Object> input);

	/**
	* 方法描述:通过交易ID查询交易树
	* @param tab
	* @return
	*/
	public List<Map<String, Object>> getTxnTree(Map<String,Object> tab);

	/**
	* 方法描述:查询交易的属性信息
	* @param reqs
	*/
	public Map<String,Object> queryTxnFeature(Map<String, Object> reqs);

	/**
	* 方法描述:保存
	* @param formList
	*/
	public Map<String,Object> saveStat(Map<String, List<Map<String, ?>>> formList);
}
