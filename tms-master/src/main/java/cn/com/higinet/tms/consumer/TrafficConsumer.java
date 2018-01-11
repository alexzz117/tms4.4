package cn.com.higinet.tms.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import cn.com.higinet.tms.base.entity.TrafficData;
import cn.com.higinet.tms.common.elasticsearch.ElasticSearchAdapter;

@Component
public class TrafficConsumer {
	private static final Logger logger = LoggerFactory.getLogger( TrafficConsumer.class );

	@Autowired
	ElasticSearchAdapter elasticSearchAdapter;

	@Autowired
	TrafficQueue trafficQueue;

	@KafkaListener(topics = { "traffic" })
	public void listen( ConsumerRecord<String, String> record ) {
		logger.info( JSON.toJSONString( record ) );

		TrafficData traffic = new TrafficData();
		try {
			trafficQueue.put( traffic );
		}
		catch( Exception e ) {
			logger.error( e.getMessage(), e );
		}
	}
}
