package cn.com.higinet.tms.engine.core.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.core.cache.db_fd;
import cn.com.higinet.tms.engine.core.cache.db_tab;
import cn.com.higinet.tms.engine.core.cache.linear;
import cn.com.higinet.tms.engine.core.cache.str_id;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.run.run_txn_values;

public class dao_trafficdata_insert extends batch_stmt_jdbc_obj<run_txn_values>
{
	static Logger log = LoggerFactory.getLogger(dao_trafficdata_insert.class);

	static db_tab base_tab; // 交易数据基本表
	static linear<db_fd> base_fd; // 交易数据基本表字段
	static String sql; // 批量插入的sql
	static int[] sql_param_type; // 批量插入sql的参数数据类型

	public dao_trafficdata_insert(db_tab.cache dtc, db_fd.cache dfc, data_source ds)
	{
		super(ds, make_sql(dtc, dfc), sql_param_type);
	}

	Object[] convt(run_txn_values rf)
	{
		db_fd.cache dfc = rf.m_env.get_txn().g_dc.field();
		linear<str_id> fdid_list = dfc.get_fdname_localid(rf.id());
		db_fd bd;
		str_id si;
		Object[] ret = new Object[base_fd.size()];
		si = fdid_list.get_uncheck(0);

		for (int b = 0, f = 0, flen = fdid_list.size(); b < base_fd.size(); b++)
		{
			bd = base_fd.get_uncheck(b);
			while (str_tool.is_empty(si.s))
			{
				if (++f >= flen)
					break;
				si = fdid_list.get_uncheck(f);
			}
			if (si.s.equals(bd.fd_name))
			{
				ret[b] = rf.get_fd(si.id);
//				log.info(b+":"+f+":"+si.s+"="+ret[b]);
				if (++f >= flen)
					break;
				si = fdid_list.get_uncheck(f);
			}
		}

		return ret;
	}

	@Override
	final public Object[] toArray(run_txn_values rf)
	{
		return convt(rf);
	}

	static synchronized String make_sql(db_tab.cache dtc, db_fd.cache dfc)
	{
		if (sql != null)
			return sql;

		db_tab tab = dtc.get("T");
		base_tab = dtc.get(tab.base_tab);
		base_fd = dfc.get_tab_fields(base_tab.tab_name);
		sql_param_type = new int[base_fd.size()+1];

		StringBuffer sb = new StringBuffer(128);
		StringBuffer v = new StringBuffer(128);
		sb.append("insert into ").append(base_tab.tab_name).append('(');
		v.append("\nvalues(");

		db_fd fd = null;
		for (int i = 0, len = base_fd.size(); i < len; i++)
		{
			// log.debug(i);
			fd = base_fd.get(i);
			sb.append(fd.fd_name).append(',');
			v.append("?,");
			sql_param_type[i] = fd.sql_type();
			if (sql_param_type[i] == 0)
				log.warn("初始化sqltype失败:" + fd.toString());
			
		}
		//inserttime字段用于增量同步数据时作为时间戳
		sql_param_type[sql_param_type.length-1] =-5;
//		sb.append("INSERTTIME").append(',');
//		v.append("((SYSDATE) - TO_DATE('1970-01-01 08:00:00', 'yyyy-MM-dd HH24:mi:ss'))*86400000,");
		sb.setCharAt(sb.length() - 1, ')');
		v.setCharAt(v.length() - 1, ')');
		sb.append(v);

		return sql = sb.toString();
	}

	@Override
	public String name()
	{
		return "traffic_data_insert";
	}
}
