/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  stat_serv_worker_query.java   
 * @Package cn.com.higinet.tms35.stat.serv   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-8-24 12:03:58   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms35.stat.serv;

import java.util.ArrayList;
import java.util.List;

import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.comm.translog_worker;
import cn.com.higinet.tms35.core.concurrent.tms_worker_base;
import cn.com.higinet.tms35.core.concurrent.work_deque;
import cn.com.higinet.tms35.core.persist.Stat;
import cn.com.higinet.tms35.stat.StatQueryTransPin;
import cn.com.higinet.tms35.stat.stat_row;

/**
 * 统计查询类
 *
 * @ClassName:  stat_serv_worker_query
 * @author: 王兴
 * @date:   2017-8-24 12:03:58
 * @since:  v4.3
 */
public class stat_serv_worker_query extends tms_worker_base<stat_row> {

	/** 一次从统计查询队列中抽取的统计数量. */
	static private int m_query_batch_size = tmsapp.get_config("tms.stat.query.batchsize", 1024);

	/**统计查询的抽取动作时间间隔. */
	static private int m_query_batch_time = tmsapp.get_config("tms.stat.query.batchtime", 10);

	/**查询的时间阈值，超过这个阈值的话，就会打印对应的key值 edit by 王兴 2017-6-19 */
	static private int m_query_threshold_time = tmsapp.get_config("tms.stat.query.thresholdTime", 100);

	/** 统计持久化接口. */
	private Stat stat;

	/**
	 * 构造一个新的 stat serv worker query 对象.
	 *
	 * @param name the name
	 * @param request the request
	 * @param stat the stat
	 */
	public stat_serv_worker_query(String name, work_deque<stat_row> request, Stat stat) {
		super(name, request);
		this.stat = stat;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.core.concurrent.tms_worker_base#setup(java.lang.String[])
	 */
	@Override
	@Deprecated
	public void setup(String[] param) {
	}

	/** m cur list. */
	List<stat_row> m_cur_list = new ArrayList<stat_row>(1024);

	/** last run time. */
	long last_run_time = 0;

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.core.concurrent.tms_worker_base#run_once()
	 */
	@Override
	protected void run_once() {
		m_cur_list.clear();
		drainTo(m_cur_list, m_query_batch_size, m_query_batch_time);
		if (m_cur_list.isEmpty())
			return;

		StatQueryTransPin transPin = new StatQueryTransPin();
		transPin.pin_time(StatQueryTransPin.INDEX_PREPARE_STAT_QUERY);
		transPin.addData(StatQueryTransPin.INDEX_STAT_QUEUE_SIZE, m_cur_list.size());
		long start = System.currentTimeMillis();
		if (m_cur_list.size() > 0) {
			try {
				stat.queryList(m_cur_list); //查询中带有counter的减数操作
			} catch (Throwable e) {
				log.error("", e);
				// 如果出错了，本次请求的这些行，直接减数
				for (stat_row sd : m_cur_list) {
					if (sd != null && sd.getCounter() != null && sd.getCounter().get() > 0)//有m_batch的对象，并且m_batch大于0的，才进行set_error操作
						sd.set_error();
				}
				return;
			}
		}
		long costTime = System.currentTimeMillis() - start;
		if (costTime > m_query_threshold_time) {//riskServer多个交易的的规则执行都将受到这一批统计查询的影响
			//		//查询超过200毫秒的，把对应的key都打印出来
			log.warn(String.format("Query Stat in DB cost %dms,it's more than threshold time %dms.Query keys:%s", costTime, m_query_threshold_time, getKeysFromList(m_cur_list)));
		}
		transPin.pin_time(StatQueryTransPin.INDEX_STAT_QUERY_END);
		translog_worker.worker().request(transPin);
	}

	/**
	 * 返回 keys from list 属性.
	 *
	 * @param list the list
	 * @return keys from list 属性
	 */
	private String getKeysFromList(List<stat_row> list) {
		StringBuilder sb = new StringBuilder(1024 * 2);
		for (stat_row sv : list) {
			sb.append(sv.getUniqID()).append(",");
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.core.concurrent.tms_worker_base#shutdown(boolean)
	 */
	@Override
	public void shutdown(boolean abort) {
		super.shutdown(abort);
	}
}
