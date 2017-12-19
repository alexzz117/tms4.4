package cn.com.higinet.tms.engine.core.eval.eval_func_im.score;

import cn.com.higinet.tms.engine.core.cache.db_strategy_rule_eval;
import cn.com.higinet.tms.engine.core.eval.eval_func;
import cn.com.higinet.tms.engine.core.eval.eval_row;

public class eval_func_max extends eval_func {

	@Override
	public String name() {
		return "MAX";
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
		float max = 0.f;
		for (int i = 0, len = datas.length; i < len; i++) {
			if (i == 0) {
				max = Float.valueOf(datas[i]);
			} else {
				float f = Float.valueOf(datas[i]);
				if (f > max)
					max = f;
			}
		}
		row.set_score(max);
		row.set_disposal(stre.ps_code(get_score(max)));
		return row.get_disposal();
	}
}