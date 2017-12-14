/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  AbstractDataRepository.java   
 * @Package cn.com.higinet.tms.common.repository   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-21 14:47:26   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.common.repository.converters.Converter;

/**
 * 抽象数据仓
 *
 * @ClassName:  AbstractDataRepository
 * @author: 王兴
 * @date:   2017-9-21 14:47:26
 * @since:  v4.3
 */
public abstract class AbstractDataRepository implements DataRepository {

	/** log. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/** id. */
	protected String id;

	/** metadata. */
	protected Metadata metadata;

	/** 数据仓关联的类型转换器. */
	protected Map<String, Converter<?>> converters = new HashMap<String, Converter<?>>();

	public String getRepositoryID() {
		return id;
	}

	public void setRepositoryID(String id) {
		this.id = id;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public Map<String, Converter<?>> getConverters() {
		return converters;
	}

	public void setConverters(Map<String, Converter<?>> converters) {
		this.converters = converters;
	}

	/**
	 * 启动时候注册转换器.
	 * 没有多线程并发，不考虑同步
	 * @param converter the converter
	 * @return true, if successful
	 */
	public boolean registerConverter(String name, Converter<?> converter) {
		converters.put(name, converter);
		return true;

	}

	/**
	 * 注销转换器
	 * 没有多线程并发，不考虑同步
	 * @param converter the converter
	 * @return true, if successful
	 */
	public boolean unregisterConverter(String name) {
		converters.remove(name);
		return true;

	}

	/**
	 * @see cn.com.higinet.tms.common.repository.DataRepository#createNewData(java.lang.String)
	 */
	public StructuredData createNewData(String key) {
		StructuredData data = new StructuredData(key, metadata);
		data.setConverters(converters);
		data.setRepository(this);
		return data;
	}

	/**
	 * @see cn.com.higinet.tms.common.lang.Initializable#initialize()
	 */
	@Override
	public void initialize() throws Exception {

	}

	/**
	 * @see cn.com.higinet.tms.common.lang.Initializable#destroy()
	 */
	@Override
	public void destroy() throws Exception {
	}

}
