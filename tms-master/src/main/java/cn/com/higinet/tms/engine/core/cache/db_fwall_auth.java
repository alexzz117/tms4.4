package cn.com.higinet.tms.engine.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

public final class db_fwall_auth
{
	public String APP_ID;
	public String APP_KEY;
	public String MD_APPID; 
	public String MD_APPKEY;
	
	static Comparator<db_fwall_auth> comp_by_txn_sworder = new Comparator<db_fwall_auth>()
	{
		public int compare(db_fwall_auth o1, db_fwall_auth o2)
		{
			int c = o1.APP_ID.compareTo(o2.APP_ID);
			if (c != 0)
				return c;

			return 0;
		}
	};
	
	public db_fwall_auth() {
		super();
	}

	public db_fwall_auth(String app_id) {
		super();
		APP_ID = app_id;
	}
	
	static public class cache
	{
		linear<db_fwall_auth> list_ = new linear<db_fwall_auth>(comp_by_txn_sworder);

		public db_fwall_auth get(String app_id)
		{
			return list_.get(new db_fwall_auth(app_id));
		}

		public linear<db_fwall_auth> all()
		{
			return list_;
		}

		public static cache load(data_source ds)
		{
			final cache c = new cache();

			String sql = "select APP_ID, APP_KEY,MD_APPID,MD_APPKEY"+
					" from TMS_FWALL_AUTH ";

			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_fwall_auth fa = new db_fwall_auth();
						fa.APP_ID = rs.getString("APP_ID");
						fa.APP_KEY = rs.getString("APP_KEY");
						fa.MD_APPID = rs.getString("MD_APPID");
						fa.MD_APPKEY = rs.getString("MD_APPKEY");
						
						c.list_.add(fa);
						
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_fwall_auth.cache error.");
			}
			finally
			{
				stmt.close();
			}
			
			return c;
		}
	}


}
