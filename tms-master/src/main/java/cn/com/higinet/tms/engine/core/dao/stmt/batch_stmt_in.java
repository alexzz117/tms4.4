package cn.com.higinet.tms.engine.core.dao.stmt;

import java.sql.SQLException;

public interface batch_stmt_in
{
	public void flush() throws SQLException;
	public void update(Object[] d) throws SQLException;

	public void commit() throws SQLException;
	public void rollback();
	public void close();
}
