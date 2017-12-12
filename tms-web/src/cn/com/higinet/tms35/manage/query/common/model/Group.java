package cn.com.higinet.tms35.manage.query.common.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 查询条件组
 * @author liining
 *
 */
public class Group extends Member {
	/**
	 * 标签中文名
	 */
	private String label;
	/**
	 * 条件成员之间连接符
	 */
	private String separator;
	/**
	 * 组织查询条件语句时，
	 * 条件组成员(member)之间的组合方式
	 */
	private String expr;
	/**
	 * 多个成员时，他们之间的关系
	 */
	private String relation;
	/**
	 * 条件成员
	 */
	private Set<Member> members = new LinkedHashSet<Member>();

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getExpr() {
		return expr;
	}

	public void setExpr(String expr) {
		this.expr = expr;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public Set<Member> getMembers() {
		return members;
	}

	public void setMembers(Set<Member> members) {
		this.members = members;
	}
}