package cn.com.higinet.tms.loadrunner;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KfkProducer {

	private static Logger logger = LoggerFactory.getLogger(KfkProducer.class); 

	private static KafkaProducer<String,String> producer;
	static{
		 Properties props = System.getProperties();
		 props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		 props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		 producer = new KafkaProducer<String,String>(props);
	}
	
	private static ProducerRecord<String,String> getProduceRecord(){
		
		String topic = System.getProperty("topic");
		int partion = Integer.parseInt(System.getProperty("partions"));
		partion = (int) (Thread.currentThread().getId() % partion);
		String value = "TXNCODE:adashhhu4hi124423242,CHANCODE:CH01,SESSIONID:asdasdasdasddasd,TXNID:NPP01,TXNTYPE:NPP01,USERID:asdasdasdsad,DEVICEID:asdasdds,SCORE:90,IPADDR:163.23.21.25"
				+ "COUNTRYCODE:00011,REGIONCODE:2255555,CITYCODE:asdasd,TXNTIME:548454545,CREATETIME:45545454545,FINISHTIME:54545454545,TXNSTATUS:1,BATCHNO:545454,HITRULENUM:5,DISPOSAL:54892656";
		ProducerRecord<String,String> record = new ProducerRecord<String,String>(topic,partion,null,value);
		return record;
	}
	
	public static void send(){
		try{
			producer.send(getProduceRecord());
		}catch(Exception e){
			logger.error("",e);
			throw new RuntimeException(e);
		}
		
	}
}
