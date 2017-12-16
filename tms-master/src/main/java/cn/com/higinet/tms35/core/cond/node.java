package cn.com.higinet.tms35.core.cond;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.base.util.Arrayz;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.cache.db_fd;
import cn.com.higinet.tms35.core.cache.db_roster;
import cn.com.higinet.tms35.core.cache.db_rule;
import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.core.cache.db_tab;
import cn.com.higinet.tms35.core.cache.str_id;
import cn.com.higinet.tms35.core.cache.txn;
import cn.com.higinet.tms35.core.cache.txn_ref_stat;
import cn.com.higinet.tms35.core.cond.func_map.map_function;
import cn.com.higinet.tms35.core.cond.parser.cond_parser;
import cn.com.higinet.tms35.core.cond.parser.tms_exception_lexer;

final class Stack {
	Object buff[] = new Object[128];

	int pos = -1;

	Object[] buf() {
		return buff;
	}

	void push(Object o) {
		buff[++pos] = o;
	}

	int pop(int n) {
		return pos -= n;
	}
}

public class node {
	static Logger log = LoggerFactory.getLogger(node.class);

	static short fn_index_last = -1;

	static short fn_index_set = -1;

	static short get_fn_last_impl_index() {
		if (fn_index_last != -1)
			return fn_index_last;
		return fn_index_last = func_map.get(op.func_, "last_impl", new byte[] { op.any_ }).fun_index;
	}

	static short get_fn_set_impl_index() {
		if (fn_index_set != -1)
			return fn_index_set;
		return fn_index_set = func_map.get(op.func_, "set_impl", new byte[] { op.any_, op.any_ }).fun_index;
	}

	// 该环境为优化、解释环境，真正运行时，不再需要op，type,name;如果想继续优化，可以构建没有这三个变量的结构
	public String name;

	public short fn_index;

	public byte m_op;

	public byte type;

	public node[] child;

	public Object value;

	public int n1 = -1; // 字段、统计、名单、规则索引

	byte prepare_count;

	public node(int op) {
		this.m_op = (byte) op;
		this.type = -1;
		this.fn_index = -1;
	}

	public node(int op, String name) {
		this.m_op = (byte) op;
		this.name = name;
		this.type = -1;
		this.fn_index = -1;
	}

	public node(int op, int type, Object n) {
		this.m_op = (byte) op;
		this.type = (byte) type;
		this.value = n;
		this.fn_index = -1;
	}

	boolean has_child() {
		return this.child != null && child.length > 0;
	}

	public void copy_from(node n) {
		name = n.name;
		fn_index = n.fn_index;
		m_op = n.m_op;
		type = n.type;
		value = n.value;
		n1 = n.n1;
		child = null;
		if (n.child != null) {
			child = new node[n.child.length];
			for (int i = 0; i < n.child.length; i++) {
				child[i] = new node(op.op_end);
				child[i].copy_from(n.child[i]);
			}
		}
	}

	public void add(node n) {
		if (child == null)
			child = new node[] { n };
		else {
			child = Arrayz.copyOf(child, child.length + 1);
			child[child.length - 1] = n;
		}

	}

	public void add(node[] n) {
		if (child == null)
			child = n;
		else {
			child = Arrayz.copyOf(child, child.length + n.length);
			for (int i = child.length - n.length; i < child.length; i++)
				child[i] = n[i - child.length + n.length];
		}

	}

	public void add(node n1, node n2) {
		if (child == null)
			child = new node[] { n1, n2 };
		else {
			child = Arrayz.copyOf(child, child.length + 2);
			child[child.length - 2] = n1;
			child[child.length - 1] = n1;
		}
	}

