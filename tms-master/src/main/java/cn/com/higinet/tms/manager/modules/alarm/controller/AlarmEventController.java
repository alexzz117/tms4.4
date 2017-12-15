package cn.com.higinet.tms.manager.modules.alarm.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.modules.alarm.service.AlarmEventService;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrServiceException;

/**
 * 警报事件Controller
 * 
 * @author lining
 *
 */
@Controller("alarmEventController")
@RequestMapping("/tms/alarmevent")
public class AlarmEventController {

	@Autowired
	@Qualifier("alarmEventService")
	private AlarmEventService alarmEventService;

	/**
	 * 报警事件处理页面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/process", method = RequestMethod.GET)
	public String alarmProcessView() {
		return "tms/alarm/alarmevent_process";
	}

	/**
	 * 获取报警事件处理信息
	 * 
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/process", method = RequestMethod.POST)
	public Model alarmProcessAction(@RequestParam Map<String, String> reqs) {
		Model model = new Model();
		Map<String, Object> txnMap = alarmEventService.getTrafficDataForAlarmProcessInfo(reqs);
		reqs.put("AC_TYPE", "1");// 普通动作
		List<Map<String, Object>> actList = alarmEventService.getAlarmProcessActions(reqs);
		model.set("txnMap", txnMap);
		model.set("actList", actList);
		return model;
	}

	/**
	 * 获取报警事件处理历史列表
	 * 
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/getAlarmHis", method = RequestMethod.POST)
	public Model getAlarmHisActions(@RequestParam Map<String, String> reqs) {
		Model model = new Model();
		Page<Map<String, Object>> armHisPage = alarmEventService.getAlarmHisActions(reqs);
		model.setPage(armHisPage);
		return model;
	}

	/**
	 * 提交报警事件处理
	 * 
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/saveProcess", method = RequestMethod.POST)
	public Model saveAlarmProcessAction(@RequestParam Map<String, String> reqs, HttpServletRequest request) {
		Model model = new Model();
		//alarmService.alarmProcess(reqs, request);
		
		// 通过客户号查询非挂起的待处理的交易
		Map<String, Object> transMap1 = alarmEventService.getTrafficDataForAlarmProcessInfo(reqs);

		// 挂起
		if ("PS04".equals(MapUtil.getString(transMap1, "DISPOSAL"))) {
			alarmEventService.alarmProcess(reqs, request);
			return model;
		}

		// 非挂起类报警事件,当某审核员处理完其中某一会员的一例报警事件后，该会员的其余报警事件自动得到相同处理
		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute("OPERATOR");
		if(null!=operator){
			String operId= (String) operator.get("OPERATOR_ID");
			transMap1.put("OPERID", operId);
		}
		
		List<Map<String, Object>> user_trans = alarmEventService.getTrafficDataByUserid(transMap1);
		for (Map<String, Object> map1 : user_trans) {
			reqs.put("TXN_CODE", MapUtil.getString(map1, "TXNCODE"));
			alarmEventService.alarmProcess(reqs, request);
		}
		
		return model;
	}

	/**
	 * 查询快捷动作函数
	 * 
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/shortActionList", method = RequestMethod.POST)
	public Model shortActionList(@RequestParam Map<String, String> reqs) {
		Model model = new Model();
		model.setList(alarmEventService.shortActionList(reqs));
		return model;
	}

	/**
	 * 添加报警事件处理动作
	 * 
	 * @return
	 */
	@RequestMapping(value = "/addPsAct", method = RequestMethod.POST)
	public Model addAlarmPsActAction(@RequestParam Map<String, String> reqs) {
		Model model = new Model();
		reqs.put("AC_TYPE", "1");
		model.setRow(alarmEventService.addAlarmProcessAction(reqs));
		return model;
	}

	/**
	 * 删除报警事件处理动作
	 * 
	 * @return
	 */
	@RequestMapping(value = "/delPsAct", method = RequestMethod.POST)
	public Model delAlarmPsActAction(@RequestParam Map<String, String> reqs) {
		Model model = new Model();
		alarmEventService.delAlarmProcessAction(reqs);
		return model;
	}

	/**
	 * 报警事件审核页面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/audit", method = RequestMethod.GET)
	public String alarmAuditView() {
		return "tms/alarm/alarmevent_audit";
	}

	/**
	 * 获取报警事件审核信息
	 * 
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/audit", method = RequestMethod.POST)
	public Model alarmAuditAction(@RequestParam Map<String, String> reqs, HttpServletRequest request) {
		Model model = new Model();
		Map<String, Object> txnMap = alarmEventService.getTrafficDataForAlarmProcessInfo(reqs);
		reqs.put("AC_TYPE", "1");// 普通动作
		List<Map<String, Object>> actList = alarmEventService.getAlarmProcessActions(reqs);
		List<Map<String, Object>> psList = alarmEventService.getAlarmProcessInfoList(reqs);
		model.set("txnMap", txnMap);
		model.set("actList", actList);
		model.set("psList", psList);
		return model;
	}

	/**
	 * 提交报警事件审核信息
	 * 
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/saveAudit", method = RequestMethod.POST)
	public Model saveAlarmAuditAction(@RequestParam Map<String, String> reqs, HttpServletRequest request) {
		Model model = new Model();
		int result = alarmEventService.alarmAudit(reqs, request);
		model.addObject("result", result);
		return model;
	}

	/**
	 * 报警事件分派view
	 * 
	 * @return
	 */
	@RequestMapping(value = "/assign", method = RequestMethod.GET)
	public String alarmAssignView() {
		return "tms/alarm/alarmevent_assign";
	}

