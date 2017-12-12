package cn.com.higinet.tmsreport.web.service;

import java.util.List;
import java.util.Map;

public interface DisposalService {

	/**
	 * 获取一个代码类别下所有代码
	 * @return 存放代码的键值对的Map对象
	 */
	public Map<String, String> getDp();
	
	/**
	 * 获取一个代码类别下所有代码
	 * @return 存放代码的键值对的List对象
	 * 	Map<CODE_KEY,CODE_VALUE>
	 */
	public List<Map<String, Object>> queryList();

	/**
	* 方法描述:
	* @param txnid
	* @return
	*/
	List<Map<String, Object>> queryList(String txnid);
}
