package cn.com.higinet.tms.engine.core.dao;

import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.cache.db_device_user;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;

public class dao_device_user_update extends batch_stmt_jdbc_obj<db_device_user> {
	static Logger log = LoggerFactory.getLogger(dao_device_user_update.class);
	static String sql = "update TMS_DFP_USER_DEVICE set DEVICE_ID=?, STATUS=?, LASTMODIFIED=?"
			+ " where DEVICE_ID=? and USER_ID=?";
	static int[] sql_param_type = new int[] { Types.BIGINT, Types.INTEGER, Types.BIGINT, Types.BIGINT, Types.VARCHAR};
	
	public dao_device_user_update(data_source ds)
	{
		super(ds, sql, sql_param_type);
	}
	
	@Override
	public String name() {
		return "device_user_update";
	}

	@Override
	public Object[] toArray(db_device_user e) {
		return new Object[] { e.device_id, e.status, e.lastmodified, e.old_device_id < 0 ? e.device_id : e.old_device_id, e.user_id};
	}
}