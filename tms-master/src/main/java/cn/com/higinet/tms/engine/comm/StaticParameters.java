package cn.com.higinet.tms.engine.comm;

import java.util.HashMap;
import java.util.Map;


public class StaticParameters {

	public static final String HEALTH_TMS_SERVER_LIST		= "TMS_SERVER_LIST";//获取风控服务的服务列表的标识
	public static final String HEALTH_TMS_SERVER_TYPE		= "TMS_SERVER_TYPE";//风控服务的类型
	//返回错误代码
	
	public static final String SYSTEM_SUCCESS 				= "02000000";//风险评估成功
	public static final String ERROR_MESSAGE 				= "02000001";//报文数据异常
	public static final String ERROR_NOSIGNATURE 			= "02000201";//交易未识别
	public static final String ERROR_NOTXN 					= "02003001";//没有此交易
	public static final String ERROR_NONEEDCONFIRM 			= "02003002";//交易无需确认
	public static final String ERROR_DISABLED	 			= "02003003";//交易未启用
	public static final String ERROR_NOSESSION 				= "02004001";//没有此会话
	public static final String ERROR_NOUSER 				= "02004002";//没有此用户
	
	public static final String TMS_SERVER_ERROR 			= "02009002";//TMS服务器异常。调用TMS服务器，向其发送报文失败
	public static final String TMS_SERVICE_TIMEOUT          = "02009003";//调用业务监控系统超时
	public static final String TMS_API_ERROR 				= "02009004";//API接口开关关闭
	public static final String TMS_DATA_ERROR 				= "02009005";//数据不全
	public static final String TMS_API_FAILD 				= "02009006";//调用API接口失败
	public static final String TMS_CONFIRM_NOTRANS			= "02009007";//风险确认找不到交易数据
	public static final String MESSAGE_ERROR 				= "02009998";//风控返回报文解析错误
	public static final String SYSTEM_ERROR 				= "02009999";//风险评估异常
	
	/**
	 * 未知平台异常
	 */
	public static final String ERR_UNKNOWN 					= "00999999";
	/**
	 * 未知处理器异常
	 */
	public static final String ERR_SERVICE_HANDLER_UNKNOWN 	= "00009999";
	/**
	 * 未知服务异常
	 */
	public static final String ERR_SERVICE_UNKNOWN 			= "00001999";
	/**
	 * 服务没找到
	 */
	public static final String ERR_SERVICE_NOFOUND 			= "00001404";
	/**
	 * 解码处理器解码异常
	 */
	public static final String ERR_SERVICE_HANDLER_DECODE	= "00000911";
	/**
	 * 编码处理器编码异常
	 */
	public static final String ERR_SERVICE_HANDLER_ENCODE	= "00000912";
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
	public static final String TXN_STATUS_INHAND = "2";
	
	
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
	 * 配置缓存刷新接口编号
	 */
	public static final String CONFIG_CACHE_REFRESH = "0005";
	/**
	 * 更新签约数据接口编号
	 */
	public static final String UPDATE_SIGN_DATA = "0006";
	/**
	 * 签约数据缓存刷新接口编号
	 */
	public static final String SIGN_CACHE_REFRESH = "0008";
	/**
	 * http风险确认接口编号
	 */
	public static final String HTTP_RISK_CONFIRM = "1001";
	/**
	 * http风险评估接口编号
	 */
	public static final String HTTP_RISK_EVALUAT = "1002";
	
	/**
	 * http批量风险确认接口编号
	 */
	public static final String HTTP_RISK_CONFIRM_BATCH = "1003";
	/**
	 * http批量风险评估接口编号
	 */
	public static final String HTTP_RISK_EVALUAT_BATCH = "1004";
	/**
	 * 健康检查接口
	 */
	public static final String RISK_HEALTH_CHECK = "0098";

	/**
	 * 加密密钥
	 */
	public static final String KEY = "Q51ScCB/rcE=";
	
	//返回错误代码
	public static final String TMS_JSON_ERROR 				= "02009007";//JSON格式错误
	

