package cn.com.higinet.tms.base.entity.offline;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

import cn.com.higinet.tms.base.entity.common.EntityBase;

@Entity
public class tms_mgr_rostervalue extends EntityBase<tms_mgr_rostervalue> {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "generator")
	@GenericGenerator(name = "generator", strategy = "increment")
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

	public Integer getRosterid() {
		return this.rosterid;
	}

	public void setRosterid( Integer rosterid ) {
		this.rosterid = rosterid;
	}

	public Integer getRostervalueid() {
		return this.rostervalueid;
	}

	public void setRostervalueid( Integer rostervalueid ) {
		this.rostervalueid = rostervalueid;
	}

	public String getRostervalue() {
		return this.rostervalue;
	}

	public void setRostervalue( String rostervalue ) {
		this.rostervalue = rostervalue;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark( String remark ) {
		this.remark = remark;
	}

	public Long getEnabletime() {
		return this.enabletime;
	}

	public void setEnabletime( Long enabletime ) {
		this.enabletime = enabletime;
	}

	public Long getDisabletime() {
		return this.disabletime;
	}

	public void setDisabletime( Long disabletime ) {
		this.disabletime = disabletime;
	}

	public Long getCreatetime() {
		return this.createtime;
	}

	public void setCreatetime( Long createtime ) {
		this.createtime = createtime;
	}

	public Long getModifytime() {
		return this.modifytime;
	}

	public void setModifytime( Long modifytime ) {
		this.modifytime = modifytime;
	}

	public String getEncrypt() {
		return this.encrypt;
	}

	public void setEncrypt( String encrypt ) {
		this.encrypt = encrypt;
	}
}