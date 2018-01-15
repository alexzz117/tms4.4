package cn.com.higinet.tms.common.serialization;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import cn.com.higinet.tms.base.entity.TrafficData;
import cn.com.higinet.tms.base.util.Kryoz;

public class KafkaDeserializer implements Deserializer<TrafficData> {

	@Override
	public void configure( Map<String, ?> configs, boolean isKey ) {
		// TODO Auto-generated method stub

	}

	@Override
	public TrafficData deserialize( String topic, byte[] data ) {
		return Kryoz.toObject( TrafficData.class, data );
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
