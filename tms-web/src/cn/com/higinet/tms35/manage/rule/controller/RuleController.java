/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms35.manage.rule.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.com.higinet.rapid.web.model.Model;
import cn.com.higinet.tms35.manage.common.DBConstant;
import cn.com.higinet.tms35.manage.common.RuleJsonUtil;
import cn.com.higinet.tms35.manage.common.StaticParameters;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.exception.TmsMgrWebException;
import cn.com.higinet.tms35.manage.rule.service.RuleService;
import cn.com.higinet.tmsreport.web.service.DisposalService;

/**
 * 功能/模块:
 * @author 张立群
 * @version 1.0  May 21, 2013
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
@Controller("ruleController35")
@RequestMapping("/tms35/rule")
public class RuleController {
	
	private static Log log = LogFactory.getLog(RuleController.class);
	@Autowired
	private RuleService ruleService35;
	@Autowired
	private DisposalService disposalService;
	@Autowired
	private ObjectMapper objectMapper = null;
	
	/**
	* 方法描述:规则列表页面（已废弃）
	* @return
	 */
	@RequestMapping(value="/list",method=RequestMethod.GET)
	public String listRuleView() {
		return "tms35/rule/rule_list";
	}

	@RequestMapping(value="/list",method=RequestMethod.POST)
	public Model listRuleAction(@RequestParam Map<String,Object> reqs) {
		Model model = new Model();
		model.setRow(ruleService35.listRule(reqs));
		return model;
	}
	
	/**
	* 方法描述:保存动作配置
	* @param reqs
	* @return
	 */
	@RequestMapping(value="/save",method=RequestMethod.POST)
	public Model saveRuleAction(@RequestParam Map<String,Object> reqs) {
		
		String json = MapUtil.getString(reqs, "postData");
		
		Map<String,List<Map<String, ?>>> formList = null;
		try {
			formList = objectMapper.readValue(json, Map.class);
		} catch (Exception e) {
			log.error(e);
			throw new TmsMgrWebException("保存动作Json数据解析异常");
		} 
		Model m = new Model();
		try {
			m.setRow(ruleService35.saveRule(formList));
		} catch (Exception e) {
			m.addError(e.getMessage());
		}
		return m;
	}

	/**
	* 方法描述:查询处置方式下拉列表
	* @param category_id 代码
	* @param args sql参数
	* @return
	 */
	@RequestMapping(value="/disposal",method=RequestMethod.POST)
	public Model queryCode(@RequestParam Map<String,Object> reqs) {
		Model model = new Model();
		String txnid = MapUtil.getString(reqs, "txn_id");
		model.setRow(disposalService.queryList(txnid));
		return model;
	}
	
	/**
	* 方法描述:校验条件有效性
	* @param reqs
	* @return
	 */
	@RequestMapping(value="/check",method=RequestMethod.POST)
	public Model checkCondAction(@RequestParam Map<String,Object> reqs) {
		Model model = new Model();
		ruleService35.check(reqs);
		return model;
	}
	/**
	* 方法描述:删除、停用需要批量校验是否被引用
	* @param rule_id
	* @return
	 */
	@RequestMapping(value="/checkRef",method=RequestMethod.POST)
	public Model checkRefAction(@RequestParam String[] rule_ids,String oper) {
		Model model = new Model();
		ruleService35.batchCheckRef(rule_ids,oper);
		return model;
	}
	

	@RequestMapping(value="/getRuleId",method=RequestMethod.POST)
	public Model getRuleIdAction(@RequestParam Map<String,Object> reqs) {
		Model model = new Model();
		String ruleId = ruleService35.getRuleId();
		Map<String, Object> rule = new HashMap<String, Object>();
		rule.put(DBConstant.TMS_COM_RULE_RULE_ID, Long.parseLong(ruleId));
		rule.put(DBConstant.TMS_COM_RULE_RULE_SHORTDESC, MapUtil.getString(reqs, DBConstant.TMS_COM_RULE_RULE_SHORTDESC));
		rule.put(DBConstant.TMS_COM_RULE_RULE_NAME, MapUtil.getString(reqs, DBConstant.TMS_COM_RULE_RULE_NAME));
		rule.put(DBConstant.TMS_COM_RULE_RULE_DESC,  MapUtil.getString(reqs, DBConstant.TMS_COM_RULE_RULE_DESC));
		rule.put(DBConstant.TMS_COM_RULE_RULE_COND,  MapUtil.getString(reqs, DBConstant.TMS_COM_RULE_RULE_COND));
		rule.put(DBConstant.TMS_COM_RULE_RULE_COND_IN,  MapUtil.getString(reqs, DBConstant.TMS_COM_RULE_RULE_COND_IN));
		rule.put(DBConstant.TMS_COM_RULE_RULE_SCORE,  MapUtil.getString(reqs, DBConstant.TMS_COM_RULE_RULE_SCORE));
		rule.put(DBConstant.TMS_COM_RULE_RULE_ENABLE,  MapUtil.getInteger(reqs, DBConstant.TMS_COM_RULE_RULE_ENABLE));
		rule.put(DBConstant.TMS_COM_RULE_RULE_ISTEST,  StaticParameters.rule_istest.equals(MapUtil.getString(reqs, DBConstant.TMS_COM_RULE_RULE_ISTEST))?Integer.parseInt(StaticParameters.YES):Integer.parseInt(StaticParameters.NO));
		rule.put(DBConstant.TMS_COM_RULE_RULE_TXN,  MapUtil.getString(reqs, DBConstant.TMS_COM_RULE_RULE_TXN));
		rule.put(DBConstant.TMS_COM_RULE_RULE_TIMESTAMP,  System.currentTimeMillis());
		rule.put("POSITION",  MapUtil.getString(reqs, "X") + "a" + MapUtil.getString(reqs, "Y"));
		rule.put("DIRECTIONAL",  new ArrayList<String>());
		model.set("RULE", RuleJsonUtil.map2JsonAll(rule));
		return model;
	}


	@RequestMapping(value="/getRule",method=RequestMethod.POST)
	public Model getRuleAction(@RequestParam Map<String,Object> reqs) {
		Model model = new Model();
		model.setRow(ruleService35.getRule(reqs));
		return model;
	}


	
}
