package cn.com.higinet.tmsreport.web.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.rapid.base.dao.Page;

public interface ModelReportService {
	/**
	 * 根据查询条件查询时间段内(默认当月)每天交易告警数据信息 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> listModelReport(Map<String, String> conds);
	/**
	 * 根据查询条件，获取相应的数据集，并组装成展示图形所需要的字符串
	 * @param conds			查询条件
	 * @return
	 */
	public List<Map<String, Object>> getChartData(Map<String, String> conds);
	/**
	 * 根据导出条件，查询时间段内交易告警信息，并将信息导出到excel文件
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> exportList(Map<String, String> conds);

}
