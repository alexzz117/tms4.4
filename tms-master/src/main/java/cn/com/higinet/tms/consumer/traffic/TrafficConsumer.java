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
import cn.com.higinet.tms.consumer.traffic.queue.RuleActionHitQueue;
import cn.com.higinet.tms.consumer.traffic.queue.RuleTrigQueue;
import cn.com.higinet.tms.consumer.traffic.queue.TrafficQueue;

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
		List<tms_run_ruletrig> ruletrigs = traffic.getRuletrigs();
		List<tms_run_rule_action_hit> ruleActionHits = traffic.getRuleActionHit();

		if( trafficData != null ) trafficQueue.put( trafficData );
		if( ruletrigs != null ) ruleTrigQueue.put( ruletrigs );
		if( ruleActionHits != null ) ruleActionHitQueue.put( ruleActionHits );
	}
}
