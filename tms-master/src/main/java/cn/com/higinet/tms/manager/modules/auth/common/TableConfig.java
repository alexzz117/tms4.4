package cn.com.higinet.tms.manager.modules.auth.common;

import java.util.List;
import java.util.Map;

public class TableConfig {
	private String id;
	private List<Map<String,String>> fields;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<Map<String, String>> getFields() {
		return fields;
	}
	public void setFields(List<Map<String, String>> fields) {
		this.fields = fields;
	}
}
