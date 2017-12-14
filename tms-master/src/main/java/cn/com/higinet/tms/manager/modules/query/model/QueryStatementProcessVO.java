package cn.com.higinet.tms.manager.modules.query.model;


public class QueryStatementProcessVO {

	private String sql;

	private Object conds;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Object getConds() {
		return conds;
	}

	public void setConds(Object conds) {
		this.conds = conds;
	}

}
