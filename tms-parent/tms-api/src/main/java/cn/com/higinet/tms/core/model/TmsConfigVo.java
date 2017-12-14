package cn.com.higinet.tms.core.model;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.core.common.StaticParameter;
import cn.com.higinet.tms.core.util.ResourceUtils;
import cn.com.higinet.tms.core.util.StringUtil;

/**
 * 接口信息配置信息
 * 
 * @author zhangfg
 */
public class TmsConfigVo {

	// static Logger logger = LogManager.getLogger(TmsConfigVo.class);
	private static Logger logger = LoggerFactory.getLogger(TmsConfigVo.class);

	private static String serverIp;

	private static String port;

	private static int timeOut;

	private static int connectTimeOut;

	private static int resendTimes;

	private static int count;

	private static int queueCapacity;

	/**
	 * 报文解析器名称
	 */
	private static String parserName;

	/**
	 * 哪些处置方式可以不需要中断交易
	 */
	private static String allowDisposal;

	/**
	 * api总开关
	 */
	private static boolean apiFlag = true;

	/**
	 * filter总开关
	 */
	private static boolean filterFlag = true;

	/**
	 * filter配置的风险评估方式
	 */
	private static String filterRiskType = "";

	/**
	 * 交易的配置信息
	 */
	private static Map<String, TransConfigVo> transMap;

	/**
	 * 交易详细信息开关
	 */
	private static boolean transpinFlag = false;

	private static String datePatten;

	public static int getResengTimes() {
		return resendTimes;
	}

	public static void setResengTimes(int resengTimes) {
		TmsConfigVo.resendTimes = resengTimes;
	}

	// private static Map<String,TransInfoConversion> transConverMap;
	private static List<String> serverList = new ArrayList<String>();

	private static List<String> portList = new ArrayList<String>();
	//	private static AtomicIntegerArray serv_err_count = new AtomicIntegerArray(32);
	//	private static AtomicLongArray serv_failed_time = new AtomicLongArray(32);
	//	private static AtomicIntegerArray serv_testing = new AtomicIntegerArray(32);

	private static Integer maxServerErrorCount;

	private static Integer servRetryDelay;

	private static int servSuccFoundNum;

	private static boolean servSuccFoundFlag;

	private static int servMaxFoundNum;

	private static int maxServerNum;

	private static Properties prop = new Properties();
	//	private static boolean isServFailed(int i) {
	//		return serv_err_count.get(i) >= maxServerErrorCount;
	//	}

	//	public static boolean incServError(int i) {
	//		int c = serv_err_count.incrementAndGet(i) - maxServerErrorCount;
	//		if (c >= 0) {
	//			serv_failed_time.set(i, System.currentTimeMillis());
	//			if (c == 0)
	//				logger.error("服务器" + i + "出现错误，已经标记为不可用！");
	//			else
	//				logger.error("服务器" + i + "依旧不可用！");
	//
	//			return true;
	//		}
	//		logger.error("服务器" + i + "出现错误，错误计数：" + serv_err_count.get(i));
	//		return false;
	//	}

	//	public static int wantRetry(int i) {
	//		if (isServFailed(i))
	//			return serv_failed_time.get(i) + servRetryDelay <= System.currentTimeMillis() && serv_testing.compareAndSet(i, 0, 1) ? 1 : 0;
	//
	//		return -1;
	//	}

	//	public static boolean clearTestFlag(int i) {
	//		return serv_testing.compareAndSet(i, 1, 0);
	//	}

	//	public static boolean clearServError(int i) {
	//		serv_err_count.lazySet(i, 0);
	//		return serv_testing.compareAndSet(i, 1, 0);
	//	}

