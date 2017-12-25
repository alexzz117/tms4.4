package cn.com.higinet.tms35.stat.serv;

import java.util.ArrayList;
import java.util.List;

import cn.com.higinet.tms35.comm.hash;
import cn.com.higinet.tms35.core.concurrent.tms_worker_proxy;
import cn.com.higinet.tms35.core.dao.stat_value;

public class stat_serv_dispatch_local_back extends tms_worker_proxy<stat_value>
{
	public stat_serv_dispatch_local_back(int bufSize, int threadCnt)
	{
		super("stat-back-group", null, threadCnt);
		for (int i = 0; i < super.m_worker_pool.length; i++)
			super.m_worker_pool[i] = new stat_serv_worker_back("stat-back-" + i, bufSize);
	}

	final private int req_hash(String param)
	{
		return hash.clac(param,134217943) % 2087 % m_worker_pool.length;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean request(List<stat_value> req, int b, int e)
	{
		if (req == null || req.size() == 0)
			return false;

		List<stat_value>[] list = new ArrayList[m_worker_pool.length];
		for (int i = 0; i < m_worker_pool.length; i++)
			list[i] = new ArrayList<stat_value>(req.size());

		for (int i = b; i < e; i++)
		{
			stat_value sv = req.get(i);
			list[req_hash(sv.m_param)].add(sv);
		}

		for (int i = 0; i < m_worker_pool.length; i++)
			m_worker_pool[i].request(list[i]);

		return true;
	}
}
