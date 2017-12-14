package cn.com.higinet.tms35.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.core.cache.db_monitor_stat_txn_area;
import cn.com.higinet.tms35.core.cache.linear;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public class dao_monitor_stat_txn_area extends batch_stmt_jdbc_obj<db_monitor_stat_txn_area> {
	static Logger log = LoggerFactory.getLogger(dao_monitor_stat_txn_area.class);

	dao_monitor_stat_txn_area_insert insert;
	dao_monitor_stat_txn_area_update update;
	dao_monitor_stat_txn_area_read select;

	public dao_monitor_stat_txn_area(data_source ds) {
		super(ds, null, null);
		insert = new dao_monitor_stat_txn_area_insert(ds);
		update = new dao_monitor_stat_txn_area_update(ds);
		select = new dao_monitor_stat_txn_area_read(ds);
	}
	
	@Override
	public void update(db_monitor_stat_txn_area e) throws SQLException
	{
		update(null, e);
	}
	
	@Override
	public void update(String batch_code, db_monitor_stat_txn_area e) throws SQLException
	{
		if (e.is_indb())
			update.update(batch_code, e);
		else
			insert.update(batch_code, e);
	}
	
	@Override
	public linear<db_monitor_stat_txn_area> read_list(Object... p) throws SQLException
	{
		return select.read_list((String) p[0], (Long) p[1]);
	}

	@Override
	public String name() {
		return "monitor_stat_txn_area";
	}
	
	@Override
	public void close()
	{
		insert.close();
		update.close();
		select.close();
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
	public Object[] toArray(db_monitor_stat_txn_area u)
	{
		return (u.is_indb()) ? update.toArray(u) : insert.toArray(u);
	}
}

class dao_monitor_stat_txn_area_read {
	static Logger log = LoggerFactory.getLogger(dao_monitor_stat_txn_area_read.class);
	static String sql = "select IPPORT, COUNTRYCODE, REGIONCODE, "
			+ "CITYCODE, CHANNELID, TXNID, TIME, TXN_NUMBER, TXN_RUNTIME_MAX, TXN_RUNTIME_MIN, "
			+ "TXN_RUNTIME_AVG, MR_NUMBER, MRNOT_NUMBER, MR_AP_NUMBER, MR_APNOT_NUMBER, MR_AP_CR_NUMBER, "
			+ "MR_AP_CRNOT_NUMBER, MR_APNOT_CR_NUMBER, MR_APNOT_CRNOT_NUMBER, MRNOT_CR_NUMBER, "
			+ "MRNOT_CRNOT_NUMBER, MODEL_ID, STATDATE, DISPOSAL, DISPOSAL_NUM "
			+ "from TMS_MONITOR_TXN_AREA_STAT where IPPORT = ? and TIME = ?";
	static int[] sql_param_type = new int[] { Types.VARCHAR, Types.BIGINT };

	batch_stmt_jdbc read_stmt;
	
	public dao_monitor_stat_txn_area_read(data_source ds)
	{
		read_stmt = new batch_stmt_jdbc(ds, sql, new int[] { Types.VARCHAR, Types.BIGINT });
	}
	
	linear<db_monitor_stat_txn_area> read_list(String ipport, long time) throws SQLException
	{
		final linear<db_monitor_stat_txn_area> list = new linear<db_monitor_stat_txn_area>();
		read_stmt.query(new Object[] { ipport, time }, new row_fetch()
		{
			public boolean fetch(ResultSet rs) throws SQLException
			{
				db_monitor_stat_txn_area stat = new db_monitor_stat_txn_area();
				stat.ipport = rs.getString("IPPORT");
			    stat.time = rs.getLong("TIME");
			    stat.countrycode = rs.getString("COUNTRYCODE");
		        stat.regioncode = rs.getString("REGIONCODE");
		        stat.citycode = rs.getString("CITYCODE");
			    stat.channelid = rs.getString("CHANNELID");
		        stat.txnid = rs.getString("TXNID");
		        stat.txn_number = rs.getLong("TXN_NUMBER");
		        stat.txn_runtime_max = rs.getLong("TXN_RUNTIME_MAX");
		        stat.txn_runtime_min = rs.getLong("TXN_RUNTIME_MIN");
		        stat.txn_runtime_avg = rs.getFloat("TXN_RUNTIME_AVG");
		        stat.mr_number = rs.getLong("MR_NUMBER");
		        stat.mrnot_number = rs.getLong("MRNOT_NUMBER");
		        stat.mr_ap_number = rs.getLong("MR_AP_NUMBER");
		        stat.mr_apnot_number = rs.getLong("MR_APNOT_NUMBER");
		        stat.model_id = rs.getString("MODEL_ID");
		        stat.statdate = rs.getInt("STATDATE");
		        stat.disposal = rs.getString("DISPOSAL");
		        stat.disposal_num = rs.getLong("DISPOSAL_NUM");
				stat.set_indb(true);
				list.add(stat);
				return true;
			}
		});
		return list;
	}

	public void close()
	{
		read_stmt.close();
	}
}

class dao_monitor_stat_txn_area_insert extends batch_stmt_jdbc_obj<db_monitor_stat_txn_area> {
	static Logger log = LoggerFactory.getLogger(dao_monitor_stat_txn_area_insert.class);
	static String sql = "insert into TMS_MONITOR_TXN_AREA_STAT(IPPORT, COUNTRYCODE, REGIONCODE, CITYCODE, "
			+ "CHANNELID, TXNID, TIME, MODTIME, TXN_NUMBER, TXN_RUNTIME_MAX, TXN_RUNTIME_MIN, TXN_RUNTIME_AVG, "
			+ "MR_NUMBER, MRNOT_NUMBER, MR_AP_NUMBER, MR_APNOT_NUMBER, MR_AP_CR_NUMBER, MR_AP_CRNOT_NUMBER, "
            + "MR_APNOT_CR_NUMBER, MR_APNOT_CRNOT_NUMBER, MRNOT_CR_NUMBER, MRNOT_CRNOT_NUMBER, MODEL_ID, STATDATE, DISPOSAL, DISPOSAL_NUM)"
			+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	static int[] sql_param_type = new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, 
			Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.DOUBLE, 
			Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.BIGINT, 
			Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.VARCHAR, Types.BIGINT, Types.VARCHAR, Types.BIGINT };

	public dao_monitor_stat_txn_area_insert(data_source ds) {
		super(ds, sql, sql_param_type);
	}

	@Override
	public String name() {
		return "monitor_txn_stat_area_insert";
	}

	@Override
	public Object[] toArray(db_monitor_stat_txn_area e) {
		return new Object[] {e.ipport, e.countrycode, e.regioncode, e.citycode, 
				e.channelid, e.txnid, e.time, e.modtime, e.txn_number, 
				e.txn_runtime_max, e.txn_runtime_min, (long) (e.txn_runtime_avg * 100) / 100., e.mr_number, e.mrnot_number,
				e.mr_ap_number, e.mr_apnot_number, e.mr_ap_cr_number, e.mr_ap_crnot_number,
				e.mr_apnot_cr_number, e.mr_apnot_crnot_number, e.mrnot_cr_number, e.mrnot_crnot_number, e.model_id, e.statdate,
				e.disposal, e.disposal_num };
	}
}

