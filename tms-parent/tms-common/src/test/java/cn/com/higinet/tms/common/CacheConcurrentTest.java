package cn.com.higinet.tms.common;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import cn.com.higinet.tms.common.cache.Cache;
import cn.com.higinet.tms.common.cache.CacheManager;
import cn.com.higinet.tms.common.cache.CacheProvider;
import cn.com.higinet.tms.common.cache.KV;
import cn.com.higinet.tms.common.cache.initializer.ConfigInitializer;
import cn.com.higinet.tms.common.util.ClockUtils;
import cn.com.higinet.tms.common.util.ClockUtils.Clock;
import cn.com.higinet.tms.common.util.StringUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CacheConcurrentTest extends BaseConcurrentTest {
	private static CacheManager cm = new CacheManager();

	private static final String OP_SET = "set";

	private static final String OP_GET = "get";

	private static final String OP_DELETE = "delete";

	static {
		cm.setEnvInitializer(new ConfigInitializer());
		cm.start();
	}

	@Test
	public void a_ehcacheSet() throws Throwable {
		CacheProvider provider = cm.getProvider("ehcache");
		baseConcurrentTest(OP_SET, provider);
	}

	@Test
	public void a_redisSet() throws Throwable {
		CacheProvider provider = cm.getProvider("redis");
		baseConcurrentTest(OP_SET, provider);
	}

	@Test
	public void a_asSet() throws Throwable {
		CacheProvider provider = cm.getProvider("aerospike");
		baseConcurrentTest(OP_SET, provider);
	}

	@Test
	public void b_ehcacheGet() throws Throwable {
		CacheProvider provider = cm.getProvider("ehcache");
		baseConcurrentTest(OP_GET, provider);
	}

	@Test
	public void b_redisGet() throws Throwable {
		CacheProvider provider = cm.getProvider("redis");
		baseConcurrentTest(OP_GET, provider);
	}

	@Test
	public void b_asGet() throws Throwable {
		CacheProvider provider = cm.getProvider("aerospike");
		baseConcurrentTest(OP_GET, provider);
	}

	@Test
	public void c_ehcacheDel() throws Throwable {
		CacheProvider provider = cm.getProvider("ehcache");
		baseConcurrentTest(OP_DELETE, provider);
	}

	@Test
	public void c_redisDel() throws Throwable {
		CacheProvider provider = cm.getProvider("redis");
		baseConcurrentTest(OP_DELETE, provider);
	}

	@Test
	public void c_asDel() throws Throwable {
		CacheProvider provider = cm.getProvider("aerospike");
		baseConcurrentTest(OP_DELETE, provider);
	}

	public void baseConcurrentTest(final String operation,final CacheProvider provider) throws Throwable {
		final AtomicInteger counter = new AtomicInteger(0);
		Clock c = ClockUtils.createClock();
		final CountDownLatch latch = new CountDownLatch(DEFAULT_TIMES);
		createThreads(THREAD_COUNT, new Runnable() {
			@Override
			public void run() {
				byte[] data = getData(DEFAULT_DATA_SIZE);
				while (latch.getCount() > (THREAD_COUNT - 1)) {
					try {
						exec(operation, counter, data, provider);
					} catch (Throwable e1) {
						e1.printStackTrace();
					} finally {
						latch.countDown();
					}
				}
			}

		});
		latch.await();
		System.out.println(StringUtils.format("%s %s operation TPS is \"%d\"", provider.getEnv().getCacheId(), operation, (DEFAULT_TIMES / c.countMillisAndReset() * 1000)));
	}

	private void exec(String operation, AtomicInteger counter, byte[] data, CacheProvider provider) throws Exception {
		Cache cache = provider.getCache();
		KV kv = null;
		try {
			if (OP_SET.equals(operation)) {
				kv = new KV(String.valueOf(counter.getAndIncrement()));
				kv.setValue(data);
				cache.set(kv);
			} else if (OP_GET.equals(operation)) {
				kv = new KV(String.valueOf(counter.getAndIncrement()));
				cache.get(kv);
			} else if (OP_DELETE.equals(operation)) {
				kv = new KV(String.valueOf(counter.getAndIncrement()));
				cache.delete(kv);
			} else {
				throw new UnsupportedOperationException(operation);
			}
		} finally {
			cache.close();
		}
	}

}
