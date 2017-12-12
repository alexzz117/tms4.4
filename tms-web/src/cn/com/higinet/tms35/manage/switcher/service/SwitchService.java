package cn.com.higinet.tms35.manage.switcher.service;

import java.util.List;
import java.util.Map;

public interface SwitchService {

	/**
	 * 查询某交易的所有开关
	 * @param txnid
	 * @return
	 */
	public List<Map<String, Object>> listAllSwitchInOneTxn(String txnid); 
	/**
	 * 查询某交易的所有开关
	 * @param formMap
	 * @return
	 */
	public Map saveSwitch(Map formMap); 
}
