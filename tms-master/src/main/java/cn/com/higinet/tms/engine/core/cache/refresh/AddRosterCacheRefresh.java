/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  IPLocationCacheRefresh.java   
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
package cn.com.higinet.tms.engine.core.cache.refresh;

import java.util.Map;

import cn.com.higinet.tms.engine.core.cache.db_cache;
import cn.com.higinet.tms.engine.core.cache.txn;

/**
 * 新增名单缓存
 *
 * @ClassName:  IPLocationCacheRefresh
 * @author: 王兴
 * @date:   2017-12-12 13:28:33
 * @since:  v4.3
 */
public class AddRosterCacheRefresh implements CacheRefresh {

	/** 常量 NAME. */
	private static final String NAME = "ADD_ROSTER";

	@Override
	public boolean refresh(String cacheName, Map<String, Object> params, int index, RefreshChain chain) throws Exception {
		if (NAME.equals(cacheName)) {
			synchronized (txn.class) {
				db_cache.get().roster().add_value((String) params.get("rosterName"), (String) params.get("rosterValue"), Long.parseLong((String) params.get("txnTime")));
			}
			return true;
		} else {
			return chain.refresh(cacheName, params, ++index);
		}
	}
}