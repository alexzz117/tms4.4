package cn.com.higinet.tms.manager.modules.tmsreport.common;

import cn.com.higinet.tms.manager.dao.Order;

public class SqlUtil {

	public static String makeQuerySqlByOrder(String sql, Order order){
		String orderStr = order.toSqlString();
		if(!orderStr.isEmpty()){
			StringBuffer sqlSb = new StringBuffer();
			int startIndex = sql.toLowerCase().indexOf("select");
			int endIndex = startIndex + "select".length();
			sqlSb.append(sql.substring(0, startIndex));
			sqlSb.append(sql.substring(startIndex, endIndex));
			sqlSb.append(" row_number() over(").append(orderStr).append("),");
			sqlSb.append(sql.substring(endIndex));
			sql = sqlSb.toString();
		}
		return sql;
	}
}
