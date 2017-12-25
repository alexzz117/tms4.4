package cn.com.higinet.tms.manager.modules.common;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms35.comm.web_tool;
import cn.com.higinet.tms35.core.cache.cache_init;
import cn.com.higinet.tms35.core.cache.db_fd;
import cn.com.higinet.tms35.core.cache.db_process;
import cn.com.higinet.tms35.core.cache.db_rule;
import cn.com.higinet.tms35.core.cache.db_rule_action;
import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.core.cache.db_strategy;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

@Component("commonCheckService")
public class CommonCheckService {
	
	private static Log log = LogFactory.getLog(CommonCheckService.class);
	
	@Autowired
	@Qualifier("dynamicDataSource")
	private DataSource dynamicDataSource;

	public void checkCond(String cond, String desc, String txnid) {
		checkCond(cond, desc, null, txnid);
	}
	
	public void checkCond(String cond, String desc, String label, String txnid) {
		checkCond(cond, desc, label, txnid, null);
	}
	
	public void checkCond(String cond, String desc, String label, String txnid, DataSource mDs) {
		
		StringBuffer error = new StringBuffer();
		
		if (!StringUtil.isEmpty(cond)) {
			initCache(mDs);
			if(StringUtil.isEmpty(label)){
				label = "表达式";
			}
			// 检查条件正确性
			boolean isTrue = true;
			try {
				isTrue = web_tool.compile_expr(txnid, cond, error);
			} catch (Exception e) {
				log.error(e);
				throw new TmsMgrServiceException(label + "不合法，由于" + "[" + desc + "]" + e.getLocalizedMessage());
			}
			// 不正确弹出错误信息
			if (!isTrue) {
				throw new TmsMgrServiceException(label + "不合法，由于" + "[" + desc + "]" + error);
			}
		}
	}
	
	public void findRefField(String tab_name, String ref_name,
			List<db_fd> ref_fd,
			List<db_rule_action> ref_act,
			List<db_stat> ref_stat,
			List<db_rule> ref_rule,
			List<db_strategy> ref_sw,
			DataSource mDs) {
		
		initCache(mDs);
		web_tool.find_ref_field(tab_name, ref_name, ref_fd, ref_act, ref_stat, ref_rule, ref_sw);
	}
	
	public void findRefField(String tab_name, String ref_name,
			List<db_fd> ref_fd,
			List<db_rule_action> ref_act,
			List<db_stat> ref_stat,
			List<db_rule> ref_rule,
			List<db_strategy> ref_sw) {
		
		findRefField(tab_name, ref_name, ref_fd, ref_act, ref_stat, ref_rule, ref_sw, null);
	}

	public void initCache(DataSource mDs) {
		
		
		DataSource ds = null;
		
		try {
			
			if(mDs == null) {
				ds = dynamicDataSource;
			} else {
				ds = mDs;
			}
			
			cache_init.init(new data_source(ds));
			
		} catch (Exception e) {
			log.error(e);
			throw new TmsMgrServiceException("缓存初始化错误，由于" + e.getMessage());
		} finally {
			printDBInfos(ds, log);
		}
	}

	/**
	 * strings :
	 *    0,    1,     2
	 * cond, desc, txnid
	 */
	public static void checkCond(Map map, String... strings) throws IllegalArgumentException{

		if (strings.length != 3 || !map.containsKey(strings[0])
				|| !map.containsKey(strings[1]) || !map.containsKey(strings[2]) ) {
			log.error("参数错误,需要cond, desc, txnid三个参数");
			throw new IllegalArgumentException("参数错误:需要cond,desc,txnid三个参数在map中");
		}

		String cond = MapUtil.getString(map, strings[0]);
		String desc = MapUtil.getString(map, strings[1]);
		String txnid = MapUtil.getString(map, strings[2]);

		StringBuffer error = new StringBuffer();

		if (!StringUtil.isEmpty(cond)) {
			// 检查条件正确性
			boolean isTrue = true;
			try {
				isTrue = web_tool.compile_expr(txnid, cond, error);
			} catch (Exception e) {
				log.error(e);
				throw new TmsMgrServiceException("表达式不合法，由于" + "[" + desc + "]" + e.getLocalizedMessage());
			}
			// 不正确弹出错误信息
			if (!isTrue) {
				throw new TmsMgrServiceException("表达式不合法，由于" + "[" + desc + "]" + error);
			}
		}
	}
	
	public static void printDBInfos (DataSource mDs, Log log) {
		try {
			if (log.isDebugEnabled()) {
				
				if (mDs == null) {
					throw new IllegalArgumentException("DataSource Can not be null");
				}
				
				Connection con = mDs.getConnection();
				// 获取数据库的信息
				DatabaseMetaData dbMetaData = con.getMetaData();
				// 返回一个String类对象，代表数据库的URL
				log.debug("URL:" + dbMetaData.getURL() + ";");
				// 返回连接当前数据库管理系统的用户名。
				log.debug("UserName:" + dbMetaData.getUserName() + ";");
				// 返回一个boolean值，指示数据库是否只允许读操作。
				log.debug("isReadOnly:" + dbMetaData.isReadOnly() + ";");
				// 返回数据库的产品名称。
				log.debug("DatabaseProductName:" + dbMetaData.getDatabaseProductName() + ";");
				// 返回数据库的版本号。
				log.debug("DatabaseProductVersion:" + dbMetaData.getDatabaseProductVersion() + ";");
				// 返回驱动驱动程序的名称。
				log.debug("DriverName:" + dbMetaData.getDriverName() + ";");
				// 返回驱动程序的版本号。
				log.debug("DriverVersion:" + dbMetaData.getDriverVersion());

				con.close();
			}
		} catch (Exception e) {
			// ignore
		}
		
	}
	
