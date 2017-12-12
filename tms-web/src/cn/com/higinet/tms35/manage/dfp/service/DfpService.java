/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms35.manage.dfp.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * 功能/模块:设备指纹
 * @author zlq
 */
public interface DfpService {

	/**
	* 方法描述:查询应用列表
	* @param reqs
	* @return
	*/
	List<Map<String, Object>> appList(Map<String, Object> reqs);

	/**
	* 方法描述:应用
	* @param formList
	*/
	Map<String, Object> saveApp(Map<String, List<Map<String, ?>>> formList);

	/**
	* 方法描述:查询属性列表
	* @param reqs
	* @return
	*/
	List<Map<String, Object>> proList(Map<String, Object> reqs);

	/**
	* 方法描述:保存属性
	* @param formList
	* @return
	*/
	Map<String, Object> savePro(Map<String, List<Map<String, ?>>> formList);

	/**
	* 方法描述:查询应用属性关联列表
	* @param reqs
	* @return
	*/
	List<Map<String, Object>> appProList(Map<String, Object> reqs);

	/**
	* 方法描述:报错应用属性关联
	* @param formList
	* @return
	*/
	Map<String, Object> saveAppPro(Map<String, List<Map<String, ?>>> formList);

//	/**
//	* 方法描述:
//	* @return
//	*/
//	
//	List<Map<String,Object>> channleAllList();

	/**
	* 方法描述:
	 * @param response 
	* @return
	 * @throws IOException 
	*/
	
	void createJsFile(HttpServletResponse response,String app_id) throws IOException;
	
	/**
	* 方法描述:校验应用是否有属性
	* @param app_id
	* @return
	 */
	public boolean checkAppReferenced(String app_id);
	
	/**
	 * 解除用户和设备的绑定关系
	 * @param list
	 * @return
	 */
	public void unbindUserDeviceRel(List<Map<String, ?>> list);
}