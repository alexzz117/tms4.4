package cn.com.higinet.tms.engine.core.dao;

import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.cache.db_device_user;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;

public class dao_device_user_insert extends batch_stmt_jdbc_obj<db_device_user> {
	static Logger log = LoggerFactory.getLogger(dao_device_user_insert.class);
	static String sql = "insert into TMS_DFP_USER_DEVICE (DEVICE_ID, USER_ID, STATUS, LASTMODIFIED,CREATETIME) values (?,?,?,?,?)";
	static int[] sql_param_type = new int[] { Types.BIGINT, Types.VARCHAR, Types.INTEGER, Types.BIGINT, Types.BIGINT};
	
	public dao_device_user_insert(data_source ds)
	{
		super(ds, sql, sql_param_type);
	}

	@Override
	public String name() {
		return "device_user_insert";
	}

	@Override
	public Object[] toArray(db_device_user e) {
		return new Object[] { e.device_id, e.user_id, e.status , e.lastmodified, e.createTime};
	}
}