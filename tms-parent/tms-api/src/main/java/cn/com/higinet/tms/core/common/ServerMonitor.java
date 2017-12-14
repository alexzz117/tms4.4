package cn.com.higinet.tms.core.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.core.model.Server;
import cn.com.higinet.tms.core.model.TmsConfigVo;
import cn.com.higinet.tms.core.service.TransApiMessage;
import cn.com.higinet.tms.core.service.impl.TransApiMessageImpl;
import cn.com.higinet.tms.core.util.StringUtil;

public class ServerMonitor {
	private static Logger logger = LoggerFactory.getLogger(ServerMonitor.class);

	//	private static ServerMonitor instance;
	//	public static final int maxServer = 8;
	private static volatile List<Server> servers = Collections.synchronizedList(new ArrayList<Server>(TmsConfigVo.getMaxServerNum()));

	private static AtomicIntegerArray serv_err_count = new AtomicIntegerArray(TmsConfigVo.getMaxServerNum());

	private static TransApiMessage transMessage = new TransApiMessageImpl();

	//	private static AtomicReferenceArray<Byte> serv_err_count = new AtomicReferenceArray<Byte>(TmsConfigVo.getMaxServerNum());
	private static final byte server_succ = 0;

	public static final byte server_fail = -1;

	static AtomicBoolean isInit = new AtomicBoolean(true);

	private static String ipsCache = "";

	private static boolean updateServerList = Boolean.valueOf(TmsConfigVo.getProperty("servermonitor.updateServerList", "true"));
	static {
		init();
		//初始化等待，第一次初始化完成后才可以连接交易
		logger.info("正在初始化IP列表,请稍候...");
		checkServers();
		logger.info("第一次连接服务器初始化列表完成...");
		CheckTask ct = new CheckTask();
		Thread monitor = new Thread(ct);
		monitor.setDaemon(true);
		monitor.setName("ServerMonitor");
		monitor.start();
	}
	//	public static ServerMonitor getInstance(){
	//		
	//			if (instance != null)
	//				return instance;
	//
	//			synchronized (ServerMonitor.class)
	//			{
	//				if (instance != null)
	//					return instance;
	//				return instance = new ServerMonitor();
	//			}
	//	}

	private static void init() {
		if (TmsConfigVo.getServerList().size() == 0 || TmsConfigVo.getServerList().size() != TmsConfigVo.getPortList().size()) {
			throw new RuntimeException("服务器配置为空或服务器端口数量不匹配");
		}
		for (int i = 0; i < TmsConfigVo.getServerList().size(); i++) {
			servers.add(new Server(TmsConfigVo.getServerList().get(i), TmsConfigVo.getPortList().get(i)));
			serv_err_count.set(i, server_succ);
		}
		for (int i = TmsConfigVo.getServerList().size(); i < 8; i++) {
			servers.add(null);
			serv_err_count.set(i, server_fail);
		}
	}
	//	@SuppressWarnings("unchecked")
	//	private List<Server> getServerList() {
	//		Map<String, Object> serverInfo = transMessage.getRemoteServerInfo(0);
	//		if(serverInfo == null){
	//			return servers;
	//		}
	//		List<String> remoteServers = (List<String>) serverInfo.get("Server");
	//		if(remoteServers == null){
	//			return servers;
	//		}
	//		if(logger.isDebugEnabled()){
	//			logger.info("获取远程服务器列表："+remoteServers);
	//		}
	//		List<Server> serverlist = new ArrayList<Server>(servers);
	//		Server tmp = null;
	//		for(String ipport:remoteServers){
	//			tmp = new Server(ipport);
	//			if(!serverlist.contains(tmp)){
	//				serverlist.add(tmp);
	//			}
	//		}
	//		if(serverlist.size() == servers.size()){
	//			return servers;
	//		}
	//		return serverlist;
	//	}

