package com.higinet.kfk.consumer.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

public class Consumer {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@KafkaListener(topics = {"sifulin"})
	public void listen(ConsumerRecord<String, String> record) {
		logger.info("TOPIC:"+record.topic() + "--PARTION:" + record.partition() + "--MSG:" +record.value());
		//System.out.println("TOPIC:"+record.topic() + "--PARTION:" + record.partition() + "--MSG:" +record.value());
	}
}
