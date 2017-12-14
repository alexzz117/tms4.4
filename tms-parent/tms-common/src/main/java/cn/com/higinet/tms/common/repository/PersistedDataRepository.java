/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  PersistedDataRepository.java   
 * @Package cn.com.higinet.tms.common.repository   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-20 11:28:29   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository;

import cn.com.higinet.tms.common.repository.persistence.Persistence;

/**
 * 持久化数据仓，数据除了进缓存外，还会进入持久化
 *
 * @ClassName:  PersistedDataRepository
 * @author: 王兴
 * @date:   2017-9-20 11:28:29
 * @since:  v4.3
 */
public class PersistedDataRepository extends CachedDataRepository {

	private Persistence persistence;

	public void setPersistence(Persistence persistence) {
		this.persistence = persistence;
	}

	@Override
	public StructuredData get(String key) throws Exception {
		StructuredData data = getFromCache(key);
		if (data == null) {
			//如果没有，就从持久化里面取
			data = persistence.get(createNewData(key));
			if (data == null || data.getData() == null || data.getData().isEmpty())
				return null;
			super.save(data);//保存到缓存
		}
		return data;
	}

	/**
	 * 先忘cache中保存，如果之前有值，就update，没有值就insert，一切操作以cache为主
	 * @see cn.com.higinet.tms.common.repository.DataRepository#update(cn.com.higinet.tms.common.repository.StructuredData)
	 */
	@Override
	public StructuredData save(StructuredData structure) throws Exception {
		StructuredData oldValue = super.save(structure);
		if (oldValue == null) {
			persistence.insert(structure);
		} else {
			persistence.update(structure);
		}
		return oldValue;
	}

	/**
	 * @see cn.com.higinet.tms.common.repository.DataRepository#delete(java.lang.String)
	 */
	@Override
	public StructuredData delete(String Key) throws Exception {
		StructuredData oldValue = super.delete(Key);
		if (oldValue != null) {
			persistence.delete(oldValue);
		}
		return oldValue;
	}

	/**
	 * @see cn.com.higinet.tms.common.lang.Initializable#initialize()
	 */
	@Override
	public void initialize() throws Exception {
		super.initialize();
		persistence.initialize();
	}

	/**
	 * @see cn.com.higinet.tms.common.lang.Initializable#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		super.destroy();
		persistence.destroy();
	}
}