	//	public List<Server> getServerCache() {
	//		return servers;
	//	}
	public static boolean incServError(int i) {
		//		if (serv_err_count.get(i) == server_fail) {
		//			logger.error("服务器(" + getServer(i) + ")依旧不可用！");
		//			return true;
		//		}
		int c = serv_err_count.incrementAndGet(i) - TmsConfigVo.maxServErrorCount();
		if (c >= 0) {
			//		serv_failed_time.set(i, System.currentTimeMillis());
			if (c == 0)
				logger.error("服务器[" + i + "](" + getServer(i) + ")出现错误，已经标记为不可用！");
			else
				logger.error("服务器[" + i + "](" + getServer(i) + ")依旧不可用！");

			//			serv_err_count.set(i, server_fail);
			return true;
		}
		logger.error("服务器[" + i + "](" + getServer(i) + ")出现错误，错误计数：" + serv_err_count.get(i));
		return false;
	}

	public static void clearServError(int i) {
		int num = serv_err_count.get(i);
		serv_err_count.lazySet(i, server_succ);
		if (num != server_succ) {
			logger.warn("服务器[" + i + "](" + getServer(i) + ")错误次数已重置为0,上次错误次数:" + num);
		}
	}

	/**
	 * 是否服务不可用
	 * 	true:不可用
	 * @param i
	 * @return
	 */
	public static boolean isServFailed(int v) {
		return v >= TmsConfigVo.maxServErrorCount() || v == server_fail;
		//		return v == server_fail || v >= TmsConfigVo.maxServErrorCount();
	}

	public static Server getServer(int index) {
		if (index < servers.size()) {
			return servers.get(index);
		}
		return null;
	}

	//	static List<Server> getServerList(){
	//		return servers;
	//	}
	//	static void setServErrCount(int i ,int v){
	//		serv_err_count.set(i, v);
	//	}
	/**
	 * 返回当前服务可用记得复制数组
	 * @return
	 */
	public static int[] getCopyServerStatus() {
		int length = serv_err_count.length();
		int[] ret = new int[length];
		if (length > 0) {
			int last = length - 1;
			for (int i = 0; i < last; ++i)
				ret[i] = serv_err_count.get(i);
			// Do the last write as volatile
			int e = serv_err_count.get(last);
			ret[last] = e;
		}
		return ret;
	}

	private static boolean changeServerList(String ips) {
		boolean initFlag = false;
		boolean ischange = false;
		if (isInit.get()) {
			ipsCache = ips;
			initFlag = true;
		} else {
			if (ipsCache.equals(ips)) {
				return ischange;
			} else if (ips.startsWith(ipsCache)) {
				initFlag = false;
			} else {
				initFlag = true;
			}
		}
		String[] ip = ips.split(",");
		boolean islog = false;
		if (initFlag) {
			for (int i = 0; i < ip.length && i < TmsConfigVo.getMaxServerNum(); i++) {
				Server tmp = ServerMonitor.getServer(i);
				islog = true;
				ischange = true;
				//当列表发生切换,则替换列表
				if (null == tmp) {
					//新增服务器
					servers.add(i, new Server(ip[i]));
					serv_err_count.set(i, server_succ);
					logger.info("新增服务器[" + i + "](" + getServer(i) + ")");
				} else {
					//当列表发生切换,则替换列表
					if (!tmp.equals(ip[i])) {
						logger.info("服务器[" + i + "](" + tmp + ")IP地址已改变，新地址:" + ip[i]);
						tmp.setServer(ip[i]);
						clearServError(i);
					}
				}
			}
		} else {
			for (int i = 0; i < ip.length && i < TmsConfigVo.getMaxServerNum(); i++) {
				Server tmp = ServerMonitor.getServer(i);
				//当列表发生切换,则替换列表
				if (null == tmp) {
					islog = true;
					//新增服务器
					ischange = true;
					servers.add(i, new Server(ip[i]));
					serv_err_count.set(i, server_succ);
					logger.info("新增服务器[" + i + "](" + getServer(i) + ")");
				}
			}
		}
		//		for (int i = 0; i < ip.length && i < TmsConfigVo.getMaxServerNum(); i++) {
		//			Server tmp = ServerMonitor.getServer(i);
		//			if (null != tmp) {
		//				if (tmp.equals(ip[i])) {
		//					continue;
		//				}else {
		//					ischange = true;
		//					islog = true;
		//					//当列表发生切换,则替换列表
		//					logger.info("服务器("+tmp+")IP地址已改变，新地址:"+ip[i]);
		//					tmp.setServer(ip[i]);
		////					break;
		//				}
		//			}else{
		//				ischange = true;
		//				islog = true;
		//				//新增服务器
		//				servers.add(i,new Server(ip[i]));
		//				serv_err_count.set(i, server_succ);
		//				logger.info("新增服务器("+getServer(i)+")");
		//			}
		//		}

		if (getServer(ip.length) != null) {
			for (int i = ip.length; i < servers.size(); i++) {
				if (null != getServer(i)) {
					islog = true;
					logger.info("服务器[" + i + "](" + getServer(i) + ")不存在,正在清理...");
					servers.set(i, null);
					serv_err_count.set(i, server_fail);
				}
			}
		}
		if (islog) {
			logger.info("服务器列表更新:" + servers);
		}
		ipsCache = ips;
		return ischange;
	}

