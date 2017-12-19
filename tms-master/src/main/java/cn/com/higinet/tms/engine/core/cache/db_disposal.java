package cn.com.higinet.tms.engine.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.comp_tool;
import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.core.cache.db_process.ps_item;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

public class db_disposal {
	static Logger log = LoggerFactory.getLogger(db_disposal.class);
	
	public static Comparator<db_disposal> comp_by_dpcode = new Comparator<db_disposal>()
	{
		public int compare(db_disposal o1, db_disposal o2)
		{
			return o1.dp_code.compareTo(o2.dp_code);
		}
	};
	
	public static Comparator<db_disposal> comp_by_dporder = new Comparator<db_disposal>()
	{
		public int compare(db_disposal o1, db_disposal o2)
		{
			return comp_tool.comp(o1.dp_order, o2.dp_order);
		}
	};
	
	public static Comparator<db_disposal> comp_by_ruleorder = new Comparator<db_disposal>()
	{
		public int compare(db_disposal o1, db_disposal o2)
		{
			return comp_tool.comp(o1.rule_order, o2.rule_order);
		}
	};
	
	public static Comparator<ps_item> comp_ps_item = new Comparator<ps_item>()
	{
		public int compare(ps_item o1, ps_item o2)
		{
			return comp_tool.comp(o1.score, o2.score);
		}
	};
	
	public static class cache {
		private linear<db_disposal> list_ = new linear<db_disposal>(comp_by_dpcode);
		private linear<db_disposal> list_dporder = new linear<db_disposal>(comp_by_dporder);
		private linear<db_disposal> list_ruleorder = new linear<db_disposal>(comp_by_ruleorder);
		private linear<ps_item> ps_item = new linear<ps_item>(comp_ps_item);
		private linear<ps_item> md_ps_item = new linear<ps_item>(comp_ps_item);
		
		public static cache load(data_source ds)
		{
			cache c = new cache();
			final linear<db_disposal> list = new linear<db_disposal>();
			String sql = "select DP_CODE, DP_NAME, DP_ORDER, RULE_ORDER, ASSIGN_COND, DEFAULT_SCORE, MODEL_SCORE"
					+ " from TMS_COM_DISPOSAL order by DP_ORDER";
			final StringBuffer ps_score = new StringBuffer();
			final StringBuffer md_ps_score = new StringBuffer();
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_disposal dp = new db_disposal();
						dp.dp_code = rs.getString("DP_CODE");
						dp.dp_name = rs.getString("DP_NAME");
						dp.dp_order = rs.getInt("DP_ORDER");
						dp.rule_order = rs.getInt("RULE_ORDER");
						dp.assign_cond = rs.getString("ASSIGN_COND");
						dp.default_score = rs.getDouble("DEFAULT_SCORE");
						if (ps_score.length() > 0) {
							ps_score.append('|');
						}
						ps_score.append(dp.default_score).append(',').append(dp.dp_code);
						dp.model_score = rs.getDouble("MODEL_SCORE");
						if (md_ps_score.length() > 0) {
							md_ps_score.append('|');
						}
						md_ps_score.append(dp.model_score).append(',').append(dp.dp_code);
						dp.index = list.size();
						list.add(dp);
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_disposal.cache error.");

			}
			finally
			{
				stmt.close();
			}
			c.list_.addAll(list);
			c.list_dporder.addAll(list);
			c.list_ruleorder.addAll(list);
			c.list_.sort();
			c.list_dporder.sort();
			c.list_ruleorder.sort();
			c.post_init(false, ps_score.toString());
			c.post_init(true, md_ps_score.toString());
			return c;
		}
		
		public linear<db_disposal> get_by_dporder() {
			return list_dporder;
		}
		
		public linear<db_disposal> get_by_ruleorder() {
			return list_ruleorder;
		}
		
		public db_disposal get_dp(String dp_code) {
			if (str_tool.is_empty(dp_code))
				return null;
			return list_.get(mk_this(dp_code));
		}
		
		public String ps_code(double ret)
		{
			int index = ps_item.upper_bound(new ps_item(ret, null))-1;
			return ps_item.get(index).ps_code;
		}
		
		public String md_ps_code(double ret)
		{
			int index = md_ps_item.upper_bound(new ps_item(ret, null))-1;
			return md_ps_item.get(index).ps_code;
		}
		
		private db_disposal mk_this(String dp_code) {
			db_disposal dp = new db_disposal();
			dp.dp_code = dp_code;
			return dp;
		}
		
		private void post_init(boolean isModel, String ps_score) {
			if (!str_tool.is_empty(ps_score))
			{
				String[] item = ps_score.split("\\|");
				for (int i = 0; i < item.length; i++)
				{
					String[] n = item[i].split(",");
					if (n.length < 2)
						continue;
					(isModel ? md_ps_item : ps_item).add(new ps_item(Double.parseDouble(n[0]), n[1]));
				}
				(isModel ? md_ps_item : ps_item).sort();
			}
		}
	}
	
	public int index;
	public String dp_code;
	public String dp_name;
	public int dp_order;
	public int rule_order;
	public String assign_cond;
	public double default_score;
	public double model_score;
}