package cn.com.higinet.tms35.comm;

/**
 * 不算错误的异常
 * @author lining@higinet.com.cn
 * @version 4.0
 * @since 2014-9-3 下午5:46:56
 * @description
 */
public class NotErrorException extends tms_exception {

	private static final long serialVersionUID = 5400370822856658860L;

	public NotErrorException(Object... what) {
		super(what);
	}
}
