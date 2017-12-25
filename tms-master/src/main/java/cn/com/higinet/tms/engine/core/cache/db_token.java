package cn.com.higinet.tms.engine.core.cache;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.bin_stream;

public class db_token
{
	static Logger log = LoggerFactory.getLogger(db_token.class);
	
	public String token ;
	
	public db_token(String token)
	{
		this.token = token;
	}
	
	public static db_token load_from(bin_stream b) {
		db_token u = new db_token(b.load_string());
		return u;
	}
	/**
	* 方法描述:更新缓存数据
	* @param bs
	*/
	
	public void save_to(bin_stream bs) {
		bs.save(token);
	}
}