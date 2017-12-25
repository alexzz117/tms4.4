package cn.com.higinet.tms.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import cn.com.higinet.tms.engine.core.cache.db_alarm_action;
import cn.com.higinet.tms.engine.core.cache.linear;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

public class dao_alarm_action_read {
	static final String sql = "select AC_ID, TXN_CODE, AC_NAME, AC_COND, AC_EXPR, CREATE_TIME, "
			+ "EXECUE_TIME, EXECUE_RESULT, EXECUE_INFO from TMS_MGR_ALARM_ACTION where TXN_CODE = ?";
	batch_stmt_jdbc read_stmt;

	public dao_alarm_action_read(data_source ds)
	{
		read_stmt = new batch_stmt_jdbc(ds, sql, new int[] { Types.VARCHAR });
	}

	linear<db_alarm_action> read_list(String txn_code) throws SQLException
	{
		final linear<db_alarm_action> list = new linear<db_alarm_action>();
		read_stmt.query(new Object[] { txn_code }, new row_fetch()
		{
			public boolean fetch(ResultSet rs) throws SQLException
			{
				int c = 0;
				db_alarm_action e = new db_alarm_action();
				e.ac_id = rs.getLong(++c);
				e.txn_code = rs.getString(++c);
				e.ac_name = rs.getString(++c);
				e.ac_cond = rs.getString(++c);
				e.ac_expr = rs.getString(++c);
				e.create_time = rs.getLong(++c);
				e.execue_time = rs.getLong(++c);
				e.execue_result = rs.getString(++c);
				e.execue_info = rs.getString(++c);
				list.add(e);
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