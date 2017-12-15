package cn.com.higinet.tms35.core.eval.eval_func_im;

import cn.com.higinet.tms35.core.cache.db_strategy_rule_eval;
import cn.com.higinet.tms35.core.eval.eval_func;
import cn.com.higinet.tms35.core.eval.eval_row;

public class eval_func_dummy extends eval_func {
	
	int eval_mech;
	String name;
	
	public eval_func_dummy(int eval_mech, String name)
	{
		this.eval_mech = eval_mech;
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int eval_mech() {
		return eval_mech;
	}

	@Override
	public String get(eval_row row, db_strategy_rule_eval stre) {
		return null;
	}
}