package cn.com.higinet.tms35.stat.serv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cn.com.higinet.tms35.comm.clock;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.concurrent.tms_worker_base;
import cn.com.higinet.tms35.core.dao.dao_stat_update_back_lob;
import cn.com.higinet.tms35.core.dao.stat_value;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.core.dao.stmt.sql_reconnect_exception;

public class stat_serv_worker_back extends tms_worker_base<stat_value>
{
	static final int g_back_bacth_size = tmsapp.get_config("tms.stat.back.batchsize", 1024);
	static final int g_back_bacth_time = tmsapp.get_config("tms.stat.back.batchtime", 100);
	static final int g_update_batch_size = tmsapp.get_config("tms.stat.back.commitbatch", 1000);
	static final int g_commit_size = tmsapp.get_config("tms.stat.back.commit_byte_size", 8) * 1024;
	static final int g_commit_maxperiod = tmsapp.get_config("tms.stat.back.commit_period", 1000);

	@Override
	public void setup(String[] param)
	{
		if (param[0].equals("reset_db"))
		{
			m_batch_stat_update.close();

			data_source ds = new data_source(param[1], param[2], param[3], param[4]);
			m_batch_stat_update = new updater(ds);
		}
	}

	class updater
	{
		private dao_stat_update_back_lob m_batch_update;
		List<stat_value> m_tmp_list = new LinkedList<stat_value>();
		List<stat_value> m_commit_list = new LinkedList<stat_value>();
		int m_commit_size = 0;

		public updater(data_source ds)
		{
			m_batch_update = new dao_stat_update_back_lob(ds, g_update_batch_size);
		}

		protected void commit(java.util.Collection<stat_value> request)
		{
			if (request != null)
				m_tmp_list.addAll(request);

			while (commit())
			{
			}
		}

		clock m_commit_clock = new clock();

		protected boolean commit()
		{
			Iterator<stat_value> it = m_tmp_list.iterator();
			for (; it.hasNext();)
			{
				stat_value sv = it.next();
				it.remove();

				m_commit_list.add(sv);
				m_commit_size += sv.toString().length() + 1;// +1为了对付清空的情况
				if (m_commit_size >= g_commit_size)
					break;
			}

			if (m_commit_size == 0 || m_commit_size < g_commit_size
					&& m_commit_clock.count_ms() < g_commit_maxperiod)
			{
				return false;
			}

			m_commit_clock.reset();
			while (!m_commit_list.isEmpty())
			{
				try
				{
					for (stat_value sv : m_commit_list)
						m_batch_update.update(sv);

					m_batch_update.flush();
					break;
				}
				catch (sql_reconnect_exception e)
				{
					log.error("数据库异常断开，已经重连，现在重做事务。", e);
					continue;
				}
				catch (Exception e)
				{
					log.error(null, e);
					break;
				}
			}

			m_commit_list.clear();
			m_commit_size = 0;

			return true;
		}

		public void close()
		{
			m_batch_update.close();
		}
	}

	updater m_batch_stat_update;

	public stat_serv_worker_back(String name, int bufSize)
	{
		super(name, bufSize);
		data_source ds = new data_source();
		m_batch_stat_update = new updater(ds);
	}

	// 存放当前线程需要马上处理的全部统计集合
	List<stat_value> m_cur_list = new ArrayList<stat_value>(1024);

	clock c = new clock();
	protected void run_once()
	{
		m_cur_list.clear();
		drainTo(m_cur_list, g_back_bacth_size, g_back_bacth_time);

		m_batch_stat_update.commit(null);
		if (m_cur_list.isEmpty())
			return;

		log.info("备份统计写库数量：" + m_cur_list.size());
		c.reset();
		m_batch_stat_update.commit(m_cur_list);
		if (c.count_ms() > 1000)
		{
			log.warn("备份统计提交时间:" + c.count_ms());
		}
	}

	@Override
	public void shutdown(boolean abort)
	{
		super.shutdown(abort);
		m_batch_stat_update.close();
	}
}