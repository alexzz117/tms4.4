package cn.com.higinet.tms.engine.core.eval.eval_func_im.score;

import java.math.BigDecimal;

import cn.com.higinet.tms.engine.core.cache.db_strategy_rule_eval;
import cn.com.higinet.tms.engine.core.eval.eval_func;
import cn.com.higinet.tms.engine.core.eval.eval_row;

public class eval_func_sum extends eval_func {

	@Override
	public String name() {
		return "SUM";
	}

	@Override
	public int eval_mech() {
		return EVAL_MECH_SCORE;
	}

	@Override
	public String get(eval_row row, db_strategy_rule_eval stre) {
		if (row == null || row.get_data_length() == 0)
			return "";
		String[] datas = row.get_datas();
		BigDecimal sum = new BigDecimal(0.f);
		for (String s : datas) {
			sum = sum.add(new BigDecimal(s));
		}
		row.set_score(sum.floatValue());
		row.set_disposal(stre.ps_code(get_score(row.get_score())));
		return row.get_disposal();
	}
}