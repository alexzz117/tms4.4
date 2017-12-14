/*
 * Copyright © 2012 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.common;

/**
 * 
 * @author zhangfg
 * @version 2.2.0
 * @date 2012-06-21
 * @description 静态常量类，定义接口中使用到的常量
 *
 */
public class StaticParameter {

	/**
	 * 风险确认接口编号
	 */
	public static final String RISK_CONFIRM = "0001";
	/**
	 * 风险评估接口编号
	 */
	public static final String RISK_EVALUAT = "0002";

	/**
	 * 批量风险确认接口编号
	 */
	public static final String RISK_CONFIRM_BATCH = "0003";
	/**
	 * 批量风险评估接口编号
	 */
	public static final String RISK_EVALUAT_BATCH = "0004";
	/**
	 * 刷新缓存接口编号
	 */
	public static final String CACHE_REFRESH = "0005";

	/**
	 * 更新签约数据接口编号
	 */
	public static final String UPDATE_SIGN_DATA = "0006";

	/**
	 * 获取指定的统计值接口编号
	 */
	public static final String GET_STAT_VALUE = "0008";

	/**
	 * 风险评估服务监听接口编号
	 */
	public static final String RISK_MONITOR = "0099";
	/**
	 * 风险评估服务获取IP接口
	 */
	public static final String RISK_MONITOR_SERVERS = "0098";

	public static final String CHANNEL_DATE_PATTEN = "channel.date.patten";

	// 返回错误代码

	public static final String SYSTEM_SUCCESS = "02000000";// 风险评估成功
	public static final String ERROR_MESSAGE = "02000001";// 报文数据异常
	public static final String ERROR_NOSIGNATURE = "02000201";// 交易未识别
	public static final String ERROR_NOTXN = "02003001";// 没有此交易
	public static final String ERROR_NONEEDCONFIRM = "02003002";// 交易无需确认
	public static final String ERROR_DISABLED = "02003003";// 交易未启用
	public static final String ERROR_NOSESSION = "02004001";// 没有此会话
	public static final String ERROR_NOUSER = "02004002";// 没有此用户

	public static final String TMS_SERVER_ERROR = "02009002";// TMS服务器异常。调用TMS服务器，向其发送报文失败
	public static final String TMS_SERVICE_TIMEOUT = "02009003";// 调用业务监控系统超时
	public static final String TMS_API_ERROR = "02009004";// API接口开关关闭
	public static final String TMS_DATA_ERROR = "02009005";// 数据不全
	public static final String TMS_API_FAILD = "02009006";// 调用API接口失败
	public static final String TMS_CONFIRM_NOTRANS = "02009007";// 风险确认找不到交易数据
	public static final String MESSAGE_ERROR = "02009998";// 风控返回报文解析错误
	public static final String SYSTEM_ERROR = "02009999";// 风险评估异常

	/**
	 * 未知平台异常
	 */
	public static final String ERR_UNKNOWN = "00999999";
	/**
	 * 未知处理器异常
	 */
	public static final String ERR_SERVICE_HANDLER_UNKNOWN = "00009999";
	/**
	 * 未知服务异常
	 */
	public static final String ERR_SERVICE_UNKNOWN = "00001999";
	/**
	 * 服务没找到
	 */
	public static final String ERR_SERVICE_NOFOUND = "00001404";
	/**
	 * 解码处理器解码异常
	 */
	public static final String ERR_SERVICE_HANDLER_DECODE = "00000911";
	/**
	 * 编码处理器编码异常
	 */
	public static final String ERR_SERVICE_HANDLER_ENCODE = "00000912";

	// 异步调用风险接口标识
	public static final String RISK_INTERFACE_EVALUATE = "riskEvaluate";
	public static final String RISK_INTERFACE_CONFIRM = "riskConfirm";
	public static final String RISK_INTERFACE_BATCH_EVALUATE = "riskBatchEvaluate";
	public static final String RISK_INTERFACE_BATCH_CONFIRM = "riskBatchConfirm";
	public static final String RISK_INTERFACE_BATCH_TRANCONFIRM = "riskBatchTransConfirm";

	public static final int HEAD_LEN = 32; // 返回报文的长度，根据该变量从返回的报文中截取报文头
	public static final int MESSAGE_LEN_LEN = 0;// 标识返回报文的开始位置，根据该变量和报文头长度的和，开始截取报文体
	public static final String MESSAGE_TYPE = "XML";// 报文头中标识报文的类型

