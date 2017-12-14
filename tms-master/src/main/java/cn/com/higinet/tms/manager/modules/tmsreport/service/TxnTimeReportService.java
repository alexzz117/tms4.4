package cn.com.higinet.tms.manager.modules.tmsreport.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.tms.manager.dao.Page;

public interface TxnTimeReportService {
	/**
	 * 根据查询条件查询时间段内(默认当月)每天交易交易运行效率信息 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> listTxnTimeReport(Map<String, String> conds);
	/**
	 * 根据查询条件，获取相应的数据集，并组装成展示图形所需要的字符串
	 * @param conds			查询条件
	 * @param dataList		数据集，如果数据集不为空，则不再重新查询，否则根据查询条件查询
	 * @return
	 */
	public List<Map<String, Object>> getChartData(List<Map<String, Object>> dataList ,Map<String, String> conds);
	/**
	 * 根据导出条件，查询时间段内交易交易运行效率信息，并将信息导出到excel文件
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> exportList(Map<String, String> conds);

}
