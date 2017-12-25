/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  CacheRefresh.java   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-12-12 13:07:04   
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

/**
 * 缓存刷新接口
 *
 * @ClassName:  CacheRefresh
 * @author: 王兴
 * @date:   2017-12-12 13:07:12
 * @since:  v4.3
 */
public interface CacheRefresh {

	/**
	 * Refresh.
	 *
	 * @param cacheName 需要刷新的缓存名
	 * @param params service解析到的参数
	 * @param chain 责任链
	 */
	public boolean refresh(String cacheName, Map<String, Object> params, int index, RefreshChain chain) throws Exception;
}
