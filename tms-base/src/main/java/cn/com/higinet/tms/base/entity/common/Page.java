/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.base.entity.common;

import java.util.List;

import lombok.Data;

/**
 * 页对象（分页查询后结果）
 * @param <T> 页中记录列表的记录（可以是域对象或者Map） 
 * @author chenr
 * @version 2.0.0, 2011-6-22
 * 
 * @author zhang.lei
 */
@Data
public class Page<T> {

	/**
	 * 页大小，默认25
	 */
	
	private int size = 25;
	/**
	 * 页码，默认1
	 */
	private int index = 1;
	/**
	 * 总记录数，默认0
	 */
	private long total = 0L;
	/**
	 * 记录列表
	 */
	private List<T> list;

}
