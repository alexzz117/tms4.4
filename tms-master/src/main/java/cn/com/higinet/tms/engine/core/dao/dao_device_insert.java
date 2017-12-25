package cn.com.higinet.tms.engine.core.dao;

import java.sql.Types;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.cache.db_device;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;

public class dao_device_insert extends batch_stmt_jdbc_obj<db_device> {
	static Logger log = LoggerFactory.getLogger(dao_device_insert.class);
	static String sql = "insert into TMS_DFP_DEVICE (DEVICE_ID, RANDOM_NUM, CHANNEL_DEVICEID, "
			+ "APP_ID, CREATE_TIME, LASTMODIFIED, PROP_VALUES%s)"
			+ " values (?,?,?,?,?,?,?%s)"; // 批量插入的sql
	static int[] sql_param_type = new int[] { Types.INTEGER, Types.VARCHAR, Types.VARCHAR,
			Types.VARCHAR, Types.BIGINT, Types.BIGINT, Types.VARCHAR}; // 批量插入sql的参数数据类型
	static int fixed_size = sql_param_type.length;//非扩展字段的参数个数
	static
	{
		StringBuffer sql_param_name_buff = new StringBuffer();
		StringBuffer sql_param_buff = new StringBuffer();
		sql_param_type = Arrays.copyOf(sql_param_type, db_device.EXTEND_FIELD_NUM + fixed_size);
		for (int i=0,len=db_device.EXTEND_FIELD_NUM;i<len;i++)
		{
			sql_param_name_buff.append(",TEXT" + (i + 1));
			sql_param_buff.append(",?");
			sql_param_type[i + fixed_size] = Types.VARCHAR;
		}
		sql = String.format(sql, sql_param_name_buff.toString(), sql_param_buff.toString());
	}
	
	public dao_device_insert(data_source ds)
	{
		super(ds, sql, sql_param_type);
	}

	@Override
	public String name() {
		return "device_insert";
	}

	@Override
	public Object[] toArray(db_device e) {
		Object[] objs = new Object[] { e.device_id,e.random_num, e.channel_deviceid, 
				e.app_id, e.create_time, e.lastmodified, e.prop_values };
		objs = Arrays.copyOf(objs, objs.length + db_device.EXTEND_FIELD_NUM);
		for (int i=0,len=db_device.EXTEND_FIELD_NUM;i<len;i++)
		{
			objs[i + fixed_size] = e.extend_fields[i];
		}
		return objs;
	}

}
