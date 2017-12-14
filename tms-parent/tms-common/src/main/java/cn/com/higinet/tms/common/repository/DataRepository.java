/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  DataRepository.java   
 * @Package cn.com.higinet.tms.common.repository   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-20 9:58:12   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository;

import cn.com.higinet.tms.common.lang.Initializable;
import cn.com.higinet.tms.common.repository.converters.Converter;

/**
 * 数据仓库，提供结构化数据的获取、更新、删除。
 * 支持批量操作。
 *
 * @ClassName:  DataRepository
 * @author: 王兴
 * @date:   2017-9-20 9:58:12
 * @since:  v4.3
 */
public interface DataRepository extends Initializable {

	/**
	 * 本数据仓的唯一标识
	 *
	 * @return ID 属性
	 */
	public String getRepositoryID();

	/**
	 * 此数据仓的元数据结构
	 *
	 * @return meta 属性
	 */
	public Metadata getMetadata();

	/**
	 * 构造一个新的结构化对象
	 *
	 * @return the structured data
	 */
	public StructuredData createNewData(String key);

	/**
	 * 注册转换器.
	 *
	 * @param converter the converter
	 * @return true, if successful
	 */
	public boolean registerConverter(String name, Converter<?> converter);

	/**
	 * 注销转换器
	 *
	 * @param converter the converter
	 * @return true, if successful
	 */
	public boolean unregisterConverter(String name);

	/**
	 * 获取指定key的元数据对象
	 *
	 * @param Key the key
	 * @return data 属性
	 */
	public StructuredData get(String key) throws Exception; //key的类型采用string类型，这是内部类型

	/**
	 * 更新元数据，返回old data。
	 *
	 * @param newData the new data
	 * @return the structured data
	 */
	public StructuredData save(StructuredData structure) throws Exception;

	/**
	 * 删除数据，返回被删除的结构对象.
	 *
	 * @param Key the key
	 * @return the structured data
	 */
	public StructuredData delete(String key) throws Exception;

}
