package cn.com.higinet.tms35.manage.alarm.service;

import java.util.Map;

public interface AlarmQueueService {
    /**
     * 获取交易流水信息
     * @param txncode   交易流水号   
     * @return
     */
    public Map<String, Object> getTrafficData(String txncode);
	/**
	 * 更新人工确认交易风险状态
	 * @param status	风险状态，0：无风险；1：有风险
	 * @param txncode	交易流水号
	 */
	public void updateRiskStatus(String status, String... txncodes);
}