	public static final String TMS_HTTP_ERROR				= "03000000";//风控云服务接口错误
	
	public static final String TMS_HTTP_JSONFORMATERROR		= "03001001";//交易数据格式错误
	public static final String TMS_HTTP_TRANSACTIONNULL		= "03001002";//交易数据为空
	public static final String TMS_HTTP_READCONFIGERROR		= "03001003";//读取配置文件异常
	
	public static final String TMS_HTTP_TRANSDATEERROR		= "03002001";//转换交易时间异常
	public static final String TMS_HTTP_DEVICEDECODEERROR	= "03002002";//设备信息解密异常
	public static final String TMS_HTTP_ENCRYERROR			= "03002003";//交易信息SM3加密密异常

	public static final String TMS_HTTP_RELIABILITYERROR	= "03003001";//获取可信度异常


	public static final String TMS_HTTP_RISKNULL			= "03009999";//风险评估结果为空
	
	
	public static Map<String, String> errorInfoMap = new HashMap<String, String>();
	static
	{
		errorInfoMap.put(StaticParameters.ERROR_MESSAGE, "报文数据异常。");
		errorInfoMap.put(StaticParameters.ERROR_NOSIGNATURE, "交易未识别。");
		errorInfoMap.put(StaticParameters.ERROR_NOTXN, "没有此交易。");
		errorInfoMap.put(StaticParameters.ERROR_DISABLED, "交易未启用。");
		errorInfoMap.put(StaticParameters.ERROR_NONEEDCONFIRM, "交易无需确认。");
		errorInfoMap.put(StaticParameters.ERROR_NOSESSION, "没有此会话。");
		errorInfoMap.put(StaticParameters.ERROR_NOUSER, "没有此用户。");
		errorInfoMap.put(StaticParameters.TMS_SERVER_ERROR, "TMS服务器异常。调用TMS服务器，向其发送报文失败。");
		errorInfoMap.put(StaticParameters.TMS_SERVICE_TIMEOUT, "调用业务监控系统超时。");
		errorInfoMap.put(StaticParameters.TMS_API_ERROR, "API接口开关关闭。");
		errorInfoMap.put(StaticParameters.TMS_DATA_ERROR, "数据不全。");
		errorInfoMap.put(StaticParameters.SYSTEM_ERROR, "风险评估异常");
		errorInfoMap.put(StaticParameters.ERR_UNKNOWN, "未知平台异常。");
		errorInfoMap.put(StaticParameters.ERR_SERVICE_HANDLER_UNKNOWN, "未知处理器异常。");
		errorInfoMap.put(StaticParameters.ERR_SERVICE_UNKNOWN, "未知服务异常。");
		errorInfoMap.put(StaticParameters.ERR_SERVICE_NOFOUND, "服务没找到。");
		errorInfoMap.put(StaticParameters.ERR_SERVICE_HANDLER_DECODE, "解码处理器解码异常。");
		errorInfoMap.put(StaticParameters.ERR_SERVICE_HANDLER_ENCODE, "编码处理器编码异常。");
		errorInfoMap.put(StaticParameters.SYSTEM_SUCCESS, "风险评估成功。");

		errorInfoMap.put(StaticParameters.TMS_HTTP_ERROR, "风控云服务接口错误。");
		errorInfoMap.put(StaticParameters.TMS_HTTP_RISKNULL, "风险评估结果为空。");
		errorInfoMap.put(StaticParameters.TMS_HTTP_JSONFORMATERROR, "交易数据格式错误。");
		errorInfoMap.put(StaticParameters.TMS_HTTP_TRANSACTIONNULL, "交易数据为空。");
		errorInfoMap.put(StaticParameters.TMS_HTTP_READCONFIGERROR, "读取配置文件异常。");
		
		
		errorInfoMap.put(StaticParameters.TMS_HTTP_TRANSDATEERROR, "转换交易时间异常。");
		errorInfoMap.put(StaticParameters.TMS_HTTP_RELIABILITYERROR, "获取可信度异常。");
		errorInfoMap.put(StaticParameters.TMS_HTTP_DEVICEDECODEERROR, "设备信息解密异常。");
		
		
	}
	

