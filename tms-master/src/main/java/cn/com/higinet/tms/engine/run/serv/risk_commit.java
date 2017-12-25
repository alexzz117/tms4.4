/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  risk_commit.java   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-25 18:10:59   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.engine.run.serv;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import cn.com.higinet.tms.engine.dataSyncLog;
import cn.com.higinet.tms.engine.comm.clock;
import cn.com.higinet.tms.engine.comm.delay_tool;
import cn.com.higinet.tms.engine.comm.hash;
import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.comm.tm_tool;
import cn.com.higinet.tms.engine.comm.tmsapp;
import cn.com.higinet.tms.engine.core.bean;
import cn.com.higinet.tms.engine.core.concurrent.tms_worker;
import cn.com.higinet.tms.engine.core.concurrent.tms_worker_base;
import cn.com.higinet.tms.engine.core.concurrent.tms_worker_proxy;
import cn.com.higinet.tms.engine.core.dao.dao_rule_action_hit;
import cn.com.higinet.tms.engine.core.dao.dao_rule_hit;
import cn.com.higinet.tms.engine.core.dao.row_in_db;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.run.run_db_commit;

/**
 * {类的详细说明}.
 *
 * @ClassName:  risk_commit
 * @author: 王兴
 * @date:   2017-9-25 18:10:59
 * @since:  v4.3
 */
public class risk_commit extends tms_worker_proxy<run_db_commit> {
	
	/** inst. */
	static tms_worker<run_db_commit> inst;

	/** 常量 thread_cnt. */
	final static int thread_cnt = tmsapp.get_config("tms.commit.workerCount", 4);

	/** 常量 batch_size. */
	final static int batch_size = tmsapp.get_config("tms.commit.batchsize", 128);

	/** 常量 batch_time. */
	final static int batch_time = tmsapp.get_config("tms.commit.batchtime", 500);

	/** 常量 deque_size. */
	final static int deque_size = tmsapp.get_config("tms.commit.dequesize", 8192);

	/** 常量 offer_timeout. */
	final static int offer_timeout = tmsapp.get_config("tms.commit.offer_timeout", 3000);

	/** 常量 isSyncOn. */
	final static int isSyncOn = tmsapp.get_config("sync.isOn", 2);

	/** 常量 useTable. */
	final static String[] useTable = tmsapp.get_config("tms.commit.isUseTable", "1,1,1,1,1").split(",");

	/**
	 * Commit pool.
	 *
	 * @return the tms worker
	 */
	public static tms_worker<run_db_commit> commit_pool() {
		if (inst != null)
			return inst;

		synchronized (risk_commit.class) {
			if (inst != null)
				return inst;

			return inst = new risk_commit("commit", deque_size, thread_cnt);
		}
	}

	/**
	 * 构造一个新的 risk commit 对象.
	 *
	 * @param name the name
	 * @param deque_size the deque size
	 * @param thread_cnt the thread cnt
	 */
	public risk_commit(String name, int deque_size, int thread_cnt) {
		super(name, null, thread_cnt);
		for (int i = 0; i < m_worker_pool.length; i++)
			m_worker_pool[i] = new risk_commit_worker(name + "-" + i, deque_size);
	}

	/**
	 * Thread id.
	 *
	 * @param dispatch the dispatch
	 * @return the int
	 */
	private int thread_id(String dispatch) {
		return hash.clac(dispatch, 134218199);
	}

	@Override
	public boolean request(run_db_commit e) {
		int i = thread_id(e.get_dispatch()) % 2281;
		risk_commit_worker worker = (risk_commit_worker) m_worker_pool[i % m_worker_pool.length];
		boolean is_ok = worker.request(e, offer_timeout);
		if (!is_ok) {
			// 向队列中存放失败(超时), 关闭原数据库连接, 重新构建连接
			worker.db_reset();
		}
		return is_ok;
	}

	/**
	 * 交易流水已经放入外层单独缓存和持久化
	 * 其他的放入结构化数据的数据仓管理
	 *
	 * @ClassName:  risk_commit
	 * @author: 王兴
	 * @date:   2017-9-25 18:10:59
	 * @since:  v4.3
	 */
	static public class risk_commit_worker extends tms_worker_base<run_db_commit> {
//		dao_trafficdata m_dao_txn;  //4.3版本已经迁移至cn.com.higinet.tms35.core.persist.impl.CacheTrafficImpl.save方法中实现

//		dao_session m_dao_session;

		/** m dao rule hit. */
		dao_rule_hit m_dao_rule_hit;

		/** m dao rule action hit. */
		dao_rule_action_hit m_dao_rule_action_hit;

//		dao_user m_dao_user;
//
//		dao_acc m_dao_acc;

