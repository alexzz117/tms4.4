package cn.com.higinet.tms35.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import cn.com.higinet.tms35.comm.comp_tool;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.cond.node;
import cn.com.higinet.tms35.core.cond.parser.cond_parser;
import cn.com.higinet.tms35.core.dao.dao_base;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

/**
 * 交易策略实体对象
 * @author lining
 *
 */
public final class db_strategy
{
	static public final int ST_SYNC = 0;//实时
	static public final int ST_ASYNC = 1;//准实时
	
	private static final int RULE_ALL_EXEC = 0;	// 策略规则执行方式，全部执行
	private static final int RULE_SELECT_EXEC = 1; // 策略规则执行方式，选择执行
	
	private static Comparator<db_rule> comp_by_dp_score = new Comparator<db_rule>()
	{
		public int compare(db_rule o1, db_rule o2)
		{
			int c = comp_tool.comp(o1.dp_order, o2.dp_order);
			if (c != 0)
				return c;
			return comp_tool.comp(o1.score, o2.score);
		}
	};
	
	static public class cache
	{
		// 策略排序，优先执行当前交易下的策略，逐层向上级递归
		static Comparator<db_strategy> comp_by_sttxn_stid = new Comparator<db_strategy>()
		{
			public int compare(db_strategy o1, db_strategy o2)
			{
				int c = o1.st_txn.compareTo(o2.st_txn);
				if (c != 0)
					return c;
				return comp_tool.comp(o1.st_id, o2.st_id);
			}
		};
		static final java.util.Comparator<db_strategy_rule> comp_by_stid_rid = new Comparator<db_strategy_rule>()
		{
			public int compare(db_strategy_rule o1, db_strategy_rule o2)
			{
				int c1 = comp_tool.comp(o1.st_id, o2.st_id);
				if (c1 != 0)
					return c1;
				return comp_tool.comp(o1.rule_id, o2.rule_id);
			}
		};

		linear<db_strategy> list_ = new linear<db_strategy>(comp_by_sttxn_stid);
		linear<linear<db_strategy>> list_txn_txnorder = new linear<linear<db_strategy>>();

		private void init_txn_st(db_tab.cache dtc, db_rule.cache drc)
		{
			for (int i = 0, len = dtc.tab_count(); i < len; i++)
			{
				db_tab tab = dtc.get(i);
				if (!tab.is_txnview())
					continue;
				list_txn_txnorder.set(tab.index, init_txn_sts(dtc, tab, drc.get_txn_rules(tab.index)));
			}
		}

		private linear<db_strategy> init_txn_sts(db_tab.cache dtc, db_tab tab, linear<db_rule> rules)
		{
			linear<db_strategy> list = new linear<db_strategy>(comp_by_sttxn_stid);
			if (!str_tool.is_empty(tab.parent_tab))// 根交易表
			{
				db_tab ptab = dtc.get(tab.parent_tab);
				list.addAll(list_txn_txnorder.get(ptab.index));
			}

			int first = list_.lower_bound(mk_this(tab.tab_name, -1));
			int last = list_.upper_bound(mk_this(tab.tab_name, Integer.MAX_VALUE));

			if (last > first)
				list.addAll(list_.sub(first, last));

			list.sort();
			return list;
		}

		private static db_strategy mk_this(String tabName, int id)
		{
			db_strategy st = new db_strategy();
			st.st_txn = tabName;
			st.st_id = id;
			return st;
		}
		
		public int st_count()
		{
			return list_.size();
		}

		public linear<db_strategy> get(int txn_id)
		{
			return list_txn_txnorder.get(txn_id);
		}
		
		public db_strategy get_st(int st_index)
		{
			return list_.get(st_index);
		}

		public linear<db_strategy> all()
		{
			return list_;
		}
		
