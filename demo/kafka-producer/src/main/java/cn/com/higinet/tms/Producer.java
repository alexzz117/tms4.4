package cn.com.higinet.tms;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import cn.com.higinet.tms.base.entity.Traffic;
import cn.com.higinet.tms.base.entity.TrafficData;

@Component
public class Producer {

	private static final Logger logger = LoggerFactory.getLogger( Producer.class );
	ExecutorService executorService;

	@Autowired
	private KafkaTemplate<String, Traffic> kafkaTemplate;

	@PostConstruct
	public void init() {
		executorService = Executors.newFixedThreadPool( 20 );
	}

	//每秒10000条
	@Scheduled(fixedRate = 1000)
	private void executeTask() throws Exception {
		executorService.execute( new Runnable() {
			@Override
			public void run() {
				for( int i = 0; i < 20000; i++ ) {
					Traffic traffic = new Traffic();
					TrafficData trafficData = createData();
					traffic.setTrafficData( trafficData );
					kafkaTemplate.send( "traffic", trafficData.getTxnCode(), traffic );
					//ListenableFuture<SendResult<String, Traffic>> result = kafkaTemplate.send( "traffic", trafficData.getTxnCode(), traffic );
					/*try {
						logger.info( result.get().getProducerRecord().key() + ", " + result.get().getProducerRecord().topic() + ", " + result.get().getProducerRecord().partition() );
					}
					catch( InterruptedException | ExecutionException e ) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
				}
			}

		} );

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
		//data.setText1(Enums.random(NameEnum.class).toString());
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
