/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.model;

import java.io.Serializable;
/**
 * 
 * @author zhangfg
 * @version 3.0.0
 * @date 2012-06-12
 * @description 账户信息的持久化类
 */
public class Account implements Serializable {
	//账号
	private String accNo;
	//网银客户号
	private String cstNo;
	//开户网点
	private String openNode;
	//开户时间
	private String openTime;
	//开户城市
	private String openNodeCity;
	//余额
	private String balance;
	
	/**
	 * 获取账号
	 * @return
	 */
	public String getAccNo() {
		return accNo;
	}
	/**
	 * 设置账号
	 * @param accNo
	 */
	public void setAccNo(String accNo) {
		this.accNo = accNo;
	}
	/**
	 * 获取网银客户号
	 * @return
	 */
	public String getCstNo() {
		return cstNo;
	}
	/**
	 * 设置网银客户号
	 * @param cstNo
	 */
	public void setCstNo(String cstNo) {
		this.cstNo = cstNo;
	}
	/**
	 * 获取开户网点
	 * @return
	 */
	public String getOpenNode() {
		return openNode;
	}
	/**
	 * 设置开户网点
	 * @param openNode
	 */
	public void setOpenNode(String openNode) {
		this.openNode = openNode;
	}
	/**
	 * 获取开户时间
	 * @return
	 */
	public String getOpenTime() {
		return openTime;
	}
	/**
	 * 设置开户时间
	 * @param opentTme
	 */
	public void setOpenTime(String openTime) {
		this.openTime = openTime;
	}
	/**
	 * 获取开户城市
	 * @return
	 */
	public String getOpenNodeCity() {
		return openNodeCity;
	}
	/**
	 * 设置开户城市
	 * @param openNodeCity
	 */
	public void setOpenNodeCity(String openNodeCity) {
		this.openNodeCity = openNodeCity;
	}
	/**
	 * 获取余额
	 * @return
	 */
	public String getBalance() {
		return balance;
	}
	/**
	 * 设置余额
	 * @param balance
	 */
	public void setBalance(String balance) {
		this.balance = balance;
	}
	
	
}
