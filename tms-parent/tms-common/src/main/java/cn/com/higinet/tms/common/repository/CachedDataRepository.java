/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  CachedDataRepository.java   
 * @Package cn.com.higinet.tms.common.repository   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-20 11:27:26   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository;

import cn.com.higinet.tms.common.cache.Cache;
import cn.com.higinet.tms.common.cache.CacheManager;
import cn.com.higinet.tms.common.cache.KV;
import cn.com.higinet.tms.common.exception.NullParamterException;
import cn.com.higinet.tms.common.repository.serializer.DefaultSerializer;
import cn.com.higinet.tms.common.repository.serializer.Serializer;
import cn.com.higinet.tms.common.util.ObjectUtils;
import cn.com.higinet.tms.common.util.StringUtils;

/**
 * 基于缓存实现的数据仓
 *
 * @ClassName:  CachedDataRepository
 * @author: 王兴
 * @date:   2017-9-20 11:27:26
 * @since:  v4.3
 */
public class CachedDataRepository extends AbstractDataRepository {

	protected CacheManager cacheManager;

	protected Serializer serializer;

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public Serializer getSerializer() {
		return serializer;
	}

	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}

	/**
	 * @throws Exception 
	 * @see cn.com.higinet.tms.common.repository.DataRepository#get(java.lang.String)
	 */
	@Override
	public StructuredData get(String key) throws Exception {
		return getFromCache(key);
	}

	/**
	 * 这个方法本来是干的get的活，单独写在这里是为了防止子类在调用父类方法的时候，导致死循环
	 *
	 * @param key the key
	 * @return from cache 属性
	 * @throws Exception the exception
	 */
	protected StructuredData getFromCache(String key) throws Exception  {
		if (StringUtils.isNull(key)) {
			throw new NullParamterException("Key can not be null.");
		}
		Cache cache = null;
		try {
			cache = cacheManager.getMainProvider().getCache();
			KV kv = new KV(id, key);
			cache.get(kv);
			byte[] data = kv.getValue();
			if (data != null) {
				StructuredData res = serializer.bytesToStructure(data);
				res.setConverters(getConverters());
				res.setMetadata(getMetadata());
				return res;
			}
		} catch (Exception e) {
			log.error("Get structure data error.Key is" + key, e);
		} finally {
			if (cache != null) {
				cache.close();
			}
		}
		return null;
	}

	/**
	 * @see cn.com.higinet.tms.common.repository.DataRepository#update(cn.com.higinet.tms.common.repository.StructuredData)
	 */
	@Override
	public StructuredData save(StructuredData structure) throws Exception {
		StructuredData oldValue = getFromCache(structure.getKey());
		StructuredData newData = structure;
		if (oldValue != null) {
			//修改
			newData = ObjectUtils.clone(oldValue);
			newData.getData().putAll(structure.getData());
		}
		Cache cache = null;
		try {
			cache = cacheManager.getMainProvider().getCache();
			byte[] data = serializer.structureToBytes(newData);
			KV kv = new KV(id, newData.getKey(), data);
			cache.set(kv);
		} catch (Exception e) {
			log.error("Save structure data error.Key is" + structure.getKey(), e);
		} finally {
			if (cache != null) {
				cache.close();
			}
		}
		return oldValue;
	}

	/**
	 * @see cn.com.higinet.tms.common.repository.DataRepository#delete(java.lang.String)
	 */
	@Override
	public StructuredData delete(String key) throws Exception {
		StructuredData oldValue = get(key);
		if (oldValue == null) {
			return null;
		}
		Cache cache = null;
		try {
			cache = cacheManager.getMainProvider().getCache();
			KV kv = new KV(id, key);
			cache.delete(kv);
		} catch (Exception e) {
			log.error("Delete structure data error.Key is" + key, e);
		} finally {
			if (cache != null) {
				cache.close();
			}
		}
		return oldValue;
	}

	/**
	 * @see cn.com.higinet.tms.common.lang.Initializable#initialize()
	 */
	@Override
	public void initialize() throws Exception {
		super.initialize();
		if (serializer == null) {
			serializer = new DefaultSerializer();
		}
	}

	/**
	 * @see cn.com.higinet.tms.common.lang.Initializable#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		super.destroy();
		serializer = null;
	}
}
