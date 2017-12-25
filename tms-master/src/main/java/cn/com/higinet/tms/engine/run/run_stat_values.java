package cn.com.higinet.tms.engine.run;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.cache.db_stat;
import cn.com.higinet.tms.engine.core.cache.db_userpattern;
import cn.com.higinet.tms.engine.core.cache.linear;
import cn.com.higinet.tms.engine.core.cache.txn_ref_stat;
import cn.com.higinet.tms.engine.core.concurrent.counter;
import cn.com.higinet.tms.engine.stat.stat_row;
import cn.com.higinet.tms.engine.stat.txn_stat;
import cn.com.higinet.tms.engine.stat.serv.stat_serv;

/**
 * 1、缓存当前交易所有运行时需要的统计数据 2、处理当前交易所有统计相关的逻辑
 * 
 * 引用统计的功能有： 1、开关条件 2、处置条件 3、规则条件 4、统计条件
 */

public final class run_stat_values
{
	static Logger log = LoggerFactory.getLogger(run_stat_values.class);

	private run_env m_re;// 运行环境
	private linear<txn_ref_stat> ref_stat;// 结构， 当前交易引用的统计和参数列表，使用该数据加载本交易
	private counter query_counter;

	/*
	 * 数据,当前交易所需要的所有的统计数据,所有的统计数据，同一个统计，使用不同的引用参数，会计算为2个
	 */
	private List<stat_row> st_data;

	public run_stat_values(run_env re, counter counterEval)
	{
		m_re = re;
		query_counter = counterEval;
		ref_stat = re.get_ref_stat();
		st_data = new ArrayList<stat_row>(ref_stat.size());
		load(re.is_confirm());
	}

	public boolean load(boolean is_confirm)
	{
		return is_confirm // 
		? init_for_confirm() //
				: (m_re.get_txn_status() != null ? init_for_all() : init_for_eval());
	}

	private boolean init_for_all()
	{
		db_stat.cache dsc = m_re.get_txn().g_dc.stat();

		for (int i = 0, len = ref_stat.size(); i < len; i++)
		{
			txn_ref_stat rs = ref_stat.get(i);
			stat_row row = txn_stat.make_query_data(m_re, dsc.get(rs.st_index), rs.param);
			st_data.add(row);
			if (row == null)
				continue;
			row.set_counter(query_counter);
		}
		return load();
	}

	private boolean init_for_eval()
	{
		db_stat.cache dsc = m_re.get_txn().g_dc.stat();
		for (int i = 0, len = ref_stat.size(); i < len; i++)
		{
			txn_ref_stat rs = ref_stat.get(i);
			if (!rs.ref_in_risk_eval())
			{
				st_data.add(null);
				continue;
			}

			stat_row row = txn_stat.make_query_data(m_re, dsc.get(rs.st_index), rs.param);
			st_data.add(row);
			if (row == null)
				continue;
			row.set_counter(query_counter);
		}
		return load();
	}

	private boolean init_for_confirm()
	{
		db_stat.cache dsc = m_re.get_txn().g_dc.stat();
		for (int i = 0, len = ref_stat.size(); i < len; i++)
		{
			txn_ref_stat rs = ref_stat.get(i);
			if (!rs.ref_in_confirm())
			{
				st_data.add(null);
				continue;
			}

			stat_row row = txn_stat.make_query_data(m_re, dsc.get(rs.st_index), rs.param);
			st_data.add(row);
			if (row == null)
				continue;
			row.set_counter(query_counter);
		}
		return load();
	}

	private boolean load()
	{
		List<stat_row> tmp = new ArrayList<stat_row>(st_data.size());
		for (stat_row row : st_data)
		{
			if (row == null)
				continue;
			tmp.add(row);
		}
		if (tmp.isEmpty())
			return true;

		// if(true)
		// {
		// for(stat_row row:st_data)
		// {
		// if(row==null)
		// continue;
		// row.set_value("");
		// row.dec_query_batch();
		// }
		//				
		// return true;
		// }

		stat_serv.query_inst().request(tmp);
		return true;
	}

	public Object get_val(int local_id, int txn_minute, Object curVal)
	{
		db_stat.cache dsc = m_re.get_txn().g_dc.stat();

		//int st_index = ref_stat.get(local_id).st_index;
		txn_ref_stat trs=ref_stat.get(local_id);
		if(null==trs){
			return null;
		}
		int st_index = trs.st_index;
		
		db_stat st = dsc.get(st_index);
		stat_row sd = st_data.get(local_id);

		if (st.stat_data_fd != null && curVal == null)
			curVal = m_re.get_fd_value(st.stat_fd_index);

		// start add by zhanglq 20131008
		// 如果引用对象为用户的统计存在自定义行为习惯，取行为习惯的值
		if (run_env.have_user_pattern && m_re.user_pattern != null
				&& st.is_user_pattern(this.m_re.get_txn().dc().field().INDEX_USERID))
		{
			long txntime = m_re.get_txn_time();
			db_userpattern up = m_re.user_pattern;
			Object pv = st.func_st.getPatternValue(st, sd, up, txntime, txn_minute, curVal);
			if (log.isDebugEnabled())
				log.debug("user pattern value:" + pv);
			if (pv != null)
			{
				return pv;
			}
		}
		// end

		// 取统计的值
		Object o = txn_stat.get_value(m_re, st, sd, txn_minute, curVal);

//		if (log.isInfoEnabled())
//			log.info(st.toString() + "=" + str_tool.to_str(o));

		return o;
	}

	// public static void main(String[] args)
	// {
	// String a = null;
	// Object curVal = "1000.0";
	// String pattern_value = "2012-02-12 22:12:33";
	//
	// SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// // System.out.println(formatter.parse(pattern_value).getTime());
	// System.out.println(date_tool.parse(pattern_value).getTime());
	// }
}