package cn.com.higinet.tms.consumer.traffic;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import cn.com.higinet.tms.base.constant.Constants;
import cn.com.higinet.tms.base.entity.Traffic;
import cn.com.higinet.tms.base.entity.TrafficData;
import cn.com.higinet.tms.base.entity.online.tms_run_rule_action_hit;
import cn.com.higinet.tms.base.entity.online.tms_run_ruletrig;

@Component
public class TrafficConsumer {

	private static final Logger logger = LoggerFactory.getLogger( TrafficConsumer.class );

	@Autowired
	TrafficQueue trafficQueue;

	@Autowired
	RuleTrigQueue ruleTrigQueue;

	@Autowired
	RuleActionHitQueue ruleActionHitQueue;

	@KafkaListener(topics = { Constants.Kafka.Topic.TRAFFIC })
	public void listen( ConsumerRecord<String, Traffic> record ) throws Exception {
		Traffic traffic = record.value();
		if( traffic == null ) return;

		TrafficData trafficData = traffic.getTrafficData();
		if( trafficData != null ) trafficQueue.put( trafficData );

		List<tms_run_ruletrig> ruletrigs = traffic.getRuletrigs();
		ruleTrigQueue.put( ruletrigs );

		List<tms_run_rule_action_hit> ruleActionHits = traffic.getRuleActionHits();
		ruleActionHitQueue.put( ruleActionHits );
	}
}
