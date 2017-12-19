/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  AbstractEventChannel.java   
 * @Package cn.com.higinet.tms.common.event.impl   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-10 15:45:49   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.event.impl;

import cn.com.higinet.tms.common.event.EventBus;
import cn.com.higinet.tms.common.event.EventChannel;
import cn.com.higinet.tms.common.event.EventChannelHandler;
import cn.com.higinet.tms.common.event.EventContext;
import cn.com.higinet.tms.common.lang.Initializable;
import cn.com.higinet.tms.common.lifecycle.Service;
import cn.com.higinet.tms.common.util.StringUtils;

/**
 * 抽象的事件通道，所有事件通道的基类，详细介绍参考{@link cn.com.higinet.tms.common.event.EventChannel}
 *
 * @author: 王兴
 * @date:   2017-5-10 15:45:49
 * @since:  v4.3
 */
public abstract class AbstractEventChannel extends Service implements EventChannel {
	/** 通道订阅的topic属性. */
	protected final String topic;

	/** 与此通道关联的事件总线. */
	protected EventBus bus;

	/** 实现处理类. */
	protected EventChannelHandler handler;

	/**
	 * 构造一个新的对象.
	 *
	 * @param topic the topic
	 */
	public AbstractEventChannel(String topic) {
		this.topic = topic;
	}

	/**
	 * 构造一个新的对象.
	 *
	 * @param topic the topic
	 */
	public AbstractEventChannel(String channelName, String topic) {
		this.name = channelName;
		this.topic = topic;
	}

	public void setBus(EventBus bus) {
		this.bus = bus;
	}

	public EventBus getBus() {
		return bus;
	}

	@Override
	public String getChannelName() {
		return getName();
	}

	public EventChannelHandler getHandler() {
		return handler;
	}

	/**
	 * @see cn.com.higinet.tms.common.event.EventChannel#registered(cn.com.higinet.tms.common.event.EventBus)
	 */
	@Override
	public void registered(EventBus bus) {
		if (this.bus != null) {
			throw new UnsupportedOperationException("This channel is already registered!");
		}
		setBus(bus);
	}

	/**
	 * @see cn.com.higinet.tms.common.event.EventChannel#publish(cn.com.higinet.tms.common.event.EventContext)
	 */
	@Override
	public void publish(String targetTopic, EventContext event) throws Exception {
		if (bus == null) {
			throw new UnsupportedOperationException("Can't publish event before register this channel to a event bus.");
		}
		if (StringUtils.isNull(targetTopic)) {
			throw new UnsupportedOperationException("Can't publish event on the topic \"null\".");
		}
		if (targetTopic.equals(topic)) {
			throw new UnsupportedOperationException("Can't publish event on the topic which subscribed by the channel itself.");
		}
		EventContext _event = null;
		//这里避免修改原event对象的topic，因为可能原event对象会被多个channel使用，如果是单线程通道还好说，就怕多个异步通道同时使用event，同时修改event对象的topic
		//因此这里如果发现目标topic与当前event不一致时，复制event的简单属性给新生成的event对象
		if (!targetTopic.equalsIgnoreCase(event.getTopic())) {
			_event = new EventContext(targetTopic);
			event.copy(_event);
		} else {
			_event = event;//有可能这个event是在handler内部新增的，这种情况直接使用event对象
		}
		if (bus != null) {
			_event.setSourceChannel(this.getChannelName());
			bus.publish(_event);
		}
	}

	@Override
	public String getTopic() {
		return topic;
	}

	/**
	 * @see cn.com.higinet.tms.common.event.EventChannel#unregistered(cn.com.higinet.tms.common.event.EventBus)
	 */
	@Override
	public void unregistered(EventBus bus) {
		setBus(null);
	}

	/**
	 * @see cn.com.higinet.tms.common.event.EventChannel#busStarted(cn.com.higinet.tms.common.event.EventBus)
	 */
	@Override
	public void busStarted(EventBus bus) {
		// TODO Auto-generated method stub
		if (handler instanceof Initializable) {
			try {
				((Initializable) handler).initialize();
			} catch (Exception e) {
				logger.error("EventChannel " + getChannelName() + "eventChannelHandler initialize failed.", e);
			}
		}
	}

	/**
	 * @see cn.com.higinet.tms.common.event.EventChannel#busStopped(cn.com.higinet.tms.common.event.EventBus)
	 */
	@Override
	public void busStopped(EventBus bus) {
		if (handler instanceof Initializable) {
			try {
				((Initializable) handler).destroy();
				;
			} catch (Exception e) {
				logger.error("EventChannel " + getChannelName() + "eventChannelHandler initialize failed.", e);
			}
		}
	}

	/**
	 * @see cn.com.higinet.tms.common.event.EventChannel#setEventHandler(com.lmax.disruptor.EventHandler)
	 */
	@Override
	public void setEventHandler(EventChannelHandler handler) {
		this.handler = handler;
		if (StringUtils.isNull(name)) {
			this.name = handler.getClass().getSimpleName() + "-channel";
		}
	}
}
