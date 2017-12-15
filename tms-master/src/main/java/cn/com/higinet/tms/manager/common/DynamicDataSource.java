/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.common;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 功能名称: 动态数据源加载器 功能说明: 
 * url参数动态切换数据源
 * 
 * @author 杨葵
 * @version 2011-12-22 下午2:03:30
 * @see
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

	protected Object determineCurrentLookupKey() {
		return DSContextHolder.getDSType();
	}
}
