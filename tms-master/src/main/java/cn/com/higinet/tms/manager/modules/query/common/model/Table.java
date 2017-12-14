package cn.com.higinet.tms.manager.modules.query.common.model;

import java.util.Set;

import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrWebException;

/**
 * 数据表
 * @author liining
 *
 */
public class Table {
	/**
     * TMS_COM_TAB表中TAB_NAME对应，
     * 或动态自定义的唯一表名
     * 
     */
    private String name;
    /**
     * 表别名
     * 
     */
    private String alias;
    /**
     * 是否加载表中字段设置
     */
    private boolean loadField = true;
    /**
     * 动态表名的SQL查询语句
     * 表名不确定时使用
     * 
     */
    private String dynamic;
    /**
     * 数据字段设置
     * 
     */
    private Set<Field> fields;
    
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if(CmcStringUtil.isBlank(name)){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[dbdata]->[tables]节点下的[name]不能为空");
		}
		this.name = name;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		if(CmcStringUtil.isBlank(alias)){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[dbdata]->[tables]节点下的[alias]不能为空");
		}
		this.alias = alias;
	}
	public boolean isLoadField() {
		return loadField;
	}
	public void setLoadField(boolean loadField) {
		this.loadField = loadField;
	}
	public String getDynamic() {
		return dynamic;
	}
	public void setDynamic(String dynamic) {
		this.dynamic = dynamic;
	}
	public Set<Field> getFields() {
		return fields;
	}
	public void setFields(Set<Field> fields) {
		this.fields = fields;
	}
}
