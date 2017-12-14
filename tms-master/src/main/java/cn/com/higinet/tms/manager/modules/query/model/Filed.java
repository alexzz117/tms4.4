package cn.com.higinet.tms.manager.modules.query.model;

public class Filed {
	/**
	 * 数据字段名
	 * 可为TMS_COM_FD表中已有字段名，
	 * 也可自定义字段名
	 */
	private String fd_name;

	/**
	 * 字段的中文描述
	 * 对应TMS_COM_FD表中NAME字段值
	 */
	private String title;

	/**
	 * TMS类型
	 * 对应TMS_COM_FD表中TYPE字段值
	 */
	private String tms_type;

	/**
	 * 数据库类型
	 * 对应TMS_COM_FD表中DB_TYPE字段值
	 */
	private String db_type;

	/**
	 * 数据库字段长度
	 * 对应TMS_COM_FD表中DB_LEN字段值
	 */
	private int db_len;

	/**
	 * 引用的TMS代码值
	 * 对应TMS_COM_FD表中CODE字段值
	 */
	private String code;

	/**
	 * 字段超链接路径
	 * 对应TMS_COM_FD表中的LINK字段值
	 */
	private String link;

	public String getFd_name() {
		return fd_name;
	}

	public void setFd_name(String fd_name) {
		this.fd_name = fd_name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTms_type() {
		return tms_type;
	}

	public void setTms_type(String tms_type) {
		this.tms_type = tms_type;
	}

	public String getDb_type() {
		return db_type;
	}

	public void setDb_type(String db_type) {
		this.db_type = db_type;
	}

	public int getDb_len() {
		return db_len;
	}

	public void setDb_len(int db_len) {
		this.db_len = db_len;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
}