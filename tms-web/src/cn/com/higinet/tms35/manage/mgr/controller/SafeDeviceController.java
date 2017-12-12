package cn.com.higinet.tms35.manage.mgr.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.com.higinet.rapid.web.model.Model;
import cn.com.higinet.tms35.manage.aop.cache.CacheRefresh;
import cn.com.higinet.tms35.manage.mgr.service.SafeDeviceService;

@Controller("safeDeviceController")
@RequestMapping("/tms/safeDevice")
public class SafeDeviceController {
	@Autowired
	private SafeDeviceService safeDeviceService;
		
	@Autowired
	private CacheRefresh commonCacheRefresh;
	
	@RequestMapping(value="/list", method=RequestMethod.GET)
	public String safeDeviceLisView(){
		return "tms/mgr/safeDevice_list";
	}
	
	@RequestMapping(value="/list", method=RequestMethod.POST)
	public Model safeDeviceLisAction(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		model.setPage(safeDeviceService.getSafeDeviceList(reqs));
		return model;
	}
	
	@RequestMapping(value="/add", method=RequestMethod.POST)
	public Model addSafeDeviceAction(@RequestParam Map<String, String> reqs,HttpServletRequest request){
		Model model = new Model();
		if(!safeDeviceService.getTableList("TMS_MGR_SAFE_DEVICE","DEVICE_ID",reqs.get("DEVICE_ID")).isEmpty()){
			model.addError("终端ID已存在，请重新填写一个终端ID");
			return model;
		}
		safeDeviceService.addSafeDeviceAction(reqs,request);
		return model;
	}

	@RequestMapping(value="/get", method=RequestMethod.POST)
	public Model getSafeDeviceAction(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		Map<String, Object> List = safeDeviceService.getTableList("TMS_MGR_SAFE_DEVICE","DEVICE_ID",reqs.get("DEVICE_ID"));
		model.setRow(List);
		return model;
	}
	
	@RequestMapping(value="/mod", method=RequestMethod.POST)
	public Model modSafeDeviceAction(@RequestParam Map<String, String> reqs,HttpServletRequest request){
		Model model = new Model();
		safeDeviceService.updateSafeDeviceByUuid(reqs, request);
		refresh("TMS_MGR_SAFE_DEVICE,"+reqs.get("DEVICE_ID"));
		return model;
	}
	
	public String refresh(String refreshMsg) {
		while (commonCacheRefresh.refresh(refreshMsg).isEmpty())
			break;
		return "";
	}
}
