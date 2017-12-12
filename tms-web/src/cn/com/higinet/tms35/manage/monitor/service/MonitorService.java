package cn.com.higinet.tms35.manage.monitor.service;

import java.util.Map;

import cn.com.higinet.tms35.manage.monitor.model.DataVO;

/**
 * 实时运行监控服务接口
 */
public interface MonitorService {
	public DataVO getDataList(DataVO dataVO, boolean isPage);
	
	public Map<String, Integer> compareDescTop5(Map<String,Integer> dataMap);

	public String getFullTxnPath(String txnid, String txnField);
}
