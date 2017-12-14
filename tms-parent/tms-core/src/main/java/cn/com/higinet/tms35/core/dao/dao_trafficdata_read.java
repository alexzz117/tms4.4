package cn.com.higinet.tms35.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.comm.hash;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.cache.db_fd;
import cn.com.higinet.tms35.core.cache.db_tab;
import cn.com.higinet.tms35.core.cache.linear;
import cn.com.higinet.tms35.core.cache.str_id;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.run.run_txn_values;

public class dao_trafficdata_read
{
	static Logger log=LoggerFactory.getLogger(dao_trafficdata_read.class);
	static private final int SQL_EXECOP_MODEL = tmsapp.get_config("tms.sql.model", 4);
	
	static db_tab base_tab; // 交易数据基本表
	static String sql; // 批量插入的sql
	static linear<db_fd> base_fd; // 交易数据基本表字段

	batch_stmt_jdbc read_stmt;

	public dao_trafficdata_read(db_tab.cache dtc, db_fd.cache dfc, data_source ds)
	{
	    int hashId = hash.hash_id(Thread.currentThread().getName(), SQL_EXECOP_MODEL);
		read_stmt = new batch_stmt_jdbc(ds, ("/*\nhashId=" + hashId + "\n*/\n"
                + make_sql(dtc, dfc)), new int[] { Types.VARCHAR });
	}

	static String make_sql(db_tab.cache dtc, db_fd.cache dfc)
	{
		if (sql != null)
			return sql;

		db_tab tab = dtc.get("T");
		base_tab = dtc.get(tab.base_tab);
		base_fd = dfc.get_tab_fields(base_tab.tab_name);

		StringBuffer sel_fd = new StringBuffer(128);
		sel_fd.append("select ");

		db_fd fd;
		for (int i = 0, len = base_fd.size(); i < len; i++)
		{
			fd = base_fd.get(i);
			sel_fd.append(fd.fd_name);
			sel_fd.append(',');
		}

		sel_fd.setCharAt(sel_fd.length() - 1, ' ');
		sel_fd.append("\nfrom ").append(base_tab.tab_name)//
				.append("\nwhere TXNCODE=?");
		return sql = sel_fd.toString();
	}

	public run_txn_values read(final run_env re,final String txn_code) throws SQLException
	{
		final int txnId = re.id();
		final run_txn_values ret = new run_txn_values(re);
		read_stmt.query(new Object[] { txn_code }, new row_fetch()
		{
			public boolean fetch(ResultSet rs) throws SQLException
			{
				convert(rs, txnId, ret);
				ret.set_indb(true);
				return true;
			}
		});
		ret.m_env = null;
		return ret;
	}

	protected void convert(ResultSet rs, int txnId, run_txn_values ret) throws SQLException
	{
		db_fd.cache dfc = ret.m_env.get_txn().g_dc.field();

		linear<str_id> fdid_list = dfc.get_fdname_localid(txnId);
		int flen = fdid_list.size();
		str_id si = fdid_list.get_uncheck(0);
		for (int f = 0, b = 0;; b++)
		{
			while (str_tool.is_empty(si.s))
			{
				if (++f >= flen)
					break;
				si = fdid_list.get_uncheck(f);
			}
			db_fd bd = base_fd.get_uncheck(b);
			if (!str_tool.is_empty(si.s) && si.s.equals(bd.fd_name))
			{
				ret.set_fd(si.id, rs.getObject(b + 1));
				if (++f >= flen)
					break;
				si = fdid_list.get_uncheck(f);
			}
		}
	}

	public void close()
	{
		read_stmt.close();
	}
}
