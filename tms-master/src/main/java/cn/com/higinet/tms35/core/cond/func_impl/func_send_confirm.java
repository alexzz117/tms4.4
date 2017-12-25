package cn.com.higinet.tms35.core.cond.func_impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.core.cond.func;

/**
 * 发送交易确认报文函数
 * @author lining
 *
 */
public class func_send_confirm implements func {
	static Logger log = LoggerFactory.getLogger(func_send_confirm.class);
	static String[] fields = new String[] { "TXNCODE", "TXNID", 
		"CHANCODE", "USERID", "TXNSTATUS", "PSSTATUS", "FRAUD_TYPE" };
	
	@Override
	public Object exec(Object[] p, int n) {
		return 1;
	}
}