package cn.com.higinet.tms.engine.stat.client;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.tm_tool;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms.engine.stat.client.x_socket_pool.server;
/*
 * 注册与选择
 * 1、统计服务端自动完成注册
 * 2、客户端进行选举，所有客户端通过后，才可以确定可用
 * 20、选举采用记录锁定方式，锁超时10秒
 * 3、连续3次100毫秒不可连接时，或者连续三次服务出错时，标记为不可用
 * 
 * 数据库分表
 * 1、不再支持分表
 * 2、统计端支持上线连接数据库，下线断开数据库连接
 * 3、上线时，由客户端进行启动，下线时，由客户端发送命令
 * 
  * 
 * */
public class x_server_db
{
	static Logger log = LoggerFactory.getLogger(x_server_db.class);
	final String[] db_server;

	// int count_u, count_b;

	public x_server_db()
	{
		db_server = new String[64];
		// count_u = count_b = 0;
	}

	boolean load()
	{
		batch_stmt_jdbc stmt = new batch_stmt_jdbc(new data_source(),
				"select SERV,STATUS from TMS_RUN_CLUSTER where SERV!='lock'");
		try
		{
			stmt.query(new Object[] {}, new row_fetch()
			{
				public boolean fetch(ResultSet rs) throws SQLException
				{
					String serv = rs.getString(1);
					int status = rs.getInt(2);

					db_server[status] = serv;
					return true;
				}
			});

			// count_use();
			// count_bak();

			return true;
		}
		catch (SQLException e)
		{
			log.error("load from db error.", e);
		}
		finally
		{
			stmt.close();
		}

		return false;
	}

	String make_local_list(server[] local)
	{
		ArrayList<String> t = new ArrayList<String>(64);
		for (int i = 0; i < local.length; i++)
		{
			if (local[i] == null)
				continue;
			t.add(Integer.toString(i));
			t.add(local[i].toString());
		}

		Collections.sort(t);
		return t.toString();
	}

	String make_db_list()
	{
		ArrayList<String> t = new ArrayList<String>(64);
		for (int i = 0; i < db_server.length; i++)
		{
			if (db_server[i] == null)
				continue;
			t.add(Integer.toString(i));
			t.add(db_server[i]);
		}

		Collections.sort(t);
		return t.toString();
	}

	boolean match(server[] local)
	{
		return make_db_list().equals(make_local_list(local));
	}

	boolean clear_db()
	{
		batch_stmt_jdbc stmt = new batch_stmt_jdbc(new data_source(),
				"delete from tms_run_cluster where serv!='lock'");
		try
		{
			stmt.execute();
			stmt.flush();
			stmt.commit();
			return true;
		}
		catch (SQLException e)
		{
			stmt.rollback();
		}
		finally
		{
			stmt.close();
		}

		return false;
	}

	boolean save(final String local_serv_id)
	{
		clear_db();
		batch_stmt_jdbc stmt = new batch_stmt_jdbc(new data_source(),
				"insert into tms_run_cluster(serv,status,time,owner) values(?,?,?,?)", //
				new int[] { Types.VARCHAR, Types.BIGINT, Types.BIGINT, Types.VARCHAR });

		try
		{
			long time = tm_tool.lctm_ms();
			for (int i = 0; i < 64; i++)
			{
				if (db_server[i] == null)
					continue;

				stmt.update(new Object[] { db_server[i], i, time, local_serv_id });
			}
			stmt.flush();
			stmt.commit();
			return true;
		}
		catch (SQLException e)
		{
			stmt.rollback();
			log.error("save to db error.", e);
		}
		finally
		{
			stmt.close();
		}

		return false;
	}

	boolean from_local(server[] local)
	{
		for (int i = 0; i < local.length; i++)
		{
			if (local[i] == null)
				this.db_server[i] = null;
			else
				this.db_server[i] =local[i].toString(); 
		}

		return true;
	}
}
