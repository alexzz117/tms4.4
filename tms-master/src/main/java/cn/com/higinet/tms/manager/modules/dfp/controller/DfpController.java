/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.dfp.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.com.higinet.tms.base.entity.common.Page;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import cn.com.higinet.tms.manager.modules.dfp.service.DfpService;

@RestController("dfpController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/dfp")
public class DfpController {

	private static Log log = LogFactory.getLog( DfpController.class );

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DfpService dfpService;

	/**
	* 方法描述:设备指纹页签
	* @return
	 */
	@RequestMapping(value = "/dfpTab", method = RequestMethod.GET)
	public String listActionView() {
		return "tms35/dfp/dfp_tab";
	}

	/**
	* 方法描述:查询应用列表
	* @param reqs
	* @return
	 */
	@RequestMapping(value = "/appList", method = RequestMethod.POST)
	public Model listAppAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		List<Map<String, Object>> statList = dfpService.appList( reqs );
		model.setRow( statList );
		//		model.set("channleList", dfpService.channleAllList());
		return model;
	}

	/**
	* 方法描述:保存应用
	* @param reqs
	* @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/appSave", method = RequestMethod.POST)
	public Model saveStatAction( @RequestBody Map<String, Object> reqs ) {

		String json = MapUtil.getString( reqs, "postData" );

		Map<String, List<Map<String, ?>>> formList = null;
		try {
			formList = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
			log.error( e );
			throw new TmsMgrWebException( "保存应用Json数据解析异常" );
		}

		Model m = new Model();
		try {
			m.setRow( dfpService.saveApp( formList ) );
		}
		catch( Exception e ) {
			log.error( e );
			m.addError( e.getMessage() );
		}

		return m;
	}

	/**
	 * 方法描述:查询属性列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/proList", method = RequestMethod.POST)
	public Model listProAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		Page<Map<String, Object>> statList = dfpService.proListPage( reqs );
		model.setPage( statList );
		return model;
	}

	/**
	 * 方法描述:保存属性
	 * @param reqs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/proSave", method = RequestMethod.POST)
	public Model saveProAction( @RequestBody Map<String, Object> reqs ) {

		String json = MapUtil.getString( reqs, "postData" );

		Map<String, List<Map<String, ?>>> formList = null;
		try {
			formList = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
			log.error( e );
			throw new TmsMgrWebException( "保存应用Json数据解析异常" );
		}

		Model m = new Model();
		try {
			m.setRow( dfpService.savePro( formList ) );
		}
		catch( Exception e ) {
			log.error( e );
			m.addError( e.getMessage() );
		}

		return m;
	}

	/**
	 * 方法描述:查询属性列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/appProList", method = RequestMethod.POST)
	public Model listAppProAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		Page<Map<String, Object>> statList = dfpService.appProListPage( reqs );
		model.setPage( statList );
		model.set( "appList", dfpService.appList( reqs ) );
		model.set( "proList", dfpService.proList( reqs ) );
		return model;
	}

	/**
	 * 方法描述:保存属性
	 * @param reqs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/appProSave", method = RequestMethod.POST)
	public Model saveAppProAction( @RequestBody Map<String, Object> reqs ) {

		String json = MapUtil.getString( reqs, "postData" );

		Map<String, List<Map<String, ?>>> formList = null;
		try {
			formList = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
			log.error( e );
			throw new TmsMgrWebException( "保存应用Json数据解析异常" );
		}

		Model m = new Model();
		try {
			m.setRow( dfpService.saveAppPro( formList ) );
		}
		catch( Exception e ) {
			log.error( e );
			m.addError( e.getMessage() );
		}

		return m;
	}

	@RequestMapping(value = "/exportJs", method = RequestMethod.GET)
	public void exportJsAction( @RequestBody Map<String, String> reqs, HttpServletResponse response ) {

		String app_id = MapUtil.getString( reqs, "app_id" );

		try {
			dfpService.createJsFile( response, app_id );
		}
		catch( IOException e ) {
			e.printStackTrace();
			throw new TmsMgrWebException( "生成JS文件出错" );
		}
	}

	/**
	 * 导出日志
	 * @param reqs
	 * @param response
	 */
	@RequestMapping(value = "/exportLog", method = RequestMethod.POST)
	public Model exportLog( @RequestBody Map<String, String> reqs, HttpServletRequest request ) {
		Model model = new Model();
		String app_name = MapUtil.getString( reqs, "app_name" );
		request.setAttribute( "app_name", app_name );
		return model;
	}

	/**
	 * 解除用户和设备绑定关系
	 * @param reqs
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/unbind", method = RequestMethod.POST)
	public Model unbindUserDeviceRel( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		String json = MapUtil.getString( reqs, "postData" );
		List<Map<String, ?>> formList = null;
		try {
			formList = objectMapper.readValue( json, List.class );
		}
		catch( Exception e ) {
			throw new TmsMgrWebException( "解除用户与设备绑定关系Json数据解析异常" );
		}
		dfpService.unbindUserDeviceRel( formList );
		return model;
	}
}