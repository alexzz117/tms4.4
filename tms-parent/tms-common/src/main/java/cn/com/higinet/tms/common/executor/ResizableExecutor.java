/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  ResizableExecutor.java   
 * @Package cn.com.higinet.tms.common.executor   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-9 11:08:15   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.executor;

import java.util.concurrent.ExecutorService;

/**
 * 可伸缩的线程池接口定义。
 *
 * @author: 王兴
 * @date:   2017-5-9 11:08:17
 * @since:  v4.3
 */
public interface ResizableExecutor extends ExecutorService {

	/**
	 * 获取当前线程池中所有线程数，包含活动和非活动。
	 *
	 * @return 当前线程池中所有线程数，包含活动和非活动
	 */
	public int getPoolSize();

	/**
	 * 返回最大线程池大小。
	 *
	 * @return 最大线程池大小
	 */
	public int getMaxThreads();

	/**
	 * 获取当前活动线程数。
	 *
	 * @return 当前活动线程数
	 */
	public int getActiveCount();

	/**
	 * 动态设置核心线程和最大线程大小。
	 *
	 * @param corePoolSize the core pool size
	 * @param maximumPoolSize the maximum pool size
	 * @return true, if successful
	 */
	public boolean resizePool(int corePoolSize, int maximumPoolSize);

	/**
	 * 动态设置等待队列大小。
	 *
	 * @param capacity the capacity
	 * @return true, if successful
	 */
	public boolean resizeQueue(int capacity);

}
