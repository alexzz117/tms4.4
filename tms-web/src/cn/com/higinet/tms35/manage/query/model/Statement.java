package cn.com.higinet.tms35.manage.query.model;

public class Statement {
	/**
	 * 语句类型
	 * 1：sql查询语句
	 * 2：存储过程查询语句
	 * 3：多个查询组合的标签页面
	 */
	private int type;

	/**
	 * 对应的查询语句
	 * 或多个查询组合的表达式
	 * (例如：10010||20301，
	 * 10010和20301为自定义查询主键id)
	 */
	private String content;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
