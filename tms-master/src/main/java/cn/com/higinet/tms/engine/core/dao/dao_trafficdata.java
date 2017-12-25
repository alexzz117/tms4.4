package cn.com.higinet.tms.engine.core.dao;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.cache.db_fd;
import cn.com.higinet.tms.engine.core.cache.db_tab;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.run.run_txn_values;

public class dao_trafficdata extends batch_stmt_jdbc_obj<run_txn_values> {
	static Logger log = LoggerFactory.getLogger(dao_trafficdata.class);

	dao_trafficdata_insert insert;

	dao_trafficdata_update update;

	dao_trafficdata_read select;

	public dao_trafficdata() {
	}

	db_fd.cache dfc;

	public dao_trafficdata(db_tab.cache dtc, db_fd.cache dfc, data_source ds) {
		super(null, null, null);
		insert = new dao_trafficdata_insert(dtc, dfc, ds);
		update = new dao_trafficdata_update(dtc, dfc, ds);
		select = new dao_trafficdata_read(dtc, dfc, ds);
		this.dfc = dfc;
	}

	batch_stmt_jdbc_obj<run_txn_values> stmt(run_txn_values e) {
		if (e.is_indb() || e.m_env.is_confirm())
			return update;
		return insert;
	}

	@Override
	public void update(run_txn_values e) throws SQLException {
		update(null, e);
	}

	@Override
	public void update(String batch_code, run_txn_values e) throws SQLException {
		stmt(e).update(batch_code, e);
	}

	@Override
	public String name() {
		return "traffic_data";
	}

	@Override
	public void close() {
		insert.close();
		update.close();
		select.close();
	}

	@Override
	public void reset_update_pos() {
		insert.reset_update_pos();
		update.reset_update_pos();
	}

	@Override
	public void flush() throws SQLException {
		insert.flush();
		update.flush();
	}

	@Override
	public void commit() throws SQLException {
		insert.commit(); //因为是同一个连接，因此只需要一个提交即可
	}
	@Override
	public void rollback() {
		insert.rollback();//因为是同一个连接，因此只需要一个提交即可
	}
	@Override
	public Object[] toArray(run_txn_values e) {
		return stmt(e).toArray(e);
	}

}