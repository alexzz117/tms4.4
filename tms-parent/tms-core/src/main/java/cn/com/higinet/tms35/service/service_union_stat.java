package cn.com.higinet.tms35.service;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.server.core.AsyncService;
import cn.com.higinet.rapid.server.core.Request;
import cn.com.higinet.rapid.server.core.Response;
import cn.com.higinet.tms35.comm.StaticParameters;
import cn.com.higinet.tms35.run.serv.risk_union_stat;
import cn.com.higinet.tms35.stat.stat_row;

@Service("unionstatService")
public class service_union_stat extends AsyncService
{
	static Logger log = LoggerFactory.getLogger(service_union_stat.class);
	AtomicLong counter = new AtomicLong(0);

	public void doService(Request req, Response res)
	{
		String key = (String) req.getParameter("STAT_KEY");
		String value = (String) req.getParameter("STAT_VALUE");

		io_env ie = new io_env(req, res);
		if (key == null | value == null)
		{
			ie.setReturnCode(StaticParameters.ERROR_MESSAGE);
			ie.done();

			return;
		}
		ie.setReturnCode(StaticParameters.SYSTEM_SUCCESS);
		ie.done();

		risk_union_stat.worker().request(new stat_row(stat_row.UNION, key, value));
	}
}

