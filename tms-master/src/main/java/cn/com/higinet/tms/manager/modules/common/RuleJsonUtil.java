/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.codehaus.jackson.map.ObjectMapper;

import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;

/**
 * 功能/模块:
 * @author 张立群
 * @version 1.0  May 22, 2013
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
public class RuleJsonUtil {
	/**
	 * 通过json字符串转成map对象
	 * @param json
	 * @return
	 */
	public static Map<String, Map<String,Object>> json2Maps(String json) {
		Map<String, Map<String,Object>>  ruleJsonMap = new LinkedHashMap<String, Map<String,Object>>();
		try {
			if (StringUtil.isEmpty(json)) return null;
			String tempjson = json.replaceAll("'", "\\\"") ;
			tempjson = tempjson.replaceAll("\n", "");
			tempjson = tempjson.replaceAll("	", "");
			List<Map<String,Object>> list = new ObjectMapper().readValue(tempjson, List.class);
			for (int j = 0; j < list.size(); j++) {
				Map<String,Object> map = list.get(j);
				ruleJsonMap.put(MapUtil.getString(map, "RULE_ID"), map);
			}

		} catch (Exception e) {
			return null;
		}

		return ruleJsonMap;
	}

	/**
	 * 通过json字符串转成map对象
	 * @param json
	 * @return
	 */
	public static Map<String,Object> json2Map(String json) {
		try {
			if (StringUtil.isEmpty(json)) return null;
			String tempjson = json.replaceAll("'", "\\\"") ;
			tempjson = tempjson.replaceAll("\n", "");
			tempjson = tempjson.replaceAll("	", "");
			Map<String,Object> map = new ObjectMapper().readValue(tempjson, Map.class);
			return map;
		} catch (Exception e) {
			return null;
		}
	}
	/**
	* 方法描述:当个规则的MAP转成JSON串
	* @param jsonMap
	* @return
	 */
	public static String map2Json(Map<String,Object> jsonMap) {
		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("'RULE_ID':").append("'").append(MapUtil.getString(jsonMap, "RULE_ID")).append("'").append(",");
		json.append("'POSITION':").append("'").append(MapUtil.getString(jsonMap, "POSITION")).append("'").append(",");
		json.append("'DIRECTIONAL':").append("[");
		List<String> directional = (List<String>) MapUtil.getObject(jsonMap, "DIRECTIONAL");
		if(directional!=null && directional.size()>0) {
			for (int i = 0; i < directional.size(); i++) {
				json.append("'").append(directional.get(i)).append("'");
				if(i!=directional.size()-1) json.append(",");
			}
		}
		json.append("]");
		json.append("}");
		return json.toString();
	}
	/**
	 * 方法描述:当个规则的MAP转成JSON串
	 * @param jsonMap
	 * @return
	 */
	public static String map2JsonAll(Map<String,Object> jsonMap) {
		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("'RULE_ID':").append("'").append(MapUtil.getString(jsonMap, "RULE_ID")).append("'").append(",");
		json.append("'RULE_NAME':").append("'").append(MapUtil.getString(jsonMap, "RULE_NAME")).append("'").append(",");
		json.append("'RULE_SHORTDESC':").append("'").append(MapUtil.getString(jsonMap, "RULE_SHORTDESC")).append("'").append(",");
		json.append("'RULE_DESC':").append("'").append(MapUtil.getString(jsonMap, "RULE_DESC")).append("'").append(",");
		json.append("'RULE_TXN':").append("'").append(MapUtil.getString(jsonMap, "RULE_TXN")).append("'").append(",");
		json.append("'RULE_COND':").append("'").append(MapUtil.getString(jsonMap, "RULE_COND").replace("'", "\\'")).append("'").append(",");
		json.append("'RULE_COND_IN':").append("'").append(MapUtil.getString(jsonMap, "RULE_COND_IN").replace("'", "\\'")).append("'").append(",");
		json.append("'RULE_SCORE':").append("'").append(MapUtil.getString(jsonMap, "RULE_SCORE")).append("'").append(",");
		json.append("'RULE_TIMESTAMP':").append("'").append(MapUtil.getString(jsonMap, "RULE_TIMESTAMP")).append("'").append(",");
		json.append("'RULE_ENABLE':").append("'").append(MapUtil.getString(jsonMap, "RULE_ENABLE")).append("'").append(",");
		json.append("'RULE_ISTEST':").append("'").append(MapUtil.getString(jsonMap, "RULE_ISTEST")).append("'").append(",");
		json.append("'POSITION':").append("'").append(MapUtil.getString(jsonMap, "POSITION")).append("'").append(",");
		json.append("'DIRECTIONAL':").append("[");
		List<String> directional = (List<String>) MapUtil.getObject(jsonMap, "DIRECTIONAL");
		if(directional!=null && directional.size()>0) {
			for (int i = 0; i < directional.size(); i++) {
				json.append("'").append(directional.get(i)).append("'");
				if(i!=directional.size()-1) json.append(",");
			}
		}
		json.append("]");
		json.append("}");
		return json.toString();
	}

	/**
	* 方法描述:多个规则的MAP组成JSON
	* @param jsonMap
	* @return
	 */
	public static String maps2Json(Map<String,Map<String,Object>> jsonMap) {
		if(MapUtil.isEmpty(jsonMap)) return "";

		StringBuilder json = new StringBuilder();
		json.append("[");
		Set<Entry<String, Map<String, Object>>> set = jsonMap.entrySet();
		for (Entry<String, Map<String, Object>> entry : set) {
			json.append(map2Json(entry.getValue())).append(",");
		}
		json = new StringBuilder(json.substring(0, json.length() - 1));
		json.append("]");
		return json.toString();
	}

	/**
	 * 方法描述:多个规则的MAP组成JSON
	 * @param jsonMap
	 * @return
	 */
	public static String maps2JsonAll(Map<String,Map<String,Object>> jsonMap) {
		if(MapUtil.isEmpty(jsonMap)) return "";

		StringBuilder json = new StringBuilder();
		json.append("[");
		Set<Entry<String, Map<String, Object>>> set = jsonMap.entrySet();
		for (Entry<String, Map<String, Object>> entry : set) {
			json.append(map2JsonAll(entry.getValue())).append(",");
		}
		json = new StringBuilder(json.substring(0, json.length() - 1));
		json.append("]");
		return json.toString();
	}

}
