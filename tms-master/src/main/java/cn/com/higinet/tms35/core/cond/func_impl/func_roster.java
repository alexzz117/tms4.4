package cn.com.higinet.tms35.core.cond.func_impl;

import cn.com.higinet.tms35.core.cache.db_roster;
import cn.com.higinet.tms35.core.cond.func;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.core.cache.txn;

/**
 * 添加名单的动作函数
 * @author lining
 *
 */
public class func_roster implements func {

	@Override
	 public  Object exec(Object[] p, int n) {
		synchronized(txn.class){
			run_env re = (run_env) p[n - 1];
			db_roster.cache drc = re.get_txn().g_dc.roster();
			if(drc != null)
			{
				long txn_time = re.get_txn_time();
				if (txn_time <= 0)
				{
					Object txn_time_o = re.get_fd_value(re.get_txn().g_dc.field().INDEX_TXNTIME);
					txn_time = ((Number) txn_time_o).longValue();
				}
				drc.add_value(((Number) p[n + 1]).intValue(), p[n], txn_time);
			}
			return 1;
		}
	}
}