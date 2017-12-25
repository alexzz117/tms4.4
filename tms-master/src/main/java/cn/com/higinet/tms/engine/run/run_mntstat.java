package cn.com.higinet.tms.engine.run;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.date_tool;
import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.core.cache.db_monitor_stat;
import cn.com.higinet.tms.engine.core.cache.db_monitor_stat_rule;
import cn.com.higinet.tms.engine.core.cache.db_monitor_stat_txn_area;
import cn.com.higinet.tms.engine.core.cache.db_monitor_stat_txn_dp;
import cn.com.higinet.tms.engine.core.cache.db_monitor_stat_txn_time;
import cn.com.higinet.tms.engine.core.cache.db_rule_hit;
import cn.com.higinet.tms.engine.core.cache.linear;
import cn.com.higinet.tms.engine.core.dao.dao_monitor_stat_rule;
import cn.com.higinet.tms.engine.core.dao.dao_monitor_stat_txn_area;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;

public class run_mntstat {
	static Logger log = LoggerFactory.getLogger(run_mntstat.class);
	private db_monitor_stat_txn_area.cache stat_txn_area_cache;
	private db_monitor_stat_txn_time.cache stat_txn_time_cache;
	private db_monitor_stat_txn_dp.cache stat_txn_dp_cache;
	private db_monitor_stat_rule.cache stat_rule_cache;
	private run_stat_commit stat_commit;
	private static String format = db_monitor_stat.format;
    private static int cache_size = db_monitor_stat.cache_size;
    private dao_monitor_stat_rule m_dao_stat_rule;
    private dao_monitor_stat_txn_area m_dao_stat_txn_area;
    
	public static run_mntstat load(data_source ds, dao_monitor_stat_rule 
			m_dao_stat_rule, dao_monitor_stat_txn_area m_dao_stat_txn_area) {
		run_mntstat run_stat = new run_mntstat(ds, m_dao_stat_rule, m_dao_stat_txn_area);
		return run_stat;
	}
	
	private run_mntstat(data_source ds, dao_monitor_stat_rule 
			m_dao_stat_rule, dao_monitor_stat_txn_area m_dao_stat_txn_area) {
		stat_txn_area_cache = db_monitor_stat_txn_area.cache.load(ds);
		stat_txn_time_cache = db_monitor_stat_txn_time.cache.load(ds);
		stat_txn_dp_cache = db_monitor_stat_txn_dp.cache.load(ds);
		stat_rule_cache = db_monitor_stat_rule.cache.load(ds);
		this.m_dao_stat_rule = m_dao_stat_rule;
		this.m_dao_stat_txn_area = m_dao_stat_txn_area;
		stat_commit = new run_stat_commit();
	}
	
	public run_stat_commit get_stat_commit() {
		return stat_commit;
	}

	public void monitor_stat(run_db_commit row, long curr_time) {
		run_txn_values txn = row.get_txn_data();
		try {
			long txn_time = ((Number) txn.get_fd(txn.m_env.field_cache().INDEX_TXNTIME)).longValue();
			long run_time = (Long) txn.m_env.getStartTime();
			String date_time = date_tool.get_date_time(format, txn_time);//当前交易时间
			String run_date_time = date_tool.get_date_time(format, run_time);//当前时间
			load_history_monitor_stat(txn.m_env.is_confirm(), date_time);
			monitor_stat_txn_area(txn.m_env, stat_txn_area_cache, date_time, curr_time);
			monitor_stat_txn_time(txn.m_env, stat_txn_time_cache, run_date_time, curr_time);
			if (!txn.m_env.is_confirm()) {
				monitor_stat_txn_dp(txn.m_env, stat_txn_dp_cache, date_time);
				monitor_stat_rule(txn.m_env, stat_rule_cache, date_time, curr_time);
			}
		} catch (RuntimeException e) {
			log.error("交易流水号: " + str_tool.to_str(txn.get_fd(
					txn.m_env.field_cache().INDEX_TXNCODE)) + ", 实时监控统计出错.", e);
			throw e;
		}
	}
	
	public void monitor_stat_commit() {
		stat_commit.get_stat_txn_dps().addAll(stat_txn_dp_cache.get_stat_txn_dp_cache());
	}
	
