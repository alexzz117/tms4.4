/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms35.core.dao;

import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.cache.db_token;
import cn.com.higinet.tms35.core.concurrent.cache.tm_cache;

/**
 * 功能/模块:
 * @author zhanglq
 * @version 1.0  Dec 16, 2014
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
public class token_cache {
	static tm_cache<String, db_token> m_cache = null;
	
	static {
		// POC测试修改，去除C代码层的缓存，只保留google缓存
		int cache_size = tmsapp.get_config("tms.token.cachesize", 1000);
		//int max_memory = tmsapp.get_config("tms.token.cachememory", 100);
		if (cache_size > 0) {
			//tm_cache_token tmp = new tm_cache_token(cache_size, max_memory);
			//m_cache = new local_cache<db_token>(tmp, 10000);
			m_cache = new tm_cache<String, db_token>(cache_size);
		}
	}
	
	public static db_token read(Object... p){
		db_token u = null;
		if (m_cache != null) {
			u = m_cache.get(str_tool.to_str(p[0]));
			if (u != null)
				return u;
			else
				m_cache.putIfAbsent(str_tool.to_str(p[0]), new db_token(str_tool.to_str(p[0])));
		}
		return u;
	}
}
