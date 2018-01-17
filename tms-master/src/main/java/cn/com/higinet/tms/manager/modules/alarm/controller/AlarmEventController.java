package cn.com.higinet.tms.manager.modules.alarm.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.com.higinet.tms.base.entity.common.RequestModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.modules.alarm.service.AlarmEventService;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;

/**
 * 警报事件Controller
 * 
 * @author lining
 * @author zhang.lei
 */
@RestController("alarmEventController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/alarmevent")
public class AlarmEventController {

	@Autowired
	@Qualifier("alarmEventService")
	private AlarmEventService alarmEventService;

	/**
	 * 预警事件分派交易
	 * 
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model listNameListActoin(@RequestBody Map<String, String> modelMap) {
		Model model = new Model();
		model.setPage(alarmEventService.QueryListByPage(modelMap));
		return model;
	}

	
	/**
	 * 预警事件处理交易
	 * 
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/executeList", method = RequestMethod.POST)
	public Model executeList(@RequestBody Map<String, String> modelMap) {
		Model model = new Model();
		model.setPage(alarmEventService.QueryExecuteListByPage(modelMap));
		return model;
	}
	
	/**
	 * 预警事件处理审核
	 * 
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/auditList", method = RequestMethod.POST)
	public Model auditList(@RequestBody Map<String, String> modelMap) {
		Model model = new Model();	
		model.setPage(alarmEventService.QueryAuditListByPage(modelMap));
		return model;
	}

	/**
	 * 获取报警事件处理信息
	 * 
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/process", method = RequestMethod.POST)
	public Model alarmProcessAction(@RequestBody Map<String, String> reqs) {
		Model model = new Model();
		Map<String, Object> txnMap = alarmEventService.getTrafficDataForAlarmProcessInfo(reqs);
		reqs.put("AC_TYPE", "1");// 普通动作
		List<Map<String, Object>> actList = alarmEventService.getAlarmProcessActions(reqs);
		model.set("txnMap", txnMap);
		model.setList(actList);
		//model.set("actList", actList);
		return model;
	}

	/**
	 * 获取报警事件处理历史列表
	 * 
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/getAlarmHis", method = RequestMethod.POST)
	public Model getAlarmHisActions(@RequestBody Map<String, String> reqs) {
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
	public Model saveAlarmProcessAction(@RequestBody Map<String, String> reqs, HttpServletRequest request) {
		Model model = new Model();
		// alarmService.alarmProcess(reqs, request);

		// 通过客户号查询非挂起的待处理的交易
		Map<String, Object> transMap1 = alarmEventService.getTrafficDataForAlarmProcessInfo(reqs);

		// 挂起
		if ("PS04".equals(MapUtil.getString(transMap1, "DISPOSAL"))) {
			alarmEventService.alarmProcess(reqs, request);
			return model;
		}

		// 非挂起类报警事件,当某审核员处理完其中某一会员的一例报警事件后，该会员的其余报警事件自动得到相同处理
		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute("OPERATOR");
		if (null != operator) {
			String operId = (String) operator.get("OPERATOR_ID");
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
	public Model shortActionList(@RequestBody Map<String, String> reqs) {
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
	public Model addAlarmPsActAction(@RequestBody Map<String, String> reqs) {
		Model model = new Model();
		reqs.put("AC_TYPE", "1");
		model.setRow(alarmEventService.addAlarmProcessAction(reqs));
		return model;
	}
	

	/**
	 * 编辑报警事件处理动作
	 * 
	 * @return
	 */
	@RequestMapping(value = "/updPsAct", method = RequestMethod.POST)
	public Model updAlarmPsActAction(@RequestBody Map<String, Object> reqs) {
		Model model = new Model();
		reqs.put("AC_TYPE", "1");
		model.setRow(alarmEventService.updAlarmProcessAction(reqs));
		return model;
	}
	

	/**
	 * 删除报警事件处理动作
	 * 
	 * @return
	 */
	@RequestMapping(value = "/delPsAct", method = RequestMethod.POST)
	public Model delAlarmPsActAction(@RequestBody Map<String, List<Map<String, Object>>> modelMap) {
		Model model = new Model();
		List<Map<String, Object>> condList = modelMap.get("del");
		List<Map<String, ?>> reqslist = new ArrayList<Map<String, ?>>();
		for(Map<String, Object> map :condList) {
			Map<String, Object> temp =new HashMap<String, Object>();
			temp.put("AC_ID", map.get("ac_id"));
			reqslist.add(temp);
		}
		alarmEventService.delsAlarmProcessAction(reqslist);
		return model;
	}


