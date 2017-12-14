package cn.com.higinet.tms.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import cn.com.higinet.tms.common.cache.Cache;
import cn.com.higinet.tms.common.cache.CacheManager;
import cn.com.higinet.tms.common.cache.CacheProvider;
import cn.com.higinet.tms.common.cache.KV;
import cn.com.higinet.tms.common.util.ClockUtils;
import cn.com.higinet.tms.common.util.ClockUtils.Clock;

public class CacheTest {
	private static CacheManager cm = new CacheManager();

	static {
		cm.start();
	}

	@Test
	public void redisSave() throws Exception {
		CacheProvider provider = cm.getProvider("redis");
		Cache cache = provider.getCache();
		List<KV> kvs = new ArrayList<KV>();
		kvs.add(new KV("test", "a", "1"));
		kvs.add(new KV("test", "b", "2"));
		kvs.add(new KV("test", "c", "3"));
		cache.set(kvs);
	}

	@Test
	public void redisExists() throws Exception {
		CacheProvider provider = cm.getProvider("redis");
		Cache cache = provider.getCache();
		List<KV> kvs = new ArrayList<KV>();
		kvs.add(new KV("test", "a"));
		kvs.add(new KV("test", "b"));
		kvs.add(new KV("test", "c"));
		kvs.add(new KV("test", "d"));
		Map<KV, Boolean> res = cache.exists(kvs);
		System.out.println(res);
	}

	@Test
	public void asSave() throws Exception {
		CacheProvider provider = cm.getProvider("aerospike");
		Cache cache = provider.getCache();
		List<KV> kvs = new ArrayList<KV>();
		kvs.add(new KV("test", "a", "1"));
		kvs.add(new KV("test", "b", "2"));
		kvs.add(new KV("test", "c", "3"));
		cache.set(kvs);
	}

	@Test
	public void asExists() throws Exception {
		CacheProvider provider = cm.getProvider("aerospike");
		Cache cache = provider.getCache();
		List<KV> kvs = new ArrayList<KV>();
		kvs.add(new KV("test", "a"));
		kvs.add(new KV("test", "b"));
		kvs.add(new KV("test", "c"));
		kvs.add(new KV("test", "d"));
		Map<KV, Boolean> res = cache.exists(kvs);
		System.out.println(res);
	}

	@Test
	public void asDeleteAll() throws Exception {
		CacheProvider provider = cm.getProvider("aerospike");
		Cache cache = provider.getCache();
		cache.deleteAll();
	}
}
