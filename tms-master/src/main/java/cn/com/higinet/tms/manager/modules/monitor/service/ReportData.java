package cn.com.higinet.tms.manager.modules.monitor.service;

import cn.com.higinet.tms.manager.modules.monitor.model.DataVO;

public interface ReportData {

	/**
	 * 
	 * @param fChartVO
	 * @return
	 */
	public Object getDataList(DataVO dataVO, boolean isPage);
}
