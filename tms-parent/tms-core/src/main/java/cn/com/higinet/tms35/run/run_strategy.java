package cn.com.higinet.tms35.run;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.RuleTestLog;
import cn.com.higinet.tms35.comm.clock;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tm_tool;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.comm.trans_pin;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.cache.db_disposal;
import cn.com.higinet.tms35.core.cache.db_fd;
import cn.com.higinet.tms35.core.cache.db_rule;
import cn.com.higinet.tms35.core.cache.db_rule_action;
import cn.com.higinet.tms35.core.cache.db_rule_action_hit;
import cn.com.higinet.tms35.core.cache.db_rule_hit;
import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.core.cache.db_strategy;
import cn.com.higinet.tms35.core.cache.db_strategy_rule_eval;
import cn.com.higinet.tms35.core.cache.db_tab;
import cn.com.higinet.tms35.core.cache.linear;
import cn.com.higinet.tms35.core.cache.txn;
import cn.com.higinet.tms35.core.cache.txn_ref_stat;
import cn.com.higinet.tms35.core.cond.func_map;
import cn.com.higinet.tms35.core.dao.dao_rule_action_hit;
import cn.com.higinet.tms35.core.dao.dao_rule_hit;
import cn.com.higinet.tms35.core.eval.eval_func;
import cn.com.higinet.tms35.core.eval.eval_row;
import cn.com.higinet.tms35.service.io_env;
import cn.com.higinet.tms4.model.RiskModelEngine;

public final class run_strategy {
	static Logger log = LoggerFactory.getLogger(run_strategy.class);

	private run_env m_re;

	private List<db_rule_hit> m_hit_rules;

	private List<db_rule_action_hit> m_hit_rule_acts;

	private static int ruleExecTimeout = tmsapp.get_config("tms.rule.exec.timeout", 1);//规则执行超时时间，单位毫秒，默认1 edit by 王兴 2017-6-27

	// 是否使用风险模型
	static boolean isUseRiskModel = (1 == tmsapp.get_config("tms.risk.model.isOn", 0)) ? true : false;

	private clock clock = new clock();

	public run_strategy(run_env re) {
		m_re = re;
		m_hit_rules = re.get_hit_rules();
		m_hit_rule_acts = re.get_hit_rule_acts();
	}

