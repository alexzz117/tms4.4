package cn.com.higinet.tms35.core.cache;

import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.dao.row_in_db_impl;
import cn.com.higinet.tms35.run.run_txn_values;

public class db_device_user extends row_in_db_impl {
	public static final boolean maxDeviceOn = tmsapp.get_config("tms.dfp.max_device.ison", 0) == 1;
	public long device_id;
	public long old_device_id = -1;
	public String user_id;
	public int status;
	public long lastmodified;
	public long createTime = System.currentTimeMillis();
	public db_device_user() 
	{
		status = -1;
		lastmodified = System.currentTimeMillis();
	}
	public db_device_user(run_txn_values rf)
	{
		this();
		user_id = (String) rf.get_fd(rf.m_env.field_cache().INDEX_USERID);
		device_id = Long.parseLong((String)rf.get_fd(rf.m_env.field_cache().INDEX_DEVID));
		lastmodified = System.currentTimeMillis();
//		old_device_id = device_id;
	}
	
}