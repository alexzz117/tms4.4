package cn.com.higinet.tms35.core.dao;

import java.sql.Types;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.core.cache.db_device;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public class dao_device_update extends batch_stmt_jdbc_obj<db_device> {
	static Logger log = LoggerFactory.getLogger(dao_device_update.class);
	static String sql = "update TMS_DFP_DEVICE set DEVICE_ID=?, RANDOM_NUM=?, CHANNEL_DEVICEID=?, LASTMODIFIED=?"
			+ ", PROP_VALUES=?%s where DEVICE_ID=?";
	static int[] sql_param_type = new int[] { Types.BIGINT, Types.VARCHAR, Types.VARCHAR, Types.BIGINT, Types.VARCHAR};
	static int fixed_size = sql_param_type.length + 1;//非扩展字段的参数个数
	
	static
	{
		StringBuffer sql_param_buff = new StringBuffer();
		sql_param_type = Arrays.copyOf(sql_param_type, db_device.EXTEND_FIELD_NUM + fixed_size);
		for (int i=0,len=db_device.EXTEND_FIELD_NUM;i<len;i++)
		{
			sql_param_buff.append(", TEXT" + (i + 1)).append("=?");
			sql_param_type[i + fixed_size - 1] = Types.VARCHAR;
		}
		sql = String.format(sql, sql_param_buff.toString());
		sql_param_type[sql_param_type.length - 1] = Types.BIGINT;
	}
	
	public dao_device_update(data_source ds)
	{
		super(ds, sql, sql_param_type);
	}
	
	@Override
	public String name() {
		return "device_update";
	}

	@Override
	public Object[] toArray(db_device e) {
		Object[] objs = new Object[] { e.device_id,e.random_num, e.channel_deviceid, e.lastmodified, e.prop_values };
		objs = Arrays.copyOf(objs, db_device.EXTEND_FIELD_NUM + fixed_size);
		for (int i=0,len=db_device.EXTEND_FIELD_NUM;i<len;i++)
		{
			objs[i + fixed_size - 1] = e.extend_fields[i];
		}
		objs[objs.length - 1] = e.old_device_id < 0 ? e.device_id : e.old_device_id;
		return objs;
	}
}