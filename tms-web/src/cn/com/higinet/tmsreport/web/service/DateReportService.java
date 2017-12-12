package cn.com.higinet.tmsreport.web.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.rapid.base.dao.Page;

public interface DateReportService {
	/**
	 * 根据查询条件查询时间段内(默认当月)每天交易告警数据信息 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> listDateReport(Map<String, String> conds);
	/**
	 * 根据查询条件，获取相应的数据集，并组装成展示图形所需要的字符串
	 * @param conds			查询条件
	 * @param dataList		数据集，如果数据集不为空，则不再重新查询，否则根据查询条件查询
	 * @return
	 */
	public List<Map<String, Object>> getChartData(List<Map<String, Object>> dataList ,Map<String, String> conds);
	/**
	 * 根据导出条件，查询时间段内交易告警信息，并将信息导出到excel文件
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> exportList(Map<String, String> conds);
//
//	/**
//	 * 拼装按日期期查询的sql语句
//	 * @param conds
//	 * @return
//	 */
//	public String getSqlByDay(Map<String, String> conds);
//	/**
//	 * 拼装按周期查询的sql语句
//	 * @param conds
//	 * @return
//	 */
//	public String getSqlByWeek(Map<String, String> conds);
//	/**
//	 * 拼装按月期查询的sql语句
//	 * @param conds
//	 * @return
//	 */
//	public String getSqlByMonth(Map<String, String> conds);
}
