package cn.com.higinet.tms35.run.serv;

import cn.com.higinet.tms35.comm.delay_tool;
import cn.com.higinet.tms35.comm.hash;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.comm.translog_worker;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.concurrent.tms_worker;
import cn.com.higinet.tms35.core.concurrent.tms_worker_base;
import cn.com.higinet.tms35.core.concurrent.tms_worker_proxy;
import cn.com.higinet.tms35.core.dao.dao_combin;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.service.io_env;

/**
 * 风险评估线程池
 * 
 * @author lining
 * 
 */
public class risk_serv_eval extends tms_worker_proxy<run_env>
{
	volatile static tms_worker<run_env> inst_eval = null;
	final static int thread_cnt = tmsapp.get_config("tms.eval.workerCount", 24);
	final static int deque_size = tmsapp.get_config("tms.eval.dequesize", 256);
//	final static dispacther disp = new dispacther(thread_cnt);

	public static tms_worker<run_env> eval_pool()
	{
		if (inst_eval != null)
			return inst_eval;
		synchronized (risk_serv_eval.class)
		{
			if (inst_eval != null)
				return inst_eval;
			return inst_eval = new risk_serv_eval("eval", deque_size,
					thread_cnt);
		}
	}

	public risk_serv_eval(String name, int deque_size, int thread_cnt)
	{
		super(name, null, thread_cnt);
		for (int i = 0; i < m_worker_pool.length; i++)
			m_worker_pool[i] = new risk_evaluate_worker(name + "-" + i,
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

	static public class risk_evaluate_worker extends tms_worker_base<run_env>
	{
		dao_combin m_dc = null;

		public risk_evaluate_worker(String name, int deque_size)
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
			e.m_ie.pin_time(io_env.INDEX_TRANS_INTO_EVALQUEUE_BG);
			boolean b = super.offer(e);
			if (!b)
			{
				log.warn(name() + "- [txn_code: "
						+ e.m_channel_data.get("TXNCODE") + ", isConfirm: "
						+ e.is_confirm() + "], 放入风险评估线程队列失败.");
			}
			e.m_ie.pin_time(io_env.INDEX_TRANS_INTO_EVALQUEUE_END);
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
			re.m_ie.pin_time(io_env.INDEX_TRANS_OUT_EVALQUEUE);
			re.m_ie.pin_thread(io_env.INDEX_RISKEVAL_THREADID);
			re.m_ie.pin_time(io_env.INDEX_TRANS_RISKEVAL_BG);
			re.run(m_dc);
			translog_worker.worker().request(re.m_ie);
		}
	}
}