	public void remove_timeout(long txn_time, long curr_time) {
		String del_txn_datetime = delete_date_time(format, txn_time, cache_size);//根据当前交易时间计算出要删除的交易时间
		String del_date_time = delete_date_time(format, curr_time, cache_size);
		//删除过期缓存
		if (log.isDebugEnabled()) {
			log.debug("删除" + del_txn_datetime + "时间段的交易和规则统计");
		}
		remove_timeout(del_txn_datetime, stat_txn_area_cache.get_stat_txn_area_cache());
		remove_timeout(del_txn_datetime, stat_rule_cache.get_stat_rule_cache());
		remove_timeout(del_txn_datetime, stat_rule_cache.get_txn_trig_number_cache());
		remove_timeout(del_date_time, stat_txn_time_cache.get_stat_txn_time_cache());
	}
	
	private void remove_timeout(String timeout, Map<String, ?> statMap) {
		Iterator<String> it = statMap.keySet().iterator();
		while (it.hasNext()) {
			String datetime = it.next();
			if (datetime.compareTo(timeout) <= 0) {
				it.remove();
			}
		}
	}
	
	private void load_history_monitor_stat(boolean is_confirm, String datetime) {
		String ipport = String.format(db_monitor_stat.COMBINATION_FIELD_STRING, 
				db_monitor_stat.ipaddr, db_monitor_stat.port);
		long time = date_tool.get_date_time(format, datetime);
		boolean query_db = false;
		Map<String, db_monitor_stat_txn_area> tm_stat_txn_area = stat_txn_area_cache.get_stat_txn_area_cache().get(datetime);
		if (tm_stat_txn_area == null) {
			if (stat_txn_area_cache.get_stat_txn_area_cache().isEmpty()) {
				query_db = true;
			} else {
				Iterator<String> it = stat_txn_area_cache.get_stat_txn_area_cache().keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					if (datetime.compareTo(key) < 0) {
						query_db = true;
						break;
					}
				}
			}
			if (query_db) {
				try {
					linear<db_monitor_stat_txn_area> list = m_dao_stat_txn_area.read_list(ipport, time);
					Map<String, db_monitor_stat_txn_area> cacheMap = new HashMap<String, db_monitor_stat_txn_area>();
					for (db_monitor_stat_txn_area stat : list) {
						cacheMap.put(stat.name(), stat);
					}
					if (!cacheMap.isEmpty()) {
						stat_txn_area_cache.get_stat_txn_area_cache().put(datetime, cacheMap);
					}
				} catch (SQLException e) {
					log.error("load db_monitor_stat_txn_area history data error.", e);
				}
			}
		}
		query_db = false;
		if (!is_confirm) {
			Map<String, Map<String, db_monitor_stat_rule>> tm_stat_rule = stat_rule_cache.get_stat_rule_cache().get(datetime);
			if (tm_stat_rule == null) {
				if (stat_rule_cache.get_stat_rule_cache().isEmpty()) {
					query_db = true;
				} else {
					Iterator<String> it = stat_rule_cache.get_stat_rule_cache().keySet().iterator();
					while (it.hasNext()) {
						String key = it.next();
						if (datetime.compareTo(key) < 0) {
							query_db = true;
							break;
						}
					}
				}
				if (query_db) {
					try {
						linear<db_monitor_stat_rule> list = m_dao_stat_rule.read_list(ipport, time);
						Map<String, Map<String, db_monitor_stat_rule>> cacheMap = new HashMap<String, Map<String,db_monitor_stat_rule>>();
						Map<String, Long> trigNumMap = new HashMap<String, Long>();
						for (db_monitor_stat_rule stat : list) {
							Map<String, db_monitor_stat_rule> ruleMap = cacheMap.get(stat.txnid);
							boolean null_hit_rule = stat.ruleid.equals(db_monitor_stat.NULL_CODE);// 是否为只为记录交易数的记录,ruleid = -1时
							if (ruleMap == null) {
								ruleMap = new HashMap<String, db_monitor_stat_rule>();
								cacheMap.put(stat.txnid, ruleMap);
								if (!null_hit_rule) {
									trigNumMap.put(stat.txnid, stat.trig_number);
								}
							} else {
								if (!null_hit_rule) {
									Long trig_number = trigNumMap.get(stat.txnid);
									if (trig_number != null && stat.trig_number > trig_number) {
										trigNumMap.put(stat.txnid, stat.trig_number);
									}
								}
							}
							ruleMap.put(stat.ruleid, stat);
						}
						if (!cacheMap.isEmpty()) {
							stat_rule_cache.get_stat_rule_cache().put(datetime, cacheMap);
							stat_rule_cache.get_txn_trig_number_cache().put(datetime, trigNumMap);
						}
					} catch (SQLException e) {
						log.error("load db_monitor_stat_rule history data error.", e);
					}
				}
			}
		}
	}

	private void monitor_stat_txn_area(run_env re,
			db_monitor_stat_txn_area.cache stat_txn_area_cache, String datetime, long curr_time) {
		Map<String, db_monitor_stat_txn_area> tm_stat_txn_area = stat_txn_area_cache
				.get_stat_txn_area_cache().get(datetime);
		if (tm_stat_txn_area == null) {
			tm_stat_txn_area = new HashMap<String, db_monitor_stat_txn_area>();
			stat_txn_area_cache.get_stat_txn_area_cache().put(datetime, tm_stat_txn_area);
		}
		db_monitor_stat_txn_area stat_txn_area = new db_monitor_stat_txn_area(re);
		db_monitor_stat_txn_area c_stat_txn_area = tm_stat_txn_area.get(stat_txn_area.name());
		if (c_stat_txn_area != null) {
			c_stat_txn_area.modtime = curr_time;
			c_stat_txn_area.update_stat(stat_txn_area);//更新统计数据
			stat_commit.get_stat_txn_areas().add(c_stat_txn_area);
		} else {
			//新增统计数据
			stat_txn_area.time = date_tool.get_date_time(format, datetime);
			stat_txn_area.modtime = curr_time;
			tm_stat_txn_area.put(stat_txn_area.name(), stat_txn_area);
			stat_commit.get_stat_txn_areas().add(stat_txn_area);
		}
	}
	
	private void monitor_stat_txn_time(run_env re,
			db_monitor_stat_txn_time.cache stat_txn_time_cache, String datetime, long curr_time) {
		Map<String, db_monitor_stat_txn_time> tm_stat_txn_time = stat_txn_time_cache
				.get_stat_txn_time_cache().get(datetime);
		if (tm_stat_txn_time == null) {
			tm_stat_txn_time = new HashMap<String, db_monitor_stat_txn_time>();
			stat_txn_time_cache.get_stat_txn_time_cache().put(datetime, tm_stat_txn_time);
		}
		db_monitor_stat_txn_time stat_txn_time = new db_monitor_stat_txn_time(re);
		db_monitor_stat_txn_time c_stat_txn_time = tm_stat_txn_time.get(stat_txn_time.name());
		if (c_stat_txn_time != null) {
			c_stat_txn_time.update_stat(stat_txn_time);//更新统计数据
			stat_commit.get_stat_txn_times().add(c_stat_txn_time);
		} else {
			//新增统计数据
			stat_txn_time.time = date_tool.get_date_time(format, datetime);
			tm_stat_txn_time.put(stat_txn_time.name(), stat_txn_time);
			stat_commit.get_stat_txn_times().add(stat_txn_time);
		}
	}
	private void monitor_stat_txn_dp(run_env re, db_monitor_stat_txn_dp.cache stat_txn_dp_cache,String datetime) {
		db_monitor_stat_txn_dp stat_txn_dp = new db_monitor_stat_txn_dp(re);
		stat_txn_dp_cache.add_stat_txn_dp(stat_txn_dp);
	}

	private void monitor_stat_rule(run_env re, db_monitor_stat_rule.cache
			stat_rule_cache, String datetime,long curr_time) {
		Map<String, Map<String, db_monitor_stat_rule>> tm_stat_rule = stat_rule_cache
				.get_stat_rule_cache().get(datetime);
		Map<String, Long> tm_txn_trig = stat_rule_cache.get_txn_trig_number_cache().get(datetime);
		if (tm_stat_rule == null) {
			tm_stat_rule = new HashMap<String, Map<String, db_monitor_stat_rule>>();
			stat_rule_cache.get_stat_rule_cache().put(datetime, tm_stat_rule);
			tm_txn_trig = new HashMap<String, Long>();
			stat_rule_cache.get_txn_trig_number_cache().put(datetime, tm_txn_trig);
		}
		Map<String, db_monitor_stat_rule> rule_map = tm_stat_rule.get(re.get_txntype());
		long trig_number = tm_txn_trig.containsKey(re.get_txntype()) ? tm_txn_trig.get(re.get_txntype()) : 0;
		if (rule_map == null) {
			rule_map = new HashMap<String, db_monitor_stat_rule>();
			tm_stat_rule.put(re.get_txntype(), rule_map);
		}
		List<db_rule_hit> m_hit_rules = re.get_hit_rules();
		boolean null_hit_rule = (m_hit_rules == null || m_hit_rules.isEmpty());
		if (null_hit_rule) {
			//没有命中规则时，要记录规则触发数(交易执行数)
		    db_monitor_stat_rule db_stat_rule = new db_monitor_stat_rule(re, null);
            db_monitor_stat_rule c_stat_rule = rule_map.get(db_stat_rule.ruleid);
            if (c_stat_rule != null) {
                c_stat_rule.update_stat(db_stat_rule);
            } else {
                db_stat_rule.time = date_tool.get_date_time(format, datetime);
                rule_map.put(db_stat_rule.ruleid, db_stat_rule);
            }
		} else {
			//命中规则
			for (db_rule_hit hit : m_hit_rules) {
                db_monitor_stat_rule db_stat_rule = new db_monitor_stat_rule(re, hit);
                db_monitor_stat_rule c_stat_rule = rule_map.get(db_stat_rule.ruleid);
                if (c_stat_rule != null) {
                    c_stat_rule.update_stat(db_stat_rule);
                } else {
                    db_stat_rule.time = date_tool.get_date_time(format, datetime);
                    rule_map.put(db_stat_rule.ruleid, db_stat_rule);
                }
            }
		}
		// 触发此类型的交易所有规则，触发数+1
		trig_number++;
		Iterator<Entry<String, db_monitor_stat_rule>> it = rule_map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, db_monitor_stat_rule> en = it.next();
			db_monitor_stat_rule rule = en.getValue();
			if (null_hit_rule) {
				if (rule.ruleid.equals(db_monitor_stat_rule.NULL_CODE)) {
					rule.modtime = curr_time;
					rule.trig_number += 1;
					stat_commit.get_stat_rules().add(rule);
					break;
				}
			} else {
				if (!rule.ruleid.equals(db_monitor_stat_rule.NULL_CODE)) {
					rule.modtime = curr_time;
					rule.trig_number = trig_number;
					stat_commit.get_stat_rules().add(rule);
				}
			}
		}
		if (!null_hit_rule) {
			tm_txn_trig.put(re.get_txntype(), trig_number);
		}
	}
	
	public static String delete_date_time(String format, long txn_time, int cache_size) {
		long del_date_time = date_tool.get_before_time(format, txn_time, cache_size);
		String del_date = date_tool.get_date_time(format, (txn_time - del_date_time));
		return del_date;
	}
	
	public class run_stat_commit {
		private Set<db_monitor_stat_txn_area> stat_txn_areas;
		private Set<db_monitor_stat_txn_time> stat_txn_times;
		private List<db_monitor_stat_txn_dp> stat_txn_dps;
		private Set<db_monitor_stat_rule> stat_rules;
		
		public run_stat_commit() {
			stat_txn_areas = new HashSet<db_monitor_stat_txn_area>();
			stat_txn_times = new HashSet<db_monitor_stat_txn_time>();
			stat_txn_dps = new ArrayList<db_monitor_stat_txn_dp>();
			stat_rules = new HashSet<db_monitor_stat_rule>();
		}
		
		public void clear() {
			stat_txn_areas.clear();
			stat_txn_times.clear();
			stat_txn_dps.clear();
			stat_rules.clear();
		}
		
		public int size()
		{
			return stat_txn_areas.size() + stat_txn_times.size()
					+ stat_txn_dps.size() + stat_rules.size();
		}
		
		public Set<db_monitor_stat_txn_area> get_stat_txn_areas() {
			return stat_txn_areas;
		}
		
		public Set<db_monitor_stat_txn_time> get_stat_txn_times() {
			return stat_txn_times;
		}
		public List<db_monitor_stat_txn_dp> get_stat_txn_dps() {
			return stat_txn_dps;
		}
		
		public Set<db_monitor_stat_rule> get_stat_rules() {
			return stat_rules;
		}
	}
}