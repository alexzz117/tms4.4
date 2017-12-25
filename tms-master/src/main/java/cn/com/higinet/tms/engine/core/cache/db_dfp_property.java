package cn.com.higinet.tms.engine.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import cn.com.higinet.tms.engine.comm.comp_tool;
import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

public final class db_dfp_property {
	static Comparator<db_dfp_property> comp_by_id = new Comparator<db_dfp_property>()
	{
		public int compare(db_dfp_property o1, db_dfp_property o2)
		{
			return comp_tool.comp(o1.prop_id, o2.prop_id);
		}
	};

	static public class cache
	{
		linear<db_dfp_property> list_ = new linear<db_dfp_property>(comp_by_id);

		public static cache load(data_source ds)
		{
			cache c = new cache();
			c.init(ds);
			return c;
		}

		public void clear()
		{
			list_.clear();
		}

		public void init(data_source ds)
		{
			String sql = "select PROP_ID, PROP_NAME, PROP_TYPE, OPTIONAL, "
					+ "PROP_COMMENT from TMS_DFP_PROPERTY order by PROP_ID";
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_dfp_property prop = new db_dfp_property();
						prop.prop_id = rs.getInt("PROP_ID");
						prop.prop_name = rs.getString("PROP_NAME");
						prop.prop_type = rs.getString("PROP_TYPE");
						prop.optional = rs.getString("OPTIONAL");
						prop.prop_comment = rs.getString("PROP_COMMENT");
						list_.add(prop);
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_dfp_property.cache error.");
			}
			finally
			{
				stmt.close();
			}
		}

		private db_dfp_property mk_this(int prop_id)
		{
			db_dfp_property prop = new db_dfp_property();
			prop.prop_id = prop_id;
			return prop;
		}

		public db_dfp_property get(int prop_id)
		{
			return list_.get(mk_this(prop_id));
		}

		public linear<db_dfp_property> all()
		{
			return list_;
		}
	}

	public int prop_id;
	public String prop_name;
	public String prop_type;
	public String optional;
	public String prop_comment;
}
