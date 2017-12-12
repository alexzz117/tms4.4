package cn.com.higinet.tms35.manage.aop.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import cn.com.higinet.tms35.manage.common.DBConstant;
import cn.com.higinet.tms35.manage.common.StaticParameters;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.mgr.service.impl.NameListServiceImpl;

/**
 * socket 同步刷新抽象类，实现了公共的刷新方法，定义了抽象方法
 * @author yanghui
 * @date 2011-7-28
 */
public abstract class AbstractCacheRefresh implements CacheRefresh {
	// server表记录
	protected abstract List<Map<String, Object>> getIpList();
	// 如果失败重试次数
	protected abstract int againNum();
	
	/**
	 * 通知各个缓存刷新服务器刷新缓存
	 * @return 缓存刷新结果信息
	 */
	public String refresh(String refreshMsg) {
		List<Map<String, Object>> ipList = getIpList();
		if(ipList==null || ipList.size()==0){
			return "未能成功获取可用的缓存刷新服务；";
		}
		
		String refreshInfo = "";
//		long currentTime = System.currentTimeMillis();
		for (Map<String, Object> server: ipList) {
//			long activeTime = Long.parseLong(MapUtil.getString(server, "ACTIVETIME"));
//			long pingDelay = Long.parseLong(MapUtil.getString(server, "PINGDELAY"));
			String ip  = MapUtil.getString(server, DBConstant.TMS_RUN_SERVER_IPADDR);
			String port = MapUtil.getString(server, DBConstant.TMS_RUN_SERVER_PORT); 
			
//			if(currentTime-activeTime>pingDelay){
				//refreshInfo += ip+"端口"+port+"上的缓存刷新服务已失效；";
//				System.out.println("currentTime-activeTime="+(currentTime-activeTime));
//				continue;
//			} else {
			try{
				Map<String, byte[]> res = CacheSynService.getMessage(refreshMsg);
				byte[] b = res.get("b");
				byte[] ws = res.get("ws");
				CacheSynService.sendMessage(ip, port,b, ws, againNum());
			}catch(Exception e){
				refreshInfo += e.getMessage();
			}
//			}
		}
		return refreshInfo;
	}
	
	/**
	 * 刷新用户缓存
	 * @return 缓存刷新结果信息
	 */
	public String refreshUserCache(String refreshMsg) {
		List<Map<String, Object>> ipList = getIpList();
		if(ipList==null || ipList.size()==0){
			return "未能成功获取可用的缓存刷新服务；";
		}
		
		String refreshInfo = "";
//		long currentTime = System.currentTimeMillis();
		for (Map<String, Object> server: ipList) {
//			long activeTime = Long.parseLong(MapUtil.getString(server, "ACTIVETIME"));
//			long pingDelay = Long.parseLong(MapUtil.getString(server, "PINGDELAY"));
			String ip  = MapUtil.getString(server, DBConstant.TMS_RUN_SERVER_IPADDR);
			String port = MapUtil.getString(server, DBConstant.TMS_RUN_SERVER_PORT); 
			
//			if(currentTime-activeTime>pingDelay){
				//refreshInfo += ip+"端口"+port+"上的缓存刷新服务已失效；";
//				System.out.println("currentTime-activeTime="+(currentTime-activeTime));
//				continue;
//			} else {
			try{
				Map<String, byte[]> res = CacheSynService.getUserAcheMessage(refreshMsg);
				byte[] b = res.get("b");
				byte[] ws = res.get("ws");
				CacheSynService.sendMessage(ip, port,b, ws, againNum());
			}catch(Exception e){
				refreshInfo += e.getMessage();
			}
//			}
		}
		return refreshInfo;
	}
	
