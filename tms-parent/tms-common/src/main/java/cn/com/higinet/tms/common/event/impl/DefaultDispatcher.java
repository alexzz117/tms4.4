/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  DefaultDispatcher.java   
 * @Package cn.com.higinet.tms.common.event.impl   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-9 18:59:21   
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.common.event.EventContext;
import cn.com.higinet.tms.common.event.EventChannel;
import cn.com.higinet.tms.common.event.EventDispatcher;
import cn.com.higinet.tms.common.util.StringUtils;

/**
 * 事件调度器实现，只根据topic选择对应的channel，如果一个topic包含有多个channel，
 * 那么则会将event深克隆多份，分别发送至每个channel。
 * 
 *
 * @author: 王兴
 * @date: 2017-5-9 18:59:23
 * @since: v5.0
 */
public class DefaultDispatcher implements EventDispatcher {
	private static final Logger logger = LoggerFactory.getLogger(DefaultDispatcher.class);

	private Map<String, List<EventChannel>> topics = new ConcurrentHashMap<String, List<EventChannel>>();

	/** 已经分派过的事件数，可以用于统计。 */
	private AtomicLong handledEvents = new AtomicLong();

	public long getHandledEvents() {
		return handledEvents.get();
	}

	/**
	 * @see cn.com.higinet.tms.common.event.EventDispatcher#registerChannel(java.lang.String,
	 *      cn.com.higinet.tms.common.event.EventChannel)
	 */
	@Override
	public synchronized void registerChannel(EventChannel channel) {
		String topic = channel.getTopic();
		List<EventChannel> eventChannels = topics.get(topic);
		if (eventChannels == null) {
			eventChannels = new ArrayList<EventChannel>();
			topics.put(topic, eventChannels);
		}
		eventChannels.add(channel);
	}

	/**
	 * @see cn.com.higinet.tms.common.event.EventDispatcher#unregisterChannel(java.lang.String,
	 *      cn.com.higinet.tms.common.event.EventChannel)
	 */
	@Override
	public synchronized void unregisterChannel(EventChannel channel) {
		String topic = channel.getTopic();
		List<EventChannel> eventChannels = topics.get(topic);
		if (eventChannels != null) {
			eventChannels.remove(channel);
		}
	}

	/**
	 * @see cn.com.higinet.tms.common.event.EventDispatcher#dispatch(cn.com.higinet.tms.common.event.EventContext)
	 */
	@Override
	public void dispatch(EventContext event) {
		handledEvents.incrementAndGet();
		String topic = event.getTopic();
		List<EventChannel> channels = topics.get(topic);
		if (channels != null && !channels.isEmpty()) {
			if (channels.size() == 1) {// 如果当前topic只有一个channel，大部分情况下是1，直接取第一个channel，发送事件
				try {
					channels.get(0).onEvent(event);
				} catch (Exception e) {
					logger.error("Exception occurred when dispatch event object.", e);
				}
			} else {
				EventChannel channel;
				// 如果当前topic只关联了多个channel，循环往每个通道发送事件
				for (int i = 0, len = channels.size(); i < len; i++) {
					try {
						channel = channels.get(i);
						// channel.onEvent(event.deepCopy());
						channel.onEvent(event);// 暂时不对event对象进行深克隆，始终使用同一个对象，要注意多线程并发控制
					} catch (Exception e) {
						logger.error("Exception occurred when dispatch event object.", e);
					}
				}
			}
		} else {
			logger.warn(StringUtils.format("There is no channel subscribed topic \"%s\"", topic));
		}
	}

}
