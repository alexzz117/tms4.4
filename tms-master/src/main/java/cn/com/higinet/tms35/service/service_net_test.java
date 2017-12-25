package cn.com.higinet.tms35.service;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.server.core.AsyncService;
import cn.com.higinet.rapid.server.core.Request;
import cn.com.higinet.rapid.server.core.Response;
import cn.com.higinet.tms35.comm.StaticParameters;
import cn.com.higinet.tms35.comm.clock;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.dao.dao_base;

@Service("nettestService")
public class service_net_test extends AsyncService
{
	static Logger log = LoggerFactory.getLogger(service_net_test.class);
	static AtomicLong counter = new AtomicLong(0);

	public void doService(Request req, Response res)
	{
		clock m_clock = new clock();
		io_env ie = new io_env(req, res);
		String result = "ERROR", returnCode = StaticParameters.SYSTEM_ERROR;
		try
		{
			SqlRowSet rs = bean.get(dao_base.class).query("select 1 from dual");
			if (rs.next())
			{
				result = "RUNNING";
				returnCode = StaticParameters.SYSTEM_SUCCESS;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		res.setData(null, result);
		res.setReturnCode(returnCode);
		ie.done();
		log.info("request:" + counter.incrementAndGet() + " over, use time=" + m_clock.count_ms() + "ms.");
	}
}