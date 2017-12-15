package cn.com.higinet.tms35.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import cn.com.higinet.tms35.core.cache.db_device;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public class dao_device_read {
	static String sql = "select DEVICE_ID, RANDOM_NUM, CHANNEL_DEVICEID, APP_ID, CREATE_TIME,"
			+ " LASTMODIFIED, PROP_VALUES from TMS_DFP_DEVICE where DEVICE_ID=?";
	
	batch_stmt_jdbc read_stmt;

	public dao_device_read(data_source ds)
	{
		read_stmt = new batch_stmt_jdbc(ds, sql, new int[] { Types.VARCHAR });
	}
	
	db_device read(String device_id) throws SQLException
	{
		final db_device device = new db_device();
		read_stmt.query(new Object[] { device_id }, new row_fetch()
		{
			public boolean fetch(ResultSet rs) throws SQLException
			{
				device.device_id = rs.getLong("DEVICE_ID");
				device.random_num = rs.getString("RANDOM_NUM");
				device.channel_deviceid = rs.getString("CHANNEL_DEVICEID");
				device.app_id = rs.getString("APP_ID");
				device.create_time = rs.getLong("CREATE_TIME");
				device.lastmodified = rs.getLong("LASTMODIFIED");
				device.prop_values = rs.getString("PROP_VALUES");
				device.set_indb(true);
				return true;
			}
		});
		return device;
	}

	public void close()
	{
		read_stmt.close();
	}
}
