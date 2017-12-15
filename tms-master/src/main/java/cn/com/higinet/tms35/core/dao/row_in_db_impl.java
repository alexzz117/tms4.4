package cn.com.higinet.tms35.core.dao;

public class row_in_db_impl implements row_in_db {
	boolean m_is_indb;

	// boolean m_is_pending;

	public row_in_db_impl() {
		m_is_indb = false;
		// m_is_pending = false;
	}

	synchronized final public boolean is_indb() {
		return m_is_indb;
	}

	synchronized final public void set_indb(boolean b) {
		m_is_indb = b;
		// m_is_pending = false;
	}
	//
	// @Override
	// synchronized public boolean is_pending() {
	// return !m_is_indb && m_is_pending;
	// }
	//
	// @Override
	// synchronized public void set_pending(boolean b) {
	// if (!m_is_indb)
	// m_is_pending = b;
	// }
}
