package cn.com.higinet.tms35.core.cond.func_impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import cn.com.higinet.rapid.server.core.Request;
import cn.com.higinet.rapid.server.core.Response;
import cn.com.higinet.rapid.server.core.impl.DefaultRequestImpl;
import cn.com.higinet.rapid.server.core.impl.DefaultResponseImpl;
import cn.com.higinet.rapid.server.message.Message;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.concurrent.tms_worker;
import cn.com.higinet.tms35.core.cond.func;
import cn.com.higinet.tms35.core.cond.func_map;
import cn.com.higinet.tms35.core.dao.dao_base_tmp;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.run.serv.risk_serv_init;
import cn.com.higinet.tms35.service.io_env;

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
		if (func_map.has_nothing(p, n, 1))
			return func_map.NOTHING;
		String txn_code = str_tool.to_str(p[n]);
		try {
			Message message = new Message();
			message.setHead(Message.HEAD_SERVICE_CODE, "TMS");
			message.setHead(Message.HEAD_TRANSACTION_CODE, "0001");
			message.setHead(Message.HEAD_CONTENT_TAYPE, Message.CONTENT_TYPE_XML);
			
			SqlRowSet rs = bean.get(dao_base_tmp.class).query(
					"select * from TMS_RUN_TRAFFICDATA where TXNCODE = ?", 
								new Object[] { txn_code });
			if (rs.next()) {
				for (String fd : fields) {
					message.setData(fd, str_tool.to_str(rs.getObject(fd)));
				}
			}
			String fraudType = str_tool.to_str(message.getData("FRAUD_TYPE"));
			if ("00".equals(fraudType)) {
				message.setData("AUTHSTATUS", "1");
			} else {
				message.setData("AUTHSTATUS", "0");
			}
			String txnStatus = str_tool.to_str(message.getData("TXNSTATUS"));
			if (str_tool.is_empty(txnStatus)) {
				message.setData("TXNSTATUS", str_tool.to_str(message.getData("AUTHSTATUS")));
			}
			message.setData("PSSTATUS", "99");
			Request req = new DefaultRequestImpl(message);
			Response res = new DefaultResponseImpl(message);
			
			io_env ie = new io_env(req, res);
			ie.done();
			tms_worker<run_env> worker = risk_serv_init.init_pool();
			run_env re = run_env.identification(ie, true);
			if (re == null)
				return 1;
			worker.request(re);
		} catch (Exception e) {
			log.error("调用交易确认接口失败, txnCode: " + txn_code, e);
		}
		return 1;
	}
}