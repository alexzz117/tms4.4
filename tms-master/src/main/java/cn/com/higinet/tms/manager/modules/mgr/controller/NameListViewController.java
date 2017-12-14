package cn.com.higinet.tms.manager.modules.mgr.controller;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.mgr.service.NameListService;

/**
 * 名单管理业务类，包括名单的管理和名单值的管理
 * @author zhangfg
 *
 */
@Controller("nameListViewController")
@RequestMapping("/tms/mgrView")
public class NameListViewController {

	private static Log log = LogFactory.getLog(NameListViewController.class);
	
	@Autowired
	private NameListService nameListViewService;
	/**
	 * 名单列表查询页面
	 * @return
	 */
	@RequestMapping(value="/list", method=RequestMethod.GET)
	public String listNameListView(){
		return "tms/mgr/name_list_view";
	}

	/**
	 * 名单列表查询交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/list", method=RequestMethod.POST)
	public Model listNameListActoin(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		model.setPage(nameListViewService.listNameList(reqs));
		return model;
	}
	/**
	 * 名单编辑页面
	 * @return
	 */
	@RequestMapping(value="/edit", method=RequestMethod.GET)
	public String editNameListView(){
		return "tms/mgr/name_view";
	}

	/**
	 * 名单单个查询交易
	 * @param rosterId
	 * @return
	 */
	@RequestMapping(value="/get")
	public Model getNameListActoin(@RequestParam String rosterId){
		Model model = new Model();
		model.setRow(nameListViewService.getOneNameList(rosterId));
		return model;
	}
	
	/**
	 * 名单值列表查询页面
	 * @return
	 */
	@RequestMapping(value="/valuelist", method=RequestMethod.GET)
	public String listValueList(){
		return "tms/mgr/value_list_view";
	}
	
	/**
	 * 名单值列表查询交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/valuelist", method=RequestMethod.POST)
	public Model listValueListActoin(@RequestParam Map<String, String> reqs){
		
		log.debug("listValueListActoin inputNode ---> "+reqs);
		
		String rosterId = reqs.get("rosterId");
		
		Model model = new Model();
		// 查询条件
		reqs.put(DBConstant.TMS_MGR_ROSTERVALUE_ROSTERID, rosterId);
		
		model.setPage(nameListViewService.listValueListByPage(reqs));
		
		return model;
	}
	/**
	 * 名单值修改页面
	 * @return
	 */
	@RequestMapping(value="/valueedit", method=RequestMethod.GET)
	public String editValueListView(){
		return "tms/mgr/value_view";
	}

	/**
	 * 名单值单个查询交易
	 * @param rosterValueId
	 * @return
	 */
	@RequestMapping(value="/valueget")
	public Model getValueListActoin(@RequestParam Map<String, String> reqs){
		
		String rosterValueId = reqs.get("rosterValueId");
		
		Model model = new Model();
		
		// 通过名单值表的主键查询名单值信息
		model.setRow(nameListViewService.getOneValueList(rosterValueId));
		
		return model;
	}
	
}
