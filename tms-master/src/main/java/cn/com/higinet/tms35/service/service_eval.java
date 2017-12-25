package cn.com.higinet.tms35.service;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.server.core.AsyncService;
import cn.com.higinet.rapid.server.core.Request;
import cn.com.higinet.rapid.server.core.Response;
import cn.com.higinet.tms35.core.concurrent.tms_worker;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.run.serv.risk_serv_init;

@Service("evlService")
public class service_eval extends AsyncService
{
	static Logger log = LoggerFactory.getLogger(service_eval.class);
	AtomicLong counter = new AtomicLong(0);

	public void doService(Request req, Response res)
	{
		tms_worker<run_env> worker = risk_serv_init.init_pool();
		io_env io = new io_env(req, res);
		run_env re=null;
		try {
			re = run_env.identification(io, false);
		} catch (Exception e) {
			log.error(null,e);
		}
		if (re == null)
			return;

//		io.setReturnCode(StaticParameters.SYSTEM_SUCCESS);
//		io.done();
//		if (true)
//			return;

		worker.request(re);
	}
}
