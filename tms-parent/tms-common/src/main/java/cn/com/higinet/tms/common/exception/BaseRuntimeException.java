/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  BaseRuntimeException.java   
 * @Package cn.com.higinet.tms.common.exception   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-7 17:17:20   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.exception;
import org.apache.commons.lang3.exception.ContextedRuntimeException;

/**
 * 自定义异常类的基类，提供了统一的异常格式化输出.
 *
 * @author: 王兴
 * @date:   2017-5-7 17:17:20
 * @since:  v4.3
 * @see ContextedRuntimeException
 */
public class BaseRuntimeException extends ContextedRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** 异常id，可选。 */
	private String id;

	/** 异常详细描述。 */
	private String description;

	/**
	 * 构造一个新的 BaseRuntimeException 对象.
	 */
	public BaseRuntimeException() {
		super("");
	}

	/**
	 * 构造一个新的 BaseRuntimeException 对象.
	 *
	 * @param message 异常详细信息
	 */
	public BaseRuntimeException(String message) {
		super(message);
		this.description = message;
	}

	/**
	 * 根据一个Throwable对象构造一个新的BaseRuntimeException对象.
	 *
	 * @param ex Throwable对象或者其子类
	 */
	public BaseRuntimeException(Throwable ex) {
		super(ex);
	}

	/**
	 * 构造一个新的BaseRuntimeException对象.
	 *
	 * @param message 异常详细信息
	 * @param ex Throwable对象或者其子类
	 */
	public BaseRuntimeException(String message, Throwable ex) {
		super(message, ex);
		this.description = message;
	}

	/**
	 *构造一个新的 BaseRuntimeException 对象.
	 *详细信息可以根据string对象的格式化方法进行格式化
	 *
	 * @param message 异常详细信息的模板，可用string直接格式化
	 * @param args message里面需要格式化的参数
	 */
	public BaseRuntimeException(String message, Object... args) {
		this(String.format(message, args));
	}

	/**
	 * 构造一个新的BaseRuntimeException对象.
	 *
	 * @param message t异常详细信息的模板，可用string直接格式化
	 * @param e 实际发生的异常对象
	 * @param args message里面需要格式化的参数
	 */
	public BaseRuntimeException(String message, Exception e, Object... args) {
		this(String.format(message, args), e);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
