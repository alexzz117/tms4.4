package cn.com.higinet.tms.manager.modules.alarm.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.alarm.service.AlarmQueueService;
import cn.com.higinet.tms.manager.modules.alarm.service.MonitorStatService;
import cn.com.higinet.tms.manager.modules.common.DBConstant;

/**
 * 风险处置-报警队列管理Service
 * @author liining
 * @author alex.z
 */
@Service("alarmQueueService")
public class AlarmQueueService {
	@Autowired
	@Qualifier("tmpSimpleDao")
	protected SimpleDao tmpSimpleDao;
	@Autowired
	protected MonitorStatService monitorStatService;

	/**
	 * 获取交易流水信息
	 * @param txncode   交易流水号   
	 * @return
	 */
	public Map<String, Object> getTrafficData( String txncode ) {
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put( DBConstant.TMS_RUN_TRAFFICDATA_TXNCODE, txncode );
		return tmpSimpleDao.retrieve( DBConstant.TMS_RUN_TRAFFICDATA, conds );
	}

	/**
	 * 更新人工确认交易风险状态
	 * @param status	风险状态，0：无风险；1：有风险
	 * @param txncode	交易流水号
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateRiskStatus( String status, String... txncodes ) {
		String sql = "update " + DBConstant.TMS_RUN_TRAFFICDATA + " set " + DBConstant.TMS_RUN_TRAFFICDATA_CONFIRMRISK + " = ? where " + DBConstant.TMS_RUN_TRAFFICDATA_TXNCODE
				+ " = ?";
		for( String txncode : txncodes ) {
			Map<String, Object> trafficMap = getTrafficData( txncode );
			tmpSimpleDao.executeUpdate( sql, status, txncode );
			monitorStatService.updateMonitorStat( trafficMap, status );
		}
	}
}