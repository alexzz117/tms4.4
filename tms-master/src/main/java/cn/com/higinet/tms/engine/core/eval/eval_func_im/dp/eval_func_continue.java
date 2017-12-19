package cn.com.higinet.tms.engine.core.eval.eval_func_im.dp;

public class eval_func_continue extends eval_func_all {

	@Override
	public String name() {
		return "CONTINUE";
	}
	
	@Override
	public boolean is_continue(int length) {
		if (length <= 0)
			return false;
		return true;
	}
}