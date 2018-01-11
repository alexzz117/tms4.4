package cn.com.higinet.tms35.stat.serv;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.common.cache.Cache;
import cn.com.higinet.tms.common.cache.CacheManager;
import cn.com.higinet.tms.common.cache.CacheProvider;
import cn.com.higinet.tms.common.cache.KV;
import cn.com.higinet.tms35.comm.hash;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.bean;

public class filter_log {
	private static CacheManager cacheManager = (CacheManager) bean.get("CacheManager");

	private static CacheProvider cacheProvider = cacheManager.getMainProvider();

	public static Logger log = LoggerFactory.getLogger(filter_log.class);

	private static int thread_cnt = tmsapp.get_config("tms.stat.eval.workerCount", 2);

	public static Set<String> m_key_filter = new HashSet<String>();

	public static Set<Integer> m_thread_filter = new HashSet<Integer>();

	static public void add_thread_filter(String key) {
		Set<Integer> thread_filter = new HashSet<Integer>(m_thread_filter);
		thread_filter.add(req_hash(key));
		m_thread_filter = thread_filter;
	}

	static public void del_thread_filter(String key) {
		Set<Integer> thread_filter = new HashSet<Integer>(m_thread_filter);
		if (thread_filter.remove(req_hash(key)))
			m_thread_filter = thread_filter;
	}

	static public void clr_thread_filter() {
		m_thread_filter = new HashSet<Integer>();
	}

	static public void add_key_filter(String key) {
		Set<String> key_filter = new HashSet<String>(m_key_filter);
		key_filter.add(key);
		m_key_filter = key_filter;
	}

	static public void del_key_filter(String key) {
		Set<String> key_filter = new HashSet<String>(m_key_filter);
		if (key_filter.remove(key))
			m_key_filter = key_filter;
	}

	static public void clr_key_filter() {
		m_key_filter = new HashSet<String>();
	}

	static public int get_thread_id() {
		try {
			String name = Thread.currentThread().getName();
			if (name == null) {
				return -1;
			}
			int p = name.lastIndexOf("-", 10000);
			if (p == -1)
				return -1;
			int id = Integer.parseInt(name.substring(p + 1));
			return id;
		} catch (Exception e) {
			//ignored
		}
		return -1;
	}

	static private int req_hash(String param) {
		return hash.clac(param, 134217943) % 2087 % thread_cnt;
	}

	static public boolean filter_thread() {
		return m_thread_filter.contains(get_thread_id());
	}

	static public boolean filter(String key) {
		if (key == null)
			return false;

		if (!m_thread_filter.isEmpty()) {
			if (m_key_filter.isEmpty())
				return m_thread_filter.contains(get_thread_id());
			else
				return m_key_filter.contains(key);
		}

		return m_key_filter.contains(key);
	}

	public static void info(String s) {
		if (!filter_thread())
			return;
		StackTraceElement stack = Thread.currentThread().getStackTrace()[2];
		log.info(String.format("thread=%d %s(%s:%d) %s", get_thread_id(), stack.getMethodName(), stack.getFileName(), stack.getLineNumber(), s));
	}

	static public void info(String key, String addinfo) {
		try {
			if (!filter(key))
				return;

			StackTraceElement stack = Thread.currentThread().getStackTrace()[2];
			Cache cache = cacheProvider.getCache();
			try {
				log.info(String.format("thread=%d %s(%s:%d) %s %s:%s", get_thread_id(), stack.getMethodName(), stack.getFileName(), stack.getLineNumber(), addinfo, key, cache.get(new KV("stat", key))));
			} finally {
				cache.close();
			}
		} catch (Throwable e) {
		}
	}

	static public void main(String[] a) {
		filter_log.add_key_filter("user01-");
		filter_log.info("user01-", "完成缓存保存");
		filter_log.info("user02-", "开始数据库存储");
	}
}
