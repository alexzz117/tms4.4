package cn.com.higinet.tms.engine.stat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.core.cache.db_stat;
import cn.com.higinet.tms.engine.core.cond.func_map;
import cn.com.higinet.tms.engine.run.run_env;

public class txn_stat
{
	static Logger log = LoggerFactory.getLogger(txn_stat.class);
	public final static char ch_split = '-';
	public db_stat base;

	static public String mk_param_str(run_env re, db_stat st, int[] param)
	{
		if (st.is_operational == 1)
			return null;
		
		if (param.length == 0)
			param = st.param_fd_index;
		StringBuffer sb = new StringBuffer(64);
		Object o = null;
		for (int i = 0, len = param.length; i < len; i++)
		{
			o = re.get_fd_value(param[i]);
			if (str_tool.is_empty(str_tool.to_str(o)))
			{
				// log.warn("统计对象为空值:"+st.toString()+":"+re.m_channel_data);
				return null;
			}
			sb.append(o);
			sb.append(ch_split);
		}
		return sb.toString();
	}

	static public stat_row make_query_data(run_env re, db_stat st, int[] param)
	{
		String p = mk_param_str(re, st, param);
		if (p == null)
			return null;
		return new stat_row(st.stat_id, p, stat_row.QUERY);
	}

	static public Object get_value(run_env re, db_stat st, stat_row sd, int cur_minute,
			Object cur_value)
	{
		try
		{
			if (st.is_operational == 1) {//计算表达式
		    	Object val = st.node.exec(re);
	            if (func_map.is_nothing(val))
	                val = null;
	            return val;
		    }
		    
			if (sd == null)
				return null;
	
			if (st.stat_id != sd.m_stat_id)
			{
				throw new RuntimeException("统计值与统计数据不匹配！");
			}
			
			// 事中统计
			if (st.stat_online != 0)
			{
				/*
				 * add lining 2015-07-01 begin
				 * 当跨交易(当前交易和引用的统计所在交易不存在父子关系时)引用统计时，
				 * 如果统计是事中的，则只取此统计的当前数据，不再做事中处理
				 */
				String tab_name = re.get_txn().m_tab.tab_name;// 当前交易编码
				String stat_txn = st.stat_txn;// 当前统计所在的交易编码
				// 当前统计和当前交易是否存在父子关系
				if (!(tab_name.startsWith(stat_txn) || stat_txn.startsWith(tab_name)))
				{
					// 不存在父子关系, 则使用非事中的方式获取统计数据
					return st.func_st.get(sd.get_value(), st, cur_minute, null);
				}
				/* add lining 2015-07-01 end */
				// 定义为成功或失败时统计的，不满足该条件，则不进行处理
				if (st.is_valid == 0)
					return st.func_st.get(sd.get_value(), st, cur_minute, cur_value);
	
				boolean cond = st.node == null ? true : func_map.is_true(st.node.exec(re));
	
				if (!cond)
				{
					return st.func_st.get(st.is_continues != 0 ? "" : sd.get_value(), // //
																						// 定义为连续的统计，条件不满足，清除
							st, cur_minute, cur_value);
				}
	
				return st.func_st.get_online(sd.get_value(), st, cur_minute, cur_value);
			}
	
			return st.func_st.get(sd.get_value(), st, cur_minute, cur_value);
		}
		catch (Exception e) {
			log.error(st.toString() + ": " + e.getMessage(), e);
			throw new tms_exception(e);
		}
	}
}
