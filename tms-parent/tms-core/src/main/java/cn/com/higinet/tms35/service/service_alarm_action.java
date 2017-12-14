package cn.com.higinet.tms35.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.server.core.AsyncService;
import cn.com.higinet.rapid.server.core.Request;
import cn.com.higinet.rapid.server.core.Response;
import cn.com.higinet.tms35.comm.StaticParameters;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tm_tool;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.cache.db_alarm_action;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.core.cache.linear;
import cn.com.higinet.tms35.core.cache.txn;
import cn.com.higinet.tms35.core.concurrent.tms_worker;
import cn.com.higinet.tms35.core.cond.func_map;
import cn.com.higinet.tms35.core.cond.node;
import cn.com.higinet.tms35.core.cond.parser.cond_parser;
import cn.com.higinet.tms35.core.dao.dao_alarm_action;
import cn.com.higinet.tms35.core.dao.dao_trafficdata_read;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.run.run_stats;
import cn.com.higinet.tms35.run.run_txn_values;
import cn.com.higinet.tms35.run.serv.risk_serv_init;

/**
 * 执行报警处理动作接口
 * @author lining
 *
 */
@Service("alarmActionService")
public class service_alarm_action extends AsyncService {
	static Logger log = LoggerFactory.getLogger(service_alarm_action.class);
	static final boolean isClearStatOn = tmsapp.get_config("tms.alarm.norisk.clear_stat", 0) == 1;
	
	@Override
	public void doService(Request req, Response res) {
		dao_trafficdata_read m_dao_txn_read = null;
		dao_alarm_action m_dao_act = null;
		io_env ie = new io_env(req, res);
		try {
			run_env re = run_env.identification(ie, true);
			if (re == null)
				return;
			String txnCode = ie.getParameterMap().get("TXNCODE");
			data_source ds = new data_source(bean.get_DataSource_tmp());
			m_dao_txn_read = new dao_trafficdata_read(
					db_cache.get().table(), db_cache.get().field(), ds);
			m_dao_act = new dao_alarm_action(ds);
			
			run_txn_values m_rf = null;
			if (isClearStatOn) {
				m_rf = m_dao_txn_read.read(re, txnCode);
				re.set_fd_value(m_rf);
				
				String confirmRisk = str_tool.to_str(re.get_fd_value(re.field_cache().INDEX_CONFIRMRISK));
				if ("0".equals(confirmRisk)) {
					// 人工确认无风险，清理统计值
					log.info(String.format("交易流水号:{%s}, 人工确认无风险, 查找需要清理的统计, 开始.", txnCode));
					linear<db_stat> list_stat = re.get_txn().m_stat;
					List<db_stat> statList = new ArrayList<db_stat>(list_stat.size());
					for (int i = 0, len = list_stat.size(); i < len; i++) {
						db_stat stat = list_stat.get(i);
						if (stat.is_continues != 0 && !str_tool.is_empty(stat.stat_cond)
								&& str_tool.upper_case(stat.stat_cond).indexOf("CONFIRMRISK") != -1) {
							// 1、统计为连续的
							// 2、统计条件不为空
							// 3、统计条件中包括人工确认风险状态的属性代码名称
							log.info(String.format("交易流水号:{%s}, 人工确认无风险, 查找到需要清理的统计:{%s}.", txnCode, stat.toString()));
							statList.add(stat);
						}
					}
					log.info(String.format("交易流水号:{%s}, 人工确认无风险, 查找需要清理的统计, 结束, 查找数: {%d}.", txnCode, statList.size()));
					if (!statList.isEmpty()) {
						long txn_time = ((Number) re.get_fd_value(re.field_cache().INDEX_TXNTIME)).longValue();
						re.set_txn_time(txn_time);
						run_stats.do_stat(re, statList);
					}
				}
			}
			
			linear<db_alarm_action> acts = m_dao_act.read_list(txnCode);
			if (acts.size() > 0) {
				if (m_rf == null) {
					m_rf = m_dao_txn_read.read(re, txnCode);
					re.set_fd_value(m_rf);
				}
				for (int i = 0, len = acts.size(); i < len; i++) {
					db_alarm_action act = acts.get(i);
					try {
						if (!str_tool.is_empty(act.ac_cond)) {
							node cond = cond_parser.build(act.ac_cond);
							node_prepare(re.get_txn(), cond);
							if (!func_map.is_true(cond.exec(m_rf.m_env))) {
								act.execue_result = "0";
								act.execue_info = "不满足执行条件";
								continue;
							}
						}
						node expr = cond_parser.build(act.ac_expr);
						node_prepare(re.get_txn(), expr);
//						expr.exec(m_rf.m_env); //此处一直报空指针异常，发现dao_trafficdata_read.read(run_env, String)方法里面把m_rf.m_env置空，现在直接修改成re对象  editor王兴 2017-4-21
						expr.exec(re);
						act.execue_result = "1";
						act.execue_info = "执行成功";
					} catch (Exception e) {
						act.execue_result = "0";
						act.execue_info = e.getLocalizedMessage();
						log.error("execute alarm action error.", e);
					} finally {
						act.execue_time = tm_tool.lctm_ms();
						m_dao_act.update(act);
					}
				}
				m_dao_act.flush();
				m_dao_act.commit();
			}
			ie.setReturnCode(StaticParameters.SYSTEM_SUCCESS);
			ie.done();
			
			//added by wujw,20160107
			tms_worker<run_env> worker = risk_serv_init.init_pool();
			worker.request(re);
		} catch (Exception e) {
			log.error("execue alarm action interface error.", e);
			ie.setData("errorInfo", e.getLocalizedMessage());
			ie.setReturnCode(StaticParameters.SYSTEM_ERROR);
			ie.done(e);
		} finally {
			if (m_dao_txn_read != null) {
				m_dao_txn_read.close();
			}
			if (m_dao_act != null) {
				m_dao_act.close();
			}
		}
	}
	
	private final void node_prepare(txn txn, node n)
	{
		if (n == null)
			return;
		try
		{
			n.prepare_rule(txn, null);
			n.prepare1(txn, null);
			n.prepare2(null);
			n.prepare3(txn);
		}
		catch (RuntimeException e)
		{
			throw e;
		}
	}
}