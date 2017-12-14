package cn.com.higinet.tms.core.thread;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.core.model.Batch;
import cn.com.higinet.tms.core.model.TmsConfigVo;
import cn.com.higinet.tms.core.model.Transaction;
import cn.com.higinet.tms.core.util.DateUtil;
import cn.com.higinet.tms.core.util.StringUtil;

public class DispatcherExecutor {

	//	private ThreadPoolExecutor[] executors;
	private ThreadPoolExecutor executor;

	//	private int[] queueNum = null;

	//	private int eCount = 0;
	private int minPoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;

	private int maxPoolSize = minPoolSize < 100 ? 200 : minPoolSize * 2;

	private int maxIdleTime = 300; // 单位秒

	private static DispatcherExecutor instance = null;

	// private final static Logger logger = LogManager.getLogger(DispatcherExecutor.class);
	private static Logger logger = LoggerFactory.getLogger(DispatcherExecutor.class);

	public static DispatcherExecutor getInstance() {
		if (instance == null) {
			try {
				// ServerConfig config = ServerConfig.getInstance();
				// 分发线程池初始化
				int count = TmsConfigVo.getCount();
				int queueCapacity = TmsConfigVo.getQueueCapacity();

				instance = new DispatcherExecutor(count, queueCapacity);
				logger.info(DateUtil.dateConvert(new Date(), "yyyy-MM-dd HH:mm:ss") + "  ThreadPool init finished.  ");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	/**
	 * @param count
	 *            线程数
	 * @param queueLength
	 *            每线程队列长度
	 */
	private DispatcherExecutor(int count, int queueLength) {
		//		eCount = count;
		//		queueNum = new int[eCount];
		//		executors = new ThreadPoolExecutor[count > 0 ? count : 2];
		//		for (int i = 0; i < executors.length; i++) {
		//			executors[i] = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(queueLength));
		//			queueNum[i] = 0;
		//			executors[i].setRejectedExecutionHandler(new BlockRunsPolicy());
		//		}
		executor = new ThreadPoolExecutor(minPoolSize, maxPoolSize, maxIdleTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
				SecurityManager s = System.getSecurityManager();
				ThreadGroup group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
				return new Thread(group, r, "tms-api-async-" + threadNumber.getAndIncrement());
			}

		});
	}

	/**
	 * api单笔异步报文发送执行方法
	 * 
	 * @param transaction
	 * @param timeout
	 * @param actionCode
	 * @param interfaceFlag
	 */
	public void execute(Transaction transaction, int timeout, String actionCode, String interfaceFlag) {
		execute(transaction, null, null, timeout, actionCode, interfaceFlag);
	}

	/**
	 * api批量异步报文发送执行方法
	 * 
	 * @param batch
	 * @param transactions
	 * @param timeout
	 * @param actionCode
	 * @param interfaceFlag
	 */
	public void execute(Batch batch, List<Transaction> transactions, int timeout, String actionCode, String interfaceFlag) {
		execute(null, batch, transactions, timeout, actionCode, interfaceFlag);
	}

	/**
	 * api异步报文发送执行方法
	 * 
	 * @param transaction
	 * @param batch
	 * @param transactions
	 * @param timeout
	 * @param actionCode
	 * @param interfaceFlag
	 */
	private void execute(Transaction transaction, Batch batch, List<Transaction> transactions, int timeout, String actionCode, String interfaceFlag) {
//		String runId = "";
//		if (transaction != null) {
//			runId = transaction.getCstNo();
//			if (StringUtil.isBlank(runId)) {
//				runId = transaction.getSessionId();
//			}
//		} else if (batch != null) {
//			runId = batch.getCstNo();
//			if (StringUtil.isBlank(runId)) {
//				runId = batch.getSessionId();
//			}
//		}

//		int thid = Math.abs(runId.hashCode() % executors.length);
//		queueNum[thid]++;
//		executors[thid].execute(new WorkThread(transaction, batch, transactions, timeout, actionCode, interfaceFlag));
		executor.execute(new WorkThread(transaction, batch, transactions, timeout, actionCode, interfaceFlag));
	}

	/**
	 * 销毁线程
	 */
	public void destroy() {
//		for (int i = 0; i < executors.length; i++) {
//			if (!executors[i].isShutdown()) {
//				executors[i].shutdown();
//			}
//		}
		executor.shutdown();
	}

	public void print() {

//		for (int i = 0; i < eCount; i++) {
//			logger.info("queueNum" + i + "=" + queueNum[i]);
//		}
	}

	public String getNum() {
		StringBuffer sb = new StringBuffer();
//		for (int i = 0; i < executors.length; i++) {
//
//			sb.append("线程" + i + "剩余：" + executors[i].getQueue().remainingCapacity() + "******");
//		}
		sb.append("线程剩余：" + executor.getQueue().remainingCapacity() + "******");
		return sb.toString();

	}

	public static class BlockRunsPolicy implements RejectedExecutionHandler {
		/**
		 * Creates a <tt>CallerRunsPolicy</tt>.
		 */
		public BlockRunsPolicy() {
		}

		/**
		 * Executes task r in the caller's thread, unless the executor has been shut down, in which case the task is discarded.
		 * 
		 * @param r
		 *            the runnable task requested to be executed
		 * @param e
		 *            the executor attempting to execute this task
		 */
		public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
			try {
				logger.info("队列还有空余：" + e.getQueue().remainingCapacity());
				Thread.currentThread().sleep(200);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if (!e.isShutdown()) {
				e.execute(r);
			}
		}
	}
}
