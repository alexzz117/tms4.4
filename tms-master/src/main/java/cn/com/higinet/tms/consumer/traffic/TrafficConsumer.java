package cn.com.higinet.tms.consumer.traffic;

import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import cn.com.higinet.tms.base.constant.Constants;
import cn.com.higinet.tms.base.entity.Traffic;
import cn.com.higinet.tms.base.entity.TrafficData;

@Component
public class TrafficConsumer {

	private static final Logger logger = LoggerFactory.getLogger( TrafficConsumer.class );

	@Autowired
	TrafficQueue trafficQueue;

	@KafkaListener(topics = { Constants.Kafka.Topic.TRAFFIC })
	public void listen( ConsumerRecord<String, Traffic> record ) throws Exception {
		//TrafficData trafficData = record.value().getTrafficData();
		TrafficData trafficData = this.createData();
		trafficQueue.put( trafficData );
	}

	private TrafficData createData() {
		TrafficData data = new TrafficData();
		data.setTxnCode( UUID.randomUUID().toString() );
		data.setIpAddress( "11.11.11.11" );
		data.setDisposal( "PS01" );
		data.setCounTryCode( "5044啊啊啊啊啊啊啊大大大大大大撒大" );
		data.setRegionCode( "5044110000啊啊啊啊啊啊啊大大大" );
		data.setCityCode( "5044110000啊啊啊啊啊啊啊大大大大大大撒大苏打" );
		data.setIspCode( "504411000050441100005044110000" );
		data.setCreateTime( 1513040400000L );
		data.setFinishTime( 1513040400000L );
		data.setTxnStatus( 1 );
		data.setBatchNo( "123123啊啊啊啊啊啊啊大大大大大大撒大苏打似的" );
		data.setHitrulenum( 1 );
		data.setIsCorrect( "1啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊啊啊大大" );
		data.setConfirmRisk( "1啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊啊啊大大大" );
		data.setIsEval( "1啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊啊啊大大大大大" );
		data.setModelId( "102啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊啊啊大大大大" );
		data.setText1( "北京三里屯啊啊啊啊啊啊啊大大大大大大撒大苏打似的" );
		data.setText2( "北京西城区啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊啊啊大大大大大大撒大苏" );
		data.setText3( "啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊" );
		data.setText5( "北京三里屯啊啊啊啊啊啊啊大大大大" );
		data.setText6( "北京三里屯啊啊啊啊啊啊啊大大大大" );
		data.setText7( "北京三里屯啊啊啊啊啊啊啊大大大大" );
		data.setText8( "北京三里屯啊啊啊啊啊啊啊大大大大" );
		data.setText9( "北京三里屯啊啊啊啊啊啊啊大大大大" );
		data.setNum1( 1.00 );
		data.setNum2( 1.00 );
		data.setNum3( 1.00 );
		data.setNum4( 1.00 );
		data.setNum5( 1.00 );
		data.setNum6( 1.00 );
		data.setNum7( 1.00 );
		data.setNum8( 1.00 );

		return data;
	}
}
