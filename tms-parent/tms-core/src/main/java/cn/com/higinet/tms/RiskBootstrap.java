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

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import cn.com.higinet.rapid.server.core.Server;
import cn.com.higinet.tms.common.cache.CacheManager;
import cn.com.higinet.tms.common.lifecycle.Service;
import cn.com.higinet.tms35.comm.monitor.mon_thread;
import cn.com.higinet.tms35.comm.roster_refresh_worker;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.comm.translog_worker;
import cn.com.higinet.tms35.core.cache.cache_init;
import cn.com.higinet.tms35.core.cache.db_roster;
import cn.com.higinet.tms35.core.cache.ip_cache;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.run.serv.risk_commit;
import cn.com.higinet.tms35.run.serv.risk_mntstat;
import cn.com.higinet.tms35.run.serv.risk_serv_eval;
import cn.com.higinet.tms35.run.serv.risk_serv_init;
import cn.com.higinet.tms35.run.serv.risk_union_stat;
import cn.com.higinet.tms35.stat.client.x_socket_pool;
import cn.com.higinet.tms35.stat.serv.stat_serv;
import cn.com.higinet.tms4.model.RiskModelService;

/**
 * Risk引擎启动的引导类
 *
 * @ClassName:  RiskBootstrap
 * @author: 王兴
 * @date:   2018-1-11 17:17:34
 * @since:  v4.3
 */
public class RiskBootstrap extends Service implements ApplicationContextAware {
	
	/** context. */
	private ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;

	}

	/** tms data source. */
	@Autowired
	private DataSource tmsDataSource;

	/** cache manager. */
	@Autowired
	private CacheManager cacheManager;

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStart()
	 */
	@Override
	protected void doStart() throws Throwable {
		//1、初始化serverconfig文件
		Resource serverConfig = new ClassPathResource("application.properties");
		Properties serverProps = new Properties();
		try (InputStream in = serverConfig.getInputStream();) {
			serverProps.load(in);
		}
		tmsapp.set_config(serverProps);
		cacheManager.start();
		//2、初始化cache
		cache_init.init(new data_source(tmsDataSource), true);
		ip_cache.Instance();
		//3、初始化模型
		RiskModelService riskModelService = (RiskModelService) context.getBean("riskModelService");
		riskModelService.initCacheTms();

		//4、启动线程组
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
		risk_mntstat.commit_worker().start();

		//启动服务
		Map<String, Server> servers = context.getBeansOfType(Server.class);
		for (String name : servers.keySet()) {
			Server server = servers.get(name);
			server.start();
		}
		logger.info("TMS服务成功启动。");
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStop()
	 */
	@Override
	protected void doStop() throws Throwable {
		// TODO Auto-generated method stub

	}

}
