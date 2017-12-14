/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  StandardThreadExecutor.java   
 * @Package cn.com.higinet.tms.common.executor   
 * @Description: 标准线程池类   
 * @author: 王兴
 * @date:   2017-5-8 18:24:55   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.executor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cn.com.higinet.tms.common.lifecycle.Service;
import cn.com.higinet.tms.common.util.SystemUtils;

/**
 * 标准线程池类，采用装饰模式封装了TOMCAT的executor，支持自定义长度的等待队列，
 * 同时支持动态设置线程池大小，也支持task提交超时。
 * 使用方式：<br>
 * {@code 
 * StandardThreadExecutor executor=new StandardThreadExecutor();
 * //设置关键属性
 * executor.setMaxQueueSize(xxx);//设置等待队列的长度
 * executor.setMinSpareThreads(xxx);//设置核心线程数，也就是正常运行时候的线程数
 * executor.setMaxThreads(xxx); //最大线程数
 * executor.setMaxIdleTime(xxx);  //最大空闲时间
 * executor.setNamePrefix(xxx);  //设置执行线程前缀
 * executor.setThreadPriority(xxx);  //设置线程优先级
 * executor.setDaemon(xxx); //是否是守护线程，默认true
 * //还有更多的属性设置，请参考每个属性的意义
 * 
 * executor.start(); //启动线程池实例
 * 
 * //提交任务
 * executor.execute(xxx,xxx,xxx); //支持提交任务超时，防止无限等待。
 * }
 * <br>
 * 可以通过{@link #resizePool(int, int)}动态设置线程池大小。
 * 
 * @author: 王兴
 * @date:   2017-5-8 18:25:00
 * @since:  v4.3
 */
public class StandardThreadExecutor extends Service implements InnerExecutor, ResizableExecutor {

	/** 线程优先级，默认java.lang.Thread.NORM_PRIORITY */
	protected int threadPriority = Thread.NORM_PRIORITY;

	/** 是否是守护线程，默认是true。 */
	protected boolean daemon = true;

	/** 线程名前缀，这个参数是传递给线程工厂用的，默认"tms-exec-"，可以根据实际使用情况定义 */
	protected String namePrefix = "tms-exec-";

	/** 线程池最大线程数，默认Runtime.getRuntime().availableProcessors() * 2 + 1 */
	protected int maxThreads = SystemUtils.SYS_PROCESSORS * 2 + 1;

	/** 核心线程数，也就是空闲的时候，线程池需要维护的线程数，默认 Runtime.getRuntime().availableProcessors() / 2 + 1。*/
	protected int minSpareThreads = SystemUtils.SYS_PROCESSORS / 2 + 1;

	/** 最大空闲时间，当线程池中的线程数超过核心线程数minSpareThreads时候，如果有线程已经超过最大空闲时间没有接受到任务，那么这个线程会被线程池释放掉，默认300秒。 */
	protected int maxIdleTime = 300;

	/** 内部线程池对象，tomcat封装的执行线程池。 */
	protected ThreadPoolExecutor executor = null;

	/** 是否需要在线程池启动的时候就创建核心线程。 */
	protected boolean prestartminSpareThreads = false;

	/** 等待队列最大长度，默认是Integer.MAX_VALUE。 */
	protected int maxQueueSize = Integer.MAX_VALUE;

	/** 为了避免在上下文停止之后，所有的线程在同一时间段被更新，所以进行线程的延迟操作，默认是1000毫秒延迟。 */
	protected long threadRenewalDelay = 1000l;

	/** 线程的任务队列，tomcat内部封装的队列，继承了java.util.concurrent.LinkedBlockingQueue<Runnable>. */
	private TaskQueue taskqueue = null;

	/** 当线程队列满了的时候，该如何去处理提交的任务，默认是抛出异常。也可以采用CallerRunsPolicy策略，由提交线程池自己去执行，这样会不会抛弃任务，只是会降低执行速度。 */
	private RejectedExecutionHandler rejectedExecutionHandler = new AbortPolicy();//new CallerRunsPolicy();

