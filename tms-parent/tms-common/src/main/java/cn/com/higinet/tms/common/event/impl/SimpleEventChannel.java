/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  SampleEventChannel.java   
 * @Package cn.com.higinet.tms.common.event.impl   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-10 15:45:06   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.event.impl;

import cn.com.higinet.tms.common.event.EventContext;

/**
 * 单线程事件通道，具体说明参考{@link cn.com.higinet.tms.common.event.EventChannel}
 *
 * @author: 王兴
 * @date:   2017-5-10 15:45:06
 * @since:  v4.3
 */
public class SimpleEventChannel extends AbstractEventChannel {

	/**
	 * 构造一个新的对象.
	 *
	 * @param topic the topic
	 */
	public SimpleEventChannel(String topic) {
		super(topic);
	}

	public SimpleEventChannel(String channelName, String topic) {
		super(channelName, topic);
	}

	/**
	 * @see cn.com.higinet.tms.common.event.EventChannel#onEvent(cn.com.higinet.tms.common.event.EventContext)
	 */
	@Override
	public void onEvent(EventContext event) throws Exception {
		this.handler.handleEvent(event, this);
	}

	/**
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStart()
	 */
	@Override
	protected void doStart() throws Throwable {

	}

	/**
	 * @see cn.com.higinet.tms.common.lifecycle.Service#doStop()
	 */
	@Override
	protected void doStop() throws Throwable {

	}

}