	public String risk_disposal() {
		String disposal = "PS01";
		float score = 0.f;// 规则分值, 用于商户评级
		db_strategy strategy = m_re.m_strategy;
		db_strategy_rule_eval.cache strec = db_cache.get().st_rule_eval();
		db_rule_action.cache drac = db_cache.get().rule_action();
		final linear<db_strategy_rule_eval> stres = strec.get_strategy_rule_eval_bystid(strategy.index);
		Map<Integer, eval_row> rows = new HashMap<Integer, eval_row>();
		List<db_rule_action> miss_rule_action = new ArrayList<db_rule_action>(16);//装载未满足执行条件的规则动作
		linear<db_rule> rules = strategy.st_rules;
		int trigrulenum = 0, hitrulenum = 0;
		StringBuilder sb = new StringBuilder(256);
		for (int i = rules.size() - 1; i >= 0; i--) {
			db_rule rule = rules.get(i);
			if (!rule.is_enable)
				continue;
			db_strategy_rule_eval dsre = null;
			eval_row row = null;
			if (stres != null && stres.size() > 0) {
				dsre = stres.get(rule.eval_type);
				if (dsre != null) {
					row = rows.get(rule.eval_type);
					if (row == null) {
						row = new eval_row();
						rows.put(rule.eval_type, row);
					} else {
						if (dsre.is_disposal() && dsre.func_eval.is_continue(row.get_data_length()))
							continue;
					}
				}
			}
			trigrulenum++;
			long start_time = tm_tool.lctm_ms();
			clock.reset();
			Object res = rule.node.exec(m_re); //加入规则执行的返回对象res，判断是否为NOTHING,如果是NOTHING的话，则打印出rule相关的信息，便于日志跟踪问题  edit by王兴 2017-6-18
			long cost = clock.count_ms();
			if (cost >= ruleExecTimeout) {
				//当规则执行时间超过限额时间的时候，在transpin日志里面打印出来
				sb.append(rule.id).append('-').append(cost).append('|');
			}
			if (func_map.is_nothing(res)) {
				//目前仅加入NOTHING判断，其他逻辑一概不动
				run_txn_values values = m_re.get_fd_value();
				txn _txn = m_re.get_txn();
				if (values != null && _txn != null) {
					String transcode = String.valueOf(values.get_fd(_txn.g_dc.field().INDEX_TXNCODE));
					String txnId = String.valueOf(values.get_fd(_txn.g_dc.field().INDEX_TXNID));
					log.warn(String.format("Rule node execute result is \"NOTHING\".Rule transCode=%s,txnId=%s,ruleId=%d,ruleDesc=%s", transcode, txnId, rule.id, rule.name));
				}
			}

			if (func_map.is_true(res)) {
				int numtimes = (int) (tm_tool.lctm_ms() - start_time);
				if (log.isDebugEnabled())
					log.debug("规则命中:" + rule.toString());
				//m_hit_rules.add(new db_rule_hit(dao_rule_hit.next_trigid(), m_re, rule, numtimes, this.m_re.get_txn_time()));
				m_hit_rules.add(new db_rule_hit(dao_rule_hit.next_trigid(), m_re, rule, numtimes, start_time));
				hitrulenum++;
				linear<db_rule_action> rule_actions = drac.get_rule_action(rule.index);
				for (db_rule_action rule_action : rule_actions) {
					if (!rule_action.is_enable)
						continue;
					if (rule_action.node_cond == null || (rule_action.node_cond != null && func_map.is_true(rule_action.node_cond.exec(m_re)))) {
						m_hit_rule_acts.add(new db_rule_action_hit(dao_rule_action_hit.next_hitid(), m_re, rule_action));
						rule_action.node_expr.exec(m_re);
					} else {
						miss_rule_action.add(rule_action);
					}
				}
				if (rule.is_test) {
					// 测试规则，不参加分值、处置的计算
					continue;
				}
				if (dsre == null) {
					db_disposal current = db_cache.get().disposal().get_dp(disposal);//当前处置方式，默认是PS01
					db_disposal triggered = db_cache.get().disposal().get_dp(rule.disposal);//已经触发的规则对应的处置方式
					if (current.dp_order < triggered.dp_order) { //目前修改成根据处置方式的order来定优先级，order值越大优先级越高
						disposal = rule.disposal;
					}
					score = score_add(score, rule.score);
				} else {
					dsre.func_eval.set(row, rule);
					if (dsre.is_disposal() && dsre.func_eval.is_break(row.get_data_length())) {
						break;
					}
				}
			} else {
				//打印rule相关的信息
			}
		}
		if (sb.length() > 0) {
			//当有信息输出的时候，在transpin中打印出rule id和执行时间
			m_re.m_ie.setData(trans_pin.INDEX_TRANS_RULE_TIMEOUT, sb.toString());
		}
		if (stres != null && stres.size() > 0) {
			for (int i = 0, len = stres.size(); i < len; i++) {
				db_strategy_rule_eval dsre = stres.get(i);
				if (dsre == null)
					continue;
				eval_row row = rows.get(dsre.eval_type);
				if (row != null) {
					String dp = dsre.func_eval.get(row, dsre);
					score = score_add(score, row.get_score());
					if (disposal.compareTo(dp) < 0)
						disposal = dp;
				}
			}
		}
		score = eval_func.get_score(score);
		if (!"PS01".equals(disposal)) {//不是放行,规则风险
			m_re.get_fd_value().set_fd(m_re.field_cache().INDEX_ISMODELRISK, 0);
		}
		/**********取风险模型分值【开始】add zhanglq**********/
		if (isUseRiskModel) {
			db_disposal.cache dpc = db_cache.get().disposal();
			String tab_name = m_re.get_txn().get_tab().tab_name;
			db_tab model = getModelById(tab_name);// 通过交易ID获取模型对象
			m_re.set_txn_model(model);
			if (model != null && "2".equals(model.modelused)) {
				m_re.m_ie.pin_time(io_env.INDEX_TRANS_MODEL_BG);
				// 风险模型分值
				double risk_model_score = -(calculateRiskModelScore(model));
				m_re.m_ie.pin_time(io_env.INDEX_TRANS_MODEL_END);
				String m_disposal = dpc.md_ps_code(risk_model_score);
				if (risk_model_score < 0) {
					m_re.get_fd_value().set_fd(m_re.field_cache().INDEX_ISMODELRISK, 1);
					if (model != null) {
						m_re.get_fd_value().set_fd(m_re.field_cache().INDEX_MODELID, model.tab_name);
					}
				}
				// 风险模型处置和风险引擎处置比较，取大的
				if (m_disposal.compareTo(disposal) > 0) {
					disposal = m_disposal;
					score = (float) risk_model_score;
				}
			}
		}
		/**********取风险模型分值【结束】add zhanglq**********/
		if (strategy.st_id != -1) {
			m_re.get_fd_value().set_fd(m_re.field_cache().INDEX_STRATEGY, strategy.st_id);
		}
		m_re.get_fd_value().set_fd(m_re.field_cache().INDEX_SCORE, score);
		m_re.get_fd_value().set_fd(m_re.field_cache().INDEX_TRIGRULENUM, trigrulenum);
		m_re.get_fd_value().set_fd(m_re.field_cache().INDEX_HITRULENUM, hitrulenum);
		m_re.get_fd_value().set_fd(m_re.field_cache().INDEX_DISPOSAL, disposal);
		if (!miss_rule_action.isEmpty()) {
			run_actions.add_actions(m_re.get_txn_code(), miss_rule_action);
		}
		// 规则测试记录报警日志
		RuleTestLog.txn_log(m_re.get_fd_value());

		// 规则测试记录报警日志
		//RuleTestLog.alert_log(m_re.get_alert(), ret);

		// 规则测试记录规则触发日志
		RuleTestLog.rule_log(m_re.get_hit_rules());

		// 规则测试记录开关触发日志
		//RuleTestLog.sw_log(m_re.get_sw());
		return disposal;
	}

