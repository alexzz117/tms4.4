package cn.com.higinet.tms35.core.eval;

import java.math.BigDecimal;
import java.util.Arrays;

import cn.com.higinet.tms35.core.cache.db_rule;
import cn.com.higinet.tms35.core.cache.db_strategy_rule_eval;
import cn.com.higinet.tms35.core.eval.eval_func_im.eval_func_dummy;
import cn.com.higinet.tms35.core.eval.eval_func_im.dp.eval_func_all;
import cn.com.higinet.tms35.core.eval.eval_func_im.dp.eval_func_break;
import cn.com.higinet.tms35.core.eval.eval_func_im.dp.eval_func_continue;
import cn.com.higinet.tms35.core.eval.eval_func_im.score.eval_func_avg;
import cn.com.higinet.tms35.core.eval.eval_func_im.score.eval_func_max;
import cn.com.higinet.tms35.core.eval.eval_func_im.score.eval_func_min;
import cn.com.higinet.tms35.core.eval.eval_func_im.score.eval_func_sum;


/**
 * 评估函数基类
 * @author lining
 *
 */
public abstract class eval_func implements Comparable<eval_func> {
	protected static final int EVAL_MECH_SCORE = 0;	//评估机制-分值
	protected static final int EVAL_MECH_DISPOSAL = 1;	//评估机制-处置
	static final float RULE_START_SCORE = -100.f;
	static final float RULE_STOP_SCORE = 100.f;
	static eval_func[] g_func = new eval_func[] {
		new eval_func_max(), new eval_func_min(), new eval_func_sum(), new eval_func_avg(),
		new eval_func_all(), new eval_func_continue(), new eval_func_break()
	};
	
	static {
		Arrays.sort(g_func);
	}
	
	static final int get_local_id(int eval_mech, String name)
	{
		return Arrays.binarySearch(g_func, new eval_func_dummy(eval_mech, name));
	}

	public final static eval_func get(int eval_mech, String name)
	{
		if (eval_mech < 0 || name == null)
			return null;
		int index = get_local_id(eval_mech, name);
		if (index < 0) {
			System.out.println(index);
			return null;
		}
		return g_func[index];
	}
	
	final public int compareTo(eval_func o)
	{
		if (eval_mech() != o.eval_mech()) {
			return eval_mech() - o.eval_mech();
		}
		return name().toLowerCase().compareTo(o.name().toLowerCase());
	}
	
	public boolean is_continue(int length)
	{
		return false;
	}
	
	
	public boolean is_break(int length)
	{
		return false;
	}
	
	public final boolean is_disposal()
	{
		return this.eval_mech() == EVAL_MECH_DISPOSAL;
	}
	
	public abstract String name();
	
	public abstract int eval_mech(); 
	
	public final void set(eval_row row, db_rule rule) {
		if (this.is_disposal()) {
			row.append(rule.disposal);
			BigDecimal row_score = new BigDecimal(row.get_score());
			BigDecimal rule_score = new BigDecimal(rule.score);
			row.set_score(row_score.add(rule_score).floatValue());
		} else {
			row.append(rule.score);
		}
	}
	
	public static float get_score(float f) {
		if(f < RULE_START_SCORE)
			return RULE_START_SCORE;
		if (f > RULE_STOP_SCORE)
			return RULE_STOP_SCORE;
		return f;
	}
	
	public abstract String get(eval_row row, db_strategy_rule_eval stre);
}