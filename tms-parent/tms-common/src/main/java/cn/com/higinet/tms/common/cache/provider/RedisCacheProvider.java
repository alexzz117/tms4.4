/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  RedisCacheProvider.java   
 * @Package cn.com.higinet.tms.common.cache.provider   
 * @Description: (redis客户端初始化以及赋值客户端给RedisAdaper调用)<br/>
 * @author: 刘晓春
 * @date:   2017-7-25 18:20:07   
 * @version V4.3 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.cache.provider;

import cn.com.higinet.tms.common.cache.Cache;
import cn.com.higinet.tms.common.cache.CacheEnv;
import cn.com.higinet.tms.common.cache.adapter.RedisCacheAdaper;
import cn.com.higinet.tms.common.exception.BaseRuntimeException;
import cn.com.higinet.tms.common.exception.NullParamterException;
import cn.com.higinet.tms.common.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis客户端初始化以及将生成的jedis赋值给RedisCacheAdaper调用，方便RedisCacheAdaper对redis存储的数据进行增删改查
 * {@code
 * 		jedisPool redis连接池
 * 		DEFAUL_TIMEOUT 连接超时时间默认值
 * 		DEFAUL_MAX_TOTAL 默认最大连接数500
 * 		DEFAUL_MAX_IDLE 默认最小空闲连接数100
 * 		DEFAUL_DB_INDEX 默认redis操作的数据库编号为0
 * 		DEFAUL_MAX_WAIT_MILLIS 默认获取连接时的最大等待毫秒数3000
 * 		DEFAUL_TEST_ON_BORROW 取连接之前，默认需要测试连接是否可用
 * 		DEFAUL_TEST_ON_RETURN 返回连接时，默认需要测试连接是否可用
 * 		REDIS_CACHE_ID 对应server.properties文件中的配置cacheId名称
 * 		REDIS_URL 对应server.properties文件中的配置redis的ip和端口名称
 * 		REDIS_AUTH 对应server.properties文件中的配置redis的auth名称
 * 		REDIS_MAX_TOTAL 对应server.properties文件中的配置redis的最大连接数名称
 * 		REDIS_TIMEOUT 对应server.properties文件中的配置redis的连接超时名称
 * 		REDIS_MAX_IDLE 对应server.properties文件中的配置redis的最小空闲连接数名称
 * 		REDIS_DB_INDEX 对应server.properties文件中的配置redis的数据库编号名称
 * 		REDIS_MAX_WAIT_MILLS 对应server.properties文件中的配置redis的获取连接时的最大等待毫秒数名称
 * 		REDIS_TEST_ON_BORROW 对应server.properties文件中的配置redis的取连接之前，是否需要测试连接名称
 * 		REDIS_TEST_ON_RETURN 对应server.properties文件中的配置redis的取返回连接时，是否需要测试连接可用名称
 * } <br/>
 * 1.在调用redis缓存的时候是通过{@link cn.com.higinet.tms.common.cache.CacheManager}进入
 * 2.创建jedis客户端后，使用{@link cn.com.higinet.tms.common.cache.adapter.RedisCacheAdaper}调用jedis客户端进行相应的处理
 * 
 * @ClassName: RedisCacheProvider
 * @author: 刘晓春
 * @date: 2017-7-26 9:53:51
 * @since: v4.3
 */
public class RedisCacheProvider extends AbstractCacheProvider {

	/** jedis pool. */
	private JedisPool jedisPool = null;

	/** 默认超时时间1000. */
	private static final int DEFAUL_TIMEOUT = 1000;

	/** 默认最大连接数500. */
	private static final int DEFAUL_MAX_TOTAL = 500;

	/** 最小空闲连接数100. */
	private static final int DEFAUL_MAX_IDLE = 100;
	
	/** 默认查询的数据库 */
	private static final int DEFAUL_DB_INDEX = 0;

	/** 获取连接时的最大等待毫秒数3000. */
	private static final int DEFAUL_MAX_WAIT_MILLIS = 3000;

	/** 取连接之前，默认需要测试连接是否可用. */
	private static final boolean DEFAUL_TEST_ON_BORROW = true;

