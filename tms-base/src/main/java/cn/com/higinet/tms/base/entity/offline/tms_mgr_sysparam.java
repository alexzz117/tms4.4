package cn.com.higinet.tms.base.entity.offline;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import cn.com.higinet.tms.base.entity.common.EntityBase;
import lombok.Data;

@Data
@Entity
public class tms_mgr_sysparam implements EntityBase<tms_mgr_sysparam> {

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

}