package cn.com.higinet.tms35.run;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.rapid.server.message.Message;
import cn.com.higinet.tms.base.constant.Constants;
import cn.com.higinet.tms.base.entity.online.tms_run_rule_action_hit;
import cn.com.higinet.tms.base.entity.online.tms_run_ruletrig;
import cn.com.higinet.tms.common.event.EventBus;
import cn.com.higinet.tms.common.event.EventContext;
import cn.com.higinet.tms.event.Params;
import cn.com.higinet.tms.event.Topics;
import cn.com.higinet.tms.event.modules.kafka.KafkaTopics;
import cn.com.higinet.tms35.comm.MapUtil;
import cn.com.higinet.tms35.comm.NotErrorException;
import cn.com.higinet.tms35.comm.StaticParameters;
import cn.com.higinet.tms35.comm.TransNotFoundException;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tm_tool;
import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.cache.db_action;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.cache.db_device;
import cn.com.higinet.tms35.core.cache.db_fd;
import cn.com.higinet.tms35.core.cache.db_rule_action_hit;
import cn.com.higinet.tms35.core.cache.db_rule_hit;
//import cn.com.higinet.tms35.core.cache.db_session;
import cn.com.higinet.tms35.core.cache.db_strategy;
import cn.com.higinet.tms35.core.cache.db_tab;
import cn.com.higinet.tms35.core.cache.db_userpattern;
import cn.com.higinet.tms35.core.cache.linear;
import cn.com.higinet.tms35.core.cache.str_id;
import cn.com.higinet.tms35.core.cache.txn;
import cn.com.higinet.tms35.core.cache.txn_ref_stat;
import cn.com.higinet.tms35.core.concurrent.counter;
import cn.com.higinet.tms35.core.cond.func_map;
import cn.com.higinet.tms35.core.cond.node;
import cn.com.higinet.tms35.core.dao.dao_combin;
import cn.com.higinet.tms35.core.persist.Traffic;
import cn.com.higinet.tms35.service.io_env;
import cn.com.higinet.tms4.model.common.StringUtil;

/*
 * 描述一个交易运行时的数据
 * 规则、条件、开关、策略运行时需要的回调函数 * 
 * */
public final class run_env extends run_time {
	static Logger log = LoggerFactory.getLogger(run_env.class);

	public static final boolean have_user_pattern = 1 == tmsapp.get_config("tms.eval.have_user_pattern", 0);

	public static final int isSyncOn = tmsapp.get_config("sync.isOn", 2);

	public static final String auth_status_fdname = "AUTHSTATUS";

	public final static String DFP_COOKIE_NAME = tmsapp.get_config("tms.dfp.cookiename", "higinet_tms_dfp");

	public io_env m_ie;

	private txn m_txn;

	private boolean m_is_confirm;

	public db_strategy m_strategy;

	//	private db_session m_session;

	//	private db_user m_user;

	//	private db_device m_device,
	//					m_stark_device;//要替换的设备
	private double m_device_status;//设备状态-1可疑设备，0新设备，>0的匹配度

	private double m_user_device_status = -1;//设备状态-1可疑设备，0新设备，>0的匹配度

	private String m_device_token;//设备token

	private run_txn_values m_rf, m_last_rf;

	private long txn_time;

	private run_stat_values m_rs;

	private List<db_rule_hit> m_hit_rules;

	private List<db_rule_action_hit> m_hit_rule_acts;

	private List<db_action> m_actions;

	public db_userpattern user_pattern;

	private db_tab model;

	private String thread_name;

	private dao_combin m_dc;// init和run可能不是同一个线程执行的，所以dao_combin也可能不是同一个，在init和run时都要重现赋值 add lining 20140618

	public boolean returned = false;

	public boolean isHealth = false;// 是否健康检查的交易

	private static final Traffic trafficdata = bean.get(Traffic.class); //持久化接口由外部注入

	private static final EventBus eventBus = bean.get(EventBus.class); //消息总线由外部注入

