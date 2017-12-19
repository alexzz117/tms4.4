package cn.com.higinet.tms.engine.core.eval.eval_func_im.score;

import java.math.BigDecimal;

import cn.com.higinet.tms.engine.core.cache.db_strategy_rule_eval;
import cn.com.higinet.tms.engine.core.eval.eval_func;
import cn.com.higinet.tms.engine.core.eval.eval_row;

public class eval_func_avg extends eval_func {

	@Override
	public String name() {
		return "AVG";
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
		int num = 0;
		BigDecimal sum = new BigDecimal(0.f);
		for (String s : datas) {
			sum = sum.add(new BigDecimal(s));
			num++;
		}
		float avg = sum.divide(new BigDecimal(num)).floatValue();
		row.set_score(avg);
		row.set_disposal(stre.ps_code(get_score(avg)));
		return row.get_disposal();
	}
}