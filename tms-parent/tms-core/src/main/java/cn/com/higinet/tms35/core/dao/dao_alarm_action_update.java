package cn.com.higinet.tms35.core.dao;

import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.core.cache.db_alarm_action;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public class dao_alarm_action_update extends batch_stmt_jdbc_obj<db_alarm_action> {
	static Logger log = LoggerFactory.getLogger(dao_alarm_action_update.class);
	
	static String sql = "update TMS_MGR_ALARM_ACTION set EXECUE_TIME = ?, " +
			"EXECUE_RESULT = ?, EXECUE_INFO = ? where AC_ID = ?";
	static int[] sql_param_type = new int[] { Types.BIGINT, 
		Types.VARCHAR, Types.VARCHAR, Types.BIGINT };

	@Override
	public Object[] toArray(db_alarm_action e)
	{
		return new Object[] { e.execue_time, e.execue_result, e.execue_info, e.ac_id };
	}

	public dao_alarm_action_update(data_source ds)
	{
		super(ds, sql, sql_param_type);
	}

	@Override
	public String name()
	{
		return "process_action_update";
	}
}