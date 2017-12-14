/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.service.impl;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.core.common.LogHolder;
import cn.com.higinet.tms.core.common.StaticParameter;
import cn.com.higinet.tms.core.model.Account;
import cn.com.higinet.tms.core.model.Batch;
import cn.com.higinet.tms.core.model.Customer;
import cn.com.higinet.tms.core.model.OperatCycle;
import cn.com.higinet.tms.core.model.RiskResult;
import cn.com.higinet.tms.core.model.TmsConfigVo;
import cn.com.higinet.tms.core.model.Transaction;
import cn.com.higinet.tms.core.model.User;
import cn.com.higinet.tms.core.service.MessageService;
import cn.com.higinet.tms.core.service.TransApiMessage;
import cn.com.higinet.tms.core.util.DateUtil;
import cn.com.higinet.tms.core.util.StringUtil;

/**
 * @author zhangfg
 * @version 3.0.0
 * @date 2012-06-12
 * @description 采用TMS_API方式的报文服务实现类
 * 
 */
public class TransApiMessageImpl implements TransApiMessage {

	// static Logger logger = LogManager.getLogger(TransApiMessageImpl.class);
	private static Logger logger = LoggerFactory.getLogger(TransApiMessageImpl.class);
	private static final boolean parseBody = Boolean.valueOf(TmsConfigVo.getProperty("parse.body", "true"));
	public static MessageService messageUtil = null;

	public TransApiMessageImpl() {
		String parserName = TmsConfigVo.getParserName();
		if (StaticParameter.XML_PARSER_SAX.equals(parserName)) {
			messageUtil = new SaxXml();
		} else if (StaticParameter.XML_PARSER_OPEN4J.equals(parserName)) {
			messageUtil = new MessageFastXml();
		} else if (StaticParameter.XML_PARSER_VTD.equals(parserName)) {
			messageUtil = new MessageVtdXml();// vtd 解析器
		} else {
			messageUtil = new MessageVtdXml();
		}
	}

	public MessageService getMessageUtil() {
		return messageUtil;
	}

	public RiskResult singleEvaluate(Transaction transaction, boolean syncFlag, int timeOut, String actionCode, String interfaceFlag) {
		LogHolder.put(LogHolder.TXN_CODE, transaction.getTransCode());
		RiskResult riskResult = null;
		OperatCycle operatCycle = transaction.getOperatCycle();
		try {
			operatCycle.setPinTime(OperatCycle.INDEX_DATA_COMPLETE);
			transaction.full();
			operatCycle.setSync(syncFlag);
			operatCycle.setConfirm(StaticParameter.RISK_CONFIRM.equals(actionCode));

			operatCycle.setPinTime(OperatCycle.INDEX_COMPOSE_MESSAGE_START);
			String body = composeRiskEvaluatBody(transaction, syncFlag);
			String head = composeHead(actionCode, transaction.getDispatch(), body.getBytes("UTF-8").length);
			logger.info("请求报文:" + head + body);
			operatCycle.setPinTime(OperatCycle.INDEX_COMPOSE_MESSAGE_END);
			operatCycle.setSendInfo(head + body);
			String resultInfo = messageUtil.sendMessage(operatCycle, transaction.getDispatch(), head, body, timeOut, null);
			operatCycle.setReciveInfo(resultInfo);
			if (StringUtil.isBlank(resultInfo)) {
				riskResult = errorResult("The return message is null, " + interfaceFlag + " exception.", StaticParameter.SYSTEM_ERROR);
			} else {
				riskResult = getRiskResult(resultInfo);
			}
		} catch (Exception e) {
			logger.error("singleEvaluate exception:", e);
			riskResult = errorResult("Call the method " + interfaceFlag + " faild. ", StaticParameter.TMS_API_FAILD);
		} finally {
			operatCycle.setRiskResult(riskResult);
			if (TmsConfigVo.isTranspinFlag()) {
				logger.info(operatCycle.toString());
			}
		}
		return riskResult;
	}

