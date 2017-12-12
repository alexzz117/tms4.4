package cn.com.higinet.tms35.manage.alarm.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.cmc.util.StringUtil;
import cn.com.higinet.rapid.base.dao.Order;
import cn.com.higinet.rapid.base.dao.Page;
import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.tms35.manage.alarm.service.AlarmEventService;
import cn.com.higinet.tms35.manage.alarm.service.AlarmQueueService;
import cn.com.higinet.tms35.manage.alarm.service.MonitorStatService;
import cn.com.higinet.tms35.manage.alarm.service.SendMessageService;
import cn.com.higinet.tms35.manage.common.PropertiesUtil;
import cn.com.higinet.tms35.manage.common.SequenceService;
import cn.com.higinet.tms35.manage.common.StaticParameters;
import cn.com.higinet.tms35.manage.common.util.CalendarUtil;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.exception.TmsMgrServiceException;
import cn.com.higinet.tms35.manage.mgr.service.RiskCaseService;
import cn.com.higinet.tms35.manage.mgr.service.ServerService;
import cn.com.higinet.tms35.manage.tran.TransCommon;

import com.sfpay.sypay.risk.proxy.SypayTransactionAuditService;

@Service("alarmEventService")
public class AlarmEventServiceImpl implements AlarmEventService {

	private static Log log = LogFactory.getLog(AlarmEventServiceImpl.class);

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private SimpleDao tmpSimpleDao;

	@Autowired
	private DataSource tmpTmsDataSource;

	@Autowired
	private SimpleDao officialSimpleDao;

	@Autowired
	private SequenceService sequenceService;

	@Autowired
	private MonitorStatService monitorStatService;

	@Autowired
	private AlarmQueueService alarmQueueService;

	@Autowired
	private SendMessageService sendMessageService;

	@Autowired
	private ServerService serverService;

	@Autowired
	private RiskCaseService riskCaseService;

	@Autowired
	private SypayTransactionAuditService sypayTransactionAuditService;

	private String alarmAssignFuncId;// 报警事件分派菜单ID

	private String alarmProcessFuncId;// 报警事件处理菜单ID

	private String alarmAuditFuncId;// 报警事件审核菜ID

	@PostConstruct
	public void init() {
		PropertiesUtil propertiesUtil = PropertiesUtil.getPropInstance();
		alarmAssignFuncId = propertiesUtil.get(ALARM_AS_FUNC_ID);
		alarmProcessFuncId = propertiesUtil.get(ALARM_PS_FUNC_ID);
		alarmAuditFuncId = propertiesUtil.get(ALARM_AD_FUNC_ID);
	}

	@Override
	public Map<String, Object> getTrafficDataForAlarmProcessInfo(Map<String, String> cond) {
		String sql = "select TXNCODE, CHANCODE, TXNID, TXNTYPE, USERID, COUNTRYCODE, REGIONCODE, CITYCODE, TXNTIME, " + "DISPOSAL, MODELID, STRATEGY, PSSTATUS, ASSIGNID, ASSIGNTIME, OPERID, OPERTIME, AUDITID, AUDITTIME, "
				+ "FRAUD_TYPE,M_NUM2 from TMS_RUN_TRAFFICDATA t where TXNCODE = :TXN_CODE";
		List<Map<String, Object>> list = tmpSimpleDao.queryForList(sql, cond);
		if (list == null || list.isEmpty())
			return null;
		return list.get(0);
	}

