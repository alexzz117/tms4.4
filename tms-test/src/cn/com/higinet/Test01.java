package cn.com.higinet;

import cn.com.higinet.tms.api.TmsApi;
import cn.com.higinet.tms.core.common.StaticParameter;
import cn.com.higinet.tms.core.model.RiskResult;
import cn.com.higinet.tms.core.model.Transaction;

public class Test01 extends Rule {

	/**
	 * 登录交易 未登录天数超过15天
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public void execute() throws Exception {
		RiskResult result = null;
		Transaction transaction = new Transaction();
		transaction.setTransCode("c27248569aca4a219274da94850b4311");
		transaction.setTransType("NPP101");
		transaction.setCstNo("123456789");
		transaction.setChannelType("CH01");
		transaction.setDeviceToken("0a33e84a-99de-465f-9727-48747ec30f1d");
		transaction.setDeviceFinger("1|2001:352204062284031|2002:460019121503926|2003:AC:36:13:CF:1B:10|2004:4facd2a2|2006:8fbb91df18fe6a17|2009:1|2010:true|2013:4.589389937671455|2014:720*1280|2015:SM-N7506V|2016:hlltezn|2018:android|2019:4.3|2020:zh|2021:com.sfpay.mobile|2022:V2.1.2|2023:322d4985062b16|2024:18|2025:1.20 GB|2026:1.83 GB|2027:95|2028:N7506VZNUAOL1|2029:3.4.0-2576597|2030:CN|2031:TimeZone   GMT+08:00Timezon id :: Asia/Shanghai|2032:192.168.1.107|2033:8.8.8.8|2034:46001|2035:460|2036:01|2037:0c:72:2c:cd:9b:ea|2039:L&E|2040:0.0|2042:0.0|2044:40.008784|2045:116.278501|2046:0.0|2051:12.51 GB|2052:ARMv7 Processor rev 3 (v7l) 0|2053:BOARD: MSM8928, BOOTLOADER: N7506VZNUAOL1|2054:hllte|2055:wifi|2057:89860115811016362405");
		transaction.setTransStatus("1");
		//result = TmsApi.updateTransaction(transaction, true, false, 0);
		//result = TmsApi.riskEvaluate(transaction);
		printResult(result);
		System.out.println("-------------------" + transaction.getTransCode());

	}

	public static void main(String[] args) throws Exception {
		System.setProperty(StaticParameter.TMS_API_PATH, "classpath:properties");
		long s = System.currentTimeMillis();
		// for (int n = 0; n < 10000; n++) {
		new Test01().execute();
		// }
		System.out.println("----------" + (System.currentTimeMillis() - s));
	}

}
