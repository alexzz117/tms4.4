package cn.com.higinet.tms.manager.modules.modelmgr.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.tms.manager.dao.Page;

/**
 * 模型管理服务类
 * @author zlq
 */
public interface ModelMgrService {
	/**查询模型列表
	 * @param conds
	 * @return
	 */
	Page<Map<String, Object>> modelList(Map<String, String> conds);

	/**
	* 方法描述:
	* @param reqs
	* @return
	*/
	
	Page<Map<String, Object>> modelHisList(Map<String, String> reqs);

	/**
	* 方法描述:模型的相关度列表
	* @param reqs
	* @return
	*/
	List<Map<String,Object>> modelCorrelationList(Map<String, String> reqs);
	
}
