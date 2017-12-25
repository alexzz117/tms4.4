package cn.com.higinet.tms35.stat.serv;

import java.util.ArrayList;
import java.util.List;

import cn.com.higinet.tms35.comm.hash;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.concurrent.tms_worker_base;
import cn.com.higinet.tms35.core.concurrent.tms_worker_proxy;
import cn.com.higinet.tms35.core.dao.stat_value;
import cn.com.higinet.tms35.core.persist.Stat;
import cn.com.higinet.tms35.stat.stat_row;

public class stat_serv_dispatch_local extends tms_worker_proxy<stat_row>
{
	public volatile tms_worker_base<stat_value> inst_back;

	public stat_serv_dispatch_local(int bufSize, int threadCnt,
			Stat stat)
	{
		super("stat-eval-group", null, threadCnt);
		for (int i = 0; i < super.m_worker_pool.length; i++)
			super.m_worker_pool[i] = new stat_serv_worker_eval("stat-eval-" + i, bufSize, stat);
		
		back_inst();
	}
	
	private tms_worker_base<stat_value> back_inst()
	{
		int deque_size = tmsapp.get_config("tms.stat.back.dequesize", 4096);
		int thread_cnt = tmsapp.get_config("tms.stat.back.workerCount", 2);
		return inst_back = new stat_serv_dispatch_local_back(deque_size, thread_cnt);
	}

	final private int req_hash(String param)
	{
		return hash.clac(param,134217943) % 2087 % m_worker_pool.length;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean request(List<stat_row> req, int b, int e)
	{
		if (req == null || req.size() == 0)
			return false;

		List<stat_row>[] list = new ArrayList[m_worker_pool.length];
		for (int i = 0; i < m_worker_pool.length; i++)
			list[i] = new ArrayList<stat_row>(req.size());

		for (int i = b; i < e; i++)
		{
			stat_row row = req.get(i);
			list[req_hash(row.m_param)].add(row);
		}

		for (int i = 0; i < m_worker_pool.length; i++)
			m_worker_pool[i].request(list[i]);

		return true;
	}
	
	@Override
	public void start()
	{
		inst_back.start();
		super.start();
	}
	
	@Override
	public void shutdown(boolean abort)
	{
		super.shutdown(abort);
		inst_back.shutdown(abort);
	}
	
	@Override
	public void setup(String[] string) {
		inst_back.setup(string);
		super.setup(string);
	}

	public tms_worker_base<stat_value> inst_back() {
		return inst_back;
	}	
}