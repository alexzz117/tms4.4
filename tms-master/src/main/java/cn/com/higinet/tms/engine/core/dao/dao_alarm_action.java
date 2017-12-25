package cn.com.higinet.tms.engine.core.dao;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.core.cache.db_alarm_action;
import cn.com.higinet.tms.engine.core.cache.linear;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;

public class dao_alarm_action extends batch_stmt_jdbc_obj<db_alarm_action> {

	static Logger log = LoggerFactory.getLogger(dao_trafficdata.class);
	
	dao_alarm_action_update update;
	dao_alarm_action_read select;

	public dao_alarm_action() {
	}

	public dao_alarm_action(data_source ds) {
		super(ds, null, null);
		update = new dao_alarm_action_update(ds);
		select = new dao_alarm_action_read(ds);
	}
	
	@Override
	public void update(db_alarm_action e) throws SQLException {
		update.update(null, e);
	}

	@Override
	public String name() {
		return "process_action";
	}

	@Override
	public void close() {
		update.close();
		select.close();
		super.close();
	}

	@Override
	public void reset_update_pos() {
		update.reset_update_pos();
	}

	@Override
	public void flush() throws SQLException {
		update.flush();
	}

	@Override
	public Object[] toArray(db_alarm_action e) {
		return update.toArray(e);
	}

	@Override
	public linear<db_alarm_action> read_list(Object... p) throws SQLException {
		return select.read_list(str_tool.to_str(p[0]));
	}
}