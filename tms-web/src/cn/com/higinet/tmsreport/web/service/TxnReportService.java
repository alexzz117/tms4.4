package cn.com.higinet.tmsreport.web.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.rapid.base.dao.Page;

public interface TxnReportService {
	/**
	 * 根据查询条件查询所有交易告警数据信息 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> listTxnReport(Map<String, String> conds);
	/**
	 * 根据查询条件，获取相应的数据集，并组装成展示图形所需要的字符串
	 * @param conds			查询条件
	 * @param dataList		数据集，如果数据集不为空，则不再重新查询，否则根据查询条件查询
	 * @return
	 */
	public String getChartData(List<Map<String, Object>> dataList ,Map<String, String> conds);
	/**
	 * 获取系统所有的渠道列表
	 * @return
	 */
	public List<Map<String,Object>> getChannelList(String channelId);
	/**
	 * 查询所有国家
	 * @return
	 */
	public List<Map<String,Object>> getAllCountry();
	/**
	 * 根据国家代码获取所有的省份列表
	 * @param countryId
	 * @return
	 */
	public List<Map<String,Object>> getAllRegion(String countryId);
	/**
	 * 根据省份代码获取该省份下所有的地区列表
	 * @param regionId
	 * @return
	 */
	public List<Map<String,Object>> getAllCity(String regionId);
	/**
	 * 根据导出条件，查询交易告警信息，并将信息导出到excel文件
	 * @param conds
	 * @return
	 */
	public List<Map<String,Object>> exportList(Map<String, String> conds);
	
}