	public static run_env identification(io_env ie, boolean is_confirm) {
		TreeMap<String, String> m = ie.getParameterMap();
		int txnIdx = -1;
		boolean isHealth = false;
		String transCode = MapUtil.getString(ie.getHeadMap(), Message.HEAD_TRANSACTION_CODE);
		if (StaticParameters.RISK_HEALTH_CHECK.equals(transCode)) {
			// 健康检查
			txnIdx = 0;
			isHealth = true;
		} else {
			String txnid = m.get("TXNID");
			db_tab txn = db_cache.get().table().get_by_txnid(txnid);

			if (txn == null) {
				ie.setReturnCode(StaticParameters.ERROR_NOSIGNATURE);
				String s = null;
				ie.setData("errorInfo", s = "无法识别的交易：" + txnid);
				log.error(s, new tms_exception(s));
				ie.done(null);
				return null;
			}

			if (txn.is_enable == 0) {
				ie.setReturnCode(StaticParameters.ERROR_DISABLED);
				ie.setData("errorInfo", "交易未启用：" + txnid);
				ie.done(null);

				return null;
			}

			if (str_tool.is_empty(m.get("USERID")) && str_tool.is_empty(m.get("LOGINNAME"))) {
				set_faild_result(ie, "交易属性USERID和LOGINNAME不能都为空");
				return null;
			}

			String dispatch = m.get("DISPATCH");
			if (str_tool.is_empty(dispatch) || "null".equals(dispatch)) {
				String txnCode = m.get("TXNCODE");
				log.warn("交易流水号: " + txnCode + ", DISPATCH为" + dispatch);
				m.put("DISPATCH", txnCode);
			}

			/* add lining 2014-03-14 begin 
			 * 设备指纹中的特殊字符转换
			 * */
			String device_finger = m.get("DEVICEFINGER");
			if (!str_tool.is_empty(device_finger)) {
				m.put("DEVICEFINGER", str_tool.xml_decode(device_finger));

			} else {//added by wujw 20150930
				m.put("DEVICEFINGER", "");
			}
			/* end */
			txnIdx = txn.index;
		}
		run_env re = new run_env(txnIdx, (TreeMap<String, String>) m, is_confirm);
		re.isHealth = isHealth;
		ie.pin_time(io_env.INDEX_TRANS_IDENTIFY_END);
		ie.isConfirm(is_confirm);
		re.m_ie = ie;
		re.setStartTime(re.m_ie.get_base_time());
		return re;
	}

	public TreeMap<String, String> m_channel_data;

	private run_env(int txn_id, TreeMap<String, String> m, boolean is_confirm) {
		super();
		m_txn = txn.get(txn_id);
		m_is_confirm = is_confirm;
		m_channel_data = m;
		init_ok = false;
	}

	static AtomicLong init_counter = new AtomicLong(0);

	static AtomicLong eval_counter = new AtomicLong(0);

	boolean init_ok;

	public boolean init(dao_combin m_dc, counter counterEval) {
		try {
			// 发送离线交易数据
			if (isSyncOn == 1) {
				// bin_stream bs = new bin_stream(128);
				//				SyncApi.send(m_channel_data, null, null, null);
			}
			this.m_dc = m_dc;
			init_(m_dc, counterEval);
			return init_ok = true;
		} catch (NotErrorException e) {
			log.warn(e.getMessage(), e);
			this.set_succeed_result();
			return false;
		} catch (TransNotFoundException e) {
			log.error(e.getMessage(), e);
			this.set_faild_result(e.getMessage(), StaticParameters.TMS_CONFIRM_NOTRANS);
			return false;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			this.set_faild_result(e.getMessage());
			return false;
		}
	}

