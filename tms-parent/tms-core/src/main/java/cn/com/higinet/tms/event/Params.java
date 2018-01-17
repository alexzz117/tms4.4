/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  Params.java   
 * @Package cn.com.higinet.tms.event   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-15 10:35:35   
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
 * 内部事件的参数
 *
 * @ClassName:  Params
 * @author: 王兴
 * @date:   2018-1-15 10:35:35
 * @since:  v4.3
 */
public class Params {

	/** 用于kafka主题的入参，消息主题. */
	public static final String KAFKA_TOPIC = "_kafka_topic_";

	/** 用于kafka主题的入参，消息数据. */
	public static final String KAFKA_DATA = "_kafka_data_";

	/** 用于kafka主题的入参，是否使用分区，true/false，默认false */
	public static final String KAFKA_USE_PARTITION = "_kafka_use_partition_";

	/** 用于kafka主题的入参，分区键值，KAFKA_USE_PARTITION为true时候使用，会被hash后取模分区数 */
	public static final String KAFKA_PARTITION_KEY = "_kafka_partition_key_";
}
