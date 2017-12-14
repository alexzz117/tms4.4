/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  ThreadPoolTest.java   
 * @Package cn.com.higinet.tms.common   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-11 11:15:57   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import cn.com.higinet.tms.common.executor.StandardThreadExecutor;

/**
 * 线程池的单元测试类
 *
 * @author: 王兴
 * @date:   2017-5-11 11:16:01
 * @since:  v5.0
 */
public class ThreadPoolTest {
	/**
	 * Thread pool test.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void threadPoolTest() throws InterruptedException {
		ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 10, 2, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(5));
		pool.setRejectedExecutionHandler(new CallerRunsPolicy());
		CountDownLatch latch = new CountDownLatch(10000);
		while (latch.getCount() > 0) {
			pool.submit(new Runnable() {

				@Override
				public void run() {
					try {
						//						Thread.sleep(100);
//						System.out.println(Thread.currentThread().getName());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			});
		}
		latch.await();
	}

	/**
	 * Standard thread pool test.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void standardThreadPoolTest() throws InterruptedException {
		StandardThreadExecutor pool = new StandardThreadExecutor();
		pool.setRejectedExecutionHandler(new CallerRunsPolicy());
		pool.start();
		CountDownLatch latch = new CountDownLatch(10000);
		while (latch.getCount() > 0) {
			pool.submit(new Runnable() {

				@Override
				public void run() {
					try {
						//						Thread.sleep(100);
//						System.out.println(Thread.currentThread().getName());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			});
		}
		latch.await();
	}

}