	private void init_(dao_combin m_dc, counter counterEval) throws Exception {
		/*4.3版本中注释掉
		 * if (str_tool.is_empty(this.get_user_id())) {
			// USERID为空时, 通过登陆名LOGINNAME, 查询USERID并设置到渠道Map中
			String loginName = m_channel_data.get("LOGINNAME");
			if (!str_tool.is_empty(loginName)) {
				m_user = m_dc.stmt_user.read(null, loginName);
				if (str_tool.is_empty(m_user.userid)) {
					m_user = null;
				} else {
					m_channel_data.put("USERID", m_user.userid);
				}
			}
		} else {
			String loginName = m_channel_data.get("LOGINNAME");
			if (str_tool.is_empty(loginName)) {
				m_user = m_dc.stmt_user.read(this.get_user_id());
				if (str_tool.is_empty(m_user.userid)) {
					m_user = null;
				} else {
					m_channel_data.put("LOGINNAME", m_user.username);
				}
			}
		}*/

		m_rf = new run_txn_values(this);
		m_rf.init_data(m_dc, m_channel_data);
		{
			Object txn_time_o = m_rf.get_fd(m_txn.g_dc.field().INDEX_TXNTIME);
			if (txn_time_o == null)
				txn_time = tm_tool.lctm_ms();
			else
				txn_time = ((Number) txn_time_o).longValue();
		}
		if (db_device.IS_DEVFINGER_ON) {
			m_rf.set_fd(m_txn.g_dc.field().INDEX_DEVSTATUS, m_device_status);
			m_rf.set_fd(m_txn.g_dc.field().INDEX_USER_DEVSTATUS, m_user_device_status);
		}

		// log.info("start-async-query:"+counterEval.get());
		m_ie.setTxnCode(this.get_txn_code());
		m_ie.setTxnId(this.get_txn().get_tab().txn_id);
		m_ie.setUserId(this.get_user_id());
		m_ie.pin_time(io_env.INDEX_TRANS_LOAD_STAT_BG);
		m_rs = new run_stat_values(this, counterEval);// 异步过程

		m_actions = new ArrayList<db_action>();
		// add by zhanglq 20130930
		// start 初始化用户自定义行为习惯的缓存
		if (have_user_pattern) {
			String user_id = str_tool.to_str(this.get_fd_value(m_txn.g_dc.field().INDEX_USERID));
			if (user_id != null) {
				//user_pattern = db_userpattern.cache.get(user_id);
				user_pattern = m_dc.stmt_user_pattern.read(user_id);
			}
		}
		// end

		String txn_code = str_tool.to_str(m_rf.get_fd(m_txn.g_dc.field().INDEX_TXNCODE));
		if (StringUtil.isNotEmpty(txn_code)) {
			if (this.is_confirm()) {
				run_txn_values rf = trafficdata.getById(txn_code, this); //此处改由持久化接口实现，4.3版本流水集中缓存修改  2017-09-05 王兴
				if (!rf.not_empty()) {
					// 查不到对应的风险评估数据
					throw new TransNotFoundException("风险确认找不到对应的交易, txnCode: " + txn_code);
				}
				String txn_status = str_tool.to_str(rf.get_fd(this.field_cache().INDEX_TXNSTATUS));
				if ("1".equals(txn_status) || "0".equals(txn_status)) {
					// 此交易已经是最终状态成功或失败，不可再进行交易确认
					throw new NotErrorException("交易状态已经是最终值, 不可再进行风险确认, txnCode: " + txn_code);
				}
				m_rf.set_indb(rf.is_indb());
				m_rf.extend(rf);

				Object txn_time_o = m_rf.get_fd(field_cache().INDEX_TXNTIME);
				if (txn_time_o != null)
					txn_time = ((Number) txn_time_o).longValue();

				//m_rf.set_fd(field_cache().INDEX_SCORE, rf.get_fd(field_cache().INDEX_SCORE));//设置分值，用于分值类的统计
			} else {
				run_txn_values rf = trafficdata.getCacheById(txn_code, this); //此处改由持久化接口实现，4.3版本流水集中缓存修改  2017-09-05 王兴
				//303-311行代码是2017-07-07添加，这里加入这个判断，是为了避免如下情况发生而导致的重复统计
				//1、评估交易发往服务器A，A服务器readtimeout，进而转移到服务器B
				//2、交易确认发往服务器A，A服务器readtimeout，APi客户端忽略掉了此异常，不再重发
				//3、服务器A先执行了交易确认，再执行一遍评估，因为先执行了交易确认，所以此时的评估是有txnstatus的，导致统计重复
				//修改人 王兴 2017-07-07
				if (rf.not_empty()) {
					String txn_status = str_tool.to_str(rf.get_fd(this.field_cache().INDEX_TXNSTATUS));
					if ("1".equals(txn_status) || "0".equals(txn_status)) {
						// 此交易已经是最终状态成功或失败，不可再进行风险评估
						throw new NotErrorException("交易状态已经是最终值, 不可再进行风险评估, txnCode: " + txn_code);
					}
				}

				/*tms4.3去掉会话表，目前没有任何业务使用这个表
				 * String session_id = str_tool.to_str(m_rf.get_fd(m_txn.g_dc.field().INDEX_SESSIONID));
				if (session_id != null) {
					m_session = m_dc.stmt_session.read(session_id);
					m_ie.pin_time(io_env.INDEX_TRANS_LOAD_SESSION_END);
					if (m_session != null && str_tool.is_empty(m_session.sessionid))
						init_session_info();
				
					if (m_session.is_indb()) {
						log.info("session in db,then load traffic data.");
						m_last_rf = rf;
					}
				}*/

				if (rf.not_empty()) {
					//					m_rf.set_indb(rf.is_indb());
					m_rf.set_indb(true);//当缓存中有数据时，直接认为是已经持久化到数据库中了
					m_rf.extend(rf);
				} else {
					// 把当前的交易数据，赋值给缓存
					// rf.extend(m_rf);  //4.3版本注释掉
				}

				/*if (m_user == null) {
					String user_id = str_tool.to_str(m_rf.get_fd(m_txn.g_dc.field().INDEX_USERID));
					if (!str_tool.is_empty(user_id)) {
						m_user = m_dc.stmt_user.read(user_id);
						m_ie.pin_time(io_env.INDEX_TRANS_LOAD_USER_END);
					}
				}*/
			}
			m_ie.pin_time(io_env.INDEX_TRANS_LOAD_TRANFFIC_END);
		}
		if (log.isDebugEnabled())
			log.debug("CUR-TXNTIME:" + txn_time);
	}

