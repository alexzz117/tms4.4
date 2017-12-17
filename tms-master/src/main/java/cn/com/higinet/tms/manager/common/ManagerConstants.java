/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common;

/**
 * 常量类
 * @author zhang.lei
 */
public class ManagerConstants {
	
	/**
	 * 会话操作员登录信息对象的key名称
	 */
	public final static String SESSION_KEY_OPERATOR = "OPERATOR";
	public final static String SESSION_KEY_FUNCIDS = "FUNCIDS";
	public final static String SESSION_KEY_PROFILE = "PROFILE";
	
	
	/**
	 * 主界面路径
	 */
	public final static String URI_MAIN = "/";
	/**
	 * 登录URI路径
	 */
	public final static String URI_LOGIN = "/login";
	
	/**
	 * 静态资源路径前缀 
	 */
	public final static String URI_STATIC_PREFIX = "/s/";
	
	/**
	 * 主界面路径
	 */
	public final static String URI_PREFIX = "/manager";
	
	
	//TODO: 统一错误信息的命名规范
	
	/**
	 * 错误信息：登录输入错误
	 */
	public final static String ERROR_LOGIN_INPUTWORNG = "error.cmc.login.inputwrong";
	
	/**
	 * 错误信息：登录操作员处非有效状态
	 */
	public final static String ERROR_LOGIN_FLAGINVALID = "error.cmc.login.flaginvalid";
	
}
