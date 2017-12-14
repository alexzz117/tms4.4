package cn.com.higinet.tms.core.survivor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.core.common.StaticParameter;
import cn.com.higinet.tms.core.model.TmsConfigVo;
import cn.com.higinet.tms.core.service.MessageService;
import cn.com.higinet.tms.core.service.impl.MessageFastXml;
import cn.com.higinet.tms.core.service.impl.MessageVtdXml;
import cn.com.higinet.tms.core.service.impl.SaxXml;

/**
 * 从幸存者队列里面获取context，进行消息重发操作。
 * 
 * @author wx
 *
 */
public class Lifeguard implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(Lifeguard.class);
	private BlockingQueue<EventContext> queue;
	private int waitTime = 300; // 秒
	private volatile boolean running = true;
	private int foundSuvivers;
	private AtomicInteger savedSurvivers = new AtomicInteger(0);
	private AtomicInteger lostMessages = new AtomicInteger(0);
	private ThreadPoolExecutor savers;
	private MessageService messageSender;
	private int minPoolSize = 1;
	private int maxPoolSize = 50;
	private int maxIdleTime = 300; // 单位秒

	public BlockingQueue<EventContext> getQueue() {
		return queue;
	}

	public void setQueue(BlockingQueue<EventContext> queue) {
		this.queue = queue;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public void increaseLost() {
		lostMessages.getAndIncrement();
	}

	@Override
	public void run() {
		init();
		while (running) {
			try {
				EventContext ec = queue.poll(waitTime, TimeUnit.SECONDS);
				if (ec != null) {
					// 将搜索到的事故消息送往执行线程池
					if (ec.getCurrentFailureTimes() == 0) {
						foundSuvivers++;
					}
					Saver saver = new Saver();
					saver.setContext(ec);
					saver.setMessageSender(messageSender);
					saver.setSavedSuvivers(savedSurvivers);
					savers.submit(saver);
				}
				StringBuilder msg = new StringBuilder("Lifeguard found ").append(foundSuvivers).append(" survivers,saved ").append(savedSurvivers.get()).append(",lost ").append(lostMessages.get()).append(".");
				log.info(msg.toString());
			} catch (InterruptedException e) {
				// 只有外部关闭服务才会产生这个异常
				running = false;
			} catch (Exception e2) {
				log.error("Exception occurred when poll surviver from surviver queue.", e2);
			}
		}
		destroy();
	}

	private void init() {
		log.info("Lifeguard for surviver queue is initialized,minPoolSize:" + minPoolSize + ",maxPoolSize:" + maxPoolSize + ",maxIdleTime:" + maxIdleTime + " sec.");
		savers = new ThreadPoolExecutor(minPoolSize, maxPoolSize, maxIdleTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
				SecurityManager s = System.getSecurityManager();
				ThreadGroup group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
				return new Thread(group, r, "tms-api-saver-" + threadNumber.getAndIncrement());
			}

		});

		String parserName = TmsConfigVo.getParserName();
		if (StaticParameter.XML_PARSER_SAX.equals(parserName)) {
			messageSender = new SaxXml();
		} else if (StaticParameter.XML_PARSER_OPEN4J.equals(parserName)) {
			messageSender = new MessageFastXml();// open4j解析器
		} else if (StaticParameter.XML_PARSER_VTD.equals(parserName)) {
			messageSender = new MessageVtdXml();// vtd 解析器
		} else {
			messageSender = new MessageVtdXml();
		}
	}

	private void destroy() {
		// 停止工作线程池
		savers.shutdownNow(); // 由于补救时间可能会很长，强制停止
		StringBuilder msg = new StringBuilder("Lifeguard found ").append(foundSuvivers).append(" survivers,saved ").append(savedSurvivers.get()).append(",lost ").append(lostMessages.get()).append(".");
		log.info(msg.toString());
		log.info("Lifeguard for surviver queue is destroyed.");
	}
}
