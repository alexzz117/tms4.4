package cn.com.higinet.tms.manager.modules.switcher.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.modules.common.CommonCheckService;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.SequenceService;
import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_SWITCH;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.switcher.service.SwitchService;
import cn.com.higinet.tms.manager.modules.tran.service.TransDefService;

@SuppressWarnings("all")
@Service("switchService")
public class SwitchServiceImpl implements SwitchService{
	
	private static Log log = LogFactory.getLog(SwitchServiceImpl.class);
	
	@Autowired
	private CommonCheckService commonCheckService;
	@Autowired
	private TransDefService transDefService;
	@Autowired
	@Qualifier("tmsSimpleDao")
	private SimpleDao tmsSimpleDao;
	@Autowired
	private SqlMap tmsSqlMap;
	@Autowired
	private SequenceService sequenceService;
	
	public List<Map<String, Object>> listAllSwitchInOneTxn(String txnid) {

		StringBuffer sql = new StringBuffer("select sw.*, tab.TAB_DESC ");
		sql.append(" from ");
		sql.append(TMS_COM_SWITCH.TABLE_NAME).append(" sw,");
		sql.append(TMS_COM_TAB.TABLE_NAME).append(" tab");
		sql.append(" where ");
		sql.append("sw.").append(TMS_COM_SWITCH.SW_TXN).append("=tab.").append(TMS_COM_TAB.TAB_NAME).append(" AND ");
		sql.append("sw.").append(TMS_COM_SWITCH.SW_TXN);
		sql.append("=? order by ").append(TMS_COM_SWITCH.SW_ORDER);
		return tmsSimpleDao.queryForList(sql.toString(), txnid);
	}

	@Transactional
	public Map saveSwitch(Map formMap) {
		
		List<Map<String, Object>> delList = MapUtil.getList(formMap, "del");
		List<Map<String, Object>> modList = MapUtil.getList(formMap, "mod");
		List<Map<String, Object>> addList = MapUtil.getList(formMap, "add");
		List<Map<String, Object>> enableList = MapUtil.getList(formMap, "valid-y");
		List<Map<String, Object>> disbaleList = MapUtil.getList(formMap, "valid-n");
		
		Map m = new HashMap();
		
		if (delList != null && delList.size() > 0) {
			m = deleteRecord(delList);
		}
		if (addList != null && addList.size() > 0) {
			m = addRecord(addList);
		}		
		if (modList != null && modList.size() > 0) {
			m = updateRecord(modList);
		}
		if (enableList != null && enableList.size() > 0) {
			m = updateRecord(enableList);
		}
		if (disbaleList != null && disbaleList.size() > 0) {
			m = updateRecord(disbaleList);
		}
		
		return m;
	}
	
	/*
	 * 提交新数据
	 */
	private Map addRecord(List add){
		
		for(Object o: add){
			Map m = (Map) o;
			
			String cond = MapUtil.getString(m, "SW_COND");
			String desc = MapUtil.getString(m, "SW_DESC");
			String txnid = MapUtil.getString(m, "SW_TXN");
			
			commonCheckService.checkCond(cond, desc, txnid);
			transDefService.checkSingleParentAndAllSubSame(null, MapUtil.getString(m, "SW_DESC"), MapUtil.getString(m, "SW_TXN"), false, "sw", true);
			m.put(TMS_COM_SWITCH.SW_CREATETIME, System.currentTimeMillis());
			m.put(TMS_COM_SWITCH.SW_ID, Long.parseLong(sequenceService.getSequenceIdToString(DBConstant.SEQ_TMS_COM_SWITCH_ID)));
			
			m.put(TMS_COM_SWITCH.SW_ORDER, MapUtil.getLong(m, TMS_COM_SWITCH.SW_ORDER));
			m.put(TMS_COM_SWITCH.SW_TYPE, MapUtil.getLong(m, TMS_COM_SWITCH.SW_TYPE));
			m.put(TMS_COM_SWITCH.SW_ENABLE, MapUtil.getLong(m, TMS_COM_SWITCH.SW_ENABLE));
		}
		
		tmsSimpleDao.batchUpdate(buildAddSql(), add);
		return (Map) add.get(0);
	}
	/*
	 * 修改数据
	 */
	private Map updateRecord(List update){
		
		for(Object o: update){
			Map m =  (Map) o;
			
			String cond = MapUtil.getString(m, "SW_COND");
			String desc = MapUtil.getString(m, "SW_DESC");
			String txnid = MapUtil.getString(m, "SW_TXN");
			
			commonCheckService.checkCond(cond, desc, txnid);
			
			m.put(TMS_COM_SWITCH.SW_MODIFYTIME, System.currentTimeMillis());
			m.put(TMS_COM_SWITCH.SW_ID, MapUtil.getLong(m, TMS_COM_SWITCH.SW_ID));
			m.put(TMS_COM_SWITCH.SW_ORDER, MapUtil.getLong(m, TMS_COM_SWITCH.SW_ORDER));
			m.put(TMS_COM_SWITCH.SW_TYPE, MapUtil.getLong(m, TMS_COM_SWITCH.SW_TYPE));
			m.put(TMS_COM_SWITCH.SW_ENABLE, MapUtil.getLong(m, TMS_COM_SWITCH.SW_ENABLE));
		}
		
		if (update != null && update.size() == 1) {
			Map m = (Map) update.get(0);
			transDefService.checkSingleParentAndAllSubSame(MapUtil.getString(m, "SW_ID"),MapUtil.getString(m, "SW_DESC"), MapUtil.getString(m, "SW_TXN"), false, "sw", false);
		}
		
		
		tmsSimpleDao.batchUpdate(buildUpdateSql(), update);
		return (Map) update.get(0);
	}
	/*
	 * 删除数据
	 */
	private Map deleteRecord(List delete){
		
		tmsSimpleDao.executeUpdate(buildDeleteSql(delete));
		return null;
	}
	