	private double calculateRiskModelScore(db_tab model) {
		if (model == null || !"2".equals(model.modelused)) {
			log.debug("模型在非运行期，不执行风险模型评分！");
			return 0;
		}

		String model_id = model.tab_name;

		// 组织本次的交易数据
		Map<String, Object> txn_data = new HashMap<String, Object>();
		List<Object> txn_info = m_re.get_fd_value().m_txn_data;
		for (int i = 0; i < txn_info.size(); i++) {
			db_fd fd = m_re.get_fd_def(i);
			if (fd != null)
				txn_data.put(fd.fd_name, txn_info.get(i));
		}

		db_fd.cache dfc = m_re.get_txn().g_dc.field();
		db_stat.cache dsc = m_re.get_txn().g_dc.stat();
		linear<db_stat> list_stat = m_re.get_txn().m_stat;
		linear<txn_ref_stat> list_ref_stat = m_re.get_txn().m_ref_stat;
		for (int i = 0, len = list_ref_stat.size(); i < len; i++) {
			txn_ref_stat ref_stat = list_ref_stat.get(i);
			if (ref_stat.param.length > 0) {
				continue;
			}
			db_stat stat = dsc.get(ref_stat.st_index);
			String storecolumn = stat.storecolumn;
			if (stat.is_valid == 0 || str_tool.is_empty(storecolumn) || !list_stat.m_list.contains(stat)) {
				continue;
			}
			if (dfc.get_fdname_local_index(m_re.id(), storecolumn) > -1) {
				txn_data.put(String.valueOf(storecolumn), m_re.get_stat_value(i, null));
			}
		}

		// 组织属性列表
		List<Map<String, Object>> featureList = new ArrayList<Map<String, Object>>();
		linear<db_fd> fd = m_re.get_txn().m_fd;
		for (db_fd db_fd : fd) {
			String fd_name = db_fd.fd_name;
			if (str_tool.is_empty(fd_name))
				continue;

			Map<String, Object> m = new HashMap<String, Object>();
			m.put("FD_NAME", fd_name);
			m.put("TYPE", db_fd.type);
			featureList.add(m);
		}
		linear<db_stat> m_ref_stat = m_re.get_txn().m_stat;
		for (db_stat db_stat : m_ref_stat) {
			String storecolumn = db_stat.storecolumn;
			if (str_tool.is_empty(storecolumn))
				continue;

			Map<String, Object> m = new HashMap<String, Object>();
			m.put("FD_NAME", storecolumn);
			m.put("TYPE", db_stat.datatype);
			featureList.add(m);
		}

		// 计算风险模型分值
		double risk_model_score = ((RiskModelEngine) bean.get("riskModelEngine")).getRiskModeScore(txn_data, featureList, model_id);

		return risk_model_score > 100 ? 100 : risk_model_score;
	}

	// 通过交易ID获取模型对象
	private db_tab getModelById(String tsb_name) {
		String[] parentAndSelf = cutToIds(tsb_name);

		int tab_index = db_cache.get().table().tab_count();
		for (int j = parentAndSelf.length - 1; j >= 0; j--) {
			for (int i = tab_index - 1; i > 0; i--) {
				db_tab tab = db_cache.get().table().get(i);
				String modelUsed = tab.modelused;
				if (!str_tool.is_empty(modelUsed) && !"0".equals(modelUsed) && parentAndSelf[j].equals(tab.tab_name)) {
					return tab;
				}
			}
		}
		return null;
	}

	public static final String[] cutToIds(String txnid) {

		String[] txnids = new String[txnid.length() / 2 + 1];
		int offset = 0;

		for (int i = 0; i < txnids.length; i++) {
			txnids[i] = txnid.substring(0, 1 + offset);
			offset += 2;
		}

		return txnids;
	}

	private float score_add(float s1, float s2) {
		BigDecimal b1 = new BigDecimal(Float.toString(s1));
		BigDecimal b2 = new BigDecimal(Float.toString(s2));
		return b1.add(b2).floatValue();
	}
}