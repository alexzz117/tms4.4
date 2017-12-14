package cn.com.higinet.tms35;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.com.higinet.rapid.server.core.Server;
import cn.com.higinet.tms.common.cache.CacheManager;
import cn.com.higinet.tms.common.repository.RepositoryManager;
import cn.com.higinet.tms35.comm.monitor.mon_thread;
import cn.com.higinet.tms35.comm.roster_refresh_worker;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.comm.translog_worker;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.cache.cache_init;
import cn.com.higinet.tms35.core.cache.db_roster;
import cn.com.higinet.tms35.core.cache.ip_cache;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.core.persist.Traffic;
import cn.com.higinet.tms35.run.serv.risk_commit;
import cn.com.higinet.tms35.run.serv.risk_mntstat;
import cn.com.higinet.tms35.run.serv.risk_serv_eval;
import cn.com.higinet.tms35.run.serv.risk_serv_init;
import cn.com.higinet.tms35.run.serv.risk_union_stat;
import cn.com.higinet.tms35.stat.client.x_socket_pool;
import cn.com.higinet.tms35.stat.serv.stat_serv;
import cn.com.higinet.tms4.model.RiskModelService;
import sun.misc.Signal;
import sun.misc.SignalHandler;

@SuppressWarnings("restriction")
public class riskServer implements SignalHandler {
	static Logger log = LoggerFactory.getLogger(riskServer.class);

	static ClassPathXmlApplicationContext context;

	static void startup() throws Exception{
		x_socket_pool.Instance().start();
		db_roster.cache.init_for_service_eval();
		mon_thread.start();// 监控线程的繁忙程度
		stat_serv.eval_inst().start();
		stat_serv.query_inst().start();
		risk_union_stat.worker().start();
		roster_refresh_worker.worker().start();
		risk_commit.commit_pool().start();
		translog_worker.worker().start();// 监控每个交易各个时间点统计
		risk_serv_eval.eval_pool().start();
		risk_serv_init.init_pool().start();
		tmsapp.start_live_deamon(false);
		bean.get(Traffic.class).initialize(); //初始化交易流水对象，本来应该是在bean注入时候初始化，但是由于相关资源没有通过spring管理，因此只能在此处写死 2017-09-11 tms4.3 王兴
		bean.get(RepositoryManager.class).start();//初始化结构化数据仓管理
		risk_mntstat.commit_worker().start();
	}

	static void shutdown() {
		log.info("begin shutdown server.");
		Map<String, Server> servers = context.getBeansOfType(Server.class);
		for (String name : servers.keySet()) {
			log.info("shutdown:" + name);
			Server server = servers.get(name);
			server.shutdown();
		}

		risk_serv_init.init_pool().shutdown(false);
		risk_serv_eval.eval_pool().shutdown(false);
		translog_worker.worker().shutdown(false);
		risk_commit.commit_pool().shutdown(false);
		roster_refresh_worker.worker().shutdown(false);
		risk_union_stat.worker().shutdown(false);
		stat_serv.eval_inst().shutdown(false);
		stat_serv.query_inst().shutdown(false);
		x_socket_pool.Instance().close();
		risk_mntstat.commit_worker().shutdown(false);
		context.destroy(); //2017-07-25王兴添加，销毁context对象，会自动销毁有destroy-method的bean
		log.info("shutdown server over.");
	}

	public static void main(String[] args) throws Exception {
		Properties p = new Properties();
		InputStream in = riskServer.class.getResourceAsStream("/server.properties");
		p.load(in);
		in.close();
		tmsapp.set_config(p);
		String configFilePath = p.getProperty("spring.configfile");
		context = new ClassPathXmlApplicationContext(configFilePath.split(","));
		//手动控制缓存服务启动，这个服务比较重要，启动失败的话，直接终止当前进程 2017-07-25王兴
		CacheManager cacheManager = context.getBean(CacheManager.class);
		boolean success = cacheManager.start();
		if (!success) {
			cacheManager.stop();
			System.exit(0);
		}
		cache_init.init(new data_source(), true);
		ip_cache.Instance();

		RiskModelService riskModelService = (RiskModelService) context.getBean("riskModelService");
		riskModelService.initCacheTms();

		startup();

		Map<String, Server> servers = context.getBeansOfType(Server.class);
		for (String name : servers.keySet()) {
			Server server = servers.get(name);
			server.start();
		}

		reg_signal();
		log.info("TMS服务成功启动。");
	}

	public static void reg_signal() {
		riskServer ns = new riskServer();
		Signal.handle(new Signal("INT"), ns);
		Signal.handle(new Signal("TERM"), ns);
	}

	public void handle(Signal arg0) {
		log.info("接收到信号：" + arg0);
		shutdown();
	}
}