	/** 返回连接时，默认需要测试连接是否可用. */
	private static final boolean DEFAUL_TEST_ON_RETURN = true;

	/** URL 配置 例如192.168.10.189:6379 */
	private static final String REDIS_URL = "url";

	/** 密码默认不设置. */
	private static final String REDIS_AUTH = "auth";

	/** 最大连接数, 默认8个. */
	private static final String REDIS_MAX_TOTAL = "maxTotal";

	/** 连接超时时间. */
	private static final String REDIS_TIMEOUT = "timeout";

	/** 最小空闲连接数, 默认0. */
	private static final String REDIS_MAX_IDLE = "maxIdle";
	
	/** 默认查询的数据 */
	private static final String REDIS_DB_INDEX = "dbIndex";

	/**
	 * 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间, 默认-1.
	 */
	public static final String REDIS_MAX_WAIT_MILLS = "maxWaitMills";

	/** 获取连接之前，是否测试连接可用，默认是false。网络不稳定的情况，可以采用true,如果测试不通过，从pool中移除，并再次执行获取连接。. */
	public static final String REDIS_TEST_ON_BORROW = "testOnBorrow";

	/** 返回连接时，是否测试连接可用，默认是false。. */
	public static final String REDIS_TEST_ON_RETURN = "testOnReturn";

	/**
	 * 同步获取Jedis实例
	 * 
	 * @return Jedis
	 */
	public Jedis getJedis() {
		Jedis jedis = null;
		try {
			if (jedisPool != null) {
				jedis = jedisPool.getResource();
			} else {
				throw new NullParamterException("JedisPool cannot be null");
			}
		} catch (Exception e) {
			throw new BaseRuntimeException("From jedisPool get jedis anomaly", e);
		}
		return jedis;
	}

	/**
	 * 将jedis缓存客户端设置给RedisCacheAdaper里面的jedis成员变量
	 * @return RedisCacheAdaper
	 */
	@Override
	public Cache getCache() throws Exception {
		RedisCacheAdaper redisAdapter = new RedisCacheAdaper();
		redisAdapter.setJedis(getJedis());
		return redisAdapter;
	}

	/*
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStart()
	 */
	@Override
	protected void doStart() throws Throwable {
		if (jedisPool == null) {
			CacheEnv env = getEnv();
			if (env == null) {
				throw new NullParamterException("Env is equal to null");
			}
			if (StringUtils.isNull(env.getString(REDIS_URL, null))) {
				throw new NullParamterException("No configuration IP and port configuration file");
			}
			try {
				JedisPoolConfig config = new JedisPoolConfig();
				config.setMaxTotal(env.getInteger(REDIS_MAX_TOTAL, DEFAUL_MAX_TOTAL));
				config.setMaxIdle(env.getInteger(REDIS_MAX_IDLE, DEFAUL_MAX_IDLE));
				config.setMaxWaitMillis(env.getInteger(REDIS_MAX_WAIT_MILLS, DEFAUL_MAX_WAIT_MILLIS));
				config.setTestOnBorrow(env.getBoolean(REDIS_TEST_ON_BORROW, DEFAUL_TEST_ON_BORROW));
				config.setTestOnReturn(env.getBoolean(REDIS_TEST_ON_RETURN, DEFAUL_TEST_ON_RETURN));
				jedisPool = new JedisPool(config, env.getString(REDIS_URL, null).split(":")[0],
						Integer.valueOf(env.getString(REDIS_URL, null).split(":")[1]),
						env.getInteger(REDIS_TIMEOUT, DEFAUL_TIMEOUT), env.getString(REDIS_AUTH, null),
						env.getInteger(REDIS_DB_INDEX, DEFAUL_DB_INDEX));
			} catch (Exception e) {
				throw new BaseRuntimeException("Create a jedisPool anomaly", e);
			}
		}
	}

	/*
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStop()
	 */
	@Override
	protected void doStop() throws Throwable {
		if (jedisPool != null) {
			jedisPool.destroy();
			jedisPool = null;
		}
	}

}
