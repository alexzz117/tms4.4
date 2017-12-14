package cn.com.higinet.tms35.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import cn.com.higinet.tms35.comm.comp_tool;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.core.cond.node;
import cn.com.higinet.tms35.core.cond.parser.cond_parser;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;

public final class db_sw
{
	static public final int SW_LOG = 1;
	static public final int SW_SYNC = 2;
	static public final int SW_ASYNC = 3;

	static Comparator<db_sw> comp_by_txn_sworder = new Comparator<db_sw>()
	{
		public int compare(db_sw o1, db_sw o2)
		{
			int c = o1.sw_txn.compareTo(o2.sw_txn);
			if (c != 0)
				return c;

			return comp_tool.comp(o2.sw_order, o1.sw_order);
		}
	};

	static public class cache
	{
		linear<db_sw> list_ = new linear<db_sw>(comp_by_txn_sworder);
		linear<linear<db_sw>> list_txn_txnorder = new linear<linear<db_sw>>();

		private void init_txn_sw(db_tab.cache dtc)
		{
			for (int i = 0, len = dtc.tab_count(); i < len; i++)
			{
				db_tab tab = dtc.get(i);
				if (!tab.is_txnview())
					continue;

				list_txn_txnorder.set(tab.index, init_txn_sws(dtc, tab));
			}
		}

		private linear<db_sw> init_txn_sws(db_tab.cache dtc, db_tab tab)
		{
			linear<db_sw> list = new linear<db_sw>(comp_by_txn_sworder);
			if (!str_tool.is_empty(tab.parent_tab))// 根交易表
			{
				db_tab ptab = dtc.get(tab.parent_tab);
				list.addAll(list_txn_txnorder.get(ptab.index));
			}

			int first = list_.lower_bound(mk_this(tab.tab_name, -1));
			int last = list_.upper_bound(mk_this(tab.tab_name, Integer.MAX_VALUE));

			if (last < first)
				list.addAll(list_.sub(last, first));

			list.sort();
			return list;
		}

		private static db_sw mk_this(String tabName, int i)
		{
			db_sw sw = new db_sw();
			sw.sw_txn = tabName;
			sw.sw_order = i;
			return sw;
		}

		public linear<db_sw> get(int txn_id)
		{
			return list_txn_txnorder.get(txn_id);
		}

		public linear<db_sw> all()
		{
			return list_;
		}

		public static cache load(data_source ds, db_tab.cache dtc)
		{
			final cache c = new cache();

			String sql = "select SW_ID, SW_ORDER,SW_DESC,SW_TXN,SW_COND," + //
					"SW_TYPE,SW_ENABLE,SW_CREATETIME,SW_MODIFYTIME " + //
					"from TMS_COM_SWITCH " + //
//					"where sw_enable=1 " + //
					"order by SW_TXN,SW_ORDER";

			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_sw sw = new db_sw();
						sw.sw_id = rs.getInt("SW_ID");
						sw.sw_order = rs.getInt("SW_ORDER");
						sw.sw_type = rs.getInt("SW_TYPE");
						sw.sw_desc = rs.getString("SW_DESC");
						sw.sw_txn = str_tool.upper_case(rs.getString("SW_TXN"));
						sw.sw_cond = (rs.getString("SW_COND"));
						sw.sw_enable = rs.getInt("SW_ENABLE") != 0;
						sw.sw_createtime = rs.getLong("SW_CREATETIME");
						sw.sw_modifytime = rs.getLong("SW_MODIFYTIME");

						sw.post_init();

						c.list_.add(sw);
						
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_sw.cache error.");
			}
			finally
			{
				stmt.close();
			}
			c.init_txn_sw(dtc);
			return c;
		}
	}

	public int sw_id; // 开关主键，自动递增
	public String sw_desc; // 开关的汉字描述
	public String sw_txn; // 定义开关所在的交易
	public String sw_cond; // 开关条件，逻辑表达式(转账金额 > 10万元 AND 当日累计转账金额>100万元)
	public int sw_type; // 开关类型 1：log，同步，返回0分 2：事中，返回风险 3：事后，存储数据，事后运算分值
	public int sw_order; // 执行顺序
	public boolean sw_enable;
	public long sw_createtime; // 开关创建时间
	public long sw_modifytime; // 开关修改时间

	public node node;

	public String toString()
	{
		return sw_txn + ":" + sw_order + ":" + sw_type;
	}

	void post_init()
	{
//		if (!sw_enable)
//			return;

		if (!str_tool.is_empty(sw_cond))
			node = cond_parser.build(sw_cond);
	}

	public boolean is_log_only()
	{
		return sw_type == SW_LOG;
	}

	public boolean is_sync()
	{
		return sw_type == SW_SYNC;
	}

	public boolean is_async()
	{
		return sw_type == SW_ASYNC;
	}

}
