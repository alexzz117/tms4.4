package cn.com.higinet.tms.base.entity.offline;

import javax.persistence.*;

import cn.com.higinet.tms.base.entity.common.EntityBase;

import java.util.Date;

@Entity
public class cmc_operator extends EntityBase<cmc_operator> {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	@Column(name = "OPERATOR_ID")
	private String operatorId;

	@Column(name = "LOGIN_NAME")
	private String loginName;

	@Column(name = "PASSWORD")
	private String password;

	@Column(name = "REAL_NAME")
	private String realName;

	@Column(name = "FLAG")
	private String flag;

	@Column(name = "PHONE")
	private String phone;

	@Column(name = "MOBILE")
	private String mobile;

	@Column(name = "EMAIL")
	private String email;

	@Column(name = "ADDRESS")
	private String address;

	@Column(name = "MEMO")
	private String memo;

	@Column(name = "LAST_LOGIN")
	private Date lastLogin;

	@Column(name = "LAST_PWD")
	private Date lastPwd;

	@Column(name = "CONF")
	private String conf;

	@Column(name = "CREDENTIALTYPE")
	private String credentialtype;

	@Column(name = "CREDENTIALNUM")
	private String credentialnum;

	@Column(name = "FAILED_LOGIN_ATTEMPTS")
	private Integer failedLoginAttempts;

	@Column(name = "LAST_FAILED_LOGIN_DATE")
	private Date lastFailedLoginDate;

	@Column(name = "LOCKOUT")
	private Integer lockout;

	@Column(name = "LOCKOUT_DATE")
	private Date lockoutDate;

	@Column(name = "UPLOAD_FILE_TYPE")
	private String uploadFileType;

	@Column(name = "UPLOAD_FILE_MAX_SIZE")
	private String uploadFileMaxSize;

	public Long getId() {
		return id;
	}

	public void setId( Long id ) {
		this.id = id;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId( String operatorId ) {
		this.operatorId = operatorId;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName( String loginName ) {
		this.loginName = loginName;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName( String realName ) {
		this.realName = realName;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin( Date lastLogin ) {
		this.lastLogin = lastLogin;
	}

	public Date getLastPwd() {
		return lastPwd;
	}

	public void setLastPwd( Date lastPwd ) {
		this.lastPwd = lastPwd;
	}

	public Integer getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	public void setFailedLoginAttempts( Integer failedLoginAttempts ) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	public Date getLastFailedLoginDate() {
		return lastFailedLoginDate;
	}

	public void setLastFailedLoginDate( Date lastFailedLoginDate ) {
		this.lastFailedLoginDate = lastFailedLoginDate;
	}

	public Date getLockoutDate() {
		return lockoutDate;
	}

	public void setLockoutDate( Date lockoutDate ) {
		this.lockoutDate = lockoutDate;
	}

	public String getUploadFileType() {
		return uploadFileType;
	}

	public void setUploadFileType( String uploadFileType ) {
		this.uploadFileType = uploadFileType;
	}

	public String getUploadFileMaxSize() {
		return uploadFileMaxSize;
	}

	public void setUploadFileMaxSize( String uploadFileMaxSize ) {
		this.uploadFileMaxSize = uploadFileMaxSize;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword( String password ) {
		this.password = password;
	}

	public String getFlag() {
		return this.flag;
	}

	public void setFlag( String flag ) {
		this.flag = flag;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone( String phone ) {
		this.phone = phone;
	}

	public String getMobile() {
		return this.mobile;
	}

	public void setMobile( String mobile ) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail( String email ) {
		this.email = email;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress( String address ) {
		this.address = address;
	}

	public String getMemo() {
		return this.memo;
	}

	public void setMemo( String memo ) {
		this.memo = memo;
	}

	public String getConf() {
		return this.conf;
	}

	public void setConf( String conf ) {
		this.conf = conf;
	}

	public String getCredentialtype() {
		return this.credentialtype;
	}

	public void setCredentialtype( String credentialtype ) {
		this.credentialtype = credentialtype;
	}

	public String getCredentialnum() {
		return this.credentialnum;
	}

	public void setCredentialnum( String credentialnum ) {
		this.credentialnum = credentialnum;
	}

	public Integer getLockout() {
		return this.lockout;
	}

	public void setLockout( Integer lockout ) {
		this.lockout = lockout;
	}

}