class dao_monitor_stat_txn_area_update extends batch_stmt_jdbc_obj<db_monitor_stat_txn_area> {
	static Logger log = LoggerFactory.getLogger(dao_monitor_stat_txn_area_update.class);
	static String sql = "update TMS_MONITOR_TXN_AREA_STAT set MODTIME = ?, TXN_NUMBER = ?, TXN_RUNTIME_MAX = ?, "
			+ "TXN_RUNTIME_MIN = ?, TXN_RUNTIME_AVG = ?, MR_NUMBER = ?, MRNOT_NUMBER = ?, "
			+ "MR_AP_NUMBER = ?, MR_APNOT_NUMBER = ?, MR_AP_CR_NUMBER = ?, "
            + "MR_AP_CRNOT_NUMBER = ?, MR_APNOT_CR_NUMBER = ?, MR_APNOT_CRNOT_NUMBER = ?, "
            + "MRNOT_CR_NUMBER = ?, MRNOT_CRNOT_NUMBER = ?, DISPOSAL_NUM = ? "
            + "where IPPORT = ? and COUNTRYCODE = ? and REGIONCODE = ? and CITYCODE = ? "
            + "and CHANNELID = ? and TXNID = ? and TIME = ? and MODEL_ID = ? and DISPOSAL = ?";
	static int[] sql_param_type = new int[] { Types.BIGINT, Types.BIGINT, Types.BIGINT, 
			Types.BIGINT, Types.DOUBLE, Types.BIGINT, Types.BIGINT, 
			Types.BIGINT, Types.BIGINT, Types.BIGINT, 
			Types.BIGINT, Types.BIGINT, Types.BIGINT, 
            Types.BIGINT, Types.BIGINT, Types.BIGINT, 
            Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, 
            Types.VARCHAR, Types.VARCHAR, Types.BIGINT, Types.VARCHAR, Types.VARCHAR };
	public dao_monitor_stat_txn_area_update(data_source ds) {
		super(ds, sql, sql_param_type);
	}

	@Override
	public String name() {
		return "monitor_stat_txn_area_update";
	}

	@Override
	public Object[] toArray(db_monitor_stat_txn_area e) {
		return new Object[] { e.modtime, e.txn_number, e.txn_runtime_max, e.txn_runtime_min, (long) (e.txn_runtime_avg * 100) / 100.,
				e.mr_number, e.mrnot_number, e.mr_ap_number, e.mr_apnot_number,
				e.mr_ap_cr_number, e.mr_ap_crnot_number, e.mr_apnot_cr_number, e.mr_apnot_crnot_number,
				e.mrnot_cr_number, e.mrnot_crnot_number, e.disposal_num, e.ipport, e.countrycode, e.regioncode, 
				e.citycode, e.channelid, e.txnid, e.time, e.model_id, e.disposal };
	}
}