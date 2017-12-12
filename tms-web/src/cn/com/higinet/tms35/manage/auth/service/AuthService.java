package cn.com.higinet.tms35.manage.auth.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.rapid.base.dao.Page;
import cn.com.higinet.tms35.manage.aop.cache.common.MethodConfig;

/**
 * 
 * @author zhangfg
 * @version 1.0.0 2012-09-03
 * @disctripte	授权中心服务接口类
 *
 */
public interface AuthService {
	
	/**
	 * 查询待授权信息所属的模块列表
	 * @param conds
	 * @return
	 */
	public List<Map<String,Object>> showCenter(Map<String,String> conds);
	/**
	 * 分页查询待授权列表信息
	 * @param conds
	 * @return
	 */
	public Page<Map<String,Object>> dataList(Map<String,String> conds);
	/**
	 * 根据授权信息ID获取授权信息
	 * @param conds
	 * @return
	 */
	public Map<String,Object> getAuthInfoById(String authId);

	/**
	 * 分页查询授权日志列表
	 * @param conds
	 * @return
	 */
	public Page<Map<String,Object>> authLogList(Map<String,String> conds);
	
	/**
	 * 
	 * @param mc
	 * @param tablePkValue
	 * @return
	 */
	public String getOperateData(MethodConfig mc,String tablePkValue);
	
	/**
	 * 根据条件查询授权信息
	 * @param conds
	 * @return
	 */
	public Page<Map<String,Object>> historyDataList(Map<String,String> conds);
	/**
	 * 根据表名，主键及主键值获取正式表数据
	 * @param tableName
	 * @param pk
	 * @param pkValue
	 * @return
	 */
	public List<Map<String,Object>> queryOldData(String tableName,String pk,String pkValue);
	
	/**
	 * 获取数据对比数据
	 * @param reqs
	 * @return
	 */
	public List<Map<String,Object>> getDataCompare(Map<String, String> reqs);
	
	/**
	 * 根据AuthId获取多条授权信息
	 * @param authIds
	 * @return
	 */
//	public List<Map<String, Object>> getAuthByAuthIds(String[] authIds);
	
	/**
	 * 批量更新授权信息
	 * @param reqs
	 */
	public void batchUpadteAuth(Map<String, String> reqs);
	
	/**
	 * 更新授权信息
	 * @param authInfo
	 * @return 
	 */
	public void aopUpdateAuthInfo(Map<String, Object> authInfo);
	
	/**
	 * 创建授权信息
	 * @param authInfo
	 * @return 
	 */
	public void aopCreateAuthInfo(Map<String, Object> authInfo);
	
	/**
	 * 获取授权查看模块列表
	 * @return
	 */
	public List<Map<String, Object>> listAuthQueryModel();
	
	/**
	 * 生成授权记录信息
	 * @param authRecord
	 */
	public void aopCreateAuthRecord(Map<String, Object> authRecord);
	
	/**
	 * 删除授权信息和相关的授权记录信息
	 * @param authId
	 */
	public void aopDeleteAuthInfo(String authId,String authStatus);
	
	/**
	 * 获取子操作列表
	 * @param reqs
	 * @return
	 */
	public Page<Map<String, Object>> subDataList(Map<String, String> reqs);
	
	/**
	 * 根据QueryPkValue获取授权信息
	 * @param queryPkvalue
	 * @return
	 */
	public Map<String, Object> getAuthInfoByQueryPkValue(String queryPkvalue, MethodConfig mc);
	
	/**
	 * 获取授权记录信息
	 * @param mc
	 * @param tablePkValue 
	 * @return
	 */
	public Map<String, Object> getAuthRecordByMc(MethodConfig mc, String tablePkValue,boolean queryMain);
	
	/**
	 * 更新授权记录信息
	 * @param authRecord
	 */
	public void aopUpdateAuthRecord(Map<String, Object> authRecord);
	
	/**
	 * 获取授权依赖信息
	 * @param authId
	 * @return
	 */
	public String getDependencyInfo(String authId);
	
	/**
	 * 获取下一个日志序号
	 * @param authId
	 * @return
	 */
	public Integer getNextLogOrder(String authId);
	
	public List<Map<String, Object>> getNoneMainAuthRecordByAuthId(String authId);
	
	public void aopdeleteAuthRecord(String recordId);
	
	public String[] getLogByLogId(String logId);
	
	public void aopCreateAuthLog(Map<String, Object> logMap);
	
	public String[] getTmpUserPatternName(String userId, String statId,
			String patternValue);
	
	public String getUserPatternName(MethodConfig mc, Map dataMap);
	
	public String checkTranCreate(Map data, MethodConfig mc);
	
	public String getRuleChildName(Map dataMap);
	
	// 授权中心中的授权操作
	public void doAuth(Object[] args);
	
	// 申请授权信息
	public void doApply(Object[] args, String methodName);
}
