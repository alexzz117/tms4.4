package cn.com.higinet.tms.engine.stat.serv;

import cn.com.higinet.tms.engine.core.concurrent.tms_worker_proxy;
import cn.com.higinet.tms.engine.core.persist.Stat;
import cn.com.higinet.tms.engine.stat.stat_row;

public class stat_serv_dispatch_local_query extends tms_worker_proxy<stat_row>
{
	public stat_serv_dispatch_local_query(int bufSize, int threadCnt,
			Stat stat)
	{
		super("stat-query-group", bufSize,threadCnt);
		for (int i = 0; i < m_worker_pool.length; i++)
			m_worker_pool[i] = new stat_serv_worker_query("stat-query-" + i, m_request, stat);
	}
}
