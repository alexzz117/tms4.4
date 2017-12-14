package cn.com.higinet.tms.manager.common.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author yanghui
 * @date 2011-2-28
 * 
 * @version 1.0
 */
public class CmcMapUtil {
	public static boolean isContainsKey(Map map, Object key) {
		return map != null && map.containsKey(key);
	}

	public static boolean isContainsObject(Map map, Object value) {
		return map.containsValue(value);
	}
	
	public static Object getObject(Map map, String key) {
		if (map == null || map.isEmpty()||CmcStringUtil.isEmpty(key))
			return null;
		if (!map.containsKey(key))
			return null;
		if (map.get(key) == null)
			return null;
		return map.get(key);
	}

	public static String getString(Map map, String key) {
		if (map == null || map.isEmpty()||CmcStringUtil.isEmpty(key))
			return "";
		if (!map.containsKey(key))
			return "";
		if (map.get(key) == null)
			return "";
		return map.get(key).toString();
	}
	//判断协议中是否存在key
	public static boolean isContainsKeyByProtocol(Map map, String key) {
		if (map == null || map.isEmpty()||CmcStringUtil.isEmpty(key))
			return false;
		String[] arr = key.split("\\.");
		//if (arr.length != 2) return "";
		if (arr.length != 2){
			return isContainsKey ( map,  key);
		}
		if (!isContainsKey(map, arr[0]))
			return false;
		Object obj = map.get(arr[0]);
		if (obj == null)
			return false;
		if (!(obj instanceof Map)) return false;
		Map temp = (Map)obj;
		return isContainsKey ( temp,  arr[1]);
	}
	
	// 扩展，如果key没有带.则直接取
	// maxiao 2011-10-21
	public static String getStringByProtocol(Map map, String key) {
		if (map == null || map.isEmpty()||CmcStringUtil.isEmpty(key))
			return "";
		String[] arr = key.split("\\.");
		//if (arr.length != 2) return "";
		if (arr.length != 2){
			return getString( map,  key);
		}
		if (!map.containsKey(arr[0]))
			return "";
		if (map.get(arr[0]) == null)
			return "";
		if (!(map.get(arr[0]) instanceof Map)) return "";
		Map temp = (Map)map.get(arr[0]);
		if (!temp.containsKey(arr[1]))
			return "";
		if (temp.get(arr[1]) == null) 
			return "";
		return temp.get(arr[1]).toString();
	}

	// 扩展，如果key没有带.则直接放
	// maxiao 2011-10-21
	public static void setStringByProtocol(Map map, String key, String value) {
		if (map == null || map.isEmpty()||CmcStringUtil.isEmpty(key))
			return ;
		String[] arr = key.split("\\.");
		//if (arr.length != 2) return ;
		if(key.indexOf(".") != -1){
			
			if (!map.containsKey(arr[0]))
				return ;
			if (map.get(arr[0]) == null)
				return ;
			if (!(map.get(arr[0]) instanceof Map)) return ;
			Map temp = (Map)map.get(arr[0]);
			if (temp == null) {
				temp = new HashMap();
				map.put(arr[0], temp);
			}
	
			temp.put(arr[1], value);
		}
		else {
			map.put(key, value);
		}
	}
	
	// 和上面方法的不同之处在于遇到空不返回，继续插入
	public static void setStringByProtocolIgnoreNulls(Map map, String key, String value) {
		if (map == null ||CmcStringUtil.isEmpty(key))
			return ;
		String[] arr = key.split("\\.");

		if ((!map.containsKey(arr[0])
				||map.get(arr[0]) == null)
				||!(map.get(arr[0]) instanceof Map)){
			map.put(arr[0], new HashMap());
		}
		
		if(key.indexOf(".") != -1){
			
			Map temp = (Map)map.get(arr[0]);
			if(temp == null){
				temp = new HashMap();
				map.put(arr[0], temp);
			}
			temp.put(arr[1], value);
		} 
		else {
			map.put(key, value);
		}
	}

	public static int getInteger(Map map, String key) {
		if (map == null || map.isEmpty()||CmcStringUtil.isEmpty(key))
			return 0;
		if (!map.containsKey(key))
			return 0;
		if (map.get(key) == null)
			return 0;
		return  Integer.parseInt(map.get(key).toString());
	}

	public static boolean getBoolean(Map map, String key) {
		if (map == null || map.isEmpty()||CmcStringUtil.isEmpty(key))
			return false;
		if (!map.containsKey(key))
			return false;
		if (map.get(key) == null)
			return false;
		return  (Boolean)map.get(key);
	}
	
	public static int getDecimalInt(Map map,String key){
		if (map == null || map.isEmpty()||CmcStringUtil.isEmpty(key))
			return 0;
		if (!map.containsKey(key))
			return 0;
		if (map.get(key) == null)
			return 0;
		BigDecimal decimalValue = (BigDecimal) map.get(key);		

		return decimalValue.intValue();
	}
	
	public static long getDecimalLong(Map map,String key){
		if (map == null || map.isEmpty()||CmcStringUtil.isEmpty(key))
			return 0;
		if (!map.containsKey(key))
			return 0;
		if (map.get(key) == null)
			return 0;
		BigDecimal decimalValue = (BigDecimal) map.get(key);		

		return decimalValue.longValue();
	}
	public static long getLong(Map map,String key){
		if (map == null || map.isEmpty()||CmcStringUtil.isEmpty(key))
			return 0;
		if (!map.containsKey(key))
			return 0;
		if (map.get(key) == null)
			return 0;
		
		return Long.parseLong(map.get(key).toString());
	}
	
	public static double getDecimalDouble(Map map,String key){
		if (map == null || map.isEmpty()||CmcStringUtil.isEmpty(key))
			return 0;
		if (!map.containsKey(key))
			return 0;
		if (map.get(key) == null)
			return 0;
		BigDecimal decimalValue = (BigDecimal) map.get(key);		

		return decimalValue.doubleValue();
	}

	public static double getDouble(Map map, String key) {
		if (map == null || map.isEmpty()||CmcStringUtil.isEmpty(key))
			return 0;
		if (!map.containsKey(key))
			return 0;
		if (map.get(key) == null)
			return 0;
		return (Double)map.get(key);
	}	
	
	public static List<Map<String, Object>> getList(Map map, String key) {
		if (map == null || map.isEmpty()||CmcStringUtil.isEmpty(key))
			return null;
		if (!map.containsKey(key))
			return null;
		if (map.get(key) == null)
			return null;

		return (List<Map<String, Object>>) map.get(key);
	}

	public static Map<String, Object> getMap(Map map, String key) {
		if (map == null || map.isEmpty()||CmcStringUtil.isEmpty(key))
			return null;
		if (!map.containsKey(key))
			return null;
		if (map.get(key) == null)
			return null;

		return (Map<String, Object>) map.get(key);
	}

	public static boolean isEmptyMap(Map map) {
		return map.isEmpty();
	}

	public static boolean isEmpty(Map<?, ?> map) {
		return null == map || map.size() == 0;
	}

	public static Map paseOneMapToOther(Map oldMap, Map newMap) {
		newMap.putAll(oldMap);
		return newMap;
	}
	
	/**
	 * sql占位符为null的情况.  by yanghui
	 * @param row
	 * @param fields
	 * @return
	 */
	public static Map<String, Object> fullRow(Map<String, Object> row, String[] fields) {
		for (int i = 0; i < fields.length; i++) {
			String column = fields[i];
			if (isContainsKey(row, column) && getObject(row, column) != null)  continue;
			
			row.put(column, null);
		}
		return row;
	}
}
