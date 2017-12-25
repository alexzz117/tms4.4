package cn.com.higinet.tms.engine.comm;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 平台标准数据结构基类
 * 
 * @author 杨文彦 创建日期：2011-03-14
 * @version 2011-04-21 删除设置读取属性方法（setAttribute和getAttribute）
 * @version 2011-05-24 HashMap->LinkedHashMap by chenr
 */
public class Message {

	/**
	 * 消息体长度
	 */
	public static final String HEAD_CONTENT_LENGTH = "ContentLength";
	/**
	 * 服务号
	 */
	public static final String HEAD_SERVICE_CODE = "ServiceCode";
	/**
	 * 交易号
	 */
	public static final String HEAD_TRANSACTION_CODE = "TransactionCode";
	/**
	 * 消息体类型
	 */
	public static final String HEAD_CONTENT_TAYPE = "ContentType";
	/**
	 * 返回码
	 */
	public static final String HEAD_RETURN_CODE = "ReturnCode";
	
	/**
	 * XML类型
	 */
	public static final String CONTENT_TYPE_XML = "XML";
	/**
	 * GoogleProtolBuffer类型
	 */
	public static final String CONTENT_TYPE_GPB = "GPB";
	/**
	 * JSON类型
	 */
	public static final String CONTENT_TYPE_JSON = "JSON";
	
	/**
	 * 默认头模板
	 */
	protected static final Map<String, Object> HEAD_TEMPLATE = new LinkedHashMap<String, Object>();
	
	static{
		HEAD_TEMPLATE.put(HEAD_CONTENT_LENGTH, "0");
		HEAD_TEMPLATE.put(HEAD_SERVICE_CODE, "0");
		HEAD_TEMPLATE.put(HEAD_TRANSACTION_CODE, "0");
		HEAD_TEMPLATE.put(HEAD_CONTENT_TAYPE, "0");
		HEAD_TEMPLATE.put(HEAD_RETURN_CODE, "0");
	}
	
	/**
	 * 响应头部数据保存对象
	 */
	private Map<String, Object> head = new LinkedHashMap<String, Object>();

	/**
	 * 响应内容数据保存对象
	 */
	private Map<String, Object> data = new LinkedHashMap<String, Object>();

	public Message(){
		head.putAll(HEAD_TEMPLATE);
	}
	
	/**
	 * 获取消息头对象
	 * @return
	 */
	public Map<String, Object> getHead() {
		return head;
	}

	/**
	 * 获取消息体对象
	 * @return
	 */
	public Map<String, Object> getData() {
		return data;
	}

	/**
	 * 设置头参数值
	 * @param name
	 * @param value
	 */
	public void setHead(String name, Object value) {
		head.put(name, value);
	}

	/**
	 * 获取头参数值
	 * @param name
	 * @return
	 */
	public Object getHead(String name) {
		return head.get(name);
	}

	/**
	 * 删除头参数
	 * @param name
	 */
	public void removeHead(String name) {
		head.remove(name);
	}
	
	/**
	 * 设置头参数值
	 * @param name
	 * @param value
	 */
	public void setData(String name, Object value) {
		data.put(name, value);
	}

	/**
	 * 获取消息体参数值
	 * @param name
	 * @return
	 */
	public Object getData(String name) {
		return data.get(name);
	}

	/**
	 * 删除消息体参数
	 * @param name
	 */
	public void removeData(String name) {
		data.remove(name);
	}

//	public void setParameter(String name, Object value) {
//		data.put(name, value);
//	}
//
//	public Object getParameter(String name) {
//		return data.get(name);
//	}
//
//	public void removeParameter(String name) {
//		data.remove(name);
//	}

	// 应该有getHeadNames 和getParameterNames 方法
}
