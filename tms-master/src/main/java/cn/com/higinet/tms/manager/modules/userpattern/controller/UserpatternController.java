/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.userpattern.controller;

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

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.util.MapWrap;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.stat.service.StatService;
import cn.com.higinet.tms.manager.modules.userpattern.service.UserPatternService;

/**
 * 功能/模块:
 * @author zhanglq
 * @version 1.0  Aug 29, 2013
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
@Controller("userpatternController")
@RequestMapping("/tms/userpattern")
public class UserpatternController {
	
	private static Log log = LogFactory.getLog(UserpatternController.class);

	@Autowired
	private UserPatternService userPatternService35;
	
	@Autowired
	private StatService statService;
	
	@Autowired
	private SimpleDao officialSimpleDao;
	
	@Autowired
	private ObjectMapper objectMapper = null;
	/**
	 * 方法描述:查询列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/list",method=RequestMethod.GET)
	public String listUserpatternAction() {
		return "/tms/userpattern/stat_list";
	}
	
	/**
	 * 方法描述:查询列表(已废弃）
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/userList",method=RequestMethod.GET)
	public String userListAction() {
		return "/tms/userpattern/user_list";
	}
	
	/**
	 * 方法描述:查询列表（已废弃）
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/userPatternList",method=RequestMethod.GET)
	public String userPatternListAction() {
		return "/tms/userpattern/userpattern_list";
	}
	
	/**
	 * 方法描述:查询统计列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/list",method=RequestMethod.POST)
	public Model listUserpatternAction(@RequestParam Map<String,Object> reqs) {
		
		Model model = new Model();
		model.setPage(userPatternService35.pageStatPattern(reqs));
		return model;
	}

	/**
	 * 方法描述:查询客户列表（已废弃)
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/userList",method=RequestMethod.POST)
	public Model userListAction(@RequestParam Map<String,Object> reqs) {
		
		Model model = new Model();
		model.setPage(userPatternService35.pageUser(reqs));
		return model;
	}
	/**
	 * 方法描述:查询自定义行为习惯列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/userPatternList",method=RequestMethod.POST)
	public Model userPatternListAction(@RequestParam Map<String,Object> reqs) {
		
		Long stat_id = MapUtil.getLong(reqs, "stat_id");
		
		Map<String,Object> statInfo = officialSimpleDao.retrieve("TMS_COM_STAT", MapWrap.map(DBConstant.TMS_COM_STAT_STAT_ID, stat_id).getMap());//statService.getOneStat(stat_id);
		
		Model model = new Model();
		
		model.set("statInfo", statInfo);
		
		model.setList(userPatternService35.queryUserPatternList(reqs,statInfo));
		return model;
	}
	/**
	 * 方法描述:查询自学习行为习惯列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/statPatternList",method=RequestMethod.POST)
	public Model statPatternListAction(@RequestParam Map<String,Object> reqs) {
		
		Model model = new Model();
		model.setList(userPatternService35.queryStatPatternList(reqs));
		return model;
	}
	/**
	 * 方法描述:查询统计信息
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/statInfo",method=RequestMethod.POST)
	public Model statInfoAction(@RequestParam Map<String,Object> reqs) {
		
		Model model = new Model();
		String statId = MapUtil.getString(reqs, "stat_id");
		model.setRow(statService.getOneStat(statId));
		return model;
	}
	
	/**
	* 方法描述:保存自定义行为习惯配置
	* @param reqs
	* @return
	 */
	@RequestMapping(value="/save",method=RequestMethod.POST)
	public Model saveStatAction(@RequestParam Map<String,Object> reqs) {
		
		String json = MapUtil.getString(reqs, "postData");
		
		Map<String,List<Map<String, ?>>> formList = null;
		try {
			formList = objectMapper.readValue(json, Map.class);
		} catch (Exception e) {
			log.error(e);
			throw new TmsMgrWebException("保存行为习惯Json数据解析异常");
		} 
		Model m = new Model();
		try {
			String userId = MapUtil.getString(reqs, "userId");
			String statId = MapUtil.getString(reqs, "statId");
			m.setRow(userPatternService35.saveUserPattern(formList,userId,statId));
		} catch (Exception e) {
			m.addError(e.getMessage());
			log.error("保存行为习惯错误，由于"+e);
		}
		return m;
	}

	/**
	 * 方法描述:国家列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/getCountry",method=RequestMethod.POST)
	public Model getCountryAction(@RequestParam Map<String,Object> reqs) {
		
		Model model = new Model();
		model.setList(userPatternService35.getCountry(reqs));
		return model;
	}
	
	/**
	 * 方法描述:地区列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/getRegion",method=RequestMethod.POST)
	public Model getRegionAction(@RequestParam Map<String,Object> reqs) {
		
		Model model = new Model();
		model.setList(userPatternService35.getRegion(reqs));
		return model;
	}
	
	/**
	 * 方法描述:城市列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/getCity",method=RequestMethod.POST)
	public Model getCityAction(@RequestParam Map<String,Object> reqs) {
		
		Model model = new Model();
		model.setList(userPatternService35.getCity(reqs));
		return model;
	}
	
}
