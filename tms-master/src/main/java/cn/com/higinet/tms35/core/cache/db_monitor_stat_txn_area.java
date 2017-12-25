package cn.com.higinet.tms35.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import cn.com.higinet.tms35.comm.date_tool;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.run.run_env;

public class db_monitor_stat_txn_area extends db_monitor_stat_txn {
	
	public db_monitor_stat_txn_area() {}
	
	public db_monitor_stat_txn_area(run_env re) {
		super(re);
		countrycode = (String) re.get_fd_value(re.field_cache().INDEX_COUNTRY);
		regioncode = (String) re.get_fd_value(re.field_cache().INDEX_REGION);
		citycode = (String) re.get_fd_value(re.field_cache().INDEX_CITY);
		if (str_tool.is_empty(countrycode)) {
			countrycode = NULL_CODE;
		}
		if (str_tool.is_empty(regioncode)) {
			regioncode = NULL_CODE;
		}
		if (str_tool.is_empty(citycode)) {
			citycode = NULL_CODE;
		}
	}

	public String countrycode;// 国家代码
	public String regioncode;// 省份代码
	public String citycode;// 城市代码
	public long modtime;// 修改时间
	
	static public class cache
	{
		private Map<String, Map<String, db_monitor_stat_txn_area>> stat_txn_area_cache = new HashMap<String, Map<String, db_monitor_stat_txn_area>>();
		
		public static cache load(data_source ds)
		{
			cache c = new cache();
			c.init(ds);
			return c;
		}
		
		public void init(data_source ds)
		{
			String sql = "select IPPORT, COUNTRYCODE, REGIONCODE, CITYCODE, CHANNELID, TXNID, TIME, TXN_NUMBER, "
					+ "TXN_RUNTIME_MAX, TXN_RUNTIME_MIN, TXN_RUNTIME_AVG, MR_NUMBER, MRNOT_NUMBER, MR_AP_NUMBER, MR_APNOT_NUMBER, "
					+ "MODEL_ID, STATDATE, DISPOSAL, DISPOSAL_NUM from TMS_MONITOR_TXN_AREA_STAT where IPPORT = ? and TIME > ?";
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {
					java.sql.Types.VARCHAR, java.sql.Types.VARCHAR });
			try
			{
				stmt.query(new Object[] { String.format(COMBINATION_FIELD_STRING,
						ipaddr, port), stat_time }, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_monitor_stat_txn_area txn = new db_monitor_stat_txn_area();
						txn.assign_stat(rs);
						txn.set_indb(true);
						String date = date_tool.get_date_time(format, txn.time);
						if (stat_txn_area_cache.containsKey(date)) {
							stat_txn_area_cache.get(date).put(txn.name(), txn);
						} else {
							Map<String, db_monitor_stat_txn_area> cacheMap = new HashMap<String, db_monitor_stat_txn_area>();
							cacheMap.put(txn.name(), txn);
							stat_txn_area_cache.put(date, cacheMap);
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
		
		public Map<String, Map<String, db_monitor_stat_txn_area>> get_stat_txn_area_cache() {
			return stat_txn_area_cache;
		}
	}
	
	@Override
    public void assign_stat(ResultSet rs) throws SQLException {
		super.assign_stat(rs);
        this.countrycode = rs.getString("COUNTRYCODE");
        this.regioncode = rs.getString("REGIONCODE");
        this.citycode = rs.getString("CITYCODE");
	}
	
	@Override
	public db_monitor_stat update_stat(db_monitor_stat stat) {
		db_monitor_stat_txn_area stat_txn_area = (db_monitor_stat_txn_area) stat;
		super.update_stat(stat_txn_area);
		return this;
	}
	
	@Override
	public String name() {
		return super.name() + "_" + countrycode + "_" + regioncode + "_" + citycode;
	}
}
