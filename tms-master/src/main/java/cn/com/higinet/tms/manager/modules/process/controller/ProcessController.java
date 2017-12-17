package cn.com.higinet.tms.manager.modules.process.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.process.service.ProcessService;

/**
 * 开关
 * @author yangk
 * @author zhang.lei
 */

@RestController("processController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/process")
public class ProcessController {
	
	private static final Logger log = LoggerFactory.getLogger( ProcessController.class );
	
	@Autowired
	private ProcessService processService;
	
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * 方法描述:查询开关列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model listProcessAction( @RequestBody Map<String, Object> reqs ) {

		String txn_id = MapUtil.getString( reqs, "txnId" );
		List<Map<String, Object>> process = processService.listAllProcessInOneTxn( txn_id );
		Model model = new Model();
		model.set( "process", process );
		return model;
	}

	/**
	 * 方法描述:添加开关跳转
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String addSwitchAction() {
		return "tms35/process/process_add";
	}

	/**
	 * 方法描述:添加开关action
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Model addProcessAction( @RequestBody Map<String, Object> reqs ) {

		String txn_id = MapUtil.getString( reqs, "txnId" );
		List<Map<String, Object>> process = processService.listAllProcessInOneTxn( txn_id );
		Model model = new Model();
		model.set( "process", process );
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
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public Model saveProcessAction( @RequestBody Map<String, Object> reqs ) {
		String json = MapUtil.getString( reqs, "postData" );
		Map<String, List<Map<String, ?>>> formMap = null;
		try {
			formMap = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
			log.error( e.getMessage(), e );
			throw new TmsMgrWebException( "保存处置Json数据解析异常:json=" + json );
		}

		Model model = new Model();
		model.setRow( processService.saveProcess( formMap ) );
		return model;
	}
}
