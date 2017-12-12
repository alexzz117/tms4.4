package cn.com.higinet.tms35.manage.monitor.model;

import java.util.List;
import java.util.Map;

public class FusionChartVO {

	private List<Map<String,Object>> dataList;
	private Map<String,Integer> dataMap;
	private String chartType;
	private String caption;
	private String subcaption;
	private String xAxisName;
	private String yAxisName;
	private boolean showLables;
	private boolean showLinks;
	private String chartId;
	
	public List<Map<String, Object>> getDataList() {
		return dataList;
	}
	public void setDataList(List<Map<String, Object>> dataList) {
		this.dataList = dataList;
	}
	public Map<String, Integer> getDataMap() {
		return dataMap;
	}
	public void setDataMap(Map<String, Integer> dataMap) {
		this.dataMap = dataMap;
	}
	public String getChartType() {
		return chartType;
	}
	public void setChartType(String chartType) {
		this.chartType = chartType;
	}
	
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public String getSubcaption() {
		return subcaption;
	}
	public void setSubcaption(String subcaption) {
		this.subcaption = subcaption;
	}
	public String getxAxisName() {
		return xAxisName;
	}
	public void setxAxisName(String xAxisName) {
		this.xAxisName = xAxisName;
	}
	public String getyAxisName() {
		return yAxisName;
	}
	public void setyAxisName(String yAxisName) {
		this.yAxisName = yAxisName;
	}
	public Boolean getShowLables() {
		return showLables;
	}
	public void setShowLables(boolean showLables) {
		this.showLables = showLables;
	}
	public Boolean getShowLinks() {
		return showLinks;
	}
	public void setShowLinks(boolean showLinks) {
		this.showLinks = showLinks;
	}
	public String getChartId() {
		return chartId;
	}
	public void setChartId(String chartId) {
		this.chartId = chartId;
	}
}