	public void set_succeed_result() {
		m_ie.setReturnCode(StaticParameters.SYSTEM_SUCCESS);
		m_ie.done();
	}

	public void set_faild_result(String message) {
		set_faild_result(m_ie, message, StaticParameters.SYSTEM_ERROR);
	}

	public void set_faild_result(String message, String return_code) {
		set_faild_result(m_ie, message, return_code);
	}

	static void set_faild_result(io_env ie, String message) {
		set_faild_result(ie, message, StaticParameters.SYSTEM_ERROR);
	}

	static void set_faild_result(io_env ie, String message, String return_code) {
		ie.setData("errorInfo", message);
		ie.setReturnCode(return_code);
		ie.done();
	}

	public void run(dao_combin dc) {
		try {
			this.m_dc = dc;
			run_(dc);
			this.m_ie.pin_time(io_env.INDEX_TRANS_RISKEVAL_END);
			set_succeed_result();
			this.stop();
		} catch (Exception e) {
			this.m_ie.pin_time(io_env.INDEX_TRANS_RISKEVAL_END);
			log.error(e.getMessage(), e);
			set_faild_result(e.getMessage());
		}
	}

	public void run_(dao_combin dc) throws Exception {
		m_strategy = get_strategy();
		if (m_strategy.is_async()) {
			m_ie.isAsync(true);
			m_ie.setData("score", 0);
			m_ie.setData("disposal", "PS01");
			this.set_succeed_result();
		}

		if (this.is_confirm()) {
			/* add lining 风险确认交易，判断认证是否通过
			 * 认证通过: 1
			 * 认证未通过，0
			 * */
			m_rf.set_fd(this.field_cache().INDEX_ISCORRECT, get_auth_status());
			run_stats.do_stat(this);
		} else {
			m_ie.pin_time(io_env.INDEX_TRANS_STRATEGY_BG);

			String disposal = "";

			//			if (!bank_switch_is_on()) {
			//				disposal = "PS05";
			//				this.get_fd_value().set_fd(this.field_cache().INDEX_SCORE, -80);
			//				this.get_fd_value().set_fd(this.field_cache().INDEX_TRIGRULENUM, 0);
			//				this.get_fd_value().set_fd(this.field_cache().INDEX_HITRULENUM, 0);
			//				this.get_fd_value().set_fd(this.field_cache().INDEX_DISPOSAL, disposal);
			//			} else {
			disposal = eval_strategy();
			//			}

			/*if ("PS05".equals(disposal) || "PS06".equals(disposal) || "PS07".equals(disposal)) {
				// 处置方式为:阻断、阻断并冻结账户、阻断并冻结会员时,自动设置交易状态为失败,并冻结账户或会员
				//comment by wujw,20151015,改成有顺手付上送交易状态
				//m_rf.set_fd(field_cache().INDEX_TXNSTATUS, "0");//设置交易状态为失败
				if ("PS06".equals(disposal)) {
					// 冻结账户
					//log.info("冻结TMS账户");
				}
				if ("PS07".equals(disposal)) {
					if (m_user != null) {
						// 冻结会员
						//log.info("冻结TMS会员");
					}
				}
			}*/
			m_ie.pin_time(io_env.INDEX_TRANS_STRATEGY_END);
			m_ie.setData("disposal", disposal);
			StringBuffer hitRules = new StringBuffer(512);
			if (m_hit_rules != null) {
				for (db_rule_hit hit_rule : m_hit_rules) {
					hitRules.append(hit_rule.dr.toString()).append(",");
				}
				if (hitRules.length() > 0)
					hitRules.setLength(hitRules.length() - 1);
			}
			m_ie.setData("hitRules", hitRules.toString());
			if (get_txn_status() != null) {
				run_stats.do_stat(this);
				StringBuffer actionInfo = new StringBuffer(512);
				for (db_action action : m_actions) {
					actionInfo.append(action.toString()).append(',');
				}
				if (actionInfo.length() > 0)
					actionInfo.setLength(actionInfo.length() - 1);

				m_ie.setData("actionInfo", actionInfo.toString());
			}
			//			update_session_info();
			m_rf.init_stat_data();
			m_ie.setData("hitRuleNum", m_rf.get_fd(m_txn.g_dc.field().INDEX_HITRULENUM));
			m_ie.setData("trigRuleNum", m_rf.get_fd(m_txn.g_dc.field().INDEX_TRIGRULENUM));
			m_ie.setData("score", m_rf.get_fd(m_txn.g_dc.field().INDEX_SCORE));
			m_ie.setData("txnId", m_rf.get_fd(m_txn.g_dc.field().INDEX_TXNID));
			m_ie.setData("txnName", m_txn.get_tab().tab_desc);
			m_ie.setData("transcode", m_rf.get_fd(m_txn.g_dc.field().INDEX_TXNCODE));
			m_ie.setData("strategyInfo", "strategyName:" + m_strategy.st_name + ", strategyTxn:" + m_strategy.st_txn + ", strategyId:" + m_strategy.st_id + ", strategyCond:" + m_strategy.eval_cond);
		}
		if (str_tool.is_empty(this.get_txn_code()))
			return;
		if (db_device.IS_DEVFINGER_ON) {
			//			System.out.println(db_cache.get().application());
			//			System.out.println(db_cache.get().application());
			//			System.out.println(db_cache.get().application().get(m_device.app_id).cookiename);
			//			m_ie.setData("cookieName", db_cache.get().application().get(m_device.app_id).cookiename);//db_cache.get().application().get(get_txn_chancode()).cookiename);
			m_ie.setData("cookieName", DFP_COOKIE_NAME);
			m_ie.setData("devToken", this.m_device_token);
			//			save_device_data_sycn(dc);
		}
		save_txn_data_ansync(dc);
	}

