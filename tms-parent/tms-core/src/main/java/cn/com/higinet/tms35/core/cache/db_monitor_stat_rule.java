package cn.com.higinet.tms35.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import cn.com.higinet.tms35.comm.date_tool;
import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.run.run_env;

public class db_monitor_stat_rule extends db_monitor_stat {
	
	
	public db_monitor_stat_rule() {}
	
	public db_monitor_stat_rule(run_env re, db_rule_hit hit) {
		this(re);
		if (hit == null) {
			ruleid = NULL_CODE;
		} else {
			ruleid = String.valueOf(hit.ruleid);
			hit_number = 1;
			rule_runtime_max = rule_runtime_min = hit.numtimes;
			rule_runtime_avg = hit.numtimes;
		}
	}

	public db_monitor_stat_rule(run_env re) {
		super(re);
		txnid = re.get_txntype();
	}
	
	@Override
	public void assign_stat(ResultSet rs) throws SQLException {
	    super.assign_stat(rs);
	    this.txnid = rs.getString("TXNID");
	    this.ruleid = rs.getString("RULEID");
	    this.trig_number = rs.getLong("TRIG_NUMBER");
	    this.hit_number = rs.getLong("HIT_NUMBER");
	    this.rule_runtime_max = rs.getLong("RULE_RUNTIME_MAX");
	    this.rule_runtime_min = rs.getLong("RULE_RUNTIME_MAX");
	    this.rule_runtime_avg = rs.getFloat("RULE_RUNTIME_AVG");
	}
	
	@Override
	public db_monitor_stat update_stat(db_monitor_stat stat) {
		db_monitor_stat_rule stat_rule = (db_monitor_stat_rule) stat;
		super.update_stat(stat_rule);
		trig_number += stat_rule.trig_number;
		if (rule_runtime_max < stat_rule.rule_runtime_max) {
			rule_runtime_max = stat_rule.rule_runtime_max;
		}
		if (rule_runtime_min > stat_rule.rule_runtime_min) {
			rule_runtime_min = stat_rule.rule_runtime_min;
		}
		if (hit_number + stat_rule.hit_number <= 0) {
			rule_runtime_avg = 0;
		} else {
			rule_runtime_avg = ((hit_number * rule_runtime_avg) + 
					(stat_rule.hit_number * stat_rule.rule_runtime_avg)) / 
						(hit_number += stat_rule.hit_number);
		}
		return this;
	}
	
	@Override
	public String name() {
		return super.name() + "_" + txnid + "_" + ruleid;
	}
	
	public static class cache {
		private Map<String, Map<String, Map<String, db_monitor_stat_rule>>> stat_rule_cache = new HashMap<String, Map<String, Map<String, db_monitor_stat_rule>>>();
		private Map<String, Map<String, Long>> txn_trig_number_cache = new HashMap<String, Map<String, Long>>();
		
		public static cache load(data_source ds) {
			cache c = new cache();
			c.init(ds);
			return c;
		}

		public void init(data_source ds) {
			String sql = "select IPPORT, TXNID, RULEID, TIME, TRIG_NUMBER, HIT_NUMBER, "
					+ "RULE_RUNTIME_MAX, RULE_RUNTIME_MIN, RULE_RUNTIME_AVG from TMS_MONITOR_RULE_STAT "
					+ "where IPPORT = ? and TIME > ?";
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {
					java.sql.Types.VARCHAR, java.sql.Types.VARCHAR });
			try {
				stmt.query(new Object[] { String.format(COMBINATION_FIELD_STRING,
						ipaddr, port), stat_time }, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_monitor_stat_rule rule = new db_monitor_stat_rule();
						rule.assign_stat(rs);
						rule.set_indb(true);
						String date = date_tool.get_date_time(format, rule.time);
						if (stat_rule_cache.containsKey(date)) {
							if (stat_rule_cache.get(date).containsKey(rule.txnid)) {
								stat_rule_cache.get(date).get(rule.txnid).put(rule.ruleid, rule);
								if (rule.trig_number > txn_trig_number_cache.get(date).get(rule.txnid)) {
								    txn_trig_number_cache.get(date).put(rule.txnid, rule.trig_number);
								}
							} else {
								Map<String, db_monitor_stat_rule> ruleMap = new HashMap<String, db_monitor_stat_rule>();
								ruleMap.put(rule.ruleid, rule);
								stat_rule_cache.get(date).put(rule.txnid, ruleMap);
								txn_trig_number_cache.get(date).put(rule.txnid, rule.trig_number);
							}
						} else {
							Map<String, Map<String, db_monitor_stat_rule>> cacheMap = new HashMap<String, Map<String,db_monitor_stat_rule>>();
							Map<String, db_monitor_stat_rule> ruleMap = new HashMap<String, db_monitor_stat_rule>();
							ruleMap.put(rule.ruleid, rule);
							cacheMap.put(rule.txnid, ruleMap);
							stat_rule_cache.put(date, cacheMap);
							Map<String, Long> trigNumMap = new HashMap<String, Long>();
							trigNumMap.put(rule.txnid, rule.trig_number);
							txn_trig_number_cache.put(date, trigNumMap);
						}
						return true;
					}
				});
			} catch (SQLException e) {
				e.printStackTrace();
				throw new tms_exception("load db_monitor_stat_rule.cache error.");
			} finally {
				stmt.close();
			}
		}
		/**
		 * Map<datetime, Map<txntype, Map<ruleid, db_monitor_stat_rule>>>
		 * @return
		 */
		public Map<String, Map<String, Map<String, db_monitor_stat_rule>>> get_stat_rule_cache() {
			return stat_rule_cache;
		}
		
		public Map<String, Map<String, Long>> get_txn_trig_number_cache() {
		    return txn_trig_number_cache;
		}
	}

	public String ruleid;// 规则编码
	public String txnid;//交易编码
	public long modtime;// 修改时间
	public long trig_number;// 触发数，实际是交易执行次数
	public long hit_number;// 命中数
	public long rule_runtime_max;// 规则最大执行时间
	public long rule_runtime_min;// 规则最小执行时间
	public float rule_runtime_avg;// 规则平均执行时间
}