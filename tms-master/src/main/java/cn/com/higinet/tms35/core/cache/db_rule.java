package cn.com.higinet.tms35.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.comm.comp_tool;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.cond.node;
import cn.com.higinet.tms35.core.cond.parser.cond_parser;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public final class db_rule
{
	static Logger log = LoggerFactory.getLogger(db_rule.class);
	static String CMC_CODE_RULE_DISPOSAL = tmsapp.get_config("cmc_code.rule_disposal", "tms.rule.disposal");
	public static Comparator<db_rule> comp_by_id = new Comparator<db_rule>()
	{
		public int compare(db_rule o1, db_rule o2)
		{
			return comp_tool.comp(o1.id, o2.id);
		}
	};
	
	private static Comparator<db_rule> comp_by_txnname_rulecode = new Comparator<db_rule>()
	{
		public int compare(db_rule o1, db_rule o2)
		{
			int c = o1.txn_name.compareTo(o2.txn_name);
			if (c != 0)
				return c;
			return o1.code.compareTo(o2.code);
		}
	};
	
	private static Comparator<db_rule> comp_by_tnx_dp_score = new Comparator<db_rule>()
	{
		public int compare(db_rule o1, db_rule o2)
		{
			int c = o1.txn_name.compareTo(o2.txn_name);
			if (c != 0)
				return c;
			c = comp_tool.comp(o1.dp_order, o2.dp_order);
			if (c != 0)
				return c;
			return comp_tool.comp(o2.score, o1.score);
		}
	};

	static public class cache
	{
		private linear<db_rule> list_ = new linear<db_rule>(comp_by_id); // 系统所有规则
		private linear<linear<db_rule>> list_txn_rulecode = new linear<linear<db_rule>>();// 每个交易内的所有规则，包括上层交易
		private linear<linear<db_rule>> list_dp_score = new linear<linear<db_rule>>(); // 每个交易内的所有规则，包括上层交易
		
		public db_rule get(int index)
		{
			return list_.get(index);
		}

		public db_rule get(db_tab tab, String code)
		{
			linear<db_rule> t = list_txn_rulecode.get(tab.index);
			if (t == null)
				return null;
			db_rule dr = new db_rule();
			dr.txn_name = tab.tab_name;
			dr.code = code;
			return t.get(dr);
		}

		public int get_index(int rule_id)
		{
			return list_.index(mk_this("", rule_id));
		}

		public db_rule get_by_ruleid(int rule_id)
		{
			return list_.get(mk_this("", rule_id));
		}

		public linear<db_rule> get_txn_rules(int txn_id)
		{
			return list_dp_score.get(txn_id);
		}

		public linear<db_rule> all()
		{
			return list_;
		}

		public static cache load(data_source ds, db_tab.cache dtc, final db_disposal.cache dpc)
		{
			cache c = new cache();
			final linear<db_rule> list = new linear<db_rule>();
			String sql = "select RULE_ID, RULE_NAME, RULE_SHORTDESC, RULE_DESC, RULE_TXN, RULE_COND, EVAL_TYPE, DISPOSAL, RULE_SCORE, RULE_ORDER, RULE_ENABLE, RULE_ISTEST, RULE_TIMESTAMP"
					+ " from TMS_COM_RULE order by RULE_ID";
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_rule dr = new db_rule();
						dr.id = rs.getInt("RULE_ID");
						dr.code = str_tool.upper_case(rs.getString("RULE_NAME"));
						dr.name = rs.getString("RULE_SHORTDESC");
						dr.desc = rs.getString("RULE_DESC");
						dr.txn_name = rs.getString("RULE_TXN");
						dr.cond = rs.getString("RULE_COND");
						dr.eval_type = rs.getInt("EVAL_TYPE");
						dr.disposal = rs.getString("DISPOSAL");
						db_disposal dp = dpc.get_dp(dr.disposal);
						if (dp != null)
							dr.dp_order = dp.rule_order;
						dr.score = rs.getFloat("RULE_SCORE");
						dr.order = rs.getInt("RULE_ORDER");
						dr.timestamp = rs.getLong("RULE_TIMESTAMP");
						dr.is_enable = !(0 == rs.getInt("RULE_ENABLE"));
						dr.is_test = !(0 == rs.getInt("RULE_ISTEST"));
						dr.post_init();
						dr.index = list.size();
						list.add(dr);
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_rule.cache error.");

			}
			finally
			{
				stmt.close();
			}
			c.list_.addAll(list);
			c.init_txn(dtc);
			return c;
		}

		private void init_txn(db_tab.cache dtc)
		{
			linear<db_rule> list_txn = new linear<db_rule>(comp_by_tnx_dp_score, list_.m_list);
			list_txn.sort();
			for (int i = 0, len = dtc.tab_count(); i < len; i++)
			{
				db_tab tab = dtc.get(i);
				if (!tab.is_txnview())
					continue;
				linear<db_rule> temp = new linear<db_rule>(comp_by_tnx_dp_score, 
						get_txn_rules(dtc, tab, list_txn).m_list);
				temp.sort();
				list_dp_score.set(tab.index, temp);
				linear<db_rule> tmp = new linear<db_rule>(comp_by_txnname_rulecode, get_txn_rules(
						dtc, tab, list_txn).m_list);
				tmp.sort();
				list_txn_rulecode.set(tab.index, tmp);
			}
		}

		private linear<db_rule> get_txn_rules(db_tab.cache dtc, db_tab tab, linear<db_rule> list_txn)
		{
			linear<db_rule> list = new linear<db_rule>();
			if (!str_tool.is_empty(tab.parent_tab))
			{
				db_tab ptab = dtc.get(tab.parent_tab);
				list.addAll(list_dp_score.get(ptab.index));
			}
			list.addAll(sub(tab.tab_name, list_txn));
			return list;
		}

		private static linear<db_rule> sub(String txn, linear<db_rule> list_txn)
		{
			int first = list_txn.lower_bound(mk_this(txn, -1, 100.f));
			int last = list_txn.upper_bound(mk_this(txn, Integer.MAX_VALUE, -100.f));
			return list_txn.sub(first, last);
		}

		private static db_rule mk_this(String txn, int dp_order, float score)
		{
			db_rule dr = new db_rule();
			dr.txn_name = txn;
			dr.dp_order = dp_order;
			dr.score = score;
			return dr;
		}
		
		private static db_rule mk_this(String txn, int i)
		{
			db_rule dr = new db_rule();
			dr.txn_name = txn;
			dr.id = i;
			return dr;
		}
	}

	public int id; // 规则主键，自动递增
	public String code; // 规则编号，例如R1
	public String name; // 规则名称，简要描述
	public String desc; // 规则的汉字描述
	public String txn_name; // 定义规则所在的交易
	public String cond; // 规则条件，逻辑表达式(转账金额 > 10万元 AND //
	// 当日累计转账金额>100万元)，或者是子规则表达式（R1*R2+R3)
	public int eval_type; // 评估类型
	public String disposal; // 处置方式
	public int dp_order; // 处置方式的顺序
	public float score; // 规则分值
	public int order; // 规则顺序
	public long timestamp; // 规则定义修改的时间戳
	public boolean is_enable;
	public boolean is_test;

	public int index;
	public node node; // 对rule_cond进行预编译、优化之后的语法树
	public String message;// 规则命中时的说明

	public int get_index()
	{
		return index;
	}

	void post_init()
	{
		if (this.is_enable)
		{
			if (!str_tool.is_empty(cond))
			{
				node = cond_parser.build(cond);
			}
			else
			{
				throw new tms_exception("规则条件不能为空：", this.toString());
			}
			if (node == null)
			{
				throw new tms_exception("规则条件不能正确编译：", this.toString());
			}
		}
	}

	public String toString()
	{
		return "[" + txn_name + "," + code + "," + name + "," + desc + "," + disposal + "," + dp_order + "," + score + "]";
	}
}
