/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  ConfigInitializer.java   
 * @Package cn.com.higinet.tms.common.cache.initializer   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-13 15:44:16   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.cache.initializer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import cn.com.higinet.tms.common.cache.CacheEnv;
import cn.com.higinet.tms.common.cache.CacheManager;
import cn.com.higinet.tms.common.cache.EnvInitializer;
import cn.com.higinet.tms.common.util.StringUtils;

/**
 * 根据指定配置文件去初始化cache上下文的类
 *
 * @ClassName:  ConfigInitializer
 * @author: 王兴
 * @date:   2017-9-13 15:44:19
 * @since:  v4.3
 */
public class ConfigInitializer implements EnvInitializer {

	/** CacheManager加载的cache配置，默认配置文件，基于classpath的根目录的路径. */
	private static final String DEFAULT_CONFIG = "/server.properties";

	/** 在cache配置中，缓存相关配置都是以指定前缀开头的，此值为默认前缀，当注入的CacheManager没有设置配置前缀的时候，采用此值. */
	private static final String DEFAULT_PREFIX = "tms.svc.cache.";

	/** 缓存配置文件路径，默认是server.properties，此属性是注入进来的，仅支持properties文件配置. */
	private String configPath;

	/** 缓存配置的前缀，也是通过注入来配置，建议默认"tms.svc.cache."开头，尽量避免与其他参数冲突，注意前缀结尾的小数点要加上. */
	private String cachePrefix;

	public String getCachePrefix() {
		return cachePrefix;
	}

	public void setCachePrefix(String cachePrefix) {
		this.cachePrefix = cachePrefix;
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	/**
	 * @throws Exception 
	 * @see cn.com.higinet.tms.common.cache.EnvInitializer#initCacheEnv()
	 */
	@Override
	public List<CacheEnv> initCacheEnv() throws Exception {
		List<CacheEnv> evns = new ArrayList<CacheEnv>();
		InputStream in = null;
		Properties p = new Properties();
		try {
			in = CacheManager.class.getResourceAsStream(configPath); // 基于CacheManager的类加载器classpath根目录开始的路径
			p.load(in);
		} catch (Exception e) {
			throw e;
		} finally {
			if (in != null) {
				in.close();
			}
		}
		if (!p.isEmpty()) {
			Map<String, Map<String, Object>> cacheConfigs = new HashMap<String, Map<String, Object>>();
			Set<Entry<Object, Object>> entries = p.entrySet();
			if (StringUtils.isNull(cachePrefix)) {
				cachePrefix = DEFAULT_PREFIX;
			}
			int prefixLength = cachePrefix.length();
			String key = null;
			// 将set中的cache相关的配置过滤出来
			for (Entry<Object, Object> entry : entries) {
				key = entry.getKey().toString();
				if (key.startsWith(cachePrefix)) {
					String cacheId = key.substring(prefixLength, key.indexOf(".", prefixLength));
					Map<String, Object> config = cacheConfigs.get(cacheId);
					if (config == null) {
						config = new HashMap<String, Object>();
						cacheConfigs.put(cacheId, config);
					}
					String attributeName = key.substring(prefixLength + cacheId.length() + 1);// +1是将cacheid后的小数点去掉
					Object value = entry.getValue();
					config.put(attributeName, value);
				}
			}
			if (!cacheConfigs.isEmpty()) {
				// 有缓存相关配置时候，遍历配置，生成CacheEnv对象和对应的CacheProvider
				Set<Entry<String, Map<String, Object>>> _entries = cacheConfigs.entrySet();
				String cacheId = null;
				for (Entry<String, Map<String, Object>> entry : _entries) {
					cacheId = entry.getKey();
					CacheEnv env = new CacheEnv(cachePrefix, cacheId, entry.getValue());
					evns.add(env);
				}
			}
		}
		return evns;
	}

	@Override
	public void initialize() throws Exception {
		if (StringUtils.isNull(configPath)) {
			configPath = DEFAULT_CONFIG;
		}
	}

	@Override
	public void destroy() {
	}

}
