/*
 ***************************************************************************************
` * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  EventTest.java   
 * @Package cn.com.higinet.tms.common   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-10 16:07:36   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import cn.com.higinet.tms.common.event.EventBus;
import cn.com.higinet.tms.common.event.EventChannel;
import cn.com.higinet.tms.common.event.EventChannelHandler;
import cn.com.higinet.tms.common.event.EventContext;
import cn.com.higinet.tms.common.event.impl.BatchDisruptorEventChannelHandler;
import cn.com.higinet.tms.common.event.impl.ConcurrentEventChannel;
import cn.com.higinet.tms.common.event.impl.DisruptorEventBus;
import cn.com.higinet.tms.common.event.impl.DisruptorEventChannel;
import cn.com.higinet.tms.common.event.impl.SimpleEventBus;
import cn.com.higinet.tms.common.event.impl.SimpleEventChannel;

/**
 * 事件总线单元测试类
 *
 * @author: 王兴
 * @date:   2017-5-10 16:07:36
 * @since:  v5.0
 */
public class EventChannelTest extends BaseConcurrentTest {

	/**
	 * 异步总线搭配异步通道
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void disruptorBusWithConcurrentChannelCostTest() throws InterruptedException {
		final EventBus bus = new DisruptorEventBus();
		ConcurrentEventChannel channel = new ConcurrentEventChannel(TOPIC);
		channel.setName("testChannel");
		channel.setEventHandler(new EventChannelHandler() {

			@Override
			public void handleEvent(EventContext event, EventChannel channel) throws Exception {
				//				System.out.println(Thread.currentThread().getName());
			}

		});
		bus.registerChannel(channel);
		bus.start();
		final CountDownLatch latch = new CountDownLatch(DEFAULT_TIMES);
		createThreads(THREAD_COUNT, new Runnable() {

			@Override
			public void run() {
				while (latch.getCount() > (THREAD_COUNT - 1)) {
					EventContext e = new EventContext(TOPIC);
					e.setData("in", 1);
					try {
						bus.publish(e);
					} catch (Exception e1) {
						e1.printStackTrace();
					} finally {
						latch.countDown();
					}
				}
			}

		});
		latch.await();
		bus.stop();
	}

	/**
	 * 异步总线搭配异步通道
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void disruptorBusWithDisruptorChannelCostTest() throws InterruptedException {
		final EventBus bus = new DisruptorEventBus();
		DisruptorEventChannel channel = new DisruptorEventChannel(TOPIC);
		channel.setName("testChannel");
		channel.setEventHandler(new EventChannelHandler() {

			@Override
			public void handleEvent(EventContext event, EventChannel channel) throws Exception {
				//				System.out.println(Thread.currentThread().getName());
			}

		});
		bus.registerChannel(channel);
		bus.start();
		final CountDownLatch latch = new CountDownLatch(DEFAULT_TIMES);
		createThreads(THREAD_COUNT, new Runnable() {

			@Override
			public void run() {
				while (latch.getCount() > (THREAD_COUNT - 1)) {
					EventContext e = new EventContext(TOPIC);
					e.setData("in", 1);
					try {
						bus.publish(e);
					} catch (Exception e1) {
						e1.printStackTrace();
					} finally {
						latch.countDown();
					}
				}
			}

		});
		latch.await();
		bus.stop();
	}

	/**
	 * 单线程总线搭配单线程通道
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void simpleBusWithSimpleChannelTest() throws InterruptedException {
		final EventBus bus = new SimpleEventBus();
		SimpleEventChannel channel = new SimpleEventChannel(TOPIC);
		channel.setName("testChannel");
		channel.setEventHandler(new EventChannelHandler() {

			@Override
			public void handleEvent(EventContext event, EventChannel channel) throws Exception {
				//				System.out.println(Thread.currentThread().getName());
			}

		});
		bus.registerChannel(channel);
		bus.start();
		final CountDownLatch latch = new CountDownLatch(DEFAULT_TIMES);
		createThreads(THREAD_COUNT, new Runnable() {

			@Override
			public void run() {
				while (latch.getCount() > (THREAD_COUNT - 1)) {
					EventContext e = new EventContext(TOPIC);
					e.setData("a", 1);
					try {
						bus.publish(e);
					} catch (Exception e1) {
						e1.printStackTrace();
					} finally {
						latch.countDown();
					}
				}
			}

		});
		latch.await();
		bus.stop();
	}

	/**
	 * 异步总线搭配单线程通道
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void disruptorBusWithSimpleChannelTest() throws InterruptedException {
		final EventBus bus = new DisruptorEventBus();
		SimpleEventChannel channel = new SimpleEventChannel(TOPIC);
		channel.setName("testChannel");
		channel.setEventHandler(new EventChannelHandler() {

			@Override
			public void handleEvent(EventContext event, EventChannel channel) throws Exception {
				//				System.out.println(Thread.currentThread().getName());
			}

		});
		bus.registerChannel(channel);
		bus.start();
		final CountDownLatch latch = new CountDownLatch(DEFAULT_TIMES);
		createThreads(THREAD_COUNT, new Runnable() {

			@Override
			public void run() {
				while (latch.getCount() > (THREAD_COUNT - 1)) {
					EventContext e = new EventContext(TOPIC);
					e.setData("a", 1);
					try {
						bus.publish(e);
					} catch (Exception e1) {
						e1.printStackTrace();
					} finally {
						latch.countDown();
					}
				}
			}

		});
		latch.await();
		bus.stop();
	}

	/**
	 * 单线程总线搭配异步通道
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void simpleBusWithConcurrentChannelTest() throws InterruptedException {
		final EventBus bus = new SimpleEventBus();
		ConcurrentEventChannel channel = new ConcurrentEventChannel(TOPIC);
		channel.setName("testChannel");
		channel.setEventHandler(new EventChannelHandler() {

			@Override
			public void handleEvent(EventContext event, EventChannel channel) throws Exception {
				//				System.out.println(Thread.currentThread().getName());
			}

		});
		bus.registerChannel(channel);
		bus.start();
		final CountDownLatch latch = new CountDownLatch(DEFAULT_TIMES);
		createThreads(THREAD_COUNT, new Runnable() {

			@Override
			public void run() {
				while (latch.getCount() > (THREAD_COUNT - 1)) {
					EventContext e = new EventContext(TOPIC);
					e.setData("a", 1);
					try {
						bus.publish(e);
					} catch (Exception e1) {
						e1.printStackTrace();
					} finally {
						latch.countDown();
					}
				}
			}

		});
		latch.await();
		bus.stop();
	}

	/**
	 * 单线程总线搭配异步通道
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void simpleBusWithDisruptorChannelTest() throws InterruptedException {
		final EventBus bus = new SimpleEventBus();
		DisruptorEventChannel channel = new DisruptorEventChannel(TOPIC);
		channel.setName("testChannel");
		channel.setEventHandler(new EventChannelHandler() {

			@Override
			public void handleEvent(EventContext event, EventChannel channel) throws Exception {
			}
		});
		bus.registerChannel(channel);
		bus.start();
		final CountDownLatch latch = new CountDownLatch(DEFAULT_TIMES);
		createThreads(THREAD_COUNT, new Runnable() {

			@Override
			public void run() {
				while (latch.getCount() > (THREAD_COUNT - 1)) {
					EventContext e = new EventContext(TOPIC);
					e.setData("a", 1);
					try {
						bus.publish(e);
					} catch (Exception e1) {
						e1.printStackTrace();
					} finally {
						latch.countDown();
					}
				}
			}
		});
		latch.await();
		bus.stop();
	}

	/**
	 * 单线程总线搭配多个通道
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void simpleBusWithMultiChannelTest() throws InterruptedException {
		final EventBus bus = new SimpleEventBus();
		ConcurrentEventChannel channel1 = new ConcurrentEventChannel("1");
		channel1.setName("channel1");
		channel1.setEventHandler(new EventChannelHandler() {

			@Override
			public void handleEvent(EventContext event, EventChannel channel) throws Exception {
				//				System.out.println(Thread.currentThread().getName());
			}

		});
		bus.registerChannel(channel1);

		SimpleEventChannel channel2 = new SimpleEventChannel("2");
		channel2.setName("channel2");
		channel2.setEventHandler(new EventChannelHandler() {

			@Override
			public void handleEvent(EventContext event, EventChannel channel) throws Exception {
				//				System.out.println(Thread.currentThread().getName());
			}

		});
		bus.registerChannel(channel2);

		SimpleEventChannel channel3 = new SimpleEventChannel("3");
		channel3.setName("channel3");
		channel3.setEventHandler(new EventChannelHandler() {

			@Override
			public void handleEvent(EventContext event, EventChannel channel) throws Exception {
				//				System.out.println(Thread.currentThread().getName());
			}

		});
		bus.registerChannel(channel3);
		bus.start();
		final CountDownLatch latch = new CountDownLatch(DEFAULT_TIMES);
		createThreads(THREAD_COUNT, new Runnable() {

			@Override
			public void run() {
				while (latch.getCount() > (THREAD_COUNT - 1)) {
					int i = 0;
					int v = Long.valueOf(i++ % 3).intValue();
					EventContext e = new EventContext(String.valueOf(++v));
					e.setData("a", v);
					try {
						bus.publish(e);
					} catch (Exception e1) {
						e1.printStackTrace();
					} finally {
						latch.countDown();
					}
				}
			}

		});
		latch.await();
		bus.stop();
	}

	/**
	 * 单线程总线搭配异步通道
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void simpleBusWithBatchDisruptorEventChannelHandlerTest() throws InterruptedException {
		final EventBus bus = new SimpleEventBus();
		DisruptorEventChannel channel = new DisruptorEventChannel(TOPIC);
		channel.setName("testChannel");
		channel.setEventHandler(new BatchDisruptorEventChannelHandler() {
			private int i = 0;

			@Override
			public void handleEvents(List<EventContext> events, EventChannel channel) throws Exception {
//				System.out.println(i);
			}
		});
		bus.registerChannel(channel);
		bus.start();
		final CountDownLatch latch = new CountDownLatch(DEFAULT_TIMES);
		createThreads(THREAD_COUNT, new Runnable() {

			@Override
			public void run() {
				while (latch.getCount() > (THREAD_COUNT - 1)) {
					EventContext e = new EventContext(TOPIC);
					e.setData("a", 1);
					try {
						bus.publish(e);
					} catch (Exception e1) {
						e1.printStackTrace();
					} finally {
						latch.countDown();
					}
				}
			}

		});
		latch.await();
		bus.stop();
	}
}
