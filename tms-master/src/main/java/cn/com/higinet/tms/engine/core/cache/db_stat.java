package cn.com.higinet.tms.engine.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.comp_tool;
import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.core.cond.date_tool;
import cn.com.higinet.tms.engine.core.cond.node;
import cn.com.higinet.tms.engine.core.cond.parser.cond_parser;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms.engine.stat.stat_func;
import cn.com.higinet.tms.engine.stat.stat_win_time;

public final class db_stat
{
	static Logger log = LoggerFactory.getLogger(db_stat.class);

	public static Comparator<db_stat> comp_by_statid = new Comparator<db_stat>()
	{
		public int compare(db_stat o1, db_stat o2)
		{
			return comp_tool.comp(o1.stat_id, o2.stat_id);
		}
	};

	static Comparator<db_stat> comp_by_txnid_statname = new Comparator<db_stat>()
	{
		public int compare(db_stat o1, db_stat o2)
		{
			int c = o1.stat_txn.compareTo(o2.stat_txn);
			if (c != 0)
				return c;

			return o1.stat_name.compareTo(o2.stat_name);
		}
	};

	static Comparator<db_stat> comp_by_txnid_statid = new Comparator<db_stat>()
	{
		public int compare(db_stat o1, db_stat o2)
		{
			int c = o1.stat_txn.compareTo(o2.stat_txn);
			if (c != 0)
				return c;

			return comp_tool.comp(o1.stat_id, o2.stat_id);
		}
	};

	public static class cache
	{
		linear<db_stat> list_ = new linear<db_stat>(comp_by_statid);// stat_id排序
		linear<db_stat> list_txn_name = new linear<db_stat>(comp_by_txnid_statname);// txn,stat_name排序
		linear<linear<db_stat>> g_list_txn = new linear<linear<db_stat>>();// 一级索引：

		db_tab.cache g_dtc;
		db_fd.cache g_dfc;

		// txnid，二级索引：txnname,statname

		public db_stat get(int index)
		{
			return list_.get(index);
		}

		public int get_index(int stat_id)
		{
			return list_.index(mk_this(stat_id, null, null));
		}

		public db_stat get_by_statid(int stat_id)
		{
			return list_.get(mk_this(stat_id, null, null));
		}

		public db_stat get(String txn, String name)
		{
			return list_txn_name.get(mk_this(0, txn, name));
		}

		public linear<db_stat> get_txn_stats(String txn)
		{
			db_tab tab = g_dtc.get(txn);
			if (tab == null)
				return null;

			return g_list_txn.get(tab.index);
		}
		
		public linear<db_fd> get_txn_storefd(db_tab tab)
		{
			linear<db_stat> stat_list = get_txn_stats(tab.index);
			linear<db_fd> fd_list = new linear<db_fd>();
			for (db_stat stat : stat_list)
			{
			    if (stat.is_valid == 0 || str_tool.is_empty(stat.storecolumn))
			        continue;
			    fd_list.add(new db_fd(tab, stat));
			}
			return fd_list;
		}

		public linear<db_stat> get_txn_stats(int txn)
		{
			return g_list_txn.get(txn);
		}

		public static cache load(data_source ds, db_tab.cache dtc, db_fd.cache dfc)
		{
			cache c = new cache();
			c.g_dtc = dtc;
			c.g_dfc = dfc;
			c.init(ds);
			return c;
		}

		public void init(data_source ds)
		{
			String sql = "select STAT_ID, STAT_NAME, STAT_DESC, STAT_TXN, STAT_PARAM, STAT_COND, "
					+ "RESULT_COND, STAT_DATAFD, STAT_FN, COUNUNIT, COUNTROUND, STAT_UNRESULT, "
					+ "CONTINUES, STAT_VALID, FN_PARAM, STORECOLUMN, DATATYPE " + "from TMS_COM_STAT " + "order by STAT_ID";

			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_stat st = new db_stat();

						st.stat_id = rs.getInt("STAT_ID");
						st.stat_name = str_tool.upper_case(rs.getString("STAT_NAME"));
						st.stat_desc = str_tool.upper_case(rs.getString("STAT_DESC"));
						st.stat_txn = str_tool.upper_case(rs.getString("STAT_TXN"));
						st.stat_param_fd = str_tool.upper_case(rs.getString("STAT_PARAM"));
						if (str_tool.is_empty(st.stat_param_fd))
							st.stat_param_fd = "";
						st.stat_fd_name = str_tool.upper_case(rs.getString("STAT_DATAFD"));
						st.stat_func_name = str_tool.upper_case(rs.getString("STAT_FN"));
						st.stat_cond = rs.getString("STAT_COND");
						st.stat_cond_result = rs.getInt("RESULT_COND");
						st.stat_online = rs.getInt("STAT_UNRESULT");
						st.stat_unit_min = rs.getInt("COUNUNIT");
						st.stat_num_unit = rs.getInt("COUNTROUND");
						if (st.stat_num_unit == 0)
							st.stat_num_unit = 1;
						st.is_continues = rs.getInt("CONTINUES");
						st.is_valid = rs.getInt("STAT_VALID");
						st.fn_param = rs.getString("FN_PARAM");
						st.storecolumn = rs.getString("STORECOLUMN");
						st.datatype = rs.getString("DATATYPE");
						st.post_init(g_dtc.get(st.stat_txn), g_dfc);
						st.index = list_.size();
						list_.add(st);
						
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_stat.cache error.");
			}
			finally
			{
				stmt.close();
			}

