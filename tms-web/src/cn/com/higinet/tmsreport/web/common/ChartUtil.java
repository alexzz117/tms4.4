package cn.com.higinet.tmsreport.web.common;

import java.util.List;
import java.util.Map;

public interface ChartUtil {
	/**
	 * 根据相关参数拼装展示图形需要的字符串
	 * @param datalist  数据集
	 * @param info		拼装图形所需要的信息。如：图形横坐标表示的名称、从数据集中获取的字段名作为每一项的名、从数据集中获取的字段名作为每一项的值等
	 * @return
	 */
	public String joinDataStr(List<Map<String, Object>> datalist,Map<String,Object> info);
}
