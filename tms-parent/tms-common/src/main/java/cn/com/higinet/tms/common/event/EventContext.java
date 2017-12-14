/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  Event.java   
 * @Package cn.com.higinet.tms.common.event   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-9 16:16:13   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.event;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.com.higinet.tms.common.lang.Copyable;
import cn.com.higinet.tms.common.util.ClockUtils;
import cn.com.higinet.tms.common.util.ClockUtils.Clock;

/**
 * 事件对象，实现了克隆与序列化接口，
 * 事件中包含了当前的主题。
 *
 * @author: 王兴
 * @date:   2017-5-9 16:16:17
 * @since:  v4.3
 */
public class EventContext implements Copyable<EventContext>, Serializable {

	/** 事件主题. */
	private final String topic;

	/** 发起时间的源通道，默认是none */
	private String sourceChannel = "none";

	/** 用于执行过程分析的clock对象. */
	private Clock clock = ClockUtils.createClock();

	/** 事件中包含的对象，channel里面处理的时候可以根据指定类型进行转换。 */
	private Map<String, Object> data = new ConcurrentHashMap<String, Object>();

	/** 在RingBuffer队列中的序号，业务不需要关系此属性. */
	private long sequence;

	/** 是否在RingBuffer队尾，业务不需要关系此属性. */
	private boolean endOfBatch;

	private static final long serialVersionUID = -8999547272120236500L;

	public EventContext(String topic) {
		this.topic = topic;
	}

	public String getTopic() {
		return topic;
	}

	public String getSourceChannel() {
		return sourceChannel;
	}

	public void setSourceChannel(String sourceChannel) {
		this.sourceChannel = sourceChannel;
	}

	public Clock getClock() {
		return clock;
	}

	public void setClock(Clock clock) {
		this.clock = clock;
	}

	public long getSequence() {
		return sequence;
	}

	public void setSequence(long sequence) {
		this.sequence = sequence;
	}

	public boolean isEndOfBatch() {
		return endOfBatch;
	}

	public void setEndOfBatch(boolean endOfBatch) {
		this.endOfBatch = endOfBatch;
	}

	/**
	 * 根据指定的类型，返回转换过后的data对象
	 *
	 * @param <T> the generic type
	 * @param t the t
	 * @return data 属性
	 */
	@SuppressWarnings("unchecked")
	public <T> T getData(Class<T> t, String key) {
		Object res = data.get(key);
		if (res == null) {
			return null;
		}
		return (T) res;
	}

	public Object getData(String key) {
		return data.get(key);
	}

	public void setData(String key, Object value) {
		this.data.put(key, value);
	}

	/**
	 * 浅克隆
	 * @see cn.com.higinet.tms.common.lang.Copyable#copy()
	 */
	@Override
	public EventContext copy() {
		EventContext e = new EventContext(topic);
		e.setClock(clock);
		e.data = this.data;
		return e;
	}

	/**
	 * 深克隆
	 * @see cn.com.higinet.tms.common.lang.Copyable#deepCopy()
	 */
	@Override
	public EventContext deepCopy() {
		EventContext e = new EventContext(topic);
		e.setClock(clock);
		//data进行深克隆，暂时没实现
		return e;
	}

	public void copy(EventContext context) {
		context.clock = clock;
		context.data = this.data;
		context.endOfBatch = endOfBatch;
		context.sequence = sequence;
	}

}
