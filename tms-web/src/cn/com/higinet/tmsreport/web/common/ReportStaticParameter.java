package cn.com.higinet.tmsreport.web.common;

import java.util.HashMap;
import java.util.Map;

public class ReportStaticParameter {
	
	public static final String [] SHAPECOLOR = {"#FF0000","#ff6600","#F6BD0F","#0000ff",
												"#00ff66","#330000","#dd6600","#ccBD0F",
												"#6600ff","#aaff66","#FF3399","#CC9933",
												"#99CCFF","#33CC99","#993366","#9900FF",
												"#99CC00","#FF3399","#99CC00","#CC9933",
												"#99CC00","#9900FF","#FFFF99","#42BF7A",
												"#B72CCB","#9999CC","#999999","#CCCCCC",
												"#FF6600","##999999"};
	public static final Map<String,String> txnTargetMap = new HashMap<String,String>() { 
		       { 
		   		put("ALERTNUMBER", "报警总数图");
				put("TXNNUMBER", "交易总数图");
				put("TXNALERTRATE", "交易报警比例图");
				put("ALERTSUSNUMBER", "报警成功数图");
				put("SUSRATE", "报警成功率图");
				put("AVGTIME", "平均计算时间图");
				put("MAXTIME", "最大计算时间图");
				put("MINTIME", "最小计算时间图");
				put("BLOCKRATE", "阻断率图");
				put("HANGRATE", "挂起率图");
				put("AUTHRATE", "强认证率图");
				put("ALERTRATE", "告警率图");
				put("SUMALERTRATE", "报警占比图");
				put("TXNRATE", "交易占比图");
		       } 
	}; 
	public static final Map<String,String> ruleTargetMap = new HashMap<String,String>() { 
	       { 
	   		put("TRIGGERNUM", "交易执行次数图");
			put("HITNUM", "规则命中数图");
			put("HITRATE", "规则命中率图");
			put("AVGTIME", "平均运行时间图");
			put("MAXTIME", "最大运行时间图");
			put("MINTIME", "最小运行时间图");
			put("TRIGGERRATE", "交易执行占比图");
			put("HITNUMRATE", "命中占比图");
	       } 
	}; 

}