	/**
	 * 构造一个新的对象.
	 */
	public StandardThreadExecutor() {
	}

	/**
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStart()
	 */
	@Override
	protected void doStart() throws Throwable {
		taskqueue = new TaskQueue(maxQueueSize);
		TaskThreadFactory tf = new TaskThreadFactory(namePrefix, daemon, getThreadPriority());
		executor = new ThreadPoolExecutor(getMinSpareThreads(), getMaxThreads(), maxIdleTime, TimeUnit.SECONDS, taskqueue, tf);
		executor.setThreadRenewalDelay(threadRenewalDelay);
		executor.setRejectedExecutionHandler(rejectedExecutionHandler);
		if (prestartminSpareThreads) {
			executor.prestartAllCoreThreads();
		}
		taskqueue.setParent(executor);
	}

	/**
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStop()
	 */
	@Override
	protected void doStop() throws Throwable {
		if (executor != null)
			executor.shutdownNow();
		executor = null;
		taskqueue = null;
	}

	/**
	 * @see cn.com.higinet.tms.common.executor.InnerExecutor#execute(java.lang.Runnable, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void execute(Runnable command, long timeout, TimeUnit unit) {
		if (executor != null) {
			executor.execute(command, timeout, unit);
		} else {
			throw new IllegalStateException("StandardThreadExecutor not started.");
		}
	}

	/**
	 * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
	 */
	@Override
	public void execute(Runnable command) {
		if (executor != null) {
			try {
				executor.execute(command);
			} catch (RejectedExecutionException rx) {
				//there could have been contention around the queue
				if (!((TaskQueue) executor.getQueue()).force(command))
					throw new RejectedExecutionException("Work queue full.");
			}
		} else
			throw new IllegalStateException("StandardThreadPool not started.");
	}

	//	/**
	//	 * 停止上下文，这个目前用的比较少，tomcat内部使用的
	//	 */
	//	public void contextStopping() {
	//		if (executor != null) {
	//			executor.contextStopping();
	//		}
	//	}

	public int getThreadPriority() {
		return threadPriority;
	}

	public RejectedExecutionHandler getRejectedExecutionHandler() {
		return rejectedExecutionHandler;
	}

