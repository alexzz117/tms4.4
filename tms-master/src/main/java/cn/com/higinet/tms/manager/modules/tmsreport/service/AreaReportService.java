package cn.com.higinet.tms.manager.modules.tmsreport.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.tms.base.entity.common.Page;

public interface AreaReportService {
	/**
	 * 根据查询条件查询地区告警数据信息 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> listAreaReport(Map<String, String> conds);
	/**
	 * 根据查询条件，获取相应的数据集，并组装成中国地图所需要的字符串
	 * @param conds			查询条件
	 * @param dataList		数据集，如果数据集不为空，则不再重新查询，否则根据查询条件查询
	 * @return
	 */
	public String getChinaChartData(List<Map<String, Object>> dataList ,Map<String, String> conds);
	
	/**
	 * 根据查询条件查询各市告警数据信息 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> listCityReport(Map<String, String> conds);
	/**
	 * 根据查询条件，获取相应的数据集，并组装前台图形展示所需要的字符串
	 * @param conds			查询条件
	 * @param dataList		数据集，如果数据集不为空，则不再重新查询，否则根据查询条件查询
	 * @return
	 */
	public String getChartData(List<Map<String, Object>> dataList ,Map<String, String> conds);
	/**
	 * 根据导出条件，查询地区告警信息，并将信息导出到excel文件
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> exportList(Map<String, String> conds);
	/**
	 * 根据导出条件，查询城市告警信息，并将信息导出到excel文件
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> exportCityList(Map<String, String> conds);

	
}
