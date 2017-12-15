package cn.com.higinet.tms35.core.dao.stmt;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class batch_stmt_jdbc implements batch_stmt_in
{
	static Logger log = LoggerFactory.getLogger(batch_stmt_jdbc.class);

	static public interface row_fetch
	{
		boolean fetch(ResultSet rs) throws SQLException;
	}

	private data_source m_ds;
	private String m_sql;
	private int[] m_param_type;

	private PreparedStatement m_stmt;
	private int m_batch_size;

	public batch_stmt_jdbc()
	{

	}

	public batch_stmt_jdbc(data_source ds, String mSql)
	{
		this(ds, mSql, null, 0);
	}

	public batch_stmt_jdbc(data_source ds, String mSql, int[] mParamType)
	{
		this(ds, mSql, mParamType, 0);
	}

	public batch_stmt_jdbc(data_source ds, String mSql, int[] mParamType, int batch_size)
	{
		init(ds, mSql, mParamType, batch_size);
	}

	public void init(data_source ds, String mSql, int[] mParamType, int batch_size)
	{
		m_ds = ds;
		if (m_ds != null)
			m_ds.add_ref(this);
		m_sql = mSql;
		m_param_type = mParamType;
		m_batch_size = batch_size;
		
		if(m_batch_size==0)
			m_batch_size=256;
	}

	public int getUpdateCount() throws SQLException
	{
		return m_stmt.getUpdateCount();
	}
	
	public void setQueryTimeout(int seconds) throws SQLException
	{
		m_stmt.setQueryTimeout(seconds);
	}

	final private void prepare() throws SQLException
	{
		if (m_stmt != null)
			return;
		Connection conn = m_ds.get_conn();
		if (conn == null)
			return;
		m_stmt = conn.prepareStatement(m_sql);
		
		if (m_batch_size > 0)
			m_stmt.setFetchSize(m_batch_size);
	}

	private void set_param(Object[] d) throws SQLException
	{
		if (d == null)
			return;

		for (int i = 0, len = d.length; i < len; i++)
		{
			try
			{
				if (d[i] == null)
					m_stmt.setNull(i + 1, m_param_type[i]);
				else
					m_stmt.setObject(i + 1, d[i], m_param_type[i]);
			}
			catch (SQLException e)
			{
				log.error(m_sql + "\n" + i + ":" + d[i] + ":" + m_param_type[i], e);
				throw e;
			}
		}

	}

	public ResultSet query(Object[] d) throws SQLException
	{
		prepare();
		set_param(d);
		return m_stmt.executeQuery();
	}

	public <E> void query(Object[] d, final row_fetch row_map) throws SQLException
	{
		ResultSet rs = null;
		try
		{
			rs = query(d);
			while (rs.next())
			{
				if(!row_map.fetch(rs))
					break;
			}
		}
		catch (SQLException e)
		{
			log.error("ERROR", e);
			m_ds.reset();
			throw e;
		}
		finally
		{
			if (rs != null)
				rs.close();
		}
	}

	public <E> void query_fetch_all(Object[] d, final row_fetch row_map) throws SQLException
	{
		ResultSet rs = null;
		try
		{
			row_map.fetch(rs = query(d));
		}
		catch (SQLException e)
		{
			m_ds.reset();
			throw e;
		}
		finally
		{
			if (rs != null)
				rs.close();
		}
	}

	public boolean execute(final Object[] d) throws SQLException
	{
		this.prepare();
		set_param(d);
		return m_stmt.execute();
	}

	public void execute() throws SQLException
	{
		this.prepare();
		m_stmt.execute();
	}

	int m_batch_pos;

	public void flush() throws SQLException
	{
		if (m_batch_pos <= 0)
			return;

		m_batch_pos = 0;
		try
		{
			prepare();
			m_stmt.executeBatch();
			m_stmt.clearBatch();
		}
		catch (BatchUpdateException e)
		{
			m_stmt.cancel();
			throw e;
		}
		catch (SQLException e)
		{
			rollback();
			m_ds.reset();
			throw e;
		}
	}

	public void update(Object[] d) throws SQLException
	{
		try
		{
			prepare();
			set_param(d);

			m_stmt.addBatch();
			m_batch_pos++;

			if (m_batch_size > 0 && m_batch_pos >= m_batch_size)
				flush();
		}
		catch (SQLException e)
		{
			log.error("",e);
			rollback();
			m_ds.reset();
			throw e;
		}
	}

	public void close()
	{
		reset();
		m_ds.release(this);
	}

	public void reset()
	{
		if (m_stmt == null)
			return;
		try
		{
			m_stmt.close();
		}
		catch (SQLException e)
		{
		}
		finally
		{
			m_stmt = null;
		}
	}

	public Connection get_conn() throws SQLException
	{
		return m_ds.get_conn();
	}

	public void commit() throws SQLException
	{
		try
		{
			get_conn().commit();
		}
		catch (SQLException e)
		{
			m_ds.reset();
			throw e;
		}
	}

	public void rollback()
	{
		try
		{
			this.reset_update_pos();
			get_conn().rollback();
		}
		catch (SQLException e)
		{
			log.error(null, e);
		}
	}

	public boolean is_conn_valid()
	{
		try
		{
			this.prepare();
		}
		catch (SQLException e)
		{
			return false;
		}

		return true;
	}

	public void reset_update_pos()
	{
		if (m_batch_pos == 0)
			return;

		try
		{
			m_batch_pos = 0;
			m_stmt.clearBatch();
		}
		catch (SQLException e)
		{
			log.error(null, e);
		}
	}
	
	public String get_sql() {
		return m_sql;
	}

	public int[] get_param_type() {
		return m_param_type;
	}
	
	public data_source get_data_source() {
	    return m_ds;
	}
}
