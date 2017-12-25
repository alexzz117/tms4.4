/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  CacheTrafficImpl.java   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-8-26 10:59:59   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.engine.core.persist.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.dao.dao_trafficdata_read;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.persist.Filter;
import cn.com.higinet.tms.engine.run.run_env;
import cn.com.higinet.tms.engine.run.run_txn_values;

/**
 * 交易流水的持久化类
 *
 * @ClassName:  CacheTrafficImpl
 * @author: 王兴
 * @date:   2017-8-26 10:59:59
 * @since:  v4.3
 */
public class CacheTrafficImpl {

	private static final Logger log = LoggerFactory.getLogger(CacheTrafficImpl.class);

	/** ds. */
	private data_source ds;

	/** reader. */
	private dao_trafficdata_read reader;

	/** traffic provider. */
	//private CacheProvider trafficProvider;

	/** 常量 TRANSACTION_GROUP. */
	private static final String TRANSACTION_GROUP = "trans"; //用于缓存分组

	private static final String CACHE_ID = "temp";//缓存ID

	/**
	 * @see cn.com.higinet.tms.engine.core.persist.Traffic#save(java.lang.String, cn.com.higinet.tms.engine.run.run_txn_values, cn.com.higinet.tms.engine.core.persist.Filter)
	 */
	public run_txn_values save(run_txn_values value, Filter<run_txn_values> filter) throws Exception {
		return null;
	}

	/**
	 * @see cn.com.higinet.tms.engine.core.persist.Traffic#getById(java.lang.String, cn.com.higinet.tms.engine.run.run_env)
	 */
	public run_txn_values getById(String key, run_env env) throws Exception {
		return null;
	}

	private byte[] valuesToBytes(run_txn_values values) throws IOException {
		return null;
	}

	@SuppressWarnings("unchecked")
	private run_txn_values bytesToValues(byte[] data) throws IOException {
		return null;
	}

	public void destroy() {
		if (reader != null)
			reader.close();
		ds = null;
		TrafficdCommit.commit_pool().shutdown(true);
	}

	/**
	 * 这一步操作是异步的
	 * @see cn.com.higinet.tms.engine.core.persist.Traffic#setInDB(cn.com.higinet.tms.engine.run.run_txn_values)
	 */
	public void setInDB(run_txn_values newValue) throws Exception {}

	public run_txn_values getCacheById(String key, run_env env) throws Exception {
		return null;
	}

}
