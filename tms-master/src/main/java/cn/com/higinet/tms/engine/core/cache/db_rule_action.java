package cn.com.higinet.tms.engine.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import cn.com.higinet.tms.engine.comm.comp_tool;
import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.core.cond.node;
import cn.com.higinet.tms.engine.core.cond.parser.cond_parser;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

/**
 * 规则动作
 * @author lining
 *
 */
public final class db_rule_action
{			
	static Comparator<db_rule_action> comp_by_ruleid_acid = new Comparator<db_rule_action>()
	{
		public int compare(db_rule_action o1, db_rule_action o2)
		{
			int c = comp_tool.comp(o1.rule_id, o2.rule_id);
			if (c != 0)
				return c;
			return comp_tool.comp(o1.ac_id ,o2.ac_id);
		}
	};

	public static class cache
	{
		public linear<db_rule_action> list_ = new linear<db_rule_action>(comp_by_ruleid_acid);
		public linear<linear<db_rule_action>> list_rule = new linear<linear<db_rule_action>>();

		public static cache load(data_source ds, db_rule.cache drc)
		{
			cache c = new cache();
			c.int_base(ds, drc);
			c.init_rule_ac(drc);
			return c;
		}

		private void int_base(data_source ds, db_rule.cache drc)
		{
			String sql = "select AC_ID, RULE_ID, AC_DESC, AC_COND, AC_EXPR, AC_ENABLE " + 
					"from TMS_COM_RULE_ACTION order by RULE_ID, AC_ID";

			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_rule_action ac = new db_rule_action();
						ac.ac_id = rs.getInt("AC_ID");
						ac.rule_id = rs.getInt("RULE_ID");
						ac.ac_name = rs.getString("AC_DESC");
						ac.ac_cond = rs.getString("AC_COND");
						ac.ac_expr = rs.getString("AC_EXPR");
						ac.is_enable = !(0 == rs.getInt("AC_ENABLE"));
						ac.post_init();
						list_.add(ac);
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_action.cache error.");
			}
			finally
			{
				stmt.close();
			}
		}
		
		private void init_rule_ac(db_rule.cache drc)
		{
			linear<db_rule> rules = drc.all();
			for (int i = 0, len = rules.size(); i < len; i++)
			{
				db_rule rule = rules.get(i);
				linear<db_rule_action> list = new linear<db_rule_action>();
				int first = list_.lower_bound(mk_this(rule.id, -1));
				int last = list_.upper_bound(mk_this(rule.id, 999999999));
				list.addAll(list_.sub(first, last));
				list_rule.set(rule.index, list);
			}
		}
		
		private db_rule_action mk_this(int rule_id, int ac_id)
		{
			db_rule_action ac = new db_rule_action();
			ac.rule_id = rule_id;
			ac.ac_id = ac_id;
			return ac;
		}

		public linear<db_rule_action> get_rule_action(int index)
		{
			return list_rule.get(index);
		}

		public linear<db_rule_action> all()
		{
			return list_;
		}
	}

	public int ac_id;			//动作
	public int rule_id;			//所属规则ID
	public String ac_name;		//动作名称
	public String ac_cond;		//动作条件
	public String ac_expr;		//动作表达是
	public boolean is_enable;	//有效性

	public node node_cond, node_expr;

	void post_init()
	{
		if (!str_tool.is_empty(ac_cond))
			node_cond = cond_parser.build(ac_cond);

		if (!str_tool.is_empty(ac_expr))
			node_expr = cond_parser.build(ac_expr);
	}
	
	public String toString()
	{
		return "[ac_id:" + ac_id +",rule_id:" + rule_id + ",ac_name:" + ac_name + ",ac_cond:" + ac_cond + ",ac_expr:" + ac_expr + "]";
	}
}
