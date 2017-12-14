/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  ThreadService.java   
 * @Package cn.com.higinet.tms.common.lifecycle   
 * @Description: 具有生命周期的线程服务类   
 * @author: 王兴
 * @date:   2017-5-7 17:46:14   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.lifecycle;

/**
 * 继承自{@link cn.com.higinet.tms.common.lifecycle.Service}类，主要为生命周期对象提供了线程化的模板，
 * 此类也是属于模板设计模式。启动此类后，会生成一个非守护线程，可以通过{@link #setDaemon(boolean)}方法
 * 设置此对象是不是守护线程。
 * 子类需要实现{@link #onStart()}、{@link #onRun()}、{@link #onStop()}方法，{@link #onStart()}是在主线程中调用，
 * {@link #onRun()}、{@link #onStop()}是在线程服务启动过后调用的，他们使用的classloader可能不一样，这里需要注意。
 * {@link #onStart()}放在主线程中去执行，是因为可以及时发现线程服务是否有启动成功，如果是子线程中去执行，那么只能
 * 通过线程间通信，告知主线程执行情况。
 * @author: 王兴
 * @date:   2017-5-7 17:46:14
 * @since:  v4.3
 */
public abstract class ThreadService extends Service implements Runnable {

	/** thread. */
	protected Thread thread;

	/** daemon. */
	protected boolean daemon;

	/** delay. */
	private int delay = -1;

	public Thread getThread() {
		return thread;
	}

	/**
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStart()
	 */
	protected void doStart() throws Throwable {
		thread = new Thread(this);
		//		try {
		onStart();
		//		} catch (Throwable e) {
		//			logger.error("Exceptions occurred when start Service[name={" + getServiceName() + "}].", e);
		//		}
		if (daemon) {
			thread.setDaemon(daemon);
		}
		thread.setName(this.getServiceName());
		thread.start();
	}

	/**
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStop()
	 */
	protected void doStop() throws Throwable {
		onStop();

		thread.interrupt();

		try {
			thread.join();
		} catch (InterruptedException ignore) {
		} finally {
			thread = null;
		}
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (delay > 0) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
			}
		}

		while (isStarting() || isRunning()) {
			try {
				onRun();
			} catch (Throwable ex) {
			}
		}
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public boolean isDaemon() {
		return daemon;
	}

	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	/**
	 * On start.
	 *
	 * @throws Throwable the throwable
	 */
	protected abstract void onStart() throws Throwable;

	/**
	 * On stop.
	 *
	 * @throws Throwable the throwable
	 */
	protected abstract void onStop() throws Throwable;

	/**
	 * On run.
	 *
	 * @throws Throwable the throwable
	 */
	protected abstract void onRun() throws Throwable;
}