/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.strategy.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.strategy.service.StrategyService;

/**
 * 功能/模块:策略控制类
 * @author zlq
 * @version 1.0  Mar 13, 2015
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
@Controller("strategyController35")
@RequestMapping("/tms/strategy")
public class StrategyController {
	
	private static Log log = LogFactory.getLog(StrategyController.class);
	
	@Autowired
	private StrategyService strategyService35;
	@Autowired
	private ObjectMapper objectMapper = null;
	

	@RequestMapping(value="/list",method=RequestMethod.POST)
	public Model listStrategyAction(@RequestParam Map<String,Object> reqs) {
		Model model = new Model();
		model.setRow(strategyService35.listStrategy(reqs));
		return model;
	}
	
	
	@RequestMapping(value="/refList",method=RequestMethod.POST)
	public Model listRuleRefStrategyAction(@RequestParam Map<String,Object> reqs) {
		Model model = new Model();
		model.setRow(strategyService35.listStrategyByRuleid(reqs));
		return model;
	}
	
	/**
	* 方法描述:保存动作配置
	* @param reqs
	* @return
	 */
	@RequestMapping(value="/save",method=RequestMethod.POST)
	public Model saveStrategyAction(@RequestParam Map<String,Object> reqs) {
		
		String json = MapUtil.getString(reqs, "postData");
		
		Map<String,List<Map<String, ?>>> formList = null;
		try {
			formList = objectMapper.readValue(json, Map.class);
		} catch (Exception e) {
			log.error(e);
			throw new TmsMgrWebException("保存策略Json数据解析异常");
		} 
		Model m = new Model();
		try {
			m.setRow(strategyService35.saveStrategy(formList));
		} catch (Exception e) {
			m.addError(e.getMessage());
		}
		return m;
	}
	/**
	* 方法描述:策略的规则列表
	* @param reqs
	* @return
	 */
	@RequestMapping(value="/rulelist",method=RequestMethod.POST)
	public Model listSrListAction(@RequestParam Map<String,Object> reqs) {
		Model model = new Model();
		model.setRow(strategyService35.listStrategyRule(reqs));
		return model;
	}

	/**
	 * 策略的规则删除交易，支持批量删除
	 * @param arrs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/saveStrategyRule",method=RequestMethod.POST)
	public Model delStrategyRuleActoin(@RequestParam Map<String,Object> reqs){
		String json = MapUtil.getString(reqs, "postData");
		Map<String,List<Map<String, String>>> formList = null;
		try {
			formList = objectMapper.readValue(json, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TmsMgrWebException("保存规则Json数据解析异常");
		} 
		
		Model m = new Model();
		try {
			strategyService35.saveStrategyRule(formList);
		} catch (Exception e) {
			System.out.println(e);
			m.addError(e.getMessage());
		}
		return m;
	}
}
