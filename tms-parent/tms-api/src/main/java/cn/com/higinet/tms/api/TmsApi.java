/*
 * Copyright © 2012 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.core.common.LogHolder;
//import cn.com.higinet.tms.core.common.LogManager;
import cn.com.higinet.tms.core.common.SocketClient;
import cn.com.higinet.tms.core.common.StaticParameter;
import cn.com.higinet.tms.core.model.Batch;
import cn.com.higinet.tms.core.model.OperatCycle;
import cn.com.higinet.tms.core.model.RiskResult;
import cn.com.higinet.tms.core.model.TmsConfigVo;
import cn.com.higinet.tms.core.model.Transaction;
import cn.com.higinet.tms.core.model.User;
import cn.com.higinet.tms.core.service.TransApiMessage;
import cn.com.higinet.tms.core.service.impl.TransApiMessageImpl;
import cn.com.higinet.tms.core.survivor.SurvivorSanctuary;
import cn.com.higinet.tms.core.thread.DispatcherExecutor;
import cn.com.higinet.tms.core.util.DateUtil;
import cn.com.higinet.tms.core.util.StringUtil;

//import java.util.logging.Level;

/**
 * @author zhangfg
 * @version 1.1.0,2012-06-15
 * @description 网银调用风险监控系统接口
 * 
 */

public class TmsApi {
	private static Logger logger = LoggerFactory.getLogger(TmsApi.class);
	private static boolean useFlag = true;// 启用标识，开启/关闭API
	private static boolean syncFlag = true;// 同步标志 true同步 false异步
	private static int timeout = 3000;// 超时时间(毫秒)
	private static AtomicInteger isInit = new AtomicInteger(0);
	
	static {
		// 初始化数据，根据之前设计，所有方法均为static，包括init
		init();
	}

