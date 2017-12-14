/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  AbstractEventBus.java   
 * @Package cn.com.higinet.tms.common.event.impl   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-10 15:40:00   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.event.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.com.higinet.tms.common.event.EventContext;
import cn.com.higinet.tms.common.event.EventBus;
import cn.com.higinet.tms.common.event.EventChannel;
import cn.com.higinet.tms.common.event.EventDispatcher;
import cn.com.higinet.tms.common.lifecycle.Service;
import cn.com.higinet.tms.common.util.StringUtils;

/**
 * 事件总线的基类，提供了一些基础方法，例如注册、注销通道。
 * 具体说明，请参考{@link cn.com.higinet.tms.common.event.EventBus}
 * 
 * @author: 王兴
 * @date: 2017-5-10 15:40:00
 * @since: v5.0
 */
public abstract class AbstractEventBus extends Service implements EventBus {
	/** 内部调度器 */
	protected EventDispatcher dispatcher = new DefaultDispatcher();

	/** 所有通道对象都在此保存. */
	protected List<EventChannel> allChannels = Collections.synchronizedList(new ArrayList<EventChannel>());

	@Override
	public abstract void publish(EventContext event) throws Exception;

	/**
	 * @see cn.com.higinet.tms.common.event.EventBus#registerChannel(java.lang.String,
	 *      cn.com.higinet.tms.common.event.EventChannel)
	 */
	@Override
	public void registerChannel(EventChannel channel) {
		channel.registered(this);
		dispatcher.registerChannel(channel);
		allChannels.add(channel);
		logger.info("Register channel \"{}\" on topic \"{}\" in EventBus.", channel.getChannelName(),
				channel.getTopic());
	}

	/**
	 * @see cn.com.higinet.tms.common.event.EventBus#unregisterChannel(java.lang.String,
	 *      cn.com.higinet.tms.common.event.EventChannel)
	 */
	@Override
	public void unregisterChannel(EventChannel channel) {
		channel.unregistered(this);
		dispatcher.unregisterChannel(channel);
		allChannels.remove(channel);
		logger.info("Unregister channel \"{}\" on topic \"{}\" in EventBus", channel.getChannelName(),
				channel.getTopic());
	}

	@Override
	protected void doStart() throws Throwable {
		for (int i = 0, len = allChannels.size(); i < len; i++) {
			EventChannel c = allChannels.get(i);
			try {
				c.busStarted(this);
			} catch (Exception e) {
				logger.error(StringUtils.format("Exception occurred when fire \"busStarted\" event on channel \"%s\".",
						c.getChannelName()), e);
			}
			try {
				c.start();
			} catch (Exception e) {
				logger.error(StringUtils.format("Exception occurred when start channel \"%s\".", c.getChannelName()),
						e);
			}
		}

	}

	@Override
	protected void doStop() throws Throwable {
		logger.info("Event Bus is Shutdown,handled {} events.", ((DefaultDispatcher) dispatcher).getHandledEvents());
		for (int i = 0, len = allChannels.size(); i < len; i++) {
			EventChannel c = allChannels.get(i);
			try {
				c.busStopped(this);
			} catch (Exception e) {
				logger.error("Exception occurred when fire \"busStopped\" event on channel \"{}\".",
						c.getChannelName());
			}
			try {
				c.stop();
			} catch (Exception e) {
				logger.error("Exception occurred when stop channel \"{}\".", c.getChannelName());
			}
		}
	}

}
