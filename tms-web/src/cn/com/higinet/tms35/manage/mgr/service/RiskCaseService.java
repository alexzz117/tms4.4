package cn.com.higinet.tms35.manage.mgr.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.rapid.base.dao.Page;

public interface RiskCaseService {

	/**
	 * 查询风险案件列表
	 * 
	 * @param cond
	 *            查询条件参数值
	 * @return
	 */
	public Page<Map<String, Object>> getRiskCaseList(Map<String, String> reqs);

	/**
	 * 新增风险案件
	 * 
	 * @param cond
	 *            查询条件参数值
	 * @return
	 */
	public void addRiskCase(Map<String, String> riskCaseMap);

	/**
	 * 新增风险案件关联的交易
	 * 
	 * @param cond
	 *            查询条件参数值
	 * @return
	 */
	public void addRiskCaseTxn(Map<String, String> riskCaseTxnMap);

	/**
	 * 删除风险案件及关联的交易
	 * 
	 * @param uuid
	 *            风险案件UUID
	 * @return
	 */
	public void delRiskCase(String uuid);

	/**
	 * 批量删除风险案件及关联的交易
	 * 
	 * @param uuid
	 *            风险案件UUID
	 * @return
	 */
	public void delRiskCaseBatch(Map<String, List<Map<String, String>>> batchMap);

	/**
	 * 更新风险案件
	 * 
	 * @param uuid
	 *            风险案件UUID
	 * @return
	 */
	public void updateRiskCase(Map<String, String> riskCaseMap);

	/**
	 * 更新风险案件关联的交易
	 * 
	 * @param uuid
	 *            风险案件UUID
	 * @return
	 */
	public void updateRiskCaseTxn(Map<String, String> riskCaseMapTxn);

	/**
	 * 更新风险案件的状态
	 * 
	 * @param uuid
	 *            风险案件UUID
	 * @return
	 */
	public void resetRiskCaseStatus(String uuid);

	/**
	 * 增加风险案件的调查记录
	 * 
	 * @param riskCaseInvstMap
	 * @return
	 */
	public void addRiskCaseInvst(Map<String, String> riskCaseInvstMap);

	/**
	 * 获取风险案件的调查记录
	 * 
	 * @param caseUuid
	 * @return map
	 */
	public Map<String, Object> getRiskCaseInvst(String caseUuid);

	/**
	 * 查询表的记录
	 * 
	 * @param tableName
	 * @param 字段名
	 * @param 查询值
	 * @return
	 */
	public Map<String, Object> getTableMap(String tableName, String cond, String value);

	public Page<Map<String, Object>> getRiskCaseHisList(Map<String, String> reqs);

	// public Page<Map<String, Object>> getInfoRiskCased(Map<String, String> reqs);
}
