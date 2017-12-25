package cn.com.higinet.tms.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import cn.com.higinet.tms.engine.core.cache.db_device_user;
import cn.com.higinet.tms.engine.core.cache.linear;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

public class dao_device_user_read {
	protected final static String sql = "select DEVICE_ID, USER_ID, STATUS, LASTMODIFIED, CREATETIME from TMS_DFP_USER_DEVICE where DEVICE_ID=? and USER_ID=?";
	protected final static String sql_list = "select DEVICE_ID, USER_ID, STATUS, LASTMODIFIED ,CREATETIME from TMS_DFP_USER_DEVICE where DEVICE_ID=?";
	protected final static String SELECT_BY_USER = "select DEVICE_ID, USER_ID, STATUS, LASTMODIFIED ,CREATETIME from TMS_DFP_USER_DEVICE where USER_ID=? ORDER BY LASTMODIFIED DESC";
	
	batch_stmt_jdbc read_stmt;
	batch_stmt_jdbc read_stmt_list;
	batch_stmt_jdbc readStmtListByUser;

	public dao_device_user_read(data_source ds)
	{
		read_stmt = new batch_stmt_jdbc(ds, sql, new int[] { Types.BIGINT, Types.VARCHAR });
		read_stmt_list = new batch_stmt_jdbc(ds, sql_list, new int[] { Types.BIGINT });
		readStmtListByUser = new batch_stmt_jdbc(ds, SELECT_BY_USER, new int[] { Types.VARCHAR });
	}
	
	db_device_user read(long device_id, String user_id) throws SQLException
	{
		final db_device_user device_user = new db_device_user();
		read_stmt.query(new Object[] { device_id, user_id }, new row_fetch()
		{
			public boolean fetch(ResultSet rs) throws SQLException
			{
				device_user.device_id = rs.getLong("DEVICE_ID");
				device_user.user_id = rs.getString("USER_ID");
				device_user.status = rs.getInt("STATUS");
				device_user.lastmodified = rs.getLong("LASTMODIFIED");
				device_user.createTime = rs.getLong("CREATETIME");
				device_user.set_indb(true);
				return true;
			}
		});
		return device_user;
	}
	
	linear<db_device_user> read_list(long device_id) throws SQLException
	{
		final linear<db_device_user> list = new linear<db_device_user>();
		read_stmt_list.query(new Object[] { device_id }, new row_fetch()
		{
			public boolean fetch(ResultSet rs) throws SQLException
			{
				db_device_user device_user = new db_device_user();
				device_user.device_id = rs.getLong("DEVICE_ID");
				device_user.user_id = rs.getString("USER_ID");
				device_user.status = rs.getInt("STATUS");
				device_user.lastmodified = rs.getLong("LASTMODIFIED");
				device_user.createTime = rs.getLong("CREATETIME");
				device_user.set_indb(true);
				list.add(device_user);
				return true;
			}
		});
		return list;
	}
	
	linear<db_device_user> readListByUser(String userId) throws SQLException
	{
		final linear<db_device_user> list = new linear<db_device_user>();
		readStmtListByUser.query(new Object[] { userId }, new row_fetch()
		{
			public boolean fetch(ResultSet rs) throws SQLException
			{
				db_device_user device_user = new db_device_user();
				device_user.device_id = rs.getLong("DEVICE_ID");
				device_user.user_id = rs.getString("USER_ID");
				device_user.status = rs.getInt("STATUS");
				device_user.lastmodified = rs.getLong("LASTMODIFIED");
				device_user.createTime = rs.getLong("CREATETIME");
				device_user.set_indb(true);
				list.add(device_user);
				return true;
			}
		});
		return list;
	}

	public void close()
	{
		read_stmt.close();
		read_stmt_list.close();
		readStmtListByUser.close();
	}
}
