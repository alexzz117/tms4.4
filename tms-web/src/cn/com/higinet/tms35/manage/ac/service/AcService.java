/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms35.manage.ac.service;

import java.util.List;
import java.util.Map;

/**
 * 功能/模块:
 * @author 张立群
 * @version 1.0  May 31, 2013
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
public interface AcService {

	/**
	* 方法描述:查询动作列表
	* @param reqs
	* @return
	*/
	List<Map<String, Object>> listAction(Map<String, Object> reqs);

	/**
	* 方法描述:通过动作主键获取动作信息
	* @param actionId
	* @return
	*/
	Map<String,Object> getOneAction(String actionId);

	/**
	* 方法描述:更新动作有效性
	* @param stat_id
	* @param stat_status
	*/
	void updateValidAc(String[] stat_id, String stat_status);

	/**
	* 方法描述:更新条件
	* @param input
	*/
	void updateAcCond(Map<String, Object> input);

	/**
	* 方法描述:
	* @param formList
	*/
	
	Map<String, Object> saveAc(Map<String, List<Map<String, Object>>> formList);
	
	public Map<String, Object> createAction(Map<String, Object> reqs);

}
