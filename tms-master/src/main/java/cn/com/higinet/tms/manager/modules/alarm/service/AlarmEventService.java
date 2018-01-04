package cn.com.higinet.tms.manager.modules.alarm.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.common.PropertiesUtil;
import cn.com.higinet.tms.manager.modules.common.SequenceService;
import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.mgr.service.RiskCaseService;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;

@Service("alarmEventService")
public class AlarmEventService {

	@Autowired
	private HttpServletRequest request;

	@Autowired
	@Qualifier("offlineSimpleDao")
	private SimpleDao offlineSimpleDao;

	@Autowired
	@Qualifier("offlineDataSource")
	private DataSource tmpTmsDataSource;

	@Autowired
	@Qualifier("onlineSimpleDao")
	private SimpleDao onlineSimpleDao;

	@Autowired
	private SequenceService sequenceService;

	@Autowired
	private MonitorStatService monitorStatService;

	@Autowired
	private RiskCaseService riskCaseService;

	// @Autowired
	// private SypayTransactionAuditService sypayTransactionAuditService;

	private String alarmAssignFuncId;// 报警事件分派菜单ID
	private String alarmProcessFuncId;// 报警事件处理菜单ID
	private String alarmAuditFuncId;// 报警事件审核菜ID

	public static final String ALARM_PS_FUNC_ID = "tms.alarm.process.funcid";
	public static final String ALARM_AS_FUNC_ID = "tms.alarm.assign.funcid";
	public static final String ALARM_AD_FUNC_ID = "tms.alarm.audit.funcid";
	public static final String ALARM_PS_TIMEOUT = "alarmProcessTimeout";

	@PostConstruct
	public void init() {
		PropertiesUtil propertiesUtil = PropertiesUtil.getPropInstance();
		alarmAssignFuncId = propertiesUtil.get(ALARM_AS_FUNC_ID);
		alarmProcessFuncId = propertiesUtil.get(ALARM_PS_FUNC_ID);
		alarmAuditFuncId = propertiesUtil.get(ALARM_AD_FUNC_ID);
	}

	/**
	 * 通过查询交易流水, 获取其中报警处理相关信息
	 * 
	 * @param cond
	 *            查询条件参数值
	 * @return
	 */
	public Map<String, Object> getTrafficDataForAlarmProcessInfo(Map<String, String> cond) {
		String sql = "select TXNCODE, CHANCODE, TXNID, TXNTYPE, USERID, COUNTRYCODE, REGIONCODE, CITYCODE, TXNTIME, "
				+ "DISPOSAL, MODELID, STRATEGY, PSSTATUS, ASSIGNID, ASSIGNTIME, OPERID, OPERTIME, AUDITID, AUDITTIME, "
				+ "FRAUD_TYPE,M_NUM2 from TMS_RUN_TRAFFICDATA t where TXNCODE = :TXN_CODE";
		List<Map<String, Object>> list = offlineSimpleDao.queryForList(sql, cond);
		if (list == null || list.isEmpty())
			return null;
		return list.get(0);
	}

	public List<Map<String, Object>> getTrafficDataByUserid(Map<String, Object> cond) {
		String sql = "select TXNCODE, CHANCODE, TXNID, TXNTYPE, USERID, COUNTRYCODE, REGIONCODE, CITYCODE, TXNTIME, "
				+ "DISPOSAL, MODELID, STRATEGY, PSSTATUS, ASSIGNID, ASSIGNTIME, OPERID, OPERTIME, AUDITID, AUDITTIME, "
				+ "FRAUD_TYPE from TMS_RUN_TRAFFICDATA t where (t.psstatus = '02' or t.psstatus = '04') and t.DISPOSAL!='PS04' and USERID = :USERID and OPERID = :OPERID";
		return offlineSimpleDao.queryForList(sql, cond);
	}

