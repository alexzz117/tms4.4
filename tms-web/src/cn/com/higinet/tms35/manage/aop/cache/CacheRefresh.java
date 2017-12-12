package cn.com.higinet.tms35.manage.aop.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * socket 缓存刷新接口类
 * @author yanghui
 * @date 2011-7-28
 * 
 * @version 1.0
 */
public interface CacheRefresh {
	public String refresh(String msgArr);
	//刷新用户缓存
	public String refreshUserCache(String msgArr);
	//刷新名单或名单值
	public Set<Map<String, Object>> refreshNameList(List<Map<String,Object>> rosterList , List<Map<String, Object>> rosterValueList);
}
