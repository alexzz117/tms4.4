package cn.com.higinet.tms.common;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;

public class SyncTest extends BaseConcurrentTest {

	/**
	 * {类的详细说明}.
	 *
	 * @author: 王兴
	 * @date:   2017-5-10 16:07:36
	 * @since:  v5.0
	 */
	private static class IntTest {

		/** i. */
		private int i = 0;

		/**
		 * Incr.
		 */
		public void incr() {
			i++;
		}

		/**
		 * Gets the.
		 *
		 * @return the int
		 */
		public int get() {
			return i;
		}
	}

	/**
	 * Sync cost test.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void syncCostTest() throws InterruptedException {
		final IntTest _i = new IntTest();
		final CountDownLatch latch = new CountDownLatch(DEFAULT_TIMES);
		createThreads(THREAD_COUNT, new Runnable() {

			@Override
			public void run() {
				while (latch.getCount() > (THREAD_COUNT - 1)) {
					synchronized (TOPIC) {
						_i.incr();
					}
					latch.countDown();
				}
			}

		});
		latch.await();
		System.out.println("sync:" + _i.get());
	}

	/**
	 * Lock cost test.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void lockCostTest() throws InterruptedException {
		final IntTest _i = new IntTest();
		final Lock lock = new ReentrantLock();
		final CountDownLatch latch = new CountDownLatch(DEFAULT_TIMES);
		createThreads(THREAD_COUNT, new Runnable() {

			@Override
			public void run() {
				while (latch.getCount() > (THREAD_COUNT - 1)) {
					try {
						lock.lock();
						_i.incr();
					} finally {
						lock.unlock();
						latch.countDown();
					}
				}
			}

		});

		latch.await();
		System.out.println("lock:" + _i.get());
	}

	/**
	 * Cas cost test.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void casCostTest() throws InterruptedException {
		final AtomicLong l = new AtomicLong();
		final CountDownLatch latch = new CountDownLatch(DEFAULT_TIMES);
		createThreads(THREAD_COUNT, new Runnable() {

			@Override
			public void run() {
				while (latch.getCount() > (THREAD_COUNT - 1)) {
					try {
						l.incrementAndGet();
					} finally {
						latch.countDown();
					}
				}
			}

		});
		latch.await();
		System.out.println("cas:" + l.get());
	}

	/**
	 * Blocking queue cost test.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void blockingQueueCostTest() throws InterruptedException {
		final BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
		final CountDownLatch latch = new CountDownLatch(DEFAULT_TIMES);
		createThreads(THREAD_COUNT, new Runnable() {

			@Override
			public void run() {
				while (latch.getCount() > (THREAD_COUNT - 1)) {
					try {
						queue.put(new Integer(1));
					} catch (InterruptedException e) {
					} finally {
						latch.countDown();
					}
				}
			}

		});
		Thread counter = new Thread(new Runnable() {
			private int i;

			@Override
			public void run() {
				try {
					while (true) {
						i += queue.take();
					}
				} catch (InterruptedException e) {
					System.out.println("bq:" + i);
				}
			}

		});
		counter.start();
		latch.await();
		while (queue.size() > 0) {
		}
		counter.interrupt();
		counter.join();
	}

	@Test
	public void lockTest() throws InterruptedException {
		final Lock lock = new ReentrantLock();
		final CountDownLatch latch = new CountDownLatch(2);
		new Thread(new Runnable() {
			@Override
			public void run() {
				lock.lock();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
					latch.countDown();
				}
			}
		}).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				lock.lock();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
					latch.countDown();
				}
			}
		}).start();
		latch.await();
	}

	@Test
	public void syncTest() throws InterruptedException {
		final Object lock = new ReentrantLock();
		final CountDownLatch latch = new CountDownLatch(2);
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					try {
						lock.wait(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						latch.countDown();
					}
				}
			}
		}).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					try {
						lock.wait(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						latch.countDown();
					}
				}
			}
		}).start();
		latch.await();
	}
	//	@Test
	//	public void bufferTest() {
	//		while (true) {
	//			ByteBuffer buffer = ByteBuffer.allocateDirect(10 * 1024 * 1024);
	//		}
	//	}

	//	/**
	//	 * -XX:+DisableExplicitGC	会导致OOM异常
	//	 * @param args
	//	 */
	//	public static void main(String[] args) {
	//		//		while(true){
	//		//			ByteBuffer.allocateDirect(10*1024*1024);
	//		//		}
	//		int i = 0;
	//		try {
	//			while (true) {
	//				i++;
	//				new Thread(new Runnable() {
	//
	//					@Override
	//					public void run() {
	//						try {
	//							Thread.sleep(1000000);
	//						} catch (InterruptedException e) {
	//							e.printStackTrace();
	//						}
	//					}
	//
	//				}).start();
	//			}
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//			System.out.println(i);
	//		}
	//	}
}
