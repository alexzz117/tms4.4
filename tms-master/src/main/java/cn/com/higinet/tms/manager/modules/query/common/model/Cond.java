package cn.com.higinet.tms.manager.modules.query.common.model;

import java.util.Set;

/**
 * 查询条件
 * @author liining
 *
 */
public class Cond {
	/**
	 * 查询条件界面是否展示
	 * 默认false
	 */
	private boolean unfold = false;
	/**
	 * 查询条件界面Form布局
	 * auto、list
	 * 默认list
	 */
	private String layout = "list";
	/**
	 * 组织查询条件语句时，
	 * 条件组(group)之间的组合方式
	 */
	private String expr;
	
	private Set<Group> groups;
	
	/**
	 * 查询校验script
	 */
	private String callcheck;
	
	public boolean isUnfold() {
		return unfold;
	}
	public void setUnfold(boolean unfold) {
		this.unfold = unfold;
	}
	public String getLayout() {
		return layout;
	}
	public void setLayout(String layout) {
		this.layout = layout;
	}
	public String getExpr() {
		return expr;
	}
	public void setExpr(String expr) {
		this.expr = expr;
	}
	public Set<Group> getGroups() {
		return groups;
	}
	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}
	public String getCallcheck() {
		return callcheck;
	}
	public void setCallcheck(String callcheck) {
		this.callcheck = callcheck;
	}
}