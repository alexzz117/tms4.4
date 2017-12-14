package cn.com.higinet.tms.manager.modules.exception;

import cn.com.higinet.tms.manager.common.BaseServiceException;

/**
 * 后台管理,service异常
 * @author yangk
 */
public class TmsMgrServiceException extends BaseServiceException {

	private static final long serialVersionUID = -6796851981284901237L;

	public TmsMgrServiceException(String msg) {
		super(msg);
	}

	public TmsMgrServiceException(Throwable cause) {
		super(cause);
	}

	public TmsMgrServiceException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
