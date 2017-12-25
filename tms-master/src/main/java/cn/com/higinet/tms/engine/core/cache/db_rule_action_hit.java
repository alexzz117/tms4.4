package cn.com.higinet.tms.engine.core.cache;

import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.run.run_env;

public class db_rule_action_hit {
	public db_rule_action_hit(long hitid, run_env re, db_rule_action dra)
	{
		this.hitid = hitid;
		this.txntype = re.get_txntype();
		txncode = str_tool.to_str(re.get_fd_value(re.field_cache().INDEX_TXNCODE));
		//createtime = re.get_txn_time();//updated by wujw,20160601
		createtime =System.currentTimeMillis();
		ruleid = dra.rule_id;
		acid = dra.ac_id;
		ac_cond = dra.ac_cond;
		ac_expr = dra.ac_expr;
	}
	
	public long hitid;
	public long ruletrigid;
	public long alertid;
	public String txntype;
	public String txncode;
	public int ruleid;
	public int acid;
	public String ac_cond;
	public String ac_expr;
	public long createtime;
}