	// 返回报文的节点名称
	public static final String NODE_NAME_TXNID = "txnId";
	public static final String NODE_NAME_TXNNAME = "txnName";
	public static final String NODE_NAME_TRANSCODE = "transcode";
	public static final String NODE_NAME_BACKCODE = "backCode";
	public static final String NODE_NAME_DISPOSAL = "disposal";
	public static final String NODE_NAME_SCORE = "score";
	public static final String NODE_NAME_ERRORINFO = "errorInfo";
	public static final String NODE_NAME_RISKID = "riskId";
	public static final String NODE_NAME_HIT_RULE_NUM = "hitRuleNum";
	public static final String NODE_NAME_TRIG_RULE_NUM = "trigRuleNum";
	public static final String NODE_NAME_PROCESS_INFO = "processInfo";
	public static final String NODE_NAME_SWITCH_INFO = "switchInfo";
	public static final String NODE_NAME_ACTION_INFO = "actionInfo";
	public static final String NODE_NAME_HIT_RULES = "hitRules";
	public static final String NODE_NAME_DEVTOKEN = "devToken";
	public static final String NODE_NAME_COOKIENAME = "cookieName";

	// 返回报文中标识风险结果节点的名称
	public static final String RISK_NODE_NAME = "txn";
	public static final String ROOT_ELEMENT_NAME = "Message";
	public static final String ELEMENT_ATTRBUTE_TYPE = "type";

	// 配置文件路径
	/**
	 * 配置文件properties的路径,added by wujw 20150922
	 */
	public static final String TMS_API_PATH = "TMS_API_PATH";
	public static final String FILTERCONFIG_PATH = "/filterConfig.xml";
	public static final String PROPERTIES_PATH_TMS = "/tmsServer.properties";
	public static final String PROPERTIES_PATH_LOG = "/tmsLog.properties";
	public static final String PRO_SERVER_IP_NAME = "tms.server.ip";
	public static final String PRO_SERVER_PORT_NAME = "tms.server.port";
	public static final String PRO_SERVER_TIMEOUT = "timeOut";
	public static final String PRO_SERVER_CONNECT_TIMEOUT = "connect.timeOut";
	public static final String PRO_SERVER_TRANSPIN_FLAG = "transpin.isOn";
	public static final String PRO_SERVER_RESEND_TIMES = "resend.times";
	public static final String PRO_SERVER_CHECK_PRIOD = "check.priod";
	public static final String PRO_SERVER_MAX_ERROR_COUNT = "tms.server.max.error.count";
	public static final String PRO_SERVER_NUM = "tms.server.num";
	public static final String PRO_SERVER_FOUND_NUM = "tms.server.found.num";
	public static final String PRO_SERVER_SUCC_F_NUM = "tms.server.succ.f.num";
	public static final String PRO_SERVER_SUCC_F_ON = "tms.server.succ.f.isON";
	public static final int TIMEOUT_VALUE = 100;
	// xml解析器类型名称在properties文件中的配置项
	public static final String XML_PARSER_NAME = "xml.parser.name";
	public static final String ALLOW_DISPOSAL = "tms.disposal.allow";
	public static final String XML_PARSER_SAX = "sax";
	public static final String XML_PARSER_OPEN4J = "open4j";
	public static final String XML_PARSER_VTD = "vtd";

	// 风险等级标识
	public static final String TMS_MGR_SYSPARAM_HIGN = "high"; // 风险等级：高
	public static final String TMS_MGR_SYSPARAM_MID = "mid"; // 风险等级：中
	public static final String TMS_MGR_SYSPARAM_LOW = "low"; // 风险等级：低
	public static final String TMS_MGR_SYSPARAM_NORMAL = "normal"; // 风险等级：正常

	// filter中的配置节点及值

	public static final String RISK_TYPE_DEF_NAME = "defRiskType";
	public static final String RISK_TYPE_BEFORE_NAME = "beforRiskTransList";
	public static final String RISK_TYPE_BEFORE_VALUE = "before";
	public static final String RISK_TYPE_AFTER_NAME = "afterRiskTransList";
	public static final String RISK_TYPE_AFTER_VALUE = "after";
	public static final String RISK_RESULTKEY = "riskResultKey";
	// fileter实现类路径在配置文件中的项名
	public static final String MESSAGE_FILTER = "message.filter.class";
	// fileter中需要调用api接口的交易实现类路径在配置文件中的项名
	public static final String MESSAGE_FILTER_API = "message.filter.api.class";
	// fileter中需要调用api接口的交易在配置文件中的项名
	public static final String MESSAGE_FILTER_API_TRANS = "message.filter.api.trans";

