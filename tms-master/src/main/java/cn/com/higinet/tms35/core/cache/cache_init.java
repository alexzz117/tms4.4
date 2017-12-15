package cn.com.higinet.tms35.core.cache;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.core.dao.dao_seq;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public class cache_init
{
	static final Logger log = LoggerFactory.getLogger(cache_init.class);

	public static void check(DataSource ds)
	{
		init(new data_source(ds));
	}
	
	public static void init(data_source ds)
	{
		init(ds, false);
	}
	
	public static void init(data_source ds, boolean load_rosterval)
	{
		try
		{
			dao_seq.init(ds);
			txn.init(ds, load_rosterval);
		}
		catch (RuntimeException e)
		{
			log.error(e.getLocalizedMessage(),e);
			throw e;
		}
	}

	public static void init_for_stat(data_source ds)
	{
		try
		{
			db_cache.set(db_cache.read(ds,true));
		}
		catch (RuntimeException e)
		{
			log.error(e.getLocalizedMessage(),e);
			throw e;
		}
	}
}
