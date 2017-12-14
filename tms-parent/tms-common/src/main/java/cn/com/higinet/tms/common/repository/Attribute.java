/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  AttributeDefinition.java   
 * @Package cn.com.higinet.tms.common.repository   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-20 11:26:20   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository;

import java.util.Map;

/**
 * 属性定义类
 * 注：所有属性的原始类型都是String
 * 
 * @ClassName:  AttributeDefinition
 * @author: 王兴
 * @date:   2017-9-20 11:26:20
 * @since:  v4.3
 */
public class Attribute {

	/** 是否主键. */
	private boolean primary;

	/** 内部使用时候的名字 */
	private String name;

	/** 描述. */
	private String description;

	/** 对外类型，调用结构化数据方法获取属性值时候，会根据此类型转换成具体的类型。此类型是最终外部使用的类型. */
	private Class<?> outerType;

	/** 对外类型处理的转换器，如果有的话，如果没有定义，则使用内部默认转换器。转换器转换后的类型一定是outerType对应的类型 */
	private String outerConverter;

	/** 持久化时候的名字，相当于关系型数据库中的字段名. */
	private String persistenceName;

	/** 持久化时候的类型，持久化的时候会转换成此类型。请参考{@link java.sql.Types}，此类型是最终存储在持久化中的类型 */
	private int persistedType;

	/** 持久化时候调用的数据转换器名，转换器通过注入实现，如果有自定义转换器则使用自定义的转换器，如果没有则使用默认转换器。转换器转换后的最终类型就是persistedType定义的类型 */
	private String persistConverter;

	/** 是否能被持久化，false的时候，此字段值不参与持久化操作，默认为true. */
	private boolean persistable = true;

	/** 限制长度，同时可以作为持久化长度使用，可以用作校验. */
	private int limitedLength;

	/** 是否可为空，可用作持久化前的校验. */
	private boolean nullable;

	/** 如果此值是从外部接收，此值表示源属性名字 ，接收过来的值均为string类型*/
	private String srcName;

	/** 接收数据的时候，当值为null时候的默认值. */
	private String defaultValue;

	/** 附加信息，可用于扩展，但是系统内部不会使用。属于业务自定义部分。 */
	private Map<String, String> addons;

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public String getPersistenceName() {
		return persistenceName;
	}

	public void setPersistenceName(String persistenceName) {
		this.persistenceName = persistenceName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Class<?> getOuterType() {
		return outerType;
	}

	public void setOuterType(Class<?> outerType) {
		this.outerType = outerType;
	}

	public int getPersistedType() {
		return persistedType;
	}

	public void setPersistedType(int persistedType) {
		this.persistedType = persistedType;
	}

	public String getOuterConverter() {
		return outerConverter;
	}

	public void setOuterConverter(String outerConverter) {
		this.outerConverter = outerConverter;
	}

	public String getPersistConverter() {
		return persistConverter;
	}

	public void setPersistConverter(String persistConverter) {
		this.persistConverter = persistConverter;
	}

	public int getLimitedLength() {
		return limitedLength;
	}

	public void setLimitedLength(int limitedLength) {
		this.limitedLength = limitedLength;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public boolean isPersistable() {
		return persistable;
	}

	public void setPersistable(boolean persistable) {
		this.persistable = persistable;
	}

	public String getSrcName() {
		return srcName;
	}

	public void setSrcName(String srcName) {
		this.srcName = srcName;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Map<String, String> getAddons() {
		return addons;
	}

	public void setAddons(Map<String, String> addons) {
		this.addons = addons;
	}

}