	/**
	 * 获取报警事件分派信息
	 * 
	 * @param reqs
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/assign", method = RequestMethod.POST)
	public Model alarmAssignAction(@RequestParam Map<String, String> reqs, HttpServletRequest request) {
		Model model = new Model();
		// long currTime = System.currentTimeMillis();
		// Map<String, Object> txnMap = alarmService.getTrafficDataForAlarmProcessInfo(reqs);
		// Map<String, Object> paramMap = alarmService.getSystemParam(AlarmEventService.ALARM_PS_TIMEOUT);
		// long timeout = MapUtil.getLong(paramMap, "STARTVALUE");
		Map<String, Object> cond = new HashMap<String, Object>();
		// cond.putAll(txnMap);
		List<Map<String, Object>> operCapacityList = alarmEventService.getAlarmAssignOperCapacity(cond);
		// String psStatus = MapUtil.getString(txnMap, "PSSTATUS");
		// long assignTime = MapUtil.getLong(txnMap, "ASSIGNTIME");
		// long auditTime = MapUtil.getLong(txnMap, "AUDITTIME");
		// int isTimeout = 0;
		// if (("02".equals(psStatus) && (currTime - assignTime) > timeout) || (("04".equals(psStatus) && (currTime - auditTime) > timeout))) {
		// isTimeout = 1;
		// }

		List<Map<String, Object>> list = alarmEventService.getAlarmProcessAuthorityOperators();
		if (list == null || list.isEmpty()) {
			throw new TmsMgrServiceException("没有拥有[报警事件处理]权限的人员, 请设置.");
		}

		// txnMap.put("TIMEOUT", isTimeout);

		//只选择一个记录时才这么做
		String txnCodes = MapUtil.getString(reqs, "TXNCODES");
		if (null != txnCodes && txnCodes.trim().length() > 0) {
			String[] codes = txnCodes.split(",");
			if(1==codes.length){
				reqs.put("TXN_CODE", codes[0]);
				Map<String, Object> txnMap = alarmEventService.getTrafficDataForAlarmProcessInfo(reqs);
				model.set("txnMap", txnMap);		
			}
		}	

		model.set("operList", list);
		model.setList(operCapacityList);
		return model;
	}

	/**
	 * 提交报警事件分派信息
	 * 
	 * @param reqs
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/saveAssign", method = RequestMethod.POST)
	public Model saveAlarmAssignAction(@RequestParam Map<String, String> reqs, HttpServletRequest request) {
		Model model = new Model();
		// alarmService.alarmAssign(reqs, request);

		// 改成批量，临时这么做，wujw 20160118
		String txnCodes = MapUtil.getString(reqs, "TXNCODES");
		if (null != txnCodes && txnCodes.trim().length() > 0) {
			String[] codes = txnCodes.split(",");
			for (String cd : codes) {
				reqs.put("TXN_CODE", cd);
				alarmEventService.alarmAssign(reqs, request);
			}
		}

		return model;
	}

	@RequestMapping(value = "/alarmStrategy", method = RequestMethod.GET)
	public String alarmStrategyView() {
		return "tms/alarm/alarm_strategy";
	}

	@RequestMapping(value = "/alarmStrategy", method = RequestMethod.POST)
	public Model alarmStrategyAction(@RequestParam Map<String, String> reqs, HttpServletRequest request) {
		Model model = new Model();
		String stId = MapUtil.getString(reqs, "stid");
		String txnCode = MapUtil.getString(reqs, "txncode");
		String txnType = MapUtil.getString(reqs, "txntype");
		model.setRow(alarmEventService.getTransStrategy(stId));
		model.setList(alarmEventService.getTransStrategyRuleEvalList(stId));
		model.set("ruleList", alarmEventService.getTransHitRuleList(txnCode, txnType));
		return model;
	}

	@RequestMapping(value = "/alarmAssignOpers", method = RequestMethod.POST)
	public Model getAlarmAssignOpers(@RequestParam Map<String, String> reqs, HttpServletRequest request) {
		Model model = new Model();
		model.setList(alarmEventService.getAlarmAssingAuthorityOperators());
		return model;
	}

	@RequestMapping(value = "/alarmProcessOpers", method = RequestMethod.POST)
	public Model getAlarmProcessOpers(@RequestParam Map<String, String> reqs, HttpServletRequest request) {
		Model model = new Model();
		model.setList(alarmEventService.getAlarmProcessAuthorityOperators());
		return model;
	}

	@RequestMapping(value = "/alarmAuditOpers", method = RequestMethod.POST)
	public Model getAlarmAuditOpers(@RequestParam Map<String, String> reqs, HttpServletRequest request) {
		Model model = new Model();
		model.setList(alarmEventService.getAlarmAuditAuthorityOperators());
		return model;
	}
}