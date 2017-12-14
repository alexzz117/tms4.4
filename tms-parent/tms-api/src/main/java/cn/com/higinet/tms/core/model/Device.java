/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.model;

import java.io.Serializable;
import java.util.Map;

/**
 * 
 * @author zhangfg
 * @version 2.2.0
 * @date 2011-10-21
 * @description 标识客户端信息的持久化类
 */
public class Device implements Serializable {
	
	//设备ID
	private String deviceId;
	//设备标识（手机、电话等）
	private String deviceIdentifier;
	//ip地址
	private String IpAddress;
	//其他信息
	private Map<String,Object> extInfo;

	public Device(){
		this.deviceId="";
		this.deviceIdentifier="";
		this.IpAddress="";
		this.extInfo=null;
	}
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceIdentifier() {
		return deviceIdentifier;
	}

	public void setDeviceIdentifier(String deviceIdentifier) {
		this.deviceIdentifier = deviceIdentifier;
	}

	public String getIpAddress() {
		return IpAddress;
	}

	public void setIpAddress(String ipAddress) {
		IpAddress = ipAddress;
	}

	public Map<String, Object> getExtInfo() {
		return extInfo;
	}

	public void setExtInfo(Map<String, Object> extInfo) {
		this.extInfo = extInfo;
	}
	
}
