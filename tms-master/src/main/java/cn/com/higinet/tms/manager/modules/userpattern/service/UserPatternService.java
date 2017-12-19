/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.userpattern.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.tms.base.entity.common.Page;

/**
 * 功能/模块:
 * @author zhanglq
 * @version 1.0  Aug 29, 2013
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
public interface UserPatternService {

	/**
	* 方法描述:
	* @param reqs
	* @return
	*/
	
	Page<Map<String,Object>> pageStatPattern(Map<String, Object> reqs);

	/**
	* 方法描述:
	* @param formList
	* @return
	*/
	
	Map<String,Object> saveUserPattern(Map<String, List<Map<String, ?>>> formList,String userid,String statid);

	/**
	* 方法描述:
	* @param reqs
	* @return
	*/
	
	Page<Map<String,Object>> pageUser(Map<String, Object> reqs);

	
	
	List<Map<String,Object>> queryUserPatternList(Map<String, Object> reqs,Map<String,Object> statInfo);

	/**
	* 方法描述:
	* @param reqs
	* @return
	*/
	
	List<Map<String,Object>> queryStatPatternList(Map<String, Object> reqs);
	
	/**
	* 方法描述:用户自定义行为习惯同步数据接口
	* @param userId
	* @param patternId
	* @param operate
	* @param isPass
	 */
	public void synUserPatternData(String userId, String statId, String patternId, String operate, boolean isPass);
	/**
	* 方法描述:获取一个自定义行为习惯
	* @param user_id 用户ID
	* @param stat_id 统计ID
	* @param pattern_id 行为习惯ID
	* @param up_s 行为习惯串USER_PATTERN+USER_PATTERN_1+USER_PATTERN_C
	* @return
	 */
	public Map<String,Object> getOneUserPattern(String user_id,String stat_id,String pattern_id,String up_s);

	/**
	* 方法描述:
	* @param reqs
	* @return
	*/
	
	List<Map<String,Object>> getCountry(Map<String, Object> reqs);

	/**
	* 方法描述:
	* @param reqs
	* @return
	*/
	
	List<Map<String,Object>> getRegion(Map<String, Object> reqs);

	/**
	* 方法描述:
	* @param reqs
	* @return
	*/
	
	List<Map<String,Object>> getCity(Map<String, Object> reqs);
}
