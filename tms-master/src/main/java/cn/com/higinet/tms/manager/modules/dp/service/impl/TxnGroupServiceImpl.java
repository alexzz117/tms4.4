/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.modules.dp.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.dp.service.TxnGroupService;

/**
 * 交易树服务类
 * 
 * @author zhangfg
 * @date 2011-7-7 
 * @version 2.0.0
 * 
 * @author zhang.lei
 */
@Service("tmsTxnGroupService")
public class TxnGroupServiceImpl implements TxnGroupService {

	@Autowired
	@Qualifier("dynamicSimpleDao")
	private SimpleDao dynamicSimpleDao;

	/**
	 * 获得所有渠道信息
	 * @return
	 */
	public List<Map<String, Object>> listAllChannel(Map<String, String> conds) {
		List<Map<String, Object>> channellist = dynamicSimpleDao
				.listAll(DBConstant.TMS_DP_CHANNEL);
		return channellist;
	}
	
	/**
	 * 获得所有渠道信息,order by orderby
	 * @return
	 */
	public List<Map<String, Object>> listAllChannelWithOrder() {
		
		List<Map<String, Object>> channellist = dynamicSimpleDao.listAll(DBConstant.TMS_DP_CHANNEL, new Order().asc("ORDERBY"));
		return channellist;
	}

}
