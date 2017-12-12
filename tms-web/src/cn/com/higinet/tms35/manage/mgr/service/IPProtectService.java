package cn.com.higinet.tms35.manage.mgr.service;

import java.util.List;
import java.util.Map;

public interface IPProtectService {
	public void importIpLocation(Map<String, Object> inputs);
	public void importBatchUpdate(String[] sqls, List<Map<String, ?>> ...lists);
	public Map<String,Object> getProgress();
	public void initialization();
	public Map<String, Object> createImpIPLog(Map<String, Object> log);
}
