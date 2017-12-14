/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  EventBus.java   
 * @Package cn.com.higinet.tms.common.event   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-9 15:12:25   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.event.impl;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import cn.com.higinet.tms.common.event.EventWrapper;
import cn.com.higinet.tms.common.event.EventContext;
import cn.com.higinet.tms.common.executor.TaskThreadFactory;

/**
 * 基于disruptor实现的消息总线，详细介绍请参考{@link cn.com.higinet.tms.common.event.EventBus}。
 *
 * @author: 王兴
 * @date:   2017-5-9 15:12:25
 * @since:  v4.3
 */
public class DisruptorEventBus extends AbstractEventBus {

	/** 默认processor线程的名字 */
	private static final String DEFAULT_NAME = "default-event-bus";

	/** 默认缓存队列大小. */
	private int DEFAULT_EVENT_BUFFERSIZE = 1024 * 8;

	/** ringbuffer的缓存队列大小，建议通过配置实现. */
	private int bufferSize = 0;

	/** 内部Disruptor对象. */
	private Disruptor<EventWrapper> eventBus;

	/** 消费端等待策略，有很多种等待策略，可以参考 com.lmax.disruptor.WaitStrategy
	 * BlockingWaitStrategy 是最低效的策略，但其对CPU的消耗最小并且在各种不同部署环境中能提供更加一致的性能表现；
	 * SleepingWaitStrategy 的性能表现跟 BlockingWaitStrategy 差不多，对 CPU 的消耗也类似，但其对生产者线程的影响最小，适合用于异步日志类似的场景；
	 * YieldingWaitStrategy 的性能是最好的，适合用于低延迟的系统。这种策略在减低系统延迟的同时也会增加CPU运算量，在要求极高性能且事件处理线数小于 CPU 逻辑核心数的场景中，推荐使用此策略；例如，CPU开启超线程的特性。
	 * BusySpinWaitStrategy 是性能最高的等待策略，同时也是对部署环境要求最高的策略。这个性能最好用在事件处理线程比物理内核数目还要小的时候。例如：在禁用超线程技术的时候。
	 * PhasedBackoffWaitStrategy： Spins, then yields, then waits，不过还是适合对低延迟和吞吐率不像CPU占用那么重要的情况。
	 * TimeoutBlockingWaitStrategy： sleep一段时间。 低延迟。
	 */
	private WaitStrategy BLOCKING_WAIT = new BlockingWaitStrategy();

	public int getBufferSize() {
		return bufferSize == 0 ? DEFAULT_EVENT_BUFFERSIZE : bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * @see cn.com.higinet.tms.common.event.EventBus#publish(cn.com.higinet.tms.common.event.EventContext)
	 */
	@Override
	public void publish(EventContext event) throws Exception {
		if (isRunning()) {
			long seq = eventBus.getRingBuffer().next();
			try {
				EventWrapper e = eventBus.getRingBuffer().get(seq);
				e.setEvent(event);
			} finally {
				eventBus.getRingBuffer().publish(seq);
			}
		} else {
			throw new cn.com.higinet.tms.common.exception.UnsupportedOperationException("Can't publish event when EventBus's status is \"%s\"", getStatusName());
		}
	}

	/**
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStart()
	 */
	@Override
	protected void doStart() throws Throwable {
		setName(DEFAULT_NAME);
		eventBus = new Disruptor<EventWrapper>(new DisruptorEventFactory(), getBufferSize(), new TaskThreadFactory(getName(), true, Thread.NORM_PRIORITY), ProducerType.MULTI, BLOCKING_WAIT);
		SequenceBarrier barrier = eventBus.getRingBuffer().newBarrier(new Sequence[0]);
		EventProcessor processor = new DisruptorEventProcessor(eventBus.getRingBuffer(), barrier, new DisruptorEventHandler(dispatcher));
		eventBus.handleEventsWith(processor); //只需要设置一个处理器，不要设置多个，总线只负责分发
		eventBus.start();
		super.doStart();
	}

	/**
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStop()
	 */
	@Override
	protected void doStop() throws Throwable {
		eventBus.shutdown();
		super.doStop();
	}

}
