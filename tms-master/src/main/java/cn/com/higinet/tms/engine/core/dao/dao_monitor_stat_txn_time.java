package cn.com.higinet.tms.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.cache.db_monitor_stat_txn_time;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms.engine.run.run_env;

public class dao_monitor_stat_txn_time extends batch_stmt_jdbc_obj<db_monitor_stat_txn_time> {
	static Logger log = LoggerFactory.getLogger(dao_monitor_stat_txn_time.class);

	dao_monitor_stat_txn_time_read select;
	dao_monitor_stat_txn_time_insert insert;
	dao_monitor_stat_txn_time_update update;

	public dao_monitor_stat_txn_time(data_source ds) {
		super(ds, null, null);
		select = new dao_monitor_stat_txn_time_read(ds);
		insert = new dao_monitor_stat_txn_time_insert(ds);
		update = new dao_monitor_stat_txn_time_update(ds);
	}
	
	public db_monitor_stat_txn_time read(Object... p) throws SQLException {
		return null;
	}
	
	db_monitor_stat_txn_time read(run_env re) throws SQLException {
		db_monitor_stat_txn_time ret = select.read(re);
		return ret;
	}
	
	@Override
	public void update(db_monitor_stat_txn_time e) throws SQLException
	{
		update(null, e);
	}
	
	@Override
	public void update(String batch_code, db_monitor_stat_txn_time e) throws SQLException
	{
		if (e.is_indb())
			update.update(batch_code, e);
		else
			insert.update(batch_code, e);
	}

	@Override
	public String name() {
		return "monitor_stat_txn_time";
	}
	
	@Override
	public void close()
	{
		select.close();
		insert.close();
		update.close();
		super.close();
	}

	@Override
	public void reset_update_pos()
	{
		insert.reset_update_pos();
		update.reset_update_pos();
	}

	@Override
	public void flush() throws SQLException
	{
		update.flush();
		insert.flush();
	}

	@Override
	public Object[] toArray(db_monitor_stat_txn_time u)
	{
		return (u.is_indb()) ? update.toArray(u) : insert.toArray(u);
	}
}

class dao_monitor_stat_txn_time_read {
	static Logger log = LoggerFactory.getLogger(dao_monitor_stat_txn_time_read.class);
	static String sql = "select IPPORT, CHANNELID, TXNID, TIME, RISK_EVAL_NUMBER, RISK_EVAL_RUNTIME_AVG, " +
			"RISK_EVAL_RUNTIME_MAX, RISK_EVAL_RUNTIME_MIN, RISK_EVAL_TPM, RISK_CFM_NUMBER, RISK_CFM_RUNTIME_AVG, " +
			"RISK_CFM_RUNTIME_MAX, RISK_CFM_RUNTIME_MIN, RISK_CFM_TPM from TMS_MONITOR_TXN_TIME_STAT " +
			"where IPPORT = ? and CHANNELID = ? and TXNID = ? and TIME = ?";
	static int[] sql_param_type = new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BIGINT };
	batch_stmt_jdbc read_stmt;
	
	public dao_monitor_stat_txn_time_read(data_source ds) {
		read_stmt = new batch_stmt_jdbc(ds, sql, sql_param_type);
	}
	
	db_monitor_stat_txn_time read(run_env re) throws SQLException
	{
		final db_monitor_stat_txn_time stat = new db_monitor_stat_txn_time(re);
		read_stmt.query(new Object[] { stat.ipport, stat.channelid, stat.txnid, stat.time }, new row_fetch()
		{
			public boolean fetch(ResultSet rs) throws SQLException
			{
				stat.ipport = rs.getString("IPPORT");
				stat.channelid = rs.getString("CHANNELID");
				stat.txnid = rs.getString("TXNID");
				stat.risk_eval_number = rs.getLong("RISK_EVAL_NUMBER");
				stat.risk_eval_runtime_avg = rs.getDouble("RISK_EVAL_RUNTIME_AVG");
				stat.risk_eval_runtime_max = rs.getLong("RISK_EVAL_RUNTIME_MAX");
				stat.risk_eval_runtime_min = rs.getLong("RISK_EVAL_RUNTIME_MIN");
				stat.set_risk_eval_tpm(rs.getString("RISK_EVAL_TPM"));
				stat.risk_cfm_number = rs.getLong("RISK_CFM_NUMBER");
				stat.risk_cfm_runtime_avg = rs.getDouble("RISK_CFM_RUNTIME_AVG");
				stat.risk_cfm_runtime_max = rs.getLong("RISK_CFM_RUNTIME_MAX");
				stat.risk_cfm_runtime_min = rs.getLong("RISK_CFM_RUNTIME_MIN");
				stat.set_risk_cfm_tpm(rs.getString("RISK_CFM_TPM"));
				stat.set_indb(true);
				return true;
			}
		});
		return stat;
	}
	
	public void close()
	{
		read_stmt.close();
	}
}

