/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common;

/**
 * CMC库表常量类
 * @author chenr
 * @version 2.0.0, 2011-6-30
 */
public class DBConstant {

	/**
	 * 代码表
	 */
	public final static String CMC_CODE = "CMC_CODE";
	
	/**
	 * 代码类别表
	 */
	public final static String CMC_CODE_CATEGORY = "CMC_CODE_CATEGORY";
	
	/**
	 * 功能表
	 */
	public final static String CMC_FUNC = "CMC_FUNC";
	public final static String CMC_FUNC_FUNC_ID = "FUNC_ID";
	public final static String CMC_FUNC_FUNC_NAME = "FUNC_NAME";
	public final static String CMC_FUNC_FUNC_TYPE = "FUNC_TYPE";
	public final static String CMC_FUNC_FUNC_PARENTID = "PARENT_ID";
	public final static String CMC_FUNC_FUNC_ONUM = "ONUM";
	public final static String CMC_FUNC_INFO = "INFO";
	
	/**
	 * 操作日志表
	 */
	public final static String CMC_OPERATE_LOG = "CMC_OPERATE_LOG";
	public final static String CMC_OPERATE_LOG_LOG_ID = "LOG_ID";
	public final static String CMC_OPERATE_LOG_OPERATOR_ID = "OPERATOR_ID";
	public final static String CMC_OPERATE_LOG_OPERATE_TIME = "OPERATE_TIME";
	public final static String CMC_OPERATE_LOG_FUNC_ID = "FUNC_ID";
	public final static String CMC_OPERATE_LOG_OPERATE_RESULT = "OPERATE_RESULT";
	public final static String CMC_OPERATE_LOG_OPERATE_DATA = "OPERATE_DATA";
	public final static String CMC_OPERATE_LOG_LOG_MAC = "LOG_MAC";
	public final static String CMC_OPERATE_LOG_PRIMARY_KEY_ID = "PRIMARY_KEY_ID";
	
	/**
	 * 操作员表
	 */
	public final static String CMC_OPERATOR = "CMC_OPERATOR";
	public final static String CMC_OPERATOR_OPERATOR_ID = "OPERATOR_ID";
	public final static String CMC_OPERATOR_LOGIN_NAME = "LOGIN_NAME";
	public final static String CMC_OPERATOR_PASSWORD = "PASSWORD";
	
	/**
	 * 操作员与角色关系表
	 */
	public final static String CMC_OPERATOR_ROLE_REL = "CMC_OPERATOR_ROLE_REL";
	
	/**
	 * 角色表
	 */
	public final static String CMC_ROLE = "CMC_ROLE";
	public final static String CMC_ROLE_ROLE_NAME = "ROLE_NAME";
	public final static String CMC_ROLE_ID = "ROLE_ID";
	
	/**
	 * 角色与功能关系表
	 */
	public final static String CMC_ROLE_FUNC_REL = "CMC_ROLE_FUNC_REL";
	
}
