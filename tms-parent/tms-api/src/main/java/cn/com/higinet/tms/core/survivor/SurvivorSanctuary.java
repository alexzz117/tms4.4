package cn.com.higinet.tms.core.survivor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.core.model.TmsConfigVo;

/**
 * 
 * 为了补救因为评估确认没有找到交易数据部分的接口调用而写 有时候因为网络或者其他原因， 导致风险评估和风险确认发往不同的服务器了， 此队列是为这部分数据而准备，会采用新的逻辑往评估引擎发送消息
 * 
 * @author wx
 *
 */
public class SurvivorSanctuary {
	private static final Logger log = LoggerFactory.getLogger(SurvivorSanctuary.class);

	private static SurvivorSanctuary survivors = null;

	private BlockingQueue<EventContext> survivorQueue;

	private Lifeguard lifeguard;

	private Thread guardThread; // 监控线程

	private volatile boolean running = true;

	private boolean enabled = Boolean.valueOf(TmsConfigVo.getProperty("survivorSanctuary.enabled", "true"));

	private SurvivorSanctuary() {
	};

	public static SurvivorSanctuary instance() {
		if (survivors == null) {
			synchronized (SurvivorSanctuary.class) {
				if (survivors == null) {
					survivors = new SurvivorSanctuary();
					survivors.init();
				}
			}
		}
		return survivors;
	}

	public void init() {
		running = true;
		survivorQueue = new LinkedBlockingQueue<EventContext>();// 无界队列
		lifeguard = new Lifeguard();
		lifeguard.setQueue(survivorQueue);
		guardThread = new Thread(lifeguard, "tms-api-lifeguard");
		guardThread.start();
	}

	public void addSurvivor(EventContext context) {
		if (enabled) {
			if (running) {
				if (context.getCurrentFailureTimes() < context.getRetryTimes()) {
					context.setCurrentFailureTimes(context.getCurrentFailureTimes() + 1);// 失败次数+1
					survivorQueue.add(context);
				} else {
					lifeguard.increaseLost();
					log.warn(new StringBuilder("Cann't save this message [retryTimes:").append(context.getRetryTimes()).append(",body:").append(context.getBody()).append("]").toString());
				}
			} else {
				log.info("survivor queue not running.");
			}
		}
	}

	public void destroy() {
		running = false;
		guardThread.interrupt();
		try {
			guardThread.join();
		} catch (InterruptedException e) {
		}
		int size = survivorQueue.size();
		log.info("survivor queue is stopping,there still " + size + " survivors in the queue.");
		survivorQueue.clear();
	}
}
