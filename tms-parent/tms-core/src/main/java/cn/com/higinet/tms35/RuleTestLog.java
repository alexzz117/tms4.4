/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms35;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.cache.db_action;
import cn.com.higinet.tms35.core.cache.db_alert;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.cache.db_process;
import cn.com.higinet.tms35.core.cache.db_rule_hit;
import cn.com.higinet.tms35.core.cache.db_sw;
import cn.com.higinet.tms35.core.cache.db_tab;
import cn.com.higinet.tms35.core.cache.linear;
import cn.com.higinet.tms35.core.cache.str_id;
import cn.com.higinet.tms35.core.cache.txn;
import cn.com.higinet.tms35.core.dao.dao_base;
import cn.com.higinet.tms35.run.run_txn_values;

/**
 * 功能/模块:规则测试日志输出
 * 
 * @author zhanglq
 * @version 1.0 Aug 5, 2013 类描述: 修订历史: 日期 作者 参考 描述
 * 
 */
public class RuleTestLog
{

	private static final Logger log = LoggerFactory.getLogger(RuleTestLog.class);

	private static final boolean DEBUG_ENABLED = log.isDebugEnabled();

	static dao_base ruleDao = bean.get(dao_base.class);
	/**
	 * 方法描述:交易信息
	 * 
	 * @param get_txn
	 */
	@SuppressWarnings("rawtypes")
	public static void txn_log(run_txn_values txn_values)
	{
		if (DEBUG_ENABLED) {
			if (txn_values != null) {
				//获取字段值
				List<Object> txn_info = txn_values.m_txn_data;
				//获取字段名称
				txn txn = txn_values.m_env.get_txn();
				linear<str_id> linear = txn.m_fd_refid;
				String tab_name = txn.m_tab.tab_name;
				Map<String, String> map = new HashMap<String, String>();
				for (str_id str : linear) {
					String fieldName = str.s; //字段名
					int num =  str.id; //对应的数组下标
					
					if(num>=txn_info.size()){
						continue;
					}
					String fieldValue = String.valueOf(txn_info.get(num));
					if(!fieldValue.equals("") && !fieldValue.equals("null")){
						//判断该字段是否为时间型
						if(fieldName.indexOf("TIME") != -1) {
							//将毫秒时间转换成 yyyy-MM-dd hh:mm:ss
							SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 
							Long time = Long.parseLong(fieldValue);
							fieldValue = sdf.format(time).toString();
						}
						map.put(fieldName, fieldValue);
					}
				}
				StringBuilder transaction_log = new StringBuilder();
				transaction_log.append("【交易信息】\n");
				Set set = map.entrySet();
				for(Iterator iter = set.iterator(); iter.hasNext();) {
					Map.Entry entry = (Map.Entry)iter.next();
					String key = (String)entry.getKey();
					String value = (String)entry.getValue();
				   
					int offset = 0;
					String tab_names = "";
					for (int i = 0; i < tab_name.length()/2+1; i++) {
						if(tab_names.length()>0) {
							tab_names += ",";
						}
						
						tab_names += "'"+tab_name.substring(0, 1 + offset)+"'";
						
						offset += 2;
					}
					
					String sqlParent = "select q.TAB_NAME from TMS_COM_TAB q where q.TAB_NAME in ("+tab_names+")";
					
					SqlRowSet rsParent = ruleDao.query(sqlParent);
					String tableName = "";
					String str = "";
					while (rsParent.next()){
						tableName += str + "TAB_NAME='" + rsParent.getString("TAB_NAME") + "'";
						str = " or ";
					}
					String sql = "select NAME,TYPE,CODE from TMS_COM_FD where (" + tableName + ") and REF_NAME = '"+ key +"'";
					SqlRowSet rs = ruleDao.query(sql);
					String name = "";
					String type = "";
					String code = "";
					while (rs.next()){
						name = rs.getString("NAME");
						type = rs.getString("TYPE");
						code = rs.getString("CODE");
					}
					if(type.equals("code"))	{
						String sqlCode = "select CODE_VALUE from CMC_CODE where CATEGORY_ID='" + code + "' and CODE_KEY='"+ value +"'";
						SqlRowSet rsCode = ruleDao.query(sqlCode);

						while (rsCode.next())
						{
							value = rsCode.getString("CODE_VALUE");
						}
					}
					transaction_log.append(name + "：").append(value).append(",");
				}
				transaction_log.append("\n");
				log.debug(transaction_log.toString());
			}
		}
	}

	/**
	 * 方法描述:报警信息
	 * 
	 * @param m_alert
	 */
	public static void alert_log(db_alert m_alert, float ret)
	{
		float score = (float)(int)(ret*100)/100; //取两位小数 后面舍弃
//		DecimalFormat df = new DecimalFormat(".00");
//		String score = df.format(ret); //取两位小数 四舍五入
//		int score = Math.round(ret); //取整 四舍五入
		if (DEBUG_ENABLED)
		{
			// 处置方式
			String sql = "select CODE_VALUE from CMC_CODE where CATEGORY_ID='tms.rule.disposal' and CODE_KEY='"
					+ m_alert.disposal + "'";
			SqlRowSet rs = ruleDao.query(sql);
			String code_value = null;

			while (rs.next())
			{
				code_value = rs.getString("CODE_VALUE");
			}

			StringBuilder alert_log = new StringBuilder();
			alert_log.append("【报警信息】\n");
			alert_log.append("报警单id：").append(m_alert.alertid).append(",");// 报警单ID
			alert_log.append("流水号：").append(m_alert.txncode).append(",");// 流水号
			alert_log.append("处置方式：").append(code_value).append(",");// 处置方式
			alert_log.append("规则触发数：").append(m_alert.trigrulenum).append(",");// 触发规则数
			alert_log.append("规则命中数：").append(m_alert.hitrulenum).append(",");// 命中规则数
			alert_log.append("分值：").append(score);// 分值
			alert_log.append("\n");
			log.debug(alert_log.toString());
		}
	}

