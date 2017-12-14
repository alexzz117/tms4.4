package cn.com.higinet.tms.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import cn.com.higinet.tms.common.util.ClockUtils;
import cn.com.higinet.tms.common.util.ClockUtils.Clock;
import cn.com.higinet.tms.common.util.NumberUtils;
import cn.com.higinet.tms.common.util.StringUtils;

public class MapTest extends BaseConcurrentTest {

	@Test
	public void chmTest() throws InterruptedException {
		ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>(DEFAULT_TIMES * 3 / 2);
		testMapPut(map);
		testMapGet(map);
	}

	@Test
	public void hmTest() throws InterruptedException {
		HashMap<Object, Object> map = new HashMap<>(DEFAULT_TIMES * 3 / 2);
		testMapPut(map);
		testMapGet(map);
	}

	public void testMapPut(final Map<Object, Object> map) throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(DEFAULT_TIMES);
		final Clock c = ClockUtils.createClock();
		final AtomicInteger i = new AtomicInteger();
		createThreads(THREAD_COUNT, new Runnable() {
			@Override
			public void run() {
				while (latch.getCount() > (THREAD_COUNT - 1)) {
					map.put(i.getAndIncrement(), latch.getCount());
					latch.countDown();
				}
			}
		});
		latch.await();
		long cost = c.countMillis();
		System.out.println(StringUtils.format("%s put TPS is \"%d\",total cost %dms,each request cost %fms,map size is %d", map.getClass().getSimpleName(), (DEFAULT_TIMES / cost * 1000), cost,NumberUtils.devide(cost, DEFAULT_TIMES, 5), map.size()));
	}

	public void testMapGet(final Map<Object, Object> map) throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(DEFAULT_TIMES);
		Clock c = ClockUtils.createClock();
		final AtomicInteger i = new AtomicInteger();
		createThreads(THREAD_COUNT, new Runnable() {
			@Override
			public void run() {
				while (latch.getCount() > (THREAD_COUNT - 1)) {
					map.get(i.getAndIncrement());
					latch.countDown();
				}
			}
		});
		latch.await();
		long cost = c.countMillis();
		System.out.println(StringUtils.format("%s get TPS is \"%d\",total cost %dms,each request cost %fms", map.getClass().getSimpleName(), (DEFAULT_TIMES / cost * 1000), cost,NumberUtils.devide(cost, DEFAULT_TIMES, 5)));
	}
}