	static {
		// System.out.println("*********TmsConfigVo****读取properties文件开始*************");
		// logger.setLevel(Level.INFO);
		InputStream in = null;
		try {
			// 顺丰架构要求配置文件抽出来放在单独一个目录
			// 如果系统变量放在关键属性TMS_API_PATH就从系统中读取,否则从默认目录
			String apiPath = System.getProperty(StaticParameter.TMS_API_PATH);
			if (null != apiPath && apiPath.trim().length() > 0) {
				// String propPath = ClassLoader.getSystemResource("") + path + StaticParameter.PROPERTIES_PATH_TMS;
				logger.info("-------现在正使用指定的配置文件-------");
				String propPath = ResourceUtils.getPropPath(apiPath);
				String path = propPath + StaticParameter.PROPERTIES_PATH_TMS;
				in = new FileInputStream(path);
				logger.info("-------配置文件路径:" + path);
			} else {
				logger.info("-------现在正使用默认的配置文件-------");
				in = TmsConfigVo.class.getResourceAsStream(StaticParameter.PROPERTIES_PATH_TMS);
			}
			try {
				prop.load(in);
			} catch (Exception e) {
				logger.error("Load properties error", e);
			}
			serverIp = getProperty(StaticParameter.PRO_SERVER_IP_NAME);
			port = getProperty(StaticParameter.PRO_SERVER_PORT_NAME);

			handleServerIp(serverIp);
			handleServerPort(port);
			//			serv_err_count = new AtomicIntegerArray(serverIp.length());

			String tTimeOut = getProperty(StaticParameter.PRO_SERVER_TIMEOUT);
			if (tTimeOut != null && !"".equals(tTimeOut)) {
				timeOut = Integer.valueOf(tTimeOut);
			} else {
				timeOut = 1000;
			}

			String connTimeOut = getProperty(StaticParameter.PRO_SERVER_CONNECT_TIMEOUT);
			if (connTimeOut != null && !"".equals(connTimeOut)) {
				connectTimeOut = Integer.valueOf(connTimeOut);
			} else {
				connectTimeOut = 200;
			}

			String serv_retry_delay = getProperty(StaticParameter.PRO_SERVER_CHECK_PRIOD);
			if (serv_retry_delay != null && !"".equals(serv_retry_delay)) {
				servRetryDelay = Integer.valueOf(serv_retry_delay);
			} else {
				servRetryDelay = 5000;
			}

			String tMaxErrorCount = getProperty(StaticParameter.PRO_SERVER_MAX_ERROR_COUNT);
			if (tMaxErrorCount != null && !"".equals(tMaxErrorCount)) {
				maxServerErrorCount = Integer.valueOf(tMaxErrorCount);
			} else {
				maxServerErrorCount = 30;
			}
			String tmaxServerNum = getProperty(StaticParameter.PRO_SERVER_NUM);
			if (tmaxServerNum != null && !"".equals(tmaxServerNum)) {
				maxServerNum = Integer.valueOf(tmaxServerNum);
			} else {
				maxServerNum = 8;
			}
			String tservMaxFoundNum = getProperty(StaticParameter.PRO_SERVER_FOUND_NUM);
			if (tservMaxFoundNum != null && !"".equals(tservMaxFoundNum)) {
				servMaxFoundNum = Integer.valueOf(tservMaxFoundNum);
			} else {
				servMaxFoundNum = maxServerNum * 2;
			}
			String tservSuccFoundNum = getProperty(StaticParameter.PRO_SERVER_SUCC_F_NUM);
			if (tservSuccFoundNum != null && !"".equals(tservSuccFoundNum)) {
				servSuccFoundNum = Integer.valueOf(tservSuccFoundNum);
			} else {
				servSuccFoundNum = 2;
			}
			String tservSuccFoundFlag = getProperty(StaticParameter.PRO_SERVER_SUCC_F_ON);
			servSuccFoundFlag = "1".equals(tservSuccFoundFlag);
			if (!servSuccFoundFlag) {
				servSuccFoundNum = 1;
			}

			String tResendTimes = getProperty(StaticParameter.PRO_SERVER_RESEND_TIMES,"3");
			if (tResendTimes != null && !"".equals(tResendTimes)) {
				resendTimes = Integer.valueOf(tResendTimes);
			}

			String tspinFlag = getProperty(StaticParameter.PRO_SERVER_TRANSPIN_FLAG);
			transpinFlag = "1".equals(tspinFlag);
			String tCount = getProperty(StaticParameter.DISPATCH_COUNT);
			if (tCount != null && !"".equals(tCount)) {
				count = Integer.valueOf(tCount);
			}

			String tQueueCapacity = getProperty(StaticParameter.DISPATCH_QUEUECAPACITY);
			if (tQueueCapacity != null && !"".equals(tQueueCapacity)) {
				queueCapacity = Integer.valueOf(tQueueCapacity);
			}
			datePatten = getProperty(StaticParameter.CHANNEL_DATE_PATTEN, "yyyy-MM-dd HH:mm:ss");
			parserName = getProperty(StaticParameter.XML_PARSER_NAME, StaticParameter.XML_PARSER_SAX);
			allowDisposal = getProperty(StaticParameter.ALLOW_DISPOSAL);
			// System.out.println(serverIp+":"+port+";timeOut="+timeOut);
			// System.out.println("********TmsConfigVo*****读取properties文件结束*************");
		} catch (Exception e) {
			serverIp = "";
			port = "";
			timeOut = 0;
			parserName = "";
			allowDisposal = "";
			logger.error(" TmsConfigVo init faild.Can't load the resource:", e);
		}
	}

