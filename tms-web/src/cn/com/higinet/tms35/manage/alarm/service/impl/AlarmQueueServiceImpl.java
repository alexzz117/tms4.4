package cn.com.higinet.tms35.manage.alarm.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.tms35.manage.alarm.service.AlarmQueueService;
import cn.com.higinet.tms35.manage.alarm.service.MonitorStatService;
import cn.com.higinet.tms35.manage.common.DBConstant;

/**
 * 风险处置-报警队列管理Service
 * 
 * @author liining
 * 
 */
@Service("alarmQueueService")
public class AlarmQueueServiceImpl implements AlarmQueueService {
    @Autowired
    protected SimpleDao tmpSimpleDao;
    @Autowired
    protected MonitorStatService monitorStatService;

    public Map<String, Object> getTrafficData(String txncode)
    {
        Map<String, Object> conds = new HashMap<String, Object>();
        conds.put(DBConstant.TMS_RUN_TRAFFICDATA_TXNCODE, txncode);
        return tmpSimpleDao.retrieve(DBConstant.TMS_RUN_TRAFFICDATA, conds);
    }

    @Transactional(propagation=Propagation.REQUIRED)
    public void updateRiskStatus(String status, String... txncodes)
    {
        String sql = "update " + DBConstant.TMS_RUN_TRAFFICDATA + " set " + DBConstant.TMS_RUN_TRAFFICDATA_CONFIRMRISK + " = ? where " + DBConstant.TMS_RUN_TRAFFICDATA_TXNCODE + " = ?";
        for (String txncode : txncodes)
        {
            Map<String, Object> trafficMap = getTrafficData(txncode);
            tmpSimpleDao.executeUpdate(sql, status, txncode);
            monitorStatService.updateMonitorStat(trafficMap, status);
        }
    }
}