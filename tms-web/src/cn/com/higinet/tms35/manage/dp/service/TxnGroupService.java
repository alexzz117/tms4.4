/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms35.manage.dp.service;

import java.util.List;
import java.util.Map;
/**
 * 交易树功能服务接口
 * @author zhangfg
 * @date 2011-7-7 
 * @version 2.0.0
 */
public interface TxnGroupService {
	/**
	 * 获得所有渠道信息
	 * @param  conds	查询条件
	 * @return List<Map<String, Object>>
	 */
	public  List<Map<String, Object>> listAllChannel(Map<String, String> conds);
	public List<Map<String, Object>> listAllChannelWithOrder();
}