			list_txn_name = new linear<db_stat>(comp_by_txnid_statname, list_.m_list);
			list_txn_name.sort();

			init_txn();
		}

		public void init_txn()
		{
			for (int i = 0, len = g_dtc.tab_count(); i < len; i++)
			{
				db_tab tab = g_dtc.get(i);
				if (!tab.is_txnview())
					continue;

				g_list_txn.set(tab.index, new linear<db_stat>(get_txn_stats(tab)));
			}
		}

		private linear<db_stat> get_txn_stats(db_tab tab)
		{
			linear<db_stat> list = new linear<db_stat>();
			if (!str_tool.is_empty(tab.parent_tab))
			{
				db_tab ptab = g_dtc.get(tab.parent_tab);
				list.addAll(g_list_txn.get(ptab.index));
			}

			list.addAll(sub(tab.tab_name));

			return list;
		}

		linear<db_stat> sub(String txn)
		{
			int first = list_txn_name.lower_bound(mk_this(0, txn, ""));
			int last = list_txn_name.upper_bound(mk_this(0, txn, str_tool.MAX_VALUE));
			return list_txn_name.sub(first, last);
		}

		static db_stat mk_this(int id, String txn, String name)
		{
			db_stat st = new db_stat();
			st.stat_id = id;
			st.stat_txn = txn;
			st.stat_name = name;
			return st;
		}

