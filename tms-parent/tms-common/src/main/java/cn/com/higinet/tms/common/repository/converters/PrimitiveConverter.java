package cn.com.higinet.tms.common.repository.converters;

import java.math.BigDecimal;
import java.math.BigInteger;

import cn.com.higinet.tms.common.exception.BaseRuntimeException;
import cn.com.higinet.tms.common.util.StringUtils;

public class PrimitiveConverter implements InnerConverter {

	public <T> T convert(Class<T> type, String value) throws ConvertValueException {
		if (type == null) {
			throw new BaseRuntimeException("Type is null.");
		}
		if (StringUtils.isNull(value)) {
			return null;
		}
		Object obj = null;
		try {
			if (type == String.class)
				obj = value;
			else if (type == Object.class)
				obj = value;
			else if (type == BigDecimal.class)
				obj = new BigDecimal(value);
			else if (type == BigInteger.class)
				obj = BigInteger.valueOf(Long.valueOf(value));
			else if (type == boolean.class || type == Boolean.class)
				obj = Boolean.valueOf(value);
			else if (type == char.class || type == Character.class)
				obj = new Character(value.charAt(0));
			else if (type == byte.class || type == Byte.class)
				obj = Byte.parseByte(value);
			else if (type == short.class || type == Short.class)
				obj = Short.parseShort(value);
			else if (type == int.class || type == Integer.class)
				obj = Integer.valueOf(value);
			else if (type == long.class || type == Long.class)
				obj = Long.valueOf(value);
			else if (type == float.class || type == Float.class)
				obj = Float.valueOf(value);
			else if (type == double.class || type == Double.class)
				obj = Double.valueOf(value);
		} catch (Exception e) {
			throw new ConvertValueException("Exception occurred when convert value type,target type is \"%s\",current value is \"%s\".", type.toString(), value);
		}
		if (obj == null) {
			throw new ConvertValueException("Type \"%s\" is undefined.", type.toString());
		}
		return (T) obj;
	}

}
