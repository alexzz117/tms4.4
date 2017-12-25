package cn.com.higinet.tms.engine.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import cn.com.higinet.tms.engine.comm.comp_tool;
import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.core.cond.node;
import cn.com.higinet.tms.engine.core.cond.parser.cond_parser;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

public final class db_action
{
	static Comparator<db_action> comp_by_txn = new Comparator<db_action>()
	{
		public int compare(db_action o1, db_action o2)
		{
			int c = o1.ac_txn.compareTo(o2.ac_txn);
			if (c != 0)
				return c;

			return comp_tool.comp(o1.ac_id ,o2.ac_id);
		}
	};

	public static class cache
	{
		public linear<db_action> list_ = new linear<db_action>(comp_by_txn);
		public linear<linear<db_action>> list_txn = new linear<linear<db_action>>();

		public static cache load(data_source ds, db_tab.cache dtc)
		{
			cache c = new cache();
			c.int_base(ds, dtc);
			c.init_txn_ac(dtc);
			return c;
		}

		private void int_base(data_source ds, db_tab.cache dtc)
		{
			String sql = "select AC_ID,AC_DESC,AC_COND,AC_SRC,AC_DST,AC_ENABLE,AC_TXN " + //
					"from TMS_COM_ACTION " + //
//					"where ac_enable=1 " + //
					"order by AC_TXN,AC_ID";

			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_action ac = new db_action();
						ac.ac_id = rs.getInt("ac_id");
						ac.ac_desc = rs.getString("ac_desc");
						ac.ac_cond = rs.getString("ac_cond");
						ac.ac_src = rs.getString("ac_src");
						ac.ac_dst = rs.getString("ac_dst");
						ac.ac_enable = rs.getInt("ac_enable") != 0;
						ac.ac_txn = rs.getString("ac_txn");
						ac.index = list_.size();
						ac.post_init();
						list_.add(ac);
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_action.cache error.");
			}
			finally
			{
				stmt.close();
			}
		}

		private void init_txn_ac(db_tab.cache dtc)
		{
			for (int i = 0, len = dtc.tab_count(); i < len; i++)
			{
				db_tab tab = dtc.get(i);
				if (!tab.is_txnview())
					continue;

				list_txn.set(tab.index, init_txn_acs(dtc, tab));
			}
		}

		private linear<db_action> init_txn_acs(db_tab.cache dtc, db_tab tab)
		{
			linear<db_action> list = new linear<db_action>(comp_by_txn);
			if (!str_tool.is_empty(tab.parent_tab))// 根交易表
			{
				db_tab ptab = dtc.get(tab.parent_tab);
				list.addAll(list_txn.get(ptab.index));
			}

			int first = list_.lower_bound(mk_this(tab.tab_name, -1));
			int last = list_.upper_bound(mk_this(tab.tab_name, 999999999));

			list.addAll(list_.sub(first, last));
			return list;
		}

		private db_action mk_this(String tabName, int i)
		{
			db_action ac = new db_action();
			ac.ac_txn = tabName;
			ac.ac_id = i;
			return ac;
		}

		public linear<db_action> get(int txn_id)
		{
			return list_txn.get(txn_id);
		}

		public linear<db_action> all()
		{
			return list_;
		}

		public linear<db_action> get_txn_action(int index)
		{
			return list_txn.get(index);
		}
	}

	public int ac_id;
	public String ac_desc;
	public String ac_cond;
	public String ac_src;
	public String ac_dst;
	public String ac_txn;
	public boolean ac_enable;

	public int index;
	public node node_cond, node_src;

	void post_init()
	{
//		if (!ac_enable)
//			return;

		if (!str_tool.is_empty(ac_cond))
			node_cond = cond_parser.build(ac_cond);

		if (!str_tool.is_empty(ac_src))
			node_src = cond_parser.build(ac_src);
	}
	
	public String toString()
	{
		return "[ac_id:" + ac_id +",ac_txn:" + ac_txn + ",ac_desc:" + ac_desc + ",ac_cond:" + ac_cond + ",ac_src:" + ac_src + ",ac_dst:" + ac_dst + "]";
	}
}
