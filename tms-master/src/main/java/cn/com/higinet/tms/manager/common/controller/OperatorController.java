/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common.controller;

import java.util.ArrayList;
import java.util.List;
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
import cn.com.higinet.tms.manager.common.service.OperatorService;
import cn.com.higinet.tms.manager.common.util.CmcMapUtil;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;

/**
 * 操作员控制类
 * <ul>
 * 	<li>/list ： 操作员列表视图&数据</li>
 *  <li>/get ：查看操作员信息</li>
 *  <li>/add ：新建操作员</li>
 *  <li>/mod ：修改操作员</li>
 * </ul>
 * @author chenr
 * @version 2.0.0, 2011-6-30
 */
@RestController("cmcOperatorController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/operator")
public class OperatorController {

	@Autowired
	@Qualifier("cmcOperatorService")
	private OperatorService operatorService;

	/**
	 * 操作员列表视图
	 * @return
	 */
	/*@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String listView() {
		return "cmc/operator/operator_list";
	}*/

	/**
	 * 操作员列表
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model listActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.setPage( operatorService.listOperator( reqs ) );
		return model;
	}

	/**
	 * 操作员添加视图
	 * @return
	 */
	/*@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String addOperatorView() {
		return "cmc/operator/operator_add";
	}*/

	/**
	 * 操作员添加
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Model addOperatorActoin( @RequestBody Map<String, Object> reqs, HttpServletRequest request ) {

		Model model = new Model();
		String roleId = CmcMapUtil.getString( reqs, "role" );
		Map<String, Object> map = operatorService.getRole( roleId );
		request.setAttribute( DBConstant.CMC_ROLE_ROLE_NAME, map.get( DBConstant.CMC_ROLE_ROLE_NAME ) );
		model.setRow( operatorService.createOperator( reqs ) );
		return model;
	}

	/**
	 * 操作员删除
	 * @param operatorId
	 * @return
	 */
	@RequestMapping(value = "/del")
	public Model delOperatorAction( @RequestBody String[] operatorId, HttpServletRequest request ) {
		Model model = new Model();
		if( operatorId != null && operatorId.length > 0 ) {
			Map<String, Object> map = operatorService.getOperator( operatorId[0] );
			request.setAttribute( DBConstant.CMC_OPERATOR_LOGIN_NAME, map.get( DBConstant.CMC_OPERATOR_LOGIN_NAME ) );
		}
		if( operatorId != null && operatorId.length > 0 ) {
			operatorService.deleteOperator( operatorId[0] );
		}
		return model;
	}

	/**
	 * 操作员编辑视图
	 * @return
	 */
	/*@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public String editOperatorView() {
		return "cmc/operator/operator_edit";
	}*/

	/**
	 * 操作员编辑对象
	 * @return
	 */
	@RequestMapping(value = "/get")
	public Model getRoleActoin( @RequestBody RequestModel modelMap ) {
		Model model = new Model();
		if( Stringz.isEmpty( modelMap.getString( "operatorId" ) ) ) return model.addError( "operatorId is empty" );
		model.setRow( operatorService.getOperator( modelMap.getString( "operatorId" ) ) );
		return model;
	}

	/**
	 * 操作员更新
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/mod", method = RequestMethod.POST)
	public Model updateOperatorActoin( @RequestBody Map<String, String> reqs, HttpServletRequest request ) {
		Map<String, Object> operator = operatorService.getOperator( reqs.get( "operator_id" ) );
		Model model = new Model();
		//记录分配角色日志
		String roleId = CmcMapUtil.getString( reqs, "role" );
		Map<String, Object> map = operatorService.getRole( roleId );
		request.setAttribute( DBConstant.CMC_OPERATOR_LOGIN_NAME, operator.get( DBConstant.CMC_OPERATOR_LOGIN_NAME ) );
		request.setAttribute( DBConstant.CMC_ROLE_ROLE_NAME, map.get( DBConstant.CMC_ROLE_ROLE_NAME ) );
		if( !"".equals( reqs.get( "password" ) ) ) {
			operator.put( "PASSWORD", reqs.get( "password" ) );
		}
		operator.put( "FLAG", reqs.get( "flag" ) );
		operator.put( "REAL_NAME", reqs.get( "real_name" ) );
		operator.put( "PHONE", reqs.get( "phone" ) );
		operator.put( "MOBILE", reqs.get( "mobile" ) );
		operator.put( "EMAIL", reqs.get( "email" ) );
		operator.put( "ADDRESS", reqs.get( "address" ) );
		operator.put( "MEMO", reqs.get( "memo" ) );
		operator.put( "ROLE", reqs.get( "role" ) );
		operator.put( "CREDENTIALTYPE", reqs.get( "credentialtype" ) );
		operator.put( "CREDENTIALNUM", reqs.get( "credentialnum" ) );
		operatorService.updateOperator( operator );
		return model;
	}

	/**
	 * 分配操作员角色视图
	 * @return
	 */
	/*@RequestMapping(value = "/grant", method = RequestMethod.GET)
	public String assignOperatorRoleView() {
		return "cmc/operator/operator_grant";
	}*/

