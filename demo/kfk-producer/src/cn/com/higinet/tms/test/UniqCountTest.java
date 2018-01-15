package cn.com.higinet.tms.test;

import cn.com.higinet.tms.api.TmsApi;
import cn.com.higinet.tms.core.model.RiskResult;
import cn.com.higinet.tms.core.model.Transaction;
import cn.com.higinet.tms.loadrunner.AppBase;
import cn.com.higinet.tms.loadrunner.MsgSender;
import cn.com.higinet.tms.loadrunner.TransactionGenerator;

public class UniqCountTest {
	/** The Constant transType. */
	private static final String transType = "NPP101";//JST102 IBS001

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		AppBase.init();
		MsgSender sender = new MsgSender();
		Transaction t = TransactionGenerator.generateRandomTransaction(transType);
		t.setCstNo("123456789");
		t.setIpAddress("72.137.202.128");
		sender.eval(t, true);
		t.setTransStatus("0");
		sender.confirm(t, false);
		System.exit(0);
	}
}
