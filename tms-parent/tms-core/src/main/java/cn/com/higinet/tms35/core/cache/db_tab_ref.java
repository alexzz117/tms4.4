package cn.com.higinet.tms35.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;

import cn.com.higinet.tms35.comm.comp_tool;
import cn.com.higinet.tms35.comm.set_tool;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.core.cond.node;
import cn.com.higinet.tms35.core.cond.parser.cond_parser;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;

public final class db_tab_ref
{
	private static Comparator<db_tab_ref> comp_by_id = new Comparator<db_tab_ref>()
	{
		public int compare(db_tab_ref o1, db_tab_ref o2)
		{
			return comp_tool.comp(o1.ref_id , o2.ref_id);
		}
	};
	private static Comparator<db_tab_ref> comp_by_txnname_refid = new Comparator<db_tab_ref>()
	{
		public int compare(db_tab_ref o1, db_tab_ref o2)
		{
			int c = o1.tab_name.compareTo(o2.tab_name);
			if (c != 0)
				return c;
			return comp_tool.comp(o1.ref_id , o2.ref_id);
		}
	};

	static public class cache
	{
		private linear<linear<db_tab_ref>> list_txn_reftab = new linear<linear<db_tab_ref>>();// 交易引用的签约表，包含上级交易
		private linear<db_tab_ref> list_ = new linear<db_tab_ref>(comp_by_id); // 使用视图类型>表名称<索引

		public int get_index(int ref_id)
		{
			return list_.index(mk_this(ref_id));
		}

		public db_tab_ref get(int index)
		{
			return list_.get(index);
		}

		public db_tab_ref get_by_refid(int refid)
		{
			return list_.get(mk_this(refid));
		}

		private db_tab_ref mk_this(int refId)
		{
			db_tab_ref ref = new db_tab_ref();
			ref.ref_id = refId;
			return ref;
		}

		public void clear()
		{
			list_.clear();
			list_txn_reftab.clear();
		}

		static public cache load(data_source ds, db_tab.cache dtc)
		{
			cache c = new cache();
			c.init(ds, dtc);
			return c;
		}

		public void init(data_source ds, db_tab.cache dtc)
		{
			String sql = "select REF_ID,REF_DESC,TAB_NAME,REF_TAB_NAME,SRC_EXPR,TXN_ORDER"
					+ " from TMS_COM_REFTAB" + " order by REF_ID";

			final ArrayList<db_tab_ref> list = new ArrayList<db_tab_ref>(32);
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});

			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{

					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_tab_ref tab = new db_tab_ref();

						tab.ref_id = rs.getInt("REF_ID");
						tab.tab_name = str_tool.upper_case(rs.getString("TAB_NAME"));
						tab.ref_desc = (rs.getString("REF_DESC"));
						tab.ref_tab_name = str_tool.upper_case(rs.getString("REF_TAB_NAME"));
						tab.src_expr = (rs.getString("SRC_EXPR"));
						tab.txn_order = rs.getInt("TXN_ORDER");

						tab.post_init();

						list.add(tab);
						
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_tab_ref.cache error.");
			}
			finally
			{
				stmt.close();
			}

			list_.clear();
			list_.addAll(list);
			linear<db_tab_ref> list_txn = new linear<db_tab_ref>(comp_by_txnname_refid,
					list_.m_list);
			list_txn.sort();
			init_txn_ref(dtc, list_txn);
		}

		private void init_txn_ref(db_tab.cache dtc, linear<db_tab_ref> list_txn)
		{
			for (int i = 0, len = dtc.tab_count(); i < len; i++)
			{
				db_tab tab = dtc.get(i);
				if (!tab.is_txnview())
				{
					list_txn_reftab.add(new linear<db_tab_ref>());
					continue;
				}
				list_txn_reftab.add(new linear<db_tab_ref>(init_txn_ref(dtc, tab, list_txn)));
			}
		}

		private linear<db_tab_ref> init_txn_ref(db_tab.cache dtc, db_tab tab,
				linear<db_tab_ref> list_txn)
		{
			linear<db_tab_ref> list = new linear<db_tab_ref>();
			if (!str_tool.is_empty(tab.parent_tab))// 根交易表
			{
				db_tab ptab = dtc.get(tab.parent_tab);
				list.addAll(list_txn_reftab.get(ptab.index));
			}

			int first = list_txn.lower_bound(mk_this(tab.tab_name, -1));
			int last = list_txn.upper_bound(mk_this(tab.tab_name, Integer.MAX_VALUE));

			return new linear<db_tab_ref>(comp_by_txnname_refid, set_tool.set_union(list.m_list,
					list_txn.sub(first, last).m_list, comp_by_txnname_refid));
		}

		private static db_tab_ref mk_this(String tabName, int i)
		{
			db_tab_ref ref = new db_tab_ref();
			ref.tab_name = tabName;
			ref.ref_id = i;
			return ref;
		}

		public linear<db_tab_ref> get_txn_tabref(int index2)
		{
			if (index2 < 0 || index2 >= list_txn_reftab.size())
				return null;
			return list_txn_reftab.get(index2);
		}
	}

	private void post_init()
	{
		expr_node = cond_parser.build(src_expr);
	}

	private db_tab_ref()
	{
	}

	private db_tab_ref(String tab_name)
	{
		this.tab_name = tab_name;
	}

	public int ref_id; // 递增字段
	public String tab_name; // 交易表名称
	public String ref_desc; // 用户指定的引用描述
	public String ref_tab_name; // 被引用的表名称
	public String src_expr; // 获取数据时传入主键值，可以为表达式，缺省为一个交易字段，比如引用用户表信息，则需要传入userid,比如引用账户表信息，则需要传入acc"
	public int txn_order; // 界面显示顺序
	public node expr_node;
	public node src_node;
	
	public String toString()
	{
		return ref_id+","+tab_name;
	}
}
