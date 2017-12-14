/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common;

/**
 * 服务基础异常类
 * @author chenr
 * @version 2.0.0, 2011-6-23
 * 
 */
public class BaseServiceException extends BaseRuntimeException{

	private static final long serialVersionUID = -1986608571816380520L;

	/**
	 * 实例化一个服务基础异常
	 * @param msg 异常消息
	 */
	public BaseServiceException(String msg) {
		super(msg);
	}
	
	/**
	 * 实例化一个服务基础异常
	 * @param cause 异常
	 */
	public BaseServiceException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * 实例化一个服务基础异常
	 * @param msg 异常消息
	 * @param cause 异常
	 */
	public BaseServiceException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
