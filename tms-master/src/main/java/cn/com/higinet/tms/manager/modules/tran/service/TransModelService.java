package cn.com.higinet.tms.manager.modules.tran.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.com.higinet.tms.manager.dao.Page;

/**
 * 交易模型接口
 * @author yangk
 */
public interface TransModelService {

	/**
	 * @see cn.com.higinet.tms.manager.modules.tran.service.impl.TransModelServiceImpl#getTranModels()
	 * @return
	 */
	public List<Map<String, Object>> getTranModels(String txnid);
	/**
	 * @see cn.com.higinet.tms.manager.modules.tran.service.impl.TransModelServiceImpl#getTranModels()
	 * @return
	 */
	public List<Map<String, Object>> getTranModelsRef(String txnid);
	/**
	 * @see cn.com.higinet.tms.manager.modules.tran.service.impl.TransModelServiceImpl#getCanRefTable()
	 * @return
	 */
	public List<Map<String, Object>> getCanRefTable();
	/**
	 * @see cn.com.higinet.tms.manager.modules.tran.service.impl.TransModelServiceImpl#getFunc()
	 * @return
	 */
	public List<Map<String, Object>> getFunc();
	public List<Map<String, Object>> getFuncParam();
	public List<Map<String, Object>> getTranModelsRefTab(String txnid);
	
	public Map saveModel(Map formMap);
	public Map saveModelRefTab(Map formMap);
	public Map saveModelRefFd(Map formMap);
	public List<Map<String, Object>> getCanRefFd(String tab_name);
	public List<Map<String, Object>> getAllStoreFd();
	public List<Map<String, Object>> getAvailableStoreFd(String tab_name);
	public List<Map<String, Object>> getSelfAndSubFd(String tab_name);
	/**
	 * 获取属性代码和名单英文名重复的交易模型数据
	 * @param attrCode	属性代码
	 * @return
	 */
	public boolean isDuplicateByTransAttrAndRoster(String attrCode);
	
	/*******************************shuming******************************************/
	/**
	* 方法描述:交易查询界面，数据模型的查询
	* @param input
	* @return
	 */
	public List<Map<String, Object>> getTransByTabnameList(Map<String, String> conds);
	
	/**
	* 方法描述:根据查询条件，查询交易分页数据
	* @param conds 查询条件
	* @param listdb 交易模型数据
	* @param isPage 是否分页
	* @return
	 */
	public Page<Map<String, Object>> getTrandataPage(Map<String, String> conds, List<Map<String, Object>> listdb, boolean isPage);
	
	/******************************add by wuruiqi******************************************/
	
	/**
	 * 导出自定义查询结果数据
	 * @param exportType	导出文件类型
	 * @param reqs			HttpServletRequest
	 * @param response		HttpServletResponse
	 */
	public void exportList(String exportType, HttpServletRequest request, HttpServletResponse response);
}
