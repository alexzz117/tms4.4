/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.strategy.controller;

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
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.strategy.service.StrategyService;

/**
 * 功能/模块:策略控制类
 * @author zlq
 * @version 1.0  Mar 13, 2015
 * @author zhang.lei
 */

@RestController("strategyController35")
@RequestMapping(ManagerConstants.URI_PREFIX + "/strategy")
public class StrategyController {
	
	private static final Logger logger = LoggerFactory.getLogger( StrategyController.class );
	@Autowired
	private StrategyService strategyService35;
	@Autowired
	private ObjectMapper objectMapper;

	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model listStrategyAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.setRow( strategyService35.listStrategy( reqs ) );
		return model;
	}

	@RequestMapping(value = "/refList", method = RequestMethod.POST)
	public Model listRuleRefStrategyAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.setRow( strategyService35.listStrategyByRuleid( reqs ) );
		return model;
	}

	/**
	* 方法描述:保存动作配置
	* @param reqs
	* @return
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public Model saveStrategyAction( @RequestBody Map<String, Object> reqs ) {
		String json = MapUtil.getString( reqs, "postData" );
		Map<String, List<Map<String, ?>>> formList = null;
		try {
			formList = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
			logger.error( e.getMessage(), e );
			throw new TmsMgrWebException( "保存策略Json数据解析异常" );
		}
		Model m = new Model();
		try {
			m.setRow( strategyService35.saveStrategy( formList ) );
		}
		catch( Exception e ) {
			m.addError( e.getMessage() );
		}
		return m;
	}

	/**
	* 方法描述:策略的规则列表
	* @param reqs
	* @return
	 */
	@RequestMapping(value = "/rulelist", method = RequestMethod.POST)
	public Model listSrListAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.setRow( strategyService35.listStrategyRule( reqs ) );
		return model;
	}

	/**
	 * 策略的规则删除交易，支持批量删除
	 * @param arrs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/saveStrategyRule", method = RequestMethod.POST)
	public Model delStrategyRuleActoin( @RequestBody Map<String, Object> reqs ) {
		String json = MapUtil.getString( reqs, "postData" );
		Map<String, List<Map<String, String>>> formList = null;
		try {
			formList = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
			e.printStackTrace();
			throw new TmsMgrWebException( "保存规则Json数据解析异常" );
		}

		Model m = new Model();
		try {
			strategyService35.saveStrategyRule( formList );
		}
		catch( Exception e ) {
			System.out.println( e );
			m.addError( e.getMessage() );
		}
		return m;
	}
}
