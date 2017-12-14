package cn.com.higinet.tms35.core.cache;

import java.util.ArrayList;
import java.util.List;

import cn.com.higinet.tms35.comm.bin_stream;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.run.run_env;

public class db_rule_hit {

	public db_rule_hit(long trigid, run_env re, db_rule dr, int num_times_ms, long txn_time) {

		/*		long start_time = 0l;
				String sql = "SELECT ((SYSDATE) - TO_DATE('1970-01-01 08:00:00', 'yyyy-MM-dd HH24:mi:ss')) * 24 * 60 * 60*1000 AS DATETIME FROM DUAL";
				SqlRowSet queryResult = bean.get(dao_base.class).query(sql);
				//暂时先这样取数据库时间来减少同步在线库命中规则表数据到离线库的数据遗漏 
				if(queryResult.next()){
					String currentTime = queryResult.getString("DATETIME");
					start_time = Long.parseLong(currentTime)+40*1000;
				}*/

		this.trigid = trigid;
		txncode = str_tool.to_str(re.get_fd_value(re.field_cache().INDEX_TXNCODE));
		createtime = txn_time;
		//		createtime = start_time;
		ruleid = dr.id;
		numtimes = num_times_ms;
		txntype = re.get_txntype();
		message = dr.message;
		rule_score = dr.score;
		this.dr = dr;
	}

	public db_rule_hit() {
	}

	public long trigid;

	public long createtime;

	public int ruleid;

	public int numtimes;

	public String txncode;

	public String txntype;

	public String message;

	public double rule_score;

	public db_rule dr;

	public static db_rule_hit load_from(bin_stream bs) {
		db_rule_hit d = new db_rule_hit();
		d.trigid = bs.load_long();
		d.createtime = bs.load_long();
		d.ruleid = bs.load_int();// 
		d.numtimes = bs.load_int();// 
		d.message = bs.load_string();//
		d.txntype = bs.load_string();// 
		d.txncode = bs.load_string();//
		d.rule_score = bs.load_double();

		return d;
	}

	public void save_to(bin_stream bs) {
		bs.save(trigid)// 
				.save(createtime)// 
				.save(ruleid)// 
				.save(numtimes)// 
				.save(message)// 
				.save(txntype)// 
				.save(txncode).save(rule_score);
	}

	public static void save_to(bin_stream bs, List<db_rule_hit> list) {
		bs.save(list.size());
		for (int i = 0, len = list.size(); i < len; i++)
			list.get(i).save_to(bs);
	}

	static public List<db_rule_hit> load_from_list(bin_stream bs) {
		List<db_rule_hit> list = null;
		int n = bs.load_int();
		if (n > 0) {
			list = new ArrayList<db_rule_hit>();
			for (int i = 0; i < n; i++) {
				db_rule_hit sess = db_rule_hit.load_from(bs);
				list.add(sess);
			}
		}
		return list;
	}

}
