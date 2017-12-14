package cn.com.higinet.tms35.comm;

/**
 * 找不到交易数据
 * @author lining@higinet.com.cn
 * @version 4.0
 * @since 2014-8-5 上午10:39:16
 * @description
 */
public class TransNotFoundException extends tms_exception {

	private static final long serialVersionUID = 3127517261603362256L;

	public TransNotFoundException(Object... what) {
		super(what);
	}
}