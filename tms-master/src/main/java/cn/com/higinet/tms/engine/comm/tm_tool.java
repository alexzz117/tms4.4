package cn.com.higinet.tms.engine.comm;


public class tm_tool
{
	// static Logger log = Logger.getLogger(tm_tool.class);
	// volatile static long tm_offset = 0;
	// volatile static long local_tm_base = 0;
	// volatile static long last_sync_time = 0;
	//
	// static
	// {
	// sync_local_time();
	// }
	//
	// public static void sync_local_time()
	// {
	// long cur_time = System.currentTimeMillis();
	// if (cur_time - last_sync_time < 3600 * 1000)
	// return;
	//
	// synchronized (tm_tool.class)
	// {
	// if (cur_time - last_sync_time < 3600 * 1000)
	// return;
	//
	// Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	// local_tm_base = local_time_base();
	// last_sync_time = cur_time;
	// Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
	// }
	// }
	//
	// private static long local_time_base()
	// {
	// long tm_base = System.currentTimeMillis() + 2;
	// long now = tm_base - 2;
	// while (tm_base > now)
	// now = System.currentTimeMillis();
	//
	// return now * 1000 - System.nanoTime() / 1000;
	// }
	//
	// public static void init()
	// {
	// // tm_offset = time_offset();
	// // long l_us = lctm_ms();
	// // long r_us = dbtm_us();
	// // log.info("local time:" + date_tool.format(new Date(l_us / 1000))
	// // + (String.format("%03d", l_us % 1000)));
	// // log.info("dbsys time:" + date_tool.format(new Date(r_us / 1000))
	// // + (String.format("%03d", r_us % 1000)));
	// // log.info("time offset us(local-db):" + (System.currentTimeMillis() *
	// 1000 - dbtm_us()));
	// }

	// static String sql =
	// "SELECT TO_CHAR(SYSTIMESTAMP,'YYYY-MM-DD HH24:MI:SS.FF6') FROM DUAL";
	//
	// static public long time_offset()
	// {
	// int cc = 6;
	// long ret = 0;
	// long max = Long.MIN_VALUE, min = Long.MAX_VALUE;
	// final StringBuffer _sb = new StringBuffer();
	// batch_stmt_jdbc stmt = null;
	// try
	// {
	// data_source ds = new data_source((DataSource) bean.get("tmsDataSource"));
	// ds.get_conn();
	// stmt = new batch_stmt_jdbc(ds, sql, null);
	// for (int i = 0; i < cc; i++)
	// {
	// long pre = 0, post = 0;
	// _sb.setLength(0);
	// pre = System.nanoTime() / 1000;
	// stmt.query(new Object[] {}, new row_fetch()
	// {
	// StringBuffer sb = _sb;
	//
	// public void fetch(ResultSet rs) throws SQLException
	// {
	// sb.append(rs.getString(1));
	// }
	// });
	// post = System.nanoTime() / 1000;
	// if (i == 0)
	// continue;
	//
	// String s = _sb.toString();
	// Date d = date_tool.parse(s.substring(0, s.length() - 3));
	// long dba = d.getTime() * 1000 + Long.parseLong(s.substring(s.length() -
	// 3));
	//
	// long db = (pre + post) >>> 1;
	// long off = (dba - db);
	// if (min > off)
	// min = off;
	// if (max < off)
	// max = off;
	// ret += off;
	// }
	// }
	// catch (SQLException e)
	// {
	// if (tm_offset == 0)
	// {
	// log.error("严重错误:数据库不可用，开始使用本地时间代替数据库服务器时间");
	// return lctm_us() - System.nanoTime() / 1000;
	// }
	// log.error(e);
	// return tm_offset;
	// }
	// finally
	// {
	// if (stmt != null)
	// stmt.close();
	// }
	//
	// return (ret - min - max) / (cc - 3);
	// }

	final static public long lctm_ms()
	{
		return System.currentTimeMillis();
	}

	final static public long lctm_sec()
	{
		return lctm_ms() / 1000;
	}

	final static public long lctm_min()
	{
		return lctm_ms() / 1000 / 60;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		clock c = new clock();

		while (true)
		{
			c.pin("" + (System.currentTimeMillis() - System.nanoTime() / 1000000));
			delay_tool.delay(1000);
		}

	}

}
