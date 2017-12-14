/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  ICopyable.java   
 * @Package cn.com.higinet.tms.common.lang   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-9 15:05:30   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.lang;

/**
 * 实现浅克隆和深克隆的接口.
 *
 * @author: 王兴
 * @date:   2017-5-9 15:05:30
 * @since:  v4.3
 */
public interface Copyable<T> {
	
	/**
	 * 浅克隆.
	 *
	 * @return the object
	 */
	T copy();

	/**
	 * 深克隆.
	 *
	 * @return the object
	 */
	T deepCopy();
}