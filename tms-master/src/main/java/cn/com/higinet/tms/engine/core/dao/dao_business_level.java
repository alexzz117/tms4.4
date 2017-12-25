package cn.com.higinet.tms.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import cn.com.higinet.tms.engine.core.cache.db_business_level;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

public class dao_business_level {
	static final String sql = "select RR_ID, RATEKIND_ID, RS_ID, SCORE, RISKLEVEL"
		+ ", MODTIME  from tms_mgr_rateresult where RATEKIND_ID=?";
	
	batch_stmt_jdbc stmt;
	public dao_business_level(data_source ds)
	{
		stmt = new batch_stmt_jdbc(ds, sql, new int[] { Types.VARCHAR });
	}
	
	public db_business_level read(String business_id) throws SQLException {
		final db_business_level b = new db_business_level(business_id);
		stmt.query(new Object[] { business_id }, new row_fetch() {
			@Override
			public boolean fetch(ResultSet rs) throws SQLException {
				int c = 0;
				b.rr_id = rs.getString(++c);
				b.ratekind_id = rs.getString(++c);
				b.rs_id = rs.getString(++c);
				b.score = rs.getLong(++c);
				b.level = rs.getLong(++c);
				b.modtime = rs.getLong(++c);
				b.set_indb(true);
				return true;
			}
		});
		return b;
	}
	
	public void close()
	{
		stmt.close();
	}
}