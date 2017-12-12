package cn.com.higinet.tms35.manage.query.model;

public class QueryScript {
	/**
	 * 初始化页面数据，调用的函数名
	 * 多个以分号(;)分割
	 */
	private String init_call;
	/**
	 * 页面脚本$(document).ready中定义的脚本
	 */
	private String ready_script;
	/**
	 * 提供调用的脚本函数
	 * $(document).ready之外的
	 */
	private String call_script;

	public String getInit_call() {
		return init_call;
	}

	public void setInit_call(String init_call) {
		this.init_call = init_call;
	}

	public String getReady_script() {
		return ready_script;
	}

	public void setReady_script(String ready_script) {
		this.ready_script = ready_script;
	}

	public String getCall_script() {
		return call_script;
	}

	public void setCall_script(String call_script) {
		this.call_script = call_script;
	}
}
