/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  Dispatcher.java   
 * @Package cn.com.higinet.tms.common.event   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-9 16:20:48   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.event;

/**
 * 事件的分发器，每个EventBus对象有一个自己的分发器，会根据event对象的topic分发到指定的channel中
 *
 * @author: 王兴
 * @date:   2017-5-9 16:20:48
 * @since:  v4.3
 */
public interface EventDispatcher {
	/**
	 * 注册事件通道，通道可以复用，注册到多个主题中
	 *
	 * @param topic 主题
	 * @param channel 通道对象
	 */
	public void registerChannel(EventChannel channel);

	/**
	 * 注销事件通道
	 *
	 * @param topic 主题
	 * @param channel 通道对象
	 */
	public void unregisterChannel(EventChannel channel);

	/**
	 * 分发事件.
	 *
	 * @param event 事件对象
	 */
	public void dispatch(EventContext event);
}
