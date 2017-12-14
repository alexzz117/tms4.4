/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  Converter.java   
 * @Package cn.com.higinet.tms.common.repository   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-20 17:58:17   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository.converters;

import cn.com.higinet.tms.common.repository.Attribute;

/**
 * 结构化数据的类型转换器，可以实现自定义的类型转换
 *
 * @ClassName:  Converter
 * @author: 王兴
 * @date:   2017-9-20 17:58:17
 * @since:  v4.3
 */
public interface Converter<T> {
	
	/**
	 * Convert.
	 *
	 * @param value 属性值
	 * @param attr 属性定义
	 * @return the t
	 * @throws ConvertValueException the convert value exception
	 */
	public T convert(String value, Attribute attr) throws ConvertValueException;
}