	/*private void init_session_info() {
		if (m_session == null)
			return;
	
		m_session.sessionid = str_tool.to_str(this.get_fd_value(field_cache().INDEX_SESSIONID));
		m_session.userid = str_tool.to_str(this.get_fd_value(field_cache().INDEX_USERID));
		m_session.starttime = ((Number) this.get_fd_value(field_cache().INDEX_TXNTIME)).longValue();
	}
	
	private void update_session_info() {
		if (m_session == null)
			return;
	
		m_session.deviceid = str_tool.to_str(this.get_fd_value(field_cache().INDEX_DEVID));
		m_session.citycode = str_tool.to_str(this.get_fd_value(field_cache().INDEX_CITY));
		m_session.regioncode = str_tool.to_str(this.get_fd_value(field_cache().INDEX_REGION));
		m_session.countrycode = str_tool.to_str(this.get_fd_value(field_cache().INDEX_COUNTRY));
		m_session.ipaddr = str_tool.to_str(this.get_fd_value(field_cache().INDEX_IP));
	}*/

	public String get_txn_status() {
		return str_tool.to_str(m_rf.get_fd(field_cache().INDEX_TXNSTATUS));
	}

	public txn get_txn() {
		return m_txn;
	}

	// 返回交易属性值
	public Object get_fd_value(int local_index) {
		return m_rf.get_fd(local_index);
	}

	public Object get_fd_value(String reg_name) {
		int local_index = m_txn.get_field_localindex(reg_name);
		return m_rf.get_fd(local_index);
	}

	public db_strategy get_strategy() {
		for (int i = m_txn.m_strategy.size() - 1; i >= 0; i--) {
			db_strategy st = m_txn.m_strategy.get(i);
			if (!st.st_enable)
				continue;
			if (str_tool.is_empty(st.eval_cond) || func_map.is_true(st.node.exec(this)))
				return st;
		}
		return new db_strategy(m_txn.m_tab, db_cache.get().rule());
	}

