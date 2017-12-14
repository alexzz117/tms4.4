/**  
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  JmxException.java   
 * @Package cn.com.higinet.tms.common.exception   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @author: Sanji
 * @date:   2017年5月27日 上午11:01:47   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 * 
 **/
package cn.com.higinet.tms.common.exception;

/**  
 * @ClassName:  JmxException   
 * @Description:jmx访问异常   
 * @author: Sanji
 * @date:   2017年5月27日 上午11:01:47   
 * 
 **/
public class JmxException extends BaseRuntimeException {

	/**
	 * 构造一个新的 jmx exception 对象.
	 *
	 * @param msg the msg
	 */
	public JmxException(String msg) {
		super(msg);
	}
	
	/**
	 * 构造一个新的 jmx exception 对象.
	 *
	 * @param ex the ex
	 */
	public JmxException(Throwable ex) {
		super(ex);
	}
	
	/**
	 * 构造一个新的 jmx exception 对象.
	 *
	 * @param msg the msg
	 * @param ex the ex
	 */
	public JmxException(String msg, Throwable ex) {
		super(msg, ex);
	}

	/**
	 * 构造一个新的 jmx exception 对象.
	 *
	 * @param msg the msg
	 * @param args the args
	 */
	public JmxException(String msg, Object... args) {
		super(msg, args);
	}
}
