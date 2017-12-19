/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  PersistenceConverter.java   
 * @Package cn.com.higinet.tms.common.repository.converters   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-22 16:19:51   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository.converters;

import java.math.BigDecimal;

import cn.com.higinet.tms.common.exception.BaseRuntimeException;
import cn.com.higinet.tms.common.util.StringUtils;

/**
 * 持久化对象的类型转换
 *
 * @ClassName:  PersistenceConverter
 * @author: 王兴
 * @date:   2017-9-22 16:19:51
 * @since:  v4.3
 */
public class PersistenceConverter implements InnerConverter {

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.repository.converters.InnerConverter#convert(java.lang.Class, java.lang.String)
	 */
	public <T> T convert(Class<T> type, String value) throws ConvertValueException {
		if (type == null) {
			throw new BaseRuntimeException("Type is null.");
		}
		if (StringUtils.isNull(value)) {
			return null;
		}
		Object obj = null;
		try {
			if (type == String.class) {
				obj = value;
			} else if (type == java.sql.Date.class) {
				obj = java.sql.Date.valueOf(value); //必须满足格式yyyy-MM-dd，否则自定义转换器
			} else if (type == java.sql.Time.class) {
				obj = java.sql.Time.valueOf(value); //必须满足格式hh:mm:ss，否则自定义转换器 
			} else if (type == java.sql.Timestamp.class) {
				obj = java.sql.Timestamp.valueOf(value);
			} else if (type == boolean.class) {
				obj = Boolean.valueOf(value).booleanValue();
			} else if (type == byte.class) {
				obj = Byte.valueOf(value).byteValue();
			} else if (type == short.class) {
				obj = Short.valueOf(value).shortValue();
			} else if (type == int.class) {
				obj = Integer.valueOf(value).intValue();
			} else if (type == long.class) {
				obj = Long.valueOf(value).longValue();
			} else if (type == float.class) {
				obj = Float.valueOf(value).floatValue();
			} else if (type == double.class) {
				obj = Double.valueOf(value).doubleValue();
			} else if (type == byte[].class) {
				obj = value.getBytes();
			} else if (type == java.math.BigDecimal.class) {
				obj = new BigDecimal(value);
			}
		} catch (Exception e) {
			throw new ConvertValueException("Exception occurred when convert value type,target type is \"%s\",current value is \"%s\".", type.toString(), value);
		}
		if (obj == null) {
			throw new ConvertValueException("Type \"%s\" is undefined.", type.toString());
		}
		return (T) obj;
	}

}
