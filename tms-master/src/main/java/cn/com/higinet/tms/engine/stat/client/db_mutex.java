package cn.com.higinet.tms.engine.stat.client;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.evalTest;
import cn.com.higinet.tms.engine.comm.clock;
import cn.com.higinet.tms.engine.comm.delay_tool;
import cn.com.higinet.tms.engine.comm.tm_tool;
import cn.com.higinet.tms.engine.comm.tmsapp;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

public class db_mutex
{
	final static Logger log = LoggerFactory.getLogger(db_mutex.class);
	final int max_lock_time = 10000;
	final String local_serv_id = tmsapp.get_serv_id2(false);

	boolean locked = false;
	long lock_time = 0;
	long status = 0;

	static class long_wapper
	{
		long l;

		public long_wapper()
		{
			l = 0;
		}

		void set_l(long l)
		{
			this.l = l;
		}
	}

	private void reset()
	{
		locked = false;
		lock_time = 0;
		status = 0;
	}

	synchronized public boolean lock(long tmout)
	{
		final String sql_update = "update TMS_RUN_CLUSTER " + //
				"set STATUS=-status,TIME=?,OWNER=? " + //
				"where SERV='lock' and (STATUS>0 or TIME+" + max_lock_time + "<? )"; //尝试锁定，未锁定或者锁超时

		final String sql_select = "select STATUS " + //
				"from TMS_RUN_CLUSTER " + //
				"where SERV='lock' and TIME=? and OWNER=? and STATUS<0";//查询是否被当前服务锁定

		data_source ds = new data_source();
		batch_stmt_jdbc update = new batch_stmt_jdbc(ds, sql_update, new int[] { Types.BIGINT,
				Types.VARCHAR, Types.BIGINT });
		
		
		batch_stmt_jdbc select = new batch_stmt_jdbc(ds, sql_select, new int[] { Types.BIGINT,
				Types.VARCHAR });

		try
		{
			clock c = new clock();
			for (; c.left((int) tmout) > 0;)
			{
				lock_time = tm_tool.lctm_ms();
				update.execute(new Object[] { lock_time, local_serv_id, lock_time });
				update.flush();
				update.commit();

				final long_wapper wap = new long_wapper();

				select.query(new Object[] { lock_time, local_serv_id }, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						wap.set_l(rs.getLong(1));
						return true;
					}
				});

				if (wap.l < 0)
				{
					status = wap.l;
					return locked = true;
				}

				delay_tool.delay(100);
			}
		}
		catch (SQLException e)
		{
			update.rollback();
			e.printStackTrace();
		}
		finally
		{
			update.close();
			select.close();
		}

		reset();
		return false;
	}

	public boolean lock()
	{
		return lock(Integer.MAX_VALUE);
	}

	synchronized public void unlock(long version)
	{
		if (!locked)
			return;

		if (tm_tool.lctm_ms() - lock_time > max_lock_time)
		{
			reset();
			return;
		}

		final String sql_update = "update TMS_RUN_CLUSTER set STATUS=? where SERV='lock' and STATUS<0 and TIME=? and OWNER=?";
		batch_stmt_jdbc update = new batch_stmt_jdbc(new data_source(), sql_update, new int[] {
				Types.BIGINT, Types.BIGINT, Types.VARCHAR });
		try
		{
			update.execute(new Object[] { version, lock_time, local_serv_id });
			update.flush();
			update.commit();
		}
		catch (SQLException e)
		{
			update.rollback();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			reset();
			update.close();
		}
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args)
	{
		evalTest.init();
		db_mutex m = new db_mutex();
		for (;;)
		{
			if (m.lock(2000))
			{
				delay_tool.delay(1000);
				log.info(Thread.currentThread().getId() + " locked.");
				m.unlock(-m.status + 1);
			}
			else
			{
				System.out.println(Thread.currentThread().getId() + "..........");
			}
			delay_tool.delay(1000);
		}
	}
}
