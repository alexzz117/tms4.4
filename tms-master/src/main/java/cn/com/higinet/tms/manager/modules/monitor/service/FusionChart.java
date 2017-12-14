package cn.com.higinet.tms.manager.modules.monitor.service;

import cn.com.higinet.tms.manager.modules.monitor.model.FusionChartVO;

public interface FusionChart {

	/*
	 * 参数说明：
	 * 0 dataMap Map<String,Object> 报表数据
	 * 1 caption String 标题
	 * 2 subcaption String 子标题
	 * 3 xAxisName String 横向坐标说明
	 * 4 yAxisName String 纵向坐标说明
	 * 5 showLables boolean 是否显示lable
	 * 6 showLinks boolean 是否需要链接
	 * 7.. 自定义参数
	 */
	public String generateChart(FusionChartVO fChartVO,Object... params);
}
