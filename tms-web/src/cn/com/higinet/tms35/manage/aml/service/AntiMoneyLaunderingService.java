package cn.com.higinet.tms35.manage.aml.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import cn.com.higinet.rapid.base.dao.Page;

/**
 * 反洗钱数据管理接口
 * @author wuc
 * @date 2015-1-27
 * 
 */
public interface AntiMoneyLaunderingService {
	
	/**
	 * 查询反洗钱配置信息
	 * @param reqs
	 * @return
	 */
	public Map<String, Object> queryAmlConfig(Map<String, Object> reqs);
	
	/**
	 * 保存反洗钱配置定义
	 * @param reqs
	 * @return
	 */
	public boolean saveAmlConfig(Map<String, Object> reqs);
	
	/**
	 * 查询交易模型字段信息
	 * @param reqs
	 * @return
	 */
	public List<Map<String, Object>> getTxnModelFields(Map<String, Object> reqs);

	/**
	 * 查询导出报文列表
	 * @param reqs
	 * @return
	 */
	public Page<Map<String, Object>> queryExportList(Map<String, Object> reqs);

	/**
	 * 生成报文数据
	 * @param reqs
	 */
	public String generateMessage(Map<String, Object> reqs);

	/**
	 * 导出报文文件
	 * @param reqs
	 * @param response
	 */
	public void exportFile(Map<String, Object> reqs, HttpServletResponse response);

	/**
	 * 查询反洗钱数据列表
	 * @param reqs
	 * @return
	 */
	public Page<Map<String, Object>> queryAmlList(Map<String, Object> reqs);

	/**
	 * 导出报文公共信息同步
	 * @param reqs
	 */
	public boolean syncCommonMessage(Map<String, Object> reqs);

	/**
	 * 查询报文信息
	 * @param reqs
	 * @return
	 */
	public Map<String, Object> queryMessageInfo(Map<String, Object> reqs);

	/**
	 * 编辑报文信息
	 * @param reqs
	 * @return
	 */
	public boolean updateAmlMessage(Map<String, Object> reqs);

	/**
	 * 编辑可疑交易信息
	 * @param reqs
	 * @return
	 */
	public boolean updateStifMessage(Map<String, Object> reqs);

	/**
	 * 更新报文组
	 * @param reqs
	 * @return
	 */
	public String updateGroup(Map<String, Object> reqs);

}
