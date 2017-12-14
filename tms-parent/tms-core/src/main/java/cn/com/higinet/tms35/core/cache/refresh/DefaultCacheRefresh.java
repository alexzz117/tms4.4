/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  IPLocationCacheRefresh.java   
 * @Package cn.com.higinet.tms35.core.cache.refresh   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-12-12 13:28:33   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms35.core.cache.refresh;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import cn.com.higinet.tms35.core.cache.txn;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms4.model.cache.RiskModelCache;

/**
 * 默认刷新缓存
 *
 * @ClassName:  IPLocationCacheRefresh
 * @author: 王兴
 * @date:   2017-12-12 13:28:33
 * @since:  v4.3
 */
public class DefaultCacheRefresh implements CacheRefresh {

	@Autowired
	private RiskModelCache riskModelCache;

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.core.cache.refresh.CacheRefresh#refresh(java.lang.String, java.util.Map, int, cn.com.higinet.tms35.core.cache.refresh.RefreshChain)
	 */
	@Override
	public boolean refresh(String cacheName, Map<String, Object> params, int index, RefreshChain chain) throws Exception {
		// 默认刷新配置缓存
		txn.init(new data_source(), false);
		// 风险模型缓存
		riskModelCache._initTms();
		return true;
	}
}