	/**
	 * 方法描述:规则信息
	 * 
	 * @param m_hit_rules
	 */
	public static void rule_log(List<db_rule_hit> m_hit_rules)
	{
		if (DEBUG_ENABLED)
		{
			if (m_hit_rules != null)
			{
				db_tab.cache tab_c = db_cache.get().table();

				StringBuilder rule_log = new StringBuilder();
				rule_log.append("【规则信息】\n").append("命中数：").append(m_hit_rules.size()).append(" ");
				for (int i = 0; i < m_hit_rules.size(); i++)
				{
					db_rule_hit rule = m_hit_rules.get(i);
					rule_log.append("\n(").append(i + 1).append("),");
					rule_log.append("规则ID：").append(rule.ruleid).append(",");// 规则ID
					rule_log.append("规则代码：").append(rule.dr.code).append(",");// 规则代码
					rule_log.append("规则名称：").append(rule.dr.name).append(",");// 规则名称
					rule_log.append("规则描述：").append(rule.dr.desc).append(",");// 规则描述
					rule_log.append("规则条件：").append(rule.dr.cond).append(",");// 规则条件
					rule_log.append("规则分值：").append(rule.dr.score).append(",");// 规则分值
					rule_log.append("所属交易：").append(tab_c.get(rule.dr.txn_name).tab_desc).append(",");// 所属交易
					rule_log.append("提示信息：").append(rule.message).append(",");// 规则提示信息
					rule_log.append("规则执行时间：").append(rule.numtimes).append("ms");// 规则执行时间
				}
				rule_log.append("\n");
				log.debug(rule_log.toString());
			}
		}
	}

	/**
	 * 方法描述:处置信息
	 * 
	 * @param ps
	 */
	public static void ps_log(db_process ps)
	{
		if (DEBUG_ENABLED)
		{
			db_tab.cache tab_c = db_cache.get().table();

			StringBuilder ps_log = new StringBuilder();
			ps_log.append("【处置信息】\n");
			ps_log.append("处置ID：").append(ps.ps_id).append(",");// 处置ID
			ps_log.append("处置序号：").append(ps.ps_order).append(",");// 处置序号
			ps_log.append("处置名称：").append(ps.ps_name).append(",");// 处置名称
			ps_log.append("处置条件：").append(ps.ps_cond).append(",");// 处置条件
			ps_log.append("处置值：").append(ps.ps_value).append(",");// 处置值
			ps_log.append("所属交易：").append(tab_c.get(ps.ps_txn).tab_desc);// 所属交易
			ps_log.append("\n");
			log.debug(ps_log.toString());
		}
	}

	/**
	 * 方法描述:动作信息
	 * 
	 * @param ac
	 */
	public static void ac_log(db_action ac)
	{

		if (DEBUG_ENABLED)
		{
			db_tab.cache tab_c = db_cache.get().table();

			StringBuilder ac_log = new StringBuilder();
			ac_log.append("【动作信息】\n");
			ac_log.append("动作ID：").append(ac.ac_id).append(",");// 动作ID
			ac_log.append("动作名称：").append(ac.ac_desc).append(",");// 动作名称
			ac_log.append("动作条件：").append(ac.ac_cond).append(",");// 动作条件
			ac_log.append("所属交易：").append(tab_c.get(ac.ac_txn).tab_desc).append(",");// 所属交易
			ac_log.append("交易属性：").append(ac.ac_src).append(",");// 交易属性
			ac_log.append("名单名称：").append(ac.ac_dst);// 名单名称
			ac_log.append("\n\n");
			log.debug(ac_log.toString());
		}
	}

	/**
	 * 方法描述:开关信息
	 * 
	 * @param sw
	 */
	public static void sw_log(db_sw sw)
	{

		if (DEBUG_ENABLED)
		{
			// 开关类型
			String sql = "select CODE_VALUE from CMC_CODE where CATEGORY_ID='tms35.model.switch.type' and CODE_KEY='"
					+ sw.sw_type + "'";
			SqlRowSet rs = ruleDao.query(sql);
			String code_value = null;

			while (rs.next())
			{
				code_value = rs.getString("CODE_VALUE");
			}

			db_tab.cache tab_c = db_cache.get().table();

			StringBuilder ac_log = new StringBuilder();
			ac_log.append("【开关信息】\n");
			ac_log.append("开关ID：").append(sw.sw_id).append(",");// 开关ID
			ac_log.append("开关顺序：").append(sw.sw_order).append(",");// 开关ID
			ac_log.append("开关名称：").append(sw.sw_desc).append(",");// 开关名称
			ac_log.append("所属交易：").append(tab_c.get(sw.sw_txn).tab_desc).append(",");// 所属交易
			ac_log.append("开关条件：").append(sw.sw_cond).append(",");// 开关条件
			ac_log.append("开关类型：").append(code_value);// 开关类型
			ac_log.append("\n\n");
			log.debug(ac_log.toString());
		}
	}
}
