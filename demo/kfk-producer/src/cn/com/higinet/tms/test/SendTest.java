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
package cn.com.higinet.tms.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.com.higinet.tms.core.model.RiskResult;
import cn.com.higinet.tms.core.model.Transaction;
import cn.com.higinet.tms.loadrunner.AppBase;
import cn.com.higinet.tms.loadrunner.MsgSender;
import cn.com.higinet.tms.loadrunner.TransactionGenerator;

// TODO: Auto-generated Javadoc
/**
 * The Class SendTest.
 *
 * @author 王兴
 * @since v1.0
 * @date 2017-4-27
 */
public class SendTest {

	/** The Constant transType. */
	private static final String transType = "NPP888";//JST102 IBS001 pos_cutoff_trans

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws ParseException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws ParseException, InterruptedException {
		long ti = 1613278610201l;
		AppBase.init();
		MsgSender sender = new MsgSender();
		for (int i = 0; i < 1; i++) {
			Transaction t = TransactionGenerator.generateRandomTransaction(transType);
			//			t.setTransCode("5");
			t.setCstNo("20171909941926");
			t.setChannelType("CH02");
			t.setExpCode("1111");
			t.setTransTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(ti)));
			sender.eval(t, true);
			t.setTransStatus("1");
			t.setTransTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(ti)));
			sender.confirm(t, true);
		}
		System.exit(0);
	}
}
