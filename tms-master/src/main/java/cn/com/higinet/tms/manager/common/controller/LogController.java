/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.common.DBConstant;
import cn.com.higinet.tms.manager.common.service.LogService;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;

/**
 * 日志控制类
 * @author zhangfg
 * @version 2.0.0, 2011-6-30
 */
@RestController("cmcLogController")
@RequestMapping("/cmc/log")
public class LogController {

	@Autowired
	@Qualifier("cmcLogService")
	private LogService logService;

	/**	 * 转向日志列表页面
	 * @return
	 */
	/*@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String listView() {
		return "cmc/log/log_list";
	}*/

	/**
	 * 日志列表数据
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model listAction( @RequestBody Map<String, String> reqs, HttpServletRequest request ) {
		Model model = new Model();
		Map<String, Object> map = new HashMap<String, Object>();
		map.putAll( reqs );

		//zhangfg 2012-08-29 获取当前操作员，查询日志时除管理员外，只能查看自己的操作日志
		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute( ManagerConstants.SESSION_KEY_OPERATOR );
		String operId = CmcStringUtil.objToString( operator.get( DBConstant.CMC_OPERATOR_OPERATOR_ID ) );
		map.put( DBConstant.CMC_OPERATOR_OPERATOR_ID, operId );

		model.setPage( logService.listLog( map ) );
		return model;
	}

	/**
	 * 转向查看页面
	 * @return
	 */
	/*@RequestMapping(value = "/view", method = RequestMethod.GET)
	public String logView() {
		return "cmc/log/log_view";
	}*/

	/**
	 * 获取日志对象
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/get")
	public Model getLogActoin( @RequestBody RequestModel modelMap ) {
		String logId = modelMap.getString( "logId" );
		if( Stringz.isEmpty( logId ) ) return new Model().addError( "logId is empty" );

		Model model = new Model();
		model.setRow( logService.getLog( logId ) );
		return model;
	}
}
