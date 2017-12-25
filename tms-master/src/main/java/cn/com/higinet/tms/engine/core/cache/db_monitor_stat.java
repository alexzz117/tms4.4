package cn.com.higinet.tms.engine.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.higinet.tms.engine.comm.date_tool;
import cn.com.higinet.tms.engine.comm.tmsapp;
import cn.com.higinet.tms.engine.core.dao.row_in_db_impl;
import cn.com.higinet.tms.engine.run.run_env;

public class db_monitor_stat extends row_in_db_impl {
	
	public static final String NULL_CODE = "-1";
	public final static String COMBINATION_FIELD_STRING = "%s_%d";
	public static int port;
	public static String format;
	public static String ipaddr;
	public static int cache_size;
	public static long stat_time;
	
	static {
	    port = tmsapp.get_config("tms.server.port", 8000);
	    format = tmsapp.get_config("tms.monitor.stat.datePattern", "yyyyMMddHH");
	    ipaddr = tmsapp.get_serv_ip();
	    cache_size = tmsapp.get_config("tms.monitor.stat.cachesize", 1);
	    stat_time = System.currentTimeMillis() - date_tool.get_before_time(format, cache_size);
	}
	
	db_monitor_stat() {}
	
	public db_monitor_stat(run_env re) {
		this.ipport = String.format(COMBINATION_FIELD_STRING, tmsapp.get_serv_ip(), port);
	}
	
	public void assign_stat(ResultSet rs) throws SQLException {
	    this.ipport = rs.getString("IPPORT");
	    this.time = rs.getLong("TIME");
	}
	
	public db_monitor_stat update_stat(db_monitor_stat stat) {
		return this;
	}
	
	public String name() {
        return ipport;
    }
	
	public String ipport;//ip+端口
	public long time;//统计时间
}
