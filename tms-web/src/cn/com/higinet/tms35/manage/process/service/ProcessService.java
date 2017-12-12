package cn.com.higinet.tms35.manage.process.service;

import java.util.List;
import java.util.Map;

public interface ProcessService {

	/**
	 * 查询某交易的所有处置
	 * @param txnid
	 * @return
	 */
	public List<Map<String, Object>> listAllProcessInOneTxn(String txnid); 
	/**
	 * 保存某交易的所有处置
	 * @param List
	 * @return
	 */
	public Map saveProcess(Map formMap); 
}
