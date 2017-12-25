/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  RefreshChain.java   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-12-12 13:02:59   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.engine.core.cache.refresh;

import java.util.List;
import java.util.Map;

/**
 * 刷新缓存的责任链
 *
 * @ClassName:  RefreshChain
 * @author: 王兴
 * @date:   2017-12-12 13:02:59
 * @since:  v4.3
 */
public class RefreshChain {

	/** 当前注入的cacheRefresh对象. */
	private List<CacheRefresh> cacheRefreshes;

	public void setCacheRefreshes(List<CacheRefresh> cacheRefreshes) {
		this.cacheRefreshes = cacheRefreshes;
	}

	public boolean refresh(String cacheName, Map<String, Object> params, int index) throws Exception {
		if (cacheRefreshes != null && !cacheRefreshes.isEmpty() && index < cacheRefreshes.size()) {
			CacheRefresh crf = cacheRefreshes.get(index);
			return crf.refresh(cacheName, params, index, this);
		}
		return false;
	}

}