	public static final String HTTPSESSION = "HttpSession";
	public static final String HTTPCOOKIE = "HttpCookie";
	public static final String HTTPHEAD = "HttpHead";
	public static final String HTTPPARAM = "HttpParam";
	public static final String HTTPATTRIBUTE = "HttpAttribut";
	public static final String HTTPOTHER = "HttpOther";

	public static final String ALL = "all";
	public static final String NONE = "none";
	public static final String AND = "and";
	public static final String OR = "or";

	// 线程配置的相关属性
	public static final String DISPATCH_COUNT = "dispatch.count";
	public static final String DISPATCH_QUEUECAPACITY = "dispatch.queueCapacity";
	public static final String GETTER_BATCHCOUNT = "getter.batchCount";

	// web.xml中与过滤器相关的属性名
	public static final String TMS_SERVER_IP = "serverIp";
	public static final String TMS_SERVER_PORT = "port";
	public static final String TMS_SERVER_TIMEOUT = "timeOut";
	public static final String TMS_SERVER_CONNECT_TIMEOUT = "connect.timeOut";
	public static final String TMS_SERVER_TRANSPIN_FLAG = "transpin.isOn";

	// filterConfig.xml中配置的属性名
	public static final String FILTER_ROOT = "trans";
	public static final String TRANS_TRANSCODE = "transCode";
	public static final String TRANS_TRANSNAME = "transName";
	public static final String TRANS_USEFLAG = "useFlag";
	public static final String TRANS_FILTERTYPE = "filterType";
	public static final String TRANS_CONVERSIONCLASS = "conversionClass";

	public static final String TRANS_RULES = "rules";
	public static final String TRANS_RULE = "rule";
	public static final String TRANS_RULE_PARAM = "param";
	public static final String TRANS_RULE_FROME = "from";
	public static final String TRANS_RULE_EXPRESSION = "expression";
	public static final String TRANS_RULE_VALUE = "value";
	public static final String TRANS_RULE_CONDITION = "condition";

	public static final String TRANS_ENTITY_ROOT = "root";
	public static final String TRANS_ENTITY_TXN = "TransInfo";
	public static final String TRANS_ENTITY_USER = "User";
	public static final String TRANS_ENTITY_DEVICE = "Device";

	// filterConfig.xml中配置的属性值
	public static final String TRANS_RULE_FROM_REQ = "request";
	public static final String TRANS_RULE_FROM_HEAD = "head";
	public static final String TRANS_RULE_FROM_COOKIE = "cookie";
	public static final String TRANS_RULE_FROM_URI = "uri";
	public static final String TRANS_RULE_FROM_URL = "url";
	public static final String TRANS_RULE_FROM_ATTR = "attr";
	public static final String TRANS_RULE_FROM_PARAM = "param";
	public static final String TRANS_RULE_EXPRE_EQUARY = "equal";
	public static final String TRANS_RULE_EXPRE_NOEQUARY = "noEqual";
	public static final String TRANS_RULE_EXPRE_LIKE = "like";
	public static final String TRANS_RULE_PARAM_NULL = "null";

	public static final String TRANS_HTTP_ATTR_SCOPE = "scope";
	public static final String TRANS_HTTP_TAG_PARAM = "param";
	public static final String TRANS_HTTP_PARAM_FROMNAME = "fromName";
	public static final String TRANS_HTTP_PARAM_TONAME = "toName";

	public static final String CHANNEL_TYPE_EBANK = "01";

	public static final String MONITOR_TMS_SERVER_LIST = "TMS_SERVER_LIST";
	public static final String MONITOR_TMS_SERVERS = "TMS_SERVERS";

	/**
	 * 
	 */
	/**
	 * 交易状态: 失败
	 */
	public static final String TXN_STATUS_FAIL = "0";
	/**
	 * 交易状态: 成功
	 */
	public static final String TXN_STATUS_SUCCESS = "1";
	/**
	 * 交易状态: 处理中
	 */
	public static final String TXN_STATUS_INPROCESS = "2";

	public static final int RESULT_FLAG_IOEXCEPTION = 3;// IO异常
	public static final int RESULT_FLAG_SERVER_RETURN_ERROR = 5;// 服务器返回异常
	public static final int RESULT_FLAG_NOTXN = 9;// 没有找到交易数据
	public static final int RESULT_FLAG_READTIMEOUT = 10;// 读超时
	public static final int RESULT_FLAG_WRITETIMEOUT = 11;// 写超时
}
