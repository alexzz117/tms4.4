package cn.com.higinet.tms35.run.serv;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import cn.com.higinet.tms35.dataSyncLog;
import cn.com.higinet.tms35.comm.clock;
import cn.com.higinet.tms35.comm.delay_tool;
import cn.com.higinet.tms35.comm.tm_tool;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.concurrent.tms_worker;
import cn.com.higinet.tms35.core.concurrent.tms_worker_base;
import cn.com.higinet.tms35.core.dao.dao_monitor_stat_rule;
import cn.com.higinet.tms35.core.dao.dao_monitor_stat_txn_area;
import cn.com.higinet.tms35.core.dao.dao_monitor_stat_txn_dp;
import cn.com.higinet.tms35.core.dao.dao_monitor_stat_txn_time;
import cn.com.higinet.tms35.core.dao.row_in_db;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.run.run_db_commit;
import cn.com.higinet.tms35.run.run_mntstat;
import cn.com.higinet.tms35.run.run_mntstat.run_stat_commit;
import cn.com.higinet.tms35.run.run_txn_values;

public class risk_mntstat extends tms_worker_base<run_db_commit> {
	static tms_worker<run_db_commit> inst;
	final static int batch_size = tmsapp.get_config("tms.mntstat.commit.batchsize", 512);
	final static int batch_time = tmsapp.get_config("tms.mntstat.commit.batchtime", 100);
	final static int deque_size = tmsapp.get_config("tms.mntstat.commit.dequesize", 8192);
	final static int commit_maxperiod = tmsapp.get_config("tms.mntstat.commit_period", 1000);
	final static int commit_maxsize = tmsapp.get_config("tms.mntstat.commit_size", 1024);
	final static boolean isOnStat = tmsapp.get_config("tms.monitor.stat.isOn", 0) == 1;
	final static boolean isStatSyncLog = tmsapp.get_config("sync.log.stat.isOn", 0) == 1;
	
	public static tms_worker<run_db_commit> commit_worker()
	{
		if (inst != null)
			return inst;
		synchronized (risk_mntstat.class)
		{
			if (inst != null)
				return inst;
			return inst = new risk_mntstat("mntstat", deque_size);
		}
	}
	
	public risk_mntstat(String name, int deque_size)
	{
		super(name, deque_size);
	}
	
	/* 实时监控统计 */
	run_mntstat monitor_stat;
	dao_monitor_stat_txn_area m_dao_stat_txn_area;
	dao_monitor_stat_txn_time m_dao_stat_txn_time;
	dao_monitor_stat_txn_dp m_dao_stat_txn_dp;
	dao_monitor_stat_rule m_dao_stat_rule;
	
	@Override
	protected void post_run()
	{
		m_dao_stat_txn_area.close();
		m_dao_stat_txn_time.close();
		m_dao_stat_rule.close();
	}
	
	@Override
	protected void pre_run()
	{
		data_source ds = new data_source((DataSource) bean.get("tmsDataSource"));
		m_dao_stat_txn_area = new dao_monitor_stat_txn_area(ds);
		m_dao_stat_txn_time = new dao_monitor_stat_txn_time(ds);
		m_dao_stat_txn_dp = new dao_monitor_stat_txn_dp(ds);
		m_dao_stat_rule = new dao_monitor_stat_rule(ds);
		monitor_stat = run_mntstat.load(ds, m_dao_stat_rule, m_dao_stat_txn_area);
	}
	
	List<run_db_commit> request = new ArrayList<run_db_commit>();
	List<run_db_commit> temp_list = new ArrayList<run_db_commit>();
	String batch_code, error;
	clock c = new clock();
	clock m_commit_clock = new clock();
	boolean is_delay = false;
	long max_txn_time = 0;
	
	protected void run_once()
	{
		try
		{
			_run_once();
		}
		catch (SQLException e)
		{
			log.error("mntstat commit error.", e);
		}
	}
	
	final <E> int update(String batch_code, E e, batch_stmt_jdbc_obj<E> stmt, Set<E> set) throws SQLException
	{
		int count = 0;
		if (e == null)
			return count;
		if (set.add(e)){
			stmt.update(batch_code, e);
			count++;
		}
		return count;
	}

	final <E> int update(String batch_code, List<E> e, batch_stmt_jdbc_obj<E> stmt, Set<E> set)
			throws SQLException
	{
		int count = 0;
		if (e == null)
			return count;

		for (E e1 : e)
			count += update(batch_code, e1, stmt, set);
		return count;
	}

	final <E> int update(String batch_code, E e, batch_stmt_jdbc_obj<E> stmt) throws SQLException
	{
		int count = 0;
		if (e == null)
			return count;
		stmt.update(batch_code, e);
		return ++count;
	}

