package cn.com.higinet.tms.base.entity.online;

import lombok.Data;

@Data
public class tms_run_ruletrig {

	Long trigId; //规则命中主键ID
	String txnCode;//交易流水号
	String txnType; //交易类型
	Long ruleId; //规则主键
	String message; //规则命中信息
	Long numTimes; //规则执行毫秒数
	Long createTime; //开始执行时间
	Long ruleScore; //规则分值

}
