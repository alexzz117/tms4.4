package cn.com.higinet.tms35.core.dao.stmt;

import java.sql.SQLException;

import cn.com.higinet.tms35.dataSyncLog;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.core.cache.linear;

public abstract class batch_stmt_jdbc_obj<E> extends batch_stmt_jdbc
{
	public batch_stmt_jdbc_obj()
	{
		super(null, null, null);
	}

	public batch_stmt_jdbc_obj(data_source ds, String mSql, int[] mParamType)
	{
		super(ds, mSql, mParamType);
	}

	public void update(E e) throws SQLException
	{
		update(null, e);
	}
	
	public void update(String batch_code, E e) throws SQLException
	{
		Object[] val = toArray(e);
		if (!str_tool.is_empty(batch_code)) {
			dataSyncLog.batch_print_log(batch_code, this.get_sql(), this.get_param_type(), val);
		}
		super.update(val);
	}
	
	public void delete(String batch_code, E e) throws SQLException
    {
        update(batch_code, e);
    }
	
	public void delete(E e) throws SQLException
	{
		delete(null, e);
	}

	public abstract String name();

	public abstract Object[] toArray(E e);

	public E read(Object... p) throws SQLException
	{
		return null;
	}
	
	public linear<E> read_list(Object... p) throws SQLException
	{
		return null;
	}
}
