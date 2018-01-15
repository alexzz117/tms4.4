/** 
 * @file 
 * @brief brief description 
 * @author 王兴 
 * [@author-desc 宏基恒信深圳研发中心] 
 * @date 2017-4-29
 * @version v1.0
 * @note 
 * detailed description 
 */
package cn.com.higinet.tms.loadrunner;

import cn.com.higinet.tms.api.TmsApi;
import cn.com.higinet.tms.core.common.StaticParameter;
import cn.com.higinet.tms.core.model.RiskResult;
import cn.com.higinet.tms.core.model.Transaction;
import cn.com.higinet.tms.core.service.MessageService;
import cn.com.higinet.tms.core.service.impl.MessageFastXml;
import cn.com.higinet.tms.core.service.impl.TransApiMessageImpl;

// TODO: Auto-generated Javadoc
/**
 * The Class MsgSender.
 *
 * @author 王兴
 * @since v1.0
 * @date 2017-4-27
 */
public class MsgSender {

	/** The use flag. */
	private boolean useFlag = true;// 启用标识，开启/关闭API或应用探针 true开 false关

	/** The sync flag. */
	private boolean syncFlag = true;// 同步标志 true同步 false异步

	/** The timeout. */
	private int timeout = 3000;// 超时时间(毫秒)

	/**
	 * Checks if is use flag.
	 *
	 * @return true, if is use flag
	 */
	public boolean isUseFlag() {
		return useFlag;
	}

	/**
	 * Sets the use flag.
	 *
	 * @param useFlag the new use flag
	 */
	public void setUseFlag(boolean useFlag) {
		this.useFlag = useFlag;
	}

	/**
	 * Checks if is sync flag.
	 *
	 * @return true, if is sync flag
	 */
	public boolean isSyncFlag() {
		return syncFlag;
	}

	/**
	 * Sets the sync flag.
	 *
	 * @param syncFlag the new sync flag
	 */
	public void setSyncFlag(boolean syncFlag) {
		this.syncFlag = syncFlag;
	}

	/**
	 * Gets the timeout.
	 *
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the new timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Send.
	 *
	 * @param transaction the transaction
	 */
	public void send(Transaction transaction) {
		RiskResult result = null;
		result = TmsApi.riskEvaluate(transaction, useFlag, syncFlag, timeout);
		printResult(result);
		transaction.setTransStatus("1");
		result = TmsApi.updateTransaction(transaction, useFlag, false, timeout);
		printResult(result);
	}

	/**
	 * Eval.
	 *
	 * @param transaction the transaction
	 * @param syncFlag the sync flag
	 * @return the risk result
	 */
	public RiskResult eval(Transaction transaction, boolean syncFlag) {
		return TmsApi.riskEvaluate(transaction, useFlag, syncFlag, timeout);
	}

	/**
	 * Confirm.
	 *
	 * @param transaction the transaction
	 * @param syncFlag the sync flag
	 * @return the risk result
	 */
	public RiskResult confirm(Transaction transaction, boolean syncFlag) {
		return TmsApi.updateTransaction(transaction, useFlag, syncFlag, timeout);
	}

	public RiskResult sendText(String userId, String head, String body, boolean syncFlag) {
		Transaction a = new Transaction();
		MessageService messageUtil = new MessageFastXml();
		return new TransApiMessageImpl().getRiskResult(messageUtil.sendMessage(a.getOperatCycle(), userId, head, body, 1000, null));
	}

	/**
	 * Result to string.
	 *
	 * @param result the result
	 * @return the string
	 */
	public static String resultToString(RiskResult result) {
		if (result == null)
			return null;
		if (StaticParameter.SYSTEM_SUCCESS.equals(result.getBackCode())) {
			StringBuilder sb = new StringBuilder();
			sb.append("txnId:").append(result.getTxnId()).append(", txnName:").append(result.getTxnName()).append(", txnCode:").append(result.getTransCode()).append(", \nalertId:").append(result.getRiskId()).append(", disposal:").append(result.getDisposal()).append(", hitRuleNum:").append(result.getHitRuleNum()).append(", trigRuleNum:").append(result.getTrigRuleNum()).append(", \nprocessInfo:{")
					.append(result.getProcessInfo()).append("}").append(", \nswitchInfo:{").append(result.getSwitchInfo()).append("}").append(", \nactionInfo:{").append(result.getActionInfo()).append("}").append(", \nhitRules:{").append(result.getHitRules()).append("}");
			return sb.toString();
		}
		return "返回码：" + result.getBackCode() + ", 错误信息：" + result.getBackInfo();
	}

	/**
	 * Prints the result.
	 *
	 * @param result the result
	 */
	public static void printResult(RiskResult result) {
		System.out.println(resultToString(result));
	}
}
