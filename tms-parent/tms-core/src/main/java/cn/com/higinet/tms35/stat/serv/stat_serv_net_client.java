package cn.com.higinet.tms35.stat.serv;

import java.util.ArrayList;
import java.util.List;

import cn.com.higinet.tms35.comm.delay_tool;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.concurrent.tms_worker_base;
import cn.com.higinet.tms35.core.concurrent.tms_worker_proxy;
import cn.com.higinet.tms35.core.concurrent.work_deque;
import cn.com.higinet.tms35.core.persist.Stat;
import cn.com.higinet.tms35.core.persist.impl.CacheStatImpl;
import cn.com.higinet.tms35.stat.stat_row;
import cn.com.higinet.tms35.stat.client.x_stat_client;

public class stat_serv_net_client extends tms_worker_proxy<stat_row> {
	private static final int m_batch_size = tmsapp.get_config("tms.stat.client.batchsize", 1024);

	private static final int m_batch_time = tmsapp.get_config("tms.stat.client.batchtime", 10);

	static private class stat_serv_client extends tms_worker_base<stat_row> {
		boolean is_query;

		private Stat stat;

		stat_serv_client(String name, work_deque<stat_row> request, boolean is_query) {
			super(name, request);
			this.is_query = is_query;
			stat = new CacheStatImpl();//由于没有bean管理，目前只能new，最好是通过注入 tms4.3 2017-11-06 王兴
		}

		List<stat_row> list = new ArrayList<stat_row>(1024);

		//N(4)个线程共享一个请求队列，系统可能有n个请求队列
		protected void run_once() {
			list.clear();
			this.drainTo(list, m_batch_size, m_batch_time);
			if (list.isEmpty())
				return;

			// for(stat_row sr:list)
			// sr.dec_query_batch();
			//			
			// if(true)
			// return;

			// List<Integer> times=new ArrayList<Integer>(10);
			if (is_query) {
				//tms4.3改版，如果是查询的话，直接走AS，不走统计服务
				try {
					stat.queryList(list);
				} catch (Exception e) {
					log.error("查询统计失败", e);
				}
			} else {
				int ret = x_stat_client.send_request(is_query, list, null);
				while (ret != 0 && this.m_running.get()) {
					log.error("没有可用的统计服务器[send_request return:" + ret + "],5秒后进行重试");
					delay_tool.delay(5000);
					ret = x_stat_client.send_request(is_query, list, null);
				}
			}
			// log.info("query use time:"+times);
		
		}
	}

	@Override
	public boolean request(List<stat_row> el, int b, int e) {
		int h = (el.hashCode() & 0X7FFFFFFF) % m_requests.size();
		return m_requests.get(h).offer(el, b, e);
	}

	List<work_deque<stat_row>> m_requests = new ArrayList<work_deque<stat_row>>(2);

	public stat_serv_net_client(int max_buf_size, int thread_cnt, final boolean is_query) {
		super("stat-client", max_buf_size, thread_cnt);
		work_deque<stat_row> n = null;

		//4个统计客户端线程共享一个队列
		for (int i = 0; i < (super.m_worker_pool.length + 3) / 4; i++)
			m_requests.add(new work_deque<stat_row>(max_buf_size));

		int i = 0;
		for (; i < super.m_worker_pool.length; i++) {
			n = m_requests.get(i % m_requests.size());
			super.m_worker_pool[i] = new stat_serv_client(//
					"stat-client-" + (is_query ? "query-" : "eval-") + i, //
					n, is_query);
		}
	}
}
