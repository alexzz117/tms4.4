package cn.com.higinet.tms;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

import cn.com.higinet.tms.base.entity.TrafficData;
import cn.com.higinet.tms.base.util.Kryoz;

public class KafkaEncode implements Serializer<TrafficData> {

	@Override
	public void configure( Map<String, ?> configs, boolean isKey ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] serialize( String topic, TrafficData data ) {
		return Kryoz.toBytes( data );
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}


}
