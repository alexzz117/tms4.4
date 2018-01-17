/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  KafkaMessageHandler.java   
 * @Package cn.com.higinet.tms.event.modules.kafka   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-16 15:23:58   
 * @version V1.0 
 * @Copyright: 2018 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.event.modules.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.kafka.common.PartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import cn.com.higinet.tms.common.event.EventChannel;
import cn.com.higinet.tms.common.event.EventChannelHandler;
import cn.com.higinet.tms.common.event.EventContext;
import cn.com.higinet.tms.event.Params;

/**
 * kafka消息队列的事件监听处理类
 *
 * @ClassName:  KafkaMessageHandler
 * @author: 王兴
 * @date:   2018-1-16 15:23:58
 * @since:  v4.3
 */
@Component
public class KafkaMessageHandler implements EventChannelHandler {

	@Autowired
	private KafkaTemplate<String, Object> template;

	private Map<String, Integer> topicPartitions = new HashMap<>();

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.event.EventChannelHandler#handleEvent(cn.com.higinet.tms.common.event.EventContext, cn.com.higinet.tms.common.event.EventChannel)
	 */
	@Override
	public void handleEvent(EventContext event, EventChannel channel) throws Exception {
		String topicName = event.getData(String.class, Params.KAFKA_TOPIC);
		Object message = event.getData(Params.KAFKA_DATA);
		boolean usePartition = event.getData(Boolean.class, Params.KAFKA_USE_PARTITION, false);
		if (usePartition) {
			Integer partitions = topicPartitions.get(topicName);
			if (partitions == null) {
				synchronized (topicPartitions) {
					partitions = topicPartitions.get(topicName);
					if (partitions == null) {
						List<PartitionInfo> _partitions = template.partitionsFor(topicName);
						partitions = _partitions.size();
						topicPartitions.put(topicName, partitions);
					}
				}
			}
			String partitionKey = event.getData(String.class, Params.KAFKA_PARTITION_KEY);
			int hashCode = Objects.hashCode(partitionKey);
			if (hashCode < 0) {
				hashCode *= -1;
			}
			template.send(topicName, hashCode % partitions, message);
		} else {
			template.send(topicName, message);
		}
	}

}
