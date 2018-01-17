package cn.com.higinet.tms.base.entity.online;

import lombok.Data;

@Data
public class tms_run_rule_action_hit {

	private Long hitId; //动作命中主键ID	0	0	0
	private String txnType; //交易类型	utf8mb4	utf8mb4_bin	0
	private Long ruleId; //规则主键	0	0	0
	private String txnCode; //交易流水号	utf8mb4	utf8mb4_bin	0
	private Long acId; //规则动作ID
	private String acCond; //规则动作执行条件
	private String loacExpr; //规则动作执行表达式
	private Long createTime; //执行时间

}
