package cn.com.higinet.tms.base.entity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.higinet.tms.base.entity.online.tms_run_rule_action_hit;
import cn.com.higinet.tms.base.entity.online.tms_run_ruletrig;
import lombok.Data;

@Data
public class Traffic {

	private Map<String, Object> tarffic;
	private List<tms_run_ruletrig> ruletrigs;
	private List<tms_run_rule_action_hit> ruleActionHit;

	private TrafficData trafficData;

	public TrafficData getTrafficData() {
		if( trafficData == null && tarffic != null ) {
			try {
				trafficData = new TrafficData();

				Map<String, Object> _tarffic = new HashMap<String, Object>();
				for( Map.Entry<String, Object> entry : tarffic.entrySet() ) {
					_tarffic.put( entry.getKey().toUpperCase(), entry.getValue() );
				}

				for( Field field : TrafficData.class.getDeclaredFields() ) {
					if( field.getModifiers() >= 16 ) continue;
					field.setAccessible( true );
					String key = field.getName();
					Object value = _tarffic.get( key.toUpperCase() );
					field.set( trafficData, value );

				}
			}
			catch( Exception e ) {
				e.printStackTrace();
				trafficData = null;
			}

		}
		return trafficData;
	}
}
