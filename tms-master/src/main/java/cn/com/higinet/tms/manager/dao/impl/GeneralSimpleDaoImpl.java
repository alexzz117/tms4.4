package cn.com.higinet.tms.manager.dao.impl;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import cn.com.higinet.tms.manager.dao.DomainWrap;
import cn.com.higinet.tms.manager.dao.GeneralJdbcTemplate;
import cn.com.higinet.tms.manager.dao.GeneralNamedParameterJdbcTemplate;
import cn.com.higinet.tms.manager.dao.MapRowMapper;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.Page;

/**
 * 通用数据库分页(jdbc)
 * @author lining@higinet.com.cn
 * @version 4.0
 * @since 2014-9-16 上午9:47:14
 * @description
 */
public class GeneralSimpleDaoImpl extends AbstractSimpleDao {
	
	private GeneralJdbcTemplate jdbcTemplate;
	private GeneralNamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		super.setJdbcTemplate(jdbcTemplate);
		this.jdbcTemplate = new GeneralJdbcTemplate(jdbcTemplate.getDataSource(), jdbcTemplate.isLazyInit());
		this.namedParameterJdbcTemplate = new GeneralNamedParameterJdbcTemplate(this.jdbcTemplate);
		this.namedParameterJdbcTemplate.setResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE);
	}
	
	public NamedParameterJdbcTemplate getGeneralNamedParameterJdbcTemplate() {
		return this.namedParameterJdbcTemplate;
	}
	
	private void makeConditions(Map<String, ?> params, int pageindex, int pagesize) {
		int start = (pageindex - 1) * pagesize;
		int end = start + pagesize;
		this.jdbcTemplate.setMaxRows(end);
		this.jdbcTemplate.setAbsolute(start);
	}

	public <T> Page<T> pageQuery(String sql, Map<String, ?> params,
			int pageindex, int pagesize, Order order, Class<T> c) {
		DomainWrap<T> dw = new DomainWrap<T>(c);
		long totalCount = count(sql, params);
		makeConditions(params, pageindex, pagesize);
		sql += order.toSqlString();
		String execSql = sql;
		List<T> pageList = getGeneralNamedParameterJdbcTemplate().query(execSql, params, dw);
		Page<T> page = new Page<T>();
		page.setSize(pagesize);
		page.setIndex(pageindex);
		page.setTotal(totalCount);
		page.setList(pageList);
		return page;
	}

	public Page<Map<String, Object>> pageQuery(String sql,
			Map<String, ?> params, int pageindex, int pagesize, Order order) {
		long totalCount = count(sql, params);
		makeConditions(params, pageindex, pagesize);
		sql += order.toSqlString();
		String execSql = sql;
		List<Map<String, Object>> pageList = getGeneralNamedParameterJdbcTemplate().query(execSql, params, new MapRowMapper());
		Page<Map<String, Object>> page = new Page<Map<String, Object>>();
		page.setSize(pagesize);
		page.setIndex(pageindex);
		page.setTotal(totalCount);
		page.setList(pageList);
		return page;
	}
}