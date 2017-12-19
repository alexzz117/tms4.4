package cn.com.higinet.tms.engine.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.comp_tool;
import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.core.cond.node;
import cn.com.higinet.tms.engine.core.cond.parser.cond_parser;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

public final class db_fd_ref
{
	static Logger log = LoggerFactory.getLogger(db_fd_ref.class);
	static Comparator<db_fd_ref> comp_by_refid_refname = new Comparator<db_fd_ref>()
	{
		public int compare(db_fd_ref o1, db_fd_ref o2)
		{
			int c = comp_tool.comp(o1.ref_id, o2.ref_id);
			if (c != 0)
				return c;

			return o1.ref_name.compareTo(o2.ref_name);
		}
	};

	private static Comparator<db_fd_ref> comp_txnname_refname = new Comparator<db_fd_ref>()
	{
		public int compare(db_fd_ref o1, db_fd_ref o2)
		{
			int c = o1.tab_name.compareTo(o2.tab_name);
			if (c != 0)
				return c;

			return o1.ref_name.compareTo(o2.ref_name);
		}
	};

	static public class cache
	{
		linear<db_fd_ref> list_ = new linear<db_fd_ref>(comp_by_refid_refname); // 索引:ref_id,ref_name字典顺序
		linear<db_fd_ref> list_txnname_refname = new linear<db_fd_ref>(comp_txnname_refname); // txn_name,ref_name字典顺序
		Map<String, List<String>> map_ref_tab_fields = null;

		@SuppressWarnings("unused")
		private Map<String, List<String>> get_ref_fields()
		{
			return map_ref_tab_fields;
		}

		@SuppressWarnings("unused")
		private Map<String, List<String>> init_ref_fields(db_tab.cache dtc, db_fd.cache dfc)
		{
			Map<String, List<String>> ret = new TreeMap<String, List<String>>();

			for (db_fd_ref ref : list_)
			{
				db_fd fd = ref.ref_fd;
				List<String> list = ret.get(fd.tab_name);
				if (list == null)
				{
					list = new ArrayList<String>(32);
					ret.put(fd.tab_name, list);
				}

				list.add(fd.fd_name);
			}

			for (Map.Entry<String, List<String>> m : ret.entrySet())
			{
				m.getValue().remove(dfc.get_key(m.getKey()).fd_name);
				java.util.Collections.sort(m.getValue());
				m.getValue().add(0, dfc.get_key(m.getKey()).fd_name);
			}

			return ret;
		}

		public static cache load(data_source ds, db_tab.cache dtc, db_fd.cache dfc,
				db_tab_ref.cache dtrc)
		{
			cache c = new cache();
			c.init(ds, dtc, dfc, dtrc);
			return c;
		}

		public db_fd_ref get(int ref_id, String ref_name)
		{
			return list_.get(mk_this(ref_id, ref_name));
		}

		private db_fd_ref mk_this(int ref_id, String ref_name)
		{
			db_fd_ref ref = new db_fd_ref();
			ref.ref_id = ref_id;
			ref.ref_name = ref_name;
			return ref;
		}

		public void clear()
		{
			list_.clear();
			list_txnname_refname.clear();
		}

		public void init(data_source ds, final db_tab.cache dtc, final db_fd.cache dfc,
				final db_tab_ref.cache dtrc)
		{
			String sql = "select REF_ID,TAB_NAME,REF_NAME,REF_FD_NAME,SRC_COND,SRC_EXPR,STORECOLUMN"
					+ " from TMS_COM_REFFD" + " order by REF_ID,REF_NAME";
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});

			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_fd_ref ref = new db_fd_ref();
						ref.ref_id = rs.getInt("REF_ID");
						ref.tab_name = str_tool.upper_case(rs.getString("TAB_NAME"));
						ref.ref_fd_name = str_tool.upper_case(rs.getString("REF_FD_NAME"));
						ref.ref_name = str_tool.upper_case(rs.getString("REF_NAME"));
						ref.src_cond = (rs.getString("SRC_COND"));
						ref.src_expr = (rs.getString("SRC_EXPR"));
						ref.storecolumn = rs.getString("STORECOLUMN");

						if (ref.post_init(dfc, dtrc.get_by_refid(ref.ref_id)))
							list_.add(ref);
						
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_fd_cache error.");
			}
			finally
			{
				stmt.close();
			}

			list_txnname_refname.addAll(list_);
			list_txnname_refname.sort();

			// map_ref_tab_fields=init_ref_fields(dtc, dfc) ;
		}

		public void get_txn_refields(db_tab tab, linear<db_fd> list)
		{
			int index1 = list_txnname_refname.lower_bound(make_this(tab.tab_name, ""));
			int index2 = list_txnname_refname.upper_bound(make_this(tab.tab_name, str_tool.MAX_VALUE));

			for (; index1 < index2; index1++)
				list.add(new db_fd(list_txnname_refname.get(index1)));
		}

		private db_fd_ref make_this(String tab_name, String ref_name)
		{
			db_fd_ref ref = new db_fd_ref();
			ref.tab_name = tab_name;
			ref.ref_name = ref_name;
			return ref;
		}

	}

	private boolean post_init(db_fd.cache dfc, db_tab_ref tab)
	{
		if (!str_tool.is_empty(src_expr))
			src = cond_parser.build(src_expr);
		if (!str_tool.is_empty(src_cond))
			cond = cond_parser.build(src_cond);

		if (tab == null)
		{
			log.warn(this.toString(), new tms_exception("引用表不存在"));
			return false;
		}

		ref_fd = dfc.get(tab.ref_tab_name, ref_fd_name);
		if (ref_fd == null)
		{
			throw new tms_exception("引用不存在的字段：ref_id=", this.ref_id);
		}
		this.ref_tab_name = tab.ref_tab_name;

		return true;
	}

	public int ref_id; // tms_com_ref_tab表主键
	public String tab_name; // 本字段引入的交易
	public String ref_fd_name; // 引用的字段，引用表的fd_name;
	public String ref_name; // 引用时的名称，缺省为引用字段名，用户可编辑
	public String ref_desc; // 引用时的名称，缺省为引用的字段描述
	public String src_expr; // src_cond为真时，使用该字段内容计算更新的值
	public String src_cond; // 该条件返回1时，执行更新
	public String storecolumn; // 存储到交易流水表中的字段

	public String ref_tab_name;

	public db_fd ref_fd;
	public node src, cond;

	public String toString()
	{
		return "[" + ref_id + ":" + tab_name + ":" + ref_desc + "]";
	}
}