	public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
		this.rejectedExecutionHandler = rejectedExecutionHandler;
	}

	public boolean isDaemon() {

		return daemon;
	}

	public String getNamePrefix() {
		return namePrefix;
	}

	public int getMaxIdleTime() {
		return maxIdleTime;
	}

	@Override
	public int getMaxThreads() {
		return maxThreads;
	}

	public int getMinSpareThreads() {
		return minSpareThreads;
	}

	public boolean isPrestartminSpareThreads() {

		return prestartminSpareThreads;
	}

	public void setThreadPriority(int threadPriority) {
		this.threadPriority = threadPriority;
	}

	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	public void setNamePrefix(String namePrefix) {
		this.namePrefix = namePrefix;
	}

	public void setMaxIdleTime(int maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
		if (executor != null) {
			executor.setKeepAliveTime(maxIdleTime, TimeUnit.SECONDS);
		}
	}

	public void setMaxThreads(int maxThreads) {
		maxThreads = maxThreads == 0 ? SystemUtils.SYS_PROCESSORS * 2 : maxThreads;
		this.maxThreads = maxThreads;
		if (executor != null) {
			executor.setMaximumPoolSize(maxThreads);
		}
	}

	public void setMinSpareThreads(int minSpareThreads) {
		this.minSpareThreads = minSpareThreads;
		if (executor != null) {
			executor.setCorePoolSize(minSpareThreads);
		}
	}

	public void setPrestartminSpareThreads(boolean prestartminSpareThreads) {
		this.prestartminSpareThreads = prestartminSpareThreads;
	}

	public void setMaxQueueSize(int size) {
		this.maxQueueSize = size;
	}

	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	public long getThreadRenewalDelay() {
		return threadRenewalDelay;
	}

	public void setThreadRenewalDelay(long threadRenewalDelay) {
		this.threadRenewalDelay = threadRenewalDelay;
		if (executor != null) {
			executor.setThreadRenewalDelay(threadRenewalDelay);
		}
	}

	/**
	 * @see cn.com.higinet.tms.common.executor.ResizableExecutor#getActiveCount()
	 */
	@Override
	public int getActiveCount() {
		return (executor != null) ? executor.getActiveCount() : 0;
	}

	/**
	 * 返回当前线程池对象已经完成的任务数。
	 *
	 * @return 当前线程池对象已经完成的任务数
	 */
	public long getCompletedTaskCount() {
		return (executor != null) ? executor.getCompletedTaskCount() : 0;
	}

	/**
	 * 返回线程池核心线程数。
	 *
	 * @return 线程池核心线程数
	 */
	public int getCorePoolSize() {
		return (executor != null) ? executor.getCorePoolSize() : 0;
	}

	/**
	 * 返回线程池最大池大小。
	 *
	 * @return 线程池最大池大小
	 */
	public int getLargestPoolSize() {
		return (executor != null) ? executor.getLargestPoolSize() : 0;
	}

	/**
	 * @see cn.com.higinet.tms.common.executor.ResizableExecutor#getPoolSize()
	 */
	@Override
	public int getPoolSize() {
		return (executor != null) ? executor.getPoolSize() : 0;
	}

	/**
	 * 返回当前等待队列大小。
	 *
	 * @return 当前等待队列大小
	 */
	public int getQueueSize() {
		return (executor != null) ? executor.getQueue().size() : -1;
	}

	/**
	 * @see cn.com.higinet.tms.common.executor.ResizableExecutor#resizePool(int, int)
	 */
	@Override
	public boolean resizePool(int corePoolSize, int maximumPoolSize) {
		if (executor == null)
			return false;

		executor.setCorePoolSize(corePoolSize);
		executor.setMaximumPoolSize(maximumPoolSize);
		return true;
	}

	/**
	 * @see cn.com.higinet.tms.common.executor.ResizableExecutor#resizeQueue(int)
	 */
	@Override
	public boolean resizeQueue(int capacity) {
		return false;
	}

	/**
	 * @see java.util.concurrent.ExecutorService#shutdown()
	 */
	@Override
	public void shutdown() {
		executor.shutdown();

	}

	/**
	 * @see java.util.concurrent.ExecutorService#shutdownNow()
	 */
	@Override
	public List<Runnable> shutdownNow() {
		return executor.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return executor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return executor.isTerminated();
	}

	/**
	 * @see java.util.concurrent.ExecutorService#awaitTermination(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return executor.awaitTermination(timeout, unit);
	}

	/**
	 * @see java.util.concurrent.ExecutorService#submit(java.util.concurrent.Callable)
	 */
	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return executor.submit(task);
	}

	/**
	 * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable, java.lang.Object)
	 */
	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return executor.submit(task, result);
	}

	/**
	 * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable)
	 */
	@Override
	public Future<?> submit(Runnable task) {
		return executor.submit(task);
	}

	/**
	 * @see java.util.concurrent.ExecutorService#invokeAll(java.util.Collection)
	 */
	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return executor.invokeAll(tasks);
	}

	/**
	 * @see java.util.concurrent.ExecutorService#invokeAll(java.util.Collection, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		return executor.invokeAll(tasks, timeout, unit);
	}

	/**
	 * @see java.util.concurrent.ExecutorService#invokeAny(java.util.Collection)
	 */
	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return executor.invokeAny(tasks);
	}

	/**
	 * @see java.util.concurrent.ExecutorService#invokeAny(java.util.Collection, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return executor.invokeAny(tasks, timeout, unit);
	}

}