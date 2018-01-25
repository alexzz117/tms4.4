package cn.com.higinet;

import java.util.HashMap;
import java.util.Map;

import cn.com.higinet.tms.api.TmsApi;
import cn.com.higinet.tms.core.common.StaticParameter;
import cn.com.higinet.tms.core.model.RiskResult;
import cn.com.higinet.tms.core.model.Transaction;

public class TestConfirm extends Rule {

	/**
	 * 登录交易 未登录天数超过15天
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public void execute() throws Exception {
		Map<String, Object> ext = new HashMap<String, Object>() {
			{
				put("RECCARD", "123141234");
				//put("CONFIRMRISK", "1");
			}
		};

		RiskResult result = null;
		Transaction transaction = new Transaction();
		transaction.setTransType("NPP201");
		transaction.setCstNo("10100919651924");
		transaction.setTransStatus("1");
		//transaction.setTransCode("4a8c164bbd63445fb44661001dba3faa");
		transaction.setTransCode("1");
		transaction.setExtInfo(ext);

		result = TmsApi.updateTransaction(transaction, useFlag, syncFlag, 1000);
		printResult(result);
	}

	public static void main(String[] args) throws Exception {
		System.setProperty(StaticParameter.TMS_API_PATH, "classpath:properties");
		long s = System.currentTimeMillis();
		new TestConfirm().execute();
		System.out.println("-----time-----" + (System.currentTimeMillis() - s));
	}

}
