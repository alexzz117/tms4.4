package cn.com.higinet.tms35.service;

import java.util.Enumeration;
import java.util.Map;


/**
 * 
 * 请求对象接口
 * 
 * @author chenr, 2011-5-25
 * @version 2.1.0
 *
 */
public interface Request {

	/**
	 * 取得服务码
	 * @return
	 */
	public String getServiceCode();

	/**
	 * 取得交易码
	 * @return
	 */
	public String getTransactionCode();

	/**
	 * 取得包体类型
	 * @return
	 */
	public String getContentType();

	/**
	 * 取得包体长度
	 * @return
	 */
	public int getContentLength();

	/**
	 * 取得头参数值
	 * @param name
	 * @return
	 */
	public String getHead(String name);
	
	/**
	 * 取得头参数名称集
	 * @deprecated 放弃使用Enumeration类型 ，这个类型太旧
	 * @return
	 * @see Request#getHeadMap()
	 */
	public Enumeration<String> getHeadNames();
	
	/**
	 * 取得请求头参数集
	 * @return
	 */
	public Map<String, Object> getHeadMap();
	
	/**
	 * 取得请求参数
	 * @param name
	 * @return
	 */
	public Object getParameter(String name);

	/**
	 * 取得请求参数集
	 * @return
	 */
	public Map<String, Object> getParameterMap();
	
	/**
	 * 取得请求参数名称集
	 * @deprecated 放弃使用Enumeration类型 ，这个类型太旧
	 * @return
	 * @see Request#getParameterMap()
	 */
	public Enumeration<String> getParameterNames();
	
	
}
