/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  DisruptorEventHandler.java   
 * @Package cn.com.higinet.tms.common.event.impl   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-9 17:32:46   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.event.impl;

import com.lmax.disruptor.EventHandler;

import cn.com.higinet.tms.common.event.EventContext;
import cn.com.higinet.tms.common.event.EventDispatcher;

/**
 * bus的处理类只负责分发event到具体的eventchannel中去，不做任何其他处理。
 *
 * @author: 王兴
 * @date:   2017-5-9 17:32:48
 * @since:  v4.3
 */
public class DisruptorEventHandler implements EventHandler<EventContext> {

	/** dispatcher. */
	private EventDispatcher dispatcher;

	/**
	 * 构造一个新的对象.
	 *
	 * @param dispatcher the dispatcher
	 */
	public DisruptorEventHandler(EventDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	/**
	 * @see com.lmax.disruptor.EventHandler#onEvent(java.lang.Object, long, boolean)
	 */
	@Override
	public void onEvent(EventContext event, long sequence, boolean endOfBatch) throws Exception {
		event.setSequence(sequence);
		event.setEndOfBatch(endOfBatch);
		dispatcher.dispatch(event);
	}

}
