package cn.com.higinet.tms.manager.modules.query.common.model;

import java.util.Set;

public class Script {
	/**
	 * 引入外部js文件
	 */
	private Set<Include> includes;
	/**
	 * 绑定事件代码
	 */
	private String bindEvent;
	
	public Set<Include> getIncludes() {
		return includes;
	}
	
	public void setIncludes(Set<Include> includes) {
		this.includes = includes;
	}
	
	public String getBindEvent() {
		return bindEvent;
	}
	
	public void setBindEvent(String bindEvent) {
		this.bindEvent = bindEvent;
	}
}