	/**
	 * 配置文件名称
	 */
	public static final String PROPERTIES_PATH = "/config.properties";
	/**
	 * 配置文件中微通新成服务云地址节点名称
	 */
	public static final String MICRODONEURL = "microdone.service.url";

	/**
	 * 将风险评估结果放到request中的属性名称
	 */
	public static final String RESULTJSON = "resultJson";

	/**
	 * 配置文件中微通新成云服务返回的可信度节点名称
	 */
	public static final String RELIABILITY = "reliabilityName";
	
	
	public static String USEFLAG = "useFlag";
	
	public static String ACTIONCODE = "actionCode";
	
	public static String SYNCFLAG = "syncFlag";
	
	/**
	 * 交易属性
	 */
	public static class Transaction{
		
		public static String TRANSACTIONS = "transactions";
		
		public static String TRANSACTION = "transaction";
		/**
		 * 交易流水号
		 */
		public static String TRANSCODE = "TXNCODE" ;
		/**
		 * 交易时间(年-月-日 时:分:秒)
		 */
		public static String TRANSTIME = "TXNTIME";
		/**
		 * 交易类型
		 */
		public static String TRANSTYPE = "TXNID";
		/**
		 * 交易状态(2 正在处理;1 交易成功;0交易失败)
		 */
		public static String TRANSSTATUS = "TXNSTATUS";
		/**
		 * 网银交易异常代码
		 */
		public static String EXPCODE = "expCode";
		/**
		 * 认证状态(2 正在处理;1 认证成功;0认证失败)
		 */
		public static String AUTHSTATUS = "authStatus";
		/**
		 * 客户端渠道类型(01 个人网银;02 企业网银)
		 */
		public static String CHANNELTYPE = "CHANCODE";
		/**
		 * 用户ID(客户号)
		 */
		public static String CSTNO = "USERID";
		/**
		 * 会话ID
		 */
		public static String SESSIONID = "SESSIONID";
		/**
		 * MAC地址/SIM卡编号
		 */
		public static String DEVICEID = "DEVICETOKEN";
		/**
		 * 击键行为相似度
		 */
		public static String KEYSTROKESIMI = "keyStrokeSimi";
		/**
		 * ip地址
		 */
		public static String IPADDRESS = "IPADDR";
		/**
		 * 客户签约信息
		 */
		public static String CUSTOMER = "customer";
		/**
		 * 设备指纹信息
		 */
		public static String DEVICEFINGER = "DEVICEFINGER";
		/**
		 * 设备标记
		 */
		public static String DEVICETOKEN = "DEVICETOKEN";
		/**
		 * 其它信息
		 */
		public static String EXTINFO = "extInfo";
		
		/**
		 * 设备信息加密串
		 */
		public static String DEVICEINFO = "deviceInfo";
		
		/**
		 * 设备相似度数据项
		 */
		public static String RELIABILITY = "devSimi";
		
		/**
		 * 击键行为特征
		 */
		public static String CLICKTYPE = "keyStrokeSimi";
	}
	
	/**
	 * 批量交易属性
	 * 
	 */
	public static class Batch{
		public static String BATCH = "batch";
		
		//批次号
		public static String BATCHNO = "batchNo";
		//批次别名
		public static String BATCHNAME = "batchName";
		//总金额
		public static String TOTALAMOUNT = "totalAmount";
		//总笔数
		public static String TOTALCOUNT = "totalCount";
		//批量付款账号
		public static String PAYACCOUNT = "payAccount";
		//交易时间(年-月-日 时:分:秒)
		public static String TRANSTIME = "transTime";
		//交易类型，表示具体执行的交易
		public static String TRANSTYPE = "transType";
		//交易状态：	2 正在处理	1 认证成功	0认证失败
		public static String TRANSSTATUS = "transStatus";
		//客户端渠道类型	01 个人网银	02 企业网银
		public static String CHANNELTYPE = "channelType";
		//IP地址
		public static String IPADDRESS = "ipAddress";
		//用户ID(客户号)
		public static String CSTNO = "cstNo";
		//会话ID
		public static String SESSIONID = "sessionId";
	}
	
