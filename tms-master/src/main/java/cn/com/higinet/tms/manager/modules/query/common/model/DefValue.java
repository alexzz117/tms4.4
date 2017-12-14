package cn.com.higinet.tms.manager.modules.query.common.model;


public class DefValue {
	/**
	 * 默认值
	 */
	private Object value;
	/**
	 * 默认值生成处理
	 */
	private Handle handle;

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Handle getHandle() {
		return handle;
	}

	public void setHandle(Handle handle) {
		this.handle = handle;
	}

}
