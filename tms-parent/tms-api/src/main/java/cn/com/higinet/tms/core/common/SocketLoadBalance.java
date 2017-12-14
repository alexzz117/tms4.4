package cn.com.higinet.tms.core.common;

import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.core.model.OperatCycle;
import cn.com.higinet.tms.core.model.Server;
import cn.com.higinet.tms.core.model.TmsConfigVo;

/**
 * Socket负载均衡客户端工具类
 * 
 * @author lining
 * 
 */
public class SocketLoadBalance {
	public static final String TMS_CONFIRM_NOTRANS = "02009007";// 风险确认找不到交易数据
	// static Logger logger = LogManager.getLogger(SocketLoadBalance.class);
	private static Logger logger = LoggerFactory.getLogger(SocketLoadBalance.class);

	private final static String[] errorInfo = new String[] { "无错误", "服务器连接状态变化", "当前服务器不可用", "IO错误", "服务器过早关闭连接", "服务器返回错误信息", "无可用的风险评估服务器", "socket连接错误", "socket读取数据异常", "风险确认找不到交易数据", "IO读超时", "IO写超时" };

	public static String getErrorInfo(int errorId) {
		if (errorId >= errorInfo.length || errorId < 0)
			return "错误码超出定义范围，或者连接过程发生错误";
		return errorInfo[errorId];
	}

	public static void setOperatCycleInfo(OperatCycle operatCycle, int i) {
		if (operatCycle != null) {
			operatCycle.setScoketData(i);
		}
	}

	/**
	 * 发送报文
	 * 
	 * @param operatCycle
	 *            对象生命周期对象
	 * @param sb
	 *            返回报文接收对象
	 * @param runId
	 *            负载均衡分发凭证
	 * @param message
	 *            发送的报文
	 * @param timeOut
	 *            超时时间
	 * @param socket
	 *            socket连接
	 * @return
	 */
	public static int send(OperatCycle operatCycle, StringBuffer sb, String message, int timeOut, Server server) {
		return send(operatCycle, sb, message, timeOut, 0, server);
	}

