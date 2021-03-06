/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.rule.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.RuleJsonUtil;
import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.rule.service.RuleService;
import cn.com.higinet.tms.manager.modules.tmsreport.service.DisposalService;

/**
 * 功能/模块:
 * @author 张立群
 * @version 1.0  May 21, 2013
 * @author zhang.lei
 */

@RestController("ruleController35")
@RequestMapping(ManagerConstants.URI_PREFIX + "/rule")
public class RuleController {

	private static final Logger log = LoggerFactory.getLogger( RuleController.class );
	
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
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String listRuleView() {
		return "tms35/rule/rule_list";
	}

	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model listRuleAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.setPage( ruleService35.listRule( reqs ) );
		return model;
	}

	/**
	* 方法描述:保存动作配置
	* @param reqs
	* @return
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public Model saveRuleAction( @RequestBody Map<String, Object> reqs ) {

		String json = MapUtil.getString( reqs, "postData" );

		Map<String, List<Map<String, ?>>> formList = null;
		try {
			formList = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
			log.error( e.getMessage(), e );
			throw new TmsMgrWebException( "保存动作Json数据解析异常" );
		}
		Model m = new Model();
		try {
			Map<String, List<Map<String, ?>>> formListUpperCase = saveParams2UpperCase(formList);
			m.setRow( ruleService35.saveRule( formListUpperCase ) );
		}
		catch( Exception e ) {
			m.addError( e.getMessage() );
		}
		return m;
	}

	/**
	* 方法描述:查询处置方式下拉列表
	* @return
	 */
	@RequestMapping(value = "/disposal", method = RequestMethod.POST)
	public Model queryCode( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		String txnid = MapUtil.getString( reqs, "txn_id" );
		model.setRow( disposalService.queryList( txnid ) );
		return model;
	}

	/**
	* 方法描述:校验条件有效性
	* @param reqs
	* @return
	 */
	@RequestMapping(value = "/check", method = RequestMethod.POST)
	public Model checkCondAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		ruleService35.check( reqs );
		return model;
	}

	/**
	* 方法描述:删除、停用需要批量校验是否被引用
	* @param rule_id
	* @return
	 */
	@RequestMapping(value = "/checkRef", method = RequestMethod.POST)
	public Model checkRefAction( @RequestBody String[] rule_ids, String oper ) {
		Model model = new Model();
		ruleService35.batchCheckRef( rule_ids, oper );
		return model;
	}

	@RequestMapping(value = "/getRuleId", method = RequestMethod.POST)
	public Model getRuleIdAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		String ruleId = ruleService35.getRuleId();
		Map<String, Object> rule = new HashMap<String, Object>();
		rule.put( DBConstant.TMS_COM_RULE_RULE_ID, Long.parseLong( ruleId ) );
		rule.put( DBConstant.TMS_COM_RULE_RULE_SHORTDESC, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_RULE_SHORTDESC ) );
		rule.put( DBConstant.TMS_COM_RULE_RULE_NAME, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_RULE_NAME ) );
		rule.put( DBConstant.TMS_COM_RULE_RULE_DESC, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_RULE_DESC ) );
		rule.put( DBConstant.TMS_COM_RULE_RULE_COND, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_RULE_COND ) );
		rule.put( DBConstant.TMS_COM_RULE_RULE_COND_IN, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_RULE_COND_IN ) );
		rule.put( DBConstant.TMS_COM_RULE_RULE_SCORE, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_RULE_SCORE ) );
		rule.put( DBConstant.TMS_COM_RULE_RULE_ENABLE, MapUtil.getInteger( reqs, DBConstant.TMS_COM_RULE_RULE_ENABLE ) );
		rule.put( DBConstant.TMS_COM_RULE_RULE_ISTEST, StaticParameters.rule_istest.equals( MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_RULE_ISTEST ) ) ? Integer.parseInt( StaticParameters.YES ) : Integer.parseInt( StaticParameters.NO ) );
		rule.put( DBConstant.TMS_COM_RULE_RULE_TXN, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_RULE_TXN ) );
		rule.put( DBConstant.TMS_COM_RULE_RULE_TIMESTAMP, System.currentTimeMillis() );
		rule.put( "POSITION", MapUtil.getString( reqs, "X" ) + "a" + MapUtil.getString( reqs, "Y" ) );
		rule.put( "DIRECTIONAL", new ArrayList<String>() );
		model.set( "RULE", RuleJsonUtil.map2JsonAll( rule ) );
		return model;
	}

	@RequestMapping(value = "/getRule", method = RequestMethod.POST)
	public Model getRuleAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.setRow( ruleService35.getRule( reqs ) );
		return model;
	}

	private Map<String, List<Map<String, ?>>> saveParams2UpperCase(Map<String, List<Map<String, ?>>> formMap) {
		Map<String, List<Map<String, ?>>> resultMap = new HashMap<>();
		List<Map<String, Object>> delList = MapUtil.getList( formMap, "del" );
		List<Map<String, Object>> modList = MapUtil.getList( formMap, "mod" );
		List<Map<String, Object>> addList = MapUtil.getList( formMap, "add" );
		List<Map<String, Object>> copyList = MapUtil.getList( formMap, "copy" );

		List<Map<String, Object>> validYList = MapUtil.getList( formMap, "valid-y" );
		List<Map<String, Object>> validNList = MapUtil.getList( formMap, "valid-n" );

		List<Map<String, ?>> delListTemp = new ArrayList<>();
		List<Map<String, ?>> modListTemp = new ArrayList<>();
		List<Map<String, ?>> addListTemp = new ArrayList<>();
		List<Map<String, ?>> copyListTemp = new ArrayList<>();
		List<Map<String, ?>> validYListTemp = new ArrayList<>();
		List<Map<String, ?>> validNListTemp = new ArrayList<>();

		if(delList != null){
			for(Map<String, Object> loop : delList) {
				Map<String, Object> loopMapTemp = new HashMap<>();
				for (Map.Entry<String, Object> entry : loop.entrySet()) {
					loopMapTemp.put(entry.getKey().toUpperCase(), entry.getValue());
				}
				delListTemp.add(loopMapTemp);
			}
			resultMap.put("del", delListTemp);
		}
		if(modList != null){
			for(Map<String, Object> loop : modList) {
				Map<String, Object> loopMapTemp = new HashMap<>();
				for (Map.Entry<String, Object> entry : loop.entrySet()) {
					loopMapTemp.put(entry.getKey().toUpperCase(), entry.getValue());
				}
				modListTemp.add(loopMapTemp);
			}
			resultMap.put("mod", modListTemp);
		}
		if(addList != null){
			for(Map<String, Object> loop : addList) {
				Map<String, Object> loopMapTemp = new HashMap<>();
				for (Map.Entry<String, Object> entry : loop.entrySet()) {
					loopMapTemp.put(entry.getKey().toUpperCase(), entry.getValue());
				}
				addListTemp.add(loopMapTemp);
			}
			resultMap.put("add", addListTemp);
		}
		if(copyList != null){
			for(Map<String, Object> loop : copyList) {
				Map<String, Object> loopMapTemp = new HashMap<>();
				for (Map.Entry<String, Object> entry : loop.entrySet()) {
					loopMapTemp.put(entry.getKey().toUpperCase(), entry.getValue());
				}
				copyListTemp.add(loopMapTemp);
			}
			resultMap.put("copy", copyListTemp);
		}
		if(validYList != null){
			for(Map<String, Object> loop : validYList) {
				Map<String, Object> loopMapTemp = new HashMap<>();
				for (Map.Entry<String, Object> entry : loop.entrySet()) {
					loopMapTemp.put(entry.getKey().toUpperCase(), entry.getValue());
				}
				validYListTemp.add(loopMapTemp);
			}
			resultMap.put("valid-y", validYListTemp);
		}
		if(validNList != null){
			for(Map<String, Object> loop : validNList) {
				Map<String, Object> loopMapTemp = new HashMap<>();
				for (Map.Entry<String, Object> entry : loop.entrySet()) {
					loopMapTemp.put(entry.getKey().toUpperCase(), entry.getValue());
				}
				validNListTemp.add(loopMapTemp);
			}
			resultMap.put("valid-n", validNListTemp);
		}
		return resultMap;
	}

}
