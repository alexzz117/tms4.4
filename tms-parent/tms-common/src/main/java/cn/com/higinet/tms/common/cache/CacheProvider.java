/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  CachePool.java   
 * @Package cn.com.higinet.tms.common.cache   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-7-24 15:54:14   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.cache;

import cn.com.higinet.tms.common.lifecycle.Lifecycle;

/**
 * 缓存对象提供者，不管缓存技术采用何种方式实现，客户端只需通过此接口即可获得cache对象。
 * 目前提供EHCache、Redis、Aerospike的CacheProvider实现。<br/>
 * CacheProvider通过CacheManager来获得，代码如下：<br/>
 * {@code 
 * 	CacheProvicer provider=CacheManager.getProvider("test1"); //test1建议定义到常量类中
 *  Cache cache=protiver.getCache(); //进一步获取Cache对象
 * }
 *
 * @ClassName: CacheProvider
 * @author: 王兴
 * @date: 2017-7-24 15:54:14
 * @since: v4.3
 */
public interface CacheProvider extends Lifecycle {

	/**
	 * 返回 provider对应的env配置
	 *
	 * @return env 属性
	 */
	public CacheEnv getEnv();

	/**
	 * 设置 provider对应的env配置
	 *
	 * @param env the env
	 */
	public void setEnv(CacheEnv env);

	/**
	 * 获得provider本身对应的cache对象。
	 *
	 * @return cache 属性
	 * @throws Exception the exception
	 */
	public Cache getCache() throws Exception;
}
