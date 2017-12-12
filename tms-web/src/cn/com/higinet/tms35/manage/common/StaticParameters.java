package cn.com.higinet.tms35.manage.common;

import java.util.HashMap;
import java.util.Map;

public class StaticParameters {
	/*
	 * 代码常量
	 */
	// 是否代码
	public static final String YES = "1";
	public static final String NO = "0";
	// 状态代码
	public static final String STATUS_0 = "0";// 停用
	public static final String STATUS_1 = "1";// 启用
	public static final String STATUS_2 = "2";// 删除

	// 运行监控常量
	public static final String MONITOR_TIME = "time";
	public static final String MONITOR_TODAY = "today";
	public static final String MONITOR_YESTERDAY = "yesterday";
	public static final String MONITOR_TOMORROW = "tomorrow";
	public static final String MONITOR_ALARM_TOTAL = "报警总数";
	public static final String MONITOR_ALARM_SERIOUS = "严重报警数";
	public static final String MONITOR_ALARM_HIGH = "高报警数";
	public static final String MONITOR_ALARM_MID = "中报警数";
	public static final String MONITOR_ALARM_LOW = "低报警数";

	public static final String MONITOR_ALARM_RATIO = "交易报警率";
	public static final String MONITOR_ALARM_SUCCESS_RATIO = "报警成功率";
	public static final String MONITOR_ALARM_HIT_RATIO = "规则命中率";

	public static final String MONITOR_RULE_TRIG = "规则触发量";
	public static final String MONITOR_RULE_HIT = "规则命中量";

	public static final String MONITOR_ALARM_SUCCESS = "成功报警数";
	public static final String MONITOR_ALARM_NORMAL = "无风险报警数";
	public static final String MONITOR_ALARM_REALMONITOR = "报警数据实时监控";
	public static final String MONITOR_ALARM_NON = "无";

	public static final String MONITOR_TXN_RUNMONITOR = "交易运行监控";
	public static final String MONITOR_TXN_TOTAL = "交易数";
	public static final String MONITOR_STAT_RISK = "风险统计数";
	public static final String MONITOR_ALARM_THREAD = "报警数趋势图";

	public static final String MONITOR_ALARMNUM = "alarmnum";// 报警数
	public static final String MONITOR_ALARMSUCNUM = "alarmsucnum";// 报警成功数
	public static final String MONITOR_ALARMTYPE = "alarmtype";// 报警成功数

	public static final String MONITOR_TOTAL = "总数";
	public static final String MONITOR_SERIOUS = "严重";
	public static final String MONITOR_HIGH = "高";
	public static final String MONITOR_MID = "中";
	public static final String MONITOR_LOW = "低";
	public static final String MONITOR_RISKFREE = "无风险";

	public static final String MONITOR_RULE_RUNMONITOR = "规则运行监控";
	public static final String MONITOR_RES_AVGTIME = "平均执行时间";
	public static final String MONITOR_RES_MINTIME = "最小执行时间";
	public static final String MONITOR_RES_MAXTIME = "最大执行时间";

	public static final String MONITOR_TYPE = "TYPE";
	public static final String MONITOR_CATEGORIES = "categories";
	public static final String MONITOR_DATASET = "dataset";
	public static final String MONITOR_TARGET = "target";

	public static final String MONITOR_RULEHITRATE = "rulehitrate";
	public static final String MONITOR_TRAFFIC = "traffic";
	public static final String MONITOR_ALARMSUC = "alarmsuc";

	public static final String MONITOR_REPORTTYPE = "reporttype";
	public static final String MONITOR_ALARM = "alarm";
	public static final String MONITOR_DIAL = "dial";
	public static final String MONITOR_DENOMINATOR = "denominator";// 分母
	public static final String MONITOR_NUMERATOR = "numerator";// 分子

	// 报文头 同步用
	public static final byte[] MSG_HEADER = { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',// 报文体长度
			'T', 'M', 'S', ' ', ' ', ' ', ' ', ' ',// 服务号
			'0', '0', '0', '5', // 交易号
			'X', 'M', 'L', ' ', // 报文体类型
			' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' // 返回码
	};
	
	// 报文头 删除用户缓存用
	public static final byte[] MSG_DELETE_CACHE_HEADER = { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',// 报文体长度
			'T', 'M', 'S', ' ', ' ', ' ', ' ', ' ',// 服务号
			'0', '0', '1', '1', // 交易号
			'X', 'M', 'L', ' ', // 报文体类型
			' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' // 返回码
	};

	// tms-sql key
	public static final String TMS_COMMON_SYSTIME = "tms.common.systime";
	public static final String TMS_COMMON_SEQUENCEID = "tms.common.sequenceid";

	// 宏观报表展示
	// 中国地图常量定义
	public static final String TMS_REPORT_CHINAMAP_MAPVALUE = "mapvalue";

	// 实时报表展示
	// 告警监控SQL语句
	public static final String S_TMS_MONITOR_ALERT_CHART = "tms.monitor.s_alert_chart";