	//	public boolean bank_switch_is_on() throws SQLException {
	//		if (m_user == null)
	//			return true;
	//
	//		linear<db_acc> ac_list = m_user.acc_list;
	//		if (ac_list == null || ac_list.size() == 0)
	//			return true;
	//
	//		linear<db_bank_user_switch> sw_list = db_cache.get().bank_user_switch().getAllList_();
	//		if (sw_list == null || sw_list.size() == 0)
	//			return true;
	//
	//		boolean is_on = true;
	//		String risk_level = m_user.risk_level;
	//		String txnid = (String) m_rf.get_fd(m_txn.g_dc.field().INDEX_TXNID);
	//		for (db_acc db_acc : ac_list) {
	//			String accountid = db_acc.accountid;
	//			db_ref_row acc_row = m_dc.stmt_account.read(accountid);
	//			String bank_code = acc_row.get("BANK_CODE");
	//			for (db_bank_user_switch sw : sw_list) {
	//				if (sw.status == 1)//开启
	//					continue;
	//				if (sw.txnid.equals(txnid) && sw.bank_code.equals(bank_code) && String.valueOf(sw.risk_level).equals(risk_level)) {
	//					is_on = false;
	//					break;
	//				}
	//			}
	//			if (!is_on)
	//				break;
	//		}
	//
	//		return is_on;
	//	}

	//	public void save_device_data_sycn(dao_combin dc) throws SQLException
	//	{
	//		/**
	//		 * 当该交易为风险确认且认证状态为成功时
	//		 * 更新设备指纹信息和用户设备关联状态
	//		 */
	//		if (this.m_device != null) {
	//			
	//			if (this.is_confirm() && "1".equals(get_auth_status()))
	//			{
	//				m_ie.pin_time(io_env.INDEX_TRANS_USER_DEVICE_BG);
	//			    String batch_code = null, error = null;
	//			    long exec_count = 0;
	//			    if (db_device.isDevSyncLog) {
	//			        batch_code = dataSyncLog.get_batch_code(get_thread_name(), tm_tool.lctm_ms());
	//			        dataSyncLog.batch_print_begin(batch_code);
	//			    }
	//				try {
	//					db_device device = dc.stmt_device.read(this.m_device.device_id);
	//					this.m_device.set_indb(device.is_indb());
	//					//更新设备信息
	//					if (!str_tool.is_empty(this.m_device.prop_values)
	//							&& !this.m_device.prop_values.equals(device.prop_values))
	//					{
	//					    exec_count++;
	//						dc.stmt_device.update(batch_code, this.m_device);
	//					}
	//					//更新设备用户关联信息
	//					db_device_user device_user = dc.stmt_device_user.read(this.m_device.device_id, this.get_user_id());
	//					if (!device_user.is_indb())
	//					{
	//						device_user.device_id = this.m_device.device_id;
	//						device_user.user_id = this.get_user_id();
	//					}
	//					if (this.m_stark_device != null)
	//					{
	//						//删除此设备和用户的绑定关系，同时检查此设备是否
	//						//只于该用户进行了绑定，如果是则删除此设备
	//					    exec_count++;
	//						db_device_user stark_device_user = new db_device_user();
	//						stark_device_user.device_id = this.m_stark_device.device_id;
	//						stark_device_user.user_id = this.get_user_id();
	//						dc.stmt_device_user.delete(batch_code, stark_device_user);
	//						linear<db_device_user> device_users = dc.stmt_device_user.read_list(this.m_stark_device.device_id);
	//						if (device_users.size() == 1)
	//						{
	//							if (this.get_user_id().equals(device_users.get(0).user_id))
	//							{
	//							    exec_count++;
	//								dc.stmt_device.delete(batch_code, this.m_stark_device);
	//							}
	//						}
	//					}
	//					exec_count++;
	//					device_user.status = 1;
	//					dc.stmt_device_user.update(batch_code, device_user);
	//					dc.stmt_device_user.flush();
	//					dc.stmt_device.flush();
	//					dc.stmt_account.commit();
	//				} catch (SQLException e) {
	//				    error = null;
	//					dc.stmt_device.reset_update_pos();
	//					dc.stmt_device_user.reset_update_pos();
	//					dc.stmt_account.rollback();
	//					throw e;
	//				} finally {
	//				    if (db_device.isDevSyncLog) {
	//				        dataSyncLog.batch_print_end(batch_code, exec_count,
	//				                (error == null ? dataSyncLog.Action.COMMIT : dataSyncLog.Action.ROLLBACK));
	//				    }
	//				    m_ie.pin_time(io_env.INDEX_TRANS_USER_DEVICE_END);
	//				}
	//			}
	//		}
	//	}

