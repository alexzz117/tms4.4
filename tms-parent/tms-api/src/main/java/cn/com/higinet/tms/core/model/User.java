/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.model;

import java.io.Serializable;
import java.util.List;
/**
 * 
 * @author zhangfg
 * @version 3.0.0
 * @date 2012-06-12
 * @description 客户信息的持久化类
 */
public class User implements Serializable{
	
	//客户号
	private String cstNo;
	//用户名（登录用）
	private String userName;
	//客户姓名
	private String cusName;
	//证件类型
	private String ctfType;
	//证件号码
	private String ctfNo;
	//联系电话
	private String phone;
	//开户行
	private String openNode;
	//性别
	private String Sex;
	//出生年月
	private String birthday;
	//客户类型
	private String accType;
	//开户时间
	private String openTime;
	//客户账户信息
	private List<Account> accounts;
	
	/**
	 * 获取客户号
	 * @return
	 */
	public String getCstNo() {
		return cstNo;
	}
	/**
	 * 设置客户号
	 * @param cstNo
	 */
	public void setCstNo(String cstNo) {
		this.cstNo = cstNo;
	}
	
	/**
	 * 获取用户名（登录用）
	 * @return
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * 设置用户名（登录用）
	 * @param userName
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	/**
	 * 获取客户姓名
	 * @return
	 */
	public String getCusName() {
		return cusName;
	}
	/**
	 * 设置客户姓名
	 * @param cusName
	 */
	public void setCusName(String cusName) {
		this.cusName = cusName;
	}
	/**
	 * 获取证件类型
	 * @return
	 */
	public String getCtfType() {
		return ctfType;
	}
	/**
	 * 设置证件类型
	 * @param ctfType
	 */
	public void setCtfType(String ctfType) {
		this.ctfType = ctfType;
	}
	/**
	 * 获取证件号码
	 * @return
	 */
	public String getCtfNo() {
		return ctfNo;
	}
	/**
	 * 设置证件号码
	 * @param ctfNo
	 */
	public void setCtfNo(String ctfNo) {
		this.ctfNo = ctfNo;
	}
	/**
	 * 获取联系电话
	 * @return
	 */
	public String getPhone() {
		return phone;
	}
	/**
	 * 设置联系电话
	 * @param phone
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}
	/**
	 * 获取开户行
	 * @return
	 */
	public String getOpenNode() {
		return openNode;
	}
	/**
	 * 设置开户行
	 * @param openNode
	 */
	public void setOpenNode(String openNode) {
		this.openNode = openNode;
	}
	/**
	 * 获取性别
	 * @return
	 */
	public String getSex() {
		return Sex;
	}
	/**
	 * 设置性别
	 * @param sex
	 */
	public void setSex(String sex) {
		Sex = sex;
	}
	/**
	 * 获取 出生年月
	 * @return
	 */
	public String getBirthday() {
		return birthday;
	}
	/**
	 * 设置出生年月
	 * @param birthday
	 */
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	/**
	 * 获取客户类型
	 * @return
	 */
	public String getAccType() {
		return accType;
	}
	/**
	 * 设置客户类型
	 * @param accType
	 */
	public void setAccType(String accType) {
		this.accType = accType;
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
	 * @param openTime
	 */
	public void setOpenTime(String openTime) {
		this.openTime = openTime;
	}
	/**
	 * 获取客户账户信息
	 * @return
	 */
	public List<Account> getAccounts() {
		return accounts;
	}
	/**
	 * 设置客户账户信息
	 * @param accounts
	 */
	public void setAccounts(List<Account> accounts) {
		this.accounts = accounts;
	}
	
}
