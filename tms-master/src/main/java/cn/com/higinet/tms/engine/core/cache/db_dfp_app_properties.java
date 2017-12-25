package cn.com.higinet.tms.engine.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import cn.com.higinet.tms.engine.comm.comp_tool;
import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

public final class db_dfp_app_properties {
	static Comparator<db_dfp_app_properties> comp_by_id = new Comparator<db_dfp_app_properties>()
	{
		public int compare(db_dfp_app_properties o1, db_dfp_app_properties o2)
		{
			return comp_tool.comp(o1.id, o2.id);
		}
	};
	static Comparator<db_dfp_app_properties> comp_by_app_prop = new Comparator<db_dfp_app_properties>()
	{
		public int compare(db_dfp_app_properties o1, db_dfp_app_properties o2)
		{
			int c = o1.app_id.compareTo(o2.app_id);
			if (c != 0)
				return c;
			return comp_tool.comp(o1.prop_id, o2.prop_id);
		}
	};

	static public class cache
	{
		linear<db_dfp_app_properties> list_ = new linear<db_dfp_app_properties>(comp_by_id);//id排序
		linear<db_dfp_app_properties> list_app = new linear<db_dfp_app_properties>(comp_by_app_prop);//app_id,prop_id排序
		linear<linear<db_dfp_app_properties>> g_list_app = new linear<linear<db_dfp_app_properties>>();
		
		db_dfp_application.cache g_ac;
		
		public static cache load(data_source ds, db_dfp_application.cache ac)
		{
			cache c = new cache();
			c.g_ac = ac;
			c.init(ds);
			return c;
		}

		public void clear()
		{
			list_.clear();
		}

		public void init(data_source ds)
		{
			String sql = "select ID, APP_ID, PROP_ID, WEIGHT, STORECOLUMN, IS_TOKEN "
					+ "from TMS_DFP_APP_PROPERTIES order by ID";
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_dfp_app_properties app_prop = new db_dfp_app_properties();
						app_prop.id = rs.getInt("ID");
						app_prop.app_id = rs.getString("APP_ID");
						app_prop.prop_id = rs.getInt("PROP_ID");
						app_prop.weight = rs.getFloat("WEIGHT");
						app_prop.storecolumn = rs.getString("STORECOLUMN");
						app_prop.isToken = rs.getString("IS_TOKEN");
						list_.add(app_prop);
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_dfp_app_properties.cache error.");
			}
			finally
			{
				stmt.close();
			}
			list_app = new linear<db_dfp_app_properties>(comp_by_app_prop, list_.m_list);
			list_app.sort();
			init_app_properties();
		}
		
		public void init_app_properties()
		{
			for (int i = 0, len = g_ac.app_count(); i < len; i++)
			{
				
				db_dfp_application app = g_ac.get(i);
				g_list_app.set(app.app_index, new linear<db_dfp_app_properties>(get_app_properties(app)));
			}
		}

		private linear<db_dfp_app_properties> get_app_properties(db_dfp_application app)
		{
			linear<db_dfp_app_properties> list = new linear<db_dfp_app_properties>();
			list.addAll(sub(app.app_id));
			return list;
		}

		linear<db_dfp_app_properties> sub(String app_id)
		{
			int first = list_app.lower_bound(mk_this(0, app_id, 0));
			int last = list_app.upper_bound(mk_this(0, app_id, Integer.MAX_VALUE));
			return list_app.sub(first, last);
		}

		static db_dfp_app_properties mk_this(int id, String app_id, int prop_id)
		{
			db_dfp_app_properties app_prop = new db_dfp_app_properties();
			app_prop.id = id;
			app_prop.app_id = app_id;
			app_prop.prop_id = prop_id;
			return app_prop;
		}
		
		public linear<db_dfp_app_properties> get(String app_id)
		{
			db_dfp_application app = g_ac.get(app_id);
			if (app == null) {
				return null;
			}
			return g_list_app.get(app.app_index);
		}
		
		public linear<db_dfp_app_properties> get(int app_index)
		{
			return g_list_app.get(app_index);
		}

		public db_dfp_app_properties get(String app_id, int prop_id)
		{
			return list_app.get(mk_this(0, app_id, prop_id));
		}
		
	}
	public int id;
	public String app_id;
	public int prop_id;
	public float weight;
	public String storecolumn;
	public String isToken;
}