	// 规则监控SQL语句
	public static final String S_TMS_MONITOR_RULE_TEMP_CHART = "tms.monitor.s_rule_temp_chart";
	public static final String S_TMS_MONITOR_RULE_HOUR_CHART = "tms.monitor.s_rule_hour_chart";
	public static final String S_TMS_MONITOR_RULE_DAY_CHART = "tms.monitor.s_rule_day_chart";

	// 实时监控-高风险交易
	public static final String S_TMS_MONITOR_MAP_TEMP_CHART = "tms.monitor.s_map_temp_chart";
	// public static final String S_TMS_MONITOR_MAP_REGION_CHART = "tms.monitor.s_map_region_chart";

	// 欺诈类型SQL语句
	public static final String S_TMS_MONITOR_FRAUD_TEMP_CHART = "tms.monitor.s_fraud_temp_chart";
	public static final String S_TMS_MONITOR_FRAUD_HOUR_CHART = "tms.monitor.s_fraud_hour_chart";
	public static final String S_TMS_MONITOR_FRAUD_DAY_CHART = "tms.monitor.s_fraud_day_chart";

	// 交易监控SQL语句
	public static final String S_TMS_MONITOR_TXN_TEMP_CHART = "tms.monitor.s_txn_temp_chart";
	public static final String S_TMS_MONITOR_TXN_HOUR_CHART = "tms.monitor.s_txn_hour_chart";
	public static final String S_TMS_MONITOR_TXN_DAY_CHART = "tms.monitor.s_txn_day_chart";

	// 风险统计SQL语句
	public static final String S_TMS_MONITOR_RISK_TEMP_CHART = "tms.monitor.s_risk_temp_chart";
	public static final String S_TMS_MONITOR_RISK_HOUR_CHART = "tms.monitor.s_risk_hour_chart";
	public static final String S_TMS_MONITOR_RISK_DAY_CHART = "tms.monitor.s_risk_day_chart";
	// 系统时间查询返回map字段
	public static final String DBSYSTIME = "DBSYSTIME";
	public static final String SEQUENCEID = "SEQUENCEID";

	// 系统中用到的server.properties的参数定义

	// 授权的方法和标识
	public static final String AUTH_METHOD_NAME = "updateAuth";
	public static final String AUTH_AGREE_FLAG = "AUTHOR_STATUS";
	public static final String AUTH_STATUS_0 = "0";// 待授权
	public static final String AUTH_STATUS_1 = "1";// 通过
	public static final String AUTH_STATUS_2 = "2";// 未通过
	public static final String AUTH_STATUS_3 = "3";// 无需授权
	public static final String AUTH_STATUS_4 = "4";// 正在授权
	public static final String AUTH_STATUS_9 = "9";// 删除

	/*
	 * modify by yangk on 2013/4/26 add table type
	 */
	// 物理表
	public static final String TAB_TYPE_1 = "1";
	// 结构
	public static final String TAB_TYPE_2 = "2";
	// TMS视图
	public static final String TAB_TYPE_3 = "3";
	// 交易视图
	public static final String TAB_TYPE_4 = "4";

	/**
	 * 选中
	 */
	public static final String checked = "1";
	public static final String rule_istest = "on";

	public static final String SIGNPOST = " > ";

	public static final int STATID_INDEX = 0;
	public static final int UPVALUE_INDEX = 1;
	public static final int STARTDATE_INDEX = 2;
	public static final int ENDDATE_INDEX = 3;
	public static final int UPID_INDEX = 4;

	public static final Map<String, String> ERROR = new HashMap<String, String>();
	static {
		ERROR.put("000001", "已有模型正在训练，请稍后！");
		ERROR.put("000002", "该交易未配置使用模型的字段！");
		ERROR.put("999999", "未知异常，请联系管理员查看日志！");
	}

	public static final int HEAD_LEN = 32; // 返回报文的长度，根据该变量从返回的报文中截取报文头
	public static final int MESSAGE_LEN_LEN = 0;// 标识返回报文的开始位置，根据该变量和报文头长度的和，开始截取报文体
	public static final String MESSAGE_TYPE = "XML";// 报文头中标识报文的类型
	public static final String MESSAGE_ERROR = "02009998";// 风控返回报文解析错误
	public static final String SYSTEM_SUCCESS = "02000000";// 风险评估成功
	public static final String RISK_EVAL_CODE = "0002";// 风险评估交易码

	public static final String SHORT_ACTION_PASS = "trans_pass";// 交易放行
	public static final String SHORT_ACTION_BLOCK = "trans_block";// 交易阻断
	public static final String SHORT_ACTION_CASE = "go_case";// 转风险案件调查
	
	/**
	 * 风险评估服务接口业务码枚举
	 * @author lining
	 *
	 */
	public enum ActionCode {
		UPDATE_SIGN_DATA("0006");

		private String code;

		ActionCode(String code){
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}
}