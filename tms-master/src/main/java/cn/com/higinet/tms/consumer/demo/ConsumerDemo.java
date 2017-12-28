package cn.com.higinet.tms.consumer.demo;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumerDemo {

	private static final Logger logger = LoggerFactory.getLogger( ConsumerDemo.class );

	@KafkaListener(topics = { "test3" })
	public void listen( ConsumerRecord<String, String> record ) {
		logger.info( "TOPIC:" + record.topic() + "--PARTION:" + record.partition() + "--MSG:" + record.value() );
	}

}
