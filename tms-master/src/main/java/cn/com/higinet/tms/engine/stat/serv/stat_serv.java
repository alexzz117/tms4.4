/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  stat_serv.java   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-8-21 12:44:01   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.engine.stat.serv;

import cn.com.higinet.tms.engine.comm.tmsapp;
import cn.com.higinet.tms.engine.core.concurrent.tms_worker_base;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.persist.Stat;
import cn.com.higinet.tms.engine.core.persist.impl.CacheStatImpl;
import cn.com.higinet.tms.engine.stat.stat_row;

public abstract class stat_serv {
	public volatile static tms_worker_base<stat_row> inst_eval, inst_query;
	
	// static tms_worker_base<stat_value> inst_query_sql;
	volatile static Stat stat;

	public static synchronized Stat getStat() {
		if (stat != null)
			return stat;
		stat = new CacheStatImpl(); //4.3版本修改，stat的持久化通过接口的方式来实现，目前直接采用cache的实现类，后面如果有需求，可以修改成注入的方式   2017-08-21 王兴
		return stat;
	}

	/**
	 * 4.3版本过后去掉本地cache，此方法已经不推荐使用.
	 *
	 * @param stat_tab_name the stat tab name
	 * @param ds the ds
	 */
	@Deprecated
	synchronized public static void reset_cache(String stat_tab_name, data_source ds) {
		//		stat_cache.reset_cache(stat_tab_name, ds);
	}

	public static tms_worker_base<stat_row> eval_inst() {
		final tms_worker_base<stat_row> tmp = inst_eval;
		if (tmp != null)
			return tmp;

		synchronized (stat_serv.class) {
			if (inst_eval != null)
				return inst_eval;

			int stat_deque_size = tmsapp.get_config("tms.stat.eval.dequesize", 4096);

			if (tmsapp.get_config("tms.is_stat_server", 0) == 1 || !tmsapp.is_cluster()) {
				int thread_cnt = tmsapp.get_config("tms.stat.eval.workerCount", 2);
				return inst_eval = new stat_serv_dispatch_local(stat_deque_size, thread_cnt, getStat());
			} else {
				int thread_cnt = tmsapp.get_config("tms.stat.eval.clientCount", 2);
				return inst_eval = new stat_serv_net_client(stat_deque_size, thread_cnt, false);
			}
		}
	}

	public static tms_worker_base<stat_row> query_inst() {
		final tms_worker_base<stat_row> tmp = inst_query;
		if (tmp != null)
			return tmp;

		synchronized (stat_serv.class) {
			if (inst_query != null)
				return inst_query;

			int stat_deque_size = tmsapp.get_config("tms.stat.query.dequesize", 4096);

			if (tmsapp.get_config("tms.is_stat_server", 0) == 1 || !tmsapp.is_cluster()) {
				int thread_cnt = tmsapp.get_config("tms.stat.query.workerCount", 2);
				return inst_query = new stat_serv_dispatch_local_query(stat_deque_size, thread_cnt, getStat());
			} else {
				int thread_cnt = tmsapp.get_config("tms.stat.query.clientCount", 2);
				return inst_query = new stat_serv_net_client(stat_deque_size, thread_cnt, true);
			}
		}
	}
}
