package cn.com.higinet.tms.manager.modules.query.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.com.higinet.tms.manager.modules.query.common.model.JsonDataProcess;

public interface QueryService {
	/**
	 * 查询自定义查询的数据信息
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> listQuery(Map<String, String> conds);

	/**
	 * 自定义查询分组数据
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> listQueryGroup(Map<String, String> conds);

	/**
	 * 根据自定义查询id，获取该查询数据信息
	 * @param queryId	自定义查询ID
	 * @return
	 */
	public Map<String, Object> getQueryById(long queryId);

	/**
	 * 创建自定义查询信息
	 * @param query
	 * @return
	 */
	public Map<String, Object> createQuery(Map<String, Object> query);

	/**
	 * 修改自定义查询信息
	 * @param query
	 * @return
	 */
	public void updateQuery(Map<String, Object> query);

	/**
	 * 删除自定义查询信息
	 * @param queryId	查询主键id
	 * @return
	 */
	public void deleteQuery(long queryId, HttpServletRequest request);

	/**
	 * 创建查询分组
	 * @param group
	 * @return
	 */
	public Map<String, Object> createQueryGroup(Map<String, Object> group);

	/**
	 * 修改查询分组
	 * @param group
	 */
	public void updateQueryGroup(Map<String, Object> group);

	/**
	 * 删除查询分组及其下查询
	 * @param groupId
	 * @return
	 */
	public void deleteQueryGroup(long groupId, HttpServletRequest request);

	/**
	 * 根据表名查询表信息
	 * @param tabName	表名
	 * @return
	 */
	public Map<String, Object> getQueryTableByTabName(String tabName);

	/**
	 * 根据表名查询所有字段信息
	 * @param tabName	表名
	 * @param tabType	表类型
	 * @return
	 */
	public List<Map<String, Object>> getFieldsByTabName(String tabName, String tabType);

	/**
	 * 判断此查询是否被引用
	 * @param queryId	自定义查询ID
	 * @param error		错误提示
	 */
	public boolean isUsedByQuery(long queryId, StringBuffer error, HttpServletRequest request);
	
	/**
	 * 获取自定义查询预处理结果
	 * @param queryMap
	 * @param request
	 * @return
	 */
	public JsonDataProcess getCustomQueryDataProcess(Map<String, String[]> paramterMap);
	
	/**
	 * 获取自定义查询结果
	 * @param queryMap	TMS_COM_QUERY表中数据
	 * @param request	httpServletRequest
	 * @return
	 */
	public Object getCustomQueryResultDataList(HttpServletRequest request);
	
	/**
	 * 导出自定义查询结果数据
	 * @param exportType	导出文件类型
	 * @param reqs			HttpServletRequest
	 * @param response		HttpServletResponse
	 */
	public void exportList(String exportType, HttpServletRequest request, HttpServletResponse response);
}