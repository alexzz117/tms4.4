/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import cn.com.higinet.tms.core.util.DateUtil;
import cn.com.higinet.tms.core.util.StringUtil;

/**
 * 
 * @author zhangfg
 * @version 3.0.0
 * @date 2012-06-12
 * @description 交易相关信息的持久化类
 */
public class Transaction implements Serializable {
	/**
	 * 运行周期
	 */
	private OperatCycle operatCycle;
	/**
	 * 交易流水号
	 */
	private String transCode;
	/**
	 * 交易时间(年-月-日 时:分:秒)
	 */
	private String transTime;
	/**
	 * 交易类型
	 */
	private String transType;
	/**
	 * 交易状态(2 正在处理;1 交易成功;0交易失败)
	 */
	private String transStatus;
	/**
	 * 网银交易异常代码
	 */
	private String expCode;
	/**
	 * 认证状态(2 正在处理;1 认证成功;0认证失败)
	 */
	private String authStatus;
	/**
	 * 客户端渠道类型(01 个人网银;02 企业网银)
	 */
	private String channelType;
	/**
	 * 用户ID(客户号)
	 */
	private String cstNo;
	/**
	 * 会话ID
	 */
	private String sessionId;
	/**
	 * MAC地址/SIM卡编号
	 */
	private String deviceId;
	/**
	 * ip地址
	 */
	private String ipAddress;
	/**
	 * 客户签约信息
	 */
	private Customer customer;
	/**
	 * 设备指纹信息
	 */
	private String deviceFinger;
	/**
	 * 设备标记
	 */
	private String deviceToken;

	/**
	 * 分发用
	 */
	private String dispatch;

	/**
	 * 其他信息，每个交易特有的信息，在具体交易中描述
	 */
	private Map<String, Object> extInfo;

	private boolean isTimeOut = false;

	public Transaction() {
		operatCycle = new OperatCycle();
		operatCycle.setRiskTrades(this);
		this.transCode = "";
		this.transTime = "";
		this.transType = "";
		this.transStatus = "";
		this.channelType = "";
		this.expCode = "";
		this.authStatus = "";
		this.cstNo = "";
		this.sessionId = "";
		this.deviceId = "";
		this.ipAddress = "";
		this.customer = null;
		this.extInfo = null;
	}

	/**
	 * 获取操作周期对象
	 * 
	 * @return
	 */
	public OperatCycle getOperatCycle() {
		return operatCycle;
	}

	/**
	 * 获取交易流水号
	 * 
	 * @return
	 */
	public String getTransCode() {
		return transCode;
	}

	/**
	 * 为交易流水号赋值
	 * 
	 * @param transCode
	 */
	public void setTransCode(String transCode) {
		this.transCode = transCode;
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
	 * 为交易时间(年-月-日 时:分:秒)赋值
	 * 
	 * @param transTime
	 */
	public void setTransTime(String transTime) {
		this.transTime = transTime;
	}

	/**
	 * 获取交易类型
	 * 
	 * @return
	 */
	public String getTransType() {
		return transType;
	}

	/**
	 * 为交易类型赋值
	 * 
	 * @param transType
	 */
	public void setTransType(String transType) {
		this.transType = transType;
	}

	/**
	 * 获取交易状态(2 正在处理;1 交易成功;0交易失败)
	 * 
	 * @return
	 */
	public String getTransStatus() {
		return transStatus;
	}

	/**
	 * 为交易状态(2 正在处理;1 交易成功;0交易失败)赋值
	 * 
	 * @param transStatus
	 */
	public void setTransStatus(String transStatus) {
		this.transStatus = transStatus;
	}

	/**
	 * 获取网银交易异常代码
	 * 
	 * @return
	 */
	public String getExpCode() {
		return expCode;
	}

	/**
	 * 为网银交易异常代码赋值
	 * 
	 * @param expCode
	 */
	public void setExpCode(String expCode) {
		this.expCode = expCode;
	}

	/**
	 * 获取认证状态(2 正在处理;1 认证成功;0认证失败)
	 * 
	 * @return
	 */
	public String getAuthStatus() {
		return authStatus;
	}

	/**
	 * 为认证状态(2 正在处理;1 认证成功;0认证失败)赋值
	 * 
	 * @param authStatus
	 */
	public void setAuthStatus(String authStatus) {
		this.authStatus = authStatus;
	}

	/**
	 * 获取客户端渠道类型(01 个人网银;02 企业网银)
	 * 
	 * @return
	 */
	public String getChannelType() {
		return channelType;
	}

	/**
	 * 为客户端渠道类型(01 个人网银;02 企业网银)赋值
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
	 * 为用户ID(客户号)赋值
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
	 * 为会话ID赋值
	 * 
	 * @param sessionId
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * 获取设备ID
	 * 
	 * @return
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * 为设备ID赋值
	 * 
	 * @param deviceId
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
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
	 * 为IP地址赋值
	 * 
	 * @param ipAddress
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * 获取客户对象
	 * 
	 * @return
	 */
	public Customer getCustomer() {
		return customer;
	}

	/**
	 * 为客户对象赋值
	 * 
	 * @param customer
	 */
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	/**
	 * 获取扩展信息
	 * 
	 * @return
	 */
	public Map<String, Object> getExtInfo() {
		return extInfo;
	}

	/**
	 * 为扩展信息赋值
	 * 
	 * @param extInfo
	 */
	public void setExtInfo(Map<String, Object> extInfo) {
		this.extInfo = extInfo;
	}

	/**
	 * 获取是否超时标识
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

	public String getDeviceFinger() {
		return deviceFinger;
	}

	public void setDeviceFinger(String deviceFinger) {
		this.deviceFinger = deviceFinger;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	
	public String getDispatch() {
		return dispatch;
	}

	public void setDispatch(String dispatch) {
		this.dispatch = dispatch;
	}

	public void full() {
		// 如果没有交易流水号，生成一个，并为transaction设置
		String transCode = this.getTransCode();
		if (transCode == null || "".equals(transCode)) {
			this.setTransCode(StringUtil.randomUUID());
		}
		// 设置交易时间
		String time = this.getTransTime();
		if (time == null || "".equals(time)) {
			Date date = new Date();
			this.setTransTime(DateUtil.dateConvert(date, "yyyy-MM-dd HH:mm:ss"));
		} else {
			//String datePatten = ServerConfig.getInstance().getProperty("channel.date.patten");
			String datePatten = TmsConfigVo.getDatePatten();
			this.setTransTime(DateUtil.dateConvert(time, datePatten, "yyyy-MM-dd HH:mm:ss"));
		}
	}
}
