package cn.com.higinet.tms35.comm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.evalTest;
import cn.com.higinet.tms35.core.cache.cache_init;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.cache.db_fd;
import cn.com.higinet.tms35.core.cache.db_fd_ref;
import cn.com.higinet.tms35.core.cache.db_process;
import cn.com.higinet.tms35.core.cache.db_rule;
import cn.com.higinet.tms35.core.cache.db_rule_action;
import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.core.cache.db_strategy;
import cn.com.higinet.tms35.core.cache.db_tab;
import cn.com.higinet.tms35.core.cache.linear;
import cn.com.higinet.tms35.core.cache.txn;
import cn.com.higinet.tms35.core.cond.compile_cb;
import cn.com.higinet.tms35.core.cond.date_tool;
import cn.com.higinet.tms35.core.cond.node;
import cn.com.higinet.tms35.core.cond.op;
import cn.com.higinet.tms35.core.cond.parser.cond_parser;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public class web_tool
{
	static final Logger log = LoggerFactory.getLogger(web_tool.class);

	/*
	 * 1、找出所有引用某统计的统计、规则和开关 2、编译当前表达式，校验语法错
	 */

	static public boolean find_ref_roster(String roster_name, List<db_stat> ref_stat,
			List<db_rule> ref_rule, List<db_strategy> ref_st, List<db_rule_action> ref_act)
	{
		db_cache dc = db_cache.get();

		for (db_stat s : dc.stat().all())
		{
			if (is_ref_roster(roster_name, s.stat_cond, s.stat_txn))
				ref_stat.add(s);
		}

		for (db_rule r : dc.rule().all())
		{
			if (is_ref_roster(roster_name, r.cond, r.txn_name))
				ref_rule.add(r);
			linear<db_rule_action> acts = dc.rule_action().get_rule_action(r.index);
			for (db_rule_action act : acts)
			{
				if (is_ref_roster(roster_name, act.ac_cond, r.txn_name))
					ref_act.add(act);
			}
		}

		for (db_strategy st : dc.strategy().all())
		{
			if (is_ref_roster(roster_name, st.eval_cond, st.st_txn))
				ref_st.add(st);
		}

		return (ref_stat.size() + ref_st.size() + ref_rule.size() + ref_act.size()) != 0;
	}

	/*
	 * 1、找出所有引用某统计的统计、规则和开关 2、编译当前表达式，校验语法错
	 */

	static public boolean find_ref_stat(String txn_name, String stat_name, List<db_stat> ref_stat,
			List<db_rule> ref_rule, List<db_strategy> ref_st, List<db_rule_action> ref_act,
			List<db_process> ref_ps)
	{
		db_cache dc = db_cache.get();

		for (db_stat s : dc.stat().all())
		{
			if (is_ref_stat(txn_name, stat_name, s.stat_cond, s.stat_txn))
				ref_stat.add(s);
		}

		for (db_rule r : dc.rule().all())
		{
			if (is_ref_stat(txn_name, stat_name, r.cond, r.txn_name))
				ref_rule.add(r);
			linear<db_rule_action> acts = dc.rule_action().get_rule_action(r.index);
			for (db_rule_action act : acts)
			{
				if (is_ref_stat(txn_name, stat_name, act.ac_cond, r.txn_name))
					ref_act.add(act);
			}
		}
		
		for (db_strategy st : dc.strategy().all())
		{
			if (is_ref_stat(txn_name, stat_name, st.eval_cond, st.st_txn))
				ref_st.add(st);
		}

		return (ref_stat.size() + ref_st.size() + ref_rule.size() + ref_act.size() + ref_ps.size()) != 0;
	}

	static public boolean find_ref_field(String tab_name, String fd_ref_name, //
			List<db_fd> ref_fd,//
			List<db_rule_action> ref_act,//
			List<db_stat> ref_stat,//
			List<db_rule> ref_rule,//
			List<db_strategy> ref_st)
	{
		db_cache dc = db_cache.get();

		for (db_fd fd : dc.field().all())
		{
			if (fd.ref != null)
			{
				db_fd_ref fdr = fd.ref;
				if (is_ref_fd(tab_name, fd_ref_name, fdr.src_cond, null))
					ref_fd.add(fd);
				if (is_ref_fd(tab_name, fd_ref_name, fdr.src_expr, null))
					ref_fd.add(fd);
			}
			// else
			// {
			// if (fd.srcid != null && !str_tool.is_empty(fd.cvt_func)
			// && fd.srcid.equals(fd_ref_name))
			// ref_fd.add(fd);
			// }
		}

		for (db_stat s : dc.stat().all())
		{
			if (is_ref_fd(tab_name, fd_ref_name, s.stat_cond, s.stat_txn) && s.stat_txn.startsWith(tab_name))
				ref_stat.add(s);

			if (s.stat_data_fd != null && s.stat_data_fd.ref_name.equals(fd_ref_name) && s.stat_data_fd.tab_name.startsWith(tab_name))
				ref_stat.add(s);

			if (s.stat_txn.indexOf(tab_name) == 0)
			{
				String[] pfd = s.stat_param_fd.split(",");
				for (int i = 0, len = pfd.length; i < len; i++)
				{
					if (fd_ref_name.equals(pfd[i]))
					{
						ref_stat.add(s);
						break;
					}
				}
			}
		}

		for (db_rule r : dc.rule().all())
		{
			if (is_ref_fd(tab_name, fd_ref_name, r.cond, r.txn_name) && r.txn_name.startsWith(tab_name))
				ref_rule.add(r);
			linear<db_rule_action> acts = dc.rule_action().get_rule_action(r.index);
			for (db_rule_action act : acts)
			{
				if (is_ref_fd(tab_name, fd_ref_name, act.ac_cond, r.txn_name) && r.txn_name.startsWith(tab_name))
					ref_act.add(act);
			}
		}

		for (db_strategy st : dc.strategy().all())
		{
			if (is_ref_fd(tab_name, fd_ref_name, st.eval_cond, st.st_txn) && st.st_txn.startsWith(tab_name))
				ref_st.add(st);
		}

		return (ref_fd.size() + ref_stat.size() + ref_st.size() + ref_rule.size() + ref_act.size()) != 0;
	}

	/*
	 * 1、找出所有引用某规则的动作2、编译当前表达式，校验语法错
	 */
	static public boolean find_ref_rule(String txn_name, String rule_name, List<db_rule_action> ref_act)
	{
		db_cache dc = db_cache.get();
		db_rule.cache drc = dc.rule();
		for (db_rule_action a : dc.rule_action().all())
		{
			db_rule dr = drc.get_by_ruleid(a.rule_id);
			if (is_ref_rule(txn_name, rule_name, a.ac_cond, dr.txn_name))
				ref_act.add(a);
		}

		return (ref_act.size()) != 0;
	}

	static private boolean is_ref_fd(final String tab_name, final String ref_fd_name,
			String str_cond, final String str_txn)
	{
		if (str_tool.is_empty(str_cond))
		{
			return false;
		}
		db_cache dc = db_cache.get();

		node n = cond_parser.build(str_cond);

		compile_cb cb = new compile_cb()
		{
			@Override
			public int ref_filed(String name)
			{
				if (ref_fd_name.equals(name))
					return -1;

				return 0;
			}
		};

		return n.compile_rule(dc.table().get(tab_name), dc, cb, null)
				|| n.compile_1(db_cache.get(), cb);
	}

	static private boolean is_ref_stat(final String txnName, final String statName, String swCond,
			final String swTxn)
	{
		if (str_tool.is_empty(swCond))
		{
			return false;
		}
		db_cache dc = db_cache.get();

		node n = cond_parser.build(swCond);

		compile_cb cb = new compile_cb()
		{

			@Override
			public int ref_stat(String group, String group2)
			{
				if (statName.equals(group2) && (txnName.equals(group) //
						|| group == null && txnName.equals(swTxn)))
					return -1;

				return 0;
			}

		};

		return n.compile_rule(dc.table().get(txnName), dc, cb, null)
				|| n.compile_1(db_cache.get(), cb);
	}

	static private boolean is_ref_rule(final String txnName, final String ruleName, String swCond,
			final String swTxn)
	{
		if (str_tool.is_empty(swCond))
		{
			return false;
		}
		db_cache dc = db_cache.get();

		node n = cond_parser.build(swCond);

		compile_cb cb = new compile_cb()
		{
			@Override
			public int ref_rule(String txn, String name)
			{
				return txnName.equalsIgnoreCase(txn) && ruleName.equalsIgnoreCase(name) ? -1 : 0;
			}
		};

		return n.compile_rule(dc.table().get(txnName), dc, cb, null);
	}

	static private boolean is_ref_roster(final String rosterName, String swCond, final String swTxn)
	{
		if (str_tool.is_empty(swCond))
		{
			return false;
		}

		node n = cond_parser.build(swCond);

		compile_cb cb = new compile_cb()
		{

			@Override
			public int ref_roster(String upName)
			{
				return upName.equalsIgnoreCase(rosterName) ? -1 : 0;
			}
		};

		return n.compile_1(db_cache.get(), cb);
	}

	static public boolean compile_expr(String txn_name, String expr, StringBuffer error)
	{
		db_tab tab = db_cache.get().table().get(txn_name);
		if (tab == null)
		{
			error.append("无法找到[" + txn_name + "]的定义\n");
			return false;
		}
		txn _txn = txn.get(tab.index);
		node n = cond_parser.build(expr, error);
		if (n == null)
			return false;
		int len = error.length();
		n.prepare_rule(_txn, error);
		n.prepare1(_txn, error);
		n.prepare2(error);
		//db_cache.set_roster(null);// 清空名单。如果不清空不会再取数据库
		return error.length() == len;
	}

	static public int type(String txn_name, String expr, StringBuffer error)
	{
		db_tab tab = db_cache.get().table().get(txn_name);
		if (tab == null)
		{
			error.append("无法找到[" + txn_name + "]的定义\n");
			return -1;
		}
		txn _txn = txn.get(tab.index);
		node n = cond_parser.build(expr, error);
		if (n == null)
			return -1;
		// int len = error.length();
		n.prepare_rule(_txn, error);
		n.prepare1(_txn, error);
		n.prepare2(error);
		return n.type;
	}
	
	static public String tms_type(String txn_name, String expr, StringBuffer error)
	{
	    int type = type(txn_name, expr, error);
	    return op.type2name(type);
	}

	/*
	 * static public boolean format_expr(String txn_name, String expr,
	 * StringBuffer out) { db_tab tab = db_cache.get().table().get(txn_name); if
	 * (tab == null) { out.append("无法找到[" + txn_name + "]的定义\n"); return false;
	 * }
	 * 
	 * txn _txn = txn.get(tab.index); node n = cond_parser.build(expr, out); if
	 * (n == null) return false;
	 * 
	 * out.append(n.to_loc_string(db_cache.get(), _txn));
	 * 
	 * return true; }
	 */

	static void test(String cond)
	{
		node n = cond_parser.build(cond);
		n.prepare1(txn.get(db_cache.get().table().get_index("T")), null);
		n.prepare2(null);
		n.prepare3(txn.get(db_cache.get().table().get_index("T")));
		System.out.println(n.exec(null));
	}
	
	static void cluster()
	{
		String id="1";
		batch_stmt_jdbc stmt=new batch_stmt_jdbc(new data_source(), "select TIME,OWNER,SERVINFO from TMS_RUN_CLUSTER");
		class lock
		{
		String owner,time;
		}
		
		final lock l=new lock();
		try
		{
			stmt.query(new Object[]{}, new row_fetch()
			{
				@Override
				public boolean fetch(ResultSet rs) throws SQLException
				{
					l.time=rs.getString(1);
					l.owner=rs.getString(2);
					return true;
				}
			});
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
	}


	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.print(date_tool.format(new java.util.Date(-1812096352000L)));
		evalTest.init();
		cache_init.init(new data_source());
		List<db_stat> ref_stat = new ArrayList<db_stat>();
		List<db_rule> ref_rule = new ArrayList<db_rule>();
		List<db_strategy> ref_st = new ArrayList<db_strategy>();
		// List<db_fd> ref_fd = new ArrayList<db_fd>();//
		List<db_rule_action> ref_act = new ArrayList<db_rule_action>();//

		StringBuffer sb = new StringBuffer();
		test("2*5+3*5");
		test("abs(1) and abs(1)");
		test("abs(1) and abs(0)");
		test("abs(0) and abs(1)");
		test("abs(0) and abs(0)");
		test("abs(1) or abs(0)");
		test("abs(0) or abs(1)");
		test("abs(0) or abs(0)");
		test("abs(1) or abs(1)");

//		ip_cache.remote_test();

		test("index(\"1234567890\",\"4567\")");
		compile_expr("T", "#2001-1-1dt", sb);
		// format_expr("T0101",
		// "len(TRUNCATE(\"128\",3)) and ABS(100)>0 or txntime>#2001-1-1dt",
		// sb);

		/*
		 * index lower upper
		 */

		try
		{
			// find_ref_stat("T080115", "S73", ref_stat, ref_rule,
			// ref_sw,ref_act);

			test("7000.2323-1000.9766=5999.2557");
			test("to_string(123+456)");
			test("to_long(123.23+456)");
			test("to_long(\"123.23+456\")");

			test("speed(\"1.88.0.0\",\"42.97.0.0\",#2013-1-1 12:00:00dt,#2013-1-1 13:20:00dt)");

			// test("#2001-1-1 00:04:00dt=#00:00:00t");
			// test("#2001-1-1 00:04:00dt=#00:05:00t");
			// test("#2001-1-1 00:04:00dt=#00:04:00t");
			//
			// test("#2001-1-1 00:04:00dt!=#00:00:00t");
			// test("#2001-1-1 00:04:00dt!=#00:05:00t");
			// test("#2001-1-1 00:04:00dt!=#00:04:00t");
			//
			// test("#2001-1-1 00:04:00dt<=#00:00:00t");
			// test("#2001-1-1 00:04:00dt<=#00:05:00t");
			// test("#2001-1-1 00:04:00dt<=#00:04:00t");
			//
			// test("#2001-1-1 00:04:00dt<#00:00:00t");
			// test("#2001-1-1 00:04:00dt<#00:05:00t");
			// test("#2001-1-1 00:04:00dt<#00:04:00t");
			//
			// test("#2001-1-1 00:04:00dt>=#00:00:00t");
			// test("#2001-1-1 00:04:00dt>=#00:05:00t");
			// test("#2001-1-1 00:04:00dt>=#00:04:00t");
			//
			// test("#2001-1-1 00:04:00dt>#00:00:00t");
			// test("#2001-1-1 00:04:00dt>#00:05:00t");
			// test("#2001-1-1 00:04:00dt>#00:04:00t");

			//			
			// boolean e = compile_expr("T0201",
			// "IP_BLACK (\"192.168.1.1\")",
			// sb);
			// System.out.println(e);

			// compile_expr("T", "2>1 3>2", sb);
			// compile_expr("T", "2>1 \n\n3>2", sb);
			// compile_expr("T", "1/0", sb);

		}
		catch (Exception e)
		{
			log.error(null, e);
		}

		boolean b = find_ref_roster("IP_BLACK", ref_stat, ref_rule, ref_st, ref_act);

		b = is_ref_roster("IP_BLACK", "\"10.8.1.100\" in ip_black", "T");

		System.out.println(b);

		// find_ref_field("T", "TXNTIME", ref_fd, ref_act, ref_stat, ref_rule,
		// ref_sw);
		// find_ref_field("T", "payAmount", ref_fd, ref_act, ref_stat, ref_rule,
		// ref_sw);
		// find_ref_field("T", "USERID", ref_fd, ref_act, ref_stat, ref_rule,
		// ref_sw);
		//
		// {
		// // System.out.println(sb.toString());
		// }
	}
}