		public linear<db_stat> all()
		{
			return list_;
		}

	}

	public int stat_id; // 统计主键，自动递增
	public String stat_name; // 统计名称，例如S1
	public String stat_desc; // 统计的汉字描述
	public String stat_txn; // 定义统计所在的交易
	public String stat_param_fd; // 统计引用字段，使用此变量时，需要传入的对象值
	public String stat_fd_name; // 被统计字段名
	public String stat_func_name; // "统计函数"
	public String stat_cond; // 统计条件，如果为空，则为无条件统计
	public int stat_cond_result; // 交易结果条件，0处理中时计算，1成功时计算，2失败时计算，3全部计算
	public int stat_online; // 是否事中计算
	public int stat_unit_min; // 周期
	public int stat_num_unit; // 记录周期数
	public int is_continues; // 是否连续，有条件时，条件不成功则清空
	public int is_valid; // 是否有效
	public int is_operational; // 是否只是用于运算，不保存
	public String storecolumn; // 存储到交易流水表中的字段
	public String datatype; // 统计的数据类型
	private String fn_param;
	public node node; // 统计条件编译后的语法树
	public int[] param_fd_index; // 统计所引用的交易内字段的索引
	public int stat_fd_index;
	public stat_func func_st; // 统计函数
	public int index;
	public linear<range> range = new linear<range>();
	public db_fd stat_data_fd;

	public static class range
	{
		public range(Number d, Number e)
		{
			min = d;
			max = e;
		}

		public boolean include(Number n)
		{
			if (n == null)
				return false;

			return min.doubleValue() <= n.doubleValue() && n.doubleValue() < max.doubleValue();
		}

		Number min;
		Number max;

	}

	public static class tm_range extends range
	{
		static final Number h24 = date_tool.parse("24:00:00").getTime();
		static final Number h00 = date_tool.parse("00:00:00").getTime();

		public tm_range(Number d, Number e)
		{
			super(d, e);
		}

		public boolean include(Number n)
		{
			n = date_tool.parse(
					date_tool.format(new java.util.Date(n.longValue()), date_tool.FMT_T)).getTime();

			if (min.longValue() < max.longValue())
				return min.longValue() <= n.longValue() && n.longValue() < max.longValue();
			else
			{
				return min.longValue() <= n.longValue() && n.longValue() < h24.longValue()
						|| h00.longValue() <= n.longValue() && n.longValue() < max.longValue();
			}
		}

		public String toString()
		{
			return this.min + "," + this.max;
		}
	}

	public int range_index(Number curValue)
	{
		if (curValue == null)
			return -1;
		for (int i = 0, len = range.size(); i < len; i++)
		{
			range r = range.get(i);
			if (r.include(curValue))
				return i;
		}

		return -1;
	}

	public void post_init(db_tab tab, db_fd.cache dfc)
	{
		if (this.is_valid == 0)
			return;

		try
		{

			func_st = stat_func.get(stat_func_name);
			if (func_st == null)
			{
				String s = "没有找到统计函数:" + stat_func_name;
				log.error(s, new tms_exception(s));
			}
			if ("calculat_expressions".equalsIgnoreCase(stat_func_name)) {
				// 此函数的统计只是用于运算，不保存到运行统计数据中
				is_operational = 1;
			}

			String[] pfd = stat_param_fd.split(",");
			param_fd_index = new int[pfd.length];
			for (int i = 0, len = pfd.length; i < len; i++)
				param_fd_index[i] = dfc.get_local_index(tab.index, pfd[i]);

			if (!str_tool.is_empty(this.stat_fd_name))
			{
				this.stat_fd_index = dfc.get_local_index(tab.index, stat_fd_name);
				this.stat_data_fd = dfc.get(tab.index, this.stat_fd_index);
			}

			if (!str_tool.is_empty(stat_cond))
				node = cond_parser.build(stat_cond);

			if (str_tool.is_empty(fn_param))
				return;

			db_fd fd = dfc.get(tab.index, stat_fd_index);
			if (fd.type.equals("LONG") || fd.type.equals("INT") || fd.type.equals("MONEY")
					|| fd.type.equals("DOUBLE"))
			{
				String[] item = fn_param.split("\\|");
				for (int i = 0; i < item.length; i++)
				{
					String[] n = item[i].split(",");
					if (n.length == 1)
					{
						if (range.size() == 0)
							range.add(new range(Double.MIN_VALUE, Double.parseDouble(n[0])));
						else
							range.add(new range(Double.parseDouble(n[0]), Double.MAX_VALUE));
					}
					else
					{
						range.add(new range(Double.parseDouble(n[0]), Double.parseDouble(n[1])));
					}

				}
			}

			if (fd.type.equals("DATETIME"))
			{
				String[] item = fn_param.split("\\|");
				for (int i = 0; i < item.length; i++)
				{
					String[] n = item[i].split(",");
					if (n.length == 1)
					{
						if (range.size() == 0)
							range
									.add(new tm_range(Long.MIN_VALUE, date_tool.parse(n[0])
											.getTime()));
						else
							range
									.add(new tm_range(date_tool.parse(n[0]).getTime(),
											Long.MAX_VALUE));
					}
					else
					{
						range.add(new tm_range(date_tool.parse(n[0]).getTime(), //
								date_tool.parse(n[1]).getTime()));
					}
				}
			}
		}
		catch (RuntimeException e)
		{
			log.error(this.toString(), e);
			throw e;
		}
	}

	public String toString()
	{
		return "[" + stat_txn + "," + stat_id + "," + stat_name + "," + stat_desc + "]";
	}

	public db_fd datafd()
	{
		if (stat_fd_name == null)
			return null;
		return this.stat_data_fd;
	}

	public int get_index()
	{
		return index;
	}

	public int dec_win_when_set()
	{
		return stat_win_time.dec_win_when_set(this.stat_unit_min);
	}

	public int min_win_time(int cur_minute)
	{
		return stat_win_time.min_win_time(this.stat_unit_min, stat_num_unit - 1, cur_minute);
	}

	final public int cur_win_time(int cur_minute)
	{
		return stat_win_time.cur_win_time(this.stat_unit_min, cur_minute);
	}

	final public int st2txntime(int st_minute)
	{
		return stat_win_time.st2txntime(this.stat_unit_min, st_minute);
	}

	public String type()
	{
		return this.func_st.type(this);
	}

	public boolean is_user_pattern(int fd_user_index)
	{
		return this.param_fd_index.length == 1 && this.param_fd_index[0] == fd_user_index;
	}
}
