/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  DisruptorEventFactory.java   
 * @Package cn.com.higinet.tms.common.event.impl   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-9 18:12:55   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.event.impl;

import com.lmax.disruptor.EventFactory;

import cn.com.higinet.tms.common.event.EventWrapper;

/**
 * DisruptorEvent 对象的工厂类，只负责生产空的{@link cn.com.higinet.tms.common.event.EventWrapper}对象，
 * 此对象会保持在RingBuffer中，因此只负责运输所需要的对象，当processor获取值过后，此对象会被清空内部数据。
 *
 * @ClassName:  DisruptorEventFactory
 * @author: 王兴
 * @date:   2017-5-9 18:12:55
 * @since:  v4.3
 */
public class DisruptorEventFactory implements EventFactory<EventWrapper> {

	/**
	 * @see com.lmax.disruptor.EventFactory#newInstance()
	 */
	@Override
	public EventWrapper newInstance() {
		return new EventWrapper();
	}

}