	/**
	 * 获取报警事件审核信息
	 * 
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/audit", method = RequestMethod.POST)
	public Model alarmAuditAction(@RequestBody Map<String, String> reqs) {
		Model model = new Model();
		Map<String, Object> txnMap = alarmEventService.getTrafficDataForAlarmProcessInfo(reqs);
		reqs.put("AC_TYPE", "1");// 普通动作
		List<Map<String, Object>> actList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> psList = new ArrayList<Map<String, Object>>();
		actList = alarmEventService.getAlarmProcessActions(reqs);
		psList = alarmEventService.getAlarmProcessInfoList(reqs);
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
	public Model saveAlarmAuditAction(@RequestBody Map<String, String> reqs, HttpServletRequest request) {
		Model model = new Model();
		int result = alarmEventService.alarmAudit(reqs, request);
		model.put("result", result);
		return model;
	}

	/**
	 * 获取报警事件分派信息
	 * 
	 * @param reqs
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/assign", method = RequestMethod.POST)
	public Model alarmAssignAction(@RequestBody RequestModel modelMap) {
		Model model = new Model();
		Map<String, Object> cond = new HashMap<String, Object>();
		cond.put("OPERID", modelMap.getString("oper_id"));
		//报警事件处理人员工作量
		List<Map<String, Object>> operCapacityList = alarmEventService.getAlarmAssignOperCapacity(cond);
		//有报警事件处理权限的操作员
		List<Map<String, Object>> list = alarmEventService.getAlarmProcessAuthorityOperators();
		if (list == null || list.isEmpty()) {
			throw new TmsMgrServiceException("没有拥有[报警事件处理]权限的人员, 请设置.");
		}
		// 20180101-lemon 修改 由单个改成批量
		//通过查询交易流水, 获取其中报警处理相关信息
//		List<Map<String, String>> condList = modelMap.getString("TXNCODES");
//		String rosterIds = "";
//		for (Map<String, String> delMap : condList) {
//			String rosterId = MapUtil.getString(delMap, "txncode");
//			rosterIds += ",'" + rosterId + "'";
//		}
//		rosterIds = rosterIds.substring(1);
//		List<String> rosterIds = new ArrayList<>(1);
//		rosterIds.add(modelMap.getString("txn_code"));
		List<Map<String, Object>> txnMapList = alarmEventService.getTrafficDataForAlarmProcessList(modelMap.getString("txn_code"));
		model.set("txnMap", txnMapList);
		model.set("operList", list);
		model.setList(operCapacityList);
		
		return model;
	}

	/**
	 * 提交报警事件分派信息
	 * 多个流水信息批量
	 * @param reqs
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/onsaveAssign", method = RequestMethod.POST)
	public Model onsaveAlarmAssignAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
		String txncodeList = reqs.getString( "TXNCODES" );//流水信息
		String operater = reqs.getString("OPERATER"); //选择的分派操作员信息
//		String rosterIds = "";
//		for( Map<String, String> map : txncodeList) {
//			String rosterId = MapUtil.getString( map, "txncode" );
//			rosterIds += ",'" + rosterId + "'";
//		}
//		rosterIds = rosterIds.substring( 1 );
//		if(null != txncodeList && txncodeList.size()>0 && null!= operater) {
//			alarmEventService.alarmAssign(rosterIds,operater, request);
//		}
		if(StringUtils.isNotEmpty(txncodeList) && StringUtils.isNotEmpty(operater)) {
			alarmEventService.alarmAssign(txncodeList,operater, request);
		}
		return  new Model();
	}
	
//	
//	
//	/**
//	 * 提交报警事件分派信息
//	 * 多个流水信息批量
//	 * @param reqs
//	 * @param request
//	 * @return
//	 */
//	@RequestMapping(value = "/saveAssign", method = RequestMethod.POST)
//	public Model saveAlarmAssignAction(@RequestBody Map<String, String> reqs, HttpServletRequest request) {
//		String txnCodes = MapUtil.getString(reqs, "TXNCODES");
//		if (null != txnCodes && txnCodes.trim().length() > 0) {
//			String[] codes = txnCodes.split(",");
//			for (String cd : codes) {
//				reqs.put("TXN_CODE", cd);
//				alarmEventService.alarmAssign(reqs, request);
//			}
//		}
//
//		return  new Model();
//	}

	/*
	 * @RequestMapping(value = "/alarmStrategy", method = RequestMethod.GET) public
	 * String alarmStrategyView() { return "tms/alarm/alarm_strategy"; }
	 */

	@RequestMapping(value = "/alarmStrategy", method = RequestMethod.POST)
	public Model alarmStrategyAction(@RequestBody Map<String, String> reqs) {
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
	public Model getAlarmAssignOpers(@RequestBody Map<String, String> reqs) {
		Model model = new Model();
		model.setList(alarmEventService.getAlarmAssingAuthorityOperators());
		return model;
	}

	@RequestMapping(value = "/alarmProcessOpers", method = RequestMethod.POST)
	public Model getAlarmProcessOpers(@RequestBody Map<String, String> reqs) {
		Model model = new Model();
		model.setList(alarmEventService.getAlarmProcessAuthorityOperators());
		return model;
	}

	@RequestMapping(value = "/alarmAuditOpers", method = RequestMethod.POST)
	public Model getAlarmAuditOpers(@RequestBody Map<String, String> reqs) {
		Model model = new Model();
		model.setList(alarmEventService.getAlarmAuditAuthorityOperators());
		return model;
	}
}