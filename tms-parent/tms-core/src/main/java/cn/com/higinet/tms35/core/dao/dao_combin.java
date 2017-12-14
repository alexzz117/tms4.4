package cn.com.higinet.tms35.core.dao;

import javax.sql.DataSource;

import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.cache.db_alert;
import cn.com.higinet.tms35.core.cache.db_device;
import cn.com.higinet.tms35.core.cache.db_device_user;
import cn.com.higinet.tms35.core.cache.db_fd;
import cn.com.higinet.tms35.core.cache.db_rule_hit;
//import cn.com.higinet.tms35.core.cache.db_session;
import cn.com.higinet.tms35.core.cache.db_tab;
import cn.com.higinet.tms35.core.cache.db_userpattern;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public class dao_combin
{
	data_source ds;
	
	public batch_stmt_jdbc_obj<db_rule_hit> stmt_rule_hit;
	public batch_stmt_jdbc_obj<db_alert> stmt_alert;
//	public batch_stmt_jdbc_obj<db_session> stmt_session;
//	public batch_stmt_jdbc_obj<db_user> stmt_user;
//	public batch_stmt_jdbc_obj<db_ref_row> stmt_account;
//	public batch_stmt_jdbc_obj<db_merchant> stmt_merchant;//商户的信息
	public batch_stmt_jdbc_obj<db_device> stmt_device;
	public batch_stmt_jdbc_obj<db_device_user> stmt_device_user;
	public batch_stmt_jdbc_obj<db_userpattern> stmt_user_pattern;
//	public batch_stmt_jdbc_obj<db_safe_card> stmt_safe_card;
//	public batch_stmt_jdbc_obj<db_safe_device> stmt_safe_device;
	
//	public batch_stmt_jdbc_obj<db_pos_merchant> stmt_pos_merchant;//POS商户的信息
	
//	public batch_stmt_jdbc_obj<db_riskinfo_merchant> stmt_riskinfo_merchant;//商户风险信息
	
	public dao_business_level m_dao_bus_level;
	public dao_combin(final db_tab.cache dtc,final db_fd.cache dfc)
	{
		ds = new data_source((DataSource) bean.get("tmsDataSource"));
		
		stmt_rule_hit=new dao_rule_hit(ds);
		stmt_alert=new dao_alert(ds);
//		stmt_session=new dao_session(ds);
//		stmt_merchant = new dao_merchant(ds);//商户的信息
//		stmt_user=new dao_user(ds);
//		stmt_account=new dao_acc(ds,dfc);
		stmt_user_pattern=new dao_userpattern(ds);
		
//		stmt_pos_merchant = new dao_pos_merchant(ds);//POS商户的信息
		
//		stmt_riskinfo_merchant = new dao_riskinfo_merchant(ds);//商户风险信息
		
		if (db_device.IS_DEVFINGER_ON)
		{
			dao_combin_device(ds);
		}
		
		m_dao_bus_level = new dao_business_level(ds);
//		stmt_safe_card = new dao_safe_card(ds);
//		stmt_safe_device = new dao_safe_device(ds);
	}
	
	/**
	 * 设备信息实时保存，使用不同的连接
	 */
	private void dao_combin_device(data_source ds)
	{
		stmt_device = new dao_device(ds);
		stmt_device_user = new dao_device_user(ds);
	}

	public void close()
	{
//		stmt_account.close();
//		stmt_user.close();
//		stmt_session.close();
//		stmt_merchant.close();//商户的信息
		stmt_alert.close();
		stmt_rule_hit.close();
		stmt_user_pattern.close();
		
//		stmt_pos_merchant.close();//商户的信息
//		stmt_riskinfo_merchant.close();
		
		if (db_device.IS_DEVFINGER_ON)
		{
			stmt_device.close();
			stmt_device_user.close();
		}
		m_dao_bus_level.close();
//		stmt_safe_card.close();
//		stmt_safe_device.close();
	}
}