	/**
	 * 获取操作员当期的授权情况
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/granted", method = RequestMethod.POST)
	public Model listRole( @RequestBody RequestModel modelMap ) {
		Model model = new Model();
		String operatorId = modelMap.getString( "operatorId" );
		if( Stringz.isEmpty( operatorId ) ) return model.addError( "operatorId is empty" );
		model.set( "olist", operatorService.listOperatorNotAssignRole( operatorId ) );
		model.set( "slist", operatorService.listOperatorAssignRole( operatorId ) );
		return model;
	}

	/**
	 * 为操作员分配角色
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/grant", method = RequestMethod.POST)
	public Model assignOperatorRoleActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		String operatorId = reqs.get( "operator_id" );
		String tarvalue = reqs.get( "tarvalue" );
		String roleIds[] = new String[0];
		if( tarvalue != null && tarvalue.length() > 0 ) {
			roleIds = reqs.get( "tarvalue" ).split( "," );
		}
		operatorService.grantOperator( operatorId, roleIds );
		return model;
	}

	/**
	 * 检查用户名是否重复
	 * @return
	 */
	@RequestMapping(value = "/check/username", method = RequestMethod.POST)
	public Model checkUserNameAction( @RequestBody RequestModel modelMap ) {
		Model model = new Model();
		String username = modelMap.getString( "username" );
		if( Stringz.isEmpty( username ) ) return model.addError( "username is empty" );

		boolean flag = operatorService.hasUserName( username );
		if( flag ) {
			model.set( "checke_result", "false" );
		}
		else {
			model.set( "checke_result", "true" );
		}
		return model;
	}

	@RequestMapping(value = "/reset", method = RequestMethod.POST)
	public Model resetPwd( @RequestBody RequestModel modelMap, HttpServletRequest request ) {
		Model model = new Model();

		String operatorId = modelMap.getString( "operatorId" );
		String loginName = modelMap.getString( "loginName" );
		String passWord = modelMap.getString( "passWord" );
		if( Stringz.isEmpty( operatorId, loginName, passWord ) ) {
			model.addError( "operatorId or loginName or passWord is empty" );
		}

		boolean flag = operatorService.resetPwd( operatorId, passWord );
		request.setAttribute( DBConstant.CMC_OPERATOR_LOGIN_NAME, loginName );
		if( flag ) {
			model.set( "result", "true" );
		}
		else {
			model.set( "result", "false" );
		}
		return model;
	}

	@RequestMapping(value = "/getAll", method = RequestMethod.POST)
	public Model getUsers( HttpServletRequest request ) {
		Model model = new Model();
		//zhangfg 2012-11-8 获取当前操作员，查询日志时除管理员外，只能查看自己的操作日志
		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute( ManagerConstants.SESSION_KEY_OPERATOR );
		Map<String, Object> operatorInfo = operatorService.getOperator( (String) operator.get( "OPERATOR_ID" ) );
		String operId = CmcStringUtil.objToString( operatorInfo.get( DBConstant.CMC_OPERATOR_OPERATOR_ID ) );
		if( !CmcStringUtil.isBlank( operId ) && !"1".equals( operId ) ) {//除超级管理员外，只能查看自己的操作日志
			List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
			rows.add( operatorInfo );
			model.setRow( rows );
			model.set( "count", "1" );
		}
		else {
			model.setRow( operatorService.getUsers() );
		}
		return model;
	}

}
