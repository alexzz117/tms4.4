/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  ServerManager.java   
 * @Package cn.com.higinet.tms.common.balancer   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-19 15:39:28   
 * @version V1.0 
 * @Copyright: 2018 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.balancer;

import cn.com.higinet.tms.common.balancer.strategies.BalanceStrategy;
import cn.com.higinet.tms.common.balancer.strategies.ConsistentHashingStrategy;
import cn.com.higinet.tms.common.lifecycle.Service;
import cn.com.higinet.tms.common.lifecycle.StartServiceException;

/**
 * 管理服务器对象的类
 *
 * @ClassName:  Balancer
 * @author: 王兴
 * @date:   2018-1-19 15:39:28
 * @since:  v4.4
 */
public class Balancer<T> extends Service {

	/**server info 的存管类. */
	private TargetKeeper<T> targetKeeper = new TargetKeeper<>();

	/** 负载均衡策略，默认一致性哈希. */
	private BalanceStrategy<T> strategy;

	/** 当前为了寻找可用服务器而计算的总次数，让人如果超过此计算次数后，依然没有找到服务器，那么抛出 ServerNotFoundException*/
	private int calculateThreshold;

	public void setCalculateThreshold(int calculateThreshold) {
		this.calculateThreshold = calculateThreshold;
	}

	public void setStrategy(BalanceStrategy<T> strategy) {
		this.strategy = strategy;
	}


	public void setAvailableTargets(T[] availableTargets) {
		targetKeeper.setAvailableTargets(availableTargets);
	}

	/**
	 * 选择一个可用的server
	 *
	 * @param context the context
	 * @return the server info
	 */
	public T chooseTarget(BalancerContext<T> context) throws ServerNotFoundException {
		if (targetKeeper.isEmpty()) {
			throw new ServerNotFoundException("Available servers is empty,please ensure that available servers are setted.");
		}
		return strategy.balance(targetKeeper, context);
	}

	@Override
	protected void doStart() throws Throwable {
		if (targetKeeper.isEmpty()) {
			throw new StartServiceException("Please initialize available servers before start ServerManager.");
		}
		if (calculateThreshold == 0) {
			throw new StartServiceException("Please set \"calculateThreshold\" before start ServerManager.");
		}
		if (strategy == null)
			strategy = new ConsistentHashingStrategy<T>(calculateThreshold);
	}

	@Override
	protected void doStop() throws Throwable {
	}

}
