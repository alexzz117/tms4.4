package cn.com.higinet.tms.manager.modules.monitor.service;

import java.util.Map;

public interface GenerateChartService {

	/**
	 * 告警数据实时监控
	 */
	String generateChart(Map<String, String> conds);
	/**
	 * 告警数趋势图
	 */
	String generateChartRMTrend(Map<String, String> conds);
	/**
	 * 规则命中率/交易告警率/告警成功率
	 */
	String generateChartAngular(Map<String, String> conds);
	/**
	 * 规则运行监控
	 */
	String generateChartSrcollStack(Map<String, String> conds);
	/**
	 * 交易运行监控--交易数
	 */
	String generateChartColumn(Map<String, String> conds);
	/**
	 * 风险统计数
	 */
	String generateChartBar(Map<String, String> conds);
	/**
	 * 交易运行监控-平均执行时间
	 */
	String generateChartMSStacked(Map<String, String> conds);
}
