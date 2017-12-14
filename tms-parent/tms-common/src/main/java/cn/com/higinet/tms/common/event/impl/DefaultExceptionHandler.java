/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  DefaultExceptionHandler.java   
 * @Package cn.com.higinet.tms.common.event.impl   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-9 19:23:20   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.event.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.ExceptionHandler;

import cn.com.higinet.tms.common.event.EventContext;
import cn.com.higinet.tms.common.event.EventWrapper;
import cn.com.higinet.tms.common.util.StringUtils;

/**
 * 此类目前只负责打印各种异常日志
 *
 * @author: 王兴
 * @date:   2017-5-9 19:25:08
 * @since:  v4.3
 */
public final class DefaultExceptionHandler implements ExceptionHandler<EventWrapper> {
	/** 常量 LOGGER. */
	private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

	/**
	 * Fatal exception handler.
	 */
	public DefaultExceptionHandler() {
	}

	/**
	 * @see com.lmax.disruptor.ExceptionHandler#handleEventException(java.lang.Throwable, long, java.lang.Object)
	 */
	@Override
	public void handleEventException(final Throwable ex, final long sequence, final EventWrapper boat) {
		EventContext event = boat.get();
		if (event != null) {
			logger.error(StringUtils.format("Exception occurred when processing event[topic=\"%s\",channel=\"%s\"", event.getTopic(), event.getSourceChannel()), ex);
		} else {
			logger.error("Exception occurred when processing event", ex);
		}
	}

	/**
	 * @see com.lmax.disruptor.ExceptionHandler#handleOnStartException(java.lang.Throwable)
	 */
	@Override
	public void handleOnStartException(final Throwable ex) {
		logger.error("Exception during onStart()", ex);
	}

	/**
	 * @see com.lmax.disruptor.ExceptionHandler#handleOnShutdownException(java.lang.Throwable)
	 */
	@Override
	public void handleOnShutdownException(final Throwable ex) {
		logger.error("Exception during onShutdown()", ex);
	}
}
