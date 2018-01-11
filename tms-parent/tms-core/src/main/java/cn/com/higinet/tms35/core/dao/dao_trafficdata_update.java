package cn.com.higinet.tms35.core.dao;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.core.cache.db_fd;
import cn.com.higinet.tms35.core.cache.db_tab;
import cn.com.higinet.tms35.core.cache.linear;
import cn.com.higinet.tms35.core.cache.str_id;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.run.run_txn_values;

public class dao_trafficdata_update extends batch_stmt_jdbc_obj<run_txn_values> {
	private static final String TXNTIME = "TXNTIME";//交易流水时间

	private static final String CREATETIME = "CREATETIME";//第一次评估时间

	static Logger log = LoggerFactory.getLogger(dao_trafficdata_update.class);

	static db_tab base_tab; // 交易数据基本表

	static linear<db_fd> base_fd; // 交易数据基本表字段

	static String sql; // 批量插入的sql

	static int[] sql_param_type; // 批量插入sql的参数数据类型

	public dao_trafficdata_update(db_tab.cache dtc, db_fd.cache dfc, data_source ds) {
		super(ds, make_sql(dtc, dfc), sql_param_type);
	}

	synchronized static String make_sql(db_tab.cache dtc, db_fd.cache dfc) {
		if (sql != null)
			return sql;

		db_tab tab = dtc.get("T");
		base_tab = dtc.get(tab.base_tab);
		base_fd = dfc.get_tab_fields(base_tab.tab_name);
		//这里判断如果是TXNTIME的话，就不更新，因为txntime是数据库分区条件，因此顺银出现数据更新跨分区的异常   2017-08-31 王兴
		//******************************************************************************************************
		Iterator<db_fd> strItr = base_fd.m_list.iterator();
		int count = 2;
		while (strItr.hasNext()) {
			db_fd fd = strItr.next();
			if (TXNTIME.equalsIgnoreCase(fd.fd_name)) {
				strItr.remove();
				count--;
			}
			if (CREATETIME.equalsIgnoreCase(fd.fd_name)) {
				strItr.remove();
				count--;
			}
			if (count == 0) {
				break;
			}
		}
		//******************************************************************************************************

		sql_param_type = new int[base_fd.size() + 1];

		StringBuffer sb = new StringBuffer(128);
		sb.append("update ").append(base_tab.tab_name).append("\n set ");

		db_fd fd;
		for (int i = 0, len = base_fd.size(); i < len; i++) {
			fd = base_fd.get(i);
			String fdName = fd.fd_name;
			sb.append(fdName);
			sb.append("=?,");
			sql_param_type[i] = fd.sql_type();
			if (sql_param_type[i] == 0)
				log.warn("初始化sqltype失败:" + fd.toString());

		}
		//inserttime字段用于增量同步数据时作为时间戳
		sql_param_type[sql_param_type.length - 1] = -5;
//		sb.append("INSERTTIME=((SYSDATE) - TO_DATE('1970-01-01 08:00:00', 'yyyy-MM-dd HH24:mi:ss'))*86400000,");

		sb.setCharAt(sb.length() - 1, '\n');
		sb.append(" where TXNCODE=?");
		sql_param_type[base_fd.size()] = Types.VARCHAR;

		return sql = sb.toString();
	}

	@Override
	public String name() {
		return "traffic_data_update";
	}

	Object[] convt(run_txn_values rf) {
		db_fd.cache dfc = rf.m_env.get_txn().g_dc.field();

		linear<str_id> fdid_list = dfc.get_fdname_localid(rf.id());
		//这里判断如果是TXNTIME的话，就不更新，因为txntime是数据库分区条件，因此顺银出现数据更新跨分区的异常   2017-08-31 王兴
		//******************************************************************************************************
		List<str_id> copiedData = new ArrayList<str_id>(fdid_list.m_list);
		Iterator<str_id> strItr = copiedData.iterator();
		int count = 2; //目前只有2个需要在更新时候去除
		while (strItr.hasNext()) {
			str_id str = strItr.next();
			if (TXNTIME.equalsIgnoreCase(str.s)) {
				strItr.remove();
				count--;
			}
			if (CREATETIME.equalsIgnoreCase(str.s)) {
				strItr.remove();
				count--;
			}
			if (count == 0) {
				break;
			}
		}
		fdid_list = new linear<str_id>(fdid_list.m_comp, copiedData);
		//******************************************************************************************************
		db_fd bd;
		str_id si;
		Object[] ret = new Object[base_fd.size() + 1];
		int b = 0;
		for (int f = 0, flen = fdid_list.size(); f < flen && b < base_fd.size(); f++) {
			si = fdid_list.get_uncheck(f);
			if (str_tool.is_empty(si.s)) {
				continue;
			}
			for (; b < base_fd.size(); b++) {
				bd = base_fd.get(b);
				if (si.s.equals(bd.fd_name)) {
					ret[b++] = rf.get_fd(si.id);
					break;
				}
			}
		}

		ret[ret.length - 1] = rf.get_fd(dfc.INDEX_TXNCODE);

		return ret;
	}

	@Override
	final public Object[] toArray(run_txn_values rf) {
		return convt(rf);
	}

}
