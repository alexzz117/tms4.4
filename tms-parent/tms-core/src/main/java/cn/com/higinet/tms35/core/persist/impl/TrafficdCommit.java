package cn.com.higinet.tms35.core.persist.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import cn.com.higinet.tms.common.util.NumberUtils;
import cn.com.higinet.tms35.dataSyncLog;
import cn.com.higinet.tms35.comm.clock;
import cn.com.higinet.tms35.comm.delay_tool;
import cn.com.higinet.tms35.comm.hash;
import cn.com.higinet.tms35.comm.tm_tool;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.concurrent.tms_worker;
import cn.com.higinet.tms35.core.concurrent.tms_worker_base;
import cn.com.higinet.tms35.core.concurrent.tms_worker_proxy;
import cn.com.higinet.tms35.core.dao.dao_trafficdata;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.core.persist.Traffic;
import cn.com.higinet.tms35.run.run_txn_values;

public class TrafficdCommit extends tms_worker_proxy<run_txn_values> {
	static tms_worker<run_txn_values> inst;

	final static int thread_cnt = tmsapp.get_config("tms.trafficdcommit.workerCount", NumberUtils.min(Runtime.getRuntime().availableProcessors(), 8)); //如果没有配置，则默认线程数为最大8

	final static int batch_size = tmsapp.get_config("tms.trafficdcommit.batchsize", 128);

	final static int batch_time = tmsapp.get_config("tms.trafficdcommit.batchtime", 500);

	final static int deque_size = tmsapp.get_config("tms.trafficdcommit.dequesize", 8192);

	final static int offer_timeout = tmsapp.get_config("tms.trafficdcommit.offer_timeout", 3000);

	public static tms_worker<run_txn_values> commit_pool() {
		if (inst != null)
			return inst;

		synchronized (TrafficdCommit.class) {
			if (inst != null)
				return inst;

			return inst = new TrafficdCommit("trafficd-commit", deque_size, thread_cnt);
		}
	}

	public TrafficdCommit(String name, int deque_size, int thread_cnt) {
		super(name, null, thread_cnt);
		for (int i = 0; i < m_worker_pool.length; i++)
			m_worker_pool[i] = new risk_commit_worker(name + "-" + i, deque_size);
	}

	private int thread_id(String dispatch) {
		return hash.clac(dispatch, 134218199);
	}

	@Override
	public boolean request(run_txn_values e) {
		int i = thread_id(e.m_env.get_dispatch()) % 2281;
		risk_commit_worker worker = (risk_commit_worker) m_worker_pool[i % m_worker_pool.length];
		boolean is_ok = worker.offer(e, offer_timeout);
		if (!is_ok) {
			// 向队列中存放失败(超时), 关闭原数据库连接, 重新构建连接
			worker.db_reset();
		}
		return is_ok;
	}

	static public class risk_commit_worker extends tms_worker_base<run_txn_values> {
		dao_trafficdata m_dao_txn;

//		private static final Traffic trafficdata = bean.get(Traffic.class); //持久化接口由外部注入

		public risk_commit_worker(String name, int deque_size) {
			super(name, deque_size);
		}

		@Override
		protected void post_run() {
			db_close();
		}

		@Override
		protected void pre_run() {
			db_init();
		}

		List<run_txn_values> request = new ArrayList<run_txn_values>();

		List<run_txn_values> tmp_list = new LinkedList<run_txn_values>();

		String batch_code, error;

		clock c = new clock();

		protected void run_once() {
			try {
				_run_once();
			} catch (SQLException e) {
				log.error("commit error.", e);
			}
		}

		final <E> int update(String batch_code, E e, batch_stmt_jdbc_obj<E> stmt) throws SQLException {
			int count = 0;
			if (e == null)
				return count;
			stmt.update(batch_code, e);
			return ++count;
		}

		protected void _run_once() throws SQLException {
			request.clear();
			if (tmp_list.size() < batch_size)
				super.drainTo(request, batch_size, batch_time);
			tmp_list.addAll(request);
			if (tmp_list.isEmpty())
				return;

			if (log.isDebugEnabled())
				log.debug("commit size=" + tmp_list.size());
			error = null;
			long update_count = 0;
			batch_code = dataSyncLog.get_batch_code(name(), tm_tool.lctm_ms());
			dataSyncLog.batch_print_begin(batch_code);
			try {
				for (int i = 0, len = tmp_list.size(); i < len; i++) {
					run_txn_values row = tmp_list.get(i);
					update_count += update(batch_code, row, m_dao_txn);
				}
				m_dao_txn.flush();
				c.reset();
				m_dao_txn.commit();
				if (c.count_ms() > 1000) {
					log.warn("[" + name() + "]- " + tmp_list.size() + "笔交易, " + update_count + "条数据库操作, 提交耗时: " + c.count_ms() + "ms.");
				}
				/* 4.3版本注释掉此功能，所有在缓存中的交易流水均已经入库
				 * for (run_txn_values value : tmp_list) {
					try {
						trafficdata.setInDB(value);
					} catch (Exception e) {
						log.error("Trafficdata set_in_db error.", e);
					}
				}*/
			} catch (SQLException e) {
				db_clear();
				m_dao_txn.rollback();
				error = e.getLocalizedMessage();
				throw e;
			} finally {
				tmp_list.clear();
				dataSyncLog.batch_print_end(batch_code, update_count, (error == null ? dataSyncLog.Action.COMMIT : dataSyncLog.Action.ROLLBACK));
			}
		}

//		@SuppressWarnings("unused")
//		private void debug_out(Set<db_user> setUser) {
//			StringBuffer sb = new StringBuffer(2048);
//			sb.append(Thread.currentThread().getId()).append(':');
//			for (db_user u : setUser)
//				sb.append(u.userid).append(',');
//
//			log.warn(sb.toString());
//		}

		public void db_reset() {
			db_clear();
			db_close();
			db_init();
		}

		private void db_init() {
			data_source ds = new data_source((DataSource) bean.get("tmsDataSource"));
			m_dao_txn = new dao_trafficdata(db_cache.get().table(), db_cache.get().field(), ds);
		}

		private void db_clear() {
			m_dao_txn.reset_update_pos();
		}

		private void db_close() {
			m_dao_txn.close();
		}

		@Override
		public void shutdown(boolean abort) {
			for (; m_request.size() > 0;)
				delay_tool.delay(100);
			super.shutdown(abort);
		}
	}
}
