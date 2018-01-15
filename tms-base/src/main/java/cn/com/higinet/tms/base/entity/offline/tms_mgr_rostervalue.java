package cn.com.higinet.tms.base.entity.offline;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import cn.com.higinet.tms.base.entity.common.EntityBase;
import lombok.Data;

@Data
@Entity
public class tms_mgr_rostervalue implements EntityBase<tms_mgr_rostervalue> {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "generator")
	@Column(name = "ROSTERVALUEID")
	private Integer rostervalueid;

	@Column(name = "ROSTERID")
	private Integer rosterid;

	@Column(name = "ROSTERVALUE")
	private String rostervalue;

	@Column(name = "REMARK")
	private String remark;

	@Column(name = "ENABLETIME")
	private Long enabletime;

	@Column(name = "DISABLETIME")
	private Long disabletime;

	@Column(name = "CREATETIME")
	private Long createtime;

	@Column(name = "MODIFYTIME")
	private Long modifytime;

	@Column(name = "ENCRYPT")
	private String encrypt;
}