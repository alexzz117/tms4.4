package cn.com.higinet.tms35.service;

import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.base.dao.SqlMap;
import cn.com.higinet.rapid.server.core.AsyncService;
import cn.com.higinet.rapid.server.core.Request;
import cn.com.higinet.rapid.server.core.Response;
import cn.com.higinet.tms35.comm.MapUtil;
import cn.com.higinet.tms35.comm.StaticParameters;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tm_tool;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.concurrent.tms_worker_proxy;
import cn.com.higinet.tms35.core.dao.dao_base;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.run.serv.risk_serv_init;

/**
 * 服务端线程健康检查接口
 */
@Service("healthCheckService")
public class service_health_check extends AsyncService {
	private static final Logger LOG = LoggerFactory.getLogger(service_health_check.class);
	private static int init_thread_cnt = tmsapp.get_config("tms.init.workerCount", 8);
	private static final int RISK_SERVER_TYPE = 1;
	@SuppressWarnings("unused")
	private static final int STAT_SERVER_TYPE = 2;

	@Autowired
	private SqlMap tmsSqlMap;
	private String sql;
	private Random random = new Random();

	
	private void original_service(Request req, Response res) {
		LOG.info("收到健康检查请求.");
		io_env ie = new io_env(req, res);
		String servs = "";
		try {
			Map<String, Object> map = ie.getParameterOriMap();
			boolean flag = "1".equals(MapUtil.getString(map, StaticParameters.HEALTH_TMS_SERVER_LIST));
			if (flag) {
				long time = tm_tool.lctm_ms();
				LOG.info("健康检查,[获取评估服务列表]开始.");
				if (str_tool.is_empty(sql)) {
					sql = tmsSqlMap.getSql("tms.server.whole");
					LOG.info("健康检查,[获取评估服务列表]的sql=" + sql);
					if (str_tool.is_empty(sql)) {
						LOG.info("健康检查,[获取评估服务列表]的sql为空,无法获取列表.");
						servs = "SQL.ERROR";
						return;
					}
				}
				int serverType = MapUtil.getInteger(map, StaticParameters.HEALTH_TMS_SERVER_TYPE);
				if (serverType <= 0 || serverType > 2) {
					serverType = RISK_SERVER_TYPE;
				}
				SqlRowSet rs = bean.get(dao_base.class).query(sql, new Object[] { serverType });
				StringBuffer sb = new StringBuffer(128);
				while (rs.next()) {
					String ip = rs.getString("IPADDR");
					int port = rs.getInt("PORT");
					sb.append(ip).append(':').append(port).append(',');
				}
				int len = sb.length();
				if (len > 0) {
					sb.setLength(len - 1);
				}
				servs = sb.toString();
				ie.setData("DBCSM_TIME", tm_tool.lctm_ms() - time);//查询服务列表耗时
				LOG.info("健康检查,[获取评估服务列表]结束,type=" + serverType + ",list=" + servs);
			}
		} catch (Exception e) {
			LOG.error("服务端线程健康检查异常.", e);
		} finally {
			ie.setData("TMS_SERVERS", servs);
			try {
				run_env re = run_env.identification(ie, false);
				int thread_idx = random.nextInt(init_thread_cnt);
				tms_worker_proxy<run_env> worker = (tms_worker_proxy<run_env>) risk_serv_init.init_pool();
				worker.request(thread_idx, re);
			} catch (Exception e) {
				LOG.error("健康检查,构造run_env并将其随机送入初始化线程异常.", e);
				ie.setReturnCode(StaticParameters.SYSTEM_ERROR);
				ie.done(e);
			}
		}
	}

	@Override
	public void doService(Request req, Response res)
	{
		long start_time = System.currentTimeMillis();
	try
	{
		original_service(req, res);
	}
	finally
	{
		LOG.info(String.format("service=%s,tcode=%s,used time=%dms", this.getClass().getName(),
		req.getTransactionCode(), System.currentTimeMillis() - start_time));
	}
	}
	
	
}