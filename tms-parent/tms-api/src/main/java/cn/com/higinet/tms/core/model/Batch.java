/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.model;

import java.io.Serializable;
import java.util.Date;

import cn.com.higinet.tms.core.util.DateUtil;
import cn.com.higinet.tms.core.util.StringUtil;

/**
 * 
 * @author zhangfg
 * @version 3.0.0
 * @date 2012-06-12
 * @description 批量交易信息的持久化类
 */
public class Batch implements Serializable {
	public Batch() {
		operatCycle = new OperatCycle();
		operatCycle.setRiskTrades(this);
	}

	// 操作周期
	private OperatCycle operatCycle;
	// 批次号
	private String batchNo;
	// 批次别名
	private String batchName;
	// 总金额
	private float totalAmount;
	// 总笔数
	private int totalCount;
	// 批量付款账号
	private String payAccount;
	// 交易时间(年-月-日 时:分:秒)
	private String transTime;
	// 交易类型，表示具体执行的交易
	private String transType;
	// 交易状态： 2 正在处理 1 认证成功 0认证失败
	private String transStatus;
	// 客户端渠道类型 01 个人网银 02 企业网银
	private String channelType;
	// IP地址
	private String ipAddress;
	// 用户ID(客户号)
	private String cstNo;
	// 会话ID
	private String sessionId;
	// 是否超时
	private boolean isTimeOut = false;

	/**
	 * 分发用
	 */
	private String dispatch;
	
	/**
	 * 获取操作周期对象
	 * 
	 * @return
	 */
	public OperatCycle getOperatCycle() {
		return operatCycle;
	}

	/**
	 * 获取批次号
	 * 
	 * @return
	 */
	public String getBatchNo() {
		return batchNo;
	}

	/**
	 * 设置批次号
	 * 
	 * @param batchNo
	 */
	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	/**
	 * 获取批次别名
	 * 
	 * @return
	 */
	public String getBatchName() {
		return batchName;
	}

	/**
	 * 设置批次别名
	 * 
	 * @param batchName
	 */
	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}

	/**
	 * 获取总金额
	 * 
	 * @return
	 */
	public float getTotalAmount() {
		return totalAmount;
	}

	/**
	 * 设置总金额
	 * 
	 * @param totalAmount
	 */
	public void setTotalAmount(float totalAmount) {
		this.totalAmount = totalAmount;
	}

	/**
	 * 获取总笔数
	 * 
	 * @return
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * 设置总笔数
	 * 
	 * @param totalCount
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	/**
	 * 获取批量付款账号
	 * 
	 * @return
	 */
	public String getPayAccount() {
		return payAccount;
	}

	/**
	 * 设置批量付款账号
	 * 
	 * @param payAccount
	 */
	public void setPayAccount(String payAccount) {
		this.payAccount = payAccount;
	}

	/**
	 * 获取交易时间(年-月-日 时:分:秒)
	 * 
	 * @return
	 */
	public String getTransTime() {
		return transTime;
	}

	/**
	 * 设置交易时间(年-月-日 时:分:秒)
	 * 
	 * @param transTime
	 */
	public void setTransTime(String transTime) {
		this.transTime = transTime;
	}

	/**
	 * 获取交易类型，表示具体执行的交易
	 * 
	 * @return
	 */
	public String getTransType() {
		return transType;
	}

	/**
	 * 设置交易类型，表示具体执行的交易
	 * 
	 * @param transType
	 */
	public void setTransType(String transType) {
		this.transType = transType;
	}

	/**
	 * 获取交易状态：2 正在处理1 认证成功0认证失败
	 * 
	 * @return
	 */
	public String getTransStatus() {
		return transStatus;
	}

	/**
	 * 设置交易状态：2 正在处理1 认证成功0认证失败
	 * 
	 * @param authStatus
	 */
	public void setTransStatus(String transStatus) {
		this.transStatus = transStatus;
	}

	/**
	 * 获取客户端渠道类型01 个人网银02 企业网银
	 * 
	 * @return
	 */
	public String getChannelType() {
		return channelType;
	}

	/**
	 * 设置客户端渠道类型01 个人网银02 企业网银
	 * 
	 * @param channelType
	 */
	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	/**
	 * 获取用户ID(客户号)
	 * 
	 * @return
	 */
	public String getCstNo() {
		return cstNo;
	}

	/**
	 * 设置用户ID(客户号)
	 * 
	 * @param customerId
	 */
	public void setCstNo(String cstNo) {
		this.cstNo = cstNo;
	}

	/**
	 * 获取会话ID
	 * 
	 * @return
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * 设置会话ID
	 * 
	 * @param sessionId
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * 获取IP地址
	 * 
	 * @return
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * 设置IP地址
	 * 
	 * @param ipAddress
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * 获取是否超时
	 * 
	 * @return
	 */
	public boolean getIsTimeOut() {
		return isTimeOut;
	}

	/**
	 * 设置是否超时
	 * 
	 * @param isTimeOut
	 */
	public void setIsTimeOut(boolean isTimeOut) {
		this.isTimeOut = isTimeOut;
	}

	public void full() {
		String batchNo = this.getBatchNo();
		// 如果没有交易流水号，生成一个，并为transaction设置
		if (batchNo == null || "".equals(batchNo)) {
			this.setBatchNo(StringUtil.randomUUID());
		}
		// 设置交易时间
		String transTime = this.getTransTime();
		if (transTime == null || "".equals(transTime)) {
			Date date = new Date();
			this.setTransTime(DateUtil.dateConvert(date, "yyyy-MM-dd HH:mm:ss"));
		} else {
			// String datePatten = ServerConfig.getInstance().getProperty("channel.date.patten");
			String datePatten = TmsConfigVo.getDatePatten();
			this.setTransTime(DateUtil.dateConvert(transTime, datePatten, "yyyy-MM-dd HH:mm:ss"));
		}
	}

	public String getDispatch() {
		return dispatch;
	}

	public void setDispatch(String dispatch) {
		this.dispatch = dispatch;
	}
	
	
}