	public RiskResult batchEvaluate(Batch batch, List<Transaction> transactions, boolean syncFlag, int timeOut, String actionCode, String interfaceFlag) {
		RiskResult riskResult = null;
		OperatCycle operatCycle = batch.getOperatCycle();
		try {
			operatCycle.setPinTime(OperatCycle.INDEX_DATA_COMPLETE);
			batch.full();
			operatCycle.setSync(syncFlag);
			operatCycle.setConfirm(StaticParameter.RISK_CONFIRM_BATCH.equals(actionCode));

			operatCycle.setPinTime(OperatCycle.INDEX_COMPOSE_MESSAGE_START);
			String body = composeBatchRiskEvaluatBody(batch, transactions, syncFlag);
			String head = composeHead(actionCode, body.getBytes("UTF-8").length);

			logger.info("请求报文:" + head + body);

			operatCycle.setPinTime(OperatCycle.INDEX_COMPOSE_MESSAGE_END);
			operatCycle.setSendInfo(head + body);
			String resultInfo = messageUtil.sendMessage(operatCycle, batch.getCstNo(), head, body, timeOut, null);
			operatCycle.setReciveInfo(resultInfo);
			if (StringUtil.isBlank(resultInfo)) {
				riskResult = errorResult("The return message is null, " + interfaceFlag + " exception.", StaticParameter.SYSTEM_ERROR);
			} else {
				riskResult = getRiskResult(resultInfo);
			}
		} catch (Exception e) {
			logger.error("batchEvaluate exception:", e);
			riskResult = errorResult("Call the method batch " + interfaceFlag + " faild. " + e.getMessage(), StaticParameter.TMS_API_FAILD);
		} finally {
			operatCycle.setRiskResult(riskResult);
			if (TmsConfigVo.isTranspinFlag()) {
				logger.info(operatCycle.toString());
			}
		}
		return riskResult;
	}

	private static RiskResult errorResult(String errorInfo, String backCode) {
		RiskResult riskResult = new RiskResult();
		riskResult.setBackCode(backCode);
		riskResult.setBackInfo(errorInfo);
		return riskResult;
	}

	/**
	 * 组装报文头
	 * 
	 * @param actionCode
	 *            操作代码
	 * @param bodyLength
	 *            报文体长度
	 * @return
	 */
	public String composeHead(String actionCode, int bodyLength) {
		StringBuffer sb = new StringBuffer();
		if (actionCode != null && !"".equals(actionCode)) {
			String len = String.valueOf(bodyLength);
			len = "00000000".substring(len.length()) + len;
			sb.append(len);// 报文体长度
			sb.append("TMS").append(" ").append(" ").append(" ").append(" ").append(" ");// 服务号
			sb.append(actionCode);// 交易号
			sb.append(StaticParameter.MESSAGE_TYPE).append(" ");// 报文体类型
			sb.append(" ").append(" ").append(" ").append(" ").append(" ").append(" ").append(" ").append(" ");// 返回码
		}
		return sb.toString();
	}

	/**
	 * 组装报文头
	 * 
	 * @param actionCode
	 *            操作代码
	 * @param bodyLength
	 *            报文体长度
	 * @return
	 */
	public String composeHead(String actionCode, String dispatch, int bodyLength) {
		StringBuffer sb = new StringBuffer();
		if (actionCode != null && !"".equals(actionCode)) {
			int dispatchLen = 32;
			String len = String.valueOf(bodyLength + dispatchLen);
			len = "00000000".substring(len.length()) + len;
			sb.append(len);// 报文体长度
			sb.append("TMS").append(" ").append(" ").append(" ").append(" ").append(" ");// 服务号
			sb.append(actionCode);// 交易号
			sb.append(StaticParameter.MESSAGE_TYPE).append(" ");// 报文体类型
			sb.append(" ").append(" ").append(" ").append(" ").append(" ").append(" ").append(" ").append(" ");// 返回码
			sb.append(composeHeadByDispatch(dispatch, dispatchLen));
		}
		return sb.toString();
	}

	public String composeHeadByDispatch(String dispatch, int dispLen) {
		if (StringUtil.isBlank(dispatch)) {
			dispatch = "00000000";
		}
		dispatch = String.format("%-" + dispLen + "s", dispatch);
		try {
			byte[] bs = dispatch.getBytes("UTF-8");
			if (bs.length > dispLen) {
				byte[] tmp = new byte[dispLen];
				System.arraycopy(bs, 0, tmp, 0, dispLen);
				dispatch = new String(tmp);
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("composeHeadByDispatch exception:", e);
			e.printStackTrace();
		}
		return dispatch;
	}

	/**
	 * 组装风险评估报文体
	 * 
	 * @param transaction
	 * @return
	 */
	public String composeRiskEvaluatBody(Transaction transaction, boolean syncFlag) {
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(messageUtil.getXmlHead());
			sb.append("<SYNCFLAG>").append(syncFlag ? "1" : "0").append("</SYNCFLAG>");
			sb.append(composeTranscode(transaction));
			sb.append(composeTransaction(transaction));
			sb.append("</Message>");
		} catch (Exception e) {
			logger.error("The method 'ComposeRiskEvaluatBody' of the 'TransApiMessage' class exception.", e);
		}
		return sb.toString();
	}

