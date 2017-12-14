/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  PrivilegedSetTccl.java   
 * @Package cn.com.higinet.tms.common.executor.impl   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-8 18:17:44   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.executor;

import java.security.PrivilegedAction;

/**
 * 类访问安全控制相关类，目前用于线程工厂{@link cn.com.higinet.tms.common.executor.TaskThreadFactory}。
 *
 * @author: 王兴
 * @date:   2017-5-8 18:17:44
 * @since:  v4.3
 */
public class PrivilegedSetTccl implements PrivilegedAction<Void> {

	/** cl. */
	private ClassLoader cl;

	/**
	 * 构造一个新的对象.
	 *
	 * @param cl the cl
	 */
	public PrivilegedSetTccl(ClassLoader cl) {
		this.cl = cl;
	}

	/**
	 * @see java.security.PrivilegedAction#run()
	 */
	@Override
	public Void run() {
		Thread.currentThread().setContextClassLoader(cl);
		return null;
	}
}