	/**
	 * 刷新名单或名单值缓存
	 * @return 缓存刷新结果信息
	 */
	public Set<Map<String, Object>> refreshNameList(List<Map<String, Object>> rosterList, List<Map<String, Object>> rosterValueList) {
		List<Map<String, Object>> ipList = getIpList();
		
		Set<Map<String, Object>> authInfoSet = new HashSet<Map<String,Object>>();
		if(ipList==null || ipList.size()==0) {
			for (Map<String, Object> auttInfo : authInfoSet) {
				// 有问题，更新刷新记录
				auttInfo.put("REFRESH_INFO", "未能成功获取可用的缓存刷新服务；");
			}
			return authInfoSet;
		}
		
		String ip = "";
		String port = "";
		List<Map<String, Object>> resRosterList = CacheSynService.getRosterMessage(rosterList);
		List<Map<String, Object>> resRosterValueList = CacheSynService.getRosterValueMessage(rosterValueList);
		for (Map<String, Object> server: ipList) {
//			long activeTime = Long.parseLong(MapUtil.getString(server, "ACTIVETIME"));
//			long pingDelay = Long.parseLong(MapUtil.getString(server, "PINGDELAY"));
			ip  = MapUtil.getString(server, DBConstant.TMS_RUN_SERVER_IPADDR);
			port = MapUtil.getString(server, DBConstant.TMS_RUN_SERVER_PORT);
			//名单
			if (resRosterList != null && !resRosterList.isEmpty()) {
				for (int i = 0; i < resRosterList.size(); i++) {
					Map<String, Object> map = resRosterList.get(i);
					String body = (String) map.get("msgBody");
					String result = buildMsg(body, ip, port, againNum());
					Set<Map<String, Object>> authInfos = (Set<Map<String,Object>>) map.get("authInfos");
					
					if (result != null && !result.isEmpty()) {
						for (Map<String, Object> auttInfo : authInfos) {
							// 有问题，更新刷新记录
							String refreshInfo = (String) auttInfo.get("REFRESH_INFO");
							//判断报错信息是否有重复
							if(!isDuplicate(result, auttInfo)){//不重复，加入REFRESH_INFO中
								auttInfo.put("REFRESH_INFO", refreshInfo+";"+result);
							}
						}
					}
					authInfoSet.addAll(authInfos);
				}
			}
			
			//名单值
			if (resRosterValueList != null && !resRosterValueList.isEmpty()) {
				for (int i = 0; i < resRosterValueList.size(); i++) {
					Map<String, Object> map = resRosterValueList.get(i);
					String body = (String) map.get("msgBody");
					
					String result = buildMsg(body, ip, port, againNum());
					Set<Map<String, Object>> authInfos = (Set<Map<String,Object>>) map.get("authInfos");
					
					if (result != null && !result.isEmpty()) {
				
						for (Map<String, Object> auttInfo : authInfos) {
							// 有问题，更新刷新记录(auttInfo)
							String refreshInfo = (String) auttInfo.get("REFRESH_INFO");
							//判断报错信息是否有重复
							if(!isDuplicate(result, auttInfo)){//不重复，加入REFRESH_INFO中
								auttInfo.put("REFRESH_INFO", refreshInfo+";"+result);
							}
						}
					}
					authInfoSet.addAll(authInfos);
				}
			}
		}
		return authInfoSet;
	}
	//名单值或名单发送报文
	private static String buildMsg(String body, String ip, String port, int againNum) {
		String result = "";
		try {
			byte[] b = StaticParameters.MSG_HEADER;
			byte[] ws = body.getBytes("UTF-8");
			String len = String.valueOf(ws.length);
			len = "00000000".substring(len.length()) + len;
			//System.out.println("len:"+len);
			System.arraycopy(len.getBytes(), 0, b, 0, 8);
			CacheSynService.sendMessage(ip, port, b, ws, againNum);
		} catch (Exception e) {
			result += e.getMessage();
		}
		return result;
	}
	
	//判断报错信息是否有重复
	private static boolean isDuplicate(String result, Map<String, Object> auttInfo){
		boolean flag = false;
		String value = (String) auttInfo.get("REFRESH_INFO");
		
		if(value == null || "".equals(value)){
			return flag;
		}
		
		String[] infos= value.split("\\;");
		
		for (String info : infos) {
			if (result.equals(info)) {
				flag = true;
				break;
			}
		}
		return flag;
	}
}

