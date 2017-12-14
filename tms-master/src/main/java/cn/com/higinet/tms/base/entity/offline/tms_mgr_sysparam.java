package cn.com.higinet.tms.base.entity.offline;

import javax.persistence.*;

import cn.com.higinet.tms.base.entity.common.EntityBase;

@Entity
public class tms_mgr_sysparam extends EntityBase<tms_mgr_sysparam> {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "SYSPARAMID")
	Integer sysparamId;

	@Column(name = "SYSPARAMNAME")
	String sysParamName;

	@Column(name = "DATATYPE")
	String dataType;

	@Column(name = "VALUETYPE")
	String valueType;

	@Column(name = "DICTID")
	String dictId;

	@Column(name = "ORDERBY")
	Integer orderBy;

	@Column(name = "STARTVALUE")
	String startValue;

	@Column(name = "ENDVALUE")
	String endValue;

	@Column(name = "SYSPARAMMODULE")
	String sysParamModule;

	@Column(name = "SYSPARAMDESC")
	String sysParamDesc;

	public Integer getSysparamId() {
		return sysparamId;
	}

	public void setSysparamId( Integer sysparamId ) {
		this.sysparamId = sysparamId;
	}

	public String getSysParamName() {
		return sysParamName;
	}

	public void setSysParamName( String sysParamName ) {
		this.sysParamName = sysParamName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType( String dataType ) {
		this.dataType = dataType;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType( String valueType ) {
		this.valueType = valueType;
	}

	public String getDictId() {
		return dictId;
	}

	public void setDictId( String dictId ) {
		this.dictId = dictId;
	}

	public Integer getOrderBy() {
		return orderBy;
	}

	public void setOrderBy( Integer orderBy ) {
		this.orderBy = orderBy;
	}

	public String getStartValue() {
		return startValue;
	}

	public void setStartValue( String startValue ) {
		this.startValue = startValue;
	}

	public String getEndValue() {
		return endValue;
	}

	public void setEndValue( String endValue ) {
		this.endValue = endValue;
	}

	public String getSysParamModule() {
		return sysParamModule;
	}

	public void setSysParamModule( String sysParamModule ) {
		this.sysParamModule = sysParamModule;
	}

	public String getSysParamDesc() {
		return sysParamDesc;
	}

	public void setSysParamDesc( String sysParamDesc ) {
		this.sysParamDesc = sysParamDesc;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}