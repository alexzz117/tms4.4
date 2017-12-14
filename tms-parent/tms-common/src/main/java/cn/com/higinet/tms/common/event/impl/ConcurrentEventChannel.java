/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  ConcurrentEventChannel.java   
 * @Package cn.com.higinet.tms.common.event.impl   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-10 9:50:02   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.event.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import cn.com.higinet.tms.common.event.EventContext;
import cn.com.higinet.tms.common.executor.StandardThreadExecutor;
import cn.com.higinet.tms.common.util.StringUtils;

/**
 * 支持并发的事件通道，此通道是单独的线程，具有内部的线程池，具体触发event执行的都是通过执行线程去实现的。
 * 详细介绍参考{@link cn.com.higinet.tms.common.event.EventChannel}
 *
 * @author: 王兴
 * @date:   2017-5-10 9:50:02
 * @since:  v4.3
 */
public class ConcurrentEventChannel extends AbstractEventChannel {

	/** 默认缓存队列大小. */
	protected int DEFAULT_EVENT_BUFFERSIZE = 1024 * 8;

	/** 缓存队列大小，建议通过配置实现. */
	protected int bufferSize = 0;

	/** 是否是守护线程，默认是true。 */
	protected boolean daemon = true;

	/** 线程池最大线程数，默认Runtime.getRuntime().availableProcessors() * 2 + 1 */
	protected int threadCount = Runtime.getRuntime().availableProcessors() * 2 + 1;

	/** 通道内部使用的线程池. */
	private ExecutorService executor;

	public ConcurrentEventChannel(String topic) {
		super(topic);
	}

	public ConcurrentEventChannel(String channelName, String topic) {
		super(channelName, topic);
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public int getBufferSize() {
		return bufferSize == 0 ? DEFAULT_EVENT_BUFFERSIZE : bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * @see cn.com.higinet.tms.common.event.EventChannel#onEvent(cn.com.higinet.tms.common.event.EventContext)
	 */
	@Override
	public void onEvent(final EventContext event) {
		executor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					//这里要注意，handler内部是多线程的，最好不要有竞争性资源，避免handler内部线程同步
					handler.handleEvent(event, ConcurrentEventChannel.this);
				} catch (Exception e) {
					logger.error(StringUtils.format("Exception occurred when processing event [topic=%s,source=%s,createtime=%s]", event.getTopic(), event.getSourceChannel(), event.getClock().countMillis()), e);
				}
			}
		});
	}

	/**
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStart()
	 */
	@Override
	protected void doStart() throws Throwable {
		//线程池采用的提交拒绝策略为CallerRunsPolicy，由提交线程自己去调用task
		//		executor = new ThreadPoolExecutor(threadCount, threadCount, 15, TimeUnit.MINUTES, new ArrayBlockingQueue<>(getBufferSize()), new TaskThreadFactory(getName() + "-exec-", true, Thread.NORM_PRIORITY), new CallerRunsPolicy());
		executor = new StandardThreadExecutor();
		StandardThreadExecutor _executor = (StandardThreadExecutor) executor;
		String threadPrefix = (StringUtils.isNull(name) ? topic : name) + "-";
		_executor.setNamePrefix(threadPrefix);
		_executor.setMinSpareThreads(threadCount);
		_executor.setMaxThreads(threadCount);
		_executor.setMaxIdleTime(15 * 60);//秒
		_executor.setMaxQueueSize(getBufferSize());
		_executor.setRejectedExecutionHandler(new CallerRunsPolicy());
		_executor.start();
	}

	/**
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStop()
	 */
	@Override
	protected void doStop() throws Throwable {
		executor.shutdown();
	}

}
