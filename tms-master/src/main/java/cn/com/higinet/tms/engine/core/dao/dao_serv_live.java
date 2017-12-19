package cn.com.higinet.tms.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.sql_reconnect_exception;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms.manager.common.ApplicationContextUtil;
import cn.com.higinet.tms.manager.dao.SqlMap;

public class dao_serv_live
{
	static final Logger log = LoggerFactory.getLogger(dao_serv_live.class);

	public static class db_server
	{
		public db_server(String ipaddr, int port, int servtype, int pingdelay)
		{
			this.ipaddr = ipaddr;
			this.port = port;
			//this.serverid = 0;
			this.servtype = servtype;
			this.pingdelay = pingdelay;
		}

		String ipaddr;
		int port, servtype, pingdelay;
	}

	batch_stmt_jdbc insert;
	batch_stmt_jdbc update;
	batch_stmt_jdbc select;
	
	static String insertSql = "insert into TMS_RUN_SERVER (IPADDR, PORT, SERVERID"
			+ ", SERVTYPE, ACTIVETIME, PINGDELAY) ";
	static String selectSql = "select count(1) from TMS_RUN_SERVER where IPADDR=? and PORT=?";
	
	public db_server serv_info;

	public dao_serv_live(db_server serv_info)
	{
		this.serv_info = serv_info;
		data_source ds = new data_source((DataSource) ApplicationContextUtil.getBean("tmsDataSource"));
		insert = new batch_stmt_jdbc(ds, insertSql + ApplicationContextUtil.getBean(SqlMap.class).getSql("tms.dao.merge.serv_live_insert")
				, new int[] { Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.INTEGER });
		
		update = new batch_stmt_jdbc(ds, ApplicationContextUtil.getBean(SqlMap.class).getSql("tms.dao.merge.serv_live_update")
				, new int[] { Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.INTEGER });
		
		select = new batch_stmt_jdbc(ds, selectSql, new int[] { Types.VARCHAR, Types.INTEGER });
	}

	public boolean ping()
	{
		do
		{
			try
			{
				final List<Integer> servs = new ArrayList<Integer>();
				select.query(new Object[] { serv_info.ipaddr, serv_info.port }, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						servs.add(rs.getInt(1));
						return true;
					}
				});
				batch_stmt_jdbc stmt = null;
				if(servs.size() > 0 && servs.get(0) > 0)
				{
					stmt = update();
				}
				else
				{
					stmt = insert();
				}
				stmt.flush();
				stmt.commit();
				break;
			}
			catch (sql_reconnect_exception e)
			{
				continue;
			}
			catch (SQLException e)
			{
				log.error("建立服务器ping记录失败。", e);
				return false;
			}
		} while (true);

		return true;
	}
	
	private batch_stmt_jdbc insert() throws SQLException
	{
		insert.update(new Object[] { serv_info.ipaddr, serv_info.port, 
				serv_info.servtype, serv_info.pingdelay });
		return insert;
	}
	
	private batch_stmt_jdbc update() throws SQLException
	{
		update.update(new Object[] { serv_info.servtype, serv_info.pingdelay, 
				serv_info.ipaddr, serv_info.port });
		return update;
	}

	public void close()
	{
		insert.close();
		update.close();
		select.close();
	}
}
