package cn.com.higinet.tms.manager.modules.common;

import java.util.Map;

import cn.com.higinet.tms.manager.dao.ConditionUtil;

/**
 * dao where 条件类
 * 
 * @author yanghui
 * 
 */
public class SqlWhereHelper {

	public static String[][] txnConds = new String[][] {};


	public static String[][]  centerConds = new String[][]{
	};
	

	public static String[][] funcConds = new String[][] {
		{ "=", DBConstant.TMS_COM_FUNC_FUNCID, DBConstant.TMS_COM_FUNC_FUNCID },
		{ "=", DBConstant.TMS_COM_FUNC_FUNCGROUPID, DBConstant.TMS_COM_FUNC_FUNCGROUPID },
		{ "=", DBConstant.TMS_COM_FUNC_FEATURE, DBConstant.TMS_COM_FUNC_FEATURE },
		{ "=", DBConstant.TMS_COM_FUNC_MODULE, DBConstant.TMS_COM_FUNC_MODULE },
		{ "=", DBConstant.TMS_COM_FUNC_STATUS, DBConstant.TMS_COM_FUNC_STATUS }
	};
	
	public static String[][] serverConds = new String[][] {
		{ "=", DBConstant.TMS_RUN_SERVER_SERVERID, DBConstant.TMS_RUN_SERVER_SERVERID },
		{ "=", DBConstant.TMS_RUN_SERVER_SERVERTYPE, DBConstant.TMS_RUN_SERVER_SERVERTYPE }
	};
	
	public static String[][] nameListConds = new String[][] {
		{ "like", "P." + DBConstant.TMS_MGR_ROSTER_ROSTERNAME, DBConstant.TMS_MGR_ROSTER_ROSTERNAME },
		{ "=", "P." + DBConstant.TMS_MGR_ROSTER_ROSTERTYPE, DBConstant.TMS_MGR_ROSTER_ROSTERTYPE },
		{ "=", "P." + DBConstant.TMS_MGR_ROSTER_DATATYPE, DBConstant.TMS_MGR_ROSTER_DATATYPE},
	};
	
	public static String[][] valueListConds = new String[][] {
		{ "=", DBConstant.TMS_MGR_ROSTERVALUE_ROSTERID, DBConstant.TMS_MGR_ROSTERVALUE_ROSTERID },
	};
	
	public static String[][] changeVlueListConds = new String[][] {
		{ "!=", "R." + DBConstant.TMS_MGR_ROSTER_ROSTERTYPE, DBConstant.TMS_MGR_ROSTER_ROSTERTYPE },
		{ "=", "R." + DBConstant.TMS_MGR_ROSTER_DATATYPE, DBConstant.TMS_MGR_ROSTER_DATATYPE},
	};
	
	/**
	 * 
	 * 
	 * @param conds
	 * @param condsArray
	 * @return
	 */
	public static String getPatternWhere(Map<String, String> conds,
			String[][] condsArray) {
		if (conds == null || conds.size() < 1)
			return "";
		for (int i = 0; i < condsArray.length; i++) {
			String[] temp = condsArray[i];
			if (temp[0] != null && temp[0].equals("like")) {
				conds.put(temp[2], ConditionUtil.like(conds.get(temp[2])));
			}
		}
		String where = ConditionUtil.and(conds, condsArray);
		if ("".equals(where))
			return "";

		return ConditionUtil.where(where);
	}
	
	public static String getInWhere(String[] arrs) {
		String in = "";
		for (int i = 0; i < arrs.length; i++) {
			if (!in.equals("")) in += ",";
			in = in + "'" + arrs[i] + "'";
		}
		return in;
	}
}
