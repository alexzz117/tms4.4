/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  RepositoryManager.java   
 * @Package cn.com.higinet.tms.common.repository   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-20 9:59:51   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.com.higinet.tms.common.lifecycle.Service;

/**
 * 数据仓的管理类，专门获取数据仓。
 *
 * @ClassName:  RepositoryManager
 * @author: 王兴
 * @date:   2017-9-20 9:59:51
 * @since:  v4.3
 */
public class RepositoryManager extends Service {

	private Map<String, DataRepository> repositories = new HashMap<String, DataRepository>();

	/** 数据仓的初始化器，通过注入可更改初始化方式. */
	private RepositoryInitializer initializer;

	public RepositoryInitializer getInitializer() {
		return initializer;
	}

	public void setInitializer(RepositoryInitializer initializer) {
		this.initializer = initializer;
	}

	public DataRepository getRepository(String key) throws RepositoryNotFoundException{
		DataRepository repository = repositories.get(key);
		if (repository == null) {
			throw new RepositoryNotFoundException("Can't find \"%s\" data repository,please ensure that there is a repository named \"%s\" in your configuration.", key, key);
		}
		return repository;
	}

	@Override
	protected void doStart() throws Throwable {
		initializer.initialize();
		List<DataRepository> _repositories = initializer.initRepositories();
		if (_repositories != null && !_repositories.isEmpty()) {
			for (DataRepository repository : _repositories) {
				repository.initialize();
				repositories.put(repository.getRepositoryID(), repository);
			}
		}
	}

	@Override
	protected void doStop() throws Throwable {
		Collection<DataRepository> drs = repositories.values();
		for (DataRepository dr : drs) {
			try {
				dr.destroy();
			} catch (Exception e) {
				logger.error("Exception accurred when destroy data repository \"{}\"", dr.getRepositoryID());
			}
		}
		repositories.clear();
		initializer.destroy();
	}

}
