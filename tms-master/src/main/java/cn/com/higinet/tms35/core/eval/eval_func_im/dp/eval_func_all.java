package cn.com.higinet.tms35.core.eval.eval_func_im.dp;

import cn.com.higinet.tms35.core.cache.db_strategy_rule_eval;
import cn.com.higinet.tms35.core.eval.eval_func;
import cn.com.higinet.tms35.core.eval.eval_row;

public class eval_func_all extends eval_func {

	@Override
	public String name() {
		return "ALL";
	}

	@Override
	public int eval_mech() {
		return EVAL_MECH_DISPOSAL;
	}

	@Override
	public String get(eval_row row, db_strategy_rule_eval stre) {
		if (row == null || row.get_data_length() == 0)
			return "";
		String[] datas = row.get_datas();
		String data = "";
		for (String d : datas) {
			if (data.compareTo(d) < 0)
				data = d;
		}
		row.set_disposal(data);
		return data;
	}
}