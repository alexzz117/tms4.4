package cn.com.higinet.tms.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import cn.com.higinet.tms.base.constant.Constants;
import cn.com.higinet.tms.base.entity.TrafficData;

@Component
public class TrafficConsumer {
	//private static final Logger logger = LoggerFactory.getLogger( TrafficConsumer.class );

	@Autowired
	TrafficQueue trafficQueue;

	@KafkaListener(topics = { Constants.Kafka.Topic.TRAFFIC })
	public void listen( ConsumerRecord<String, TrafficData> record ) throws Exception {
		trafficQueue.put( record.value() );
	}
}
