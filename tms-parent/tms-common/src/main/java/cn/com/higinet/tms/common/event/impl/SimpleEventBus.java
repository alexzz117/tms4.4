/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  SimpleEventBus.java   
 * @Package cn.com.higinet.tms.common.event.impl   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-10 14:27:19   
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
 * 单线程的EventBus，任务调度都是在当前执行线程中执行。
 * 具体说明，请参考{@link cn.com.higinet.tms.common.event.EventBus}
 * @author: 王兴
 * @date:   2017-5-10 14:27:19
 * @since:  v4.3
 */
public class SimpleEventBus extends AbstractEventBus {

	@Override
	public void publish(EventContext event) throws Exception {
		this.dispatcher.dispatch(event);
	}

}
