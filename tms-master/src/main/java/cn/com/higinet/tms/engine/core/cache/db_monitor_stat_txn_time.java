package cn.com.higinet.tms.engine.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import cn.com.higinet.tms.engine.comm.date_tool;
import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms.engine.run.run_env;

public class db_monitor_stat_txn_time extends db_monitor_stat {
	db_monitor_stat_txn_time() {}
	
	public db_monitor_stat_txn_time(run_env re) {
		super(re);
		time = Long.valueOf(date_tool.get_date_time(format, re.getStartTime()));
		hour = Integer.valueOf(date_tool.get_date_time("HH", re.getStartTime()));
		minute = Integer.valueOf(date_tool.get_date_time("mm", re.getStartTime()));
		txnid = re.get_txntype();
		channelid = re.get_chan_chancode();
		if (re.is_confirm())
		{
			risk_cfm_number = 1;
			risk_cfm_runtime_max = re.getUseTime();
			risk_cfm_runtime_min = risk_cfm_runtime_max;
			risk_cfm_runtime_avg = risk_cfm_runtime_max;
			risk_cfm_tpm[minute] = risk_cfm_number;
		}
		else
		{
			risk_eval_number = 1;
			risk_eval_runtime_max = re.getUseTime();
			risk_eval_runtime_min = risk_eval_runtime_max;
			risk_eval_runtime_avg = risk_eval_runtime_max;
			risk_eval_tpm[minute] = risk_eval_number;
		}
	}
	
	public String txnid;// 交易编码
	public String channelid;// 渠道代码
	public long risk_eval_number;// 风险评估调用次数
	public double risk_eval_runtime_avg;// 风险评估平均执行时间
	public long risk_eval_runtime_max;// 风险评估最大执行时间
	public long risk_eval_runtime_min;// 风险评估最小执行时间
	public Long[] risk_eval_tpm = new Long[60];// 风险评估每分钟的记录值，例如:1200:3,1201:76
	public long risk_cfm_number;// 风险确认调用次数
	public double risk_cfm_runtime_avg;// 风险确认平均执行时间
	public long risk_cfm_runtime_max;// 风险确认最大执行时间
	public long risk_cfm_runtime_min;// 风险确认最小执行时间
	public Long[] risk_cfm_tpm = new Long[60];// 风险确认每分钟的记录值
	
	private int hour;// 所在小时
	private int minute;// 所在分钟
	
	static public class cache
	{
		private Map<String, Map<String, db_monitor_stat_txn_time>> stat_txn_time_cache = new HashMap<String, Map<String, db_monitor_stat_txn_time>>();
		
		public static cache load(data_source ds)
		{
			cache c = new cache();
			c.init(ds);
			return c;
		}
		
		public void init(data_source ds)
		{
			String sql = "select IPPORT, CHANNELID, TXNID, TIME, RISK_EVAL_NUMBER, RISK_EVAL_RUNTIME_AVG, RISK_EVAL_RUNTIME_MAX, "
					+ "RISK_EVAL_RUNTIME_MIN, RISK_EVAL_TPM, RISK_CFM_NUMBER, RISK_CFM_RUNTIME_AVG, RISK_CFM_RUNTIME_MAX, "
					+ "RISK_CFM_RUNTIME_MIN, RISK_CFM_TPM from TMS_MONITOR_TXN_TIME_STAT where IPPORT = ? and TIME > ?";
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {
					java.sql.Types.VARCHAR, java.sql.Types.VARCHAR });
			try
			{
				stmt.query(new Object[] { String.format(COMBINATION_FIELD_STRING,
						ipaddr, port), stat_time }, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_monitor_stat_txn_time txn = new db_monitor_stat_txn_time();
						txn.assign_stat(rs);
						txn.set_indb(true);
						txn.hour = Integer.valueOf(date_tool.get_date_time("HH", txn.time));
						String date = date_tool.get_date_time(format, txn.time);
						if (stat_txn_time_cache.containsKey(date)) {
							stat_txn_time_cache.get(date).put(txn.name(), txn);
						} else {
							Map<String, db_monitor_stat_txn_time> cacheMap = new HashMap<String, db_monitor_stat_txn_time>();
							cacheMap.put(txn.name(), txn);
							stat_txn_time_cache.put(date, cacheMap);
						}
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new tms_exception("load db_monitor_stat_txn_area.cache error.");
			}
			finally
			{
				stmt.close();
			}
		}
		
		public Map<String, Map<String, db_monitor_stat_txn_time>> get_stat_txn_time_cache() {
			return stat_txn_time_cache;
		}
	}
	
	public void set_risk_eval_tpm(String risk_eval_tpm_str)
	{
		risk_eval_tpm = decode_tpm(risk_eval_tpm_str);
	}
	
	public void set_risk_cfm_tpm(String risk_cfm_tpm_str)
	{
		risk_cfm_tpm = decode_tpm(risk_cfm_tpm_str);
	}
	
	public String risk_eval_tpm_str()
	{
		return encode_tpm(risk_eval_tpm);
	}
	
