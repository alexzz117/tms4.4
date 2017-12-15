package cn.com.higinet.tms.manager.modules.mgr.controller;

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
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.mgr.service.AssignAutoService;

/**
 * 自动派发控制类
 * @author tlh
 */
@Controller("AssignAutoController")
@RequestMapping("/tms/assignAuto")
public class AssignAutoController {
	
	private static Log log = LogFactory.getLog(AssignAutoController.class);
	
	@Autowired
	private AssignAutoService assignAutoService;

	@Autowired
	private ObjectMapper objectMapper = null;

	
	@RequestMapping(value="/eventType", method= RequestMethod.POST)
	public Model eventTypePageList(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		Page<Map<String, Object>> eventTypePage = assignAutoService.eventTypePage(reqs);
		model.set("eventTypePage", eventTypePage.getList());
		return model;
	}
	
	@RequestMapping(value="/assignByRule", method= RequestMethod.GET)
	public String AssignByRuleList(@RequestParam Map<String, String> reqs){
		return "tms/mgr/assignByRule";
	}
	
	@RequestMapping(value="/assignInfo", method= RequestMethod.POST)
	public Model dealerInformationList(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		reqs.put("pagesize", "1000");
		model.setPage(assignAutoService.dealerInformationPage(reqs));
		return model;
	}
	
	@RequestMapping(value="/get", method= RequestMethod.POST)
	public Model getUserAssignAction(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		model.set("users", assignAutoService.getAllUserAssign(reqs).getList());
		model.set("txnMap",reqs);
		return model;
	}
	
	@RequestMapping(value="/add", method= RequestMethod.POST)
	public Model addUserAssignAction(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		assignAutoService.addUserAssign(reqs);
		return model;
	}
	
	@RequestMapping(value="/update", method= RequestMethod.POST)
	public Model updateUserAssignAction(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		assignAutoService.updateUserAssign(reqs);
		return model;
	}
	
	@RequestMapping(value="/modStatus", method= RequestMethod.POST)
	public Model modStatusUserAssignByid(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		assignAutoService.modStatusUserAssign(reqs);
		return model;
	}
	
	@RequestMapping(value="/del", method= RequestMethod.POST)
	public Model delUserAssignAction(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		String json = MapUtil.getString(reqs, "postData");
		Map<String, List<Map<String, String>>> formList = null;
		try {
			formList = objectMapper.readValue(json, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TmsMgrWebException("删除名单Json数据解析异常");
		}
		assignAutoService.delUserAssign(formList);
		return model;
	}
	
	@RequestMapping(value="/switch", method= RequestMethod.POST)
	public Model autoAssignSwitchAction(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		assignAutoService.modAutoAssignSwitch(reqs);
		return model;
	}
	
	@RequestMapping(value="/getStatus", method= RequestMethod.POST)
	public Model autoAssignSwitchStatus(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		model.set("STARTVALUE",assignAutoService.getAssignSwitchStatus(reqs));
		return model;
	}
	
	@RequestMapping(value="/paySuspend", method= RequestMethod.GET)
	public String userPaySuspendList(@RequestParam Map<String, String> reqs){
		return "tms/mgr/paySuspend_list";
	}
	
	@RequestMapping(value="/paySuspend", method= RequestMethod.POST)
	public Model allPaySuspendList(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		Page<Map<String, Object>> PaySuspendPage = assignAutoService.getAllPaySuspendList(reqs);
		model.setPage(PaySuspendPage);
		return model;
	}
	
	@RequestMapping(value="/modpaySuspend", method= RequestMethod.POST)
	public Model modPaySuspendList(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		log.debug("listValueListActoin inputNode ---> "+reqs);
		return model;
	}
}
