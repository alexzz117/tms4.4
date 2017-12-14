/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.core.common.LogHolder;
import cn.com.higinet.tms.core.common.ServerMonitor;
//import cn.com.higinet.tms.core.common.LogManager;
import cn.com.higinet.tms.core.common.SocketClient;
import cn.com.higinet.tms.core.common.SocketLoadBalance;
import cn.com.higinet.tms.core.common.StaticParameter;
import cn.com.higinet.tms.core.common.hash;
import cn.com.higinet.tms.core.model.OperatCycle;
import cn.com.higinet.tms.core.model.RiskResult;
import cn.com.higinet.tms.core.model.Server;
import cn.com.higinet.tms.core.model.TmsConfigVo;
import cn.com.higinet.tms.core.survivor.EventContext;
import cn.com.higinet.tms.core.survivor.EventContextHolder;
import cn.com.higinet.tms.core.survivor.SurvivorSanctuary;
import cn.com.higinet.tms.core.util.StringUtil;

/**
 * 
 * @author zhangfg
 * @version 2.2.0
 * @date 2011-10-21
 * @description 公用的消息服务抽象类
 */
public abstract class MessageAbstractService implements MessageService {
	// static Logger logger = LogManager.getLogger(MessageAbstractService.class);
	private static Logger logger = LoggerFactory.getLogger(MessageAbstractService.class);
	final static Map<String, String> g_error_info = new HashMap<String, String>();
	static {
		g_error_info.put(StaticParameter.ERROR_MESSAGE, "报文数据异常。");
		g_error_info.put(StaticParameter.ERROR_NOSIGNATURE, "交易未识别。");
		g_error_info.put(StaticParameter.ERROR_NOTXN, "没有此交易。");
		g_error_info.put(StaticParameter.ERROR_DISABLED, "交易未启用。");
		g_error_info.put(StaticParameter.ERROR_NONEEDCONFIRM, "交易无需确认。");
		g_error_info.put(StaticParameter.ERROR_NOSESSION, "没有此会话。");
		g_error_info.put(StaticParameter.ERROR_NOUSER, "没有此用户。");
		g_error_info.put(StaticParameter.TMS_SERVER_ERROR, "TMS服务器异常。调用TMS服务器，向其发送报文失败。");
		g_error_info.put(StaticParameter.TMS_SERVICE_TIMEOUT, "调用业务监控系统超时。");
		g_error_info.put(StaticParameter.TMS_API_ERROR, "API接口开关关闭。");
		g_error_info.put(StaticParameter.TMS_DATA_ERROR, "数据不全。");
		g_error_info.put(StaticParameter.SYSTEM_ERROR, "风险评估异常");
		g_error_info.put(StaticParameter.ERR_UNKNOWN, "未知平台异常。");
		g_error_info.put(StaticParameter.ERR_SERVICE_HANDLER_UNKNOWN, "未知处理器异常。");
		g_error_info.put(StaticParameter.ERR_SERVICE_UNKNOWN, "未知服务异常。");
		g_error_info.put(StaticParameter.ERR_SERVICE_NOFOUND, "服务没找到。");
		g_error_info.put(StaticParameter.ERR_SERVICE_HANDLER_DECODE, "解码处理器解码异常。");
		g_error_info.put(StaticParameter.ERR_SERVICE_HANDLER_ENCODE, "编码处理器编码异常。");
		g_error_info.put(StaticParameter.SYSTEM_SUCCESS, "风险评估成功。");
	}

	public String getBackInfo(String backCode) {
		return g_error_info.get(backCode);
	}

	/**
	 * 解析报文体,返回风险信息Risk对象
	 * 
	 * @param bodyStr
	 * @param actionCode
	 * @return
	 */
	public abstract void deCodeBodyXml(RiskResult risk, String bodyStr, String actionCode);

