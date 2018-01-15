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

import java.util.HashMap;
import java.util.Map;

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
public class SFBalanceQuotaTest {

	/** The Constant transType. */
	private static final String transType = "NPP101";//JST102 IBS001
	private static final MsgSender sender = new MsgSender();
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		AppBase.init();
		for (int i = 0; i < 1; i++) {
			Transaction t = TransactionGenerator.generateRandomTransaction(transType);
			t.setTransTime("2017-07-03 18:00:00.111");
			t.setCstNo("20198906271886");
			t.setIpAddress("192.168.0.1");
			t.setTransType("NPP207");
//			t.setTransCode("628620000021706403766");
			t.setDeviceToken("asdfqwejkjqhrh123u81u4uhuh981239855");
			t.setChannelType("CH01");
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("BALANCE", 12896.0);
			map.put("ORDER_ID", "628620000021706403766");
			map.put("AUTHLEVEL", "MIDD_AUTH");
			map.put("BUSYSRC", "APP");
			map.put("BUSYKIND", "CASH_WITHDR");
			map.put("PAYAMOUNT", "1");
			map.put("USER_RATING", "FIRST_LEVEL");
			map.put("RECCARD", "6228480719380819766");
			t.setExtInfo(map);
			sender.eval(t, true);
			confirm(t);
		}
		System.exit(0);
	}
	
	private static void confirm(Transaction t){
		Transaction _t = new Transaction();
		_t.setCstNo(t.getCstNo());
		_t.setTransCode(t.getTransCode());
		_t.setTransType(t.getTransType());
		_t.setTransTime(t.getTransTime());
		_t.setChannelType(t.getChannelType());
		_t.setIpAddress(t.getIpAddress());
		_t.setDeviceToken(t.getDeviceToken());
		_t.setTransStatus("1");
		sender.confirm(_t, true);
	}
}