	public static String getProperty(String name) {
		return getProperty(name, null);
	}

	public static String getProperty(String name, String defaultValue) {
		String res = prop.getProperty(name);
		if (res == null) {
			res = System.getProperty(name); //JVM级属性
		}
		if (res == null) {
			res = System.getenv(name);//操作系统级
		}
		return res == null ? defaultValue : res;
	}

	/**
	 * 获取api开关
	 * 
	 * @return
	 */
	public synchronized static boolean getApiFlag() {
		return apiFlag;
	}

	public synchronized static String getAllowDisposal() {
		return allowDisposal;
	}

	/**
	 * 设置api开关
	 * 
	 * @param apiFlag
	 */
	public synchronized static void setApiFlag(boolean apiFlag) {
		TmsConfigVo.apiFlag = apiFlag;
	}

	/**
	 * 获取filter开关
	 * 
	 * @return
	 */
	public synchronized static boolean getFilterFlag() {
		return filterFlag;
	}

	public static int maxServErrorCount() {
		return maxServerErrorCount;
	}

	/**
	 * 设置filter开关
	 * 
	 * @return
	 */
	public synchronized static void setFilterFlag(boolean filterFlag) {
		TmsConfigVo.filterFlag = filterFlag;
	}

	/**
	 * 获取filter默认风险评估方式
	 * 
	 * @return
	 */
	public synchronized static String getFilterRiskType() {
		return filterRiskType;
	}

	/**
	 * 设置filter默认风险评估方式
	 * 
	 * @return
	 */
	public synchronized static void setFilterRiskType(String filterRiskType) {
		TmsConfigVo.filterRiskType = filterRiskType;
	}

	/**
	 * 获取filter交易配置信息
	 * 
	 * @return
	 */
	public synchronized static Map<String, TransConfigVo> getTransMap() {
		return transMap;
	}

	/**
	 * 设置filter交易配置信息
	 * 
	 * @return
	 */
	public synchronized static void setTransMap(Map<String, TransConfigVo> ttransMap) {
		TmsConfigVo.transMap = ttransMap;
	}

	/**
	 * 获取交易信息转换类
	 * 
	 * @return
	 */
	// public synchronized static Map<String, TransInfoConversion> getTransConverMap() {
	// return transConverMap;
	// }
	/**
	 * 设置交易信息转换类
	 * 
	 * @param transConverMap
	 */
	// public synchronized static void setTransConverMap(Map<String, TransInfoConversion> transConverMap) {
	// TmsConfigVo.transConverMap = transConverMap;
	// }

	public static boolean isTranspinFlag() {
		return transpinFlag;
	}

	public static void setTranspinFlag(boolean transpinFlag) {
		TmsConfigVo.transpinFlag = transpinFlag;
	}

	/**
	 * 获取TMS serverIp
	 * 
	 * @return
	 */
	public synchronized static String getServerIp() {
		return serverIp;
	}

	/**
	 * 设置TMS serverIp
	 * 
	 * @return
	 */
	public synchronized static void setServerIp(String serverIp) {
		TmsConfigVo.serverIp = serverIp;
		handleServerIp(serverIp);
	}

	/**
	 * 获取TMS server端口
	 * 
	 * @return
	 */
	public synchronized static String getPort() {
		return port;
	}

	public synchronized static long getServRetryDelay() {
		return servRetryDelay;
	}

	/**
	 * 返回最大支持服务器数量
	 * @return
	 */
	public static int getMaxServerNum() {
		return maxServerNum;
	}

	/**
	 * 返回最大查询可用服务器次数
	 * @return
	 */
	public static int getServSuccFoundNum() {
		return servSuccFoundNum;
	}

	public static void setServSuccFoundNum(int servSuccFoundNum) {
		TmsConfigVo.servSuccFoundNum = servSuccFoundNum;
	}

	/**
	 * 返回是否多次查询可用服务器标识
	 * true:查询，false:只查询一次
	 * @return
	 */
	public static boolean isServSuccFoundFlag() {
		return servSuccFoundFlag;
	}

	/**
	 * 返回最大查询服务器次数
	 * @return
	 */
	public static int getServMaxFoundNum() {
		return servMaxFoundNum;
	}

	public static void setServMaxFoundNum(int servMaxFoundNum) {
		TmsConfigVo.servMaxFoundNum = servMaxFoundNum;
	}

	public static void setMaxServerNum(int maxServerNum) {
		TmsConfigVo.maxServerNum = maxServerNum;
	}