		/** is use tables. */
		Boolean[] isUseTables = null;

		/**
		 * 构造一个新的 risk commit worker 对象.
		 *
		 * @param name the name
		 * @param deque_size the deque size
		 */
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
			isUseTables = new Boolean[useTable.length];
			for (int i = 0, len = isUseTables.length; i < len; i++) {
				boolean flag = false;
				if (str_tool.is_empty(useTable[i]) || "1".equals(useTable[i])) {
					flag = true;
				}
				isUseTables[i] = flag;
			}
		}

		/** request. */
		List<run_db_commit> request = new ArrayList<run_db_commit>();

		/** tmp list. */
		List<run_db_commit> tmp_list = new LinkedList<run_db_commit>();

		/** error. */
		String batch_code, error;

		/** c. */
		clock c = new clock();

		protected void run_once() {
			try {
				_run_once();
			} catch (SQLException e) {
				log.error("commit error.", e);
			}
		}

//		Set<db_user> set_user = new HashSet<db_user>(4096, 0.5f);
//
//		Set<db_session> set_session = new HashSet<db_session>(4096, 0.5f);
//
//		Set<db_ref_row> set_acc = new HashSet<db_ref_row>(4096, 0.5f);

		/**
 * Update.
 *
 * @param <E> the element type
 * @param batch_code the batch code
 * @param e the e
 * @param stmt the stmt
 * @param set the set
 * @return the int
 * @throws SQLException the SQL exception
 */
final <E> int update(String batch_code, E e, batch_stmt_jdbc_obj<E> stmt, Set<E> set) throws SQLException {
			int count = 0;
			if (e == null)
				return count;
			if (set.add(e)) {
				stmt.update(batch_code, e);
				count++;
			}
			return count;
		}

		/**
		 * Update.
		 *
		 * @param <E> the element type
		 * @param batch_code the batch code
		 * @param e the e
		 * @param stmt the stmt
		 * @param set the set
		 * @return the int
		 * @throws SQLException the SQL exception
		 */
		final <E> int update(String batch_code, List<E> e, batch_stmt_jdbc_obj<E> stmt, Set<E> set) throws SQLException {
			int count = 0;
			if (e == null)
				return count;

			for (E e1 : e)
				count += update(batch_code, e1, stmt, set);
			return count;
		}

		/**
		 * Update.
		 *
		 * @param <E> the element type
		 * @param batch_code the batch code
		 * @param e the e
		 * @param stmt the stmt
		 * @return the int
		 * @throws SQLException the SQL exception
		 */
		final <E> int update(String batch_code, E e, batch_stmt_jdbc_obj<E> stmt) throws SQLException {
			int count = 0;
			if (e == null)
				return count;
			stmt.update(batch_code, e);
			return ++count;
		}

		/**
		 * Update.
		 *
		 * @param <E> the element type
		 * @param batch_code the batch code
		 * @param e the e
		 * @param stmt the stmt
		 * @return the int
		 * @throws SQLException the SQL exception
		 */
		final <E> int update(String batch_code, List<E> e, batch_stmt_jdbc_obj<E> stmt) throws SQLException {
			int count = 0;
			if (e == null)
				return count;

			for (E e1 : e)
				count += update(batch_code, e1, stmt);
			return count;
		}

		/**
		 * 设置 indb 属性.
		 *
		 * @param <E> the element type
		 * @param e the e
		 */
		final <E extends row_in_db> void set_indb(List<E> e) {
			if (e == null)
				return;

			for (E e1 : e)
				e1.set_indb(true);
		}

		/**
		 * 设置 indb 属性.
		 *
		 * @param <E> the element type
		 * @param e the e
		 */
		final <E extends row_in_db> void set_indb(E e) {
			if (e == null)
				return;

			e.set_indb(true);
		}

		/**
		 * Request.
		 *
		 * @param e the e
		 * @param timeoutMs the timeout ms
		 * @return true, if successful
		 */
		public boolean request(run_db_commit e, int timeoutMs) {
//			if (e.get_session() != null)
//				m_dao_session.cache_pending_data(e.get_session());
			//			if (e.get_txn_data() != null)  //4.3版本已经迁移至cn.com.higinet.tms35.core.persist.impl.CacheTrafficImpl.save方法中实现
			//				m_dao_txn.cache_pending_data(e.get_txn_data());
//			if (e.get_user() != null)
//				m_dao_user.cache_pending_data(e.get_user());
			return super.offer(e, timeoutMs);
		}

