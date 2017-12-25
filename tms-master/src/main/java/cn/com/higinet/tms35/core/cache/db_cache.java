package cn.com.higinet.tms35.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public class db_cache {
	static Logger log = LoggerFactory.getLogger(db_cache.class);
	volatile static db_cache g_dc;
	volatile static db_roster.cache g_drc;
	volatile static long g_version = 0;

	public static long version() {
		return g_version;
	}

	public static db_cache get() {
		return g_dc;
	}

	public static db_cache get(long version) {
		if (g_version < version) {
			synchronized (db_cache.class) {
				if (g_version == version)
					return g_dc;

				g_dc = read(new data_source(), true);
				g_version = version;
			}
		}
		return g_dc;
	}

	public static void set(db_cache dc) {
		g_dc = dc;
		g_version = dc.tmd_ver();
	}

	public static void set_roster(db_roster.cache drc) {
		g_drc = drc;
	}

	public static db_cache read(data_source ds, boolean is_stat_app) {
		db_cache dc = new db_cache();
		dc.load(ds, is_stat_app);
		return dc;
	}

	private db_cache() {
	}

	private void load(data_source ds, boolean is_stat_app) {
		db_tmd_ver = get_trans_mode_version(ds);
		db_tab_cache = db_tab.cache.load(ds);
		db_tab_ref_cache = db_tab_ref.cache.load(ds, db_tab_cache);
		db_fd_cache = db_fd.cache.load(ds, db_tab_cache, db_tab_ref_cache);
		db_stat_cache = db_stat.cache.load(ds, db_tab_cache, db_fd_cache);
		db_fd_cache.reload_stat_fdname(db_tab_cache, db_stat_cache);
		db_dfp_application_cache = db_dfp_application.cache.load(ds);
		db_dfp_property_cache = db_dfp_property.cache.load(ds);
		db_dfp_app_properties_cache = db_dfp_app_properties.cache.load(ds,
				db_dfp_application_cache);
//		db_card_trade_quota_cache = db_bank_quota.cache.load(ds);
//		db_bank_user_switch_cache = db_bank_user_switch.cache.load(ds);
//		db_pay_quota_cache = db_pay_quota.cache.load(ds);

		//added by wujw 20160612
		db_region_cache = db_region.cache.load(ds);
		db_country_cache = db_country.cache.load(ds);
		
		if (!is_stat_app) {
			db_disposal_cache = db_disposal.cache.load(ds);
			db_rule_cache = db_rule.cache.load(ds, db_tab_cache,
					db_disposal_cache);
			db_rule_action_cache = db_rule_action.cache.load(ds, db_rule_cache);
			db_st_cache = db_strategy.cache.load(ds, db_tab_cache,
					db_rule_cache);
			db_st_re_cache = db_strategy_rule_eval.cache.load(ds, db_st_cache);
			db_cloud_cache = db_fwall_auth.cache.load(ds);
		}
	}

	private long get_trans_mode_version(data_source ds) {
		AtomicLong tmver = new AtomicLong(0);//交易模型配置版本号
		batch_stmt_jdbc stmt = null;
		try {
			stmt = new batch_stmt_jdbc(ds, 
					"select STARTVALUE from TMS_MGR_SYSPARAM where SYSPARAMNAME=?",
					new int[] { Types.VARCHAR });
			stmt.query(new Object[] { "transModelVersion" }, new row_fetch() {
				@Override
				public boolean fetch(ResultSet rs) throws SQLException {
					tmver.set(rs.getLong(1));
					return true;
				}
			});
		} catch (Exception e) {
			log.error("获取交易模型配置的版本号参数值异常.", e);
		} finally
		{
			stmt.close();
		}

		return tmver.get();
	}

	private long db_tmd_ver;
	private db_tab.cache db_tab_cache;
	private db_fwall_auth.cache db_cloud_cache;
	private db_tab_ref.cache db_tab_ref_cache;
	private db_fd.cache db_fd_cache;

	private db_strategy.cache db_st_cache;
	private db_strategy_rule_eval.cache db_st_re_cache;
	private db_disposal.cache db_disposal_cache;
	private db_rule.cache db_rule_cache;
	private db_rule_action.cache db_rule_action_cache;
	private db_stat.cache db_stat_cache;

	private db_dfp_application.cache db_dfp_application_cache;
	private db_dfp_property.cache db_dfp_property_cache;
	private db_dfp_app_properties.cache db_dfp_app_properties_cache;
//	private db_bank_quota.cache db_card_trade_quota_cache;
//	private db_bank_user_switch.cache db_bank_user_switch_cache;
//	private db_pay_quota.cache db_pay_quota_cache;
	
	private db_region.cache db_region_cache;
	private db_country.cache db_country_cache;

	public long tmd_ver() {
		return db_tmd_ver;
	}

	public db_strategy.cache strategy() {
		return db_st_cache;
	}

	public db_strategy_rule_eval.cache st_rule_eval() {
		return db_st_re_cache;
	}

	public db_rule.cache rule() {
		return db_rule_cache;
	}

	public db_rule_action.cache rule_action() {
		return db_rule_action_cache;
	}

	public db_stat.cache stat() {
		return db_stat_cache;
	}

	public db_tab.cache table() {
		return db_tab_cache;
	}

	public db_tab_ref.cache reftab() {
		return db_tab_ref_cache;
	}

	public db_fd.cache field() {
		return db_fd_cache;
	}

	public db_roster.cache roster() {
		return g_drc;
	}

	public db_dfp_application.cache application() {
		return db_dfp_application_cache;
	}

	public db_dfp_property.cache property() {
		return db_dfp_property_cache;
	}

	public db_dfp_app_properties.cache app_properties() {
		return db_dfp_app_properties_cache;
	}

	public db_fwall_auth.cache cloud_cache() {
		return db_cloud_cache;
	}

	public db_disposal.cache disposal() {
		return db_disposal_cache;
	}

//	public db_bank_quota.cache cardQuota() {
//		return db_card_trade_quota_cache;
//	}

//	public db_bank_user_switch.cache bank_user_switch() {
//		return db_bank_user_switch_cache;
//	}

//	public db_pay_quota.cache db_pay_quota() {
//		return db_pay_quota_cache;
//	}

	// 省份的缓存
	public db_region.cache region() {
		return db_region_cache;
	}

	// 省份的缓存
	public db_country.cache country() {
		return db_country_cache;
	}
}
