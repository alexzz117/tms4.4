package cn.com.higinet.tms.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.cache.db_monitor_stat_rule;
import cn.com.higinet.tms.engine.core.cache.linear;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

public class dao_monitor_stat_rule extends batch_stmt_jdbc_obj<db_monitor_stat_rule> {
	static Logger log = LoggerFactory.getLogger(dao_monitor_stat_rule.class);

	dao_monitor_stat_rule_insert insert;
	dao_monitor_stat_rule_update update;
	dao_monitor_stat_rule_read select;

	public dao_monitor_stat_rule(data_source ds) {
		super(null, null, null);
		insert = new dao_monitor_stat_rule_insert(ds);
		update = new dao_monitor_stat_rule_update(ds);
		select = new dao_monitor_stat_rule_read(ds);
	}
	
	@Override
	public void update(db_monitor_stat_rule e) throws SQLException
	{
		update(null, e);
	}
	
	@Override
	public void update(String batch_code, db_monitor_stat_rule e) throws SQLException
	{
		if (e.is_indb())
			update.update(batch_code, e);
		else
			insert.update(batch_code, e);
	}
	
	@Override
	public linear<db_monitor_stat_rule> read_list(Object... p) throws SQLException
	{
		return select.read_list((String) p[0], (Long) p[1]);
	}

	@Override
	public String name() {
		return "monitor_stat_rule";
	}
	@Override
	public void close()
	{
		insert.close();
		update.close();
		select.close();
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
	public Object[] toArray(db_monitor_stat_rule u)
	{
		return (u.is_indb()) ? update.toArray(u) : insert.toArray(u);
	}
}

class dao_monitor_stat_rule_read {
	static Logger log = LoggerFactory.getLogger(dao_monitor_stat_rule_read.class);
	static String sql = "select IPPORT, TXNID, RULEID, TIME, TRIG_NUMBER, HIT_NUMBER, "
			+ "RULE_RUNTIME_MAX, RULE_RUNTIME_MIN, RULE_RUNTIME_AVG from TMS_MONITOR_RULE_STAT "
			+ "where IPPORT = ? and TIME = ?";
	static int[] sql_param_type = new int[] { Types.VARCHAR, Types.BIGINT };

	batch_stmt_jdbc read_stmt;
	
	public dao_monitor_stat_rule_read(data_source ds)
	{
		read_stmt = new batch_stmt_jdbc(ds, sql, new int[] { Types.VARCHAR, Types.BIGINT });
	}
	
	linear<db_monitor_stat_rule> read_list(String ipport, long time) throws SQLException
	{
		final linear<db_monitor_stat_rule> list = new linear<db_monitor_stat_rule>();
		read_stmt.query(new Object[] { ipport, time }, new row_fetch()
		{
			public boolean fetch(ResultSet rs) throws SQLException
			{
				db_monitor_stat_rule stat = new db_monitor_stat_rule();
				stat.ipport = rs.getString("IPPORT");
			    stat.time = rs.getLong("TIME");
		        stat.txnid = rs.getString("TXNID");
			    stat.ruleid = rs.getString("RULEID");
			    stat.trig_number = rs.getLong("TRIG_NUMBER");
			    stat.hit_number = rs.getLong("HIT_NUMBER");
			    stat.rule_runtime_max = rs.getLong("RULE_RUNTIME_MAX");
			    stat.rule_runtime_min = rs.getLong("RULE_RUNTIME_MAX");
			    stat.rule_runtime_avg = rs.getFloat("RULE_RUNTIME_AVG");
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

class dao_monitor_stat_rule_insert extends batch_stmt_jdbc_obj<db_monitor_stat_rule> {
	static Logger log = LoggerFactory.getLogger(dao_monitor_stat_rule_insert.class);
	static String sql = "insert into TMS_MONITOR_RULE_STAT(IPPORT, TXNID, RULEID, TIME, MODTIME, "
			+ "TRIG_NUMBER, HIT_NUMBER, RULE_RUNTIME_MAX, RULE_RUNTIME_MIN, RULE_RUNTIME_AVG)" //
			+ "values(?,?,?,?,?,?,?,?,?,?)";
	static int[] sql_param_type = new int[] { Types.VARCHAR, Types.VARCHAR, 
			Types.VARCHAR, Types.BIGINT, Types.BIGINT, Types.BIGINT,
			Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.DOUBLE };

	public dao_monitor_stat_rule_insert(data_source ds) {
		super(ds, sql, sql_param_type);
	}

	@Override
	public String name() {
		return "monitor_stat_rule_insert";
	}

	@Override
	public Object[] toArray(db_monitor_stat_rule e) {
		return new Object[] { e.ipport, e.txnid, e.ruleid, e.time, e.modtime, e.trig_number, e.hit_number, 
				e.rule_runtime_max, e.rule_runtime_min, (long) (e.rule_runtime_avg * 100) / 100. };
	}

}

class dao_monitor_stat_rule_update extends batch_stmt_jdbc_obj<db_monitor_stat_rule> {
	static Logger log = LoggerFactory.getLogger(dao_monitor_stat_rule_update.class);
	static String sql = "update TMS_MONITOR_RULE_STAT set MODTIME = ?, TRIG_NUMBER = ?, HIT_NUMBER = ?, "
			+ "RULE_RUNTIME_MAX = ?, RULE_RUNTIME_MIN = ?, RULE_RUNTIME_AVG = ? where "
			+ "IPPORT = ? and TXNID = ? and RULEID = ? and TIME = ?";
	static int[] sql_param_type = new int[] { Types.BIGINT, Types.BIGINT, Types.BIGINT,
			Types.BIGINT, Types.BIGINT, Types.DOUBLE, Types.VARCHAR, 
			Types.VARCHAR, Types.VARCHAR, Types.BIGINT };
	public dao_monitor_stat_rule_update(data_source ds) {
		super(ds, sql, sql_param_type);
	}

	@Override
	public String name() {
		return "monitor_stat_rule_update";
	}

	@Override
	public Object[] toArray(db_monitor_stat_rule e) {
		return new Object[] { e.modtime, e.trig_number, e.hit_number, e.rule_runtime_max, e.rule_runtime_min,
				(long) (e.rule_runtime_avg * 100) / 100., e.ipport, e.txnid, e.ruleid, e.time };
	}
}