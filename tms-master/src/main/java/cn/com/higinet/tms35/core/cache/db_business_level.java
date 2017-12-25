package cn.com.higinet.tms35.core.cache;

import java.lang.reflect.Field;

import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.core.dao.row_in_db_impl;

/**
 *  商户评级信息
 * @author lining
 */
public class db_business_level extends row_in_db_impl {

	public db_business_level()
	{
		
	}
	
	public db_business_level(String business_id)
	{
		this.rr_id = business_id;
	}
	
	public String rr_id;
	public String ratekind_id;
	public String rs_id;
	public long score;
	public long level;
	public long modtime;
	
	static class_field g_field = new class_field();
	static {
		for (Field f : db_business_level.class.getFields())
			g_field.add_field(f);
	}
	
	public String get(String fd_name) {
		return str_tool.to_str(g_field.get(this, str_tool.lower_case(fd_name)));
	}

	public void put(String fd_name, String s) {
		g_field.set(this, str_tool.lower_case(fd_name), s);
	}
}