	@Override
	public List<Map<String, Object>> getTrafficDataByUserid(Map<String, Object> cond) {
		String sql = "select TXNCODE, CHANCODE, TXNID, TXNTYPE, USERID, COUNTRYCODE, REGIONCODE, CITYCODE, TXNTIME, " + "DISPOSAL, MODELID, STRATEGY, PSSTATUS, ASSIGNID, ASSIGNTIME, OPERID, OPERTIME, AUDITID, AUDITTIME, "
				+ "FRAUD_TYPE from TMS_RUN_TRAFFICDATA t where (t.psstatus = '02' or t.psstatus = '04') and t.DISPOSAL!='PS04' and USERID = :USERID and OPERID = :OPERID";
		return tmpSimpleDao.queryForList(sql, cond);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateTransProcessInfo(Map<String, Object> cond) {
		String sql = "update TMS_RUN_TRAFFICDATA set ";
		StringBuffer sb = new StringBuffer();
		if (MapUtil.isContainsKey(cond, "PSSTATUS")) {
			sb.append("PSSTATUS = :PSSTATUS");
		}
		if (MapUtil.isContainsKey(cond, "ASSIGNID")) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("ASSIGNID = :ASSIGNID");
		}
		if (MapUtil.isContainsKey(cond, "ASSIGNTIME")) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("ASSIGNTIME = :ASSIGNTIME");
		}
		if (MapUtil.isContainsKey(cond, "OPERID")) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("OPERID = :OPERID");
		}
		if (MapUtil.isContainsKey(cond, "OPERTIME")) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("OPERTIME = :OPERTIME");
		}
		if (MapUtil.isContainsKey(cond, "AUDITID")) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("AUDITID = :AUDITID");
		}
		if (MapUtil.isContainsKey(cond, "AUDITTIME")) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("AUDITTIME = :AUDITTIME");
		}
		if (MapUtil.isContainsKey(cond, "FRAUD_TYPE")) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("FRAUD_TYPE = :FRAUD_TYPE");
		}
		if (MapUtil.isContainsKey(cond, "ISCORRECT")) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("ISCORRECT = :ISCORRECT");
		}
		sb.append(" where TXNCODE = :TXN_CODE");
		sql += sb.toString();
		tmpSimpleDao.executeUpdate(sql, cond);
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> addAlarmProcessInfo(Map<String, String> cond, HttpServletRequest request) {
		long psId = sequenceService.getSequenceId("SEQ_TMS_MGR_ALARM_PROCESS", tmpTmsDataSource);
		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute("OPERATOR");
		Map<String, Object> map = new HashMap<String, Object>();
		String fraud_type = MapUtil.getString(cond, "FRAUD_TYPE");
		String short_action = MapUtil.getString(cond, "SHORT_ACTION");
		cond.put("FRAUD_TYPE", fraud_type);
		cond.put("SHORT_ACTION", short_action);
		map.putAll(cond);
		map.put("PS_ID", psId);
		map.put("PS_OPERID", operator.get("OPERATOR_ID"));
		map.put("PS_TIME", System.currentTimeMillis());
		String sql = "insert into TMS_MGR_ALARM_PROCESS(PS_ID, TXN_CODE, PS_OPERID, PS_TYPE, PS_RESULT, PS_INFO, PS_TIME,FRAUD_TYPE,SHORT_ACTION)" + "values(:PS_ID, :TXN_CODE, :PS_OPERID, :PS_TYPE, :PS_RESULT, :PS_INFO, :PS_TIME, :FRAUD_TYPE, :SHORT_ACTION)";
		tmpSimpleDao.executeUpdate(sql, map);
		return map;
	}

	@Override
	public List<Map<String, Object>> getAlarmProcessActions(Map<String, String> cond) {
		String sql = "select AC_ID, TXN_CODE, AC_NAME, AC_COND, AC_COND_IN, AC_EXPR, AC_EXPR_IN,AC_TYPE, CREATE_TIME, " + "EXECUE_TIME, EXECUE_RESULT, EXECUE_INFO from TMS_MGR_ALARM_ACTION where TXN_CODE = :TXN_CODE AND AC_TYPE =  :AC_TYPE";
		List<Map<String, Object>> list = tmpSimpleDao.queryForList(sql, cond);
		if (list == null || list.isEmpty())
			return null;
		return list;
	}

	@Override
	public Map<String, Object> addAlarmProcessAction(Map<String, String> map) {
		long acId = sequenceService.getSequenceId("SEQ_TMS_MGR_ALARM_ACTION", tmpTmsDataSource);
		Map<String, Object> _map_ = new HashMap<String, Object>();
		_map_.putAll(map);
		_map_.put("AC_ID", acId);
		_map_.put("CREATE_TIME", System.currentTimeMillis());
		String sql = "insert into TMS_MGR_ALARM_ACTION(AC_ID, TXN_CODE, AC_NAME, AC_COND, AC_COND_IN, AC_EXPR, AC_EXPR_IN, CREATE_TIME, AC_TYPE)" + "values(:AC_ID, :TXN_CODE, :AC_NAME, :AC_COND, :AC_COND_IN, :AC_EXPR, :AC_EXPR_IN, :CREATE_TIME, :AC_TYPE)";
		tmpSimpleDao.executeUpdate(sql, _map_);
		return _map_;
	}

	public void delOneAlarmProcessAction(Map<String, String> cond) {
		String sql = "delete from TMS_MGR_ALARM_ACTION where AC_ID = ?";
		tmpSimpleDao.executeUpdate(sql, MapUtil.getString(cond, "AC_ID"));
	}

	@Override
	public void delAlarmProcessAction(Map<String, String> cond) {
		String sql = "delete from TMS_MGR_ALARM_ACTION where AC_ID = :AC_ID";
		String psAction = cond.get("psActs");
		String[] psActions = psAction.split("\\,");
		List<Map<String, ?>> list = new ArrayList<Map<String, ?>>(psActions.length);
		for (String acId : psActions) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("AC_ID", acId);
			list.add(map);
		}
		tmpSimpleDao.batchUpdate(sql, list);
	}

	@Override
	public List<Map<String, Object>> getAlarmProcessInfoList(Map<String, String> cond) {
		String sql = "select p.PS_ID, p.TXN_CODE, p.PS_OPERID, p.PS_TYPE, p.PS_RESULT, p.PS_INFO, p.PS_TIME, p.SHORT_ACTION, o.LOGIN_NAME " + "from TMS_MGR_ALARM_PROCESS p left join CMC_OPERATOR o on p.PS_OPERID = o.OPERATOR_ID where p.TXN_CODE = :TXN_CODE order by p.PS_ID";
		List<Map<String, Object>> list = tmpSimpleDao.queryForList(sql, cond);
		if (list == null || list.isEmpty())
			return null;

		List<Map<String, Object>> func_list = shortActionList(null);
		Map<String, String> func_map = new HashMap<String, String>();
		for (Map<String, Object> map2 : func_list) {
			func_map.put(MapUtil.getString(map2, "value"), MapUtil.getString(map2, "text"));
		}
		for (Map<String, Object> map : list) {
			long psTime = MapUtil.getLong(map, "PS_TIME");
			map.put("PS_TIME", CalendarUtil.parseTimeMillisToDateTime(psTime, CalendarUtil.FORMAT14.toPattern()));

			String short_act = MapUtil.getString(map, "SHORT_ACTION");
			if (short_act.length() == 0)
				continue;
			String short_act_name = "";
			String[] acts = short_act.split(",");
			for (String act : acts) {
				if (short_act_name.length() > 0)
					short_act_name += ",";
				short_act_name += MapUtil.getString(func_map, act);
			}
			map.put("SHORT_ACTION", short_act_name);
		}
		return list;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void alarmProcess(Map<String, String> cond, HttpServletRequest request) {

		/*
		 * // 通过客户号查询非挂起的待处理的交易 Map<String, Object> transMap1 = getTrafficDataForAlarmProcessInfo(cond);
		 * 
		 * // 挂起 if ("PS04".equals(MapUtil.getString(transMap1, "DISPOSAL"))) { oneAlarmProcess(cond, request); return; }
		 * 
		 * // 非挂起类报警事件,当某审核员处理完其中某一会员的一例报警事件后，该会员的其余报警事件自动得到相同处理 List<Map<String, Object>> user_trans = getTrafficDataByUserid(transMap1); for (Map<String, Object> map1 : user_trans) { cond.put("TXN_CODE", MapUtil.getString(map1, "TXNCODE")); oneAlarmProcess(cond, request); }
		 */

		oneAlarmProcess(cond, request);
	}

	private void oneAlarmProcess(Map<String, String> cond, HttpServletRequest request) {
		Map<String, Object> transMap = getTrafficDataForAlarmProcessInfo(cond);
		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute("OPERATOR");
		String txnOperId = MapUtil.getString(transMap, "OPERID");
		String txnPsStatus = MapUtil.getString(transMap, "PSSTATUS");
		String sessOperId = MapUtil.getString(operator, "OPERATOR_ID");
		if (!txnOperId.equals(sessOperId)) {
			txnOperId = sessOperId;
			// throw new TmsMgrServiceException("当前交易的处理人员不匹配, 请刷新页面数据.");
		}
		if (!("02".equals(txnPsStatus) || "04".equals(txnPsStatus))) {
			throw new TmsMgrServiceException("当前交易处理状态不符合处理条件, 请刷新页面数据.");
		}
		Map<String, Object> infoMap = addAlarmProcessInfo(cond, request);

		/************ 增加快捷动作开始 ************/
		addShortAction(cond);
		/************ 增加快捷动作结束 ************/

		Map<String, Object> map = new HashMap<String, Object>();
		map.putAll(cond);
		map.put("OPERTIME", infoMap.get("PS_TIME"));
		map.put("OPERID", txnOperId);
		monitorStatService.updateAlarmProcessOperatorStat(txnOperId, MapUtil.getLong(transMap, "ASSIGNTIME"), StringUtil.isBlank(MapUtil.getString(transMap, "ASSIGNID")), true);
		updateTransProcessInfo(map);
	}

	private void addShortAction(Map<String, String> cond) {
		String short_act = MapUtil.getString(cond, "SHORT_ACTION");
		String txn_code = MapUtil.getString(cond, "TXN_CODE");
		String paus_js_time = MapUtil.getString(cond, "PAUS_JS_TIME");

		// 通过流水号查询当前快捷处理动作
		Map<String, String> ac_cond = new HashMap<String, String>();
		ac_cond.put("TXN_CODE", txn_code);
		ac_cond.put("AC_TYPE", "2");// 快捷动作
		List<Map<String, Object>> short_ac_list = getAlarmProcessActions(ac_cond);

		if (short_act.length() == 0 && (short_ac_list == null || short_ac_list.size() == 0)) {
			return;
		}

		if (short_act.length() == 0 && (short_ac_list != null && short_ac_list.size() > 0)) {
			for (Map db_ac : short_ac_list) {
				delOneAlarmProcessAction(db_ac);
			}
			return;
		}

		String[] short_ac_arr = short_act.split(",");

		if (short_ac_list == null || short_ac_list.size() == 0) {
			for (String ac : short_ac_arr) {
				addShortAct(txn_code, paus_js_time, ac);
			}
			return;
		}

		for (String ac : short_ac_arr) {
			boolean isFind = false;
			for (Map db_ac : short_ac_list) {
				String ac_expr = MapUtil.getString(db_ac, "AC_EXPR");
				if (ac_expr.length() == 0) {
					continue;
				}
				if (ac.equals(ac_expr.substring(0, ac_expr.indexOf("(")))) {
					isFind = true;
					break;
				}
			}
			if (!isFind) {
				addShortAct(txn_code, paus_js_time, ac);
			}
		}

		for (Map db_ac : short_ac_list) {
			boolean isFind = false;
			String ac_expr = MapUtil.getString(db_ac, "AC_EXPR");
			if (ac_expr.length() == 0)
				continue;

			for (String ac : short_ac_arr) {
				if (ac.equals(ac_expr.substring(0, ac_expr.indexOf("(")))) {
					isFind = true;
					break;
				}
			}
			if (!isFind)
				delOneAlarmProcessAction(db_ac);
		}
	}

	private void addShortAct(String txn_code, String paus_js_time, String ac) {
		Map<String, String> ac_data = new HashMap<String, String>();
		ac_data.put("TXN_CODE", txn_code);
		ac_data.put("AC_NAME", "");
		ac_data.put("AC_COND", "");
		ac_data.put("AC_COND_IN", "");
		ac_data.put("AC_EXPR_IN", "");
		ac_data.put("AC_TYPE", "2");// 快捷动作
		ac_data.put("AC_EXPR", ac.equals("pause_js") ? ac + "(\"" + paus_js_time + "\")" : ac + "()");
		addAlarmProcessAction(ac_data);
	}

	@Override
	@SuppressWarnings({ "unchecked", "serial" })
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public int alarmAudit(Map<String, String> cond, HttpServletRequest request) {
		int result = 1;// 默认返回成功
		String txnstatus = "1";// 默认成功

		Map<String, Object> transMap = getTrafficDataForAlarmProcessInfo(cond);

		String txncode = MapUtil.getString(transMap, "TXNCODE");
		String txnid = MapUtil.getString(transMap, "TXNID");

		String psStatus = MapUtil.getString(transMap, "PSSTATUS");
		if (!"03".equals(psStatus)) {
			throw new TmsMgrServiceException("当前交易处理状态不符合审核条件, 请刷新页面数据.");
		}

		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute("OPERATOR");

		String psResult = cond.get("PS_RESULT");
		if ("0".equals(psResult)) {// 审核不通过

			// 更新操作员工作量统计信息
			monitorStatService.updateAlarmProcessOperatorStat(MapUtil.getString(transMap, "OPERID"), MapUtil.getLong(transMap, "ASSIGNTIME"), StringUtil.isBlank(MapUtil.getString(transMap, "ASSIGNID")), false);

			Map<String, Object> infoMap = addAlarmProcessInfo(cond, request);// 增加操作历史
			map.putAll(cond);
			map.put("PSSTATUS", "04");
			map.put("AUDITTIME", infoMap.get("PS_TIME"));
			map.put("AUDITID", operator.get("OPERATOR_ID"));
			map.remove("FRAUD_TYPE");// 不更新欺诈
			updateTransProcessInfo(map);

			result = 1;// 成功
		} else if ("1".equals(psResult)) {// 审核通过

			String disposal = "PS01";
			Map<String, Object> processMap = getLastAlarmProcessInfo(txncode);// 获取最后的处理信息
			if (null != processMap) {
				String shortAction = (String) processMap.get("SHORT_ACTION");
				if (null != shortAction) {

					log.info("----调用前置接口----txncode=" + txncode + "  txnid=" + txnid);
					try {
						if (shortAction.contains(StaticParameters.SHORT_ACTION_PASS)) {// 放行
							txnstatus = "1";
							disposal = "PS01";
							result = sypayTransactionAuditService.ps01(txncode, txnid);
						} else if (shortAction.contains(StaticParameters.SHORT_ACTION_BLOCK)) {// 阻断
							txnstatus = "0";
							disposal = "PS05";
							result = sypayTransactionAuditService.ps05(txncode, txnid);
						}
					} catch (Exception e) {
						result = 0;
						log.error("调用前置接口出错.", e);
					}
					log.info("----调用前置接口返回结果:" + result);

					// 禁用会员
					// 冻结账户
					// 暂停结算

					// 转案件调查
					if (shortAction.contains(StaticParameters.SHORT_ACTION_CASE)) {
						// result = 0;//转案件
						// (:UUID,'sf001',:OPERATOR_ID,:SRC_TYPE,:USERID,:TXN_CODE,:MOBILE,:AMOUNT,to_date(:CUR_DATE,'YYYY-MM-DD'),:CREATED_DATE,:DESCR,:IS_RE_CHECK,:STATUS

						String userId = MapUtil.getString(transMap, "USERID");
						String mobile = getUserMobile(userId);

						Map<String, String> reqs = new HashMap();
						reqs.put("SRC_TYPE", "1");
						reqs.put("USERID", userId);
						reqs.put("TXNCODE", txncode);
						reqs.put("MOBILE", mobile);
						reqs.put("OPERATOR_ID", (String) operator.get("OPERATOR_ID"));
						// reqs.put("AMOUNT", "3");//不同的交易暂无法确定金额
						riskCaseService.addRiskCase(reqs);
					}
				}
			}

			// 成功才发送交易确认
			if (SypayTransactionAuditService.SUCCESS == result) {
				psStatus = "99";
				List<Map<String, Object>> servList = serverService.listServer(new HashMap<String, String>() {
					{
						put("SERVTYPE", "1");
					}
				});
				try {
					transMap.put("TXNSTATUS", txnstatus);// 确认
					//transMap.put("DISPOSAL", disposal);
					transMap.put("PSSTATUS", psStatus);
					sendMessageService.sendMessage(buildTransactionMap(transMap), "0007", servList);// 发送接口运行处理动作
				} catch (Exception e) {
					log.error("发送到确认接口运行处理动作出错.", e);
				}
				Map<String, Object> infoMap = addAlarmProcessInfo(cond, request);// 增加操作历史
				map.putAll(cond);
				map.put("PSSTATUS", psStatus);// 结束状态
				map.put("AUDITTIME", infoMap.get("PS_TIME"));
				map.put("AUDITID", operator.get("OPERATOR_ID"));
				map.remove("FRAUD_TYPE");// 不更新欺诈
				updateTransProcessInfo(map);
				// 更新操作员工作量统计信息
				monitorStatService.updateAlarmProcessOperatorStat(MapUtil.getString(transMap, "OPERID"), MapUtil.getLong(transMap, "ASSIGNTIME"), StringUtil.isBlank(MapUtil.getString(transMap, "ASSIGNID")), false);
				monitorStatService.modMonitorFraudTypeStat(transMap);

				String fraudType = MapUtil.getString(transMap, "FRAUD_TYPE");
				alarmQueueService.updateRiskStatus(("00".equals(fraudType) ? "0" : "1"), MapUtil.getString(transMap, "TXNCODE"));
			}
		}

		return result;
	}

	private Map<String, Object> buildTransactionMap(Map<String, Object> transMap) {
		//String[] fields = new String[] { "TXNCODE", "CHANCODE", "TXNID", "USERID", "TXNSTATUS", "DISPOSAL", "PSSTATUS" };
		String[] fields = new String[] { "TXNCODE", "CHANCODE", "TXNID", "USERID", "TXNSTATUS", "PSSTATUS" };
		Map<String, Object> transaction = new HashMap<String, Object>();
		for (String fd : fields) {
			transaction.put(fd, MapUtil.getObject(transMap, fd));
		}
		return transaction;
	}

	/**
	 * 获取交易下的报警事件处理信息
	 * 
	 * @param txncode
	 *            交易流水号
	 * @return
	 */
	private Map<String, Object> getLastAlarmProcessInfo(String txncode) {
		String sql = "SELECT PS_ID, TXN_CODE, PS_OPERID, PS_TYPE, PS_RESULT, PS_INFO, PS_TIME, SHORT_ACTION, FRAUD_TYPE FROM TMS_MGR_ALARM_PROCESS  WHERE TXN_CODE = ? ORDER BY PS_TIME DESC";
		List<Map<String, Object>> list = tmpSimpleDao.queryForList(sql, txncode);
		if (list == null || list.isEmpty())
			return null;
		return list.get(0);
	}

	/**
	 * 获取交易下的报警事件处理动作
	 * 
	 * @param txnCode
	 *            交易流水号
	 * @return
	 */
	private String getUserMobile(String userId) {
		String mobile = null;
		String sql = "select MOBILE from TMS_RUN_USER u where u.USERID = ?";
		List<Map<String, Object>> list = tmpSimpleDao.queryForList(sql, userId);
		if (null != list && list.size() > 0) {
			Map<String, Object> m = list.get(0);
			mobile = (String) m.get("MOBILE");
		}
		return mobile;
	}

	/**
	 * 获取交易下的报警事件处理动作
	 * 
	 * @param txnCode
	 *            交易流水号
	 * @return
	 */
	public List<Map<String, Object>> getAlarmProcessActionList(String txnCode) {
		String sql = "select AC_ID, TXN_CODE, AC_NAME, AC_COND, AC_EXPR from TMS_MGR_ALARM_ACTION " + "where TXN_CODE = ? and (EXECUE_RESULT is null or EXECUE_RESULT = '0')";
		sql = "select AC_ID, TXN_CODE, AC_NAME, AC_COND, AC_EXPR from TMS_MGR_ALARM_ACTION where TXN_CODE = ?";
		return tmpSimpleDao.queryForList(sql, txnCode);
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void alarmAssign(Map<String, String> cond, HttpServletRequest request) {
		long currTime = System.currentTimeMillis();
		Map<String, Object> transMap = getTrafficDataForAlarmProcessInfo(cond);
		Map<String, Object> sysParamMap = getSystemParam(ALARM_PS_TIMEOUT);
		// long assignTime = MapUtil.getLong(transMap, "ASSIGNTIME");
		// long timeout = MapUtil.getLong(sysParamMap, "STARTVALUE");
		String psStatus = MapUtil.getString(transMap, "PSSTATUS");
		if (!("00".equals(psStatus) || "02".equals(psStatus) || "04".equals(psStatus))) {
			// && (currTime - assignTime > timeout)))) {
			throw new TmsMgrServiceException("当前交易处理状态不符合分派条件, 请刷新页面数据.");
		}
		Map<String, Object> infoMap = addAlarmProcessInfo(cond, request);
		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute("OPERATOR");
		Map<String, Object> map = new HashMap<String, Object>();
		map.putAll(cond);
		map.put("ASSIGNID", operator.get("OPERATOR_ID"));
		map.put("ASSIGNTIME", infoMap.get("PS_TIME"));
		boolean status = "00".equals(psStatus) ? false : true;
		monitorStatService.updateAlarmAssignOperatorStat(MapUtil.getString(transMap, "OPERID"), MapUtil.getLong(transMap, "ASSIGNTIME"), status, MapUtil.getString(map, "OPERID"), currTime);
		updateTransProcessInfo(map);
	}

	@Override
	public List<Map<String, Object>> getAlarmAssignOperCapacity(Map<String, Object> cond) {
		List<Map<String, Object>> list = this.getAlarmProcessAuthorityOperators();
		if (list == null || list.isEmpty()) {
			throw new TmsMgrServiceException("没有拥有[报警事件处理]权限的人员, 请设置.");
		}
		String operId = MapUtil.getString(cond, "OPERID");// 当前交易的处理人员
		String sql = "select OPERATOR_ID, sum(ASSIGN_NUMBER) ASSIGN_NUMBER, sum(PROCESS_NUMBER) PROCESS_NUMBER, " + "sum(UNPROCESS_NUMBER) UNPROCESS_NUMBER from TMS_MGR_ALARM_OPERATOR_STAT group by OPERATOR_ID order by assign_number,unprocess_number";
		List<Map<String, Object>> statList = tmpSimpleDao.queryForList(sql);
		for (Map<String, Object> operMap : list) {
			String operatorId = MapUtil.getString(operMap, "OPERATOR_ID");
			if (operId.equals(operatorId)) {
				// 将当前交易处理人员设置为无效
				operMap.put("ISENABLE", 0);
			} else {
				operMap.put("ISENABLE", 1);
			}
			boolean hasStat = false;
			if (statList != null && !statList.isEmpty()) {
				for (Map<String, Object> statMap : statList) {
					String statOperId = MapUtil.getString(statMap, "OPERATOR_ID");
					if (operatorId.equals(statOperId)) {
						operMap.putAll(statMap);
						statList.remove(statMap);
						hasStat = true;
						break;
					}
				}
			}
			if (!hasStat) {
				operMap.put("ASSIGN_NUMBER", 0);
				operMap.put("PROCESS_NUMBER", 0);
				operMap.put("UNPROCESS_NUMBER", 0);
			}
		}
		return list;
	}

	/**
	 * 获取系统参数表中配置参数信息
	 * 
	 * @param paramName
	 *            参数名称
	 * @return
	 */
	public Map<String, Object> getSystemParam(String paramName) {
		String sql = "select DATATYPE, VALUETYPE, STARTVALUE, ENDVALUE from TMS_MGR_SYSPARAM where SYSPARAMNAME = ?";
		List<Map<String, Object>> list = officialSimpleDao.queryForList(sql, paramName);
		if (list == null || list.isEmpty())
			return null;
		return list.get(0);
	}

	/**
	 * 获取拥有报警事件分派菜单权限的用户信息
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getAlarmAssingAuthorityOperators() {
		return this.getAlarmFuncAuthorityOperators(alarmAssignFuncId);
	}

	/**
	 * 获取拥有报警事件处理菜单权限的用户信息
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getAlarmProcessAuthorityOperators() {
		return this.getAlarmFuncAuthorityOperators(alarmProcessFuncId);
	}

	/**
	 * 获取拥有报警事件审核菜单权限的用户信息
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getAlarmAuditAuthorityOperators() {
		return this.getAlarmFuncAuthorityOperators(alarmAuditFuncId);
	}

	/**
	 * 获取报警事件处理权限的用户信息
	 * 
	 * @return
	 */
	private List<Map<String, Object>> getAlarmFuncAuthorityOperators(String funcId) {
		// String sql = "SELECT co.OPERATOR_ID, co.LOGIN_NAME FROM cmc_operator co, cmc_operator_role_rel corr, cmc_role_func_rel crfr " +
		// "WHERE co.OPERATOR_ID = corr.OPERATOR_ID AND corr.ROLE_ID = crfr.ROLE_ID AND co.FLAG = '1' AND crfr.FUNC_ID = ?";
		// List<Map<String, Object>> list = tmpSimpleDao.queryForList(sql, funcId);
		String sql = "SELECT co.OPERATOR_ID, co.LOGIN_NAME FROM cmc_operator co where login_name!='admin'";
		List<Map<String, Object>> list = tmpSimpleDao.queryForList(sql);
		return list;
	}

	@Override
	public Map<String, Object> getTransStrategy(String stId) {
		String sql = "select ST_ID, ST_TXN, ST_NAME, EVAL_COND, EVAL_COND_IN, EVAL_MODE, RULE_EXEC_MODE, " + "ST_ENABLE, CREATETIME, MODIFYTIME from TMS_COM_STRATEGY where ST_ID = ?";
		List<Map<String, Object>> list = officialSimpleDao.queryForList(sql, stId);
		if (list == null || list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	@Override
	public List<Map<String, Object>> getTransStrategyRuleEvalList(String stId) {
		String sql = "select SRE_ID, ST_ID, EVAL_TYPE, EVAL_MECH, DIS_STRATEGY, STAT_FUNC, PS_SCORE " + "from TMS_COM_STRATEGY_RULE_EVAL where ST_ID = ? order by EVAL_TYPE";
		return officialSimpleDao.queryForList(sql, stId);
	}

	public List<Map<String, Object>> getTransHitRuleList(String txnCode, String txnType) {
		String ruleHitsql = "select TRIGID, TXNCODE, TXNTYPE, RULEID, MESSAGE, NUMTIMES, " + "CREATETIME, RULE_SCORE from TMS_RUN_RULETRIG where TXNCODE = ?";
		List<Map<String, Object>> trigList = tmpSimpleDao.queryForList(ruleHitsql, txnCode);
		if (trigList == null || trigList.isEmpty()) {
			return null;
		}
		String ruleSql = "select RULE_ID, RULE_NAME, RULE_DESC, RULE_COND, RULE_COND_IN, " + "RULE_SHORTDESC, EVAL_TYPE, DISPOSAL from TMS_COM_RULE where RULE_TXN in (" + TransCommon.arr2str(TransCommon.cutToIds(txnType)) + ")";
		List<Map<String, Object>> ruleList = officialSimpleDao.queryForList(ruleSql);
		if (ruleList == null || ruleList.isEmpty()) {
			return null;
		}
		for (int i = 0, len = trigList.size(); i < len; i++) {
			Map<String, Object> trigMap = trigList.get(i);
			String ruleId = MapUtil.getString(trigMap, "RULEID");
			for (int j = 0, jlen = ruleList.size(); j < jlen; j++) {
				Map<String, Object> ruleMap = ruleList.get(j);
				String rule_id = MapUtil.getString(ruleMap, "RULE_ID");
				if (ruleId.equals(rule_id)) {
					trigMap.putAll(ruleMap);
					break;
				}
			}
		}
		return trigList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.alarm.service.AlarmEventService#getAlarmHisActions(java.util.Map)
	 */
	@Override
	public Page<Map<String, Object>> getAlarmHisActions(Map<String, String> reqs) {
		String sql = "select t.userid,u.cusname,t.txncode,p.fraud_type,p.ps_info,p.short_action,p.ps_time,t.OPERID from tms_run_trafficdata t" + " inner join tms_run_user u on t.userid=u.userid"
				+ " inner join (select max(ps_time) ps_time,p.txn_code from tms_mgr_alarm_process p where p.ps_type=1 group by txn_code) a on a.txn_code = t.txncode" + " inner join tms_mgr_alarm_process  p on p.txn_code=t.txncode and p.ps_time=a.ps_time"
				+ " where t.OPERID = :OPERID and t.userid = :userid";
		Map<String, String> operator = (Map<String, String>) request.getSession().getAttribute("OPERATOR");

		if (operator != null) {
			// Map<String, Object> m = new HashMap<String, Object>();
			reqs.put("OPERID", operator.get("OPERATOR_ID"));
			reqs.put("pagesize", "10");
			Page<Map<String, Object>> pageQuery = tmpSimpleDao.pageQuery(sql, reqs, new Order().desc("ps_time"));
			if (pageQuery == null)
				return null;
			List<Map<String, Object>> func_list = shortActionList(null);
			Map<String, String> func_map = new HashMap<String, String>();
			for (Map<String, Object> map2 : func_list) {
				func_map.put(MapUtil.getString(map2, "value"), MapUtil.getString(map2, "text"));
			}
			List<Map<String, Object>> list = pageQuery.getList();
			for (Map<String, Object> map : list) {
				String short_act = MapUtil.getString(map, "SHORT_ACTION");
				if (short_act.length() == 0)
					continue;
				String short_act_name = "";
				String[] acts = short_act.split(",");
				for (String act : acts) {
					if (short_act_name.length() > 0)
						short_act_name += ",";
					short_act_name += MapUtil.getString(func_map, act);
				}
				map.put("SHORT_ACTION", short_act_name);
			}
			return pageQuery;
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.alarm.service.AlarmEventService#shortActionList(java.util.Map)
	 */
	@Override
	public List<Map<String, Object>> shortActionList(Map<String, String> reqs) {
		String sql = "select func_name text,func_code value from tms_com_func where func_catalog = '8' order by func_orderby";
		return tmpSimpleDao.queryForList(sql);
	}
}