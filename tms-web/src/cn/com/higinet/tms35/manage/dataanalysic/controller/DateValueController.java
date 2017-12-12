package cn.com.higinet.tms35.manage.dataanalysic.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.com.higinet.rapid.web.model.Model;
import cn.com.higinet.tms35.manage.dataanalysic.service.DateValueService;
import cn.com.higinet.tms35.manage.query.common.model.Stmt.Mode;

@Controller("dateValueController")
@RequestMapping("/tms35/dataanalysic")
public class DateValueController {
	/**@author yangcheng**/
	@Autowired
    private	DateValueService dateValueService;
	
	
	@RequestMapping("/timeLine")
	public String DateValueView(){
		
		return "/tms35/dataanalysic/timeLine";
	} 
	
	@RequestMapping(value="/dataValue",method=RequestMethod.POST)                     
	public Model DateValue(@RequestParam String userId,@RequestParam String deviceId,@RequestParam String thingType,@RequestParam String transBeginTime,@RequestParam String transEndTime,HttpServletRequest request
){
		
		Model model = new Model();
    
	   List<Map<String,Object>> dateAnnalysicList  = dateValueService.queryDateAnnaList(userId,deviceId,thingType,transBeginTime,transEndTime,request);
		model.setRow(dateAnnalysicList);
		return model;
	} 
	
}
