package cn.com.higinet.tms.base.serialization;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import cn.com.higinet.tms.base.constant.Constants;
import cn.com.higinet.tms.base.entity.Traffic;
import cn.com.higinet.tms.base.util.Kryoz;

public class KafkaDeserializer implements Deserializer<Object> {

	@Override
	public void configure( Map<String, ?> configs, boolean isKey ) {
		// TODO Auto-generated method stub

	}

	/**
	 * 根据不同的topic使用不同的反序列化方式
	 * */
	@Override
	public Object deserialize( String topic, byte[] data ) {
		if( data == null ) return null;
		Object obj = null;
		switch( topic ) {
			case Constants.Kafka.Topic.TRAFFIC :
				obj = Kryoz.toObject( Traffic.class, data, true );
				break;
			case Constants.Kafka.Topic.RULE_HIT :
				obj = Kryoz.toObject( Object.class, data );
				break;
			default :
				obj = Kryoz.toObject( Object.class, data );
				break;
		}
		return obj;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
