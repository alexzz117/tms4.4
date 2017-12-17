package cn.com.higinet.tms35.core.dao.stmt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.manager.common.ApplicationContextUtil;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms35.comm.tms_exception;

public class data_source {
	static Logger log = LoggerFactory.getLogger( data_source.class );
	static String testConnSql = ((SqlMap) ApplicationContextUtil.getBean( "SqlMap" )).getSql( "tms.common.testconnection" );

	DataSource m_ds;
	Connection m_conn;
	List<batch_stmt_jdbc> m_tracker;
	boolean m_conn_reseted;

	public data_source( DataSource mDs ) {
		m_ds = mDs;
		m_conn_reseted = false;
		m_tracker = new ArrayList<batch_stmt_jdbc>();
	}

	public data_source() {
		this( (DataSource) ApplicationContextUtil.getBean( "onlineDataSource" ) );
	}

	public data_source( String driver, String url, String username, String password ) {
		this( StatDataSource.make( driver, url, username, password ) );
	}

	public int add_ref( batch_stmt_jdbc stmt ) {
		m_tracker.add( stmt );
		return m_tracker.size();
	}

	public void release( batch_stmt_jdbc stmt ) {
		m_tracker.remove( stmt );
		if( m_tracker.isEmpty() ) this.close();
	}

	static int conn_count;

	public void reset() throws sql_reconnect_exception {
		batch_stmt_jdbc stmt = new batch_stmt_jdbc( this, testConnSql, null );
		try {
			if( stmt.is_conn_valid() ) return;
		}
		finally {
			stmt.close();
		}

		close();
		for( batch_stmt_jdbc s : m_tracker )
			s.reset();
		m_conn_reseted = true;
		throw new sql_reconnect_exception();
	}

	public Connection get_conn() throws SQLException {
		if( m_conn != null ) return m_conn;
		m_conn = m_ds.getConnection();
		m_conn.setAutoCommit( false );
		if( log.isDebugEnabled() ) {
			synchronized (data_source.class) {
				log.info( "get jdbc connection:" + (++conn_count), new tms_exception( "debug-info" ) );
			}
		}
		return m_conn;
	}

	private void close() {
		if( m_conn == null ) return;

		if( log.isDebugEnabled() ) {
			synchronized (data_source.class) {
				log.info( "close jdbc connection:" + (--conn_count), new tms_exception( "debug-info" ) );
			}
		}
		try {
			/**
			 * 解决Sybase数据库时，
			 * 当autoCommit为false，导致无法释放链接
			 */
			if( !m_conn.isClosed() ) {
				if( !m_conn.getAutoCommit() ) {
					m_conn.setAutoCommit( true );
				}
			}
			m_conn.close();
		}
		catch( SQLException e ) {
		}
		finally {
			m_conn = null;
		}
	}
}