	final <E> int update(String batch_code, List<E> e, batch_stmt_jdbc_obj<E> stmt) throws SQLException
	{
		int count = 0;
		if (e == null)
			return count;

		for (E e1 : e)
			count += update(batch_code, e1, stmt);
		return count;
	}
	
	final <E> int update(String batch_code, Set<E> e, batch_stmt_jdbc_obj<E> stmt) throws SQLException
	{
		int count = 0;
		if (e == null)
			return count;

		for (E e1 : e)
			count += update(batch_code, e1, stmt);
		return count;
	}

	final <E extends row_in_db> void set_indb(Set<E> e)
	{
		if (e == null)
			return;

		for (E e1 : e)
			e1.set_indb(true);
	}

	final <E extends row_in_db> void set_indb(E e)
	{
		if (e == null)
			return;

		e.set_indb(true);
	}
	
	protected void _run_once() throws SQLException
	{
		request.clear();
		super.drainTo(request, batch_size, batch_time);
		if (!isOnStat)
		{
			return;
		}
		if (!request.isEmpty())
			temp_list.addAll(request);
		if (temp_list.isEmpty())
			return;
		long update_count = 0, commit_size = 0, curr_txn_time = 0;
		try {
			//实时监控统计
			long curr_time = System.currentTimeMillis();// 当前时间
			Iterator<run_db_commit> it = temp_list.iterator();
			while (it.hasNext())
			{
				run_db_commit row = it.next();
				it.remove();
				run_txn_values rtv = row.get_txn_data();
				if (rtv == null)
					continue;
		        monitor_stat.monitor_stat(row, curr_time);
		        curr_txn_time = ((Number) rtv.get_fd(rtv.m_env.field_cache().INDEX_TXNTIME)).longValue();
		        if (max_txn_time < curr_txn_time)
		        {
		        	max_txn_time = curr_txn_time;
		        }
		        commit_size = monitor_stat.get_stat_commit().size();
		        if (commit_size >= commit_maxsize)
		        	break;
            }
			// add by 2015-03-26--用于所有获取数据处理完成后的逻辑处理
			monitor_stat.monitor_stat_commit();
			// end
			if (commit_size == 0 || (commit_size < commit_maxsize
					&& m_commit_clock.count_ms() < commit_maxperiod))
			{
				is_delay = true;
				return;
			}
			is_delay = false;
			if (isStatSyncLog) {
	            batch_code = dataSyncLog.get_batch_code(name(), tm_tool.lctm_ms());
	            dataSyncLog.batch_print_begin(batch_code);
			} else {
			    batch_code = null;
			}
			run_stat_commit stat_commit = monitor_stat.get_stat_commit();
            {
            	update_count += update(batch_code, stat_commit.get_stat_txn_areas(), m_dao_stat_txn_area);
            	update_count += update(batch_code, stat_commit.get_stat_txn_times(), m_dao_stat_txn_time);
            	update_count += update(batch_code, stat_commit.get_stat_txn_dps(), m_dao_stat_txn_dp);
            	update_count += update(batch_code, stat_commit.get_stat_rules(), m_dao_stat_rule);

            	m_dao_stat_txn_area.flush();
            	m_dao_stat_txn_time.flush();
            	m_dao_stat_txn_dp.flush();
                m_dao_stat_rule.flush();
                c.reset();
                m_dao_stat_txn_area.commit();
                if (c.count_ms() > 1000)
                {
                	log.warn("[" + name() + "]- 实时监控统计" + temp_list.size() + "笔交易, " + update_count + "条数据库操作, 提交耗时: " + c.count_ms() + "ms.");
                }
                monitor_stat.remove_timeout(max_txn_time, curr_time);
                
				set_indb(stat_commit.get_stat_txn_areas());
				set_indb(stat_commit.get_stat_txn_times());
				set_indb(stat_commit.get_stat_rules());
            }
		} catch (SQLException e) {
            m_dao_stat_txn_area.reset_update_pos();
            m_dao_stat_txn_time.reset_update_pos();
            m_dao_stat_txn_dp.reset_update_pos();
            m_dao_stat_rule.reset_update_pos();
            m_dao_stat_txn_area.rollback();
            error = e.getLocalizedMessage();
            throw e;
		} catch (Exception e) {
			log.error("commit thread other error.", e);
		} finally {
			// 是否延迟提交, 是的话，不打印日志, 不是的才打印
			if (!is_delay) {
				max_txn_time = 0;
				m_commit_clock.reset();
				monitor_stat.get_stat_commit().clear();
				if (isStatSyncLog) {
			        dataSyncLog.batch_print_end(batch_code, update_count, (error == null
			                ? dataSyncLog.Action.COMMIT : dataSyncLog.Action.ROLLBACK));
			    }
			}
        }
	}
	
	@Override
	public void shutdown(boolean abort)
	{
		for (; m_request.size() > 0;)
			delay_tool.delay(100);
		super.shutdown(abort);
	}
}