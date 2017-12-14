package cn.com.higinet.tms.manager.modules.query.common.model;

import java.util.LinkedHashSet;
import java.util.Set;


public class Dbdata {

	/**
	 * 表数据设置
	 */
	private Set<Table> tables = new LinkedHashSet<Table>();
	
	/**
	 * 全局字段设置，可新增字段数据，
	 * 会覆盖数据库和私有设置
	 */
	private Set<Field> fields = new LinkedHashSet<Field>();

	public Set<Table> getTables() {
		return tables;
	}

	public void setTables(Set<Table> tables) {
		this.tables = tables;
	}

	public Set<Field> getFields() {
		return fields;
	}

	public void setFields(Set<Field> fields) {
		this.fields = fields;
	}
}
