package cn.com.higinet.tms35.manage.exception;

import cn.com.higinet.rapid.web.exception.BaseWebException;

public class TmsMgrWebException extends BaseWebException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7443030120363517685L;
	public TmsMgrWebException(String msg) {
		super(msg);
	}
	public TmsMgrWebException(Throwable cause) {
		super(cause);
	}
	public TmsMgrWebException(String msg, Throwable cause) {
		super(msg, cause);
	}
}