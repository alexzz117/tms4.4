package cn.com.higinet.tms35.manage.query.common.model;

import java.util.Map;

public class JsonDataProcess {
	/**
	 * json解析后的实体
	 */
	private Custom custom;
	/**
	 * 所需字段数据
	 */
	private Map<String, Object> fieldsMap;
	/**
	 * 最后执行sql语句的service
	 */
	private String beanName;

	public Custom getCustom() {
		return custom;
	}

	public void setCustom(Custom custom) {
		this.custom = custom;
	}

	public Map<String, Object> getFieldsMap() {
		return fieldsMap;
	}

	public void setFieldsMap(Map<String, Object> fieldsMap) {
		this.fieldsMap = fieldsMap;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
}
