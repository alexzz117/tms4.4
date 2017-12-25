package cn.com.higinet.tms.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.comm.tmsapp;
import cn.com.higinet.tms.engine.core.cache.db_userpattern;
import cn.com.higinet.tms.engine.core.concurrent.cache.tm_cache;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;

public class dao_userpattern extends batch_stmt_jdbc_obj<db_userpattern> {

	static private tm_cache<String, db_userpattern> m_cache = null;
	static
	{
		// 去除C代码层的缓存，只保留google缓存
		int cache_size = tmsapp.get_config("tms.pattern.cachesize", 0);
		if (cache_size > 0)
		{
			m_cache = new tm_cache<String, db_userpattern>(cache_size);
		}
	}
	
	static final String sql = "SELECT USERID, USER_PATTERN, USER_PATTERN_1" +
			", USER_PATTERN_C FROM TMS_COM_USERPATTERN WHERE USERID = ?";
	
	public dao_userpattern(data_source ds) {
		super(ds, sql, new int[] { Types.VARCHAR });
	}
	
	@Override
	public db_userpattern read(Object... p) throws SQLException
	{
		return read((String) p[0]);
	}

	public db_userpattern read(String userid) throws SQLException {
		db_userpattern up = null;
		if (m_cache != null)
		{
			up = m_cache.get(userid);
			if (up != null)
			{
				return up;
			}
		}
		up = read_userpattern(userid);
		db_userpattern up2 = null;
		if (m_cache != null)
		{
			if (up != null)
			{
				up2 = m_cache.putIfAbsent(userid, up);
			}
		}
		return up2 != null ? up2 : up;
	}
	
	db_userpattern read_userpattern(String userid) throws SQLException {
		final db_userpattern up = new db_userpattern();
		query(new Object[] { userid }, new row_fetch() {
			public boolean fetch(ResultSet rs) throws SQLException {
				int c = 0;
				up.userid = rs.getString(++c);
				String user_pattern = rs.getString(++c);
				String user_pattern_1 = rs.getString(++c);
				String user_pattern_c = rs.getString(++c);
				up.pattern_s = (str_tool.is_empty(user_pattern) ? "" : user_pattern)
						+ (str_tool.is_empty(user_pattern_1) ? "" : user_pattern_1)
						+ (str_tool.is_empty(user_pattern_c) ? "" : user_pattern_c);
				up.pattern_m = db_userpattern.userPattern2Map(up.pattern_s);
				return true;
			}
		});
		return up;
	}
	
	public static void remove_cache(String userid)
	{
		if (m_cache != null)
		{
			m_cache.remove(userid);
		}
	}

	@Override
	public String name() {
		return "dao_userpattern";
	}

	@Override
	public Object[] toArray(db_userpattern e) {
		return new Object[] { e.userid };
	}
}