/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  KV.java   
 * @Package cn.com.higinet.tms.common.cache   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-7-24 17:37:48   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.cache;

import com.google.common.base.Objects;

import cn.com.higinet.tms.common.exception.UnsupportedParameterException;
import cn.com.higinet.tms.common.exception.UnsupportedTypeException;
import cn.com.higinet.tms.common.util.StringUtils;

/**
 * 缓存存储的基础对象.
 *
 * @ClassName: KV
 * @author: 王兴
 * @date: 2017-7-24 17:37:48
 * @since: v4.3
 */
public class KV {

	/**系统默认分组. */
	private static final String DEFAULT_GROUP = "default_";

	/** 值分组，可以用于向redis的key添加前缀，或者是向AS里面添加set. */
	private String group;

	/** 主键. */
	private String key;

	/** 值. */
	private byte[] value;

	/** 过期时间是否采用TTL（time to live）模式，如果是TTL的话，expireTimestamp表示存在的时长，单位是秒 */
	private boolean useTTL = true;

	/** 过期时间，值应该是一个时间撮，设置当前KV对象在缓存中的过期时间，支持对key设置过期时间的缓存有效. 
	 *  如果useTTL是true（默认），则单位是秒
	 *  如果useTTL是false，那么这个值代表KV对象过期的时间戳
	 */
	private long expireTimestamp;

	/**
	 * 构造一个新的 kv 对象.
	 *
	 * @param key the key
	 */
	public KV(String key) {
		setGroup(null);
		setKey(key);
	}

	/**
	 * 构造一个新的 kv 对象.
	 *
	 * @param group the group
	 * @param key the key
	 * @param value the value
	 */
	public KV(String group, String key, Object value) {
		setGroup(group);
		setKey(key);
		setValue(value);
	}

	/**
	 * 构造一个新的 kv 对象.
	 *
	 * @param group the group
	 * @param key the key
	 */
	public KV(String group, String key) {
		setGroup(group);
		setKey(key);
	}

	public long getExpireTimestamp() {
		return expireTimestamp;
	}

	public void setExpireTimestamp(long expireTimestamp) {
		this.expireTimestamp = expireTimestamp;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		if (StringUtils.notNull(group)) {
			this.group = group;
		} else {
			this.group = DEFAULT_GROUP;
		}
	}

	public String getKey() {
		return key;
	}

	private void setKey(String key) {
		if (StringUtils.isNull(key)) {
			throw new UnsupportedParameterException("Key can't be null.");
		}
		this.key = key;
	}

	public byte[] getValue() {
		return value;
	}

	/**
	 * 将value转换成String对象返回.
	 *
	 * @return string 属性
	 */
	public String getString() {
		if (value != null) {
			return new String(value);
		} else {
			return null;
		}
	}

	/**
	 * 设置 value 属性，目前只支持基础类型的封装和String类型，自定义复杂类型需要开发人员自己定义序列化，以byte数组的方式传入。
	 * 基础类型的封装类，都转换成String过后getBytes，不会每个类型单独转bytes，因此在转换的时候，要根据String转换成其他类型。
	 *
	 * @param value the value
	 */
	public void setValue(Object _value) {
		if (_value != null) {
			if (_value instanceof byte[]) {
				value = (byte[]) _value;
			} else if (_value instanceof String) {
				value = _value.toString().getBytes();
			} else if (_value instanceof Integer) {
				value = _value.toString().getBytes();
			} else if (_value instanceof Double) {
				value = _value.toString().getBytes();
			} else if (_value instanceof Float) {
				value = _value.toString().getBytes();
			} else if (_value instanceof Boolean) {
				value = _value.toString().getBytes();
			} else if (_value instanceof Long) {
				value = _value.toString().getBytes();
			} else if (_value instanceof Short) {
				value = _value.toString().getBytes();
			} else if (_value instanceof Byte) {
				value = _value.toString().getBytes();
			} else if (_value instanceof Character) {
				value = _value.toString().getBytes();
			} else {
				throw new UnsupportedTypeException(_value.getClass().getCanonicalName());
			}
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && (obj instanceof KV)) {
			KV kv = (KV) obj;
			return _equals(group, kv.group) && _equals(key, kv.key);
		}
		return false;
	}

	/**
	 * Equals.
	 *
	 * @param obj the obj
	 * @param target the target
	 * @return true, if successful
	 */
	private boolean _equals(Object obj, Object target) {
		if (obj != null && obj.equals(target)) {
			return true;
		} else if (obj == null && target == null) {
			return true;
		}
		return false;
	}

	public boolean isUseTTL() {
		return useTTL;
	}

	public void setUseTTL(boolean useTTL) {
		this.useTTL = useTTL;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(group, key);
	}
}
