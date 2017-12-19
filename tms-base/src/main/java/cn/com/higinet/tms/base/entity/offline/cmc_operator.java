package cn.com.higinet.tms.base.entity.offline;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import cn.com.higinet.tms.base.entity.common.EntityBase;
import lombok.Data;

@Entity
@Data
public class cmc_operator implements EntityBase<cmc_operator> {
	private static final long serialVersionUID = 1L;

	@Id
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
}