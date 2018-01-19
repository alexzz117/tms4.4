/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  ConsistentHashingStrategy.java   
 * @Package cn.com.higinet.tms.common.balancer.strategies   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-19 16:17:00   
 * @version V1.0 
 * @Copyright: 2018 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.balancer.strategies;

import java.util.Random;
import java.util.UUID;

import cn.com.higinet.tms.common.balancer.BalancerContext;
import cn.com.higinet.tms.common.balancer.ServerNotFoundException;
import cn.com.higinet.tms.common.balancer.TargetKeeper;
import cn.com.higinet.tms.common.util.ObjectUtils;
import cn.com.higinet.tms.common.util.StringUtils;

/**
 * 基于一致性hash算法的负载均衡策略
 *
 * @ClassName:  ConsistentHashingStrategy
 * @author: 王兴
 * @date:   2018-1-19 16:17:00
 * @since:  v4.4
 */
public class ConsistentHashingStrategy<T> implements BalanceStrategy<T> {
	private int calculateThreshold;

	public ConsistentHashingStrategy(int calculateThreshold) {
		this.calculateThreshold = calculateThreshold;
	}

	@Override
	public T balance(TargetKeeper<T> keeper, BalancerContext<T> context) throws ServerNotFoundException {
		String dispatchID = context.getBalanceID();
		dispatchID = StringUtils.isNull(dispatchID) ? UUID.randomUUID().toString() : dispatchID;
		long hashId = (ObjectUtils.hash(dispatchID) >>> 1) % 53777;
		Random random = new Random(hashId);
		return choose(keeper, context, random);
	}

	public T choose(TargetKeeper<T> keeper, BalancerContext<T> context, Random random) throws ServerNotFoundException {
		if (calculateThreshold == context.getCurrentCalculateCount() || context.getChoosenTargetsCount() == keeper.availableTargetCount()) {
			throw new ServerNotFoundException();
		}
		int index = random.nextInt(keeper.availableTargetCount());
		T target = keeper.getAvailableTarget(index);
		if (!context.hasBeenChoosen(target)) {
			context.addtarget(target);
			return target;
		}
		return choose(keeper, context, random);
	}

}
