package cn.com.higinet.tms35.manage.datamgr.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 每个模块的数据配置对象
 * @author zhangfg
 * @version 1.0.0 2012-09-19
 */
public class DataConfig {
	
	private String id ;
	private String name;
	private String type;
	private String statusKey;
	private Map<String,String> attrabutes;
	
	//存放当前模块中需要用到的数据，以便当前模块下的其他对象查询时作为条件使用
	private Map<String,Object> dataValues;
	
	/**
	 * 存放需要操作的数据。表名作为key，要操作的记录作为值
	 */
	private Map<String,List<Map<String,Object>>> operData;
	/**
	 * 存放需要操作的数据的查询条件，表名作为key
	 */
	private Map<String,String> whereMap;
	
	private List children;
	 /**
     * filter是table的子集描述，表示一个操作数据库的过滤条件
     */
    private List filters;
    /**
     * reference是table的子集描述，用来描述当前table的子表关系
     * List对象内为DataRule对象
     */
    private List references;
    /**
     * 获取当前记录的主键值
     */
    private String pkValue;
    /**
     * 与父表的关联字段名
     */
    private String parentReferenceFk;
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getStatusKey() {
		return statusKey;
	}
	public void setStatusKey(String statusKey) {
		this.statusKey = statusKey;
	}
	public Map<String, String> getAttrabutes() {
		return attrabutes;
	}
	public void setAttrabutes(Map<String, String> attrabutes) {
		this.attrabutes = attrabutes;
	}
	public List getChildren() {
		return children;
	}
	public void setChildren(List children) {
		this.children = children;
	}
	public List getFilters() {
		return filters;
	}
	public void setFilters(List filters) {
		this.filters = filters;
	}
	public List getReferences() {
		return references;
	}
	public void setReferences(List references) {
		this.references = references;
	}
	public Map<String, Object> getDataValues() {
		if(dataValues==null)
			dataValues = new HashMap<String, Object>();
		return dataValues;
	}
	public void setDataValues(Map<String, Object> dataValues) {
		this.dataValues = dataValues;
	}
	
	public String getPkValue() {
		return pkValue;
	}
	public void setPkValue(String pkValue) {
		this.pkValue = pkValue;
	}
	
	public Map<String, List<Map<String, Object>>> getOperData() {
		if(operData==null)
			operData = new HashMap<String, List<Map<String, Object>>>();
		return operData;
	}
	public void setOperData(Map<String, List<Map<String, Object>>> operData) {
		this.operData = operData;
	}
	
	public String getParentReferenceFk() {
		return parentReferenceFk;
	}
	public void setParentReferenceFk(String parentReferenceFk) {
		this.parentReferenceFk = parentReferenceFk;
	}
	
	public Map<String, String> getWhereMap() {
		if(whereMap==null) 
			whereMap = new HashMap<String, String>();
		return whereMap;
	}
	public void setWhereMap(Map<String, String> whereMap) {
		this.whereMap = whereMap;
	}
	/**
	 * 获取一个属性的值
	 * @param key
	 * @return
	 */
	public String getAttrabute(String key){
		if(this.attrabutes==null || this.attrabutes.isEmpty()){
			return null;
		}
		return this.attrabutes.get(key);
	}
}
