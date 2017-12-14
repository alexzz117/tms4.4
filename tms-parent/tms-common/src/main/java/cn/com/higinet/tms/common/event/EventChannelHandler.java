/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  ChannelEventHandler.java   
 * @Package cn.com.higinet.tms.common.event   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-11 15:14:46   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通道内使用的事件处理类，业务上实现这个接口即可。
 *
 * @author: 王兴
 * @date:   2017-5-11 15:14:46
 * @since:  v4.3
 */
public interface EventChannelHandler {
	public static final Logger logger = LoggerFactory.getLogger(EventChannelHandler.class);

	/**
	 * 处理事件
	 *
	 * @param event 当前事件上下文
	 * @param channel 事件通道
	 * @throws Exception channel必须捕获handle事件时候发生的异常
	 */
	public void handleEvent(EventContext event, EventChannel channel) throws Exception;
}
