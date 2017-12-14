/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  InnerExecutor.java   
 * @Package cn.com.higinet.tms.common.executor   
 * @Description: 内部线程池接口 
 * @author: 王兴
 * @date:   2017-5-9 10:54:39   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.executor;

import java.util.concurrent.TimeUnit;

/**
 * 定义的一个内部线程池接口类，支持提交任务时候设置等待时间，超过这个等待时间，将会抛出RejectedExecutionException异常
 *
 * @author: 王兴
 * @date:   2017-5-9 10:54:39
 * @since:  v4.3
 */
public interface InnerExecutor {
	/**
	 * Executes the given command at some time in the future.  The command
	 * may execute in a new thread, in a pooled thread, or in the calling
	 * thread, at the discretion of the <tt>Executor</tt> implementation.
	 * If no threads are available, it will be added to the work queue.
	 * If the work queue is full, the system will wait for the specified
	 * time until it throws a RejectedExecutionException
	 *
	 * @param command the runnable task
	 * @throws java.util.concurrent.RejectedExecutionException if this task
	 * cannot be accepted for execution - the queue is full
	 * @throws NullPointerException if command or unit is null
	*/
	void execute(Runnable command, long timeout, TimeUnit unit);
}
