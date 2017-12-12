package cn.com.higinet.tms35.manage.auth.common;

public class AuthStaticParameters {
	//查询方式
	public static final String SELECTTYPE_SQL = "sql";
	public static final String SELECTTYPE_TABLE = "table";
	//授权的方法和标识
	public static final String AUTH_STATUS_0 = "0";//待授权
	public static final String AUTH_STATUS_1 = "1";//通过
	public static final String AUTH_STATUS_2 = "2";//未通过
	public static final String AUTH_STATUS_3 = "3";//无需授权
	public static final String AUTH_STATUS_4 = "4";//正在授权
	public static final String AUTH_STATUS_9 = "9";//删除
	
	public static final String IS_MAIN_TRUE = "1";//是初始操作
	public static final String IS_MAIN_FALSE = "0";//不是初始操作
	
	//主表及模块名称
	public static final String [][] TABLENAME = {
		{"tranConf","交易配置"},
		//{"tranSwitch","交易开关"},
		//{"tranProcess","交易处置"},
		{"tranStat","交易统计"},
		//{"tranAction","交易动作"},
		{"tranRule","交易规则"},
		{"tranSt","交易策略"},
		//{"ruleChild","规则路由"},
		{"roster,rosterconvert","名单管理"}/*,
		{"userpattern","用户行为习惯管理"}*/
	};
}