	/**
	 * 初始化方法，只执行一次
	 */
	public static void init() {
		if (isInit.get() > 0) {
			return;
		}
		DispatcherExecutor.getInstance();// 初始化队列线程池
		SurvivorSanctuary.instance();// 初始化Survivers队列
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(), "TMS-API-SHUTDOWN-HOOK"));
		isInit.incrementAndGet();
		logger.info("TMS API inited.");
	}

	public static void destroy() {
		if (isInit.getAndDecrement() > 0) {
			DispatcherExecutor.getInstance().destroy(); // 关闭队列线程池
			SurvivorSanctuary.instance().destroy();
			logger.info("TMS API destroyed.");
		}
	}

	private static class ShutdownHook implements Runnable {

		@Override
		public void run() {
			TmsApi.destroy();
		}

	}

	/**
	 * 获取支付余额的接口
	 * 
	 * @param userId
	 *            用户ID
	 * @param rating
	 *            客户等级
	 * @return
	 */
	public static String getBalance(String userId, String rating) {
		String val = "0.0";
		TransApiMessageImpl transMessage = new TransApiMessageImpl();
		Map<String, Object> ext = new HashMap();
		ext.put("USERID", userId);
		ext.put("RATING", rating);
		Transaction transaction = new Transaction();
		transaction.setCstNo(userId);
		transaction.setExtInfo(ext);
		String dispatch = getDispatch(transaction);
		transaction.setDispatch(dispatch);
		RiskResult riskResult = transMessage.singleEvaluate(transaction, syncFlag, timeout, StaticParameter.GET_STAT_VALUE, StaticParameter.RISK_INTERFACE_EVALUATE);
		if (null != riskResult && null != riskResult.getDisposal()) {
			val = riskResult.getDisposal();
		}
		return val;
	}

	/**
	 * 风险评估接口
	 * 
	 * @param transaction
	 *            交易信息数据
	 * @return
	 */
	public static RiskResult eval(Transaction transaction) {
		String dispatch = getDispatch(transaction);
		transaction.setDispatch(dispatch);
		return toolEvaluate(transaction, useFlag, syncFlag, timeout, StaticParameter.RISK_EVALUAT, StaticParameter.RISK_INTERFACE_EVALUATE);
	}

	/**
	 * 风险确认接口
	 * 
	 * @param transaction
	 *            交易信息数据
	 * @return
	 */
	public static RiskResult confirm(Transaction transaction) {
		String dispatch = getDispatch(transaction);
		transaction.setDispatch(dispatch);
		return toolEvaluate(transaction, useFlag, syncFlag, timeout, StaticParameter.RISK_CONFIRM, StaticParameter.RISK_INTERFACE_CONFIRM);
	}

	/**
	 * 风险评估接口
	 * 
	 * @param transaction
	 *            交易信息数据
	 * @return
	 */
	public static RiskResult riskEvaluate(Transaction transaction) {
		String dispatch = getDispatch(transaction);
		transaction.setDispatch(dispatch);
		return toolEvaluate(transaction, useFlag, syncFlag, timeout, StaticParameter.RISK_EVALUAT, StaticParameter.RISK_INTERFACE_EVALUATE);
	}

	/**
	 * 风险确认接口
	 * 
	 * @param transaction
	 *            交易信息数据
	 * @return
	 */
	public static RiskResult updateTransaction(Transaction transaction) {
		String dispatch = getDispatch(transaction);
		transaction.setDispatch(dispatch);

		return toolEvaluate(transaction, useFlag, syncFlag, timeout, StaticParameter.RISK_CONFIRM, StaticParameter.RISK_INTERFACE_CONFIRM);
	}

	/**
	 * 风险评估接口
	 * 
	 * @param transaction
	 *            交易信息数据
	 * @param useFlag
	 *            启用标识，开启/关闭API或应用探针 true开 false关
	 * @param syncFlag
	 *            同步标志 true同步 false异步
	 * @param timeout
	 *            超时时间(毫秒) ，如果为0，则以tmsServer.properties中的timeout为准
	 * @return
	 */
	public static RiskResult riskEvaluate(Transaction transaction, boolean useFlag, boolean syncFlag, int timeout) {
		String dispatch = getDispatch(transaction);
		transaction.setDispatch(dispatch);
		return toolEvaluate(transaction, useFlag, syncFlag, timeout, StaticParameter.RISK_EVALUAT, StaticParameter.RISK_INTERFACE_EVALUATE);
	}

	/**
	 * 风险确认接口
	 * 
	 * @param transaction
	 *            交易信息数据
	 * @param useFlag
	 *            启用标识，开启/关闭API或应用探针 true开 false关
	 * @param syncFlag
	 *            同步标志 true同步 false异步
	 * @param timeout
	 *            超时时间(毫秒) ，如果为0，则以tmsServer.properties中的timeout为准
	 * @return
	 */
	public static RiskResult updateTransaction(Transaction transaction, boolean useFlag, boolean syncFlag, int timeout) {
		String dispatch = getDispatch(transaction);
		transaction.setDispatch(dispatch);
		return toolEvaluate(transaction, useFlag, syncFlag, timeout, StaticParameter.RISK_CONFIRM, StaticParameter.RISK_INTERFACE_CONFIRM);
	}

	/**
	 * 批量风险评估接口
	 * 
	 * @param batch
	 *            批量交易总信息
	 * @param transactions
	 *            批量交易列表
	 * @param useFlag
	 *            启用标识，开启/关闭API或应用探针 true开 false关
	 * @param syncFlag
	 *            同步标志 true同步 false异步
	 * @param timeout
	 *            超时时间(毫秒) ，如果为0，则以tmsServer.properties中的timeout为准
	 * @return
	 */
	public static RiskResult batchRiskEvaluate(Batch batch, List<Transaction> transactions, boolean useFlag, boolean syncFlag, int timeout) {
		return toolBatchTransaction(batch, transactions, useFlag, syncFlag, timeout, StaticParameter.RISK_EVALUAT_BATCH, StaticParameter.RISK_INTERFACE_BATCH_EVALUATE);
	}

	/**
	 * 批量风险确认接口
	 * 
	 * @param batch
	 *            批量交易总信息
	 * @param transactions
	 *            批量交易列表
	 * @param useFlag
	 *            启用标识，开启/关闭API或应用探针 true开 false关
	 * @param syncFlag
	 *            同步标志 true同步 false异步
	 * @param timeout
	 *            超时时间(毫秒) ，如果为0，则以tmsServer.properties中的timeout为准
	 * @return
	 */
	public static RiskResult batchUpdateTransaction(Batch batch, List<Transaction> transactions, boolean useFlag, boolean syncFlag, int timeout) {
		return toolBatchTransaction(batch, transactions, useFlag, syncFlag, timeout, StaticParameter.RISK_CONFIRM_BATCH, StaticParameter.RISK_INTERFACE_BATCH_CONFIRM);
	}

	private static RiskResult toolEvaluate(Transaction transaction, boolean useFlag, boolean syncFlag, int timeout, String actionCode, String interfaceFlag) {
		LogHolder.reset();
		LogHolder.put(LogHolder.TXN_ID, interfaceFlag);
		if (transaction == null) {
			return errorResult("The Transaction is null.", StaticParameter.TMS_DATA_ERROR);
		}
		transaction.getOperatCycle().setPinTime(OperatCycle.INDEX_CALL_API_INTERFACE);
		if (!useFlag) {
			return errorResult("The param 'useFlag' is FALSE or the TMS api is configed close .", StaticParameter.TMS_API_ERROR);
		}
		// 同步应用探针的总开关的状态
		TmsConfigVo.setFilterFlag(useFlag);
		if (!syncFlag) {
			// 如果是异步评估
			DispatcherExecutor.getInstance().execute(transaction, timeout, actionCode, interfaceFlag);
			return null;
		}

		TransApiMessageImpl transMessage = new TransApiMessageImpl();
		RiskResult riskResult = transMessage.singleEvaluate(transaction, syncFlag, timeout, actionCode, interfaceFlag);
		LogHolder.flush(logger);
		return riskResult;
	}

	private static RiskResult toolBatchTransaction(Batch batch, List<Transaction> transactions, boolean useFlag, boolean syncFlag, int timeout, String actionCode, String interfaceFlag) {
		if (batch == null) {
			return errorResult("The batch object is null.", StaticParameter.TMS_DATA_ERROR);
		}
		batch.getOperatCycle().setPinTime(OperatCycle.INDEX_CALL_API_INTERFACE);
		if (!useFlag) {
			return errorResult("The param 'useFlag' is FALSE or the TMS api is configed close .", StaticParameter.TMS_API_ERROR);
		}
		TmsConfigVo.setFilterFlag(useFlag);
		if (!syncFlag) {
			// 如果是异步确认
			DispatcherExecutor.getInstance().execute(batch, transactions, timeout, actionCode, interfaceFlag);
			return null;
		}

		TransApiMessageImpl transMessage = new TransApiMessageImpl();
		RiskResult riskResult = transMessage.batchEvaluate(batch, transactions, syncFlag, timeout, actionCode, interfaceFlag);
		return riskResult;
	}

	private static RiskResult errorResult(String errorInfo, String backCode) {
		RiskResult riskResult = new RiskResult();
		riskResult.setBackCode(backCode);
		errorInfo = DateUtil.dateConvert(new Date(), "yyyy-MM-dd HH:mm:ss") + " " + errorInfo;
		riskResult.setBackInfo(errorInfo);
		logger.info(errorInfo);
		return riskResult;
	}

	/**
	 * 签约数据更新接口
	 * 
	 * @param tableName
	 *            源数据表名
	 * @param fields
	 *            更新字段
	 * @param pkName
	 *            主键名称
	 * @param timeout
	 *            超时时间(毫秒) ，如果为0，则以tmsServer.properties中的timeout为准
	 */
	public static void updateUser(User user, int timeout) {
		try {
			TransApiMessageImpl transMessage = new TransApiMessageImpl();
			String body = transMessage.composeSignDataBody(user);
			String head = transMessage.composeHead(StaticParameter.UPDATE_SIGN_DATA, body.getBytes("UTF-8").length);
			logger.info("请求报文:" + head + body);
			transMessage.getMessageUtil().sendMessage(null, user.getCstNo(), head, body, timeout, null);
		} catch (Exception e) {

		}
	}

	/**
	 * 风险评估服务监听接口
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	public static RiskResult riskMonitor(String ip, String port) {
		try {
			TransApiMessage transMessage = new TransApiMessageImpl();
			Transaction transaction = new Transaction();
			transaction.setTransTime(DateUtil.dateConvert(new Date(), "yyyy-MM-dd HH:mm:ss"));

			String body = transMessage.composeRiskEvaluatBody(transaction, true);
			String head = transMessage.composeHead(StaticParameter.RISK_MONITOR, body.getBytes("UTF-8").length);
			// 发送报文，返回结果
			String resultInfo = transMessage.riskMonitor(ip, port, head, body);
			if (!StringUtil.isBlank(resultInfo)) {
				return transMessage.getRiskResult(resultInfo);
			}
		} catch (Exception e) {
			return errorResult(e.getMessage(), StaticParameter.ERR_UNKNOWN);
		}
		return errorResult(" Server:" + ip + ":" + port + " is down.", StaticParameter.ERR_SERVICE_NOFOUND);
	}

	/**
	 * @param actionCode
	 *            接口ID
	 * @param transCode
	 *            交易流水号
	 * @param data
	 *            通用交易信息对象
	 * @param useFlag
	 *            是否启用风险监控
	 * @param syncFlag
	 *            是否同步
	 * @param timeout
	 *            超时时间(毫秒)，为0则取配置文件配置的超时时间
	 * @param ip
	 *            发送的服务器IP，为空则取配置文件配置的服务器
	 * @param port
	 *            发送的服务器端口，为空则取配置文件配置的服务器
	 */
	public static RiskResult callTmsServer(String actionCode, String transCode, Map<String, Object> data, boolean useFlag, boolean syncFlag, int timeout, String ip, String port) {
		RiskResult riskResult = null;
		StringBuffer sendMes = new StringBuffer("[DEBUG][cn.com.higinet.tms.api.TmsApi]- ");
		List<String> debugLogs = new ArrayList<String>();
		try {
			boolean apiFlag = TmsConfigVo.getApiFlag();
			if (useFlag && apiFlag) {// 判断是否启用风险监控
				TransApiMessage transMessage = new TransApiMessageImpl();
				String body = transMessage.composeBody(transCode, data, syncFlag);
				String head = transMessage.composeHead(actionCode, body.getBytes("UTF-8").length);

				logger.info(head);
				logger.info(body);

				sendMes.append("Send Message: ");

				// if (logger.isLoggable(Level.INFO)) {
				if (logger.isInfoEnabled()) {
					for (String key : data.keySet()) {
						sendMes.append(key + "=" + data.get(key));
						sendMes.append(",");
					}
					sendMes = sendMes.deleteCharAt(sendMes.length() - 1);
					debugLogs.add(sendMes.toString());
				}

				SocketClient client = null;
				if (!StringUtil.isBlank(ip) && !StringUtil.isBlank(port)) {
					// List<String> port_list = TmsConfigVo.getPortList();
					// List<String> ip_list = TmsConfigVo.getServerList();
					// int index = 0;
					// for (int i = 0, len = ip_list.size(); i < len; i++) {
					// if (ip.equals(ip_list.get(i)) && port.equals(port_list.get(i))) {
					// index = i;
					// break;
					// }
					// }
					client = new SocketClient(ip, Integer.parseInt(port), TmsConfigVo.getConnectTimeOut());
				}

				String resultInfo = transMessage.getMessageUtil().sendMessage(null, null, head, body, timeout, client);// 发送报文信息

				if (resultInfo == null || "".equals(resultInfo)) {
					String errorInfo = "Call the TMS server faild.";
					riskResult = new RiskResult(StaticParameter.TMS_SERVER_ERROR, errorInfo);
					debugLogs.add("[DEBUG][cn.com.higinet.tms.api.TmsApi]- " + errorInfo);
					logger.info(errorInfo);
				} else {
					riskResult = transMessage.getRiskResult(resultInfo);// 解析返回报文，组装RiskResult对象
					debugLogs.add("[DEBUG][cn.com.higinet.tms.api.TmsApi]- Recive Message: " + resultInfo);
				}

			} else {
				String errorInfo = "The TMS api is disabled.The apiFlag maybe false.";
				riskResult = new RiskResult(StaticParameter.TMS_API_ERROR, errorInfo);
				debugLogs.add("[DEBUG][cn.com.higinet.tms.api.TmsApi]- " + errorInfo);
			}
		} catch (Exception e) {
			String errorInfo = "Call the methode callTmsServer faild." + e.getMessage();
			riskResult = new RiskResult(StaticParameter.SYSTEM_ERROR, errorInfo);
			debugLogs.add("[DEBUG][cn.com.higinet.tms.api.TmsApi]- " + errorInfo);
			logger.error(errorInfo);
		}

		// if (logger.isLoggable(Level.INFO)) {
		if (logger.isInfoEnabled()) {
			riskResult.setTestLogs(debugLogs);

		}

		return riskResult;
	}

	/**
	 * 获取分发ID
	 *
	 * @param transaction the transaction
	 * @return dispatch 属性
	 */
	private static String getDispatch(Transaction transaction) {
		if(!StringUtil.isBlank(transaction.getDispatch())){
			return transaction.getDispatch();
		}
		return transaction.getCstNo();
	}
}