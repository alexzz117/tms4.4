/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.manager.dao.DomainWrap;
import cn.com.higinet.tms.manager.dao.Order;

/**
 * 数据库的简单访问对象MYSQL实现
 * @author zlq
 * @version 1.0  Sep 16, 2014
 */
public class MysqlSimpleDaoImpl extends AbstractSimpleDao {

	/**
	 * 分页SQL语句模板
	 */
	private static final String SPLIT_PAGE_SQL = "${SQL} LIMIT :__START, :__OFFSET";

	/**
	 * 组合查询条件
	 * @param params 参数Map
	 * @param pageindex 分页页数
	 * @param pagesize 分页大小
	 * @return 包含分页参数的Map
	 */
	private Map<String, Object> makeConditions(Map<String, ?> params, int pageindex, int pagesize){
		
		if(params.containsKey("__START") || params.containsKey("__OFFSET")){
			throw new IllegalArgumentException("common defined param name can not be cover![__START or __OFFSET].");
		}
		if(pageindex <= 0 || pagesize <= 0){
			throw new IllegalArgumentException("pageindex and pagesize must be positive integer.");
		}
		Map<String, Object> p = new HashMap<String, Object>();
		p.putAll(params);
		int start = (pageindex - 1) * pagesize;
		int end = pagesize;
		p.put("__START", start);
		p.put("__OFFSET", end);
		return p;
		
	}

	public <T> Page<T> pageQuery(String sql, Map<String, ?> params,
			int pageindex, int pagesize, Order order, Class<T> c) {
		
		DomainWrap<T> dw = new DomainWrap<T>(c);
		Map<String, Object> p = makeConditions(params, pageindex, pagesize);
		
		long totalCount = count(sql, params);
		
		sql += order.toSqlString();
		String execSql = SPLIT_PAGE_SQL.replaceAll("\\$\\{SQL\\}", sql);
		List<T> pageList = getNamedParameterJdbcTemplate().query(execSql, p, dw);
		
		Page<T> page = new Page<T>();
		page.setSize(pagesize);
		page.setIndex(pageindex);
		page.setTotal(totalCount);
		page.setList(pageList);
		
		return page;
	}

	public Page<Map<String, Object>> pageQuery(String sql,
			Map<String, ?> params, int pageindex, int pagesize, Order order) {
		
		Map<String, Object> p = makeConditions(params, pageindex, pagesize);

		long totalCount = count(sql, params);
		
		sql += order.toSqlString();
		String execSql = SPLIT_PAGE_SQL.replaceAll("\\$\\{SQL\\}", sql);
		List<Map<String, Object>> pageList = queryForList(execSql, p);
		
		Page<Map<String, Object>> page = new Page<Map<String, Object>>();
		page.setSize(pagesize);
		page.setIndex(pageindex);
		page.setTotal(totalCount);
		page.setList(pageList);
		
		return page;
	}

}
