package cn.com.higinet.tms.common.elasticsearch;

import java.util.List;

public class FailData<T> {
	
	private String indexName;
	
	private List<T> dataList;
	
	private Class<T> entityType;

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public List<T> getDataList() {
		return dataList;
	}

	public void setDataList(List<T> dataList) {
		this.dataList = dataList;
	}

	public Class<T> getEntityType() {
		return entityType;
	}

	public void setEntityType(Class<T> entityType) {
		this.entityType = entityType;
	}
	
	
}
