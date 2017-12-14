package cn.com.higinet.tms.core.common;

import java.util.Date;


public class tm_tool
{
	static long tm_offset = 0;
	static long local_tm_base;

	private static long local_time_base()
	{
		long tm_base = new Date().getTime() + 10;

		long now = tm_base - 10;

		while (tm_base > now)
		{
			// System.out.println(now);
			now = new Date().getTime();
		}

		return now * 1000 - System.nanoTime() / 1000;
	}

	public static void init()
	{
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		local_tm_base = local_time_base();
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		tm_offset = time_offset();
		long l_us = lctm_us();
		long r_us = dbtm_us();
		System.out.println("local time:" + date_tool.format(new Date(l_us / 1000))
				+ (String.format("%03d", l_us % 1000)));
		System.out.println("dbsys time:" + date_tool.format(new Date(r_us / 1000))
				+ (String.format("%03d", r_us % 1000)));

		System.out.println("time offset us(local-db):" + (new Date().getTime() * 1000 - dbtm_us()));
	}

	static public long time_offset()
	{
		return lctm_us() - System.nanoTime() / 1000;
	}
	
/*
	static String sql = "SELECT TO_CHAR(SYSTIMESTAMP,'YYYY-MM-DD HH24:MI:SS.FF6') FROM DUAL";
	static public long time_offset()
	{
		clock c = new clock();
		int cc = 6;
		long ret = 0;
		long max = Long.MIN_VALUE, min = Long.MAX_VALUE;
		final StringBuffer _sb = new StringBuffer();
		c.pin("start sync");
		batch_stmt_jdbc stmt = null;
		try
		{
			data_source ds = new data_source((DataSource) bean. get("tmsDataSource"));
			ds.get_conn();
			stmt = new batch_stmt_jdbc(ds, sql, null);
			for (int i = 0; i < cc; i++)
			{
				long pre = 0, post = 0;
				_sb.setLength(0);
				pre = System.nanoTime() / 1000;
				stmt.query(new Object[] {}, new row_fetch()
				{
					StringBuffer sb = _sb;

					public void fetch(ResultSet rs) throws SQLException
					{
						sb.append(rs.getString(1));
					}
				});
				post = System.nanoTime() / 1000;
				c.pin("sync" + i);
				if (i == 0)
					continue;

				String s = _sb.toString();
				Date d = date_tool.parse(s.substring(0, s.length() - 3));
				long dba = d.getTime() * 1000 + Long.parseLong(s.substring(s.length() - 3));
				// System.out.println(date_tool.format(new Date(dba/1000)));
				long db = (pre + post) >>> 1;
				long off = (dba - db);
				if (min > off)
					min = off;
				if (max < off)
					max = off;
				ret += off;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			if (tm_offset == 0)
			{
				System.out.println("严重错误:数据库不可用，开始使用本地时间代替数据库服务器时间");
				return lctm_us() - System.nanoTime() / 1000;
			}
			return tm_offset;
		}
		finally
		{
			if (stmt != null)
				stmt.close();
		}

		return (ret - min - max) / (cc - 3);
	}*/

	final static public long lctm_us()
	{
		return (local_tm_base + System.nanoTime() / 1000);
	}

	final static public long lctm_ms()
	{
		return lctm_us() / 1000;
	}

	final static public long lctm_sec()
	{
		return lctm_ms() / 1000;
	}

	final static public long lctm_min()
	{
		return lctm_sec() / 60;
	}

	final static public long dbtm_us()
	{
		return (tm_offset + System.nanoTime() / 1000);
	}

	final static public long dbtm_ms()
	{
		return dbtm_us() / 1000;
	}

	final static public int dbtm_sec()
	{
		return (int) (dbtm_ms() / 1000);
	}

	final static public int dbtm_min()
	{
		return (int) (dbtm_sec() / 60);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		init();
		System.out.println();
	}

}
