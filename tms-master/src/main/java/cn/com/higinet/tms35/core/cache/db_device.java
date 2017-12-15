package cn.com.higinet.tms35.core.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.NumberUtils;

import cn.com.higinet.tms35.comm.DeviceUtil;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tm_tool;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.dao.row_in_db_impl;

public class db_device extends row_in_db_impl {
	private static Logger log = LoggerFactory.getLogger(db_device.class);
	public static final boolean IS_DEVFINGER_ON = tmsapp.get_config("tms.devfinger.isOn", 0) == 1;
	public static final boolean isDevSyncLog = tmsapp.get_config("sync.log.device.isOn", 0) == 1;
	public static final int EXTEND_FIELD_NUM = 115; //扩展字段个数
	public db_device(){
		create_time = tm_tool.lctm_ms();
		lastmodified = create_time;
	}
	
//	public db_device(run_txn_values rf)
//	{
//		this();
//		device_id = Long.parseLong((String)rf.get_fd(rf.m_env.field_cache().INDEX_DEVID));
//		old_device_id = device_id;
//		app_id = (String) rf.get_fd(rf.m_env.field_cache().INDEX_CHANCODE);
//		prop_values = rf.m_env.m_channel_data.get("DEVICEFINGER");
//	}
	public long device_id;
	public long old_device_id = -1;
	public String channel_deviceid;
	public String random_num;
	public String app_id;
	public long create_time;
	public long lastmodified;
	public String prop_values;
	public String [] extend_fields = new String[EXTEND_FIELD_NUM];
	public double match;// 指纹匹配度
	
	public void resolve()
	{
		if (!str_tool.is_empty(prop_values) && !str_tool.is_empty(app_id))
		{
			String[] prop_value_array = prop_values.split("\\|");
			for (int i = 0, len = prop_value_array.length; i < len; i++)
			{
				
				String prop_value = prop_value_array[i];
				int sub_len = prop_value.indexOf(":");
				int prop_id=0;
				String ps=null;
				try {
					//ps=prop_value.substring(0, sub_len);
					//prop_id=NumberUtils.parseNumber(ps, Integer.class);
				    prop_id = Integer.valueOf(prop_value.substring(0, sub_len));
				} catch (Exception e) {
					 log.warn(String.format("Exception occurred when parseNumber \"%s\". \"%s\"",prop_value, e.getMessage()));
				}
				String value = prop_value.substring(sub_len+1);
				db_dfp_app_properties app_prop = db_cache.get().app_properties().get(app_id, prop_id);
				if (app_prop != null) {
					String store_column = app_prop.storecolumn;// 存储字段
					if(store_column!=null && !"".equals(store_column)){
						int site = Integer.valueOf(store_column.substring("TEXT".length()));
						if (site > 0 && site <= EXTEND_FIELD_NUM)
						{
							extend_fields[site - 1] = value;
						}
					}
				}
			}
		}
	}
}
