package cn.com.higinet.tms35.manage.dataanalysic.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.tms35.manage.dataanalysic.model.ReportModel;

public interface UserBehaviorService {
	/**
	 * 查询交易树
	 * @return
	 */
	public List<Map<String, Object>> queryTransBranches();
	
	/**
	 * 查询交易统计
	 * @param model
	 * @return
	 */
	public List<Map<String, Object>> queryTransStatFunc(String txnId);
	
	/**
	 * 查询用户行为习惯维度
	 * @param userIdArray
	 * @param weiDus
	 * @param txnId
	 * @return
	 */
	public Map<String, List<Map<String, Object>>> queryWeiDus(String[] userIdArray, String[] weiDus, String txnId);
	
	/**
	 * 查询用户行为习惯维度
	 * @param userIdArray
	 * @param weiDus
	 * @param txnId
	 * @return
	 */
	public List<Map<String,Object>> queryTranpropertiesWeiDus(Map<String, String> reqs);
	
	
	/**
	 * 查询资金转账地域信息
	 * @return
	 */
	public List<Map<String, Object>> queryRechargeRegion(Map<String, String> reqs,ReportModel reportModel);
	
	
	/**
	 * 查询资金转账地域信息
	 * @return
	 */
	public List<Map<String, Object>> queryPhoneRechargeRegion(Map<String, String> reqs,ReportModel reportModel);
}
