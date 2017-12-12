package cn.com.higinet.tms35.manage.process.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.com.higinet.rapid.web.model.Model;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.exception.TmsMgrWebException;
import cn.com.higinet.tms35.manage.process.service.ProcessService;

/**
 * 开关
 * @author yangk
 */
@Controller("processController")
@RequestMapping("/tms35/process")
public class ProcessController {

	private static Log log = LogFactory.getLog(ProcessController.class);
	
	@Autowired
	private ProcessService processService;
	@Autowired
	private ObjectMapper objectMapper = null;
	
	/**
	 * 方法描述:查询开关列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/list",method=RequestMethod.POST)
	public Model listProcessAction(@RequestParam Map<String,Object> reqs) {
		
		String txn_id = MapUtil.getString(reqs, "txnId");
		List<Map<String, Object>> process = processService.listAllProcessInOneTxn(txn_id);
		Model model = new Model();
		model.set("process", process);
		return model;
	}
	
	/**
	 * 方法描述:添加开关跳转
	 * @return
	 */
	@RequestMapping(value="/add",method=RequestMethod.GET)
	public String addSwitchAction() {
		return "tms35/process/process_add";
	}
	/**
	 * 方法描述:添加开关action
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/add",method=RequestMethod.POST)
	public Model addProcessAction(@RequestParam Map<String,Object> reqs) {
		
		String txn_id = MapUtil.getString(reqs, "txnId");
		List<Map<String, Object>> process = processService.listAllProcessInOneTxn(txn_id);
		Model model = new Model();
		model.set("process", process);
		return model;
	}
	/**
	 * 方法描述:保存开关action
	 * @param reqs
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@SuppressWarnings("all")
	@RequestMapping(value="/save",method=RequestMethod.POST)
	public Model saveProcessAction(@RequestParam Map<String,Object> reqs) {
		
		String json = MapUtil.getString(reqs, "postData");
		
		Map<String,List<Map<String, ?>>> formMap = null;
		try {
			formMap = objectMapper.readValue(json, Map.class);
		} catch (Exception e) {
			log.error(e);
			throw new TmsMgrWebException("保存处置Json数据解析异常:json="+json);
		}
		
		Model model = new Model();
		model.setRow(processService.saveProcess(formMap));
		return model;
	}
}
