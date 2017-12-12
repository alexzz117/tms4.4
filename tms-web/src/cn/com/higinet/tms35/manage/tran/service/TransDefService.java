package cn.com.higinet.tms35.manage.tran.service;

import java.util.List;
import java.util.Map;

/**
 * 交易模型接口
 * @author yangk
 */
public interface TransDefService {

	public List<Map<String, Object>> getTranDefs();
	public List<Map<String, Object>> getTranRules();
	public Map<String, Object> getParent(String tab_name);
	public Map<String, Object> getNodeFullInfos(String tab_name);
	public List<Map<String, Object>> getFartherTranDef(String txnid);
	public List<Map<String, Object>> getFartherTranDefName(String txnid);
	public List<Map<String, Object>> getSameInfoDetail(String tab_desc, String tab_name, boolean isParentName, String type);
	public void checkSingleParentAndAllSubSame(String id, String tab_desc, String tab_name, boolean isParentName, String type, boolean isChg);
	/**
	 * 获取所有交易的主键和名称	added by wangsch
	 * @return
	 */
	public List<Map<String, Object>> getAllTxn();
	public List<Map<String, Object>> getSelfAndParentTranDef(String tab_name);
	public String getSelfAndParentTranDefAsStr(String tab_name);
	public Map saveTranDef(Map formList); 
}
