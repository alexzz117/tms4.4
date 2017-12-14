package cn.com.higinet.tms.core.survivor;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.core.common.LogHolder;
import cn.com.higinet.tms.core.service.MessageService;

public class Saver implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(Saver.class);
	private EventContext context;
	private MessageService messageSender;
	private AtomicInteger savedSuvivers;

	public EventContext getContext() {
		return context;
	}

	public void setContext(EventContext context) {
		this.context = context;
	}

	public MessageService getMessageSender() {
		return messageSender;
	}

	public void setMessageSender(MessageService messageSender) {
		this.messageSender = messageSender;
	}

	public AtomicInteger getSavedSuvivers() {
		return savedSuvivers;
	}

	public void setSavedSuvivers(AtomicInteger savedSuvivers) {
		this.savedSuvivers = savedSuvivers;
	}

	@Override
	public void run() {
		LogHolder.reset();
		LogHolder.put(LogHolder.TXN_ID, "saver-run");
		EventContextHolder.setContext(context);
		String res = messageSender.sendMessage(context.getModel(), context.getRunId(), context.getHead(), context.getBody(), context.getTimeout(), null);
		if (res != null) {
			savedSuvivers.getAndIncrement();
			log.info("Saver saved a surviver [" + context.getBody() + "]");
		}
		EventContextHolder.removeContext();
	}
}
