package cn.com.higinet.tms.engine.comm;


public class TmsHttpException extends tms_exception {
	
	private static final long serialVersionUID = 7848765243553671067L;
	
	public String ecode;
	
	public TmsHttpException(Throwable cause) {
		super(cause);
		this.ecode = StaticParameters.TMS_HTTP_ERROR;
	}

	public TmsHttpException(String msg, Throwable cause) {
		super(msg, cause);
		this.ecode = StaticParameters.TMS_HTTP_ERROR;
	}
	

	public TmsHttpException(String errorCode) {
		this.ecode = errorCode;
	}

	public TmsHttpException(String errorCode, String errorMessage) {
		super(errorMessage);
		this.ecode = errorCode;
	}

	public TmsHttpException(String errorCode,  Exception e) {
		super(e);
		this.ecode = errorCode;
	}
	
	/**
	 * 获取错误代码
	 * @return
	 */
	public String getEcode() {
		return ecode;
	}

	/**
	 * 设置错误代码
	 * @param ecode
	 */
	public void setEcode(String ecode) {
		this.ecode = ecode;
	}
	
}