	private static boolean checkServerList(boolean isGetIpList) {
		Iterator<Server> it = servers.iterator();
		int index = 0;
		int length = servers.size();
		boolean ischange = false;
		while (it.hasNext() && index < length) {
			final Server tmp = it.next();
			try {
				if (null == tmp) {
					return ischange;
				}
				//测试用报文 信息完整程度决定测试深入程度
				Map<String, Object> extinfo = new HashMap<String, Object>();
				if (isGetIpList) {
					extinfo.put(StaticParameter.MONITOR_TMS_SERVER_LIST, "1");
				} else {
					extinfo.put(StaticParameter.MONITOR_TMS_SERVER_LIST, "0");
				}
				String body = transMessage.composeBody("11111", extinfo, true);
				String head = transMessage.composeHead(StaticParameter.RISK_MONITOR_SERVERS, body.getBytes("UTF-8").length);
				//				String resultInfo = null;
				StringBuffer sb = new StringBuffer(1024);
				logger.info("Monitor send message[" + index + "](" + tmp + "):" + head + body);
				int ret = SocketLoadBalance.send(null, sb, head + body, TmsConfigVo.getTimeOut(), tmp);
				if (ret == 0) {
					String retInfo = sb.toString();
					logger.info("Monitor receive message[" + index + "](" + tmp + "):" + retInfo);
					ServerMonitor.clearServError(index);
					if (isGetIpList) {
						String ipStr = StringUtil.getNodeText(retInfo, StaticParameter.MONITOR_TMS_SERVERS);
						if (updateServerList)
							ischange = changeServerList(ipStr);
						//						ischange = changeServerList("218.17.23.12:8090,218.17.23.12:8090",isinit);
						isGetIpList = false;
						isInit.compareAndSet(true, false);
						if (ischange) {
							return ischange;
						}
					}
				} else {
					logger.info("Monitor receive message:" + ret + "(" + SocketLoadBalance.getErrorInfo(ret) + ")");
					ServerMonitor.incServError(index);
				}
			} catch (Exception e) {
				ServerMonitor.incServError(index);
				logger.error("服务器[" + tmp + "]监测方法调用异常!", e);
			} finally {
				index++;
			}
		}
		return ischange;
	}

	static void checkServers() {
		boolean ret = checkServerList(true);
		if (ret) {
			checkServerList(false);
		}
	}
}

class CheckTask implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(CheckTask.class);

	@Override
	public void run() {
		while (true) {
			try {
				long s = System.currentTimeMillis();
				ServerMonitor.checkServers();
				//				if (!ServerMonitor.isInit.get()) {
				//					logger.info("第一次连接服务器初始化列表完成...");
				//					ServerMonitor.isInit.compareAndSet(false, true);
				//				}
				long sleepTime = TmsConfigVo.getServRetryDelay() - (System.currentTimeMillis() - s);
				if (sleepTime > 0) {
					Thread.sleep(sleepTime);
				}
			} catch (Throwable t) {
				logger.error("更新服务器列表出错！", t);
			}
		}
	}

}
