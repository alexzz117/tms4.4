package cn.com.higinet.tms35.manage.query.common.model;


/**
 * 处置
 * @author liining
 *
 */
public class Handle {
	/**
	 * 数据库sql处理
	 */
	private String db;
	/**
	 * 前台js处理
	 */
	private String script;
	/**
	 * 后台处理
	 */
	private String method;

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
}
