/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.com.higinet.tms.manager.common.ManagerConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.manager.common.DBConstant;
import cn.com.higinet.tms.manager.common.service.FuncService;
import cn.com.higinet.tms.manager.common.service.RoleService;

/**
 * 角色管理控制类
 * @author chenr
 * @version 2.0.0, 2011-6-30
 * 
 * @author zhang.lei
 */
@RestController("cmcRoleController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/role")
public class RoleController {

	@Autowired
	@Qualifier("cmcRoleService")
	private RoleService roleService;

	@Autowired
	@Qualifier("cmcFuncService")
	private FuncService funcService;

	/**
	 * 角色信息列表视图
	 * @return
	 */
	/*@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String listRoleView() {
		return "cmc/role/role_list";
	}*/

	/**
	 * 角色信息列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model listRoleActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.setPage( roleService.listRole( reqs ) );
		return model;
	}

	/**
	 * 查询角色信息
	 * @param reqs
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/listAll", method = RequestMethod.POST)
	public Model listRole( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.setRow( roleService.listAllRole( reqs ) );
		return model;
	}

	/**
	 * 查询所有未停用的角色信息
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/listNormalRole", method = RequestMethod.POST)
	public Model listNormalRole( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.setRow( roleService.listNormalRole( reqs ) );
		return model;
	}

	/**
	 * 角色增加视图
	 * @return
	 */
	/*@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String addRoleView() {
		return "cmc/role/role_add";
	}*/

	/**
	 * 角色增加
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Model addRoleActoin( @RequestBody Map<String, String> reqs, HttpServletRequest request ) {
		Map<String, Object> role = new HashMap<String, Object>();
		Model model = new Model();
		role.put( "ROLE_NAME", reqs.get( "role_name" ) );
		role.put( "FLAG", reqs.get( "flag" ) );
		role.put( "INFO", reqs.get( "info" ) );
		request.setAttribute( "role_name", reqs.get( "role_name" ) );
		model.setRow( roleService.createRole( role ) );
		return model;
	}

	/**
	 * 角色删除
	 * @param roleIds
	 * @return
	 */
	@RequestMapping(value = "/del")
	public Model delRoleActoin( @RequestParam("roleId") String[] roleIds, HttpServletRequest request ) {
		Model model = new Model();
		if( roleIds != null && roleIds.length > 0 ) {
			Map<String, Object> map = roleService.getRole( roleIds[0] );
			request.setAttribute( "role_name", map.get( DBConstant.CMC_ROLE_ROLE_NAME ) );
			boolean flag = roleService.delRole( roleIds[0] );
			if( flag ) {
				model.addError( "角色已被分配，不能删除！" );
				return model;
			}
		}
		if( roleIds != null && roleIds.length > 0 ) {
			roleService.deleteRole( roleIds[0] );
		}
		return model;
	}

	/**
	 * 角色编辑视图
	 * @return
	 */
	/*@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public String editOperatorView() {
		return "cmc/role/role_edit";
	}*/

	/**
	 * 操作员编辑信息列表
	 * @return
	 */
	@RequestMapping(value = "/get")
	public Model getRoleActoin( @RequestBody RequestModel modelMap ) {
		Model model = new Model();
		model.setRow( roleService.getRole( modelMap.getString( "roleId" ) ) );
		return model;
	}

	/**
	 * 角色更新
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/mod", method = RequestMethod.POST)
	public Model updateOperatorActoin( @RequestBody Map<String, String> reqs, HttpServletRequest request ) {
		Map<String, Object> role = roleService.getRole( reqs.get( "role_id" ) );
		Model model = new Model();
		role.put( "ROLE_NAME", reqs.get( "role_name" ) );
		role.put( "FLAG", reqs.get( "flag" ) );
		role.put( "INFO", reqs.get( "info" ) );
		request.setAttribute( "role_name", reqs.get( "role_name" ) );
		roleService.updateRole( role );
		return model;
	}

	/**
	 * 角色授权视图
	 * @return
	 */
	/*@RequestMapping(value = "/grant/list", method = RequestMethod.GET)
	public String grantView() {
		return "cmc/role/role_grant";
	}*/

	/**
	 * 角色授权功能列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/grant/list", method = RequestMethod.POST)
	public Model grantActoin( @RequestParam String roleId ) {
		Model model = new Model();
		//TODO 这里只读取到功能层(ftype=2)
		List<Map<String, Object>> list = roleService.getFuncsForGrant( roleId );
		List<Map<String, Object>> list2 = new ArrayList<Map<String, Object>>();
		for( int i = 0; i < list.size(); i++ ) {
			Map<String, Object> map = list.get( i );
			Map<String, Object> map2 = new HashMap<String, Object>();
			map2.put( "text", map.get( "FUNC_NAME" ) );
			map2.put( "ftype", map.get( "FUNC_TYPE" ) );
			map2.put( "id", map.get( "FUNC_ID" ) );
			map2.put( "fid", map.get( "PARENT_ID" ) );
			map2.put( "onum", map.get( "ONUM" ) );
			map2.put( "flag", map.get( "FLAG" ) );
			map2.put( "menu", map.get( "MENU" ) );
			map2.put( "grant", map.get( "GRANT" ) );
			map2.put( "grantflag", map.get( "GRANT_FLAG" ) );
			list2.add( map2 );
		}
		model.setList( list2 );
		return model;
	}

	/**
	 * 修改角色权限
	 * @param roleId
	 * @param funcIds
	 * @return
	 */
	@RequestMapping(value = "/grant/mod", method = RequestMethod.POST)
	public Model modGrantActoin( @RequestParam String roleId, @RequestParam("funcId") String[] funcIds, HttpServletRequest request ) {
		Model model = new Model();
		Map<String, Object> map = roleService.getRole( roleId );
		request.setAttribute( "role_name", map.get( DBConstant.CMC_ROLE_ROLE_NAME ) );
		String funcNameStr = "";
		for( String funcId : funcIds ) {
			String funcName = (String) funcService.getFunc( funcId ).get( "FUNC_NAME" );
			funcNameStr = funcNameStr + funcName + ";";
		}
		request.setAttribute( "funcNameStr", funcNameStr );
		roleService.updateRoleGrant( roleId, funcIds );
		return model;
	}

	/**
	 * 检查角色名是否重复
	 * @return
	 */
	@RequestMapping(value = "/checkUserName", method = RequestMethod.POST)
	public Model checkeUserNameAction( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		boolean flag = roleService.listRoleName( reqs );
		if( flag == true ) {
			model.set( "checke_result", "true" );
		}
		else {
			model.set( "checke_result", "false" );
		}
		return model;
	}
	/**
	 * 检查角色是否被分配
	 * @param reqs
	 * @return
	 */
	/*	@RequestMapping(value="/checkRole", method=RequestMethod.POST)
		public Model checkRole(@RequestBody Map<String, String> reqs){
			Model model = new Model();
			boolean flag = roleService.delRole(reqs);
			if(flag == true){
				model.set("result", "true");
			}else{
				model.set("result", "false");
			}
			return model;
		}
		*/
}
