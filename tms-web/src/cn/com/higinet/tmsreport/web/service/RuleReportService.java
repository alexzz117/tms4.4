package cn.com.higinet.tmsreport.web.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.rapid.base.dao.Page;

public interface RuleReportService {
	/**
	 * 根据查询条件查询所有规则告警数据信息 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> listRuleReport(Map<String, String> conds);
	/**
	 * 根据查询条件，获取相应的数据集，并组装成展示图形所需要的字符串
	 * @param conds			查询条件
	 * @param dataList		数据集，如果数据集不为空，则不再重新查询，否则根据查询条件查询
	 * @return
	 */
	public List<Map<String, Object>> getChartData(List<Map<String, Object>> dataList ,Map<String, String> conds);
	/**
	 * 根据导出条件，查询规则告警信息，并将信息导出到excel文件
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>>  exportList(Map<String, String> conds);
}