	/*
	 * 组装添加switch的sql语句
	 */
	private String buildAddSql() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("INSERT INTO ");
		sb.append(TMS_COM_SWITCH.TABLE_NAME);
		sb.append("(SW_ID, SW_ORDER, SW_DESC, SW_TXN, SW_COND, SW_COND_IN, SW_TYPE, SW_ENABLE, SW_CREATETIME) ");
		sb.append("VALUES(:SW_ID,:SW_ORDER,:SW_DESC,:SW_TXN,:SW_COND,:SW_COND_IN,:SW_TYPE,:SW_ENABLE,:SW_CREATETIME)");
		
		return sb.toString();
	}
	/*
	 * 组装保存switch的sql语句
	 */
	private String buildUpdateSql() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("UPDATE ");
		sb.append(TMS_COM_SWITCH.TABLE_NAME);
		sb.append(" SET SW_ORDER=:SW_ORDER, SW_DESC=:SW_DESC, SW_TXN=:SW_TXN, SW_COND=:SW_COND, SW_COND_IN=:SW_COND_IN, SW_TYPE=:SW_TYPE, SW_ENABLE=:SW_ENABLE, SW_MODIFYTIME=:SW_MODIFYTIME ");
		sb.append(" WHERE ");
		sb.append("SW_ID=:SW_ID");
//		sb.append("UPDATE ");
//		sb.append(TMS_COM_SWITCH.TABLE_NAME);
//		sb.append(" SET SW_ORDER=:SW_ORDER, SW_DESC=:SW_DESC, SW_TXN=:SW_TXN, SW_COND=:SW_COND, SW_TYPE=:SW_TYPE, SW_ENABLE=:SW_ENABLE, SW_CREATETIME=:SW_CREATETIME, SW_MODIFYTIME=:SW_MODIFYTIME ");
//		sb.append(" WHERE ");
//		sb.append("SW_ID=:SW_ID");
		
		return sb.toString();
	}
	/*
	 * 组装删除switch的sql语句
	 */
	private String buildDeleteSql(List delete) {
		
		StringBuffer id_sb = new StringBuffer("");
		StringBuffer dlteSqlSb = new StringBuffer();
		dlteSqlSb.append("DELETE FROM ");
		dlteSqlSb.append(TMS_COM_SWITCH.TABLE_NAME).append(" ");
		dlteSqlSb.append("WHERE ");
		dlteSqlSb.append(TMS_COM_SWITCH.SW_ID).append(" in (");
		for(int i = 0; i < delete.size(); i++){
			Map dlt = (Map)delete.get(i);
			String id = MapUtil.getString(dlt, TMS_COM_SWITCH.SW_ID);
			id_sb.append(id).append(",");
		}
		id_sb.append(")");
		id_sb.setCharAt(id_sb.lastIndexOf(","), ' ');
		dlteSqlSb.append(id_sb);
		
		return dlteSqlSb.toString();
	}
	
	/*
	 * 是否需要保存
	 * 如果form的list为空,表示已经把该交易的全部switch删除了
	 */
//	private boolean needSaveNew(List formList){
//		return formList != null && formList.size() > 0;
//	}
	
}
