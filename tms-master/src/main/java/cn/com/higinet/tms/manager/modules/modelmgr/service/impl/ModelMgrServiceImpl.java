package cn.com.higinet.tms.manager.modules.modelmgr.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.modelmgr.service.ModelMgrService;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;

/**
 * 模型管理服务类
 * @author zlq
 * @author zhang.lei
 */
@Service("modelMgrService")
public class ModelMgrServiceImpl implements ModelMgrService {

	@Autowired
	@Qualifier("offlineSimpleDao")
	private SimpleDao offlineSimpleDao; 
	
	/**
	 * 分页获取所有模型
	 * @param 查询条件
	 */
	public Page<Map<String, Object>> modelList(Map<String, String> conds) {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from  TMS_COM_TAB WHERE modelused in ('1','2')");
		
		Page<Map<String, Object>> modelPage = offlineSimpleDao.pageQuery(sql.toString(),conds, new Order().asc("TAB_NAME"));
		
		List<Map<String,Object>> modelList = modelPage.getList();
		for (Map<String, Object> map : modelList) {
			String tab_name = MapUtil.getString(map, "TAB_NAME");
			String txnName = fullTxnDescPath(tab_name);
			map.put("TAB_DESC", txnName);
		}
		
		return modelPage;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.modelmgr.service.ModelMgrService#modelHisList(java.util.Map)
	 */
	public Page<Map<String, Object>> modelHisList(Map<String, String> reqs) {
		String sql = "select TRAINID,TXNID,TRAINDATE,MODELUSED,STARTTIME,ENDTIME,EXECSTATE,MODELDESC,F1SCORE,STARTDATE,TRAINTYPE from TMS_COM_MTRAINHIS where TXNID=:txn_id";
		Page<Map<String, Object>> modelPage =  offlineSimpleDao.pageQuery(sql, reqs, new Order().desc("STARTTIME"));
		List<Map<String,Object>> modelList = modelPage.getList();
		String tab_name = MapUtil.getString(reqs, "txn_id");
		String txnName = fullTxnDescPath(tab_name);
		for (Map<String, Object> map : modelList) {
			map.put("TAB_DESC", txnName);
		}
		return modelPage;
	}
	
	
	private String fullTxnDescPath(String tab_name) {
		String sql1 = "select * from TMS_COM_TAB where tab_name in ("+TransCommon.arr2str(TransCommon.cutToIds(tab_name))+") order by tab_name";
		List<Map<String,Object>> fullPathList = offlineSimpleDao.queryForList(sql1);
		String txnName = "";
		for (Map<String, Object> map2 : fullPathList) {
			String parentTxnName = MapUtil.getString(map2, "tab_desc");
			if(txnName.length()==0) {
				txnName = parentTxnName;
			}else {
				txnName += "-" + parentTxnName;
			}
		}
		return txnName;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.modelmgr.service.ModelMgrService#modelCorrelationList(java.util.Map)
	 */
	public List<Map<String, Object>> modelCorrelationList(
			Map<String, String> reqs) {
		String trainid = MapUtil.getString(reqs, "trainid");
		String txnid = MapUtil.getString(reqs, "txnid");
		String txnids = TransCommon.arr2str(TransCommon.cutToIds(txnid));
		String sql = "SELECT FEATURECODE,FVAL,RELEMEASURE, FEATURENAME FROM TMS_COM_FTRAINHIS H"
			+" INNER JOIN TMS_COM_RELEMEASUREHIS R ON  H.FRAINID=R.FRAINID"
			+" LEFT JOIN ("
			+" SELECT STAT_TXN TAB_NAME, STORECOLUMN FD_NAME,STAT_DESC FEATURENAME FROM TMS_COM_STAT  WHERE STAT_TXN in ("+txnids+")"
			+" UNION "
			+" SELECT TAB_NAME, STORECOLUMN FD_NAME,REF_DESC FEATURENAME FROM TMS_COM_REFFD WHERE TAB_NAME in ("+txnids+")"
			+" UNION "
			+" SELECT TAB_NAME, FD_NAME,NAME FEATURENAME FROM TMS_COM_FD  WHERE TAB_NAME in ("+txnids+")) FD ON H.FEATURECODE=FD.FD_NAME"
			+" WHERE R.ISCHOICE='1' AND H.FEATURECODE!='class' AND H.TRAINID=?";
		List<Map<String, Object>> queryForList = offlineSimpleDao.queryForList(sql, trainid);
		
		for (Map<String, Object> map : queryForList) {
			String fval = MapUtil.getString(map, "FVAL");
			String featurename = MapUtil.getString(map, "FEATURENAME");
			String relemeasure = MapUtil.getString(map, "RELEMEASURE");
			
			// 如果有属性值，则显示为：属性名称[属性值]
			if(fval.length() > 0) {
				map.put("FEATURENAME", featurename+"["+fval+"]");
			}
			
			// 相关度只显示该属性与class的相关度
			if(relemeasure.length()>0) {
				String[] rs_array = relemeasure.split(";");
				for (int i = 0; i < rs_array.length; i++) {
					if("class".equals(rs_array[i].split(":")[0])){
						relemeasure = rs_array[i].split(":")[1];
						break;
					}
				}
				map.put("RELEMEASURE", relemeasure);
			}
		}
		
		// 按相关度由大到小排序
		Collections.sort(queryForList, new Comparator<Object>() { 
			public int compare(Object o1, Object o2) {
				return new Double(MapUtil.getDouble((Map<String,Double>) o2, "RELEMEASURE")).compareTo(new Double(MapUtil.getDouble((Map<String,Double>) o1, "RELEMEASURE")));
			}
		});
		
		return queryForList;
	}
}
