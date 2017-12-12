package cn.com.higinet.tms35.manage.alarm.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.com.higinet.rapid.base.dao.Page;

/**
 * 警报事件Service接口
 * @author lining
 *
 */
public interface AlarmEventService {
	
	public static final String ALARM_PS_FUNC_ID = "tms.alarm.process.funcid";
	public static final String ALARM_AS_FUNC_ID = "tms.alarm.assign.funcid";
	public static final String ALARM_AD_FUNC_ID = "tms.alarm.audit.funcid";
	
	public static final String ALARM_PS_TIMEOUT = "alarmProcessTimeout";
	
	/**
	 * 通过查询交易流水, 获取其中报警处理相关信息
	 * @param cond	查询条件参数值
	 * @return
	 */
	public Map<String, Object> getTrafficDataForAlarmProcessInfo(Map<String, String> cond);
	
	/**
	 * 更新交易流水中报警处理状态
	 * @param modMap	需要更新的字段/值
	 */
	public void updateTransProcessInfo(Map<String, Object> modMap);
	
	/**
	 * 添加报警处理信息
	 * @param cond
	 * @param request
	 */
	public Map<String, Object> addAlarmProcessInfo(Map<String, String> cond, HttpServletRequest request);
	
	/**
	 * 获取报警处理动作列表信息
	 * @param cond	查询条件
	 * @return
	 */
	public List<Map<String, Object>> getAlarmProcessActions(Map<String, String> cond);
	
	/**
	 * 添加报警处理动作
	 * @param map
	 */
	public Map<String, Object> addAlarmProcessAction(Map<String, String> map);
	
	/**
	 * 删除报警处理动作
	 * @param cond
	 */
	public void delAlarmProcessAction(Map<String, String> cond);
	
	/**
	 * 获取报警处理记录信息列表
	 * @param cond
	 * @return
	 */
	public List<Map<String, Object>> getAlarmProcessInfoList(Map<String, String> cond);
	
	/**
	 * 报警事件处理人员工作量
	 * @param cond
	 * @return
	 */
	public List<Map<String, Object>> getAlarmAssignOperCapacity(Map<String, Object> cond);
	/**
	 * 报警事件处理
	 * @param cond
	 * @param request
	 */
	public void alarmProcess(Map<String, String> cond, HttpServletRequest request);
	
	/**
	 * 报警事件审核
	 * @param cond
	 * @param request
	 * @return 
	 */
	public int alarmAudit(Map<String, String> cond, HttpServletRequest request);
	
	/**
	 * 报警事件分派
	 * @param cond
	 * @param request
	 */
	public void alarmAssign(Map<String, String> cond, HttpServletRequest request);
	
	/**
	 * 获取交易策略信息
	 * @param stId	策略ID
	 * @return
	 */
	public Map<String, Object> getTransStrategy(String stId);
	
	/**
	 * 获取评估类型策略
	 * @param stId	策略ID
	 * @return
	 */
	public List<Map<String, Object>> getTransStrategyRuleEvalList(String stId);
	
	/**
	 * 获取交易命中规则信息
	 * @param txnCode	交易流水号
	 * @param txnType	交易类型
	 * @return
	 */
	public List<Map<String, Object>> getTransHitRuleList(String txnCode, String txnType);
	
	/**
	 * 获取系统参数值
	 * @param paramName		参数名
	 * @return
	 */
	public Map<String, Object> getSystemParam(String paramName);
	
	/**
	 * 获取拥有报警事件分派菜单权限的用户信息
	 * @return
	 */
	public List<Map<String, Object>> getAlarmAssingAuthorityOperators();
	
	/**
	 * 获取拥有报警事件处理菜单权限的用户信息
	 * @return
	 */
	public List<Map<String, Object>> getAlarmProcessAuthorityOperators();
	
	/**
	 * 获取拥有报警事件审核菜单权限的用户信息
	 * @return
	 */
	public List<Map<String, Object>> getAlarmAuditAuthorityOperators();

	/**
	* 方法描述:查询处理员处理的历史报警事件
	* @param reqs
	* @return
	*/
	public Page<Map<String, Object>> getAlarmHisActions(Map<String, String> reqs);

	/**
	* 方法描述:查询快捷函数
	* @param reqs
	*/
	public List<Map<String, Object>> shortActionList(Map<String, String> reqs);

	/**
	* 方法描述:
	* @param cond
	* @return
	*/
	
	List<Map<String, Object>> getTrafficDataByUserid(Map<String, Object> cond);
}