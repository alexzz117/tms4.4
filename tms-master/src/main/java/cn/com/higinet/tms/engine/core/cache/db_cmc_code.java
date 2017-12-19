package cn.com.higinet.tms.engine.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import cn.com.higinet.tms.engine.comm.comp_tool;
import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

public class db_cmc_code {
	public static Comparator<db_cmc_code> comp_by_id_onum = new Comparator<db_cmc_code>()
	{
		public int compare(db_cmc_code o1, db_cmc_code o2)
		{
			int c = o1.category_id.compareTo(o2.category_id);
			if (c != 0)
				return c;
			return comp_tool.comp(o1.onum, o2.onum);
		}
	};
			
	public static class cache {
		private linear<db_cmc_code> list_ = new linear<db_cmc_code>(comp_by_id_onum);
		
		public static cache load(data_source ds) {
			cache c = new cache();
			final linear<db_cmc_code> list = new linear<db_cmc_code>();
			String sql = "select CATEGORY_ID, CODE_KEY, CODE_VALUE, ONUM"
					+ " from CMC_CODE order by CATEGORY_ID, ONUM";
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_cmc_code cc = new db_cmc_code();
						cc.category_id = rs.getString("CATEGORY_ID");
						cc.code_key = rs.getString("CODE_KEY");
						cc.code_value = rs.getString("CODE_VALUE");
						cc.onum = rs.getInt("ONUM");
						list.add(cc);
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_cmc_code.cache error.");

			}
			finally
			{
				stmt.close();
			}
			c.list_.addAll(list);
			return c;
		}
		
		public linear<db_cmc_code> get(String category_id)
		{
			linear<db_cmc_code> list = new linear<db_cmc_code>();
			list.addAll(sub(category_id, list_));
			return list;
		}
		
		public Map<String, db_cmc_code> get_map(String category_id)
		{
			Map<String, db_cmc_code> map = new HashMap<String, db_cmc_code>();
			linear<db_cmc_code> list = get(category_id);
			for (db_cmc_code code : list) {
				map.put(code.code_key, code);
			}
			return map;
		}
		
		private static linear<db_cmc_code> sub(String category_id, linear<db_cmc_code> list)
		{
			int first = list.lower_bound(mk_this(category_id, -1));
			int last = list.upper_bound(mk_this(category_id, Integer.MAX_VALUE));
			return list.sub(first, last);
		}
		
		private static db_cmc_code mk_this(String category_id, int onum)
		{
			db_cmc_code cc = new db_cmc_code();
			cc.category_id = category_id;
			cc.onum = onum;
			return cc;
		}
	}
	
	public String category_id;
	public String code_key;
	public String code_value;
	public int onum;
}