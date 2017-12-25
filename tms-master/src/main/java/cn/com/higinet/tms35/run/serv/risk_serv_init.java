package cn.com.higinet.tms35.run.serv;

import cn.com.higinet.tms35.comm.delay_tool;
import cn.com.higinet.tms35.comm.hash;
import cn.com.higinet.tms35.comm.tm_tool;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.comm.translog_worker;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.concurrent.cond;
import cn.com.higinet.tms35.core.concurrent.counter;
import cn.com.higinet.tms35.core.concurrent.mutex;
import cn.com.higinet.tms35.core.concurrent.tms_worker;
import cn.com.higinet.tms35.core.concurrent.tms_worker_base;
import cn.com.higinet.tms35.core.concurrent.tms_worker_proxy;
import cn.com.higinet.tms35.core.dao.dao_combin;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.service.io_env;

/**
 * 数据初始化线程池
 * 
 * @author lining
 */
public class risk_serv_init extends tms_worker_proxy<run_env>
{
	volatile static tms_worker<run_env> inst_init = null;
	final static int thread_cnt = tmsapp.get_config("tms.init.workerCount", 8);
	final static int deque_size = tmsapp.get_config("tms.init.dequesize", 256);

//	final static dispacther disp = new dispacther(thread_cnt);

	public static tms_worker<run_env> init_pool()
	{
		if (inst_init != null)
			return inst_init;
		synchronized (risk_serv_init.class)
		{
			if (inst_init != null)
				return inst_init;
			return inst_init = new risk_serv_init("init", deque_size,
					thread_cnt);
		}
	}

	public risk_serv_init(String name, int deque_size, int thread_cnt)
	{
		super(name, null, thread_cnt);
		for (int i = 0; i < m_worker_pool.length; i++)
			m_worker_pool[i] = new risk_initdata_worker(name + "-" + i,
					deque_size);
	}

	public boolean request(run_env re)
	{
		 int i = thread_id(re.get_dispatch());
//		int i = disp.clac_worker_id(re.get_dispatch());

		tms_worker<run_env> worker = m_worker_pool[i % m_worker_pool.length];
		re.set_thread_name(worker.name());
		return worker.request(re);
	}

	private int thread_id(String dispatch)
	{
		return hash.clac(dispatch, 134218327) % 2297;
	}

	static public class risk_initdata_worker extends tms_worker_base<run_env>
	{
		public class counter_eval extends counter
		{
			run_env re;
			boolean next_called;

			public counter_eval(run_env re)
			{
				this.re = re;
				next_called = false;
			}

			@Override
			public void set_error(int e)
			{
				super.set_error(e);
				if (e <= 0)
					call_next(e);
			}

			@Override
			public int dec()
			{
				int c = super.dec();
				if (c <= 0)
					call_next(c);

				return c;
			}

			private void call_next(int i)
			{
				synchronized (this)
				{
					if (next_called)
						return;
					next_called = true;
				}
				request_next(this.re, i);
			}
		}

		public static class MutexHash
		{
			byte[] hashCodes = new byte[8192];
			mutex m_mutex = mutex.get_mutex_j5(false);
			cond m_cond = m_mutex.cond();

			public boolean wait(String dispatch)
			{
				int hashId = hash.clac(dispatch) % hashCodes.length;
				try
				{
					m_mutex.lock();
					while (hashCodes[hashId] == 1)
					{
						m_cond.await(1000);// add lining 2017-02-24 增加等待超时时间1秒
					}
					hashCodes[hashId] = 1;
				}
				finally
				{
					m_mutex.unlock();
				}
				return true;
			}

			public void clean(String dispatch)
			{
				try
				{
					m_mutex.lock();
					int hashId = hash.clac(dispatch) % hashCodes.length;
					hashCodes[hashId] = 0;
					m_cond.signal();
				}
				finally
				{
					m_mutex.unlock();
				}
			}
		}

		MutexHash m_mutex = new MutexHash();
		dao_combin m_dc = null;

		public risk_initdata_worker(String name, int deque_size)
		{
			super(name, deque_size);
		}

		@Override
		protected void post_run()
		{
			m_dc.close();
		}

		@Override
		protected void pre_run()
		{
			m_dc = new dao_combin(db_cache.get().table(), db_cache.get()
					.field());
		}

		@Override
		public void start()
		{
			super.start();
		}

		@Override
		public boolean request(run_env e)
		{
			e.m_ie.pin_time(io_env.INDEX_TRANS_INTO_INITQUEUE_BG);
			boolean b = super.offer(e);
			if (!b)
			{
				log.warn(name() + "- [txn_code: "
						+ e.m_channel_data.get("TXNCODE") + ", isConfirm: "
						+ e.is_confirm() + "], 放入初始化线程队列失败.");
			}
			e.m_ie.pin_time(io_env.INDEX_TRANS_INTO_INITQUEUE_END);
			return b;
		}

		@Override
		public void shutdown(boolean abort)
		{
			while (this.size() > 0)
				delay_tool.delay(10);

			super.shutdown(abort);
		}

		protected void run_once()
		{
			run_env re = take(100);
			if (re == null)
				return;
			try
			{
				_run_once(re);
			}
			catch (Exception e)
			{
				log.error(null, e);
				re.set_faild_result(e.getMessage());
			}
			finally
			{
//				disp.notify_trans_over(re.get_dispatch());
			}
		}

		protected void _run_once(run_env re)
		{
			re.set_thread_name(this.name());
			re.m_ie.pin_time(io_env.INDEX_TRANS_OUT_INITQUEUE);
			re.m_ie.pin_thread(io_env.INDEX_RISKINIT_THREADID);
			if (re.isHealth)
			{
				// 健康检查的交易，在队列中取出后，直接返回不做任何处理
				re.m_ie.setData("TCSM_TIME",
						tm_tool.lctm_ms() - re.m_ie.get_base_time());// 总耗时
				re.m_ie.setData("THREAD_NAME", this.name());// 处理线程名
				re.set_succeed_result();
				return;
			}
			counter cc = new counter_eval(re);
			cc.inc();
			re.m_ie.pin_time(io_env.INDEX_TRANS_DATAINIT_BG);
			m_mutex.wait(re.get_dispatch());
			if (!re.init(m_dc, cc))
			{
				cc.set_error(-1);
				translog_worker.worker().request(re.m_ie);
				return;
			}
			cc.dec();
		}

		void request_next(run_env re, int c)
		{
			re.m_ie.pin_time(io_env.INDEX_TRANS_DATAINIT_END);
			m_mutex.clean(re.get_dispatch());
			if (c < 0)
			{
				re.set_faild_result("数据初始化失败。");
				return;
			}
			risk_serv_eval.eval_pool().request(re);
		}
	}
}