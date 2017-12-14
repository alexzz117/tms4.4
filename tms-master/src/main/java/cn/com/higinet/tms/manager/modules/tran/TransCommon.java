package cn.com.higinet.tms.manager.modules.tran;

import java.util.HashMap;
import java.util.Map;


public class TransCommon {

	/*
	 * 交易表id是有规则的
	 * 所以可以把传入的交易主键 把txnid切分成数组
	 * 每一个都是一个交易主键,包括自己
	 */
	public static final String[] cutToIds(String txnid) {
		String[] txnids = new String[txnid.length() / 2 + 1];
		int offset = 0;

		for (int i = 0; i < txnids.length; i++) {
			txnids[i] = txnid.substring(0, 1 + offset);
			offset += 2;
		}
		return txnids;
	}

	public static final String arr2str(String[] ids) {
		String rs = "";
		for (int i = 0; i < ids.length; i++) {
			if(rs.length() > 0) {
				rs += ",";
			}
			rs += "'"+ids[i]+"'";
		}
		
		return rs;
	}

	
	/*
	 * make sql commond like 'a', 'b', 'c'
	 */
	public static final String cutToIdsForSql(String txnid) {

		String[] txnids = cutToIds(txnid);
		StringBuffer id_sb = new StringBuffer();
		for(int i = 0; i < txnids.length; i++){
			id_sb.append("'").append(txnids[i]).append("',");
		}
		id_sb.setCharAt(id_sb.lastIndexOf(","), ' ');
		return id_sb.toString();
	}
	
	/*
	 * make sql commond like a=:a, b=:b, c=:c
	 */
	public static final String cutToIdsForSqlInj(String txnid) {
		String[] txnids = cutToIds(txnid);
		StringBuffer id_sb = new StringBuffer();
		for(int i = 0; i < txnids.length; i++){
			id_sb.append(":tab").append(txnids[i]).append(",");
		}
		id_sb.setCharAt(id_sb.lastIndexOf(","), ' ');
		return id_sb.toString();
	}
	
	
	/*
	 * make sql commond like a=:a, b=:b, c=:c
	 */
	public static final Map<String, Object> cutToMapIdsForSqlInj(String txnid) {
		Map<String, Object> sqlConds = new HashMap<String, Object>();
		String[] txnids = cutToIds(txnid);
		for(int i = 0; i < txnids.length; i++){
			sqlConds.put("tab"+txnids[i], txnids[i]);
		}
		return sqlConds;
	}
	
}
