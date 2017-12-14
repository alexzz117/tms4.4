/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  EnvInitializer.java   
 * @Package cn.com.higinet.tms.common.cache   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-13 15:41:07   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.cache;

import java.util.List;

import cn.com.higinet.tms.common.lang.Initializable;

/**
 * 用于初始化缓存上下文的接口
 *
 * @ClassName:  EnvInitializer
 * @author: 王兴
 * @date:   2017-9-13 15:41:07
 * @since:  v4.3
 */
public interface EnvInitializer extends Initializable {

	/**
	 * 根据实现类自定义的方式去初始化CacheEnv对象
	 *
	 * @return the list
	 */
	public List<CacheEnv> initCacheEnv() throws Exception;
}