	public void save_txn_data_ansync(dao_combin dc2) throws Exception {

		m_rf.m_env = this;
		cn.com.higinet.tms.base.entity.Traffic traffic = new cn.com.higinet.tms.base.entity.Traffic();
		m_rf.set_fd(field_cache().INDEX_FINISHTIME, tm_tool.lctm_ms());
		//		if (StaticParameters.TXN_STATUS_SUCCESS.equals(this.get_txn_status())) {
		//			m_rf.update_ref_tabdata(dc, dc2); //更新签约数据移到引擎外部
		//		}
		if (this.is_confirm()) {
			//added by wujw,20160108,挂起交易重新发送确认时更新处置/PSTATUS start
			String disposal = m_channel_data.get("DISPOSAL");
			if (null != disposal && disposal.trim().length() > 0) {
				m_rf.set_fd(this.field_cache().INDEX_DISPOSAL, disposal);
			}
			String psStatus = m_channel_data.get("PSSTATUS");
			if (null != psStatus && psStatus.trim().length() > 0) {
				m_rf.set_fd(this.field_cache().INDEX_PSSTATUS, psStatus);
			} //added by wujw,20160108,挂起交易重新发送确认时更新处置/PSTATUS end
		} else {
			alarm_assing_by_disposal();//评估才走分派,确认无需再分派
			if (m_hit_rules != null && !m_hit_rules.isEmpty()) {
				/*********规则触发信息发送到kafka**********/
				List<tms_run_ruletrig> rs = new ArrayList<>();
				m_hit_rules.stream().forEach(r -> {
					tms_run_ruletrig ruleTrig = new tms_run_ruletrig();
					ruleTrig.setCreateTime(System.currentTimeMillis());
					ruleTrig.setMessage(r.message);
					ruleTrig.setNumTimes((long) r.numtimes);
					ruleTrig.setRuleScore(Double.valueOf(r.rule_score).longValue());
					ruleTrig.setTxnCode(r.txncode);
					ruleTrig.setTxnType(r.txntype);
					ruleTrig.setRuleId((long) r.ruleid);
					rs.add(ruleTrig);
				});
				traffic.setRuletrigs(rs);
			}
			if (m_hit_rule_acts != null && !m_hit_rule_acts.isEmpty()) {
				/*********规则Action触发信息发送到kafka**********/
				List<tms_run_rule_action_hit> rs = new ArrayList<>();
				m_hit_rule_acts.stream().forEach(r -> {
					tms_run_rule_action_hit ruleActionHit = new tms_run_rule_action_hit();
					ruleActionHit.setAcCond(r.ac_cond);
					ruleActionHit.setCreateTime(System.currentTimeMillis());
					ruleActionHit.setAcId((long) r.acid);
					ruleActionHit.setLoacExpr(r.ac_expr);
					ruleActionHit.setTxnCode(r.txncode);
					ruleActionHit.setTxnType(r.txntype);
					ruleActionHit.setRuleId((long) r.ruleid);
					rs.add(ruleActionHit);
				});
				traffic.setRuleActionHit(rs);
			}
		}
		traffic.setTarffic(trafficdata.list2map(m_rf));
		//***************************************************************************************************
		EventContext event = new EventContext(Topics.TO_KAFKA);
		event.setData(Params.KAFKA_TOPIC, Constants.Kafka.Topic.TRAFFIC);
		event.setData(Params.KAFKA_DATA, traffic);
		event.setData(Params.KAFKA_USE_PARTITION, true);
		event.setData(Params.KAFKA_PARTITION_KEY, get_dispatch());
		eventBus.publish(event);
		trafficdata.save(m_rf, null);
	}

	public String eval_strategy() {
		// 0 失败 1成功 -1未知
		m_hit_rules = new java.util.ArrayList<db_rule_hit>(16);
		m_hit_rule_acts = new java.util.ArrayList<db_rule_action_hit>(32);
		String disposal = new run_strategy(this).risk_disposal();
		/*if (m_session != null) {
			m_session.finishtime = ((Number) m_rf.get_fd(this.field_cache().INDEX_TXNTIME)).longValue();
		}*/
		return disposal;
	}

