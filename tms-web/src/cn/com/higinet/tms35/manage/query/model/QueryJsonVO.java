package cn.com.higinet.tms35.manage.query.model;

import java.util.List;

public class QueryJsonVO {

	/**
	 * 普通查询
	 */
	public static final int QUERY_TYPE_1 = 1;
	/**
	 * 实体展示
	 */
	public static final int QUERY_TYPE_2 = 2;

	/**
	 * sql语句
	 */
	public static final int QUERY_STATEMENT_TYPE_1 = 1;
	/**
	 * 存储过程
	 */
	public static final int QUERY_STATEMENT_TYPE_2 = 2;
	/**
	 * 多查询组合的标签页
	 */
	public static final int QUERY_STATEMENT_TYPE_3 = 3;
	/**
	 * 多查询组合的上下结构的同页面展示
	 */
	public static final int QUERY_STATEMENT_TYPE_4 = 4;
	/**
	 * 查询语句拼写类型，自动组装
	 */
	public static final int QUERY_SPELL_TYPE_1 = 1;
	/**
	 * 查询语句拼写类型，手工编写
	 */
	public static final int QUERY_SPELL_TYPE_2 = 2;

	/**
	 * 当查询类型为实体时，指定实体查询结果数为1时的显示类型
	 */
	public static final String QUERY_RESULT_DISPLAY_TYPE_LIST = "list";
	public static final String QUERY_RESULT_DISPLAY_TYPE_WIRELIST = "wirelist";
	public static final String QUERY_RESULT_DISPLAY_TYPE_NO_WIRELIST = "nowirelist";
	public static final String QUERY_RESULT_DISPLAY_TYPE_DETAIL = "detail";

	/**
	 * 查询展示类型
	 * 1：普通查询页面
	 * 2：实体展现页面
	 * 3：多查询标签页
	 */
	private int query_type;

	/**
	 * 查询页面标题名
	 * 设置后会覆盖TMS_COM_QUERY表中QUERY_DESC字段值
	 */
	private String query_title;
	/**
	 * 页面box的标题，不填的话不新建box框
	 */
	private String box_title;
	/**
	 * 查询语句
	 */
	private Statement statement;

	/**
	 * 查询语句拼写类型
	 * 1：自动组装
	 * 2：手工编写
	 */
	private int spell_type;

	/**
	 * 查询使用的表数据
	 */
	private List<Table> tables;

	/**
	 * 自定义数据字段的配置
	 */
	private List<Filed> fileds;

	/**
	 * 查询脚本配置
	 */
	private QueryScript query_script;

	/**
	 * 查询条件配置
	 */
	private QueryCond query_cond;

	/**
	 * 查询结果配置
	 */
	private QueryResult query_result;

	public int getQuery_type() {
		return query_type;
	}

	public void setQuery_type(int query_type) {
		this.query_type = query_type;
	}

	public String getQuery_title() {
		return query_title;
	}

	public void setQuery_title(String query_title) {
		this.query_title = query_title;
	}

	public String getBox_title() {
		return box_title;
	}

	public void setBox_title(String box_title) {
		this.box_title = box_title;
	}

	public Statement getStatement() {
		return statement;
	}

	public void setStatement(Statement statement) throws Exception {
		this.statement = statement;
	}

	public int getSpell_type() {
		return spell_type;
	}

	public void setSpell_type(int spell_type) throws Exception {
		this.spell_type = spell_type;
	}

	public List<Table> getTables() {
		return tables;
	}

	public void setTables(List<Table> tables) {
		this.tables = tables;
	}

	public List<Filed> getFileds() {
		return fileds;
	}

	public void setFileds(List<Filed> fileds) {
		this.fileds = fileds;
	}

	public QueryScript getQuery_script() {
		return query_script;
	}

	public void setQuery_script(QueryScript query_script) {
		this.query_script = query_script;
	}

	public QueryCond getQuery_cond() {
		return query_cond;
	}

	public void setQuery_cond(QueryCond query_cond) {
		this.query_cond = query_cond;
	}

	public QueryResult getQuery_result() {
		return query_result;
	}

	public void setQuery_result(QueryResult query_result) {
		this.query_result = query_result;
	}
}