		/**
		 * Run once.
		 *
		 * @throws SQLException the SQL exception
		 */
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
					int u = 0;
					run_db_commit row = tmp_list.get(i);
//					if (isUseTables == null || isUseTables.length <= u || isUseTables[u++])
//						update_count += update(batch_code, row.get_user(), m_dao_user, set_user);
//					if (isUseTables == null || isUseTables.length <= u || isUseTables[u++])
//						update_count += update(batch_code, row.get_session(), m_dao_session, set_session);
					if (isUseTables == null || isUseTables.length <= u || isUseTables[u++])
						update_count += update(batch_code, row.get_rule_hit(), m_dao_rule_hit);
					if (isUseTables == null || isUseTables.length <= u || isUseTables[u++])
						update_count += update(batch_code, row.get_rule_action_hit(), m_dao_rule_action_hit);
					//					if (isUseTables == null || isUseTables.length <= u || isUseTables[u++])
					//						update_count += update(batch_code, row.get_txn_data(), m_dao_txn);
//					if (isUseTables == null || isUseTables.length <= u || isUseTables[u++])
//						update_count += update(batch_code, row.get_acc(), m_dao_acc, set_acc);

					// 发送离线交易数据
					if (isSyncOn == 1) {
						// bin_stream bs = new bin_stream(128);
						//	SyncApi.send(null, null, row.get_rule_hit(), row.get_user());
					}
				}

//				m_dao_user.flush();
//				m_dao_session.flush();
				m_dao_rule_hit.flush();
				m_dao_rule_action_hit.flush();
//				m_dao_txn.flush();
//				m_dao_acc.flush();
				c.reset();
//				m_dao_acc.commit();
				m_dao_rule_hit.commit();
				if (c.count_ms() > 1000) {
					log.warn("[" + name() + "]- " + tmp_list.size() + "笔交易, " + update_count + "条数据库操作, 提交耗时: " + c.count_ms() + "ms.");
				}
				for (run_db_commit row : tmp_list) {
//					m_dao_txn.cache_set_indb(row.get_txn_data());
					set_indb(row.get_txn_data());
//					set_indb(row.get_session());
//					set_indb(row.get_user());
//					set_indb(row.get_acc());
				}
				risk_mntstat.commit_worker().request(tmp_list); 
			} catch (SQLException e) {
				db_clear();
//				m_dao_acc.rollback();
				m_dao_rule_hit.rollback();
				error = e.getLocalizedMessage();
				throw e;
			} finally {
//				set_user.clear();
//				set_session.clear();
//				set_acc.clear();
				tmp_list.clear();
				dataSyncLog.batch_print_end(batch_code, update_count, (error == null ? dataSyncLog.Action.COMMIT : dataSyncLog.Action.ROLLBACK));
			}
		}

/*		*//**
		 * Debug out.
		 *
		 * @param setUser the set user
		 *//*
		@SuppressWarnings("unused")
		private void debug_out(Set<db_user> setUser) {
			StringBuffer sb = new StringBuffer(2048);
			sb.append(Thread.currentThread().getId()).append(':');
			for (db_user u : setUser)
				sb.append(u.userid).append(',');

			log.warn(sb.toString());
		}*/

		/**
		 * Db reset.
		 */
		public void db_reset() {
			db_clear();
			db_close();
			db_init();
		}

		/**
		 * Db init.
		 */
		private void db_init() {
			data_source ds = new data_source((DataSource) bean.get("tmsDataSource"));
			m_dao_rule_hit = new dao_rule_hit(ds);
			m_dao_rule_action_hit = new dao_rule_action_hit(ds);
//			m_dao_txn = new dao_trafficdata(db_cache.get().table(), db_cache.get().field(), ds);
//			m_dao_session = new dao_session(ds);
//			m_dao_user = new dao_user(ds);
//			m_dao_acc = new dao_acc(ds, db_cache.get().field());
		}

		/**
		 * Db clear.
		 */
		private void db_clear() {
//			m_dao_user.reset_update_pos();
//			m_dao_session.reset_update_pos();
			m_dao_rule_hit.reset_update_pos();
			m_dao_rule_action_hit.reset_update_pos();
//			m_dao_txn.reset_update_pos();
//			m_dao_acc.reset_update_pos();
		}

		/**
		 * Db close.
		 */
		private void db_close() {
//			m_dao_session.close();
//			m_dao_txn.close();
			m_dao_rule_hit.close();
			m_dao_rule_action_hit.close();
//			m_dao_user.close();
//			m_dao_acc.close();
		}

		@Override
		public void shutdown(boolean abort) {
			for (; m_request.size() > 0;)
				delay_tool.delay(100);
			super.shutdown(abort);
		}
	}
}
