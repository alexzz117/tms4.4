package cn.com.higinet.tms35.manage.sign.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.tms35.manage.sign.model.MerchantRiskInfo;

public interface SignService {
	/**
	 * 获取用户注册即银行卡预留手机号
	 * @param userId	用户ID
	 * @return
	 */
	public List<Map<String, Object>> getUserAndBankMobileList(String userId);
	/**
	 * 刷新风险评估服务中签约"商户风险信息"缓存
	 * @param	merchantRiskInfo	商户风险信息
	 */
	public void refreshRiskServerSignCacheForMerchantRiskInfo(MerchantRiskInfo merchantRiskInfo);
}