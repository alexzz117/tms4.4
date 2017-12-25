package cn.com.higinet.tms35.stat.stat_func_im;

import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.core.cache.db_userpattern;
import cn.com.higinet.tms35.core.cond.op;
import cn.com.higinet.tms35.stat.stat_func;
import cn.com.higinet.tms35.stat.stat_row;

/**
 * 计算表达式
 * @author liining
 */
public class stat_func_expr_calculat extends stat_func {

	@Override
	public String name() {
		return "CALCULAT_EXPRESSIONS";
	}

	@Override
	public String type(db_stat st) {
		return op.type2name(st.node.type);
	}

	@Override
	public Object get(String data, db_stat stat, int cur_minute,
			Object cur_value) {
		return null;
	}

	@Override
	public Object getPatternValue(db_stat st, stat_row sd, db_userpattern up,
			long txntime, int txn_minute, Object curVal) {
		return null;
	}

	@Override
	public String union_(String d1, String d2, db_stat stat)
	{
		return null;
	}

	@Override
	public Object union_item(Object w1, Object w2)
	{
		return null;
	}

	

	@Override
	public String set(String d, db_stat stat, int cur_minute, Object cur_value) {
		return null;
	}

	@Override
	public boolean need_curval_when_get() {
		return false;
	}

	@Override
	public boolean need_curval_when_set() {
		return false;
	}

	@Override
	public Object getAll(String data, db_stat stat, int cur_minute) {
		return null;
	}
}