		private static linear<db_strategy_rule> read_strategy_rule()
		{
			linear<db_strategy_rule> ret = new linear<db_strategy_rule>(comp_by_stid_rid);
			SqlRowSet rs = bean.get(dao_base.class).query(
					"select ST_ID, RULE_ID from TMS_COM_STRATEGY_RULE_REL order by ST_ID, RULE_ID");
			while (rs.next())
				ret.add(new db_strategy_rule(rs.getInt("ST_ID"), rs.getInt("RULE_ID")));
			return ret;
		}

		public static cache load(data_source ds, final db_tab.cache dtc, final db_rule.cache drc)
		{
			final cache c = new cache();
			final linear<db_strategy_rule> list_st_rule = read_strategy_rule();
			String sql = "select ST_ID, ST_TXN, ST_NAME, EVAL_COND, EVAL_MODE, RULE_EXEC_MODE, "
					+ "ST_ENABLE from TMS_COM_STRATEGY order by ST_TXN, ST_ID";

			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{

					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_strategy st = new db_strategy();
						st.st_id = rs.getInt("ST_ID");
						st.st_txn = rs.getString("ST_TXN");
						st.st_name = rs.getString("ST_NAME");
						st.eval_cond = rs.getString("EVAL_COND");
						st.eval_mode = rs.getInt("EVAL_MODE");
						st.rule_exec_mode = rs.getInt("RULE_EXEC_MODE");
						st.st_enable = !(0 == rs.getInt("ST_ENABLE"));
						st.index = c.list_.size();
						db_tab tab = dtc.get(st.st_txn);
						st.post_init(tab, list_st_rule, drc);
						c.list_.add(st);
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new tms_exception("load db_strategy.cache error.");
			}
			finally
			{
				stmt.close();
			}
			c.init_txn_st(dtc, drc);
			return c;
		}
	}

	public String toString()
	{
	    return "[" + st_txn + ":"+ st_id + "-" + st_name + "]";
	}
	
	void post_init(db_tab tab, linear<db_strategy_rule> rules, db_rule.cache drc)
	{
		if (!str_tool.is_empty(eval_cond))
			node = cond_parser.build(eval_cond);
		st_rules.clear();
		if (rule_exec_mode == RULE_ALL_EXEC)
		{
			st_rules.addAll(drc.get_txn_rules(tab.index));
		}
		else if (rule_exec_mode == RULE_SELECT_EXEC)
		{
			int first = rules.lower_bound(new db_strategy_rule(st_id, -1));
			int last = rules.upper_bound(new db_strategy_rule(st_id, Integer.MAX_VALUE / 2));
			if (first < rules.size() && rules.get(first).st_id == st_id)
			{
				for (db_strategy_rule rc : rules.sub(first, last))
					st_rules.add(drc.get_by_ruleid(rc.rule_id));
			}
			st_rules.sort();
		}
	}
	
	public db_strategy() {

	}
	
	public db_strategy(db_tab tab, db_rule.cache drc)
	{
		st_id = index = -1;
		st_txn = tab.tab_name;
		eval_mode = ST_SYNC;
		this.post_init(tab, null, drc);
	}

	public int index; // 表的全局唯一ID，使用tab_name进行排序之后的结果

	public int st_id; // 策略ID
	public String st_txn; // 策略所属交易
	public String st_name; // 策略名称
	public String eval_cond; // 评估条件
	public int eval_mode; // 评估方式
	public int rule_exec_mode; // 规则执行方式
	public boolean st_enable; // 有效性
	public node node; // 编译的评估条件
	public linear<db_rule> st_rules = new linear<db_rule>(comp_by_dp_score); // 策略下规则
	
	private static class db_strategy_rule
	{
		public int st_id;
		public int rule_id;
		public db_strategy_rule(int st_id, int rule_id)
		{
			this.st_id = st_id;
			this.rule_id = rule_id;
		}
		
		public String toString()
		{
			return st_id + "->" + rule_id;
		}
	}
	
	public boolean is_sync()
	{
		return eval_mode == ST_SYNC;
	}

	public boolean is_async()
	{
		return eval_mode == ST_ASYNC;
	}
}