	/**
	 * 更新交易流水中报警处理状态
	 * 
	 * @param modMap
	 *            需要更新的字段/值
	 */
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
		sb.append(" where TXNCODE = :TXNCODE");
		sql += sb.toString();
		offlineSimpleDao.executeUpdate(sql, cond);
	}

	/**
	 * 添加报警处理信息
	 * 
	 * @param cond
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> addAlarmProcessInfo(Map<String, String> cond, HttpServletRequest request) {
		long psId = sequenceService.getSequenceId("SEQ_TMS_MGR_ALARM_PROCESS", tmpTmsDataSource);
		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute("OPERATOR");
		Map<String, Object> map = new HashMap<String, Object>();
		String fraud_type = MapUtil.getString(cond, "FRAUD_TYPE");
		String short_action = MapUtil.getString(cond, "SHORT_ACTION");
		cond.put("FRAUD_TYPE", fraud_type);//欺诈类型编码
		cond.put("SHORT_ACTION", short_action);//处置动作
		map.putAll(cond);
		map.put("PS_ID", psId);
		map.put("PS_OPERID", cond.get("PS_OPERID"));//处理人
		map.put("PS_TIME", System.currentTimeMillis());//处理时间
		String sql = "insert into TMS_MGR_ALARM_PROCESS(PS_ID, TXN_CODE, PS_OPERID, PS_TYPE, PS_RESULT, PS_INFO, PS_TIME,FRAUD_TYPE,SHORT_ACTION)"
				+ "values(:PS_ID, :TXN_CODE, :PS_OPERID, :PS_TYPE, :PS_RESULT, :PS_INFO, :PS_TIME, :FRAUD_TYPE, :SHORT_ACTION)";
		System.out.println(sql);
		offlineSimpleDao.executeUpdate(sql, map);
		return map;
	}

	public List<Map<String, Object>> getAlarmProcessActions(Map<String, String> cond) {
		String sql = "select AC_ID, TXN_CODE, AC_NAME, AC_COND, AC_COND_IN, AC_EXPR, AC_EXPR_IN,AC_TYPE, CREATE_TIME, "
				+ "EXECUE_TIME, EXECUE_RESULT, EXECUE_INFO from TMS_MGR_ALARM_ACTION where TXN_CODE = :TXN_CODE AND AC_TYPE =  :AC_TYPE";
		List<Map<String, Object>> list = offlineSimpleDao.queryForList(sql, cond);
		if (list == null || list.isEmpty())
			return null;
		return list;
	}

	/**
	 * 获取报警处理动作列表信息
	 * 
	 * @param cond
	 *            查询条件
	 * @return
	 */
	public Map<String, Object> addAlarmProcessAction(Map<String, String> map) {
		long acId = sequenceService.getSequenceId("SEQ_TMS_MGR_ALARM_ACTION", tmpTmsDataSource);
		Map<String, Object> _map_ = new HashMap<String, Object>();
		_map_.putAll(map);
		_map_.put("AC_ID", acId);
		_map_.put("CREATE_TIME", System.currentTimeMillis());
		String sql = "insert into TMS_MGR_ALARM_ACTION(AC_ID, TXN_CODE, AC_NAME, AC_COND, AC_COND_IN, AC_EXPR, AC_EXPR_IN, CREATE_TIME, AC_TYPE)"
				+ "values(:AC_ID, :TXN_CODE, :ac_name, :ac_cond, :ac_cond_in, :ac_expr, :ac_expr_in, :CREATE_TIME, :AC_TYPE)";
		offlineSimpleDao.executeUpdate(sql, _map_);
		return _map_;
	}

	public void delOneAlarmProcessAction(Map<String, String> cond) {
		String sql = "delete from TMS_MGR_ALARM_ACTION where AC_ID = ?";
		offlineSimpleDao.executeUpdate(sql, MapUtil.getString(cond, "AC_ID"));
	}
	
	
	/**
	 * 删除报警处理动作
	 * 
	 * @param cond
	 */
	public void delsAlarmProcessAction(List<Map<String, ?>> reqslist) {
		String sql = "delete from TMS_MGR_ALARM_ACTION where AC_ID = :AC_ID";
		offlineSimpleDao.batchUpdate(sql, reqslist);
	}
	
	
	/**
	 * 删除报警处理动作
	 * 
	 * @param cond
	 */
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
		offlineSimpleDao.batchUpdate(sql, list);
	}

	/**
	 * 获取报警处理记录信息列表
	 * 
	 * @param cond
	 * @return
	 */
	public List<Map<String, Object>> getAlarmProcessInfoList(Map<String, String> cond) {
		String sql = "select p.PS_ID, p.TXN_CODE, p.PS_OPERID, p.PS_TYPE, p.PS_RESULT, p.PS_INFO, p.PS_TIME, p.SHORT_ACTION, o.LOGIN_NAME "
				+ "from TMS_MGR_ALARM_PROCESS p left join CMC_OPERATOR o on p.PS_OPERID = o.OPERATOR_ID where p.TXN_CODE = :TXN_CODE order by p.PS_ID";
		List<Map<String, Object>> list = offlineSimpleDao.queryForList(sql, cond);
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

	/**
	 * 报警事件处理
	 * 
	 * @param cond
	 * @param request
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void alarmProcess(Map<String, String> cond, HttpServletRequest request) {

		/*
		 * // 通过客户号查询非挂起的待处理的交易 Map<String, Object> transMap1 =
		 * getTrafficDataForAlarmProcessInfo(cond);
		 * 
		 * // 挂起 if ("PS04".equals(MapUtil.getString(transMap1, "DISPOSAL"))) {
		 * oneAlarmProcess(cond, request); return; }
		 * 
		 * // 非挂起类报警事件,当某审核员处理完其中某一会员的一例报警事件后，该会员的其余报警事件自动得到相同处理 List<Map<String,
		 * Object>> user_trans = getTrafficDataByUserid(transMap1); for (Map<String,
		 * Object> map1 : user_trans) { cond.put("TXN_CODE", MapUtil.getString(map1,
		 * "TXNCODE")); oneAlarmProcess(cond, request); }
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
		monitorStatService.updateAlarmProcessOperatorStat(txnOperId, MapUtil.getLong(transMap, "ASSIGNTIME"),
				CmcStringUtil.isBlank(MapUtil.getString(transMap, "ASSIGNID")), true);
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

	/**
	 * 报警事件审核
	 * 
	 * @param cond
	 * @param request
	 * @return
	 */
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
			monitorStatService.updateAlarmProcessOperatorStat(MapUtil.getString(transMap, "OPERID"),
					MapUtil.getLong(transMap, "ASSIGNTIME"),
					CmcStringUtil.isBlank(MapUtil.getString(transMap, "ASSIGNID")), false);

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

					/*
					 * log.info( "----调用前置接口----txncode=" + txncode + "  txnid=" + txnid ); try {
					 * if( shortAction.contains( StaticParameters.SHORT_ACTION_PASS ) ) {// 放行
					 * txnstatus = "1"; disposal = "PS01"; result =
					 * sypayTransactionAuditService.ps01( txncode, txnid ); } else if(
					 * shortAction.contains( StaticParameters.SHORT_ACTION_BLOCK ) ) {// 阻断
					 * txnstatus = "0"; disposal = "PS05"; result =
					 * sypayTransactionAuditService.ps05( txncode, txnid ); } } catch( Exception e )
					 * { result = 0; log.error( "调用前置接口出错.", e ); } log.info( "----调用前置接口返回结果:" +
					 * result );
					 */

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
			/*
			 * if( SypayTransactionAuditService.SUCCESS == result ) { psStatus = "99";
			 * List<Map<String, Object>> servList = serverService.listServer( new
			 * HashMap<String, String>() { { put( "SERVTYPE", "1" ); } } ); try {
			 * transMap.put( "TXNSTATUS", txnstatus );// 确认 //transMap.put("DISPOSAL",
			 * disposal); transMap.put( "PSSTATUS", psStatus );
			 * sendMessageService.sendMessage( buildTransactionMap( transMap ), "0007",
			 * servList );// 发送接口运行处理动作 } catch( Exception e ) { log.error(
			 * "发送到确认接口运行处理动作出错.", e ); } Map<String, Object> infoMap = addAlarmProcessInfo(
			 * cond, request );// 增加操作历史 map.putAll( cond ); map.put( "PSSTATUS", psStatus
			 * );// 结束状态 map.put( "AUDITTIME", infoMap.get( "PS_TIME" ) ); map.put(
			 * "AUDITID", operator.get( "OPERATOR_ID" ) ); map.remove( "FRAUD_TYPE" );//
			 * 不更新欺诈 updateTransProcessInfo( map ); // 更新操作员工作量统计信息
			 * monitorStatService.updateAlarmProcessOperatorStat( MapUtil.getString(
			 * transMap, "OPERID" ), MapUtil.getLong( transMap, "ASSIGNTIME" ),
			 * CmcStringUtil.isBlank( MapUtil.getString( transMap, "ASSIGNID" ) ), false );
			 * monitorStatService.modMonitorFraudTypeStat( transMap );
			 * 
			 * String fraudType = MapUtil.getString( transMap, "FRAUD_TYPE" );
			 * alarmQueueService.updateRiskStatus( ("00".equals( fraudType ) ? "0" : "1"),
			 * MapUtil.getString( transMap, "TXNCODE" ) ); }
			 */
		}

		return result;
	}

	private Map<String, Object> buildTransactionMap(Map<String, Object> transMap) {
		// String[] fields = new String[] { "TXNCODE", "CHANCODE", "TXNID", "USERID",
		// "TXNSTATUS", "DISPOSAL", "PSSTATUS" };
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
		List<Map<String, Object>> list = offlineSimpleDao.queryForList(sql, txncode);
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
		List<Map<String, Object>> list = offlineSimpleDao.queryForList(sql, userId);
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
		String sql = "select AC_ID, TXN_CODE, AC_NAME, AC_COND, AC_EXPR from TMS_MGR_ALARM_ACTION "
				+ "where TXN_CODE = ? and (EXECUE_RESULT is null or EXECUE_RESULT = '0')";
		sql = "select AC_ID, TXN_CODE, AC_NAME, AC_COND, AC_EXPR from TMS_MGR_ALARM_ACTION where TXN_CODE = ?";
		return offlineSimpleDao.queryForList(sql, txnCode);
	}

	/**
	 * 报警事件分派
	 * 
	 * @param cond
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void alarmAssign(String rosterIds, List<Map<String, String>> operater, HttpServletRequest request) {
		long currTime = System.currentTimeMillis();
		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute("OPERATOR"); // 获取当前登录操作员
		// 通过查询交易流水, 获取其中报警处理相关信息
		List<Map<String, Object>> transMap = getTrafficDataForAlarmProcessList(rosterIds);
		// 查询系统参数
		//Map<String, Object> sysParamMap = getSystemParam(ALARM_PS_TIMEOUT);
		Map<String, String> operaterInfo = operater.get(0);
		
		for (Map<String, Object> tms_run_trafficdata : transMap) {
			String psStatus = MapUtil.getString(tms_run_trafficdata, "PSSTATUS");
			if (!("00".equals(psStatus) || "02".equals(psStatus) || "04".equals(psStatus))) {
				throw new TmsMgrServiceException("当前交易处理状态不符合分派条件, 请刷新页面数据.");
			}
			// 添加报警处理信息----TMS_MGR_ALARM_PROCESS
			Map<String, String> tms_mgr_alarm_process = new HashMap<String, String>();
			tms_mgr_alarm_process.put("PSSTATUS", "02"); // 处理状态改成待处理
			tms_mgr_alarm_process.put("PS_TYPE", "0");
			tms_mgr_alarm_process.put("PS_RESULT", "1");
			tms_mgr_alarm_process.put("PS_INFO", "报警事件分派");
			tms_mgr_alarm_process.put("TXN_CODE", String.valueOf(tms_run_trafficdata.get("TXNCODE"))); // 交易流水号
			tms_mgr_alarm_process.put("PS_OPERID", String.valueOf(operaterInfo.get("operator_id"))); // 报警处理操作员
			Map<String, Object> infoMap = addAlarmProcessInfo(tms_mgr_alarm_process, request);

			// 报警事件分派，更新工作量统计数据-----TMS_MGR_ALARM_OPERATOR_STAT;
			Map<String, Object> tms_mgr_alarm_operator_stat = new HashMap<String, Object>();
			//tms_mgr_alarm_operator_stat.put("ASSIGNID", operator.get("OPERATOR_ID"));// ASSIGNID分派人员
			tms_mgr_alarm_operator_stat.put("ASSIGNID", "1");// ASSIGNID分派人员
			tms_mgr_alarm_operator_stat.put("ASSIGNTIME", infoMap.get("PS_TIME")); // ASSIGNTIME分派时间
			tms_mgr_alarm_operator_stat.put("OPERID", String.valueOf(operaterInfo.get("operator_id"))); // OPERID报警处理操作员
			boolean status = "00".equals(psStatus) ? false : true;
			monitorStatService.updateAlarmAssignOperatorStat(MapUtil.getString(tms_run_trafficdata, "OPERID"),
					MapUtil.getLong(tms_run_trafficdata, "ASSIGNTIME"), status, MapUtil.getString(tms_mgr_alarm_operator_stat, "OPERID"), currTime);

			// 更新交易流水中报警处理状态----TMS_RUN_TRAFFICDATA
			tms_run_trafficdata.put("PSSTATUS", "02"); // 处理状态改成待处理
			tms_run_trafficdata.put("ASSIGNID","1");//分派人员
			//tms_run_trafficdata.put("ASSIGNID", operator.get("OPERATOR_ID"));//分派人员
			tms_run_trafficdata.put("ASSIGNTIME", infoMap.get("PS_TIME"));//分派时间
			tms_run_trafficdata.put("OPERID",  String.valueOf(operaterInfo.get("operator_id")));//报警处理操作员
			updateTransProcessInfo(tms_run_trafficdata);

		}
	}

	/**
	 * 报警事件处理人员工作量
	 * 
	 * @param cond
	 * @return
	 */
	public List<Map<String, Object>> getAlarmAssignOperCapacity(Map<String, Object> cond) {
		List<Map<String, Object>> list = this.getAlarmProcessAuthorityOperators();
		if (list == null || list.isEmpty()) {
			throw new TmsMgrServiceException("没有拥有[报警事件处理]权限的人员, 请设置.");
		}
		String operId = MapUtil.getString(cond, "OPERID");// 当前交易的处理人员
		String sql = "select OPERATOR_ID, sum(ASSIGN_NUMBER) ASSIGN_NUMBER, sum(PROCESS_NUMBER) PROCESS_NUMBER, "
				+ "sum(UNPROCESS_NUMBER) UNPROCESS_NUMBER from TMS_MGR_ALARM_OPERATOR_STAT group by OPERATOR_ID order by assign_number,unprocess_number";
		List<Map<String, Object>> statList = offlineSimpleDao.queryForList(sql);
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
	 * 获取系统参数值
	 * 
	 * @param paramName
	 *            参数名
	 * @return
	 */
	public Map<String, Object> getSystemParam(String paramName) {
		String sql = "select DATATYPE, VALUETYPE, STARTVALUE, ENDVALUE from TMS_MGR_SYSPARAM where SYSPARAMNAME = ?";
		List<Map<String, Object>> list = onlineSimpleDao.queryForList(sql, paramName);
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
		// String sql = "SELECT co.OPERATOR_ID, co.LOGIN_NAME FROM cmc_operator co,
		// cmc_operator_role_rel corr, cmc_role_func_rel crfr " +
		// "WHERE co.OPERATOR_ID = corr.OPERATOR_ID AND corr.ROLE_ID = crfr.ROLE_ID AND
		// co.FLAG = '1' AND crfr.FUNC_ID = ?";
		// List<Map<String, Object>> list = tmpSimpleDao.queryForList(sql, funcId);
		String sql = "SELECT co.OPERATOR_ID, co.LOGIN_NAME FROM cmc_operator co where login_name!='admin'";
		List<Map<String, Object>> list = offlineSimpleDao.queryForList(sql);
		return list;
	}

	/**
	 * 获取交易策略信息
	 * 
	 * @param stId
	 *            策略ID
	 * @return
	 */
	public Map<String, Object> getTransStrategy(String stId) {
		String sql = "select ST_ID, ST_TXN, ST_NAME, EVAL_COND, EVAL_COND_IN, EVAL_MODE, RULE_EXEC_MODE, "
				+ "ST_ENABLE, CREATETIME, MODIFYTIME from TMS_COM_STRATEGY where ST_ID = ?";
		List<Map<String, Object>> list = onlineSimpleDao.queryForList(sql, stId);
		if (list == null || list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	/**
	 * 获取评估类型策略
	 * 
	 * @param stId
	 *            策略ID
	 * @return
	 */
	public List<Map<String, Object>> getTransStrategyRuleEvalList(String stId) {
		String sql = "select SRE_ID, ST_ID, EVAL_TYPE, EVAL_MECH, DIS_STRATEGY, STAT_FUNC, PS_SCORE "
				+ "from TMS_COM_STRATEGY_RULE_EVAL where ST_ID = ? order by EVAL_TYPE";
		return onlineSimpleDao.queryForList(sql, stId);
	}

	/**
	 * 获取交易命中规则信息
	 * 
	 * @param txnCode
	 *            交易流水号
	 * @param txnType
	 *            交易类型
	 * @return
	 */
	public List<Map<String, Object>> getTransHitRuleList(String txnCode, String txnType) {
		String ruleHitsql = "select TRIGID, TXNCODE, TXNTYPE, RULEID, MESSAGE, NUMTIMES, "
				+ "CREATETIME, RULE_SCORE from TMS_RUN_RULETRIG where TXNCODE = ?";
		List<Map<String, Object>> trigList = offlineSimpleDao.queryForList(ruleHitsql, txnCode);
		if (trigList == null || trigList.isEmpty()) {
			return null;
		}
		String ruleSql = "select RULE_ID, RULE_NAME, RULE_DESC, RULE_COND, RULE_COND_IN, "
				+ "RULE_SHORTDESC, EVAL_TYPE, DISPOSAL from TMS_COM_RULE where RULE_TXN in ("
				+ TransCommon.arr2str(TransCommon.cutToIds(txnType)) + ")";
		List<Map<String, Object>> ruleList = onlineSimpleDao.queryForList(ruleSql);
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

	/**
	 * 方法描述:查询处理员处理的历史报警事件
	 * 
	 * @param reqs
	 * @return
	 */
	public Page<Map<String, Object>> getAlarmHisActions(Map<String, String> reqs) {
		String sql = "select t.userid,u.cusname,t.txncode,p.fraud_type,p.ps_info,p.short_action,p.ps_time,t.OPERID from tms_run_trafficdata t"
				+ " inner join tms_run_user u on t.userid=u.userid"
				+ " inner join (select max(ps_time) ps_time,p.txn_code from tms_mgr_alarm_process p where p.ps_type=1 group by txn_code) a on a.txn_code = t.txncode"
				+ " inner join tms_mgr_alarm_process  p on p.txn_code=t.txncode and p.ps_time=a.ps_time"
				+ " where t.OPERID = :OPERID and t.userid = :userid";
		Map<String, String> operator = (Map<String, String>) request.getSession().getAttribute("OPERATOR");

		if (operator != null) {
			// Map<String, Object> m = new HashMap<String, Object>();
			reqs.put("OPERID", operator.get("OPERATOR_ID"));
			reqs.put("pagesize", "10");
			Page<Map<String, Object>> pageQuery = offlineSimpleDao.pageQuery(sql, reqs, new Order().desc("ps_time"));
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

	/**
	 * 查询快捷函数
	 * 
	 * @param reqs
	 */
	public List<Map<String, Object>> shortActionList(Map<String, String> reqs) {
		String sql = "select func_name text,func_code value from tms_com_func where func_catalog = '1' order by func_orderby";
		return offlineSimpleDao.queryForList(sql);
	}

	////////////////////////////////////////////////////// lemon20180101增加查询预警事件列表服务开始/////////////////////////////////////////////////
	/**
	 * 查询报警事件信息
	 * 
	 * @param conds
	 *            查询条件
	 * @return
	 */
	public Page<Map<String, Object>> QueryListByPage(Map<String, String> conds) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ");
		sb.append("TRAFFIC.TXNCODE, TRAFFIC.USERID, U.USERNAME, TRAFFIC.TXNTYPE, ");
		sb.append(
				"(SELECT CONCAT(CHANNEL.CHANNELNAME, '--', TAB.TAB_DESC) FROM TMS_DP_CHANNEL CHANNEL, TMS_COM_TAB TAB ");
		sb.append("WHERE TRAFFIC.TXNTYPE = TAB.TAB_NAME AND TAB.CHANN = CHANNEL.CHANNELID) TXNNAME, ");
		sb.append("TRAFFIC.TXNTIME,IFNULL(TRAFFIC.ISCORRECT, '2') ISCORRECT, ");
		sb.append("(SELECT DP_NAME FROM TMS_COM_DISPOSAL DP WHERE TRAFFIC.DISPOSAL=DP.DP_CODE)  DISPOSAL,  ");
		sb.append("TRAFFIC.TXNSTATUS,TRAFFIC.ASSIGNTIME,TRAFFIC.ASSIGNTIME M_ASSIGNTIME,  ");
		sb.append(
				"(SELECT O.LOGIN_NAME FROM CMC_OPERATOR O WHERE TRAFFIC.OPERID = O.OPERATOR_ID) OPER_NAME, TRAFFIC.OPERTIME, ");
		sb.append("(SELECT O.LOGIN_NAME FROM CMC_OPERATOR O WHERE TRAFFIC.AUDITID = O.OPERATOR_ID) AUDIT_NAME,  ");
		sb.append("TRAFFIC.AUDITTIME,TRAFFIC.AUDITTIME M_AUDITTIME,TRAFFIC.PSSTATUS, TRAFFIC.STRATEGY  ");
		sb.append("FROM TMS_RUN_TRAFFICDATA TRAFFIC LEFT JOIN TMS_RUN_USER U ");
		sb.append("ON TRAFFIC.USERID = U.USERID  WHERE  ");
		if (!StringUtil.isEmpty(conds.get("userid"))) {
			sb.append("TRAFFIC.USERID =:userid  AND ");
		}
		if (!StringUtil.isEmpty(conds.get("txncode"))) {
			sb.append("TRAFFIC.TXNCODE =:txncode  AND ");
		}
		if (!StringUtil.isEmpty(conds.get("operate_time"))) {
			long operate_time = getMillisTime(conds.get("operate_time"));
			sb.append("TRAFFIC.TXNTIME >=:operate_time  AND ");
		}
		if (!StringUtil.isEmpty(conds.get("end_time"))) {
			long end_time = getMillisTime(conds.get("end_time"));
			sb.append("TRAFFIC.TXNTIME <=:end_time  AND ");
		}
		if (!StringUtil.isEmpty(conds.get("passtatus"))) {
			sb.append("TRAFFIC.PSSTATUS =:passtatus  AND ");
		}
		// sb.append("(TRAFFIC.PSSTATUS = '00' OR TRAFFIC.PSSTATUS = '02' ||
		// TRAFFIC.PSSTATUS = '04') ");
		sb.append("TRAFFIC.HITRULENUM !=0 AND  TRAFFIC.PSSTATUS = '00' OR TRAFFIC.PSSTATUS = '02'");

		Order order = new Order().desc("TXNTIME");
		System.out.println(sb.toString());
		Page<Map<String, Object>> listPage = offlineSimpleDao.pageQuery(sb.toString(), conds, order);
		System.out.println(listPage.getList().get(0));
		return listPage;
	}

	
	/**
	 * 查询报警事件处理信息
	 * 
	 * @param conds
	 *            查询条件
	 * @return
	 */
	public Page<Map<String, Object>> QueryExecuteListByPage(Map<String, String> conds) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ");
		sb.append("TRAFFIC.TXNCODE, TRAFFIC.USERID, U.USERNAME, TRAFFIC.TXNTYPE, ");
		sb.append(
				"(SELECT CONCAT(CHANNEL.CHANNELNAME, '--', TAB.TAB_DESC) FROM TMS_DP_CHANNEL CHANNEL, TMS_COM_TAB TAB ");
		sb.append("WHERE TRAFFIC.TXNTYPE = TAB.TAB_NAME AND TAB.CHANN = CHANNEL.CHANNELID) TXNNAME, ");
		sb.append("TRAFFIC.TXNTIME,IFNULL(TRAFFIC.ISCORRECT, '2') ISCORRECT, ");
		sb.append("(SELECT DP_NAME FROM TMS_COM_DISPOSAL DP WHERE TRAFFIC.DISPOSAL=DP.DP_CODE)  DISPOSAL,  ");
		sb.append("TRAFFIC.TXNSTATUS,TRAFFIC.ASSIGNTIME,TRAFFIC.ASSIGNTIME M_ASSIGNTIME,  ");
		sb.append("(SELECT O.LOGIN_NAME FROM CMC_OPERATOR O WHERE TRAFFIC.ASSIGNID = O.OPERATOR_ID) ASSIGN_NAME, ");
		sb.append(
				"(SELECT O.LOGIN_NAME FROM CMC_OPERATOR O WHERE TRAFFIC.OPERID = O.OPERATOR_ID) OPER_NAME, TRAFFIC.OPERTIME, ");
		sb.append("(SELECT O.LOGIN_NAME FROM CMC_OPERATOR O WHERE TRAFFIC.AUDITID = O.OPERATOR_ID) AUDIT_NAME,  ");
		sb.append("TRAFFIC.AUDITTIME,TRAFFIC.AUDITTIME M_AUDITTIME,TRAFFIC.PSSTATUS, TRAFFIC.STRATEGY  ");
		sb.append("FROM TMS_RUN_TRAFFICDATA TRAFFIC LEFT JOIN TMS_RUN_USER U ");
		sb.append("ON TRAFFIC.USERID = U.USERID  WHERE  ");
		if (!StringUtil.isEmpty(conds.get("userid"))) {
			sb.append("TRAFFIC.USERID =:userid  AND ");
		}
		if (!StringUtil.isEmpty(conds.get("txncode"))) {
			sb.append("TRAFFIC.TXNCODE =:txncode  AND ");
		}
		if (!StringUtil.isEmpty(conds.get("operate_time"))) {
			long operate_time = getMillisTime(conds.get("operate_time"));
			sb.append("TRAFFIC.TXNTIME >=:operate_time  AND ");
		}
		if (!StringUtil.isEmpty(conds.get("end_time"))) {
			long end_time = getMillisTime(conds.get("end_time"));
			sb.append("TRAFFIC.TXNTIME <=:end_time  AND ");
		}
		if (!StringUtil.isEmpty(conds.get("passtatus"))) {
			sb.append("TRAFFIC.PSSTATUS =:passtatus  AND ");
		}
		sb.append("TRAFFIC.PSSTATUS = '02' OR  TRAFFIC.PSSTATUS = '04' ");

		Order order = new Order().desc("TXNTIME");
		System.out.println(sb.toString());
		Page<Map<String, Object>> listPage = offlineSimpleDao.pageQuery(sb.toString(), conds, order);
		System.out.println(listPage.getList().get(0));
		return listPage;
	}
	
	/**
	 * 将2018-01-01 09:50:39转成时间戳
	 * 
	 * @param date
	 * @return
	 */
	private long getMillisTime(String date) {
		long time = -1;
		SimpleDateFormat sdf = null;
		try {
			sdf = new SimpleDateFormat(CalendarUtil.FORMAT14.toPattern());
			sdf.parse(date);// 时间校验yyyy-MM-dd HH:mm:ss
			time = CalendarUtil.parseStringToTimeMillis(date, CalendarUtil.FORMAT14.toPattern());
		} catch (ParseException e) {
			try {
				sdf = new SimpleDateFormat(CalendarUtil.FORMAT11.toPattern());// 日期校验yyyy-MM-dd
				sdf.parse(date);
				time = CalendarUtil.parseStringToTimeMillis(date, CalendarUtil.FORMAT11.toPattern());
			} catch (ParseException e1) {
			}
		}
		return time;
	}


	/**
	 * 通过查询交易流水, 获取其中报警处理相关信息
	 * 
	 * @param cond
	 *            查询条件参数值
	 * @return
	 */
	public List<Map<String, Object>> getTrafficDataForAlarmProcessList(String rosterIds) {
		String sql = "select TXNCODE, CHANCODE, TXNID, TXNTYPE, USERID, COUNTRYCODE, REGIONCODE, CITYCODE, TXNTIME, "
				+ "DISPOSAL, MODELID, STRATEGY, PSSTATUS, ASSIGNID, ASSIGNTIME, OPERID, OPERTIME, AUDITID, AUDITTIME, "
				+ "FRAUD_TYPE,M_NUM2 from TMS_RUN_TRAFFICDATA t where TXNCODE IN(" + rosterIds + ")";

		List<Map<String, Object>> list = offlineSimpleDao.queryForList(sql);
		if (list == null || list.isEmpty())
			return null;
		return list;
	}

	////////////////////////////////////////////////////// lemon20180101增加查询预警事件列表结束/////////////////////////////////////////////////

}