	public boolean find_ref_roster(String roster_name, List<db_stat> ref_stat,
			List<db_rule> ref_rule, List<db_strategy> ref_sw, List<db_rule_action> ref_act) {
		return web_tool.find_ref_roster(roster_name, ref_stat, ref_rule, ref_sw, ref_act);
	}
	
	public boolean find_ref_roster(String roster_name) {
		List<db_stat> ref_stat = new ArrayList<db_stat>();
		List<db_rule> ref_rule = new ArrayList<db_rule>();
		List<db_strategy> ref_sw = new ArrayList<db_strategy>();
		List<db_rule_action> ref_act = new ArrayList<db_rule_action>();
		return web_tool.find_ref_roster(roster_name, ref_stat, ref_rule, ref_sw, ref_act);
	}
	
	public boolean find_ref_valid_roster(String roster_name) {
		List<db_stat> ref_stat = new ArrayList<db_stat>();
		List<db_rule> ref_rule = new ArrayList<db_rule>();
		List<db_strategy> ref_sw = new ArrayList<db_strategy>();
		List<db_rule_action> ref_act = new ArrayList<db_rule_action>(); 
		
		find_ref_roster(roster_name, ref_stat, ref_rule, ref_sw, ref_act);
		
		return isEnableRef(ref_stat, ref_rule, ref_act, ref_sw, null);
	}
	
	public boolean find_ref_rule(String txn_name, String rule_name, List<db_rule_action> ref_ac) {
		return web_tool.find_ref_rule(txn_name, rule_name, ref_ac);
	}
	
	public boolean find_ref_rule(String txn_name, String rule_name) {
		List<db_rule_action> ref_ac = new ArrayList<db_rule_action>();
		
		return find_ref_rule(txn_name, rule_name, ref_ac);
	}
	
	public boolean find_ref_valid_rule(String txn_name, String rule_name) {
		List<db_rule_action> ref_ac = new ArrayList<db_rule_action>();
		
		find_ref_rule(txn_name, rule_name, ref_ac);
		
		return isEnableRef(null, null, ref_ac, null, null);
	}
	
	public boolean find_ref_stat(String txn_name, String stat_name, List<db_stat> ref_stat,
			List<db_rule> ref_rule, List<db_strategy> ref_sw, List<db_rule_action> ref_ac,
			List<db_process> ref_ps) {
		
		return web_tool.find_ref_stat(txn_name, stat_name, ref_stat, ref_rule, ref_sw,ref_ac,ref_ps);
	}
	
	public boolean find_ref_stat(String txnId, String stat_name) {
		List<db_stat> ref_stat = new ArrayList<db_stat>();
		List<db_rule> ref_rule = new ArrayList<db_rule>();
		List<db_strategy> ref_sw = new ArrayList<db_strategy>();
		List<db_rule_action> ref_ac = new ArrayList<db_rule_action>();
		List<db_process> ref_ps = new ArrayList<db_process>();

		// 缓存中查找引用
		return find_ref_stat(txnId, stat_name, ref_stat, ref_rule, ref_sw,ref_ac,ref_ps);
	}
	
	public boolean find_ref_valid_stat(String txnId, String stat_name) {
		List<db_stat> ref_stat = new ArrayList<db_stat>();
		List<db_rule> ref_rule = new ArrayList<db_rule>();
		List<db_strategy> ref_sw = new ArrayList<db_strategy>();
		List<db_rule_action> ref_ac = new ArrayList<db_rule_action>();
		List<db_process> ref_ps = new ArrayList<db_process>();

		// 缓存中查找引用
		find_ref_stat(txnId, stat_name, ref_stat, ref_rule, ref_sw,ref_ac,ref_ps);
		
		return isEnableRef(ref_stat,ref_rule,ref_ac,ref_sw,ref_ps);
	}
	
	private boolean isEnableRef(List<db_stat> ref_stat,List<db_rule> ref_rule,List<db_rule_action> ref_act, List<db_strategy> ref_sw, List<db_process> ref_ps) {
		
		if(ref_act != null) {
			for (db_rule_action db_action : ref_act) {
				if(db_action.is_enable) 
					return true;
			}
		}
		if(ref_stat != null) {
			for (db_stat db_stat : ref_stat) {
				if(db_stat.is_valid == 1) 
					return true;
			}
		}
		if(ref_sw != null) {
			for (db_strategy db_sw : ref_sw) {
				if(db_sw.st_enable) 
					return true;
			}
		}
		if(ref_rule != null) {
			for (db_rule db_rule : ref_rule) {
				if(db_rule.is_enable) 
					return true;
			}
		}
		if(ref_ps != null) {
			for (db_process db_process : ref_ps) {
				if(db_process.ps_enable) 
					return true;
			}
		}
		return false;
	}
}
