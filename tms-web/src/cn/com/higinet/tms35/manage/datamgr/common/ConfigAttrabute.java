package cn.com.higinet.tms35.manage.datamgr.common;

/**
 * 配置文件的属性名
 * @author zhangfg
 *
 */
public class ConfigAttrabute {
	
	 public final static String TYPE_MODEL="model";
	 public final static String TYPE_TABLE="table";
	 public final static String TYPE_FILTER="filter";
	 public final static String TYPE_FILTERS="filters";
	 public final static String TYPE_REFERENCE="reference";
	 public final static String TYPE_REFERENCES="references";
	 public final static String DATAOPER_SERVICE = "dataOper_service";
	 
	 public final static String ID = "id";
	 public final static String NAME = "name";
	 public final static String STATUSKEY = "status_key";
	 public final static String STATUSDEL= "2";//表示删除状态的值
	 public final static String TABLE_NAME = "table_name";
	 public final static String TABLE_PK = "table_pk";
	 public final static String REFERENCE_FK = "reference_fk";
	 public final static String SOURCE = "source";
	 public final static String SOURCE_PARENT = "parent";
	 public final static String SQL_TYPE = "sql_type";
	 public final static String SQL_TYPE_VALUE_DAO = "1";
	 public final static String SQL_TYPE_VALUE_SQL = "2";
	 public final static String SQL_ID = "sql_id";
	 
	 public final static String PARAMETER_NAME = "parameter_name";
	 public final static String PARAMETER_VALUE = "parameter_value";
	 public final static String NOT_NULL = "not_null";
	 public final static String TYPE = "type";
	 public final static String FILTER_TYPE_VALUE_DEFAULT = "default";
	 public final static String FILTER_TYPE_VALUE_FK = "fk";
	 public final static String FILTER_TYPE_VALUE_CUSTOM = "custom";
	 
	 public final static String CUSTOM_CLASS = "custom_class";
	 public final static String CUSTOM_FUNC = "custom_func";
	 
	 //调用数据操作的方法标识
	 public final static String METHOD_DATASYNC = "dataSync";
	 public final static String METHOD_PACK = "pack";
	 public final static String METHOD_UNPACK = "unpack";
	 //reference条件数据的分隔符
	 public final static String REFERENCE_APLIT = ",";
	 
	 
	 //================以下为缓存更新配置属性================
	 public final static String METHOD = "method";
	 public final static String MODELID = "modelId";
	 public final static String MODELNAME = "modelName";
	 public final static String MID = "id";
	 public final static String MNAME = "name";
	 public final static String CACHEREFRESHSERVICE = "cacheRefreshService";
	 public final static String TXNID = "txnId";
	 public static final String TABLENAME = "tableName";
	 public static final String TABLEPK = "tablePk";
	 public static final String TABLEPKTYPE = "tablePkType";
	 public static final String SYNCPK = "syncPk";
	 public final static String QUERYPK = "queryPk";
	 public final static String OPERATE_DATA = "operateData";
	 public final static String REALOPER = "realOper";
	 public final static String ISAUTH = "isAuth";
	 public final static String ISREFRESH = "isRefresh";
	 public static final String REFRESHMSG = "refreshMsg";
	 public static final String DEPPKS = "depPks";
	 public static final String DEPMODELS = "depModels";
	 public static final String QUERYTABLENAME = "queryTableName";
	 public static final String QUERYTABLEPK = "queryTablePk";
	 
	 public static final String FILTER_TYPE_VALUE_MANUAL = "manual";
	 
}
