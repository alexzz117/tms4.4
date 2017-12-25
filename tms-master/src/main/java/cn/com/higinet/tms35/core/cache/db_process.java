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

public final class db_process
{
	static Comparator<db_process> comp_by_txn_order = new Comparator<db_process>()
	{
		public int compare(db_process o1, db_process o2)
		{
			int c = o1.ps_txn.compareTo(o2.ps_txn);
			if (c != 0)
				return c;

			return comp_tool.comp(o2.ps_order, o1.ps_order);
		}
	};

	static public class cache
	{
		linear<db_process> list_ = new linear<db_process>(comp_by_txn_order);
		linear<linear<db_process>> list_txn_txnorder = new linear<linear<db_process>>();

		public static cache load(data_source ds, db_tab.cache dtc)
		{
			cache c = new cache();
			c.init(ds, dtc);
			return c;
		}

		public void clear()
		{
			list_.clear();
			list_txn_txnorder.clear();
		}

		public void init(data_source ds, db_tab.cache dtc)
		{
			String sql = "select PS_ID, PS_ORDER,PS_NAME,PS_TXN,PS_COND,PS_ENABLE,PS_VALUE "
					+ "from TMS_COM_PROCESS " + //
					// "where ps_enable=1 " + //
					"order by PS_TXN,PS_ORDER";
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_process ps = new db_process();
						ps.ps_id = rs.getInt("ps_id");
						ps.ps_order = rs.getInt("ps_order");
						ps.ps_name = rs.getString("ps_name");
						ps.ps_txn = str_tool.upper_case(rs.getString("ps_txn"));
						ps.ps_cond = (rs.getString("ps_cond"));
						ps.ps_enable = rs.getInt("ps_enable") != 0;
						ps.ps_value = rs.getString("ps_value");

						ps.post_init();
						list_.add(ps);
						
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_process.cache error.");
			}
			finally
			{
				stmt.close();
			}
			init_txn_ps(dtc);
		}

		private void init_txn_ps(db_tab.cache dtc)
		{
			for (int i = 0, len = dtc.tab_count(); i < len; i++)
			{
				db_tab tab = dtc.get(i);
				if (!tab.is_txnview())
					continue;

				list_txn_txnorder.set(tab.index, init_txn_pss(dtc, tab));
			}
		}

		private linear<db_process> init_txn_pss(db_tab.cache dtc, db_tab tab)
		{
			linear<db_process> list = new linear<db_process>(comp_by_txn_order);
			if (!str_tool.is_empty(tab.parent_tab))// 根交易表
			{
				db_tab ptab = dtc.get(tab.parent_tab);
				list.addAll(list_txn_txnorder.get(ptab.index));
			}

			int first = list_.lower_bound(mk_this(tab.tab_name, -1));
			int last = list_.upper_bound(mk_this(tab.tab_name, Integer.MAX_VALUE));

			list.addAll(list_.sub(last, first));
			list.sort();
			return list;
		}

		private db_process mk_this(String tabName, int i)
		{
			db_process ps = new db_process();
			ps.ps_txn = tabName;
			ps.ps_order = i;
			return ps;
		}

		public linear<db_process> get(int txn_id)
		{
			return list_txn_txnorder.get(txn_id);
		}

		public linear<db_process> all()
		{
			return list_;
		}
	}

	public String toString()
	{
		return ps_txn + ":" + ps_order + ":" + ps_value;
	}

	public int ps_id;
	public String ps_txn, ps_name, ps_cond, ps_value;
	public int ps_order;
	public boolean ps_enable;

	public node node;
	public linear<ps_item> ps_item;

	static public class ps_item
	{
		public ps_item(double score, String pscode)
		{
			this.score = score;
			this.ps_code = pscode;
		}

		public double score;
		public String ps_code;
	}

	java.util.Comparator<ps_item> comp_ps_item = new Comparator<ps_item>()
	{
		public int compare(ps_item o1, ps_item o2)
		{
			return comp_tool.comp(o1.score, o2.score);
		}
	};

	public void post_init()
	{
		if (!str_tool.is_empty(ps_cond))
			node = cond_parser.build(ps_cond);

		if (!str_tool.is_empty(ps_value))
		{
			ps_item = new linear<ps_item>(comp_ps_item);
			String[] item = ps_value.split("\\|");
			for (int i = 0; i < item.length; i++)
			{
				String[] n = item[i].split(",");
				if (n.length < 2)
					continue;

				ps_item.add(new ps_item(Double.parseDouble(n[0]), n[1]));
			}
			ps_item.sort();
		}
	}

	public String ps_code(double ret)
	{
		int index = ps_item.upper_bound(new ps_item(ret, null))-1;
		return ps_item.get(index).ps_code;
	}

	public static void main(String[] v)
	{
		db_process ps = new db_process();
		ps.ps_enable = true;
		ps.ps_value = "0,PS01|20,PS02|50,PS03|80,PS08";
		ps.ps_value = "-100,1|0,2|30,3|50,4|80,5";
		ps.post_init();
		System.out.println(ps.ps_code(0));
		System.out.println(ps.ps_code(49));
		System.out.println(ps.ps_code(50));
		System.out.println(ps.ps_code(51));
		System.out.println(ps.ps_code(80));
		System.out.println(ps.ps_code(81));
		System.out.println(ps.ps_code(100));
	}
}
