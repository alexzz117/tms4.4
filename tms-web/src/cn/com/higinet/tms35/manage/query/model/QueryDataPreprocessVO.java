package cn.com.higinet.tms35.manage.query.model;

import java.util.Map;

public class QueryDataPreprocessVO {

	private String statemeProcessBeanName;

	private QueryJsonVO jsonVO;

	private Map<String, Object> filedsMap;

	public String getStatemeProcessBeanName() {
		return statemeProcessBeanName;
	}

	public void setStatemeProcessBeanName(String statemeProcessBeanName) {
		this.statemeProcessBeanName = statemeProcessBeanName;
	}

	public QueryJsonVO getJsonVO() {
		return jsonVO;
	}

	public void setJsonVO(QueryJsonVO jsonVO) {
		this.jsonVO = jsonVO;
	}

	public Map<String, Object> getFiledsMap() {
		return filedsMap;
	}

	public void setFiledsMap(Map<String, Object> filedsMap) {
		this.filedsMap = filedsMap;
	}
}