	/**
	 * @param operatCycle
	 * @param sb
	 * @param message
	 * @param timeOut
	 * @param connTimeOut
	 *            为了补救队列而加，补救队列的conntimeout要设置长一点，尽量保证重发正确率， 不在乎时间问题（如果补救队列里面事故消息太多的话，则需要另外找原因了）
	 * @param server
	 * @return
	 */
	public static int send(OperatCycle operatCycle, StringBuffer sb, String message, int timeOut, int connTimeOut, Server server) {
		SocketClient client = null;

		if (connTimeOut <= 0) {
			connTimeOut = TmsConfigVo.getConnectTimeOut();
			if (connTimeOut <= 0) {
				connTimeOut = 1000;
			}
		}
		if (operatCycle != null) {
			operatCycle.newSocketData();
		}

		clock c = new clock();
		setOperatCycleInfo(operatCycle, OperatCycle.INDEX_SOCKET_DATA_GET_SOCKET_BEGORE);
		int iterCount = LogHolder.currentIterCount();
		long begin = System.currentTimeMillis();
		try {
			LogHolder.put(LogHolder.BEFORE_CONN + iterCount, begin);
			client = new SocketClient(server.getIp(), server.getPort(), connTimeOut);
		} catch (Exception e) {
			logger.error("new SocketClinet(" + server + ") error. " + e.getLocalizedMessage(), e);
			if (operatCycle != null) {
				operatCycle.setServerInfo(getErrorInfo(7));
			}
			LogHolder.put(LogHolder.ERR_CONN + iterCount, LogHolder.ERR_CONN + "[" + iterCount + "]:" + e.toString() + " [time:" + (System.currentTimeMillis() - LogHolder.beginTime()) + "]");
			return 7;
		}
		setOperatCycleInfo(operatCycle, OperatCycle.INDEX_SOCKET_DATA_GET_SOCKET_TIME);
		String errorInfo = null;
		String phase = null;
		try {
			setOperatCycleInfo(operatCycle, OperatCycle.INDEX_SOCKET_DATA_SEND_MESSAGE_BEFORE);

			int left = c.left(timeOut);
			if (left <= 0) {
				logger.error("请求超时:" + client, new RuntimeException());
				return 7;
			}
			client.setSoTimeout(left);
			phase = LogHolder.ERR_WRITE;
			client.write(message);
			begin = System.currentTimeMillis();
			LogHolder.put(LogHolder.BEFORE_WRITE + iterCount, begin);
			client.flush();
			setOperatCycleInfo(operatCycle, OperatCycle.INDEX_SOCKET_DATA_SEND_MESSAGE_FINISH);
			begin = System.currentTimeMillis();
			LogHolder.put(LogHolder.BEFORE_READ + iterCount, begin);
			phase = LogHolder.ERR_READ;
			String ret = client.read(timeOut, c);
			// logger.info("TMS返回报文:"+ret);
			setOperatCycleInfo(operatCycle, OperatCycle.INDEX_SOCKET_DATA_READ_MESSAGE_FINISH);

			if (ret == null) {
				return 8;
			}
			if (ret.length() > 32 && TMS_CONFIRM_NOTRANS.equals(ret.substring(24, 32))) {
				return StaticParameter.RESULT_FLAG_NOTXN;
			}
			sb.append(ret);
			return 0;
		} catch (Exception e) {
			errorInfo = "[" + client.toString() + "] error, " + e.getLocalizedMessage();
			logger.error(errorInfo, e);
			LogHolder.put(phase + iterCount, phase + "[" + iterCount + "]:" + e.toString() + " [time:" + (System.currentTimeMillis() - LogHolder.beginTime()) + "]");
			if (e instanceof SocketTimeoutException) {
				if (LogHolder.ERR_WRITE.equals(phase)) {
					// 写超时
					return StaticParameter.RESULT_FLAG_WRITETIMEOUT;
				} else if (LogHolder.ERR_READ.equals(phase)) {
					// 读超时
					return StaticParameter.RESULT_FLAG_READTIMEOUT;
				}
			}
		} finally {
			client.close();
			if (operatCycle != null) {
				operatCycle.setScoketData(OperatCycle.INDEX_SOCKET_DATA_SOCKET_CLOSE);
				if (errorInfo == null) {
					operatCycle.setServerInfo("[" + client.toString() + "] success.");
				} else {
					operatCycle.setServerInfo(errorInfo);
				}
			}

		}
		return 3;
	}
	// /**
	// * 发送报文
	// *
	// * @param operatCycle
	// * 对象生命周期对象
	// * @param sb
	// * 返回报文接收对象
	// * @param runId
	// * 负载均衡分发凭证
	// * @param message
	// * 发送的报文
	// * @param timeOut
	// * 超时时间
	// * @param socket
	// * socket连接
	// * @return
	// */
	// public static int send(OperatCycle operatCycle, StringBuffer sb, String runId, String message, int timeOut, SocketClient socket) {
	// SocketClient client = null;
	// if (operatCycle != null) {
	// operatCycle.newSocketData();
	// }
	//
	// clock c = new clock();
	// if (socket == null) {
	// if (operatCycle != null) {
	// operatCycle.setScoketData(OperatCycle.INDEX_SOCKET_DATA_GET_SOCKET_BEGORE);
	// }
	// client = getEffectiveSocket(runId);
	// if (operatCycle != null) {
	// operatCycle.setScoketData(OperatCycle.INDEX_SOCKET_DATA_GET_SOCKET_TIME);
	// }
	// } else {
	// client = socket;
	// }
	//
	// if (client == null) {
	// if (operatCycle != null) {
	// operatCycle.setServerInfo(getErrorInfo(6));
	// }
	// return 6;
	// }
	//
	// String errorInfo = null;
	// try {
	// if (operatCycle != null) {
	// operatCycle.setScoketData(OperatCycle.INDEX_SOCKET_DATA_SEND_MESSAGE_BEFORE);
	// }
	//
	// int left=c.left(timeOut);
	// if (left <= 0) {
	// logger.error("请求超时:" + client, new RuntimeException());
	// return 7;
	// }
	//
	//
	// client.setSoTimeout(left);
	// client.write(message);
	// client.flush();
	//
	//
	// if (operatCycle != null) {
	// operatCycle.setScoketData(OperatCycle.INDEX_SOCKET_DATA_SEND_MESSAGE_FINISH);
	// }
	// String ret = client.read(timeOut, c);
	// if (operatCycle != null) {
	// operatCycle.setScoketData(OperatCycle.INDEX_SOCKET_DATA_READ_MESSAGE_FINISH);
	// }
	//
	// if (ret == null) {
	// TmsConfigVo.incServError(client.index);
	// return 8;
	// }
	//
	// sb.append(ret);
	//
	// if (TmsConfigVo.clearServError(client.index)) {
	// logger.info("服务器恢复:" + client);
	// }
	//
	// return 0;
	// } catch (Exception e) {
	// TmsConfigVo.incServError(client.index);
	// errorInfo = "[" + client.toString() + "] error, " + e.getLocalizedMessage();
	// logger.error(errorInfo, e);
	// } finally {
	// client.close();
	// if (operatCycle != null) {
	// operatCycle.setScoketData(OperatCycle.INDEX_SOCKET_DATA_SOCKET_CLOSE);
	// if (errorInfo == null) {
	// operatCycle.setServerInfo("[" + client.toString() + "] success.");
	// } else {
	// operatCycle.setServerInfo(errorInfo);
	// }
	// }
	//
	// TmsConfigVo.clearTestFlag(client.index);
	// }
	// return 3;
	// }
	// /**
	// * 获取有效的Socket连接
	// *
	// * @param runId
	// * @return
	// */
	// private static SocketClient getEffectiveSocket(String runId) {
	// SocketClient client = null;
	// List<String> ips = TmsConfigVo.getServerList();
	// List<String> ports = TmsConfigVo.getPortList();
	// int connTimeOut = TmsConfigVo.getConnectTimeOut();
	// if (connTimeOut <= 0) {
	// connTimeOut = 1000;
	// }
	//
	// int index = -1;
	// runId = ((runId == null || runId.trim().length() == 0) ? StringUtil.randomUUID() : runId);
	// long hashId = (hash.clac(runId) >>> 1) % 53777;
	// Random random = new Random(hashId);
	//
	// // 过滤已检测无效的服务器
	// Set<Integer> set = new HashSet<Integer>();
	//
	// try {
	//
	// int wantTest =0;
	// while (set.size() < ips.size()) {
	// index = random.nextInt(ips.size());
	// // 过滤已检测的无效服务器
	// set.add(index);
	//
	// wantTest = TmsConfigVo.wantRetry(index);
	//
	// if (wantTest == 0) {
	// continue;
	// }
	//
	// String ip = ips.get(index);
	// String port = ports.get(index < ports.size() ? index : 0);
	//
	// if (wantTest == 1) {
	// logger.info("测试服务器:" + ip + ":" + port);
	// }
	//
	// try {
	// client = new SocketClient(ip, Integer.parseInt(port), connTimeOut, index);
	// } catch (Exception e) {
	// TmsConfigVo.incServError(index);
	// if (wantTest == 1)
	// TmsConfigVo.clearTestFlag(index);
	// logger.error("new SocketClinet(ip=" + ip + ", port=" + port + ") error. " + e.getLocalizedMessage(), e);
	// continue;
	// }
	// return client;
	// }
	//
	// } finally {
	// set.clear();
	// set = null;
	// }
	// TmsConfigVo.clearTestFlag(index);
	// return null;
	// }
}