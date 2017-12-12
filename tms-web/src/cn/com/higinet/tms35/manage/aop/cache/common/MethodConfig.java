package cn.com.higinet.tms35.manage.aop.cache.common;


public class MethodConfig {
	private String modelId;
	private String modelName;
	private String id;
	private String name;
	private String cacheRefreshService;
	private String txnId;
	private String tableName;
	private String queryTableName;
	private String operateData;
	private String realOper;
	private String isAuth;
	private String isRefresh;
	private String refreshMsg;
	private String depModels;
	private String queryTablePk;
	private String tablePk;
	private String tablePkType;
	private String syncPk;
	private String depPks;
	
	public String getQueryTableName() {
		return queryTableName;
	}
	public void setQueryTableName(String queryTableName) {
		this.queryTableName = queryTableName;
	}
	public String getQueryTablePk() {
		return queryTablePk;
	}
	public void setQueryTablePk(String queryTablePk) {
		this.queryTablePk = queryTablePk;
	}
	public String getTablePkType() {
		return tablePkType;
	}
	public void setTablePkType(String tablePkType) {
		this.tablePkType = tablePkType;
	}
	public String getDepModels() {
		return depModels;
	}
	public void setDepModels(String depModels) {
		this.depModels = depModels;
	}
	public String getRefreshMsg() {
		return refreshMsg;
	}
	public void setRefreshMsg(String refreshMsg) {
		this.refreshMsg = refreshMsg;
	}
	public String getModelId() {
		return modelId;
	}
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCacheRefreshService() {
		return cacheRefreshService;
	}
	public void setCacheRefreshService(String cacheRefreshService) {
		this.cacheRefreshService = cacheRefreshService;
	}
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getTablePk() {
		return tablePk;
	}
	public void setTablePk(String tablePk) {
		this.tablePk = tablePk;
	}
	public String getSyncPk() {
		return syncPk;
	}
	public void setSyncPk(String syncPk) {
		this.syncPk = syncPk;
	}
	public String getOperateData() {
		return operateData;
	}
	public void setOperateData(String operateData) {
		this.operateData = operateData;
	}
	public String getRealOper() {
		return realOper;
	}
	public void setRealOper(String realOper) {
		this.realOper = realOper;
	}
	public String getIsAuth() {
		return isAuth;
	}
	public void setIsAuth(String isAuth) {
		this.isAuth = isAuth;
	}
	public String getIsRefresh() {
		return isRefresh;
	}
	public void setIsRefresh(String isRefresh) {
		this.isRefresh = isRefresh;
	}
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public String getDepPks() {
		return depPks;
	}
	public void setDepPks(String depPks) {
		this.depPks = depPks;
	}
}