	/**
	 * 拼装报文体部分xml的头信息 messageProp：message的属性例如： xmlns="http://www.higinet.com.cn" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"; xsi:schemaLocation="http://www.higinet.com.cn/tms.xsd";
	 * 
	 * @return
	 */
	public String getXmlHead(String messageProp) {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<Message").append(messageProp == null ? "" : messageProp).append(">");
		return sb.toString();
	}

	/**
	 * 拼装报文体部分xml的头信息 没有xmlns等属性
	 * 
	 * @return
	 */
	public String getXmlHead() {
		return getXmlHead(null);
	}

	/**
	 * 拼装filter报文体部分xml的头信息
	 * 
	 * @return
	 */
	public String getHttpXmlHead() {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<Http xmlns=\"http://www.higinet.com.cn\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		sb.append(" xsi:schemaLocation=\"http://www.higinet.com.cn/tms.xsd\">");
		return sb.toString();
	}

	/**
	 * 将map中的信息拼装成xml格式，key作为节点，value作为值
	 * 
	 * @param map
	 * @return
	 */
	public String getXmlExtMap(Map<String, Object> map) {
		StringBuffer sb = new StringBuffer("");
		if (map != null && !map.isEmpty()) {
			Set keySet = map.keySet();
			Iterator keyIt = keySet.iterator();
			while (keyIt.hasNext()) {
				String key = (String) keyIt.next();
				Object value = map.get(key);
				sb.append(StringUtil.appendXmlMessage(key, value));
			}
		}
		return sb.toString();
	}

	/**
	 * 判断list是否为空，为空返回false，不为空返回true
	 * 
	 * @param list
	 * @return
	 */
	public boolean listIsNull(List list) {
		if (list == null || list.size() < 1)
			return false;
		else
			return true;
	}

	public String composeBackHead(String actionCode, String backCode, int bodyLength) {
		StringBuffer sb = new StringBuffer();
		if (actionCode != null && !"".equals(actionCode)) {
			String len = String.valueOf(bodyLength);
			len = "00000000".substring(len.length()) + len;
			sb.append(len);// 报文体长度
			sb.append("TMS").append(" ").append(" ").append(" ").append(" ").append(" ");// 服务号
			sb.append(actionCode);// 交易号
			sb.append(StaticParameter.MESSAGE_TYPE).append(" ");// 报文体类型
			sb.append(backCode);// 返回码
		}
		return sb.toString();
	}

	/**
	 * 发送消息
	 * 
	 * @param ip
	 * @param port
	 * @param b
	 * @param ws
	 * @param timeOut
	 * @return
	 */
	public String sendMessage(String runId, String head, String body, int timeOut, int resendTimes) {
		return sendMessage(null, runId, head, body, timeOut, null);
	}

	public String sendMessage(OperatCycle model, String runId, String head, String body, int timeOut, SocketClient s) {
		try {
			if (timeOut == 0) {
				timeOut = TmsConfigVo.getTimeOut();
			}
			int resendTimes = TmsConfigVo.getResengTimes();
			StringBuffer sb = new StringBuffer(1024);
			int ret = send_request(model, sb, runId, head, body, timeOut, resendTimes, s);
			if (ret == 0)
				return sb.toString();
			return null;
		} catch (Exception e) {
			logger.info("The method 'sendMessage' of the 'MessageAbstractService' class exception.！" + e.getMessage());
		}
		return null;
	}

	/**
	 * 发送报文
	 * 
	 * @param sb
	 * @param runId
	 * @param head
	 * @param body
	 * @param timeOut
	 * @param resendTimes
	 * @return 返回结果
	 */

	public int send_request(StringBuffer sb, String runId, String head, String body, int timeOut, int resendTimes) {
		return send_request(null, sb, runId, head, body, timeOut, resendTimes, null);
	}

	/**
	 * 发送报文
	 * 
	 * @param model
	 * @param sb
	 * @param runId
	 * @param head
	 * @param body
	 * @param timeOut
	 * @param resendTimes
	 * @return 返回结果
	 */
	public int send_request(OperatCycle operatCycle, StringBuffer sb, String runId, String head, String body, int timeOut, int resendTimes, SocketClient s) {
		LogHolder.put(LogHolder.BEFORE_SEND, System.currentTimeMillis());
		int ret_id = -1;
		// int send_times = 0;
		EventContext _context = EventContextHolder.currentContext();
		boolean isSurviver = false; // 2017年3月31日修改，为了解决评估和确认发往不同服务器造成评估失效的问题，
		if (_context != null) {
			isSurviver = _context.isSurviver();// 如果是true的话，则证明是在补救队列里面发的消息，走补救队列的发送逻辑
		}
		int connTimeOut = 0;// 为补救队列而加
		if (isSurviver) {
			connTimeOut = _context.getConnTimeOut();
		}
		// List<Server> copyList = new ArrayList<Server>(ServerMonitor.getInstance().getServerCache());
		int[] copyList = ServerMonitor.getCopyServerStatus();

		// SocketClient client = null;

		int index = -1;
		runId = ((runId == null || runId.trim().length() == 0) ? StringUtil.randomUUID() : runId);
		long hashId = (hash.clac(runId) >>> 1) % 53777;
		Random random = new Random(hashId);

		// 过滤已检测无效的服务器
		// Set<Integer> set = new HashSet<Integer>();
		int index_succ = 0;
		int index_sum = 0;
		// int wantTest =0;
		int listSize = copyList.length;
		int maxSuccIndex = listSize - 1;
		String message = head + body;
		while (index_succ < TmsConfigVo.getServSuccFoundNum() && index_sum < TmsConfigVo.getServMaxFoundNum()) {
			index = random.nextInt(listSize);
			index_sum++;
			if (ServerMonitor.isServFailed(copyList[index])) {
				continue;
			}
			Server tmpServer = ServerMonitor.getServer(index);
			if (null == tmpServer) {
				if (index < maxSuccIndex) {
					maxSuccIndex = index;
				}
				continue;
			}
			// 获取可用服务器加一
			index_succ++;
			LogHolder.increaseIterCount();
			long begin = System.currentTimeMillis();
			LogHolder.put(LogHolder.ITER_TIME + LogHolder.currentIterCount(), begin);
			LogHolder.put(LogHolder.ITER_SERVER + LogHolder.currentIterCount(), tmpServer.getIp() + ":" + tmpServer.getPort());
			ret_id = SocketLoadBalance.send(operatCycle, sb, message, timeOut, connTimeOut, tmpServer);
			LogHolder.recordIterateTime(begin);
			if (!isSurviver && StaticParameter.RESULT_FLAG_NOTXN == ret_id) {
				// 非补救队列发送评估确认的时候，没有在缓存找到这个评估数据，这类事故转移到surviver queue
				logger.info("评估服务器(" + tmpServer + ")出现异常，返回码:" + ret_id + "(" + SocketLoadBalance.getErrorInfo(ret_id) + ")，本次评估确认操作被加入到Surviver队列。");
				EventContext context = new EventContext();
				context.setSurviver(true);
				context.setBody(body);
				context.setHead(head);
				context.setModel(operatCycle);
				context.setRunId(runId);
				context.setBeginTime(System.currentTimeMillis());// 都是服务器本地操作，记录本地时间即可
				SurvivorSanctuary.instance().addSurvivor(context);
				return ret_id;
			}
			if (operatCycle.isConfirm() && StaticParameter.RESULT_FLAG_READTIMEOUT == ret_id) {
				// 顺丰这块遇到了问题，由于read timeout导致确认接口失效转移，发送到其他服务器，前一个服务器其实本地处理已经成功，失效转移的服务器进行了重复统计，导致统计了脏数据
				// 如果是确认接口，并且服务器响应超时了（read timeout），那么直接返回，不进行失效转移。
				logger.warn("Read timout exception occurred when confirm transaction,this will be ignored." + body);
				return ret_id;
			}
			if (ret_id != 0) {
				if (!isBusinessError(ret_id)) {//如果是业务异常，则不会记录服务器失败次数
					copyList[index] = ServerMonitor.server_fail;
					ServerMonitor.incServError(index);
					index_succ = 0;
				}
				StringBuilder _sb = new StringBuilder();
				_sb.append("评估服务器(" ).append( tmpServer ).append(")出现错误，SocketLoadBalance对象send返回码:").append(  ret_id ).append(  "(" ).append(  SocketLoadBalance.getErrorInfo(ret_id) ).append(  ").");
				if(_context!=null){
					_sb.append("Message body is: ").append(_context.getBody());
				}
				logger.error(_sb.toString());
			} else {
				ServerMonitor.clearServError(index);
				return ret_id;
			}
		}
		if (isSurviver) {
			SurvivorSanctuary.instance().addSurvivor(_context);
		}
		// if (index_succ == 0) {
		// if (logger.isInfoEnabled()) {
		// logger.info("runId:" + runId + ",开始循环补发.");
		// }
		// for (int i = 0; i < maxSuccIndex; i++) {
		// index = random.nextInt(listSize);
		// if (ServerMonitor.isServFailed(copyList[index])) {
		// continue;
		// }
		// Server tmpServer = ServerMonitor.getServer(index);
		// LogHolder.increaseResendCount();
		// ret_id = SocketLoadBalance.send(operatCycle, sb, message, timeOut, tmpServer);
		// if (ret_id != 0) {
		// ServerMonitor.incServError(index);
		// } else {
		// ServerMonitor.clearServError(index);
		// return ret_id;
		// }
		// }
		// logger.error("交易没有找到可用的服务器,报文如下:\n head:" + head + " \n body:" + body);
		// }
		// do {
		// ret_id = SocketLoadBalance.send(operatCycle, sb, runId, head + body, timeOut, s);
		// if (ret_id != 0) {
		// send_times++;
		// continue;
		// }
		// break;
		// } while (send_times <= resendTimes);
		/*
		 * if (ret_id != 0) { try { String actionCode = head.substring(16, 20); StringBuilder sf = new StringBuilder(); sf.append(getXmlHead()); sf.append(StringUtil.appendXmlMessage("errorInfo", SocketLoadBalance.getErrorInfo(ret_id))); sf.append("</Message>"); String backBody =
		 * sf.toString(); String backCode = StaticParameter.ERR_SERVICE_UNKNOWN; if (ret_id == 6) { backCode = StaticParameter.ERR_SERVICE_NOFOUND; } String backHead = composeBackHead(actionCode, backCode, backBody.getBytes("UTF-8").length); sb.append(backHead + backBody); }
		 * catch (Exception ex) { logger.info("The method 'send_request' of the 'MessageAbstractService' class exception.！" + ex.getMessage()); } }
		 */
		return ret_id;
	}

	private static final int[] BUSINESS_ERRORS = new int[] { StaticParameter.RESULT_FLAG_NOTXN };

	/**
	 * 判断异常编码是否是业务异常，如果是业务类型异常，那么不会记录服务器Server的失败次数
	 * 
	 * @param retId
	 * @return
	 */
	private boolean isBusinessError(int retId) {
		for (int i = 0, len = BUSINESS_ERRORS.length; i < len; i++) {
			if (BUSINESS_ERRORS[i] == retId) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {
//		long s = 20195999891913L;
//		for (int j = 0; j < 100000; j++) {
//			long hashId = (hash.clac((s + j) + "") >>> 1) % 53777;
//			Random random = new Random(hashId);
//			String r = "0123";
//			for (int i = 0; i < 32; i++) {
//				String n = random.nextInt(4) + "";
//				r = r.replaceAll(n, "");
//				if (r.equals("")) {
//					break;
//				}
//			}
//			if (!r.equals("")) {
//				System.out.println(r);
//				System.out.println(s + j);
//			}
//		}
	}
}