	/**
	 * 组装风险确认接口报文体
	 * 
	 * @param transaction
	 * @return
	 */
	public String composeRiskConfirmBody(Transaction transaction, boolean syncFlag) {

		StringBuffer sb = new StringBuffer();
		try {
			sb.append(messageUtil.getXmlHead());
			sb.append("<SYNCFLAG>").append(syncFlag ? "1" : "0").append("</SYNCFLAG>");
			sb.append(composeTranscode(transaction));

			sb.append(StringUtil.appendXmlMessage("TXNID", transaction.getTransType()));
			sb.append(StringUtil.appendXmlMessage("TXNSTATUS", transaction.getTransStatus()));
			sb.append(StringUtil.appendXmlMessage("USERID", transaction.getCstNo()));
			// sb.append(StringUtil.appendXmlMessage("EXPCODE", transaction.getExpCode()));
			// sb.append(StringUtil.appendXmlMessage("AUTHSTATUS", transaction.getAuthStatus()));
			sb.append("</Message>");
		} catch (Exception e) {
			logger.error("The method 'composeRiskConfirmBody' of the 'TransApiMessage' class exception.", e);
		}
		return sb.toString();
	}

	/**
	 * 组装批量风险评估报文体
	 * 
	 * @param batch
	 * @param syncFlag
	 * @return
	 */
	public String composeBatchRiskEvaluatBody(Batch batch, List<Transaction> transactions, boolean syncFlag) {
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(messageUtil.getXmlHead());
			sb.append("<syncFlag>").append(syncFlag ? "1" : "0").append("</syncFlag>");
			sb.append(composeBatch(batch, transactions));
			sb.append("</Message>");
		} catch (Exception e) {
			logger.error("The method 'composeBatchRiskEvaluatBody' of the 'TransApiMessage' class exception.", e);
		}
		return sb.toString();
	}

	/**
	 * 拼装批量交易汇总信息
	 * 
	 * @param batch
	 * @return
	 */
	private StringBuffer composeBatch(Batch batch, List<Transaction> transactions) {

		StringBuffer sb = new StringBuffer();
		sb.append("<TransInfo type='map'>");
		sb.append(StringUtil.appendXmlMessage("TXNCODE", batch.getBatchNo()));
		sb.append(StringUtil.appendXmlMessage("TXNTIME", batch.getTransTime()));
		sb.append(StringUtil.appendXmlMessage("TXNID", batch.getTransType()));
		sb.append(StringUtil.appendXmlMessage("TXNSTATUS", batch.getTransStatus()));
		sb.append(StringUtil.appendXmlMessage("CHANCODE", batch.getChannelType()));
		sb.append(StringUtil.appendXmlMessage("USERID", batch.getCstNo()));
		sb.append(StringUtil.appendXmlMessage("SESSIONID", batch.getSessionId()));
		sb.append(StringUtil.appendXmlMessage("IPADDR", batch.getIpAddress()));
		sb.append(StringUtil.appendXmlMessage("batchName", batch.getBatchName()));
		sb.append(StringUtil.appendXmlMessage("totalAmount", batch.getTotalAmount()));
		sb.append(StringUtil.appendXmlMessage("totalCount", batch.getTotalCount()));
		sb.append(StringUtil.appendXmlMessage("payAccount", batch.getPayAccount()));
		sb.append(StringUtil.appendXmlMessage("batchNo", batch.getBatchNo()));
		sb.append("</TransInfo>");
		// sb.append("<extInfo type='map'>");
		// sb.append(StringUtil.appendXmlMessage("batchNo",batch.getBatchNo()));
		// sb.append("</extInfo>");

		if (transactions != null && transactions.size() > 0) {
			sb.append("<TransList type='list'>");
			for (int i = 0; i < transactions.size(); i++) {
				sb.append("<Trans type='map'>");
				Transaction transaction = transactions.get(i);
				try {
					transaction.full();
					// 为每笔交易设置ip地址，与批量总信息的ip地址相同
					transaction.setIpAddress(batch.getIpAddress());
					// 将批次号放到扩展信息Map中
					Map<String, Object> extInfo = transaction.getExtInfo();
					if (extInfo == null) {
						extInfo = new HashMap<String, Object>();
					}
					extInfo.put("batchNo", batch.getBatchNo());
					// 为每个transaction的扩展信息中加入批次号
					transaction.setExtInfo(extInfo);
					sb.append(composeTranscode(transaction));
					sb.append(composeTransaction(transaction));
				} catch (Exception e) {
					logger.info(DateUtil.dateConvert(new Date(), "yyyy-MM-dd HH:mm:ss") + "The transaction that its transCode is " + transaction.getTransCode() + " occured exception. " + e.getMessage());
					continue;
				}

				sb.append("</Trans>");
			}
			sb.append("</TransList>");
		}

		return sb;
	}

	/**
	 * 将transCode单独作为一个节点，无论是API方式还是应用探针方式都有transCode节点， 同时判断transCode，如果为null，转换成“”
	 * 
	 * @param transaction
	 * @return
	 */
	private StringBuffer composeTranscode(Transaction transaction) {
		StringBuffer sb = new StringBuffer();
		if (transaction != null) {
			sb = StringUtil.appendXmlMessage("TXNCODE", StringUtil.nullToEmpty(transaction.getTransCode()));
		}
		return sb;
	}

	/**
	 * 拼装交易信息
	 * 
	 * @param transaction
	 * @return
	 */
	private StringBuffer composeTransaction(Transaction transaction) {
		StringBuffer sb = new StringBuffer();
		if (transaction != null) {
			String finger = transaction.getDeviceFinger();

			sb.append(StringUtil.appendXmlMessage("TXNTIME", transaction.getTransTime()));
			sb.append(StringUtil.appendXmlMessage("TXNSTATUS", transaction.getTransStatus()));
			sb.append(StringUtil.appendXmlMessage("TXNID", transaction.getTransType()));
			sb.append(StringUtil.appendXmlMessage("CHANCODE", transaction.getChannelType()));
			sb.append(StringUtil.appendXmlMessage("AUTHSTATUS", transaction.getAuthStatus()));
			sb.append(StringUtil.appendXmlMessage("USERID", transaction.getCstNo()));
			sb.append(StringUtil.appendXmlMessage("SESSIONID", transaction.getSessionId()));
			sb.append(StringUtil.appendXmlMessage("DEVICEID", transaction.getDeviceId()));
			sb.append(StringUtil.appendXmlMessage("IPADDR", transaction.getIpAddress()));
			sb.append(StringUtil.appendXmlMessage("DEVICEFINGER", finger));
			sb.append(StringUtil.appendXmlMessage("DEVICETOKEN", transaction.getDeviceToken()));
			// sb.append(StringUtil.appendNullNode("DEVICETOKEN", transaction.getDeviceToken()));
			sb.append(StringUtil.appendXmlMessage("DISPATCH", transaction.getDispatch()));
			sb.append(StringUtil.appendXmlMessage("EXPCODE", transaction.getExpCode()));
			Customer customer = transaction.getCustomer();
			if (customer != null) {
				sb.append("<Customer type='map'>");
				sb.append(StringUtil.appendXmlMessage("cstNo", customer.getCstNo()));
				sb.append(StringUtil.appendXmlMessage("userName", customer.getUserName()));
				sb.append(StringUtil.appendXmlMessage("cusName", customer.getCusName()));
				sb.append(StringUtil.appendXmlMessage("ctfType", customer.getCtfType()));
				sb.append(StringUtil.appendXmlMessage("ctfNo", customer.getCtfNo()));
				sb.append(StringUtil.appendXmlMessage("phone", customer.getPhone()));
				sb.append(StringUtil.appendXmlMessage("openNode", customer.getOpenNode()));
				sb.append(StringUtil.appendXmlMessage("Sex", customer.getSex()));
				sb.append(StringUtil.appendXmlMessage("birthday", customer.getBirthday()));
				sb.append(StringUtil.appendXmlMessage("accType", customer.getAccType()));
				sb.append(StringUtil.appendXmlMessage("openTime", customer.getOpenTime()));
				sb.append("</Customer>");

				Account account = customer.getAccount();
				// List<User> userList = customer.getUsers();

				if (account != null) {
					// 拼装单个账号信息
					sb.append(composeAccountInfo(account));
				}

				// 循环拼装用户信息
				// if (userList != null && userList.size() > 0)
				// {
				// sb.append("<Users type='list'>");
				// // 循环拼装账号信息
				// for (int i = 0; i < userList.size(); i++)
				// {
				// User user = userList.get(i);
				// if (user != null)
				// {
				// // 拼装单个用户信息
				// sb.append(composeUserInfo(user));
				// }
				// }
				// sb.append("</Users>");
				// }

			}
			// 用户信息报文拼装完成

			// 拼装扩展信息
			sb.append(composeDiyInfo(transaction));// 拼装扩展信息报文
			// 交易信息拼装完成
		}
		return sb;
	}

	/**
	 * 拼装账号信息
	 * 
	 * @param account
	 * @return
	 */
	private StringBuffer composeAccountInfo(Account account) {
		StringBuffer str = new StringBuffer();
		str.append("<Account type='map'>");

		str.append(StringUtil.appendXmlMessage("accNo", account.getAccNo()));
		str.append(StringUtil.appendXmlMessage("cstNo", account.getCstNo()));
		str.append(StringUtil.appendXmlMessage("openNode", account.getOpenNode()));
		str.append(StringUtil.appendXmlMessage("openTime", account.getOpenTime()));
		str.append(StringUtil.appendXmlMessage("openNodeCity", account.getOpenNodeCity()));
		str.append(StringUtil.appendXmlMessage("balance", account.getBalance()));

		str.append("</Account>");
		return str;
	}

	/**
	 * 拼装用户信息
	 * 
	 * @param account
	 * @return
	 */
	private StringBuffer composeUserInfo(User user) {
		StringBuffer sb = new StringBuffer();

		if (user != null) {
			sb.append("<Customer type='map'>");
			sb.append(StringUtil.appendXmlMessage("cstNo", user.getCstNo()));
			sb.append(StringUtil.appendXmlMessage("userName", user.getUserName()));
			sb.append(StringUtil.appendXmlMessage("cusName", user.getCusName()));
			sb.append(StringUtil.appendXmlMessage("ctfType", user.getCtfType()));
			sb.append(StringUtil.appendXmlMessage("ctfNo", user.getCtfNo()));
			sb.append(StringUtil.appendXmlMessage("phone", user.getPhone()));
			sb.append(StringUtil.appendXmlMessage("openNode", user.getOpenNode()));
			sb.append(StringUtil.appendXmlMessage("Sex", user.getSex()));
			sb.append(StringUtil.appendXmlMessage("birthday", user.getBirthday()));
			sb.append(StringUtil.appendXmlMessage("accType", user.getAccType()));
			sb.append(StringUtil.appendXmlMessage("openTime", user.getOpenTime()));

			List<Account> accountList = user.getAccounts();
			// 循环拼装账号信息
			if (accountList != null && accountList.size() > 0) {
				sb.append("<Accounts type='list'>");
				for (int i = 0; i < accountList.size(); i++) {
					Account account = accountList.get(i);
					if (account != null) {
						// 拼装单个账号信息
						sb.append(composeAccountInfo(account));
					}
				}
				sb.append("</Accounts>");
			}

			sb.append("</Customer>");
		}
		return sb;
	}

	/**
	 * 组装扩展信息报文
	 * 
	 * @param transaction
	 * @return
	 */
	private StringBuffer composeDiyInfo(Transaction transaction) {
		StringBuffer tmp = new StringBuffer();
		StringBuffer strB = new StringBuffer();
		boolean flag = false;

		if (transaction != null && transaction.getExtInfo() != null) {
			flag = true;
			tmp.append(messageUtil.getXmlExtMap(transaction.getExtInfo()));
		}
		if (flag) {
			strB.append(tmp);
		}
		return strB;
	}

	/**
	 * 组装风险查询报文体
	 * 
	 * @param transaction
	 * @return
	 */
	public String composeRiskQueryBody(Transaction transaction) {
		return null;
	}

	/**
	 * 解析返回报文，组装RiskResult对象，并返回
	 * 
	 * @param resultMessage
	 * @return
	 */
	public RiskResult getRiskResult(String resultMessage) {
		RiskResult riskResult = null;
		try {
			if (resultMessage != null) {
				logger.info("返回报文:" + resultMessage);
				riskResult = new RiskResult();
				byte[] bm = resultMessage.getBytes();
				byte[] head = new byte[StaticParameter.HEAD_LEN];
				// 截取报文头
				System.arraycopy(bm, StaticParameter.MESSAGE_LEN_LEN, head, 0, StaticParameter.HEAD_LEN);
				String headStr = new String(head);
				// 从报文头中截取返回码
				String backCode = headStr.substring(headStr.length() - 8);

				String bodyStr = resultMessage.substring(StaticParameter.MESSAGE_LEN_LEN + StaticParameter.HEAD_LEN);

				// 解析返回报文体
				if (parseBody&&bodyStr.length() > 0)
					messageUtil.deCodeBodyXml(riskResult, bodyStr, "");

				riskResult.setBackCode(backCode);
				if (riskResult.getErrorInfo() == null)
					riskResult.setErrorInfo(messageUtil.getBackInfo(backCode));

				return riskResult;
			}
		} catch (Exception e) {
			logger.error(DateUtil.dateConvert(new Date(), "yyyy-MM-dd HH:mm:ss") + "The method 'getRiskResult' of the 'TransApiMessage' class exception.", e);
			riskResult = new RiskResult();
			riskResult.setBackCode(StaticParameter.MESSAGE_ERROR);
			riskResult.setBackInfo("报文解析错误。" + resultMessage);
		}
		return riskResult;
	}

	public String composeBody(String transCode, Map<String, Object> data, boolean syncFlag) {
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(messageUtil.getXmlHead());
			sb.append("<SYNCFLAG>").append(syncFlag ? "1" : "0").append("</SYNCFLAG>");
			sb.append("<TXNCODE>").append(StringUtil.nullToEmpty(transCode)).append("</TXNCODE>");
			if (data != null && !data.isEmpty()) {
				sb.append(messageUtil.getXmlExtMap(data));
			}
			sb.append("</Message>");
		} catch (Exception e) {
			logger.error("The method 'composeBody' of the 'TransApiMessage' class exception.", e);
		}
		return sb.toString();
	}

	public String composeSignDataBody(User user) {
		StringBuffer sb = new StringBuffer();
		sb.append(messageUtil.getXmlHead());

		sb.append(composeUserInfo(user));
		sb.append("</Message>");
		return sb.toString();
	}

	/**
	 * 风险评估服务监听
	 * 
	 * @param ip
	 * @param port
	 * @param head
	 * @param body
	 * @return
	 */
	public String riskMonitor(String ip, String port, String head, String body) {
		int timeOut = TmsConfigVo.getTimeOut();
		String charsetName = "UTF-8";
		StringBuffer sb = new StringBuffer();
		Socket socket = null;
		DataOutputStream dos = null;
		InputStream dis = null;

		try {
			byte[] b = head.getBytes();
			byte[] ws = body.getBytes(charsetName);
			socket = new Socket();
			// 连接超时和read超时应用一个数据项 P302
			socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), timeOut);
			if (socket.isConnected()) {
				socket.setSoTimeout(timeOut);
				socket.setTcpNoDelay(true);
				dos = new DataOutputStream(socket.getOutputStream());

				String len = String.valueOf(ws.length);
				len = "00000000".substring(len.length()) + len;
				System.arraycopy(len.getBytes(), 0, b, 0, 8);

				dos.write(b);
				dos.write(ws);
				dos.flush();

				dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

				// 取返回数据长度
				byte[] rhead = new byte[StaticParameter.HEAD_LEN];
				int len1 = dis.read(rhead);
				// 判断报文头是否正确
				if (len1 >= 0) {
					// 得到报文体长度
					int resMsgLen = Integer.parseInt(new String(rhead, 0, 8, charsetName));
					byte[] rbody = new byte[resMsgLen];
					int readedLen = 0, i = 0;
					while (readedLen < resMsgLen) {
						i = dis.read(rbody, readedLen, resMsgLen - readedLen);
						readedLen += i;
						// TODO 如果报文大于8192(BufferedInputStream 的默认长度)
						if (i < 0)
							break;
					}

					sb.append(new String(rhead, 0, StaticParameter.HEAD_LEN, charsetName));
					sb.append(new String(rbody, 0, resMsgLen, charsetName));
				}
			}
		} catch (Exception e) {
			logger.error("riskMonitor exception. ", e);
		} finally {
			try {
				if (dis != null)
					dis.close();
				if (dos != null)
					dos.close();
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				logger.error("socket close faild.", e);
			}
		}

		return sb.toString();
	}
}