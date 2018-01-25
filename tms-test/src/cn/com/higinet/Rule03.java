package cn.com.higinet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import cn.com.higinet.tms.api.TmsApi;
import cn.com.higinet.tms.core.common.StaticParameter;
import cn.com.higinet.tms.core.model.RiskResult;
import cn.com.higinet.tms.core.model.Transaction;
import cn.com.higinet.tms.core.util.StringUtil;

public class Rule03 extends Rule {

	/**
	 * 登录交易 未登录天数超过15天
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public void execute() throws Exception {
		String cstNo = "a1003";
		//String ipAddress = "10.118.201.156";
		String ipAddress = "10.118.201.120";
		String deviceId = "55-5R-FC-FD-EQ-A72";
		String txnid = "rate0001";
		RiskResult result = null;
		Transaction transaction = null;
		Map<String, Object> ext =new HashMap<String, Object>();
		/*
		 * 第一次登陆
		 */
		transaction = new Transaction();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//ext.put("PAYCARD", "12345212123412333560");
		//ext.put("RECCARD", "1234");
		//ext.put("PAYAMOUNT", "230010");
		//ext.put("BINDCARD",1);
		//ext.put("CERTNO","");
		//ext.put("LOGINNAME", "dj");
		//ext.put("PHONE", "11");
		ext.put("src_ip", "10.111.254.79");
		//ext.put("src_ip", "171.221.204.79");
		ext.put("DL_TEST", "test");
		//ext.put("oper_type", "01");
		//transaction.setCstNo("122");//user58847 user795550
		//transaction.setDeviceId("0");
		//ext.put("src_ip", "119.139.199.16");
		//ext.put("RATING_LEVEL", 2);//RISK_LEVEL
		//transaction.setTransCode("124");
		//ext.put("DEVICEID", "455667");
	    transaction.setCstNo("20181939911965");
		//transaction.setDeviceToken("6f3f2882786c4e2e90e80e32c879da1q");
		//transaction.setTransType("NPP101");
		//transaction.setCstNo("20173949921954");
	   //transaction.setTransType("NPP101");
	    
		//transaction.setSessionId("42406c1d2a2b4d3e9ea0f227119cdfb6");
		//transaction.setIpAddress("117.136.45.133");
		//transaction.setDeviceFinger("1|2001:868047021163169|2004:88AKBME22ZJ8|2006:4910367dc88df82d|2009:0|2010:true|2013:4.589389937671455|2014:720*1280|2015:m2|2016:m2|2018:android|2019:5.1|2020:zh|2021:com.zte.smartpay|2022:1.0.5|2023:322d4985062b16|2024:22|2025:1.55 GB|2026:1.88 GB|2027:49|2028:MOLY.LR9.W1444.MD.LWTG.CMCC.MP.V10.P44, 2015/08/24 10:49|2029:3.10.65+|2030:CN|2031:TimeZone   GMT+08:00Timezon id :: Asia/Shanghai|2032:10.113.175.229|2033:0.0.0.0|2034:0|2035:0|2036:0|2051:11.59 GB|2052:AArch64 Processor rev 3 (aarch64) 0|2053:BOARD: m2, BOOTLOADER: unknown|2054:m2|2055:4g");
		//transaction.setDeviceToken("87154817ADD3B4518E6B36EA8902D34C4DC1EFC68046244728A47EEA4FE154D1F");
		//transaction.setDispatch("P100012945");
		//transaction.setTransTime("2012-08-12 08:42:57");
		 //transaction.setTransStatus("1")
		transaction.setTransType("SFSRC001");
		transaction.setChannelType("CH02");
		//ext.put("log_id", "123456");
		String time = sdf.format(new Date());
		ext.put("USERID", 3);
		ext.put("TXNTIME", "2017-06-05 11:04:03");
		//ext.put("TXNTIME", time);
		transaction.setExtInfo(ext);
		//transaction.setDeviceToken("6f3f2882786c4e2e90e80e32c879da1c");
		//transaction.setExtInfo(ext);
		
		transaction.setTransTime(time);
		//transaction.setTransTime("2017-03-15 05:00:00");
		
//		transaction.setDeviceFinger("1|2001:862970020019750|2003:00:0a:f5:a7:81:48|2004:e429e052|2006:3f0fdd33e07ed476|2009:0|2010:true|2013:4.589389937671455|2014:1080*1920|2015:MF198|2016:s700|2018:android|2019:5.1.1|2020:zh|2021:com.sfpay.mobile|2022:V2.1.2|2023:322d4985062b16|2024:22|2025:1.15 GB|2026:1.84 GB|2027:66|2028:.2.0.2.c1-00098-M8936FAAAANUZM-1,.2.0.2.c1-00098-M8936FAAAANUZM-1|2029:3.10.49-g4382015|2030:CN|2031:TimeZone   GMT+08:00Timezon id :: Asia/Shanghai|2032:192.168.33.174|2033:192.168.33.1|2034:0|2035:0|2036:0|2037:20:76:93:38:83:80|2039:SFPAY-SECU|2051:12.50 GB|2052:AArch64 Processor rev 1 (aarch64) 0|2053:BOARD: msm8916, BOOTLOADER: unknown|2054:MF198|2055:wifi");
		//transaction.setDeviceId("123");
		transaction.setTransCode(StringUtil.randomUUID());
		//transaction.setTransCode("2e47a1e59737446394281093c4b09a96");
		result = TmsApi.riskEvaluate(transaction, useFlag, syncFlag, timeout);
		//System.out.println(transaction.getExtInfo());
		printResult(result);
		// Thread.sleep(10);

		transaction.setTransStatus("0");
		result = TmsApi.updateTransaction(transaction, useFlag, syncFlag, timeout);
		printResult(result);
		System.out.println("===============评级得分："+result.getScore());
		// System.out.println("------------"+n+"-------" +
		// transaction.getTransCode());

	}

	public static void main(String[] args) throws Exception {
		System.setProperty(StaticParameter.TMS_API_PATH, "classpath:properties");
		for (int n = 0; n <4; n++) {
			new Rule03().execute();
		}
		/*while(true){
			new Rule01().execute();
		}*/
		/*new Rule01().execute();
		long firstTime = System.currentTimeMillis();
		double currentTime = System.currentTimeMillis();
		long tmpTime = 0;
		double time = 0;
		while(true){
			currentTime = currentTime +0.4;
			time = currentTime-firstTime;
			System.out.println(time);
			if(time>600000){
				new Rule01().execute();
				
				firstTime = System.currentTimeMillis();
				currentTime = System.currentTimeMillis();
			}
		}*/
		
		
	}

}
