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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.common.CodeDict;
import cn.com.higinet.tms.manager.common.Func;
import cn.com.higinet.tms.manager.common.service.FuncService;

/**
 * 功能管理控制类
 * @author chenr
 * @version 2.0.0, 2011-6-30
 */
@Controller("cmcFuncController")
@RequestMapping("/cmc/func")
public class FuncController {

	@Autowired
	private FuncService funcService;

	@Autowired
	@Qualifier("cmcFunc")
	private Func cmcFunc;

	@Autowired
	@Qualifier("codeDict")
	CodeDict codeDict;

	/**
	 * 功能管理功能主视图
	 * @return
	 */
	@RequestMapping(value = "/tree", method = RequestMethod.GET)
	public String treeView() {
		return "cmc/func/func_tree";
	}

	/**
	 * 获取功能树列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/tree", method = RequestMethod.POST)
	public Model listAction( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		List<Map<String, Object>> list = funcService.listAllFuncs( reqs );
		List<Map<String, Object>> list2 = new ArrayList<Map<String, Object>>();
		for( int i = 0; i < list.size(); i++ ) {
			Map<String, Object> map = list.get( i );
			Map<String, Object> map2 = new HashMap<String, Object>();
			map2.put( "text", map.get( "FUNC_NAME" ) );
			map2.put( "func_type", map.get( "FUNC_TYPE" ) );
			map2.put( "id", map.get( "FUNC_ID" ) );
			map2.put( "fid", map.get( "PARENT_ID" ) );
			map2.put( "onum", map.get( "ONUM" ) );
			map2.put( "flag", map.get( "FLAG" ) );
			map2.put( "conf", map.get( "CONF" ) );
			map2.put( "menu", map.get( "MENU" ) );
			map2.put( "info", map.get( "INFO" ) );
			map2.put( "islog", map.get( "LOG_FLAG" ) );
			map2.put( "loguri", map.get( "LOG_URI" ) );
			map2.put( "logurlMethod", map.get( "LOG_METHOD" ) );
			map2.put( "isgrant", map.get( "GRANT_FLAG" ) );
			list2.add( map2 );
		}
		model.setList( list2 );
		return model;
	}

	/**
	 * 获取一个功能的数据
	 * @param funcId
	 * @return
	 */
	@Deprecated
	@RequestMapping("/{funcId}")
	public Model getFuncAction( @PathVariable String funcId ) {
		Model model = new Model();
		model.setRow( funcService.getFunc( funcId ) );
		return model;
	}

	/**
	 * 新建一个功能
	 * @param reqs
	 * @return
	 */
	@RequestMapping("/add")
	public Model addFuncAction( @RequestBody RequestModel reqs ) {
		Model model = new Model();
		String code = codeDict.getCode( "common.model", "runmode" );
		if( code != null && "development".equals( code ) ) {
			Map<String, Object> func = new HashMap<String, Object>();
			func.put( "FUNC_NAME", reqs.get( "func_name" ) );
			func.put( "FUNC_TYPE", reqs.get( "func_type" ) );
			func.put( "PARENT_ID", reqs.get( "parent_id" ) );
			func.put( "MENU", reqs.get( "menu" ) );
			func.put( "CONF", reqs.get( "conf" ) );
			func.put( "FLAG", reqs.get( "flag" ) );
			func.put( "ONUM", reqs.get( "onum" ) );
			func.put( "INFO", reqs.get( "info" ) );
			//用于记录日志的配置
			func.put( "LOG_FLAG", reqs.get( "islog" ) );
			func.put( "LOG_URI", reqs.get( "loguri" ) );
			func.put( "LOG_METHOD", reqs.get( "logurlMethod" ) );
			if( reqs.get( "isgrant" ) != null ) {
				func.put( "GRANT_FLAG", reqs.get( "isgrant" ) );
			}
			else {
				func.put( "GRANT_FLAG", "0" );
			}
			model.setRow( funcService.createFunc( func ) );
			cmcFunc.reload();
		}
		else {
			model.addError( "error.cmc.func.runmode.msg" );
		}
		return model;
	}

	/**
	 * 修改一个功能
	 * @param reqs
	 * @return
	 */
	@RequestMapping("/mod")
	public Model modFuncAction( @RequestBody RequestModel reqs ) {
		Map<String, Object> func = new HashMap<String, Object>();
		func.put( "FUNC_NAME", reqs.get( "func_name" ) );
		func.put( "FUNC_ID", reqs.get( "func_id" ) );
		func.put( "MENU", reqs.get( "menu" ) );
		func.put( "CONF", reqs.get( "conf" ) );
		func.put( "FLAG", reqs.get( "flag" ) );
		func.put( "ONUM", reqs.get( "onum" ) );
		func.put( "INFO", reqs.get( "info" ) );
		//用于记录日志的配置
		func.put( "LOG_FLAG", reqs.get( "islog" ) );
		func.put( "LOG_URI", reqs.get( "loguri" ) );
		func.put( "LOG_METHOD", reqs.get( "logurlMethod" ) );
		if( reqs.get( "isgrant" ) != null ) {
			func.put( "GRANT_FLAG", reqs.get( "isgrant" ) );
		}
		else {
			func.put( "GRANT_FLAG", "0" );
		}
		funcService.updateFunc( func );
		String flag = reqs.get( "flag" ).toString();
		if( "0".equals( flag ) ) {
			funcService.modSubFuncs( reqs.get( "func_id" ).toString() );
		}
		if( "1".equals( flag ) ) {
			funcService.modParentFuncs( reqs.get( "func_id" ).toString() );
		}
		cmcFunc.reload();
		return Model.emptyModel();
	}

	/**
	 * 删除一个功能
	 * @param funcId
	 * @return
	 */
	@RequestMapping("/del")
	public Model delFuncAction( @RequestBody RequestModel modelMap, HttpServletRequest request ) {
		String funcId = modelMap.getString( "funcId" );
		if( Stringz.isEmpty( funcId ) ) return Model.emptyModel().addError( "funcId is empty" );

		Model model = Model.emptyModel();
		String code = codeDict.getCode( "common.model", "runmode" );
		String funcName = funcService.delFuncName( funcId );
		request.setAttribute( "funcName", funcName );
		if( code != null && "development".equals( code ) ) {
			boolean flag = funcService.deleteFunc( funcId );
			if( !flag ) {
				model.addError( "error.cmc.func.delmsg" );
			}
			cmcFunc.reload();
		}
		else {
			model.addError( "error.cmc.func.runmode.msg" );
		}
		return model;
	}

	@RequestMapping(value = "/getAll", method = RequestMethod.POST)
	public Model getAllFuncAction( @RequestBody RequestModel modelMap ) {
		List<Map<String, Object>> funcOptList = new ArrayList<Map<String, Object>>();
		funcService.listAllFuncsSort( funcOptList );
		return new Model().setRow( funcOptList );
	}
}