	/**
	 * 设置TMS server端口
	 * 
	 * @return
	 */
	public synchronized static void setPort(String port) {
		TmsConfigVo.port = port;
		handleServerPort(port);
	}

	/**
	 * 获取发送报文发送超时时间
	 * 
	 * @return
	 */
	public synchronized static int getTimeOut() {
		return timeOut;
	}

	/**
	 * 设置发送报文发送超时时间
	 * 
	 * @return
	 */
	public synchronized static void setTimeOut(int timeOut) {
		TmsConfigVo.timeOut = timeOut;
	}

	public synchronized static int getConnectTimeOut() {
		return connectTimeOut;
	}

	public synchronized static void setConnectTimeOut(int connectTimeOut) {
		TmsConfigVo.connectTimeOut = connectTimeOut;
	}

	/**
	 * 获取xml解析器名称
	 * 
	 * @return
	 */
	public synchronized static String getParserName() {
		return parserName;
	}

	/**
	 * 设置xml解析器名称
	 * 
	 * @return
	 */
	public synchronized static void setParserName(String parserName) {
		TmsConfigVo.parserName = parserName;
	}

	/**
	 * 根据配置的交易Id获取相应的配置信息
	 * 
	 * @param transId
	 * @return
	 */
	public synchronized static TransConfigVo getTransConfigVoById(String transId) {
		if (transMap == null || transMap.size() == 0) {
			return null;
		}
		return transMap.get(transId);
	}

	/**
	 * 关闭所有开关
	 */
	public synchronized static void closeAllFlag() {
		TmsConfigVo.apiFlag = false;
		TmsConfigVo.filterFlag = false;
	}

	/**
	 * 关闭所有API接口
	 */
	public synchronized static void closeApiFlag() {
		TmsConfigVo.apiFlag = false;
	}

	/**
	 * 关闭所有filter接口
	 */
	public synchronized static void closeFilterFlag() {
		TmsConfigVo.filterFlag = false;
	}

	/**
	 * 根据交易号关闭交易开关
	 * 
	 * @param transId
	 */
	public synchronized static void closeTransFlag(String transId) {
		TransConfigVo trans = transMap.get(transId);
		if (trans == null) {
			return;
		}
		trans.setStartFlag(false);
		TmsConfigVo.transMap.put(transId, trans);
	}

	/**
	 * 当前处置是否允许通过而不需要加强认证
	 */
	public static boolean isAllow(String disposal) {
		if (StringUtil.isBlank(allowDisposal) || StringUtil.isBlank(disposal)) {
			return false;
		}
		return allowDisposal.indexOf(disposal) > -1;
	}

	/**
	 * 根据交易号开启交易开关
	 * 
	 * @param transId
	 */
	public synchronized static void openTransFlag(String transId) {
		if (transMap == null || transMap.size() == 0) {
			return;
		}
		TransConfigVo trans = transMap.get(transId);
		if (trans == null) {
			return;
		}
		trans.setStartFlag(true);
		transMap.put(transId, trans);
	}

	public static List<String> getServerList() {
		return serverList;
	}

	public static List<String> getPortList() {
		return portList;
	}

	private static void handleServerIp(String serverIp) {
		List<String> serverList = getIpOrPortList(serverIp);
		synchronized (TmsConfigVo.serverList) {
			TmsConfigVo.serverList.clear();
			TmsConfigVo.serverList.addAll(serverList);
		}
	}

	private static void handleServerPort(String port) {
		List<String> portList = getIpOrPortList(port);
		synchronized (TmsConfigVo.portList) {
			TmsConfigVo.portList.clear();
			TmsConfigVo.portList.addAll(portList);
		}
	}

	private static List<String> getIpOrPortList(String ipOrPort) {
		if (ipOrPort == null || ipOrPort.trim().length() == 0)
			return null;
		String[] ipOrPorts = ipOrPort.split("\\,");
		List<String> ipOrPortList = new ArrayList<String>(ipOrPorts.length);
		for (int i = 0, len = ipOrPorts.length; i < len; i++) {
			String _ipOrPort_ = ipOrPorts[i];
			if (_ipOrPort_ == null || _ipOrPort_.trim().length() == 0)
				continue;
			ipOrPortList.add(_ipOrPort_.trim());
		}
		return ipOrPortList;
	}

	public static String getDatePatten() {
		return datePatten;
	}

	public static void setDatePatten(String datePatten) {
		TmsConfigVo.datePatten = datePatten;
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		TmsConfigVo.count = count;
	}

	public static int getQueueCapacity() {
		return queueCapacity;
	}

	public static void setQueueCapacity(int queueCapacity) {
		TmsConfigVo.queueCapacity = queueCapacity;
	}
}
