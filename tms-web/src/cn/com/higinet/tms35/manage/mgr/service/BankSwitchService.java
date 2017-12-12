package cn.com.higinet.tms35.manage.mgr.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.com.higinet.rapid.base.dao.Page;

/**
 * 通道开关管理服务类
 * @author tlh
 */

public interface BankSwitchService {
	
	/**
	 * 通道开关列表查询
	 * @param reqs 请求参数
	 * @return
	 */
	Page<Map<String,Object>> listBankSwitchPage(Map<String, String> reqs);
	
	/**
	 * 通道开关删除根据id
	 * @param reqs 请求参数
	 * @return
	 */
	void delBankSwitchList(Map<String,List<Map<String,String>>> batchMap);
	
	/**
	 * 添加一条通道开关信息
	 * @param reqs 请求参数
	 * @return
	 */
	void addBankSwitch(Map<String, String> reqs,HttpServletRequest request);
	
	/**
	 * 根据id单个查询一条通道开关信息
	 * @param id 唯一标识
	 * @return
	 */
	Map<String, Object> getBankSwitchById(String id);
	
	/**
	 * 根据id修改一条通道开关信息
	 * @param reqs 请求参数
	 * @return
	 */
	void updateBankSwitchById(Map<String, String> reqs,HttpServletRequest request);
	
	/**
	 * 根据ID查询通道开关状态修改历史
	 * @param reqs 请求参数
	 * @return
	 */
	Page<Map<String,Object>> listBankSwitchHisByIdPage(Map<String, String> reqs);
}
