/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  BatchEventHandler.java   
 * @Package cn.com.higinet.tms.common.event.impl   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-6-28 18:24:21   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.event.impl;

import java.util.ArrayList;
import java.util.List;

import cn.com.higinet.tms.common.event.EventChannel;
import cn.com.higinet.tms.common.event.EventChannelHandler;
import cn.com.higinet.tms.common.event.EventContext;

/**
 * 批处理的事件处理器，一次处理一批事件，只能结合disruptor通道一起使用。
 * 批次的定义目前为一次处理过程后的累计条数，即一次批处理过后ringbuffer中余留的事件条数。
 * 并没有对大小做限制，也没有对时间做限制。
 *
 * @author: 王兴
 * @date:   2017-6-28 18:24:26
 * @since:  v4.3
 */
public abstract class BatchDisruptorEventChannelHandler implements EventChannelHandler {

	protected List<EventContext> events = new ArrayList<EventContext>(1024);

	/** 
	 * @see cn.com.higinet.tms.common.event.EventChannelHandler#handleEvent(cn.com.higinet.tms.common.event.EventContext, cn.com.higinet.tms.common.event.EventChannel)
	 */
	@Override
	public void handleEvent(EventContext event, EventChannel channel) throws Exception {
		events.add(event);
		if (event.isEndOfBatch() | events.size() == 1024) {
			try {
				handleEvents(events, channel);
			} catch (Exception e) {
				throw e;
			} finally {
				events.clear();
			}
		}
	}

	public abstract void handleEvents(List<EventContext> events, EventChannel channel) throws Exception;

}
