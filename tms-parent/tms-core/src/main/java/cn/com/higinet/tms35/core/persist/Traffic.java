/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  Traffic.java   
 * @Package cn.com.higinet.tms35.core.persist   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-4 17:50:52   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms35.core.persist;

import java.util.Map;

import cn.com.higinet.tms.common.lang.Initializable;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.run.run_txn_values;

/**
 * 交易流水的持久化接口定义
 *
 * @ClassName:  Traffic
 * @author: 王兴
 * @date:   2017-9-4 17:50:55
 * @since:  v4.3
 */
public interface Traffic extends Initializable {

	/**
	 * 先从缓存中查询交易流水，没有再去数据库中查询
	 *
	 * @param key the key
	 * @param env the env
	 * @return by id 属性
	 */
	public run_txn_values getById(String key, run_env env) throws Exception;

	/**
	 * 从缓存中查询交易流水
	 *
	 * @param key the key
	 * @param env the env
	 * @return cache by id 属性
	 * @throws Exception the exception
	 */
	public run_txn_values getCacheById(String key, run_env env) throws Exception;

	/**
	 * 保存交易流水数据
	 * 先存入缓存，再写入数据库中，此处增加一个交易流水持久化队列，同commit队列。
	 *
	 * @param key the key
	 * @param values the values
	 * @param filter 过滤器，用于判断是否可以存储本条数据
	 * @return 交易流水在缓存中的oldValue
	 */
	public run_txn_values save(run_txn_values value, Filter<run_txn_values> filter) throws Exception;

	/**
	 *更新缓存，返回之前的值
	 *
	 * @param newValue the new value
	 * @return the run txn values
	 */
	public void setInDB(run_txn_values newValue) throws Exception;

	/**
	 * 将run_txn_values转换成map
	 *
	 * @param rf the rf
	 * @return the map
	 */
	public Map<String, Object> list2map(run_txn_values rf);
}