	public static class Customer{
		//客户号
		public static String CSTNO = "cstNo";
		//用户名（登录用）
		public static String USERNAME = "userName";
		//客户姓名
		public static String CUSNAME = "cusName";
		//证件类型
		public static String CTFTYPE = "ctfType";
		//证件号码
		public static String CTFNO = "ctfNo";
		//联系电话
		public static String PHONE = "phone";
		//开户行
		public static String OPENNODE = "openNode";
		//性别
		public static String SEX = "sex";
		//出生年月
		public static String BIRTHDAY = "birthday";
		//客户类型
		public static String ACCTYPE = "accType";
		//开户时间
		public static String OPENTIME = "openTime";
		//客户账户信息
		public static String ACCOUNT = "account";
	}
	
	public static class Account{
		public static String ACCOUNT = "account";
		//账号
		public static String ACCNO = "accNo";
		//网银客户号
		public static String CSTNO = "cstNo";
		//开户网点
		public static String OPENNODE = "openNode";
		//开户时间
		public static String OPENTIME = "openTime";
		//开户城市
		public static String OPENNODECITY = "openNodeCity";
		//余额
		public static String BALANCE = "balance";
	}
	/**
	 * 用户属性
	 * 
	 */
	public static class User{
		
		public static String USER = "user";
		//客户号
		public static String CSTNO = "cstNo";
		//用户名（登录用）
		public static String USERNAME = "userName";
		//客户姓名
		public static String CUSNAME = "cusName";
		//证件类型
		public static String CTFTYPE = "ctfType";
		//证件号码
		public static String CTFNO = "ctfNo";
		//联系电话
		public static String PHONE = "phone";
		//开户行
		public static String OPENNODE = "openNode";
		//性别
		public static String SEX = "sex";
		//出生年月
		public static String BIRTHDAY = "birthday";
		//客户类型
		public static String ACCTYPE = "accType";
		//开户时间
		public static String OPENTIME = "openTime";
		//客户账户信息
		public static String ACCOUNTS = "accounts";
		
	}
	
	//返回报文中标识风险结果节点的名称
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
	public static final String RISK_NODE_NAME = "txn";
	public static final String ROOT_ELEMENT_NAME = "Message";
	public static final String ELEMENT_ATTRBUTE_TYPE = "type";
	
	// 缓存刷新返回信息
	public static final String REFRESH_CACHE_ERROR_INFO = "ERRORINFO";
	// 缓存刷新返回代码
	public static final String REFRESH_CACHE_ERROR_CODE = "ERRORCODE";
	// 缓存刷新参数名
	public static final String REFRESH_PARAM_NAME = "tableName";
	
	// 微通新成认证接口返回结果
	public static Map<String, String> MDAUTHRESULT = new HashMap<String, String>();
	static {
		MDAUTHRESULT.put("01", "apps_id为空");
		MDAUTHRESULT.put("02", "app_type为空");
		MDAUTHRESULT.put("03", "random_num为空");
		MDAUTHRESULT.put("04", "user_name为空");
		MDAUTHRESULT.put("05", "user_finger为空");
		MDAUTHRESULT.put("06", "user_info为空");
		MDAUTHRESULT.put("07", "user_ip为空");
		MDAUTHRESULT.put("08", "data_hash为空");
		MDAUTHRESULT.put("11", "data_hash不正确");
		MDAUTHRESULT.put("12", "user_finger解密失败");
		MDAUTHRESULT.put("13", "user_info解密失败");
		MDAUTHRESULT.put("14", "user_finger数据异常");
		MDAUTHRESULT.put("21", "user_finger认证成功(键盘习惯认证成功)");
		MDAUTHRESULT.put("22", "user_finger认证失败(键盘习惯认证失败)");
		MDAUTHRESULT.put("23", "该用户暂未生成键盘数据模型，无法认证键盘习惯");
		MDAUTHRESULT.put("24", "本次数据请求为重放攻击");
		}
}
