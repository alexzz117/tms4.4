package cn.com.higinet.tms35.manage.aml.function;

/**
 * 处理函数接口
 * @author lining
 *
 */
public interface Function {
	/**
	 * 处理函数执行方法
	 * @param srcData	原数据值
	 * @param args		函数参数
	 * @return
	 */
	public Object execute(Object srcData, Object... args);
}