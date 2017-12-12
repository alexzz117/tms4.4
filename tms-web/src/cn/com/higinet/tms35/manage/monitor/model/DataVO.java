package cn.com.higinet.tms35.manage.monitor.model;

import java.util.Map;

public class DataVO {
	private Object data;
	private String dataType;
	private String sqlId;
	private Map<String,Object> cond;
	private String extSql;
	//false:在线库,true:临时库	
	private boolean useTmp = false;
	
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public String getSqlId() {
		return sqlId;
	}
	public void setSqlId(String sqlId) {
		this.sqlId = sqlId;
	}
	public Map<String, Object> getCond() {
		return cond;
	}
	public void setCond(Map<String, Object> cond) {
		this.cond = cond;
	}
	public String getExtSql() {
		return extSql;
	}
	public void setExtSql(String extSql) {
		this.extSql = extSql;
	}
	public boolean isUseTmp() {
		return useTmp;
	}
	public void setUseTmp(boolean useTmp) {
		this.useTmp = useTmp;
	}
	
}
