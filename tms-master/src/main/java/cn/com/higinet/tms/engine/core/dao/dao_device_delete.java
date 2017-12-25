package cn.com.higinet.tms.engine.core.dao;

import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.cache.db_device;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;

public class dao_device_delete extends batch_stmt_jdbc_obj<db_device> {
	static Logger log = LoggerFactory.getLogger(dao_device_delete.class);
	static String sql = "delete from TMS_DFP_DEVICE where DEVICE_ID=?";
	static int[] sql_param_type = new int[] { Types.BIGINT };
	public dao_device_delete(data_source ds)
	{
		super(ds, sql, sql_param_type);
	}

	@Override
	public String name() {
		return "device_delete";
	}

	@Override
	public Object[] toArray(db_device e) {
		return new Object[] { e.device_id };
	}
}