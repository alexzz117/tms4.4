/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  Topics.java   
 * @Package cn.com.higinet.tms.event   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-15 10:35:21   
 * @version V1.0 
 * @Copyright: 2018 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.event;

/**
 * 内部事件的主题
 *
 * @ClassName:  Topics
 * @author: 王兴
 * @date:   2018-1-15 10:35:21
 * @since:  v4.3
 */
public class Topics {

	/**发布kafka主题，将event事件发布到kafka. */
	public static final String TO_KAFKA = "_to_kafka_";
}
