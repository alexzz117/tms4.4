package cn.com.higinet.tms.consumer.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 此类将在启动时扫描
 * 判断已配置的定时任务所对应的java类是否存在，及接口方法是否实现。否则将会启动提示错误
 * 此举主要是为了避免人为删除或改动了定时任务java类，但发布和启动时不能及时发现问题。
 * */

@Component
public class ProducerRunner implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger( ProducerRunner.class );

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Override
	public void run( String... arg0 ) throws Exception {
		/*for( int i = 0; i < 900000000; i++ ) {
			String message = String.valueOf( i );
			ProducerRecord<String, String> record = new ProducerRecord<String, String>( "test3", message, message );
			kafkaTemplate.send( record );
			logger.info( String.valueOf( i ) );
		}*/
	}

}
