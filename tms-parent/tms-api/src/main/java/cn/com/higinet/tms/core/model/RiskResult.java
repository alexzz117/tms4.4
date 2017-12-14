/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.model;

import java.util.List;

import cn.com.higinet.tms.core.util.StringUtil;
/**
 * 标识交易风险信息的持久化类
 * @author zhangfg
 * @version 2.2.0
 * @date 2011-10-21
 * @description 标识交易风险信息的持久化类
 */
public class RiskResult extends Result {
	//交易识别标识
	private String txnId;
	//交易名称
	private String txnName;
	//交易流水号
	private String transCode;
	//风险标识
	private String riskId;

	//风险分值
	private float score;
	//风险等级
	//private String riskLevel;
	//风险等级名称
	//private String riskLevelName;
	//处置方式
	private String disposal;
	//规则命中数
	private int hitRuleNum;
	//规则触发数
	private int trigRuleNum;
	//处置信息
	private String processInfo;
	//开关信息
	private String switchInfo;
	//动作信息
	private String actionInfo;
	//规则命中列表
	private String hitRules;
	//设备标识
	private String deviceToken;
	//cookie名称
	private String cookieName;
	//交易数据的SM3散列
	private String txnSm3;

	//返回错误信息
	private String errorInfo;
	//for tms test
	private List<String> testLogs;
	
	public RiskResult(){
	}
	
	public RiskResult(String backCode,String backInfo){
		super(backCode,backInfo);
	}

	public void setRisk(String txnId, String txnName, String transCode, String riskId, int score, String disposal){
		this.txnId = txnId;
		this.txnName = txnName;
		this.transCode = transCode;
		this.riskId = riskId;
		this.score=score;
		this.disposal=disposal;
	}

	public String getTransCode() {
		return transCode;
	}

	public void setTransCode(String transCode) {
		this.transCode = transCode;
	}

	public float getScore() {
		return score;
	}

	public String getRiskId() {
		return riskId;
	}

	public void setRiskId(String riskId) {
		this.riskId = riskId;
	}

	public String getDisposal() {
		return disposal;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public String getTxnName() {
		return txnName;
	}

	public void setTxnName(String txnName) {
		this.txnName = txnName;
	}
	
	public boolean hasRisk(){
		return !StringUtil.isBlank(riskId);
	}

	public List<String> getTestLogs() {
		return testLogs;
	}

	public void setTestLogs(List<String> testLogs) {
		this.testLogs = testLogs;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public void setDisposal(String disposal) {
		this.disposal = disposal;
	}

	public int getHitRuleNum() {
		return hitRuleNum;
	}

	public void setHitRuleNum(int hitRuleNum) {
		this.hitRuleNum = hitRuleNum;
	}

	public int getTrigRuleNum() {
		return trigRuleNum;
	}

	public void setTrigRuleNum(int trigRuleNum) {
		this.trigRuleNum = trigRuleNum;
	}

	public String getProcessInfo() {
		return processInfo;
	}

	public void setProcessInfo(String processInfo) {
		this.processInfo = processInfo;
	}

	public String getSwitchInfo() {
		return switchInfo;
	}

	public void setSwitchInfo(String switchInfo) {
		this.switchInfo = switchInfo;
	}

	public String getActionInfo() {
		return actionInfo;
	}

	public void setActionInfo(String actionInfo) {
		this.actionInfo = actionInfo;
	}

	public String getHitRules() {
		return hitRules;
	}

	public void setHitRules(String hitRules) {
		this.hitRules = hitRules;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	public String getCookieName() {
		return cookieName;
	}

	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}

	public String getTxnSm3() {
		return txnSm3;
	}

	public void setTxnSm3(String txnSm3) {
		this.txnSm3 = txnSm3;
	}
	
}
