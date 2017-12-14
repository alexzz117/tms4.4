/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 功能名称:
 * 数据源环境切换器
 * @author 杨葵
 * @version 2011-12-22 下午2:05:30
 * @see 
 */
public class DSContextHolder {
	
	private static Log log = LogFactory.getLog(DSContextHolder.class);
	
	private static final ThreadLocal<DSType> dbTypeHolder = new ThreadLocal<DSType>();
	
	public static void setDSType(DSType dsType){
		dbTypeHolder.set(dsType);
	}
	
	public static DSType getDSType(){
		return (DSType)dbTypeHolder.get();
	}
	
	public static void clearDSType(){
		dbTypeHolder.remove();
	}
}
