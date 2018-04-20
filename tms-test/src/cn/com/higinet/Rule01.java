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

public class Rule01 extends Rule {

	/**
	 * 登录交易 未登录天数超过15天
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public void execute() throws Exception {
		
		RiskResult result = null;
		Transaction transaction = null;
		Map<String, Object> ext =new HashMap<String, Object>();
		
		transaction = new Transaction();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		//下面是添加参数
		ext.put("src_ip", "10.111.254.79");
		ext.put("DL_TEST", "test");
	    transaction.setCstNo("20181939911965");
	    
	    //参数是。交易定义的 交易识别标识 来发送
		transaction.setTransType("netbank_auth");
		transaction.setChannelType("CH02");
		String time = sdf.format(new Date());
		
		//用户号
		ext.put("USERID", 3);
		ext.put("TXNTIME", "2017-06-05 11:04:03");
		transaction.setExtInfo(ext);

		
		transaction.setTransTime(time);
		
		//参数是。编号取的随机数
		transaction.setTransCode(StringUtil.randomUUID());
		result = TmsApi.riskEvaluate(transaction, useFlag, syncFlag, timeout);
		//调用的是评估接口
		printResult(result);

		
		
		//状态0和1。0代表失败，1代表成功
		transaction.setTransStatus("1");
		result = TmsApi.updateTransaction(transaction, useFlag, syncFlag, timeout);
		//调用的是确认接口，确认只是传状态
		printResult(result);
		System.out.println("===============评级得分："+result.getScore());

	}

	//这里是主函数入口，循环是指调用多少次。可以自己改数字
	public static void main(String[] args) throws Exception {
		System.setProperty(StaticParameter.TMS_API_PATH, "classpath:properties");
		for (int n = 0; n <1; n++) {
			new Rule01().execute();
		}
		
		
		
	}

}
