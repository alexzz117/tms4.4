/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  YAMLInitializer.java   
 * @Package cn.com.higinet.tms.common.repository.initializer   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-20 11:20:09   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository.initializer;

import java.util.List;

import cn.com.higinet.tms.common.repository.DataRepository;
import cn.com.higinet.tms.common.repository.RepositoryInitializer;

/**
 * 从YAML文件中初始化数据仓
 *
 * @ClassName:  YAMLInitializer
 * @author: 王兴
 * @date:   2017-9-20 11:20:09
 * @since:  v4.3
 */
public class YAMLInitializer implements RepositoryInitializer{

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.repository.RepositoryInitializer#initRepositories()
	 */
	@Override
	public List<DataRepository> initRepositories() {
		return null;
	}

	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
