package cn.com.higinet.tms.manager.modules.switcher.controller;

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
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.switcher.service.SwitchService;

/**
 * 开关
 * @author yangk
 * @author zhang.lei
 */

@RestController("switchController")
@RequestMapping("/tms/switch")
public class SwitchController {

	private static final Logger logger = LoggerFactory.getLogger( SwitchController.class );
	@Autowired
	private SwitchService switchService;
	@Autowired
	private ObjectMapper objectMapper = null;
	
	/**
	 * 方法描述:查询开关列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model listSwitchAction( @RequestBody Map<String, Object> reqs ) {

		String txn_id = MapUtil.getString( reqs, "txnId" );
		List<Map<String, Object>> switchs = switchService.listAllSwitchInOneTxn( txn_id );
		Model model = new Model();
		model.set( "switchs", switchs );
		return model;
	}

	/**
	 * 方法描述:添加开关跳转
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String addSwitchAction() {
		return "tms35/switch/switch_add";
	}

	/**
	 * 方法描述:添加开关action
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Model addSwitchAction( @RequestBody Map<String, Object> reqs ) {

		String txn_id = MapUtil.getString( reqs, "txnId" );
		List<Map<String, Object>> switchs = switchService.listAllSwitchInOneTxn( txn_id );
		Model model = new Model();
		model.set( "switchs", switchs );
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
	public Model saveSwitchAction( @RequestBody Map<String, Object> reqs ) {
		String json = MapUtil.getString( reqs, "postData" );
		Map<String, List<Map<String, ?>>> formMap = null;
		try {
			formMap = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
			logger.error( e.getMessage(), e );
			throw new TmsMgrWebException( "保存开关Json数据解析异常" );
		}
		Model model = new Model();
		model.setRow( switchService.saveSwitch( formMap ) );
		return model;
	}
}
