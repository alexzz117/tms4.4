package cn.com.higinet.tms.engine.core.dao;

import java.sql.SQLException;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.cache.db_monitor_stat_txn_dp;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;

public class dao_monitor_stat_txn_dp extends batch_stmt_jdbc_obj<db_monitor_stat_txn_dp> {
	static Logger log = LoggerFactory.getLogger(dao_monitor_stat_txn_dp.class);

	dao_monitor_stat_txn_dp_insert insert;
	dao_monitor_stat_txn_dp_delete delete;

	public dao_monitor_stat_txn_dp(data_source ds) {
//		super(ds, null, null);
		insert = new dao_monitor_stat_txn_dp_insert(ds);
		delete = new dao_monitor_stat_txn_dp_delete(ds);
	}
	
	@Override
	public void delete(String batch_code, db_monitor_stat_txn_dp e) throws SQLException
    {
        update(batch_code, e);
    }
	@Override
	public void delete(db_monitor_stat_txn_dp e) throws SQLException
	{
		 delete.update(e);
	}
	
	@Override
	public void update(db_monitor_stat_txn_dp e) throws SQLException
	{
		update(null, e);
	}
	
	@Override
	public void update(String batch_code, db_monitor_stat_txn_dp e) throws SQLException
	{
		if (e.is_indb()) {
			insert.update(batch_code, e);
		}else{
			delete.update(batch_code, e);
		}
	}

	@Override
	public String name() {
		return "monitor_stat_txn_dp";
	}
	
	@Override
	public void close()
	{
		delete.close();
		insert.close();
//		super.close();
	}

	@Override
	public void reset_update_pos()
	{
		delete.reset_update_pos();
		insert.reset_update_pos();
	}

	@Override
	public void flush() throws SQLException
	{
		delete.flush();
		insert.flush();
	}

	@Override
	public Object[] toArray(db_monitor_stat_txn_dp u)
	{
		return (u.is_indb()) ? insert.toArray(u) : delete.toArray(u);
	}
}

class dao_monitor_stat_txn_dp_insert extends batch_stmt_jdbc_obj<db_monitor_stat_txn_dp> {
	static Logger log = LoggerFactory.getLogger(dao_monitor_stat_txn_dp_insert.class);
	static String sql = "insert into TMS_MONITOR_TXN_DP_STAT (TXNCODE,IPPORT, COUNTRYCODE, REGIONCODE, CITYCODE, CHANNELID, TXNID, TIME, DISPOSAL) "
			+ "values(?,?,?,?,?,?,?,?,?)";
	static int[] sql_param_type = new int[] { 
		Types.VARCHAR,Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, 
			Types.VARCHAR, Types.VARCHAR, Types.BIGINT, Types.VARCHAR};

	public dao_monitor_stat_txn_dp_insert(data_source ds) {
		super(ds, sql, sql_param_type);
	}

	@Override
	public String name() {
		return "monitor_stat_txn_dp_insert";
	}

	@Override
	public Object[] toArray(db_monitor_stat_txn_dp e) {
		return new Object[] { e.txncode,e.ipport, e.countrycode,e.regioncode,e.citycode,
				e.channelid,e.txnid,e.time,e.disposal};
	}
}

class dao_monitor_stat_txn_dp_delete extends batch_stmt_jdbc_obj<db_monitor_stat_txn_dp> {
	static Logger log = LoggerFactory.getLogger(dao_monitor_stat_txn_dp_delete.class);
	static String sql = "delete from TMS_MONITOR_TXN_DP_STAT where TXNCODE = ?";
	static int[] sql_param_type = new int[] {Types.VARCHAR };
	
	public dao_monitor_stat_txn_dp_delete(data_source ds)
	{
		super(ds, sql, sql_param_type);
	}

	@Override
	public String name() {
		return "monitor_stat_txn_dp_delete";
	}

	@Override
	public Object[] toArray(db_monitor_stat_txn_dp e) {
		return new Object[] { e.txncode};
	}
}