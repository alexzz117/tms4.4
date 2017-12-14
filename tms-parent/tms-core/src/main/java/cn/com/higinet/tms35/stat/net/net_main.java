package cn.com.higinet.tms35.stat.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.com.higinet.tms.common.cache.CacheManager;
import cn.com.higinet.tms35.comm.clock;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.comm.translog_worker;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.cache.cache_init;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.stat.serv.stat_serv;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class net_main implements SignalHandler {
	static Logger log = LoggerFactory.getLogger(net_main.class);

	static Properties m_prop;

	public static void init() {
		try {
			m_prop = new Properties();
			InputStream in = ClassLoader.getSystemResourceAsStream("server.properties");
			m_prop.load(in);
			in.close();
			m_prop.setProperty("tms.is_stat_server", "1");
			tmsapp.set_config(m_prop);
			String configFilePath = m_prop.getProperty("spring.configfile");
			new ClassPathXmlApplicationContext(configFilePath.split(","));
		} catch (IOException e) {
			log.error(null, e);
		}
	}

	synchronized public static void worker_closed(worker w) {
		g_worker_list.remove(w);
		if (log.isDebugEnabled())
			log.debug(w + " closed.");
	}

	synchronized public static void worker_connected(worker w) {
		g_worker_list.add(w);
		if (log.isDebugEnabled())
			log.debug(w + " connected.");
	}

	synchronized public static void close_all() {
		for (worker w : g_worker_list)
			w.shutdown();

		for (worker w : g_worker_list)
			w.wait_shutdown();
	}

	static List<worker> g_worker_list = new ArrayList<worker>(256);

	static ServerSocket g_ss;

	public static void main(String[] args) throws IOException {
		clock c = new clock();
		init();
		c.pin("init spring");
		CacheManager cacheManager = bean.get(CacheManager.class);
		boolean success = cacheManager.start();
		if (!success) {
			cacheManager.stop();
			System.exit(0);
		}
		int port = 0;
		if (args.length > 0)
			port = Integer.parseInt(args[0]);
		else
			port = Integer.parseInt(m_prop.getProperty("tms.stat.port", "4000"));

		tmsapp.set_config("tms.stat.port", port);

		cache_init.init_for_stat(new data_source());
		stat_serv.eval_inst().start();
		//		stat_serv.query_inst().start(); //tms4.3不再从stat中查询统计，直接从AS中查询
		translog_worker.worker().start();// 监控每个统计各个时间点统计
		//		stat_serv.query_sql().start();
		//		stat_serv.query_inst2().start();
		final net_main sh = new net_main();
		// install signals

		// Runtime.getRuntime().addShutdownHook(new Thread(){
		//
		// @Override
		// public void run()
		// {
		// sh.handle(null);
		// }
		//			
		// });
		Signal.handle(new Signal("INT"), sh);
		Signal.handle(new Signal("TERM"), sh);

		g_ss = new ServerSocket();
		g_ss.setReuseAddress(true);
		g_ss.setPerformancePreferences(2, 0, 1);
		g_ss.bind(new InetSocketAddress(port));

		tmsapp.start_live_deamon(true);
		log.info("stat server listen in:" + port);
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

				log.error(null, e);
			}
		}
		shutdown(null);
	}

	public void handle(Signal arg0) {
		try {
			log.info("接收到信号：" + arg0);
			g_ss.close();
		} catch (IOException e) {
		}
	}

	public static void close_main_sock() {
		try {
			g_ss.close();
		} catch (IOException e) {
		}
	}

	static private boolean m_is_shutding = false;

	public static void shutdown(PrintWriter out) {
		if (m_is_shutding)
			return;

		m_is_shutding = true;

		log.info("begin shutdown main socket.");
		clock c = new clock();
		try {
			g_ss.close();
		} catch (IOException e) {
		}
		c.pin(log, "shutdown main socket");

		log.info("begin shutdown client sockets.");
		close_all();
		c.pin(log, "shutdown client sockets");

		log.info("begin shutdown stat query thread.");
		stat_serv.query_inst().shutdown(false);
		c.pin(log, "shutdown stat query thread");

		log.info("begin shutdown stat eval thread.");
		stat_serv.eval_inst().shutdown(false);
		c.pin(log, "shutdown stat eval thread");

		log.info("game over.");
	}
}
