package cn.com.higinet.tms.manager.modules.query.model;

public class Table {
	/**
	 * 表/实体名称
	 * TMS_COM_TAB表中TAB_NAME字段名
	 */
	private String name;

	/**
	 * 别名
	 */
	private String alias;

	/**
	 * 动态获取表名的查询语句
	 */
	private String dynamic;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getDynamic() {
		return dynamic;
	}

	public void setDynamic(String dynamic) {
		this.dynamic = dynamic;
	}

}
