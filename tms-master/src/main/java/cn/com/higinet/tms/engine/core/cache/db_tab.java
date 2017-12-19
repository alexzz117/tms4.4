package cn.com.higinet.tms.engine.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

public final class db_tab
{
	static public class cache
	{
		private linear<db_tab> list_ = new linear<db_tab>(new Comparator<db_tab>()
		{
			public int compare(db_tab o1, db_tab o2)
			{
				return o1.tab_name.compareTo(o2.tab_name);
			}
		});

		private linear<str_id> list_txnid_index = new linear<str_id>(str_id.comp_str);

		public static cache load(data_source ds)
		{
			cache c = new cache();
			c.init(ds);
			return c;
		}

		public int tab_count()
		{
			return list_.size();
		}

		public db_tab get(int table_id)
		{
			return list_.get(table_id);
		}

		public db_tab get(String tab_name)
		{
			return list_.get(new db_tab(str_tool.upper_case(tab_name)));
		}

		public int get_index(String tab_name)
		{
			return list_.index(new db_tab(tab_name));
		}

		public db_tab get_by_txnid(String txnid)
		{
			str_id si = list_txnid_index.get(new str_id(str_tool.upper_case(txnid), -1));
			return si == null ? null : list_.get(si.id);
		}

		public void init(data_source ds)
		{
			String sql = "select TAB_NAME, TAB_DESC, TAB_TYPE, IS_SYS, IS_QUERY, VIEW_COND, "
					+ "BASE_TAB, PARENT_TAB, CHANN, SHOW_ORDER, TXNID, IS_ENABLE,MODELUSED"
					+ " from TMS_COM_TAB" + " order by TAB_NAME";

			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{

					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_tab tab = new db_tab();

						tab.type = rs.getInt("TAB_TYPE");
						tab.is_sys = rs.getInt("IS_SYS");
						tab.is_query = rs.getInt("IS_QUERY");
						tab.show_order = rs.getInt("SHOW_ORDER");
						tab.is_enable = rs.getInt("IS_ENABLE");
						tab.tab_name = str_tool.upper_case(rs.getString("TAB_NAME"));
						tab.tab_desc = (rs.getString("TAB_DESC"));
						tab.txn_id = str_tool.upper_case(rs.getString("TXNID"));
						tab.view_cond = (rs.getString("VIEW_COND"));
						tab.base_tab = str_tool.upper_case(rs.getString("BASE_TAB"));
						tab.parent_tab = str_tool.upper_case(rs.getString("PARENT_TAB"));
						tab.chann = str_tool.upper_case(rs.getString("CHANN"));
						tab.modelused = str_tool.upper_case(rs.getString("MODELUSED"));
						tab.index = list_.size();
						list_.add(tab);

						if (!str_tool.is_empty(tab.txn_id))
							list_txnid_index.add(new str_id(tab.txn_id, tab.index));
						
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new tms_exception("load db_tab.cache error.");
			}
			finally
			{
				stmt.close();
			}
			
			for(db_tab tab:list_)
			{
				if(tab.parent_tab==null)
					continue;
				db_tab parent=this.get(tab.parent_tab);
				if(parent==null)
					continue;
				if(parent.is_enable==0)
					tab.is_enable=0;
			}
			
			list_txnid_index.sort();
		}
	}

	db_tab()
	{
	}

	db_tab(String tab_name)
	{
		this.tab_name = tab_name;
	}

	public String toString()
	{
		//return "[" + tab_name + "-" + db_cache.get().table().get(tab_name).tab_desc + "]";
	    return "[" + tab_name + "-" + tab_desc + "]";
	}

	public int index; // 表的全局唯一ID，使用tab_name进行排序之后的结果

	public int type; // 表类型[交易视图4,物理表1,结构2,TMS视图3,]
	public int is_sys; // 是否系统专用表(系统表、用户表)
	public int is_query; // 是否可以在自定义查询中列出
	public int show_order; // 交易建模树中的顺序
	public int is_enable; // 是否启用
	public String tab_name; // 数据库表名称，主键 ，交易视图时，直接写交易编码
	public String tab_desc; // 汉字描述名称
	public String txn_id; // 交易ID，识别时的TXN_ID对应的值，API传过来的数值
	public String view_cond; // 构成视图的SQL条件
	public String base_tab; // 交易视图所对应的物理表 ，tms中唯一
	public String parent_tab; // 公共表名
	public String chann; // 渠道代码（交易视图时）
	public String modelused; // 渠道代码（交易视图时）

	public boolean is_txnview()
	{
		return type == 4;
	}

}
