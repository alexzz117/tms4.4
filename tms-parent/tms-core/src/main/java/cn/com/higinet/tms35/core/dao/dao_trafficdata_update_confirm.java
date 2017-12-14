package cn.com.higinet.tms35.core.dao;

import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.core.cache.db_fd;
import cn.com.higinet.tms35.core.cache.db_tab;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.run.run_txn_values;

public class dao_trafficdata_update_confirm extends batch_stmt_jdbc_obj<run_txn_values>
{
	static Logger log = LoggerFactory.getLogger(dao_trafficdata_update_confirm.class);

	static String sql; // 批量插入的sql
	static int[] fd_index; // 批量插入sql的参数数据类型
	static int[] sql_param_type; // 批量插入sql的参数数据类型

	public dao_trafficdata_update_confirm(db_tab.cache dtc, db_fd.cache dfc, data_source ds)
	{
		super(ds, make_sql(dtc, dfc), sql_param_type);
	}

	synchronized static String make_sql(db_tab.cache dtc, db_fd.cache dfc)
	{
		if (sql != null)
			return sql;

		db_tab tab = dtc.get("T");
		db_tab base_tab = dtc.get(tab.base_tab);
		sql_param_type = new int[4];
		fd_index = new int[4];

		StringBuffer sb = new StringBuffer(128);
		sb.append("update ").append(base_tab.tab_name).append("\n set ");
		int i = 0;
		{
			fd_index[i] = dfc.INDEX_TXNSTATUS;
			db_fd fd = dfc.get(tab.index, fd_index[i]);
			sb.append(fd.fd_name);
			sb.append("=?,");
			sql_param_type[i] = fd.sql_type();
			i++;
		}
		{
			fd_index[i] = dfc.INDEX_FINISHTIME;
			db_fd fd = dfc.get(tab.index, fd_index[i]);
			sb.append(fd.fd_name);
			sb.append("=?,");
			sql_param_type[i] = fd.sql_type();
			i++;
		}
		/* add lining 修改报警是否正确的标识 */
		{
			fd_index[i] = dfc.INDEX_ISCORRECT;
			db_fd fd = dfc.get(tab.index, fd_index[i]);
			sb.append(fd.fd_name);
			sb.append("=?,");
			sql_param_type[i] = fd.sql_type();
			i++;
		}

		sb.setCharAt(sb.length() - 1, '\n');
		sb.append(" where TXNCODE=?");
		fd_index[i] = dfc.INDEX_TXNCODE;
		sql_param_type[i] = Types.VARCHAR;

		return sql = sb.toString();
	}

	@Override
	public String name()
	{
		return "traffic_data_update_confirm";
	}

	@Override
	final public Object[] toArray(run_txn_values rf)
	{
		return new Object[] { rf.get_fd(fd_index[0]), rf.get_fd(fd_index[1]),
				rf.get_fd(fd_index[2]), rf.get_fd(fd_index[3]) };
	}
}
