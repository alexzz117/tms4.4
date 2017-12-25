package cn.com.higinet.tms.engine.comm;

public class sql_tool {

	public static String get_sql(String sql, String[] params)
	{
		if(str_tool.is_empty(sql) || params == null || params.length == 0)
		{
			return sql;
		}
		int index = 0, count = 0;
		while((index = sql.indexOf("?")) != -1 
				&& params.length > count)
		{
			String befor = sql.substring(0, index);
			String after = sql.substring(index + 1, sql.length());
			if (params.length > count)
			{
				sql = befor + params[count] + after;
			}
			else
			{
				break;
			}
			count++;
		}
		return sql;
	}
}
