/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  InnerConverter.java   
 * @Package cn.com.higinet.tms.common.repository.converters   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-22 15:17:52   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository.converters;

/**
 * 程序内部使用的转换器，只限common内部使用
 *
 * @ClassName:  InnerConverter
 * @author: 王兴
 * @date:   2017-9-22 15:17:52
 * @since:  v4.3
 */
public interface InnerConverter {
	public <T> T convert(Class<T> type, String value) throws ConvertValueException;
}
