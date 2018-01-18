package cn.com.higinet.tms.base.serialization;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

import cn.com.higinet.tms.base.constant.Constants;
import cn.com.higinet.tms.base.util.Kryoz;

public class KafkaSerializer implements Serializer<Object> {

	@Override
	public void configure( Map<String, ?> configs, boolean isKey ) {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] serialize( String topic, Object data ) {
		byte[] bytes = null;
		switch( topic ) {
			case Constants.Kafka.Topic.TRAFFIC :
				bytes = Kryoz.toBytes( data );
				break;
			case Constants.Kafka.Topic.RULE_HIT :
				bytes = Kryoz.toBytes( data );
				break;
			default :
				bytes = Kryoz.toBytes( data );
				break;
		}
		return bytes;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