	public String risk_cfm_tpm_str()
	{
		return encode_tpm(risk_cfm_tpm);
	}
	
	private String encode_tpm(Long[] tpms)
	{
		if (tpms == null || tpms.length == 0)
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0, len = tpms.length; i < len; i++)
		{
			Long tpm = tpms[i];
			if (tpm == null)
				continue;
			if (sb.length() > 0)
				sb.append(",");
			sb.append((hour < 10 ? "0" : "") + hour + (i < 10 ? "0" : "") + i);
			sb.append(":");
			sb.append(tpm);
		}
		return sb.toString();
	}
	
	private Long[] decode_tpm(String tpm)
	{
		Long[] tpms = new Long[60];
		if (!str_tool.is_empty(tpm)) {
			String[] _tpm = tpm.split("\\,");
			for (int i = 0, len = tpms.length, _len
					= _tpm.length; i < len && i < _len; i++) {
				String _tmp_ = _tpm[i];
				if (str_tool.is_empty(_tmp_))
					continue;
				String[] t = _tmp_.split("\\:");
				tpms[Integer.valueOf(t[0].substring(2))] = Long.valueOf(t[1]);
			}
		}
		return tpms;
	}
	
	@Override
    public void assign_stat(ResultSet rs) throws SQLException {
		super.assign_stat(rs);
        this.txnid = rs.getString("TXNID");
        this.channelid = rs.getString("CHANNELID");
        this.risk_eval_number = rs.getLong("RISK_EVAL_NUMBER");
        this.risk_eval_runtime_max = rs.getLong("RISK_EVAL_RUNTIME_MAX");
        this.risk_eval_runtime_min = rs.getLong("RISK_EVAL_RUNTIME_MIN");
        this.risk_eval_runtime_avg = rs.getDouble("RISK_EVAL_RUNTIME_AVG");
        this.set_risk_eval_tpm(rs.getString("RISK_EVAL_TPM"));
        this.risk_cfm_number = rs.getLong("RISK_CFM_NUMBER");
        this.risk_cfm_runtime_max = rs.getLong("RISK_CFM_RUNTIME_MAX");
        this.risk_cfm_runtime_min = rs.getLong("RISK_CFM_RUNTIME_MIN");
        this.risk_cfm_runtime_avg = rs.getDouble("RISK_CFM_RUNTIME_AVG");
        this.set_risk_cfm_tpm(rs.getString("RISK_CFM_TPM"));
	}
	
	@Override
	public db_monitor_stat update_stat(db_monitor_stat stat) {
		db_monitor_stat_txn_time stat_txn = (db_monitor_stat_txn_time) stat;
		super.update_stat(stat_txn);
		int minute = stat_txn.minute;
		if (risk_eval_number + stat_txn.risk_eval_number <= 0) {
			risk_eval_runtime_avg = 0;
		} else {
			risk_eval_runtime_avg = ((risk_eval_runtime_avg * risk_eval_number) + 
					(stat_txn.risk_eval_runtime_avg * stat_txn.risk_eval_number)) / 
						(risk_eval_number += stat_txn.risk_eval_number);
		}
		if (risk_eval_runtime_max < stat_txn.risk_eval_runtime_max) {
			risk_eval_runtime_max = stat_txn.risk_eval_runtime_max;
		}
		if (risk_eval_runtime_min > stat_txn.risk_eval_runtime_min) {
			risk_eval_runtime_min = stat_txn.risk_eval_runtime_min;
		}
		if (risk_cfm_number + stat_txn.risk_cfm_number <= 0) {
			risk_cfm_runtime_avg = 0;
		} else {
			risk_cfm_runtime_avg = ((risk_cfm_runtime_avg * risk_cfm_number) + 
					(stat_txn.risk_cfm_runtime_avg * stat_txn.risk_cfm_number)) / 
						(risk_cfm_number += stat_txn.risk_cfm_number);
		}
		if (risk_cfm_runtime_max < stat_txn.risk_cfm_runtime_max) {
			risk_cfm_runtime_max = stat_txn.risk_cfm_runtime_max;
		}
		if (risk_cfm_runtime_min > stat_txn.risk_cfm_runtime_min) {
			risk_cfm_runtime_min = stat_txn.risk_cfm_runtime_min;
		}
		Long eval_tpm = this.risk_eval_tpm[minute];
		Long cfm_tpm = this.risk_cfm_tpm[minute];
		if (eval_tpm == null) {
			eval_tpm = stat_txn.risk_eval_number;
		} else {
			eval_tpm += stat_txn.risk_eval_number;
		}
		if (cfm_tpm == null) {
			cfm_tpm = stat_txn.risk_cfm_number;
		} else {
			cfm_tpm += stat_txn.risk_cfm_number;
		}
		this.risk_eval_tpm[minute] = eval_tpm;
		this.risk_cfm_tpm[minute] = cfm_tpm;
		return this;
	}
	
	@Override
	public String name() {
		return super.name() + "_" + channelid + "_" + txnid;
	}
}