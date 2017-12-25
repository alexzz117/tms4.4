package cn.com.higinet.tms35.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.comm.comp_tool;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.core.cache.db_process.ps_item;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.core.eval.eval_func;

/**
 * 规则评估策略实体对象
 * @author lining
 *
 */
public class db_strategy_rule_eval {
	static Logger log = LoggerFactory.getLogger(db_strategy_rule_eval.class);
	public static final int EVAL_MECH_SCORE = 0;	//评估机制-分值
	public static final int EVAL_MECH_DISPOSAL = 1;	//评估机制-处置
	
	private static Comparator<db_strategy_rule_eval> comp_by_stid_evaltype = new Comparator<db_strategy_rule_eval>()
	{
		public int compare(db_strategy_rule_eval o1, db_strategy_rule_eval o2)
		{
			int c = comp_tool.comp(o1.st_id, o2.st_id);
			if (c != 0)
				return c;
			return comp_tool.comp(o1.eval_type, o2.eval_type);
		}
	};
	
	public static class cache {
		private linear<db_strategy_rule_eval> list_ = new linear<db_strategy_rule_eval>(comp_by_stid_evaltype); // 系统所有规则
		private linear<linear<db_strategy_rule_eval>> list_st_et = new linear<linear<db_strategy_rule_eval>>();
		public static cache load(data_source ds, final db_strategy.cache dsc)
		{
			final cache c = new cache();
			final linear<db_strategy_rule_eval> list = new linear<db_strategy_rule_eval>();
			String sql = "select SRE_ID, ST_ID, EVAL_TYPE, EVAL_MECH, DIS_STRATEGY, STAT_FUNC, PS_SCORE "
					+ "from TMS_COM_STRATEGY_RULE_EVAL" + " order by ST_ID, EVAL_TYPE";

			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{

					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_strategy_rule_eval stre = new db_strategy_rule_eval();
						stre.sre_id = rs.getInt("SRE_ID");
						stre.st_id = rs.getInt("ST_ID");
						stre.eval_type = rs.getInt("EVAL_TYPE");
						stre.eval_mech = rs.getInt("EVAL_MECH");
						stre.dis_strategy = rs.getString("DIS_STRATEGY");
						stre.stat_func = rs.getString("STAT_FUNC");
						stre.ps_score = rs.getString("PS_SCORE");
						stre.post_init();
						list.add(stre);
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new tms_exception("load db_strategy_rule_eval.cache error.");
			}
			finally
			{
				stmt.close();
			}
			c.list_.addAll(list);
			c.init_st_rule_eval(dsc);
			return c;
		}
		
		private void init_st_rule_eval(db_strategy.cache dsc)
		{
			for (int i = 0, len = dsc.st_count(); i < len; i++)
			{
				db_strategy st = dsc.get_st(i);
				list_st_et.set(i, sub(st.st_id, list_));
			}
		}
		
		private static linear<db_strategy_rule_eval> sub(int st_id, linear<db_strategy_rule_eval> list_stre)
		{
			linear<db_strategy_rule_eval> list = new linear<db_strategy_rule_eval>();
			int first = list_stre.lower_bound(mk_this(st_id, -1));
			int last = list_stre.upper_bound(mk_this(st_id, Integer.MAX_VALUE));
			for (db_strategy_rule_eval dsre : list_stre.sub(first, last)) {
				list.set(dsre.eval_type, dsre);
			}
			return list;
		}
		
		private static db_strategy_rule_eval mk_this(int st_id, int eval_type)
		{
			db_strategy_rule_eval stre = new db_strategy_rule_eval();
			stre.st_id = st_id;
			stre.eval_type = eval_type;
			return stre;
		}
		
		public linear<db_strategy_rule_eval> get_strategy_rule_eval_bystid(int st_index)
		{
			return list_st_et.get(st_index);
		}
	}
	
	java.util.Comparator<ps_item> comp_ps_item = new Comparator<ps_item>()
	{
		public int compare(ps_item o1, ps_item o2)
		{
			return comp_tool.comp(o1.score, o2.score);
		}
	};
	
	public String ps_code(float ret)
	{
		int index = ps_item.upper_bound(new ps_item(ret, null))-1;
		return ps_item.get(index).ps_code;
	}
	
	public boolean is_disposal()
	{
		return this.eval_mech == EVAL_MECH_DISPOSAL;
	}
	
	void post_init()
	{
		if (!str_tool.is_empty(dis_strategy))
		{
			func_eval = eval_func.get(eval_mech, dis_strategy);
			if (func_eval == null)
			{
				String s = "没有找到终断策略:" + dis_strategy;
				log.error(s, new tms_exception(s));
			}
		}
		if (!str_tool.is_empty(stat_func))
		{
			func_eval = eval_func.get(eval_mech, stat_func);
			if (func_eval == null)
			{
				String s = "没有找到统计函数:" + stat_func;
				log.error(s, new tms_exception(s));
			}
		}
		if (!str_tool.is_empty(ps_score))
		{
			ps_item = new linear<ps_item>(comp_ps_item);
			String[] item = ps_score.split("\\|");
			for (int i = 0; i < item.length; i++)
			{
				String[] n = item[i].split(",");
				if (n.length < 2)
					continue;
				ps_item.add(new ps_item(Double.parseDouble(n[0]), n[1]));
			}
		}
	}
	
	public int sre_id; // 规则评估策略主键
	public int st_id; // 所属策略主键
	public int eval_type; // 规则评估类型
	public int eval_mech; // 评估机制
	public String dis_strategy; // 终断机制
	public String stat_func; // 统计函数
	public String ps_score; // 处置分值
	public linear<ps_item> ps_item;
	public eval_func func_eval; // 处置函数
}