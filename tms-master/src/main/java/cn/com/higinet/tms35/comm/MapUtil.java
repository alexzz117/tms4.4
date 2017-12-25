package cn.com.higinet.tms35.comm;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MapUtil {
	

	public static boolean objectIsEmpty(Map map,String key){
		if (map.isEmpty())
			return true;
		if (!map.containsKey(key))
			return true;
		if (map.get(key) == null)
			return true;
		return false;
	}
	
	public static String getString(Map map, String key) {
		if (map.isEmpty())
			return "";
		if (!map.containsKey(key))
			return "";
		if (map.get(key) == null)
			return "";
		return map.get(key).toString();
	}
	
	public static Object getObject(Map map,String key){
		if (map.isEmpty())
			return null;
		if (!map.containsKey(key))
			return null;
		if (map.get(key) == null)
			return null;
		return map.get(key);
	}
	
	public static float getFloat(Map map, String key) {
		if (map == null || map.isEmpty())
			return 0;
		if (!map.containsKey(key))
			return 0;
		if (map.get(key) == null)
			return 0;
		String val = String.valueOf(map.get(key));
		if(val.length() == 0) 
			return 0;
		return Float.parseFloat(val);
	}	
	
	public static int getInteger(Map map, String key) {
		if (map == null || map.isEmpty())
			return 0;
		if (!map.containsKey(key))
			return 0;
		if (map.get(key) == null)
			return 0;
		return  Integer.parseInt(map.get(key).toString());
	}
	
	public static Map<String, Object> getMap(Map map, String key) {
		if (map == null || map.isEmpty()||str_tool.is_empty(key))
			return null;
		if (!map.containsKey(key))
			return null;
		if (map.get(key) == null)
			return null;

		return (Map<String, Object>) map.get(key);
	}
	
	public static List<Map<String, Object>> getList(Map map, String key) {
		if (map == null || map.isEmpty()||str_tool.is_empty(key))
			return null;
		if (!map.containsKey(key))
			return null;
		if (map.get(key) == null)
			return null;

		return (List<Map<String, Object>>) map.get(key);
	}
	public static boolean getBoolean(Map map, String key) {
		if (map == null || map.isEmpty()||str_tool.is_empty(key))
			return false;
		if (!map.containsKey(key))
			return false;
		if (map.get(key) == null)
			return false;
		return  (Boolean)map.get(key);
	}

}