class dao_monitor_stat_txn_time_insert extends batch_stmt_jdbc_obj<db_monitor_stat_txn_time> {
	static Logger log = LoggerFactory.getLogger(dao_monitor_stat_txn_time_insert.class);
	static String sql = "insert into TMS_MONITOR_TXN_TIME_STAT(IPPORT, CHANNELID, TXNID, TIME, RISK_EVAL_NUMBER, " +
			"RISK_EVAL_RUNTIME_AVG, RISK_EVAL_RUNTIME_MAX, RISK_EVAL_RUNTIME_MIN, RISK_EVAL_TPM, RISK_CFM_NUMBER, " +
			"RISK_CFM_RUNTIME_AVG, RISK_CFM_RUNTIME_MAX, RISK_CFM_RUNTIME_MIN, RISK_CFM_TPM)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	static int[] sql_param_type = new int[] { 
			Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BIGINT, Types.BIGINT, 
			Types.DOUBLE, Types.BIGINT, Types.BIGINT, Types.VARCHAR, Types.BIGINT, 
			Types.DOUBLE, Types.BIGINT, Types.BIGINT, Types.VARCHAR };

	public dao_monitor_stat_txn_time_insert(data_source ds) {
		super(ds, sql, sql_param_type);
	}

	@Override
	public String name() {
		return "monitor_txn_stat_area_insert";
	}

	@Override
	public Object[] toArray(db_monitor_stat_txn_time e) {
		return new Object[] { e.ipport, e.channelid, e.txnid, e.time, e.risk_eval_number, 
				(long) (e.risk_eval_runtime_avg * 100) / 100., e.risk_eval_runtime_max, e.risk_eval_runtime_min, 
				e.risk_eval_tpm_str(), e.risk_cfm_number, (long) (e.risk_cfm_runtime_avg * 100) / 100., 
				e.risk_cfm_runtime_max, e.risk_cfm_runtime_min, e.risk_cfm_tpm_str()};
	}
}

class dao_monitor_stat_txn_time_update extends batch_stmt_jdbc_obj<db_monitor_stat_txn_time> {
	static Logger log = LoggerFactory.getLogger(dao_monitor_stat_txn_time_update.class);
	static String sql = "update TMS_MONITOR_TXN_TIME_STAT set RISK_EVAL_NUMBER = ?, RISK_EVAL_RUNTIME_AVG = ?, " +
			"RISK_EVAL_RUNTIME_MAX = ?, RISK_EVAL_RUNTIME_MIN = ?, RISK_EVAL_TPM = ?, RISK_CFM_NUMBER = ?, " +
			"RISK_CFM_RUNTIME_AVG = ?, RISK_CFM_RUNTIME_MAX = ?, RISK_CFM_RUNTIME_MIN = ?, RISK_CFM_TPM = ? " +
			"where IPPORT = ? and CHANNELID = ? and TXNID = ? and TIME = ?";
	static int[] sql_param_type = new int[] { Types.BIGINT, Types.DOUBLE, 
			Types.BIGINT, Types.BIGINT, Types.VARCHAR, Types.BIGINT, 
			Types.DOUBLE, Types.BIGINT, Types.BIGINT, Types.VARCHAR,
			Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BIGINT };
	public dao_monitor_stat_txn_time_update(data_source ds) {
		super(ds, sql, sql_param_type);
	}

	@Override
	public String name() {
		return "monitor_stat_txn_area_update";
	}

	@Override
	public Object[] toArray(db_monitor_stat_txn_time e) {
		return new Object[] {e.risk_eval_number,  (long) (e.risk_eval_runtime_avg * 100) / 100., 
				e.risk_eval_runtime_max, e.risk_eval_runtime_min, e.risk_eval_tpm_str(), e.risk_cfm_number,
				(long) (e.risk_cfm_runtime_avg * 100) / 100., e.risk_cfm_runtime_max, e.risk_cfm_runtime_min, 
				e.risk_cfm_tpm_str(), e.ipport, e.channelid, e.txnid, e.time };
	}
}