package cn.com.higinet.tms.engine.run;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.StaticParameters;
import cn.com.higinet.tms.engine.comm.clock;
import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.comm.tmsapp;
import cn.com.higinet.tms.engine.core.cache.db_stat;
import cn.com.higinet.tms.engine.core.cache.linear;
import cn.com.higinet.tms.engine.core.cache.txn;
import cn.com.higinet.tms.engine.core.concurrent.counter;
import cn.com.higinet.tms.engine.core.cond.func_map;
import cn.com.higinet.tms.engine.stat.stat_row;
import cn.com.higinet.tms.engine.stat.txn_stat;
import cn.com.higinet.tms.engine.stat.serv.stat_serv;

public class run_stats {
	static Logger logger = LoggerFactory.getLogger(run_stats.class);

	static final boolean isAsync = tmsapp.get_config("tms.stat.eval.isAsync", 1) == 1;

	static final int syncTimeout = tmsapp.get_config("tms.stat.eval.syncTimeout", 50);

	public static void do_stat(run_env re) {
		linear<db_stat> list_stat = re.get_txn().m_stat;
		do_stat(re, list_stat.m_list);
	}

	public static void do_stat(run_env re, List<db_stat> statList) {
		long txn_time = re.get_txn_time();
		String txn_status = re.get_txn_status();

		counter count = new counter();
		List<stat_row> list = new ArrayList<stat_row>(statList.size());
		for (int i = 0, len = statList.size(); i < len; i++) {
			db_stat ds = statList.get(i);
			// 定义为成功或失败时统计的，不满足该条件，则不进行处理
			// 函数为计算表达式的统计，统计数据不保存到运行统计表中 add lining 2014-05-12
			if (ds.is_valid == 0 //
					|| ds.stat_cond_result == 0 && !txn_status.equals(StaticParameters.TXN_STATUS_INHAND) || ds.stat_cond_result == 1 && !txn_status.equals(StaticParameters.TXN_STATUS_SUCCESS) || ds.stat_cond_result == 2 && !txn_status.equals(StaticParameters.TXN_STATUS_FAIL) || ds.is_operational == 1) {
				continue;
			}

			//			boolean cond = ds.node == null ? true : func_map.is_true(ds.node.exec(re));
			//加上ds.node执行后的结果判断，如果是NOTHING的话，则打印统计相关日志 edit by 王兴 2017-6-18
			boolean cond = false;
			if (ds.node == null) {
				cond = true;
			} else {
				Object res = ds.node.exec(re);
				if (func_map.is_nothing(res)) {
					//如果是noting的话，打印warn日志
					run_txn_values values = re.get_fd_value();
					txn _txn = re.get_txn();
					if (values != null && _txn != null) {
						String transcode = String.valueOf(values.get_fd(_txn.g_dc.field().INDEX_TXNCODE));
						String txnId = String.valueOf(values.get_fd(_txn.g_dc.field().INDEX_TXNID));
						logger.warn(String.format("Stat node execute result is \"NOTHING\".Stat transCode=%s,txnId=%s,statId=%d,statDesc=%s", transcode, txnId, ds.stat_id,ds.stat_desc));
					}
				}
				cond = func_map.is_true(res);
			}
			// 定义为连续的统计，条件不满足，清除
			if (!cond)// 条件未满足，并且连续统计，清空统计值
			{
				if (ds.is_continues != 0) {
					String param = txn_stat.mk_param_str(re, ds, ds.param_fd_index);
					if (param == null) {
						continue;
					}
					stat_row row = new stat_row(ds.stat_id, //
							param, //
							stat_row.CLEAR, txn_time,//
							str_tool.to_str(ds.stat_fd_name != null ? re.get_fd_value(ds.stat_fd_index) : null));
					row.set_counter(count);
					list.add(row);
				}
				continue;
			}

			String param = txn_stat.mk_param_str(re, ds, ds.param_fd_index);
			if (param == null) {
				continue;
			}

			// 进行统计
			stat_row row = new stat_row(ds.stat_id, param, 0, txn_time, str_tool.to_str(ds.stat_fd_name != null ? re.get_fd_value(ds.stat_fd_index) : null));
			row.set_counter(count);
			list.add(row);
		}
		clock c = new clock();
		stat_serv.eval_inst().request(list);
		if (!isAsync) {
			// 同步更新统计
			if (syncTimeout > 0) {
				count.wait_gt(syncTimeout);
				if (count.get() > 0) {
					logger.warn("统计同步更新超时, 统计数量: " + list.size() + ", 耗时: " + c.count_ms() + "ms, 超时时间: " + syncTimeout + "ms.");
				}
			} else {
				count.wait_gt(0, 10);
				if (logger.isDebugEnabled()) {
					logger.debug("统计同步更新完成, 统计数量: " + list.size() + ", 耗时: " + c.count_ms() + "ms.");
				}
			}
		}
	}
}