/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.tms.core.model.Batch;
import cn.com.higinet.tms.core.model.RiskResult;
import cn.com.higinet.tms.core.model.Transaction;
import cn.com.higinet.tms.core.model.User;
/**
 * 
 * @author zhangfg
 * @version 3.0.0
 * @date 2012-06-12
 * @description 采用CSP_API方式的报文服务接口
 */
public interface TransApiMessage {
	/**
	 * 获取报文工具类对象
	 * @return
	 */
	public MessageService getMessageUtil();
	/**
	 * 单条评估
	 * @param transaction
	 * @param syncFlag
	 * @param timeOut
	 * @param actionCode
	 * @param interfaceFlag
	 * @return
	 */
	public RiskResult singleEvaluate(Transaction transaction, boolean syncFlag, int timeOut, String actionCode, String interfaceFlag);
	/**
	 * 批量评估
	 * @param batch
	 * @param transactions
	 * @param syncFlag
	 * @param timeOut
	 * @param actionCode
	 * @param interfaceFlag
	 * @return
	 */
	public RiskResult batchEvaluate(Batch batch,List<Transaction> transactions, boolean syncFlag, int timeOut, String actionCode, String interfaceFlag);
	
	/**
	 * 组装报文头
	 * @param actionCode 操作代码
	 * @param bodyLength 报文体长度
	 * @return
	 */
	public String composeHead(String actionCode, int bodyLength);
	
	/**
	 * 组装报文头
	 * @param actionCode 操作代码
	 * @param dispatch	   分发凭证
	 * @param bodyLength 报文体长度
	 * @return
	 */
	public String composeHead(String actionCode, String dispatch, int bodyLength);
	/**
	 * 组装风险评估报文体
	 * @param transaction
	 * @param syncFlag
	 * @return
	 */
	public String composeRiskEvaluatBody(Transaction transaction,boolean syncFlag);
	
	/**
	 * 组装批量风险评估报文体
	 * @param batch
	 * @param syncFlag
	 * @return
	 */
	public String composeBatchRiskEvaluatBody(Batch batch,List<Transaction> transactions,boolean syncFlag);
	/**
	 * 组装风险查询报文体
	 * @param transaction
	 * @param user
	 * @param device
	 * @return
	 */
	public String composeRiskQueryBody(Transaction transaction);
	/**
	 * 解析返回报文，返回RiskReslut风险结果对象
	 * @param resultMessage
	 * @return
	 */
	public RiskResult getRiskResult(String resultMessage);
	/**
	 * 向Tms服务端发送报文
	 * @param transCode
	 * @param data
	 * @param syncFlag
	 * @return
	 */
	public String composeBody(String transCode, Map<String, Object> data, boolean syncFlag);
	
	
	/**
	 * 组装更新签约数据报文体
	 * @param tableName
	 * @param fields
	 * @param pkName
	 * @return
	 */
	public String composeSignDataBody(User user);
	
	/**
	 * 风险评估服务监听
	 * @param ip
	 * @param port
	 * @param head
	 * @param body
	 * @return
	 */
	public String riskMonitor(String ip,String port,String head, String body);
}