	private void alarm_assing_by_disposal() {
		String disposal = str_tool.to_str(m_rf.get_fd(field_cache().INDEX_DISPOSAL));
		String ps_status = str_tool.to_str(m_rf.get_fd(field_cache().INDEX_PSSTATUS));
		if (!str_tool.is_empty(ps_status) && !"00".equals(ps_status))//处理状态不为空且不是未分派状态，说明已经处理过，不再进行处理
			return;
		node cond = get_txn().get_assign_cond(disposal);
		if (cond != null && func_map.is_true(cond.exec(this))) {
			m_rf.set_fd(field_cache().INDEX_PSSTATUS, "01");//待分派状态
		} else {
			m_rf.set_fd(field_cache().INDEX_PSSTATUS, "00");//未分派状态
		}
	}

	public int id() {
		return m_txn.id();
	}

	public db_fd get_fd_def(int localid) {
		return m_txn.get_fd(localid);
	}

	public linear<txn_ref_stat> get_ref_stat() {
		return m_txn.get_ref_stat();
	}

	public Object get_stat_value(int n1, Object cur_val) {
		return m_rs.get_val(n1, (int) (txn_time / 1000 / 60), cur_val);
	}

	public String get_txntype() {
		db_tab tab = m_txn.get_tab();
		return tab.tab_name;
	}

	public String get_txn_chancode() {
		return m_txn.get_tab().chann;
	}

	public String get_chan_chancode() {
		String chancode = str_tool.to_str(m_channel_data.get("CHANCODE"));
		if (str_tool.is_empty(chancode)) {
			chancode = get_txn_chancode();
		}
		return chancode;
	}

	final public long get_txn_time() {
		return txn_time;
	}

	final public void set_txn_time(long txn_time) {
		this.txn_time = txn_time;
	}

	final public int get_txn_minute() {
		return (int) (txn_time / 1000 / 60);
	}

	public Object eval_diff(String fieldName) {
		return null;
	}

	public List<db_rule_hit> get_hit_rules() {
		return m_hit_rules;
	}

	public List<db_rule_action_hit> get_hit_rule_acts() {
		if (m_hit_rule_acts == null)
			m_hit_rule_acts = new ArrayList<db_rule_action_hit>(16);
		return m_hit_rule_acts;
	}

	/*public db_session get_session() {
		return m_session;
	}*/

	public boolean is_confirm() {
		return m_is_confirm;
	}

	public String get_auth_status() {
		return this.m_channel_data.get(auth_status_fdname);
	}

	public run_txn_values get_fd_value() {
		return m_rf;
	}

	public void set_fd_value(run_txn_values ret) {
		this.m_rf = ret;
	}

	public Object last_field(int fieldId) {
		if (m_last_rf == null)
			return null;

		return m_last_rf.get_fd(fieldId);
	}

	public boolean is_init_ok() {
		return this.init_ok;
	}

	public String get_user_id() {
		return str_tool.to_str(m_channel_data.get("USERID"));
	}

	public String get_dispatch() {
		return str_tool.to_str(m_channel_data.get("DISPATCH"));
	}

	public String get_txn_code() {
		return str_tool.to_str(m_rf.get_fd(field_cache().INDEX_TXNCODE));
	}

	/*	public db_user get_user() {
			return m_user;
		}*/

	public db_fd.cache field_cache() {
		return m_txn.g_dc.field();
	}

	public Object get_chan_value(int n1) {
		str_id si = this.m_txn.m_fd_srcid.get(n1);
		if (si == null)
			return null;

		return this.m_channel_data.get(si.s);
	}

	//	public void set_device(db_device device)
	//	{
	//		this.m_device = device;
	//	}
	//
	//	public db_device get_device()
	//	{
	//		return this.m_device;
	//	}
	//	
	//	public void set_stark_device(db_device device)
	//	{
	//		this.m_stark_device = device;
	//	}
	//	
	//	public db_device get_stark_device()
	//	{
	//		return this.m_stark_device;
	//	}

	public double get_device_status() {
		return m_device_status;
	}

	public void set_device_status(double device_status) {
		this.m_device_status = device_status;
	}

	public void set_txn_model(db_tab model) {
		this.model = model;
	}

	public db_tab get_txn_model() {
		return model;
	}

	public String get_thread_name() {
		return thread_name;
	}

	public void set_thread_name(String thread_name) {
		this.thread_name = thread_name;
	}

	public dao_combin get_dao_combin() {
		return m_dc;
	}

	public String get_device_token() {
		return m_device_token;
	}

	public void set_device_token(String m_device_token) {
		this.m_device_token = m_device_token;
	}

	public double get_user_device_status() {
		return m_user_device_status;
	}

	public void set_user_device_status(double m_user_device_status) {
		this.m_user_device_status = m_user_device_status;
	}

}