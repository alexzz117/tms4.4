package com.higinet.kfk.consumer.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
@EnableKafka
public class KfkConsumer {

	@Value("${kafka.consumer.bootstrap.servers}")
	private String servers;
	@Value("${kafka.consumer.request.timeout}")
	private String requestTimeout;
	@Value("${kafka.consumer.group.id}")
	private String groupId;
	@Value("${kafka.consumer.concurrency}")
	private int concurrency;

	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory( consumerFactory() );
		factory.setConcurrency( concurrency );
		factory.getContainerProperties().setPollTimeout( 1000 );
		return factory;
	}

	private ConsumerFactory<String, String> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>( consumerConfigs() );
	}

	private Map<String, Object> consumerConfigs() {
		Map<String, Object> propsMap = new HashMap<>();
		propsMap.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers );
		propsMap.put( ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout );
		propsMap.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class );
		propsMap.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class );
		propsMap.put( ConsumerConfig.GROUP_ID_CONFIG, groupId );
		return propsMap;
	}

	@Bean
	public Consumer consumer() {
		return new Consumer();
	}

}
