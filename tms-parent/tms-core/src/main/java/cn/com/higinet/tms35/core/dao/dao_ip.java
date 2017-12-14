package cn.com.higinet.tms35.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public class dao_ip {
	static Logger log = LoggerFactory.getLogger(dao_ip.class);
	static volatile dao_ip g_ip;
	String iplog_id;
	int is_suffix;
	
	private dao_ip(){}
	
	public static dao_ip Instance(){
		return Instance(new data_source());
	}
	
	public static dao_ip Instance(data_source ds){
		if (g_ip != null)
			return g_ip;
		synchronized (dao_ip.class)
		{
			if (g_ip == null)
				g_ip = load_from_db(ds);
		}
		return g_ip;
	}
	
	public static void reset()
	{
		g_ip = null;
	}
	
	public static dao_ip load_from_db(data_source ds){
		batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds,
				"select IPLOG_ID,IS_SUFFIX from TMS_MGR_IPLOG where IPLOG_ID=(select max(IPLOG_ID) from TMS_MGR_IPLOG where OPERATE_RESULT=1)", null);
		ResultSet res = null;
		try
		{
			res = stmt.query(null);
			dao_ip ret = new dao_ip();
			if (res.next())
			{
				ret.iplog_id = res.getString(1);
				ret.is_suffix = res.getInt(2);
			}
			return ret;
		}
		catch (SQLException e)
		{
			log.error(null, e);
		}
		finally
		{
			try
			{
				if (res != null)
					res.close();
			}
			catch (SQLException e)
			{
				log.error(null, e);
			}
			stmt.close();
		}
		return null;
	}
	
	public String curr_name()
	{
		return g_ip.is_suffix == 1 ? "%s_N" : "%s";
	}
	
	public String oper_name()
	{
		return g_ip.is_suffix == 1 ? "%s" : "%s_N";
	}
	
	public String curr_name(String tab_name)
	{
		if(tab_name == null || tab_name.length() == 0){
			return tab_name;
		}
		return String.format(this.curr_name(), tab_name);
	}
	
	public String oper_name(String tab_name)
	{
		if(tab_name == null || tab_name.length() == 0){
			return tab_name;
		}
		return String.format(this.oper_name(), tab_name);
	}
}
