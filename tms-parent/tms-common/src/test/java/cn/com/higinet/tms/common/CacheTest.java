/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  CacheTest.java   
 * @Package cn.com.higinet.tms.common   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-7-26 12:32:03   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CacheTest extends BaseConcurrentTest {
	private static CacheManager cm = new CacheManager();

	private static final String AEROSPIKE_SET_NAME = "main";

	static {
		cm.setEnvInitializer(new ConfigInitializer());
		cm.start();
	}
	@Test
	public void n(){
		System.out.println(StringUtils.rightPad("5", 4, " "));
	}
	/**
	 * 注意：EHcache开启了磁盘持久化。
	 * Ehcache 写入测试，分别计算写入磁盘和更新内存的速度.
	 * 在第一次磁盘没有数据的时候写入，这属于新增操作，速度比更新快。
	 * 在第二次更新的时候，如果服务是从已有文件中加载，那么会先从磁盘加载到内存，再更新，
	 * 如果已经加载过，那么直接更新内存后，写入磁盘。
	 *
	 * @throws Throwable the throwable
	 */
	@Test
	public void ehcacheSetTest() throws Throwable {
		CacheProvider provider = cm.getProvider("ehcache");
		Cache cache = provider.getCache();
		byte[] data = getData(DEFAULT_DATA_SIZE);
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			KV kv = new KV(String.valueOf(i));
			kv.setValue(data);
			cache.set(kv);
		}
		System.out.println("ehcache set to disk:" + (DEFAULT_TIMES / c.countMillisAndReset() * 1000));
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			KV kv = new KV(String.valueOf(i));
			kv.setValue(data);
			cache.set(kv);
		}
		System.out.println("ehcache set to memory:" + (DEFAULT_TIMES / c.countMillis() * 1000));
		cache.close();
	}

	@Test
	public void ehcacheBatchSetTest() throws Throwable {
		CacheProvider provider = cm.getProvider("ehcache");
		Cache cache = provider.getCache();
		byte[] data = getData(DEFAULT_DATA_SIZE);
		Clock c = ClockUtils.createClock();
		List<KV> kvs = new ArrayList<KV>();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			KV kv = new KV(String.valueOf(i));
			kv.setValue(data);
			kvs.add(kv);
		}
		cache.set(kvs);
		System.out.println("ehcache batch set:" + (DEFAULT_TIMES / c.countMillisAndReset() * 1000));
		cache.set(kvs);
		System.out.println("ehcache batch set to memory:" + (DEFAULT_TIMES / c.countMillis() * 1000));
		cache.close();
	}

	/**
	 * 注意：EHcache开启了磁盘持久化。
	 * Ehcache读测试.
	 * 第一次从磁盘加载
	 * 第二次从内存读。
	 * @throws Throwable the throwable
	 */
	@Test
	public void ehcacheGetTest() throws Throwable {
		CacheProvider provider = cm.getProvider("ehcache");
		Cache cache = provider.getCache();
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			KV kv = new KV(String.valueOf(i));
			cache.get(kv);
		}
		System.out.println("ehcache get from disk:" + (DEFAULT_TIMES / c.countMillisAndReset() * 1000));
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			KV kv = new KV(String.valueOf(i));
			cache.get(kv);
		}
		System.out.println("ehcache get from memory:" + (DEFAULT_TIMES / c.countMillis() * 1000));
		cache.close();
	}

	@Test
	public void ehcacheBatchGetTest() throws Throwable {
		CacheProvider provider = cm.getProvider("ehcache");
		Cache cache = provider.getCache();
		Clock c = ClockUtils.createClock();
		List<KV> kvs = new ArrayList<KV>();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			KV kv = new KV(String.valueOf(i));
			kvs.add(kv);
		}
		cache.get(kvs);
		System.out.println("ehcache batch get:" + (DEFAULT_TIMES / c.countMillisAndReset() * 1000));
		cache.close();
	}

	@Test
	public void redisSetListTest() throws Throwable {
		CacheProvider provider = cm.getProvider("redis");
		Cache cache = provider.getCache();
		byte[] data = getData(DEFAULT_DATA_SIZE);
		Clock c = ClockUtils.createClock();
		List<KV> kvs = new ArrayList<KV>();
		for (int i = 0; i < DEFAULT_TIMES; i++) {//4s
			KV kv = new KV(String.valueOf(i));
			kv.setValue(data);
			kvs.add(kv);
		}
		cache.set(kvs);
		System.out.println("redis set by channel:" + (DEFAULT_TIMES / c.countMillisAndReset() * 1000));
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			KV kv = new KV(String.valueOf(i));
			kv.setValue(data);
			cache.set(kv);
		}
		System.out.println("redis set by single:" + (DEFAULT_TIMES / c.countMillis() * 1000));
		cache.close();
	}

	@Test
	public void redisGetListTest() throws Throwable {
		CacheProvider provider = cm.getProvider("redis");
		Cache cache = provider.getCache();
		Clock c = ClockUtils.createClock();
		List<KV> kvs = new ArrayList<KV>();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			KV kv = new KV(String.valueOf(i));
			kvs.add(kv);
		}
		kvs = cache.get(kvs);
		System.out.println("redis get by channel:" + (DEFAULT_TIMES / c.countMillisAndReset() * 1000));
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			KV kv = new KV(String.valueOf(i));
			cache.get(kv);
		}
		System.out.println("redis get by single:" + (DEFAULT_TIMES / c.countMillisAndReset() * 1000));
		cache.close();
	}

	@Test
	public void redisDeleteListTest() throws Throwable {
		CacheProvider provider = cm.getProvider("redis");
		Cache cache = provider.getCache();
		Clock c = ClockUtils.createClock();
		List<KV> kvs = new ArrayList<KV>();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			KV kv = new KV(String.valueOf(i));
			kvs.add(kv);
		}
		cache.delete(kvs);
		System.out.println("redis delete by channel:" + (DEFAULT_TIMES / c.countMillisAndReset() * 1000));
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			KV kv = new KV(String.valueOf(i));
			cache.delete(kv);
		}
		System.out.println("redis delete by single:" + (DEFAULT_TIMES / c.countMillisAndReset() * 1000));
		cache.close();
	}

	/**
	 * 
	 * aerospike 写入测试 
	 */
	@Test
	public void aerospikeSetTest() throws Throwable {
		CacheProvider provider = cm.getProvider("aerospike");
		Cache cache = provider.getCache();
		byte[] data = getData(DEFAULT_DATA_SIZE);
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			KV kv = new KV(AEROSPIKE_SET_NAME, String.valueOf(i), data);
			cache.set(kv);
		}
		System.out.println("aerospike  set:" + (DEFAULT_TIMES / c.countMillisAndReset() * 1000));
		cache.close();
	}

	/**
	 * 
	 * aerospike 读测试 
	 */
	@Test
	public void aerospikeGetTest() throws Throwable {
		CacheProvider provider = cm.getProvider("aerospike");
		Cache cache = provider.getCache();
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			KV kv = new KV(AEROSPIKE_SET_NAME, String.valueOf(i));
			cache.get(kv);
		}
		System.out.println("aerospike get:" + (DEFAULT_TIMES / c.countMillisAndReset() * 1000));
		cache.close();
	}

	/**
	 * 
	 * aerospike 批量读测试 
	 */
	@Test
	public void aerospikeBathGetTest() throws Throwable {
		CacheProvider provider = cm.getProvider("aerospike");
		Cache cache = provider.getCache();
		Clock c = ClockUtils.createClock();
		List<KV> list = new ArrayList<KV>();
		for (int i = 0; i < 100; i++) {
			for (int x = 0; x < 100; x++) {
				KV kv = new KV(AEROSPIKE_SET_NAME, String.valueOf(x));
				list.add(kv);
			}
			cache.get(list);
		}
		System.out.println("aerospike batch get :" + (DEFAULT_TIMES / c.countMillisAndReset() * 1000));
		cache.close();
	}

	/**
	 * 
	 * aerospike 删除测试 
	 */
	@Test
	public void aerospikeDeleteTest() throws Throwable {
		CacheProvider provider = cm.getProvider("aerospike");
		Cache cache = provider.getCache();
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			KV kv = new KV(AEROSPIKE_SET_NAME, String.valueOf(i));
			cache.delete(kv);
		}
		System.out.println("aerospike  del:" + (DEFAULT_TIMES / c.countMillisAndReset() * 1000));
		cache.close();
	}

}
