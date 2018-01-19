/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  BalanceStrategy.java   
 * @Package cn.com.higinet.tms.common.balancer   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-19 15:40:36   
 * @version V1.0 
 * @Copyright: 2018 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.balancer.strategies;

import cn.com.higinet.tms.common.balancer.BalancerContext;
import cn.com.higinet.tms.common.balancer.ServerNotFoundException;
import cn.com.higinet.tms.common.balancer.TargetKeeper;

/**
 * 负载均衡策略，基于优先级、顺序、一致性哈希等算法
 *
 * @ClassName:  BalanceStrategy
 * @author: 王兴
 * @date:   2018-1-19 15:40:36
 * @since:  v4.4
 */
public interface BalanceStrategy<T> {
	
	/**
	 * 寻找下一个server
	 *
	 * @param servers the servers
	 * @param context the context
	 * @return the server info
	 */
	public T balance(TargetKeeper<T> keeper, BalancerContext<T> context) throws ServerNotFoundException;
}
