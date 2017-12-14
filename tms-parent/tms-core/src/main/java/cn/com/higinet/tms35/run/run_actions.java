package cn.com.higinet.tms35.run;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.higinet.tms35.comm.StaticParameters;
import cn.com.higinet.tms35.core.cache.db_rule_action;
import cn.com.higinet.tms35.core.cache.db_rule_action_hit;
import cn.com.higinet.tms35.core.cond.func_map;
import cn.com.higinet.tms35.core.dao.dao_rule_action_hit;

public class run_actions
{
	static private Map<String, List<db_rule_action>> miss_hit_cache = null;

	static {
		miss_hit_cache = new HashMap<String, List<db_rule_action>>();
	}
	
	public static void do_action(run_env re)
	{
		List<db_rule_action> actions = miss_hit_cache.get(re.get_txn_code());
		if (actions != null && !actions.isEmpty())
		{
			for (int i = 0; i < actions.size();)
			{
				db_rule_action ac = actions.get(i);
				if(do_action(re, ac))
				{
					if (re.get_txn_status().equals(StaticParameters.TXN_STATUS_INHAND))
					{
						actions.remove(i);
					}
					else
					{
						i++;
					}
				}
				else
				{
					i++;
				}
			}
		}
		if (re.get_txn_status().equals(StaticParameters.TXN_STATUS_SUCCESS)
				|| re.get_txn_status().equals(StaticParameters.TXN_STATUS_FAIL))
		{
			miss_hit_cache.remove(re.get_txn_code());
		}
	}

	private static boolean do_action(run_env re, db_rule_action ac)
	{
		if (ac == null || !ac.is_enable)
			return true;
		if (ac.node_cond == null || ac.node_cond != null
				&& !func_map.is_true(ac.node_cond.exec(re)))
			return false;
		ac.node_expr.exec(re);
		re.get_hit_rule_acts().add(
				new db_rule_action_hit(dao_rule_action_hit.next_hitid(), re, ac));
		return true;
		//re.get_actions().add(ac);
		// 规则测试记录触发动作日志
		//RuleTestLog.ac_log(ac);
	}
	
	public static void add_actions(String txn_code, List<db_rule_action> list)
	{
		miss_hit_cache.put(txn_code, list);
	}
}