/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  BalanceSender.java   
 * @Package cn.com.higinet.tms.common.balancer.sender   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-19 15:30:37   
 * @version V1.0 
 * @Copyright: 2018 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.balancer.server;

import cn.com.higinet.tms.common.balancer.BalancerContext;

/**
 * 可以直接发送消息的负责均衡器接口
 *
 * @ClassName:  BalanceSender
 * @author: 王兴
 * @date:   2018-1-19 15:30:37
 * @since:  v4.4
 */
public interface BalanceSender {

	/**
	 * 同步发送消息，需要等待应答
	 *
	 * @param context the context
	 * @return the byte[]
	 */
	public byte[] sendData(BalancerContext<ServerInfo> context);
	
	/**
	 * 异步发送消息，通过线程池发送，不需要等待服务器应答
	 *
	 * @param context the context
	 * @return true 是否成功提交线程池task，false则提交不成功
	 */
	public boolean sendDataAsync(BalancerContext<ServerInfo> context);
}
