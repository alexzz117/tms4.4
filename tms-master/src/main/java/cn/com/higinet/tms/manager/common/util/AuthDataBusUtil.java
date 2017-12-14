package cn.com.higinet.tms.manager.common.util;

import java.util.HashMap;
import java.util.Map;


public class AuthDataBusUtil {
	private ThreadLocal threadConn = new ThreadLocal();
	private static AuthDataBusUtil instance = null;
	
	private AuthDataBusUtil(){
	}
	synchronized public static void initDataBusUtil(){
		if (null == instance) {
			instance = new AuthDataBusUtil();
		}
	}	
	
	public static void openDataBus(){
		if(instance==null){
			initDataBusUtil();
		}
		Map map = new HashMap();
		instance.threadConn.set(map);
	}
	
	public static void put(String id,Object obj){
		if(instance==null){
			initDataBusUtil();
		}
		Map map = (Map)instance.threadConn.get();
		map.put(id, obj);
	}
	
	public static Object get(String id){
		if(instance==null){
			initDataBusUtil();
		}
		Map map = (Map)instance.threadConn.get();
		return CmcMapUtil.getObject(map, id);
	}
	
	public static void remove(String id){
		if(instance==null){
			initDataBusUtil();
		}
		Map map = (Map)instance.threadConn.get();
		map.remove(id);
	}
	
	public static void closeDataBus(){
		if(instance==null){
			initDataBusUtil();
		}
		instance.threadConn.remove();
	}
	
}
