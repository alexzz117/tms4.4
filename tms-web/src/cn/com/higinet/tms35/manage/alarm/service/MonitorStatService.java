package cn.com.higinet.tms35.manage.alarm.service;

import java.util.Map;

public interface MonitorStatService {
    
    /**
     * 更新交易数据统计值
     * @param status   风险人工确认状态
     * @param traffic  交易流水信息
     */
    public void updateMonitorStat(Map<String, Object> traffic, String status);
    
    /**
     * 更新工作量信息
     * @param operId		处理人员ID
     * @param AssignTime	分派时间
     * @param isAutoAssign  是否自动分派
     * @param isProcess		是否处理
     */
    public void updateAlarmProcessOperatorStat(String operId, long assignTime, boolean isAutoAssign, boolean isProcess);
    
    /**
     * 报警事件分派，更新工作量统计数据
     * @param oldOperId			原处理人员
     * @param oldAssignTime		原分派时间
     * @param oldStatus			原报警单处理状态
     * @param newOperID			新处理人员
     * @param newAssignTime		新分派时间
     */
    public void updateAlarmAssignOperatorStat(String oldOperId, long oldAssignTime, boolean oldStatus, String newOperID, long newAssignTime);
    
    /**
     * 更新实时统计中欺诈类型统计数据
     * @param txnCode
     */
    public void modMonitorFraudTypeStat(Map<String, Object> map);
}
