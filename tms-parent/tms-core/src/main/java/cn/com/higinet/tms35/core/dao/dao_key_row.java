package cn.com.higinet.tms35.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.com.higinet.tms35.comm.hash;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.cache.db_fd;
import cn.com.higinet.tms35.core.cache.db_ref_row;
import cn.com.higinet.tms35.core.cache.linear;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public abstract class dao_key_row extends batch_stmt_jdbc_obj<db_ref_row>
{
	static private final int SQL_EXECOP_MODEL = tmsapp.get_config("tms.sql.model", 4);
	
	batch_stmt_jdbc stmt_read;
	batch_stmt_jdbc_obj<db_ref_row> stmt_insert;

	String insert_sql, update_sql, select_sql;
	linear<db_fd> fd_list;
	String tab_name;
	String key_name;
	
	public dao_key_row()
	{
	}

	public void init(data_source ds, String tab_name, String key_name, linear<db_fd> linear)
	{
		this.tab_name = tab_name;
		this.key_name = key_name;
		this.fd_list = linear;
		super.init(ds, make_update_sql(), make_update_sql_param(), 0);
		stmt_read = new batch_stmt_jdbc(ds, make_select_sql(), new int[] { Types.VARCHAR });
		stmt_insert = new batch_stmt_jdbc_obj<db_ref_row>(ds, make_insert_sql(), make_insert_sql_param()) {
			@Override
			public String name() {
				return "dao_key_row_insert";
			}

			@Override
			public Object[] toArray(db_ref_row e) {
				List<String> list = new ArrayList<String>(e.values().size());
				list.addAll(e.values());
				return list.toArray();
			}
		};
	}

	private String make_select_sql()
	{
		if (select_sql != null)
			return select_sql;

		int hashId = hash.hash_id(Thread.currentThread().getName(), SQL_EXECOP_MODEL);
		StringBuffer sb = new StringBuffer(128);
		sb.append("/*\nhashId=").append(hashId).append("\n*/\n");
		sb.append("select  ");
		for (db_fd fd : fd_list)
			sb.append(fd.fd_name).append(',');
		sb.setLength(sb.length() - 1);

		sb.append("\nfrom ").append(tab_name).append("\nwhere ").append(key_name).append("=?");

		return select_sql = sb.toString();
	}
	
	int[] make_insert_sql_param()
	{
		int[] param = new int[fd_list.size()];
		Arrays.fill(param, Types.VARCHAR);
		return param;
	}

	int[] make_update_sql_param()
	{
		int[] param = new int[fd_list.size() + 1];
		Arrays.fill(param, Types.VARCHAR);
		return param;
	}

	synchronized String make_insert_sql()
	{
		if (insert_sql != null)
			return insert_sql;

		StringBuffer sb = new StringBuffer(100);
		StringBuffer sb1 = new StringBuffer(fd_list.size() * 2);
		sb.append("insert into ").append(this.tab_name).append(" (");
		sb1.append(" values (");
		for (db_fd fd : fd_list)
		{
			sb.append(fd.fd_name).append(",");
			sb1.append("?,");
		}
		
		if ("TMS_RUN_ACCOUNT".equalsIgnoreCase(this.tab_name))
		{
			sb.append("CREATED_DATE").append(",").append("UPDATED_DATE").append(",");			
			sb1.append("sysdate,sysdate,");
		}
		
		sb.setCharAt(sb.length() - 1, ')');
		sb1.setCharAt(sb1.length() - 1, ')');
		return insert_sql = sb.toString() + sb1.toString();
	}
	
	synchronized String make_update_sql()
	{
		if (update_sql != null)
			return update_sql;

		StringBuffer sb = new StringBuffer(100);
		sb.append("update ").append(this.tab_name).append(" set ");
		for (db_fd fd : fd_list)
		{
			sb.append(fd.fd_name).append("=?,");
		}

		if ("TMS_RUN_ACCOUNT".equalsIgnoreCase(this.tab_name))
		{
			sb.append("UPDATED_DATE").append("=sysdate,");
		}
		
		sb.setLength(sb.length() - 1);

		sb.append("\nwhere ").append(this.key_name).append("=?");
		return update_sql = sb.toString();
	}

	public db_ref_row read(Object... p) throws SQLException
	{
		return read((String) p[0]);
	}

	public db_ref_row read(final String key) throws SQLException
	{
		final db_ref_row row = new db_ref_row(this.fd_list);
		stmt_read.query(new Object[] { key }, new row_fetch()
		{
			public boolean fetch(ResultSet rs) throws SQLException
			{
				for (int i = 0, len = fd_list.size(); i < len; i++)
					row.put(fd_list.get(i).fd_name, rs.getString(i + 1));
				row.set_indb(true);
				return true;
			}
		});
		row.set_key(key);
		return row;
	}
	
	protected batch_stmt_jdbc_obj<db_ref_row> stmt(db_ref_row e) {
		if (e.is_indb())
			return this;
		else
			return stmt_insert;
	}
	
	final public Object[] toArray(db_ref_row s)
	{
		return s.toArray();
	}
	
	@Override
	public void reset_update_pos() {
		stmt_insert.reset_update_pos();
		super.reset_update_pos();
	}

	@Override
	public void flush() throws SQLException {
		stmt_insert.flush();
		super.flush();
	}

	@Override
	public void close()
	{
		super.close();
		stmt_read.close();
		stmt_insert.close();
	}

	public String name()
	{
		return "dao_key_row";
	}
}
