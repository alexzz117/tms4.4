package cn.com.higinet.tms.base.entity;

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
}
