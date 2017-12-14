package cn.com.higinet.tms.core.thread;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.core.common.LogHolder;
import cn.com.higinet.tms.core.common.StaticParameter;
import cn.com.higinet.tms.core.model.Batch;
import cn.com.higinet.tms.core.model.Transaction;
import cn.com.higinet.tms.core.service.TransApiMessage;
import cn.com.higinet.tms.core.service.impl.TransApiMessageImpl;

public class WorkThread implements Runnable {

	// private final static Logger logger = LogManager.getLogger(WorkThread.class);
	private static Logger logger = LoggerFactory.getLogger(WorkThread.class);

	Transaction transaction = null;
	Batch batch = null;
	List<Transaction> transactions = null;
	int timeout = 0;
	String actionCode = "";
	String interfaceFlag = "";

	// 获取业务数据
	public WorkThread(Transaction transaction, Batch batch, List<Transaction> transactions, int timeout, String actionCode, String interfaceFlag) {
		this.transaction = transaction;
		this.batch = batch;
		this.transactions = transactions;
		this.timeout = timeout;
		this.actionCode = actionCode;
		this.interfaceFlag = interfaceFlag;
	}

	public void run() {
		LogHolder.reset();
		LogHolder.put(LogHolder.TXN_ID, interfaceFlag);
		TransApiMessage transMessage = new TransApiMessageImpl();
		if (interfaceFlag.equals(StaticParameter.RISK_INTERFACE_EVALUATE) || StaticParameter.RISK_INTERFACE_CONFIRM.equals(interfaceFlag)) {
			transMessage.singleEvaluate(transaction, false, timeout, actionCode, interfaceFlag);
		} else if (interfaceFlag.equals(StaticParameter.RISK_INTERFACE_BATCH_EVALUATE) || interfaceFlag.equals(StaticParameter.RISK_INTERFACE_BATCH_CONFIRM)) {
			transMessage.batchEvaluate(batch, transactions, false, timeout, actionCode, interfaceFlag);
		}
		LogHolder.flush(logger);
	}
}