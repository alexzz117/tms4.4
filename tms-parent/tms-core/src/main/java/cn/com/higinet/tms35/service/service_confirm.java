package cn.com.higinet.tms35.service;

import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.server.core.AsyncService;
import cn.com.higinet.rapid.server.core.Request;
import cn.com.higinet.rapid.server.core.Response;
import cn.com.higinet.tms35.comm.StaticParameters;
import cn.com.higinet.tms35.core.cache.db_device;
import cn.com.higinet.tms35.core.concurrent.tms_worker;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.run.serv.risk_serv_init;

@Service("cfmService")
public class service_confirm extends AsyncService
{
	public void doService(Request req, Response res)
	{
		tms_worker<run_env> worker = risk_serv_init.init_pool();
		io_env ie = new io_env(req, res);
		run_env re = run_env.identification(ie, true);
		if (re == null)
			return;

		if (!db_device.IS_DEVFINGER_ON)
		{
			ie.setReturnCode(StaticParameters.SYSTEM_SUCCESS);
			ie.done();
		}
		worker.request(re);
	}
}

