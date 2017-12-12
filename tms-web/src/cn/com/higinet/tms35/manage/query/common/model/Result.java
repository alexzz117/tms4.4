package cn.com.higinet.tms35.manage.query.common.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 查询结果
 * @author liining
 *
 */
public class Result {
	
	/**
	 * 是否初始话查询结果
	 * 默认为false
	 */
	private Boolean initData = false;
	/**
     * 是否去除重复数据
     * 默认为false
     */
    private Boolean distinct = false;
    
    /**
     * 可否多选，即是否出现chackbox框
     * 默认false
     */
    private Boolean mcEnable = false;
    
    /**
     * 数据导出
     */
    private Export export;
    /**
     * 查询结果字段设置
     * 
     */
    private Set<Column> columns = new LinkedHashSet<Column>();
    /**
     * 所有结果显示的回调处理方法
     * 
     */
    private String callback;
    /**
     * 分页设置
     * 
     */
    private Pagination pagination;
    
	public Boolean getInitData() {
		return initData;
	}
	public void setInitData(Boolean initData) {
		this.initData = initData;
	}
	public Boolean getDistinct() {
		return distinct;
	}
	public void setDistinct(Boolean distinct) {
		this.distinct = distinct;
	}
	public Boolean getMcEnable() {
		return mcEnable;
	}
	public void setMcEnable(Boolean mcEnable) {
		this.mcEnable = mcEnable;
	}
	public Export getExport() {
		return export;
	}
	public void setExport(Export export) {
		this.export = export;
	}
	public Set<Column> getColumns() {
		return columns;
	}
	public void setColumns(Set<Column> columns) {
		this.columns = columns;
	}
	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
	}
	public Pagination getPagination() {
		return pagination;
	}
	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}
}
