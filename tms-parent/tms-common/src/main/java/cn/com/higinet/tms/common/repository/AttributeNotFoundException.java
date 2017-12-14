/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  StopServiceException.java   
 * @Package cn.com.higinet.tms.common.lifecycle   
 * @Description: 空字符异常
 * @author: 王兴
 * @date:   2017-5-7 17:32:46   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository;

import cn.com.higinet.tms.common.exception.BaseRuntimeException;

/**
 * 没有找到属性定义.
 *
 * @author: 王兴
 * @date:   2017-5-7 17:32:52
 * @since:  v4.3
 */
public class AttributeNotFoundException extends BaseRuntimeException {

	/**
	 * @see BaseRuntimeException#BaseRuntimeException(String)
	 */
	public AttributeNotFoundException(String msg) {
		super(msg);
	}

	/**
	 * @see BaseRuntimeException#BaseRuntimeException(Throwable)
	 */
	public AttributeNotFoundException(Throwable ex) {
		super(ex);
	}

	/**
	 * @see BaseRuntimeException#BaseRuntimeException(String,Throwable)
	 */
	public AttributeNotFoundException(String msg, Throwable ex) {
		super(msg, ex);
	}

	/**
	 * @see BaseRuntimeException#BaseRuntimeException(String,Object...)
	 */
	public AttributeNotFoundException(String msg, Object... args) {
		super(msg, args);
	}
}
