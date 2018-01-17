/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  KafkaTopics.java   
 * @Package cn.com.higinet.tms.event.modules.kafka   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-17 15:34:53   
 * @version V1.0 
 * @Copyright: 2018 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.event.modules.kafka;

/**
 * 对应kafka上的消费主题
 *
 * @ClassName:  KafkaTopics
 * @author: 王兴
 * @date:   2018-1-17 15:34:53
 * @since:  v4.4
 */
public class KafkaTopics {
	
	/** 交易流水. */
	public static final String TRAFFIC = "traffic";

	/** 规则触发. */
	public static final String RULE_HIT = "ruleHit";

	/** 规则动作命中. */
	public static final String RULE_ACTION_HIT = "ruleActionHit";

}
