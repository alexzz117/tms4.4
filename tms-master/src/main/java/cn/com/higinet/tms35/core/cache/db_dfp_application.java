package cn.com.higinet.tms35.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public final class db_dfp_application {
	static Comparator<db_dfp_application> comp_by_app_id = new Comparator<db_dfp_application>()
	{
		public int compare(db_dfp_application o1, db_dfp_application o2)
		{
			return o1.app_id.compareTo(o2.app_id);
		}
	};

	static public class cache
	{
		linear<db_dfp_application> list_ = new linear<db_dfp_application>(comp_by_app_id);

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
			String sql = "select APP_ID, MAX_DEVICES, THRESHOLD, COOKIENAME, APP_NAME,TOKEN_TYPE "
					+ "from TMS_DFP_APPLICATION order by APP_ID";
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_dfp_application app = new db_dfp_application();
						app.app_id = rs.getString("APP_ID");
						app.max_devices = rs.getInt("MAX_DEVICES");
						app.threshold = rs.getFloat("THRESHOLD");
						app.cookiename = rs.getString("COOKIENAME");
						app.app_name = rs.getString("APP_NAME") ;
						app.token_type = rs.getString("TOKEN_TYPE") ;
						app.app_index = list_.size();
						list_.add(app);
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_dfp_application.cache error.");
			}
			finally
			{
				stmt.close();
			}
		}

		private db_dfp_application mk_this(String app_id)
		{
			db_dfp_application app = new db_dfp_application();
			app.app_id = app_id;
			return app;
		}

		public db_dfp_application get(String app_id)
		{
			return list_.get(mk_this(app_id));
		}

		public linear<db_dfp_application> all()
		{
			return list_;
		}
		
		public db_dfp_application get(int app_index)
		{
			return list_.get(app_index);
		}
		
		public int app_count()
		{
			return list_.size();
		}
	}
	public int app_index;
	public String app_id; //渠道代码
	public int max_devices;	//最大设备数
	public float threshold; //阀值
	public String cookiename;
	public String app_name;
	public String token_type;
}