	public String depstr(int dep) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < dep; i++)
			sb.append("    ");
		return sb.toString();
	}

	public String debug(int dep) {
		StringBuffer sb = new StringBuffer();
		sb.append(depstr(dep));
		if (this.type > 0) {
			sb.append("[");
			sb.append(op.op_name[type]);
			sb.append("]");
		}
		if (this.fn_index != -1) {
			sb.append("[");
			sb.append(fn_index);
			sb.append("]");
		}
		sb.append(op.op_name[m_op]);
		if (name != null) {
			sb.append(":");
			sb.append(name);
		}
		if (n1 != -1) {
			sb.append("[");
			sb.append(n1);
			sb.append("]");
		}
		if (value != null) {
			sb.append(":");
			sb.append(value_str(type, value));
		}

		if (child != null) {
			if (child.length == 1)
				sb.append("{");
			else
				sb.append("{\n");

			for (int i = 0; i < child.length; i++) {
				sb.append(((node) child[i]).debug(dep + 1));
				if (i < child.length - 1) {
					sb.append(",\n");
				}
			}
			sb.append("}");
		}

		return sb.toString();
	}

	private String value_str(byte type, Object value) {
		if (type == op.datetime_) {
			return "#" + date_tool.format(new Date(((Number) value).longValue())) + "dt";
		} else if (type == op.time_) {
			String t = date_tool.format(new Date(((Number) value).longValue()));
			t.replace("1970-01-01 ", "");
			return "#" + t.replace("1970-01-01 ", "") + "t";
		} else if (type == op.span_) {
			return "#" + ((Number) value).longValue() / 1000 + "S";

		}

		return str_tool.to_str(value);
	}

	public String toString() {
		return debug(0);
	}

	byte[] make_child_type() {
		if (child == null)
			return new byte[0];

		byte[] ret = new byte[child.length];
		for (int i = 0, len = child.length; i < len; i++)
			ret[i] = child[i].type;

		return ret;
	}

	Object[] make_child_value() {
		if (child == null)
			return new Object[0];

		Object[] ret = new Object[child.length];
		for (int i = 0, len = child.length; i < len; i++)
			ret[i] = child[i].value;

		return ret;
	}

	/*
	 * 第1遍优化 1、确定所有符号【名单，字段，统计量，函数】的类型 2、将所有的函数定位到函数序号
	 * 3、优化所有的符号,将所有的字段、统计调整为可以快速索引的格式
	 * 条件的保存，条件中包括交易字段和注册信息（f_n1_n2)，统计(s_n[f_n1_n2,f_n3_n4]);
	 * 系统编译为交易树之后，直接将字段内容存储为f_n,s_n的形式，便于数据的获取和计算
	 * 交易字段，数据直接从run_data.txn_data中使用n为下标获取，类型信息使用n从当前交易视图的中使用n为下标获取
	 * 统计(s_n[f_n1_n2,f_n3_n4])，直接使用n,以及对应的交易数据id从数据库中读取
	 */
	static final Pattern stat_pattern = Pattern.compile("(T[0-9]*):(S[0-9]+)");

	static final Pattern rule_pattern = Pattern.compile("(T[0-9]*):(R[0-9]+)");

	public boolean compile_rule(db_tab tab, db_cache dc, compile_cb ri, StringBuffer sb) {
		if (prepare_count >= 1)
			return false;
		prepare_count = 1;

		if (child != null) {
			for (int i = 0, len = child.length; i < len; i++)
				if (tab != null && child[i].compile_rule(tab, dc, ri, sb)) {
					return true;
				}
		}

		if (name == null)
			return false;

		String up_name = str_tool.upper_case(name);
		Matcher m = rule_pattern.matcher(up_name);
		if (m.matches())// 是规则
		{
			db_rule.cache c = dc.rule();
			db_rule r = c.get(dc.table().get(m.group(1)), m.group(2));
			if (r == null) {
				err_out(sb, String.format("未定义规则:%s", name));
				return false;
			}

			if (ri != null && ri.ref_rule(m.group(1), m.group(2)) != 0)
				return true;

			node tmp = cond_parser.build(r.cond);

			if (tmp == null) {
				m_op = op.const_;
				type = op.long_;
				value = new Long(1);
				name = null;
				return false;
			}

			copy_from(tmp);
		}
		return false;
	}

	public boolean compile_1(db_cache dc, compile_cb ri) {
		if (m_op == op.const_)// 常量
			return false;

		if (prepare_count >= 2)
			return false;
		prepare_count = 2;

		if (child != null) {
			for (int i = 0, len = child.length; i < len; i++)
				if (child[i].compile_1(dc, ri))
					return true;
		}

		if (child != null && child[0].m_op == op.stat_param_)
			m_op = op.stat_;

		String up_name = str_tool.upper_case(name);
		if (m_op != op.stat_) {
			// 所有的函数
			func_map.map_function tm = func_map.get(m_op, up_name, make_child_type());
			if (tm != null) {
				if (name != null && 0 != ri.ref_func(up_name, make_child_type()))
					return true;
				m_op = tm.op;
				type = tm.type;
				fn_index = tm.fun_index;
				name = null;
				return false;
			}

		}
		// 无名函数，但是没有找到匹配的类型
		if (up_name == null)
			return false;

		if (name.equalsIgnoreCase("last") && child.length == 1) {
			if (0 != ri.ref_func(up_name, make_child_type()))
				return true;
			m_op = op.func_;
			type = child[0].type;
			fn_index = 0;
			name = null;
			return false;
		}

		if (name.equalsIgnoreCase("set") && child.length == 2) {
			if (0 != ri.ref_func(up_name, make_child_type()))
				return true;
			m_op = op.func_;
			type = op.long_;
			fn_index = 0;
			name = null;
			return false;
		}

		if (child == null) {
			db_roster.cache drc = dc.roster();
			int id = drc.get_id(up_name);
			if (id >= 0 && ri.ref_roster(up_name) != 0) {
				type = op.table_;
				return true;
			}
		}

		Matcher m = stat_pattern.matcher(up_name);
		if (m.matches())// 是统计,此处没有进行类型处理
		{
			m_op = op.stat_;
			if (ri.ref_stat(m.group(1), m.group(2)) != 0)
				return true;

			return false;
		}

		// 根据名称检查字段是否存在

		if (ri.ref_filed(name) != 0)
			return true;

		return false;
	}

	void err_out(StringBuffer sb, String e) {
		tms_exception_lexer ex;
		log.error(e, ex = new tms_exception_lexer(e));
		if (sb != null)
			sb.append(e).append('\n');

		throw ex;
	}

	public void prepare_rule(txn txn, StringBuffer sb) {
		if (prepare_count >= 1)
			return;
		prepare_count = 1;

		if (child != null) {
			for (int i = 0, len = child.length; i < len; i++)
				child[i].prepare_rule(txn, sb);
		}

		if (name == null)
			return;

		String up_name = str_tool.upper_case(name);
		Matcher m = rule_pattern.matcher(up_name);
		if (m.matches())// 是规则
		{
			db_cache dc = txn.dc();
			db_rule.cache c = dc.rule();
			db_rule r = c.get(dc.table().get(m.group(1)), m.group(2));
			if (r == null) {
				err_out(sb, String.format("未定义规则:%s", name));
				return;
			}
			if (!r.is_enable) {
				err_out(sb, String.format("规则已停用:%s", name));
				return;
			}

			node tmp = cond_parser.build(r.cond);

			if (tmp == null) {
				m_op = op.const_;
				type = op.long_;
				value = new Long(1);
				name = null;
				return;
			}

			copy_from(tmp);
		}
	}

	public void prepare1(txn txn, StringBuffer sb) {
		if (m_op == op.const_)// 常量
			return;

		if (prepare_count >= 2)
			return;
		prepare_count = 2;

		if (child != null) {
			for (int i = 0, len = child.length; i < len; i++)
				child[i].prepare1(txn, sb);
		}

		if (child != null && child[0].m_op == op.stat_param_)
			m_op = op.stat_;

		String up_name = str_tool.upper_case(name);
		if (m_op != op.stat_) {
			// 所有的函数
			func_map.map_function tm = func_map.get(m_op, up_name, make_child_type());
			if (tm != null) {
				m_op = tm.op;
				type = tm.type;
				fn_index = tm.fun_index;
				name = null;
				return;
			}

			// 无名函数，但是没有找到匹配的类型
			if (up_name == null) {
				String error = "没有找到匹配的运算符:" + op.op_name[m_op] + ",当前表达式为" + this;
				this.err_out(null, error);
			}
		}
		if (name == null)
			return;

		if (name.equalsIgnoreCase("last") && child.length == 1) {
			node c = child[0];
			m_op = op.func_;
			type = c.type;

			fn_index = get_fn_last_impl_index();
			name = null;

			if (c != null) {
				c.name = null;
				c.value = new Long(c.n1);
				c.m_op = op.const_;
				c.type = op.long_;
				c.n1 = -1;
			}

			return;
		}

		if (name.equalsIgnoreCase("set") && child.length == 2) {
			node c1 = child[0];
			node c2 = child[1];
			m_op = op.func_;
			type = op.long_;
			fn_index = get_fn_set_impl_index();
			name = null;

			if (c1 != null && c2 != null) {
				c2.type = c1.type;

				c1.name = null;
				c1.value = new Long(c1.n1);
				c1.m_op = op.const_;
				c1.type = op.long_;
				c1.n1 = -1;
			}
			return;
		}

		if (child == null) {
			db_roster.cache drc = txn.g_dc.roster();
			int id = 0;

			//if ((id = drc.get_id(up_name)) >= 0)
			if (null != drc && (id = drc.get_id(up_name)) >= 0) {
				type = op.table_;
				name = null;
				this.n1 = id;
				return;
			}
		}

		Matcher m = stat_pattern.matcher(up_name);
		if (m.matches())// 是统计
		{
			m_op = op.stat_;
			db_tab tab = txn.get_tab();
			db_stat.cache dsc = txn.g_dc.stat();
			db_stat st = dsc.get(m.group(1) == null ? tab.tab_name : m.group(1), m.group(2));
			if (st == null) {
				err_out(sb, String.format("未定义统计:%s", name));
				return;
			}

			if (st.is_valid == 0) {
				err_out(sb, String.format("统计被停用:%s", name));
				return;
			}

			if (st.is_operational == 1 && st.node.type == -1) {
				//统计函数是计算表达式时且此统计尚未编译，手动编译此统计，为了获取此统计的返回值类型
				st.node.prepare_rule(txn, sb);
				st.node.prepare1(txn, sb);
			}

			type = op.name2type(st.type());
			name = "S";
			n1 = st.get_index();
			if (child != null) {
				int param[] = new int[20];
				int ppos = 0;
				for (int i = 0; i < child.length; i++) {
					if (child[i].name.charAt(0) != 'F') {
						err_out(sb, "统计的参数必须是交易属性");
						return;
					}

					if (child[i].m_op == op.stat_param_)
						continue;

					param[ppos++] = child[i].n1;
				}
				txn.set_txn_ref(new txn_ref_stat(txn.get_tab().tab_name, n1, Arrayz.copyOf(param, ppos)), st);
			} else {
				txn.set_txn_ref(new txn_ref_stat(txn.get_tab().tab_name, n1, new int[] {}), st);
			}

			return;
		}

		// 根据名称检查字段是否存在

		db_fd fd = txn.g_dc.field().get(txn.id(), str_tool.upper_case(name));
		if (fd != null) {
			type = op.name2type(fd.type);
			name = "F";
			n1 = fd.get_index(txn.id());
			return;
		}

		if (name.startsWith(db_fd.CVT_FD_PREFIX)) {
			type = op.string_;
			n1 = txn.g_dc.field().get_src_localid(txn.id()).index(new str_id(name.substring(db_fd.CVT_FD_PREFIX.length()), -1));
			name = "C";
			return;
		}

		err_out(sb, String.format("表达式分析错误：无法解释的 标示符'%s'", name));
	}

	// 第2遍优化，优化所有的常量运算
	public void prepare2(StringBuffer sb) {
		if (child == null)// 叶子节点，无需优化
			return;

		if (prepare_count >= 3)
			return;
		prepare_count = 3;

		map_function mf = func_map.get_map(fn_index);
		if (mf == null || !str_tool.is_empty(mf.name))// 有名函数的参数，不进行优化
			return;

		func f = mf.fun_impl;
		if (f == null) {
			err_out(sb, "无法找到函数,index=" + fn_index);
			return;
		}

		for (int i = 0, len = child.length; i < len; i++) {
			child[i].prepare2(sb);
			if (child[i].value == null)// 没有值，说明不是常量，不能用const.
				return;
		}

		value = f.exec(make_child_value(), 0);
		m_op = op.const_;
		child = null;
		fn_index = -1;
	}

	public void prepare3(txn txn) {
		if (prepare_count >= 4)
			return;
		prepare_count = 4;

		if (child != null) {
			for (int i = 0, len = child.length; i < len; i++)
				child[i].prepare3(txn);
		}

		if (m_op == op.stat_) {
			int ntmp = n1;
			if (child == null)
				n1 = txn.get_stat_local_id(n1, new int[] {});
			else {
				int param[] = new int[10];
				int ppos = 0;
				for (int i = 0; i < child.length; i++) {
					if (child[i].m_op == op.stat_param_)
						continue;
					param[ppos++] = child[i].n1;
				}

				n1 = txn.get_stat_local_id(n1, Arrayz.copyOf(param, ppos));
			}

			if (n1 < 0)
				throw new tms_exception("无法找到局部统计引用", ntmp);
		}
	}



	public static void test(int txn_id) {
		// txn txn = new txn();
		// long mm = System.currentTimeMillis();
		// node[] n = null;
		//
		// for (int i = 0; i < 1; i++)
		// n = new node[] {
		// // new
		// //
		// cond_parser().build("(-1)*3+2-5*3<0 and 3>=0 or 2<=2 and 1!=2 and 2<>1 and \"1\"+1=\"11\""),
		// new cond_parser().build("\"1111111\"+-123+abs(-12)"),
		// // new cond_parser().build("1+2+3+4+5+6+7+8+9+10+11"),
		// // new cond_parser().build("1-2-3-4-5-6-7-8-9-10-11"),
		//
		// new cond_parser().build("T:S1>S1"),
		// new cond_parser().build("T:S1>T0101:S1"),
		//
		// // new cond_parser().build("(-1)*3+2-5*3 IN WHITE_TABLE"),
		// // new cond_parser().build("\"acc001\" NOTIN BLACK_TABLE"),
		// // new
		// // cond_parser().build("\"acc001\"+\"010101\" NOTIN BLACK_TABLE"),
		// //
		// // new cond_parser().build("tm_year(#2013-3-1dt)=2013"),
		// // new cond_parser().build("tm_month(#2013-3-1dt)=3"),
		// // new cond_parser().build("tm_day(#2013-3-1dt)=1"),
		// // new cond_parser().build("tm_wday(#2013-3-1dt)=5"),
		// // new cond_parser().build("tm_hour(#2013-3-1 13:14:15dt)=13"),
		// // new cond_parser().build("tm_minute(#2013-3-1 13:14:15dt)=14"),
		// // new cond_parser().build("tm_second(#2013-3-1 13:14:15dt)=15"),
		// // new cond_parser().build("tm_year(#2013-3-1dt)=2013"),
		// // new cond_parser().build("tm_year(#2013-3-1dt)=2013"),
		// //
		// // new
		// // cond_parser().build("tm_date(#2000-1-1 13:14:15dt)=#2000-1-1dt"),
		// // new
		// // cond_parser().build("tm_time(#2000-1-1 13:14:15dt)=#13:14:15t"),
		// // new cond_parser().build("#2000-1-1 13:14:15dt=#13:14:15t"),
		// //
		// // new cond_parser().build("A+1>2"),
		// // new
		// //
		// cond_parser().build("#2013-3-1dt-#2013-1-1dt > #60D/2 AND PAYAMOUNT>0 AND 10*10-2>3*5"),
		// new cond_parser().build("T0101:S1(USERID)<>1"),
		// // new cond_parser().build("S1(USERID)<>1"),
		// // new
		// //
		// cond_parser().build("S1>S2 and T::S1>#200D OR V1 > #2011-1-1dt and day(TRAN_DATE)=day(S3) and T01::S2(Field1,Field2,Field3,Field4)<>1"),
		// // new cond_parser().build("1>2 AND V1 > #2011-1-1 12:0:0dt")
		// };
		// System.out.println(System.currentTimeMillis() - mm);
		// mm = System.currentTimeMillis();
		// compile_cb ref=new compile_cb(){
		// };
		// for (int j = 0; j < n.length; j++)
		// {
		// System.out.println(n[j]);
		// n[j].compile_1(ref);
		// }
		//		
		// if(true)
		// return;
		// System.out.println(System.currentTimeMillis() - mm);
		// System.out.println("-----------------------------------------------------------------------------");
		// mm = System.currentTimeMillis();
		// for (int i = 0; i < 1; i++)
		// for (int j = 0; j < n.length; j++)
		// {
		// // System.out.println(n[j]);
		// System.out.println(n[j]);
		// // n[j].prepare2();
		// // System.out.println(n[j]);
		// Object o = n[j].exec(null);
		// System.out.println(o);
		// }
		//
		// System.out.println(System.currentTimeMillis() - mm);
	}

	public static void main(String args[]) {
		test(0);
	}

	public String to_loc_string(db_cache dc, txn txn) {
		switch (m_op) {
			case op.neg_: {
				return "-" + child[0].to_loc_string(dc, txn);
			}

			case op.eq_:
			case op.ne_:
			case op.lt_:
			case op.le_:
			case op.gt_:
			case op.ge_:
			case op.and_:
			case op.or_: {

			}
			case op.in_:
			case op.notin_:
			case op.add_:
			case op.sub_:
			case op.mul_:
			case op.div_:
			case op.mod_: {
				return "(" + child[0].to_loc_string(dc, txn) //
						+ (child[0].has_child() && child[1].has_child() ? "\n" : " ")//
						+ "[" + op.op_name_loc[m_op] + "] " //
						+ child[1].to_loc_string(dc, txn) + ")";
			}

			case op.func_: {
				StringBuffer sb1 = new StringBuffer();
				sb1.append(this.name).append("(");
				for (int i = 0; i < child.length; i++)
					sb1.append(child[i].to_loc_string(dc, txn)).append(",");
				if (child.length > 0)
					sb1.setLength(sb1.length() - 1);
				sb1.append(")");
				return sb1.toString();
			}
			case op.id_: {
				return to_loc_string_id(dc, txn);
			}
			case op.const_:
				return value_str(type, value);
		}

		return "??";
	}

	private String to_loc_string_id(db_cache dc, txn txn) {
		String up_name = str_tool.upper_case(name);

		// // 所有的函数
		// func_map.map_function tm = func_map.get(m_op, up_name,
		// make_child_type());
		// if (tm != null)
		// {
		// if (name != null)
		// if (0 != ri.ref_func(up_name, make_child_type()))
		// return true;
		// m_op = tm.op;
		// type = tm.type;
		// fn_index = tm.fun_index;
		// name = null;
		// return false;
		// }
		//
		// if (name == null)
		// return false;

		if (name.equalsIgnoreCase("last") && child != null && child.length == 1)
			return "last(" + child[0].to_loc_string(dc, txn) + ")";

		if (name.equalsIgnoreCase("set") && child != null && child.length == 2)
			return "set(" + child[0].to_loc_string(dc, txn) + "," + child[1].to_loc_string(dc, txn) + ")";

		db_roster.cache drc = dc.roster();

		if (drc.get_id(up_name) >= 0) {
			type = op.table_;
			db_roster dr = drc.get(up_name);
			return dr.name;
		}

		Matcher m = stat_pattern.matcher(name);
		if (m.matches())// 是统计
		{
			m_op = op.stat_;
			db_stat ds = dc.stat().get(txn.get_tab().tab_name, up_name);

			return "[" + ds.stat_desc + "]";
		}

		db_fd fd = txn.g_dc.field().get(txn.id(), up_name);

		if (fd != null)
			return "[" + fd.name + "]";

		throw new tms_exception_lexer("无法识别的标示符:", up_name);
	}
}
