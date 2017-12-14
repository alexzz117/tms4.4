package cn.com.higinet.tms.common.event.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.ExceptionHandler;

public class DisruptorExceptionHandler implements ExceptionHandler<Object> {
	private static final Logger logger = LoggerFactory.getLogger(DisruptorExceptionHandler.class);

	@Override
	public void handleEventException(final Throwable ex, final long sequence, final Object event) {
		logger.error("Exception processing: " + sequence + " " + event, ex);
	}

	@Override
	public void handleOnStartException(final Throwable ex) {
		logger.error("Exception during onStart()", ex);
	}

	@Override
	public void handleOnShutdownException(final Throwable ex) {
		logger.error("Exception during onShutdown()", ex);
	}
}