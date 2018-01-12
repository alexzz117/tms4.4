/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  RiskBootstrap.java   
 * @Package cn.com.higinet.tms   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-11 17:17:34   
 * @version V1.0 
 * @Copyright: 2018 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import cn.com.higinet.tms.common.cache.CacheManager;
import cn.com.higinet.tms.common.lifecycle.ThreadService;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.comm.translog_worker;
import cn.com.higinet.tms35.core.cache.cache_init;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.stat.net.worker;
import cn.com.higinet.tms35.stat.serv.stat_serv;

/**
 * Risk引擎启动的引导类
 *
 * @ClassName:  RiskBootstrap
 * @author: 王兴
 * @date:   2018-1-11 17:17:34
 * @since:  v4.3
 */
public class StatBootstrap extends ThreadService {

	/** cache manager. */
	@Autowired
	private CacheManager cacheManager;

	private static ServerSocket g_ss;

	@Value("${tms.stat.eval.workerCount}")
	private int wc;
	
	static List<worker> g_worker_list = new ArrayList<worker>(256);

	public void worker_connected(worker w) {
		g_worker_list.add(w);
		if (logger.isDebugEnabled())
			logger.debug(w + " connected.");
	}

	@Override
	protected void onStart() throws Throwable {
		//1、初始化serverconfig文件
		Resource serverConfig = new ClassPathResource("statConfig.properties");
		Properties serverProps = new Properties();
		try (InputStream in = serverConfig.getInputStream();) {
			serverProps.load(in);
		}
		tmsapp.set_config(serverProps);
		cacheManager.start();
		System.out.println(wc);
		int port = 0;
		port = Integer.parseInt(serverProps.getProperty("tms.stat.port", "4000"));
		tmsapp.set_config("tms.stat.port", port);
		cache_init.init_for_stat(new data_source());
		stat_serv.eval_inst().start();
		translog_worker.worker().start();// 监控每个统计各个时间点统计
		g_ss = new ServerSocket();
		g_ss.setReuseAddress(true);
		g_ss.setPerformancePreferences(2, 0, 1);
		g_ss.bind(new InetSocketAddress(port));
		tmsapp.start_live_deamon(true);
		logger.info("stat server listen in:" + port);
	}

	@Override
	protected void onStop() throws Throwable {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRun() throws Throwable {
		for (;;) {
			try {
				Socket sock = g_ss.accept();
				sock.setSoLinger(true, 3000);
				sock.setSoTimeout(3000);
				sock.setTcpNoDelay(true);
				//				sock.setReceiveBufferSize(40960);
				//				sock.setSendBufferSize(40960);
				worker_connected(new worker(sock));
			} catch (IOException e) {
				if (g_ss.isClosed())
					break;

				logger.error(null, e);
			}
		}

	}
}
