package cn.com.higinet.tms.manager.modules.tmsreport.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.base.util.Mapz;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.tmsreport.service.DisposalService;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;

@Service("disposalService")
public class DisposalServiceImpl implements DisposalService {
	
	@Autowired
	@Qualifier("dynamicSimpleDao")
	private SimpleDao dynamicSimpleDao;
	
	private static final String DISPOSAL_TABLE_NAME = "tms_com_disposal";
	
	@Override
	public Map<String, String> getDp() {
		String sql = "select DP_CODE,DP_NAME from "+DISPOSAL_TABLE_NAME;
		List<Map<String, Object>> codelist = dynamicSimpleDao.queryForList(sql);
		Map<String, String> codemap = new HashMap<String, String>();
		if(codelist.size() > 0){
			for(Map<String, Object> code : codelist){
				codemap.put((String)code.get("DP_CODE"), (String)code.get("DP_NAME"));
			}
		}
		return codemap;
	}

	@Override
	public List<Map<String, Object>> queryList() {
		String sql = "SELECT * FROM "+DISPOSAL_TABLE_NAME+" ORDER BY DP_ORDER ASC";
		return dynamicSimpleDao.queryForList(sql);
	}

	@Override
	public List<Map<String, Object>> queryList(String txnid) {
		String sql = "SELECT * FROM "+DISPOSAL_TABLE_NAME+" ";
		String tab_disposal = ""; 
		// 交易树显示所有处置方式
		if(!txnid.equals("T") && !txnid.equals("null") && txnid!=null && txnid.length()>0) {
			String[] txnids = TransCommon.cutToIds(txnid);
			String txnid_last = txnids[txnids.length-1];
			String txn_dis_sql = "SELECT TAB_DISPOSAL FROM TMS_COM_TAB  WHERE TAB_NAME= ? ";
			List<Map<String,Object>> txn_dis_list = dynamicSimpleDao.queryForList(txn_dis_sql,txnid_last);
			tab_disposal = txn_dis_list!=null && txn_dis_list.size()>0?Mapz.getString(txn_dis_list.get(0),"TAB_DISPOSAL"):"xxx";
		}
		
		if(tab_disposal.length()>0) {
			String[] dis = tab_disposal.split(",");
			String convert_tab_disposal = "";
			for (int i = 0; i < dis.length; i++) {
				convert_tab_disposal += "'"+dis[i]+"',";
			}
			if(convert_tab_disposal.length()>0) {
				convert_tab_disposal = convert_tab_disposal.substring(0, convert_tab_disposal.length()-1);
			}
			
			sql += " WHERE DP_CODE IN ("+convert_tab_disposal+")";
		}
		
		sql += "ORDER BY DP_ORDER ASC";
		return dynamicSimpleDao.queryForList(sql);
	}
}
