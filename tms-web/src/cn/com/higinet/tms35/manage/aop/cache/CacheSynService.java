package cn.com.higinet.tms35.manage.aop.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import cn.com.higinet.cmc.util.StringUtil;
import cn.com.higinet.tms35.manage.auth.exception.TmsMgrAuthRefreshCacheException;
import cn.com.higinet.tms35.manage.common.SocketClient;
import cn.com.higinet.tms35.manage.common.StaticParameters;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
/**
 * 缓存同步service ，利用socket同步
 * @author yanghui
 * @date 2011-7-26
 * 
 * @version 1.0
 */
public class CacheSynService {
	/**
	 * 生成缓存同步所需的socket消息
	 * @param arg
	 * @return
	 */
	public static Map<String, byte[]> getMessage(String refreshMsg) {
		Map<String, byte[]> result = new HashMap<String, byte[]>();
		try {
			StringBuffer body = new StringBuffer();
			body.append(getXmlHead(null));
			body.append(StringUtil.appendXmlMessage("tableName", refreshMsg));
			body.append("</Message>");
			
			byte[] b = StaticParameters.MSG_HEADER;
			byte[] ws = body.toString().getBytes("UTF-8");

			String len = String.valueOf(ws.length);
			len = "00000000".substring(len.length()) + len;
			System.arraycopy(len.getBytes(), 0, b, 0, 8);
			result.put("b", b);
			result.put("ws", ws);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("未能成功生成缓存刷新请求报文");
		}
	}
	
