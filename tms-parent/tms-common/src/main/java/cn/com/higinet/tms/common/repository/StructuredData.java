/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  StructuredData.java   
 * @Package cn.com.higinet.tms.common.repository   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-20 9:48:58   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository;

import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.kryo.serializers.DefaultSerializers.StringSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer.Bind;
import com.esotericsoftware.kryo.serializers.MapSerializer.BindMap;

import cn.com.higinet.tms.common.repository.converters.Converter;
import cn.com.higinet.tms.common.repository.converters.ConverterNotFoundException;
import cn.com.higinet.tms.common.repository.converters.InnerConverter;
import cn.com.higinet.tms.common.repository.converters.PersistenceConverter;
import cn.com.higinet.tms.common.repository.converters.PrimitiveConverter;
import cn.com.higinet.tms.common.util.StringUtils;
import cn.com.higinet.tms.common.util.TypesUtils;

/**
 * 结构化数据对象，对象的属性有内部类型与外部类型之分，内部类型是指存储、更新、删除等程序内部用到的属性类型。
 * 外部类型是指业务程序使用时候的类型，外部类型需要根据元数据定义进行类型转换。
 *
 * @ClassName:  StructuredData
 * @author: 王兴
 * @date:   2017-9-20 9:48:58
 * @since:  v4.3
 */
public class StructuredData {

	/** 基础类型转换器. */
	private static final transient InnerConverter primitiveConverter = new PrimitiveConverter();

	/** 常量 persistenceConverter. */
	private static final transient InnerConverter persistenceConverter = new PersistenceConverter();

	/** 主键值. */
	@Bind(StringSerializer.class)
	private String key;

	/** 本地数据. */
	@BindMap(valueSerializer = StringSerializer.class, keySerializer = StringSerializer.class, valueClass = String.class, keyClass = String.class, keysCanBeNull = false)
	private Map<String, String> data = new HashMap<String, String>();

	/** 元数据. */
	private transient Metadata metadata;

	/** 转换器. */
	private transient Map<String, Converter<?>> converters;

	private transient DataRepository repository;

	/**
	 * 构造一个新的 structured data 对象.
	 */
	public StructuredData() {
	}

	/**
	 * 构造一个新的 structured data 对象.
	 *
	 * @param key the key
	 * @param metadata the metadata
	 */
	public StructuredData(String key, Metadata metadata) {
		this.key = key;
		this.metadata = metadata;
		try {
			setData(getPrimaryAttribute(), key);
		} catch (Exception e) {
			//ignore
		}
	}

	public Map<String, Converter<?>> getConverters() {
		return converters;
	}

	public void setConverters(Map<String, Converter<?>> converters) {
		this.converters = converters;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

	public DataRepository getRepository() {
		return repository;
	}

	public void setRepository(DataRepository repository) {
		this.repository = repository;
	}

	/**
	 * 返回主键定义
	 *
	 * @return primary attribute 属性
	 */
	public Attribute getPrimaryAttribute() {
		return metadata.getPrimaryAttribute();
	}

	/**
	 * 返回主键的外部类型对象
	 *
	 * @return primary value 属性
	 * @throws Exception the exception
	 */
	public Object getPrimaryValue() throws Exception {
		return getValue(getPrimaryAttribute().getName());
	}

	/**
	 * 返回主键的持久化类型对象
	 *
	 * @return primary value for persist 属性
	 * @throws Exception the exception
	 */
	public Object getPrimaryValueForPersist() throws Exception {
		return getValueForPersist(getPrimaryAttribute().getName());
	}

	public void setData(Attribute attr, String value) throws Exception {
		setData(attr.getName(), value);
	}

	/**
	 * 进来的data全部都是string类型
	 *
	 * @param field the field
	 * @param value the value
	 */
	public void setData(String field, String value) throws Exception {
		Attribute attr = metadata.getAttribute(field);
		if (attr == null) {
			throw new AttributeNotFoundException("Attribute \"%s\" of structure \"%s\" is undefined.", field, metadata.getStructureName());
		}
		boolean isNull = StringUtils.isNull(value);
		if (isNull && !attr.isNullable()) {
			throw new EmptyValueException("Value of attribute \"%s\" can not be empty.", attr.getName());
		}
		if (isNull) {
			value = attr.getDefaultValue();
		}
		int limitedLength = attr.getLimitedLength();
		if (limitedLength > 0 && limitedLength < value.length()) {
			throw new OverLengthException("Value of attribute \"%s\" out of length ,limited length is \"%d\",current length is \"%d\".", attr.getName(), attr.getLimitedLength(), value.length());
		}
		data.put(field, value);
	}

	/**
	 * 返回 string value 属性.
	 *
	 * @param field the field
	 * @return string value 属性
	 */
	public String getStringValue(String field) {
		return data.get(field);
	}

	/**
	 * 转换成对外类型
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param field the field
	 * @return value 属性
	 * @throws Exception the exception
	 */
	public <T> T getValue(Class<T> type, String field) throws Exception {
		Attribute attr = getAttribute(field);
		String conv = attr.getOuterConverter();
		return getValue(type, attr, conv, primitiveConverter);
	}

	/**
	 * 出去的data需要根据转换器来转型
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param field the field
	 * @return value 属性
	 */
	private <T> T getValue(Class<T> type, Attribute attr, String converter, InnerConverter innerConverter) throws Exception {
		String res = data.get(attr.getName());
		if (StringUtils.isNull(res) && StringUtils.isNull(res = attr.getDefaultValue())) {//给res赋默认值
			return null;
		}
		if (StringUtils.isNull(converter)) {//如果配置的转换器为空，则使用默认的原始类型转换器
			return innerConverter.convert(type, res);
		} else {
			if (converters == null) {
				throw new ConverterNotFoundException("Can't find converter \"%s\" which is defined in attribute \"%s\" of structure \"%s\"", converter, attr.getName(), metadata.getStructureName());
			}
			Converter<?> _converter = converters.get(converter);
			if (_converter == null) {
				throw new ConverterNotFoundException("Can't find converter \"%s\" which is defined in attribute \"%s\" of structure \"%s\"", converter, attr.getName(), metadata.getStructureName());
			}
			return (T) _converter.convert(res, attr);
		}
	}

	/**
	 * 根据属性的outerType类型返回 value 属性.
	 *
	 * @param field the field
	 * @return value 属性
	 * @throws Exception the exception
	 */
	public Object getValue(String field) throws Exception {
		return getValue(getAttribute(field).getOuterType(), field);
	}

	/**
	 * 返回 attribute 属性.
	 *
	 * @param field the field
	 * @return attribute 属性
	 */
	private Attribute getAttribute(String field) {
		Attribute attr = metadata.getAttribute(field);
		if (attr == null) {
			throw new AttributeNotFoundException("Attribute \"%s\" of structure \"%s\" is undefined.", field, metadata.getStructureName());
		}
		return attr;
	}

	/**
	 * 返回持久化的类型，这个要根据db的字段类型去转型
	 *
	 * @param field the field
	 * @return value for persist 属性
	 */
	public Object getValueForPersist(String field) throws Exception {
		Attribute attr = getAttribute(field);
		String conv = attr.getOuterConverter();
		return getValue(TypesUtils.getJavaType(attr.getPersistedType()), attr, conv, persistenceConverter);
	}

}
