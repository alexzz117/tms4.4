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
 * @version 2.2.0
 * @date 2011-10-21
 * @description 返回给外部的风险信息的持久化类
 */
public class Result implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 157582375627878917L;
	//接口返回代码
	private String backCode;
	//返回信息描述
	private String backInfo;
	
	public String getBackCode() {
		return backCode;
	}
	public void setBackCode(String backCode) {
		this.backCode = backCode;
	}
	public String getBackInfo() {
		return backInfo;
	}
	public void setBackInfo(String backInfo) {
		this.backInfo = backInfo;
	}	

	public Result(){
		
	}
	public Result(String backCode,String errorInfo){
		this.backCode = backCode;
		this.backInfo = errorInfo;
	}
}
