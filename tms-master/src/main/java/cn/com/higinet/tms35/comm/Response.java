package cn.com.higinet.tms35.comm;

import java.util.Map;


/**
 * 响应封装类
 * 
 * @author 杨文彦, 2011-03-14
 * @author chenr, 2011—05-25
 * 
 * @version 2.1.0
 * 
 */
public interface Response {

	/**
	 * 取得返回码
	 * @return
	 */
	public String getReturnCode();
	
	/**
	 * 设置返回码
	 * @param code
	 */
	public void setReturnCode(String code);

	/**
	 * 设置响应包体类型
	 * @param type
	 */
	public void setContentType(String type);

	/**
	 * 取得包体类型
	 * @return
	 */
	public String getContentType();

	/**
	 * 设置响应参数值
	 * @param name
	 * @param value
	 */
	public void setData(String name, Object value);
	
	/**
	 * 取得响应参数值
	 * @param name
	 * @return
	 */
	public Object getData(String name);
	
	/**
	 * 删除响应参数
	 * @param name
	 */
	public void removeData(String name);
	
	/**
	 * 获取响应数据集
	 * @return
	 */
	public Map<String, Object> getDataMap();
	
	/**
	 * 获取头参数集
	 * @return
	 */
	public Map<String, Object> getHeadMap();
	
}