	/**
	 * 生成删除用户缓存所需的socket消息
	 * @param arg
	 * @return
	 */
	public static Map<String, byte[]> getUserAcheMessage(String refreshMsg) {
		Map<String, byte[]> result = new HashMap<String, byte[]>();
		try {
			StringBuffer body = new StringBuffer();
			body.append(getXmlHead(null));
			body.append(StringUtil.appendXmlMessage("RateMessage",refreshMsg));
			body.append("</Message>");
			
			byte[] b = StaticParameters.MSG_DELETE_CACHE_HEADER;
			byte[] ws = body.toString().getBytes("UTF-8");

			String len = String.valueOf(ws.length);
			len = "00000000".substring(len.length()) + len;
			System.arraycopy(len.getBytes(), 0, b, 0, 8);
			result.put("b", b);
			result.put("ws", ws);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("未能成功生成缓存刷新请求报文");
		}
	}
	
	
	/**
	 * 名单生成缓存同步所需的xml报文数据
	 * @param arg
	 * @return
	 */
	public static List<Map<String, Object>> getRosterMessage(List<Map<String, Object>> rosterList) {
		if (rosterList == null || rosterList.isEmpty())
			return null;
		int maxItem = 274, maxMsg = 3 * 1024;
		int size = maxMsg / maxItem;// 当前报文可以接受多少个map
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();//xml报文数据的集合
		Set<Map<String, Object>> authSet = new HashSet<Map<String,Object>>();//更新授权信息用的集合
		try {
			StringBuffer body = new StringBuffer(maxMsg);
			body.append(getXmlHead(null));
			body.append(StringUtil.appendXmlMessage("tableName","TMS_MGR_ROSTER"));
			StringBuilder tmp = new StringBuilder(maxMsg);
			body.append("<rosters type='list'>");
			for (int i = 0, l = rosterList.size(); i < l; i++) {
				Map<String, Object> map = rosterList.get(i);
				authSet.add((Map<String, Object>) map.get("authInfo"));
				if (i > 0 && i % size == 0) {//达到 size个map，组成一个xml
					tmp.append("</rosters>").append("</Message>");
					String msg = body.toString() + tmp.toString();
					Map<String, Object> m = new HashMap<String, Object>();
					m.put("msgBody", msg);
					m.put("authInfos", authSet);
					list.add(m);
					
					authSet.clear();
					tmp.setLength(0);
				}
				tmp.append("<roster type='map'>");
				tmp.append(StringUtil.appendXmlMessage("rosterId",map.get("id")));
				tmp.append(StringUtil.appendXmlMessage("flag",map.get("method")));
				tmp.append(StringUtil.appendXmlMessage("rostername",map.get("rostername")));
				tmp.append(StringUtil.appendXmlMessage("datatype",map.get("datatype")));
				tmp.append(StringUtil.appendXmlMessage("iscache",map.get("iscache")));
				tmp.append("</roster>");
			}
			if (tmp.length() > 0) {//剩余的，没有达到 size个map，组成一个xml
				tmp.append("</rosters>").append("</Message>");
				String msg = body.toString() + tmp.toString();
				
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("msgBody", msg);
				m.put("authInfos", authSet);
				list.add(m);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("未能成功生成缓存刷新请求报文");
		}
	}
	
	/**
	 * 名单值生成缓存同步所需的xml报文数据
	 * @param arg
	 * @return
	 */
	public static List<Map<String, Object>> getRosterValueMessage(List<Map<String, Object>> rosterValueList) {
		if (rosterValueList == null || rosterValueList.isEmpty())
			return null;

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();//xml报文数据的集合
		Set<Map<String, Object>> authSet = new HashSet<Map<String,Object>>();//更新授权信息用的集合
		try {
			
			Map<Object, List<Map<String, Object>>> rosterValueMap = list2RosterValueMap(rosterValueList);
			Map<Object, List<Map<String, Object>>> tmpRosterValueMap = new LinkedHashMap<Object, List<Map<String,Object>>>();// 存放一个报文的所有数据
			int length = 0, maxMsg = 3 * 1024;//报文最大长度为3k
			
			//组装xml,每个xml不超过3k字节
			for (Entry<Object, List<Map<String, Object>>> entry : rosterValueMap.entrySet()) {
				Object key = entry.getKey();
//				List<Map<String, Object>> valList = entry.getValue();
				length = compList(length, tmpRosterValueMap, key, entry.getValue(), list, authSet);
				
			}
			
			StringBuffer body = new StringBuffer(maxMsg);
			body.append(getXmlHead(null));
			body.append(StringUtil.appendXmlMessage("tableName","TMS_MGR_ROSTERVALUE"));
			body.append("<rosters type='list'>");
			for (Entry<Object, List<Map<String, Object>>> entry : tmpRosterValueMap.entrySet()) {
				body.append("<roster type='map'>");
				body.append(StringUtil.appendXmlMessage("rosterId",entry.getKey()));
				body.append("<values type='list'>");
				for (int i = 0,len = entry.getValue().size();i <len ; i++) {
					Map<String, Object> map = entry.getValue().get(i);
					
					authSet.add((Map<String, Object>) map.get("authInfo"));
					
					body.append("<value type='map'>");
					body.append(StringUtil.appendXmlMessage("rosterValueId",map.get("id")));
					body.append(StringUtil.appendXmlMessage("rosterValue",map.get("operatevalue")));
					body.append(StringUtil.appendXmlMessage("flag",map.get("method")));
					body.append(StringUtil.appendXmlMessage("enabletime",map.get("enabletime")));
					body.append(StringUtil.appendXmlMessage("disabletime",map.get("disabletime")));
					body.append("</value>");
				}
				body.append("</values>").append("</roster>");
			}
			body.append("</rosters>").append("</Message>");
			
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("msgBody", body.toString());
			m.put("authInfos", authSet);
			list.add(m);
			
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("未能成功生成缓存刷新请求报文");
		}
	}
	
	private static int compList(int length, Map<Object, List<Map<String, Object>>> tmpMap,Object key, List<Map<String, Object>> list, List<Map<String, Object>> msgList, Set<Map<String, Object>> authSet) {
		int maxItem = 423, maxMsg = 3 * 1024;
		int size = (maxMsg - length) / maxItem;// 当前报文还能接受多少个map
		if (size < list.size()) {
			//  当前list数据超出报文长度
			List<Map<String, Object>> tmpList = new ArrayList<Map<String,Object>>();
			if (size > 0) {
				tmpList.addAll(list.subList(0, size));
				tmpMap.put(key, tmpList);
			}
			
			// tmpMap数据，生成报文
			StringBuffer body = new StringBuffer(maxMsg);
			body.append(getXmlHead(null));
			body.append(StringUtil.appendXmlMessage("tableName","TMS_MGR_ROSTERVALUE"));
			body.append("<rosters type='list'>");
			for (Entry<Object, List<Map<String, Object>>> entry : tmpMap.entrySet()) {
				body.append("<roster type='map'>");
				body.append(StringUtil.appendXmlMessage("rosterId",entry.getKey()));
				body.append("<values type='list'>");
				for (int i = 0,len = entry.getValue().size();i <len ; i++) {
					Map<String, Object> map = entry.getValue().get(i);
					
					authSet.add((Map<String, Object>) map.get("authInfo"));
					
					body.append("<value type='map'>");
					body.append(StringUtil.appendXmlMessage("rosterValueId",map.get("id")));
					body.append(StringUtil.appendXmlMessage("rosterValue",map.get("operatevalue")));
					body.append(StringUtil.appendXmlMessage("flag",map.get("method")));
					body.append(StringUtil.appendXmlMessage("enabletime",map.get("enabletime")));
					body.append(StringUtil.appendXmlMessage("disabletime",map.get("disabletime")));
					body.append("</value>");
				}
				body.append("</values>").append("</roster>");
			}
			body.append("</rosters>").append("</Message>");
			
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("msgBody", body.toString());
			m.put("authInfos", authSet);
			msgList.add(m);
			
			authSet.clear();
			tmpMap.clear();
			length = 0;
			tmpList.clear();
	
			tmpList.addAll(list.subList(size, list.size()));
			length = compList(length, tmpMap, key, tmpList, msgList, authSet);
		} else {
			tmpMap.put(key, list);
			length += (list.size() * maxItem);
		}
		return length;
	}
	/**
	 * 发送socket消息，通知其他服务器刷新缓存
	 * @param ip
	 * @param port
	 * @param b
	 * @param ws
	 * @param count
	 * @return
	 */
	public static void sendMessage(String ip, String port, byte[] b, byte[] ws, int count){
		try {
			new SocketClient(ip, Integer.parseInt(port)).send(b, ws);
		} catch (Exception e) {
			if (count <= 0) {
				throw new TmsMgrAuthRefreshCacheException(ip + "端口"+port+"缓存刷新失败,由于"+e.getMessage());
			}
			count--;
			sendMessage(ip, port, b, ws, count);
		}
	}
	
  	/**
	 * 拼装报文体部分xml的头信息
	 * messageProp：message的属性例如：
	 *  xmlns="http://www.higinet.com.cn" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance";
	 *  xsi:schemaLocation="http://www.higinet.com.cn/tms.xsd";
	 * @return
	 */
	public static String getXmlHead(String messageProp){
		StringBuffer sb = new StringBuffer();				
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<Message").append(messageProp==null?"":messageProp).append(">");
		return sb.toString();
	}	
	
	//名单值格式处理：相同rosterId的名单值放在一起
	private static Map<Object, List<Map<String, Object>>> list2RosterValueMap(List<Map<String, Object>> rosterValueList){
		Map<Object, List<Map<String, Object>>> rosterValueMap = new HashMap<Object, List<Map<String, Object>>>();
		
		if (rosterValueList == null || rosterValueList.isEmpty()) {
			return null;
		}
		
		for (int i = 0; i < rosterValueList.size(); i++) {
			List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
			Map<String, Object> _map =  rosterValueList.get(i);
			Object  rosterId = _map.get("rosterId");
			
			if (rosterValueMap.get(rosterId) != null) {
				rosterValueMap.get(rosterId).add(_map);
			}else {
				mapList.add(_map);
				rosterValueMap.put(rosterId,mapList);
			}
		}
		return rosterValueMap;
	}
	
}
