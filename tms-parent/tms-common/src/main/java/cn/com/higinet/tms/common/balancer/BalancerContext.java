/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  BalancerInfo.java   
 * @Package cn.com.higinet.tms.common.balancer   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-19 15:28:19   
 * @version V1.0 
 * @Copyright: 2018 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.balancer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 负载均衡器的上下文，一次完整的发送消息过程中，有且仅有一个此对象。
 * 完整的发送消息过程定义为：第一次就发送成功或者经过若干次重试后发送成功或者经过若干次重试最终发送失败的过程。
 *
 * @ClassName:  BalancerInfo
 * @author: 王兴
 * @date:   2018-1-19 15:28:19
 * @since:  v4.4
 */
public class BalancerContext<T> {

	/** 当前指向的target索引. */
	private int currentIndex = 0;

	/** 当前为了寻找可用服务器而计算的总次数 */
	private int currentCalculateCount = 0;

	/** 分发ID，由外部入参传入. */
	private String balanceID;

	private T target;

	private int choosenTargetsCount = 0;

	private Set<T> choosentargets = new HashSet<>();

	private Map<String, Object> params = new HashMap<>();

	/**
	 * 获取当前索引
	 *
	 * @return the int
	 */
	public int getCurrentIndex() {
		return currentIndex++;
	}

	/**
	 * 获取当前计算总数
	 *
	 * @return the int
	 */
	public int getCurrentCalculateCount() {
		return currentCalculateCount++;
	}

	public int getChoosenTargetsCount() {
		return choosenTargetsCount;
	}

	/**
	 * 根据指定的类型，返回转换过后的data对象
	 *
	 * @param <T> the generic type
	 * @param t the t
	 * @return data 属性
	 */
	public <S> S getData(Class<S> t, String key) {
		return getData(t, key, null);
	}

	/**
	 * 根据指定的类型，返回转换过后的data对象
	 *
	 * @param <T> the generic type
	 * @param t the t
	 * @param key the key
	 * @param defaultValue the default value
	 * @return data 属性
	 */
	@SuppressWarnings("unchecked")
	public <S> S getData(Class<S> t, String key, S defaultValue) {
		Object res = params.get(key);
		if (res == null) {
			return defaultValue;
		}
		return (S) res;
	}

	public Object getData(String key) {
		return params.get(key);
	}

	public void setData(String key, Object value) {
		this.params.put(key, value);
	}

	public String getBalanceID() {
		return balanceID;
	}

	public void setBalanceID(String balanceID) {
		this.balanceID = balanceID;
	}

	public T getTarget() {
		return target;
	}

	/**
	 * 将选中的server加入到一个set中，用于下一次过滤
	 *
	 * @param server the server
	 */
	public void addtarget(T target) {
		choosenTargetsCount++;
		choosentargets.add(target);
		this.target = target;
	}

	/**
	 * 将选中的server加入到一个set中，用于下一次过滤
	 *
	 * @param server the server
	 */
	public boolean hasBeenChoosen(T target) {
		return choosentargets.contains(target);
	}

}
