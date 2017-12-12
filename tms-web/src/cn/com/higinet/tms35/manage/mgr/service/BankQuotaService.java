package cn.com.higinet.tms35.manage.mgr.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.rapid.base.dao.Page;

/**
 * 通道限额管理服务类
 * @author tlh
 */

public interface BankQuotaService {

	/**
	 * 通道限额列表查询
	 * @param reqs 请求参数
	 * @return
	 */
	Page<Map<String,Object>> listBankQuotaPage(Map<String, String> reqs);
	
	/**
	 * 通道限额删除根据id
	 * @param reqs 请求参数
	 * @return
	 */
	void delBankQuotaList(Map<String,List<Map<String,String>>> batchMap);
	
	/**
	 * 添加一条通道限额信息
	 * @param reqs 请求参数
	 * @return
	 */
	void addBankQuota(Map<String, String> reqs);
	
	/**
	 * 根据id单个查询一条通道限额信息
	 * @param id 唯一标识
	 * @return
	 */
	Map<String, Object> getBankQuotaById(String id);
	
	/**
	 * 根据id修改一条通道限额信息
	 * @param reqs 请求参数
	 * @return
	 */
	void updateBankQuotaById(Map<String, String> reqs);
}
