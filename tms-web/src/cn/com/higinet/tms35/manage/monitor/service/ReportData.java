package cn.com.higinet.tms35.manage.monitor.service;

import cn.com.higinet.tms35.manage.monitor.model.DataVO;

public interface ReportData {

	/**
	 * 
	 * @param fChartVO
	 * @return
	 */
	public Object getDataList(DataVO dataVO, boolean isPage);
}
