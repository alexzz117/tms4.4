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
import cn.com.higinet.tms.manager.modules.dfp.service.DfpService;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrWebException;

@Controller("dfpController")
@RequestMapping("/tms/dfp")
public class DfpController {
	
	private static Log log = LogFactory.getLog(DfpController.class);
	
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DfpService dfpService;

	/**
	* 方法描述:设备指纹页签
	* @return
	 */
	@RequestMapping(value="/dfpTab",method=RequestMethod.GET)
	public String listActionView() {
		return "tms35/dfp/dfp_tab";
	}
	
	/**
	* 方法描述:查询应用列表
	* @param reqs
	* @return
	 */
	@RequestMapping(value="/appList",method=RequestMethod.POST)
	public Model listAppAction(@RequestParam Map<String,Object> reqs) {
		Model model = new Model();
		List<Map<String,Object>> statList = dfpService.appList(reqs);
		model.setRow(statList);
//		model.set("channleList", dfpService.channleAllList());
		return model;
	}
	
	/**
	* 方法描述:保存应用
	* @param reqs
	* @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/appSave",method=RequestMethod.POST)
	public Model saveStatAction(@RequestParam Map<String,Object> reqs) {
		
		String json = MapUtil.getString(reqs, "postData");
		
		Map<String,List<Map<String, ?>>> formList = null;
		try {
			formList = objectMapper.readValue(json, Map.class);
		} catch (Exception e) {
			log.error(e);
			throw new TmsMgrWebException("保存应用Json数据解析异常");
		} 
		
		Model m = new Model();
		try {
			m.setRow(dfpService.saveApp(formList));
		} catch (Exception e) {
			log.error(e);
			m.addError(e.getMessage());
		}
		
		return m;
	}

	/**
	 * 方法描述:查询属性列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/proList",method=RequestMethod.POST)
	public Model listProAction(@RequestParam Map<String,Object> reqs) {
		Model model = new Model();
		List<Map<String,Object>> statList = dfpService.proList(reqs);
		model.setRow(statList);
		return model;
	}
	
	/**
	 * 方法描述:保存属性
	 * @param reqs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/proSave",method=RequestMethod.POST)
	public Model saveProAction(@RequestParam Map<String,Object> reqs) {
		
		String json = MapUtil.getString(reqs, "postData");
		
		Map<String,List<Map<String, ?>>> formList = null;
		try {
			formList = objectMapper.readValue(json, Map.class);
		} catch (Exception e) {
			log.error(e);
			throw new TmsMgrWebException("保存应用Json数据解析异常");
		} 
		
		Model m = new Model();
		try {
			m.setRow(dfpService.savePro(formList));
		} catch (Exception e) {
			log.error(e);
			m.addError(e.getMessage());
		}
		
		return m;
	}
	/**
	 * 方法描述:查询属性列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/appProList",method=RequestMethod.POST)
	public Model listAppProAction(@RequestParam Map<String,Object> reqs) {
		Model model = new Model();
		List<Map<String,Object>> statList = dfpService.appProList(reqs);
		model.setRow(statList);
		model.set("appList", dfpService.appList(reqs));
		model.set("proList", dfpService.proList(reqs));
		return model;
	}
	
	/**
	 * 方法描述:保存属性
	 * @param reqs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/appProSave",method=RequestMethod.POST)
	public Model saveAppProAction(@RequestParam Map<String,Object> reqs) {
		
		String json = MapUtil.getString(reqs, "postData");
		
		Map<String,List<Map<String, ?>>> formList = null;
		try {
			formList = objectMapper.readValue(json, Map.class);
		} catch (Exception e) {
			log.error(e);
			throw new TmsMgrWebException("保存应用Json数据解析异常");
		} 
		
		Model m = new Model();
		try {
			m.setRow(dfpService.saveAppPro(formList));
		} catch (Exception e) {
			log.error(e);
			m.addError(e.getMessage());
		}
		
		return m;
	}

//	@RequestMapping(value="/channleList",method=RequestMethod.POST)
//	public Model channleListAction(@RequestParam Map<String,Object> reqs) {
//		Model m = new Model();
//		m.setRow(dfpService.channleAllList());
//		return m;
//	}
	
	@RequestMapping(value="/exportJs", method=RequestMethod.GET)
	public void exportJsAction(@RequestParam Map<String, String> reqs, HttpServletResponse response){
		
		String app_id = MapUtil.getString(reqs, "app_id");

		try {
			dfpService.createJsFile(response,app_id );
		} catch (IOException e) {
			e.printStackTrace();
			throw new TmsMgrWebException("生成JS文件出错");
		}
	}
	
	/**
	 * 导出日志
	 * @param reqs
	 * @param response
	 */
	@RequestMapping(value="/exportLog", method=RequestMethod.POST)
	public Model exportLog(@RequestParam Map<String, String> reqs,HttpServletRequest request){
		Model model = new Model();
		String app_name = MapUtil.getString(reqs, "app_name");
		request.setAttribute("app_name", app_name);
		return model;
	}
	
	/**
	 * 解除用户和设备绑定关系
	 * @param reqs
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/unbind", method=RequestMethod.POST)
	public Model unbindUserDeviceRel(@RequestParam Map<String, String> reqs,HttpServletRequest request) {
		Model model = new Model();
		String json = MapUtil.getString(reqs, "postData");
		List<Map<String, ?>> formList = null;
		try {
			formList = objectMapper.readValue(json, List.class);
		} catch (Exception e) {
			throw new TmsMgrWebException("解除用户与设备绑定关系Json数据解析异常");
		} 
		dfpService.unbindUserDeviceRel(formList);
		return model;
	}
}