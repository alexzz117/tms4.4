package cn.com.higinet.tms.manager.modules.alarm.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.modules.alarm.service.AlarmQueueService;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;

/**
 * 风险处置-报警队列管理
 * @author lining
 * @author zhang.lei
 */

@RestController("alarmQueueController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/alarmqueue")
public class AlarmQueueController {

	@Autowired
	protected AlarmQueueService alarmQueueService;

	/**
	 * 人工确认交易有无风险
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/confirmrisk", method = RequestMethod.POST)
	public Model ArtConfirmRiskTraffic( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		String txncode = MapUtil.getString( reqs, "txncode" );
		String status = MapUtil.getString( reqs, "status" );
		String[] txncodes = StringUtil.split( txncode, "," );
		alarmQueueService.updateRiskStatus( status, txncodes );
		model.set( "status", status );
		return model;
	}
}