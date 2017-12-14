/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common;

/**
 * WEB基础异常类
 * @author chenr
 * @version 2.0.0, 2011-6-23
 * 
 */
public class BaseWebException extends BaseRuntimeException {

	private static final long serialVersionUID = -6558221768990120227L;

	/**
	 * 实例化一个WEB基础异常
	 * @param msg 异常消息
	 */
	public BaseWebException(String msg) {
		super(msg);
	}
	
	/**
	 * 实例化一个WEB基础异常
	 * @param cause 异常
	 */
	public BaseWebException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * 实例化一个WEB基础异常
	 * @param msg 异常消息
	 * @param cause 异常
	 */
	public BaseWebException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
