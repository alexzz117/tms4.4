package cn.com.higinet.tmsreport.web.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.cmc.util.StringUtil;
import cn.com.higinet.rapid.base.common.BaseConstant;
import cn.com.higinet.rapid.base.dao.Order;
import cn.com.higinet.rapid.base.dao.Page;
import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.rapid.base.dao.SqlMap;
import cn.com.higinet.tms35.manage.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms35.manage.common.SqlWhereHelper;
import cn.com.higinet.tms35.manage.common.util.CalendarUtil;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.tran.TransCommon;
import cn.com.higinet.tmsreport.web.common.ChartUtil;
import cn.com.higinet.tmsreport.web.common.ReportStaticParameter;
import cn.com.higinet.tmsreport.web.service.RuleReportService;

@Transactional
@Service("ruleReportService")
public class RuleReportServiceImpl extends ApplicationObjectSupport  implements RuleReportService {

	@Autowired
	private SimpleDao reportSimpleDao;
	
	public void setReportSimpleDao(SimpleDao reportSimpleDao) {
		this.reportSimpleDao = reportSimpleDao;
	}

	@Autowired
	private SqlMap tmsSqlMap;
	
	public Page<Map<String, Object>> listRuleReport(Map<String, String> conds) {
		Map<String,Object> sql_conds = new HashMap<String, Object>();
		String target = conds.get("target");
		String sql = getRuleReportSQL(conds,sql_conds);
//		System.out.println(sql);
//		sql = SqlUtil.makeQuerySqlByOrder(sql, new Order().desc(target));
		Page<Map<String, Object>> page = reportSimpleDao.pageQuery(sql, sql_conds, new Order().desc(target));
		//对交易名称处理
		for (Map<String, Object> map : page.getList()) {
			String txnid = MapUtil.getString(map, "TXNID");
			String txnname = getFullTxnPath(txnid,"tab_desc");
			map.put("TXNNAME", txnname);
		}
		return page;
	}
	
	/**
	 * 获取交易名称，完整路径
	 * @param authTxn
	 * @return
	 */
	private String getFullTxnPath(String authTxn, String txnField) {
		StringBuffer sb = new StringBuffer();
		/*sb.append("select " + TMS_COM_TAB.TAB_NAME +", " + TMS_COM_TAB.TAB_DESC +" from " + TMS_COM_TAB.TABLE_NAME + " start with " + TMS_COM_TAB.TAB_NAME + "=?");
		sb.append(" connect by prior " + TMS_COM_TAB.PARENT_TAB + "=" + TMS_COM_TAB.TAB_NAME);*/
		
		sb.append("select " + TMS_COM_TAB.TAB_NAME +", " + TMS_COM_TAB.TAB_DESC +" from " + TMS_COM_TAB.TABLE_NAME + " where " + TMS_COM_TAB.TAB_NAME + " in("+TransCommon.arr2str(TransCommon.cutToIds(authTxn))+") order by TAB_NAME desc");
		
		List<Map<String, Object>> fartherTranDef = reportSimpleDao.queryForList(sb.toString());
		
		String txnName = "";
		for (Map<String, Object> tran : fartherTranDef) {
			String parentTxnName = MapUtil.getString(tran, txnField);
			txnName = parentTxnName + "-" + txnName;
		}
		if(!txnName.equals("")){
			txnName = txnName.substring(0, txnName.length()-1);
		}
		return txnName;
	}

	public List<Map<String, Object>>  exportList(Map<String, String> conds) {
		Map<String,Object> sql_conds = new HashMap<String, Object>();
		String sql = getRuleReportSQL(conds,sql_conds);
		String target = conds.get("target");
		sql += " ORDER BY "+target+" DESC";
//		System.out.println(sql);
		List<Map<String, Object>> exportList = reportSimpleDao.queryForList(sql, sql_conds);
		for (Map<String, Object> map : exportList) {
			String txnid = MapUtil.getString(map, "TXNID");
			String txnname = getFullTxnPath(txnid,"tab_desc");
			map.put("TXNNAME", txnname);
		}
		return exportList;
	}

	public List<Map<String, Object>> getChartData(List<Map<String, Object>> dataList,
			Map<String, String> conds) {
		Map<String,Object> sql_conds = new HashMap<String, Object>();
		String target = StringUtil.isBlank(conds.get("target")) ? "TRIGGERNUM" : conds.get("target");
//		String shape = StringUtil.isBlank(conds.get("shape")) ? "ColumShape" : conds.get("shape");
		String tops = StringUtil.isBlank(conds.get("tops")) ? "10" : conds.get("tops");
		List<Map<String, Object>> result = null;
		if(dataList==null || dataList.size()==0){
			String sql = getRuleReportSQL(conds,sql_conds);
//			sql = SqlUtil.makeQuerySqlByOrder(sql, new Order().desc(target));
			Page<Map<String, Object>>  page = reportSimpleDao.pageQuery(sql, sql_conds, 1, Integer.parseInt(tops), new Order().desc(target));
			result = page.getList();
		}else{
			result = new ArrayList<Map<String,Object>>();
			if(dataList.size()<=Integer.parseInt(tops)){//如果数据集中的条数小于等于图形设置的展示条数，则去全部，否则取其中的tops条
				result.addAll(dataList);
			}else{
				for(int i=0;i<Integer.parseInt(tops);i++){
					result.add(dataList.get(i));
				}
			}
		}
		
//		for(Map<String,Object> txn:result){
//			txn.put("RULENAME", MapUtil.getString(txn, "RULENAME"));
//		}
		for (Map<String, Object> map : result) {
			String txnid = MapUtil.getString(map, "TXNID");
			String txnname = getFullTxnPath(txnid,"tab_desc");
			map.put("TXNNAME", txnname);
		}
//		Map<String, Object> info = new HashMap<String, Object>();
//		info.put("xname", "规则名称");
//		info.put("name", "RULENAME");
//		info.put("value", target);
//		info.put("caption", ReportStaticParameter.ruleTargetMap.get(target));
		
//		ChartUtil chartUtil  = getApplicationContext().getBean(shape, ChartUtil.class);
//		String chartStrs = chartUtil.joinDataStr(result, info);
		
		return result;
	}
	
	private String getRuleReportSQL(Map<String, String> conds ,Map<String,Object> sql_conds){
		
		
		sql_conds.put(BaseConstant.PAGE_INDEX, 1);
		sql_conds.put(BaseConstant.PAGE_SIZE, 1000000);

		String txnIds = conds.get("txnIds");
		String ruleName = '%' + conds.get("ruleName") + '%';
		String reportType = conds.get("reporttype");
		
		StringBuffer sql = new StringBuffer();
		sql.append("select c.* from (");
		sql.append(tmsSqlMap.getSql("report.rule.list"));
		StringBuffer condition = new StringBuffer();
		StringBuffer conditionRule = new StringBuffer();

		if(!StringUtil.isBlank(txnIds)){
			String[] split = txnIds.split(",");
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < split.length; i++) {
				sb.append(":").append("TXNID").append(i);
				if(i<split.length -1){
					sb.append(",");
				}
				sql_conds.put("TXNID"+i, split[i]);
			}
			
			condition.append(" and al.TXNID in ( "+sb.toString() +" )  ");
		}
		if("dayreport".equals(reportType)){//日报
			String startTime = conds.get("startTime");
			String endTime = conds.get("endTime");
			if(!StringUtil.isBlank(startTime)){
				startTime = CalendarUtil.transFormFormatToAnother(startTime, CalendarUtil.FORMAT11, CalendarUtil.FORMAT17);
				condition.append(" and al.TIME >= :STARTTIME  ");
				sql_conds.put("STARTTIME", startTime);
			}
			if(!StringUtil.isBlank(endTime)){
				endTime = CalendarUtil.transFormFormatToAnother(endTime, CalendarUtil.FORMAT11, CalendarUtil.FORMAT17);
				condition.append(" and al.TIME <= :ENDTIME ");
				sql_conds.put("ENDTIME", endTime);
			}
		}else if("monthreport".equals(reportType)){//月报
			String monthDate = conds.get("monthDate")+"%";
			if(!StringUtil.isBlank(monthDate)){
				condition.append(" and al.TIME like :MONTHDATE  ");
				sql_conds.put("MONTHDATE", monthDate);
			}
		}else if("yearreport".equals(reportType)){//年报
			String yearDate = conds.get("yearDate")+"%";
			if(!StringUtil.isBlank(yearDate)){
				condition.append(" and al.TIME like :YEARDATE  ");
				sql_conds.put("YEARDATE", yearDate);
			}
		}
		conditionRule.append(condition);
		if(!StringUtil.isBlank(ruleName)){
//			conditionRule.append(" and r.RULE_SHORTDESC like '%"+ruleName+"%'  ");
			conditionRule.append(" and r.RULE_SHORTDESC like :RULENAME ");
			sql_conds.put("RULENAME", ruleName);
		}
		sql.append(conditionRule)
		.append(" group by r.RULE_SHORTDESC, c.CHANNELNAME, txn.TAB_NAME, r.RULE_SCORE) a1, ")
		.append("(select sum(a2_1.TRIGGER_NUMBER) TRIGGERNUMBER, a2_1.TXNID from (select distinct al.TXNID, al.TRIGGER_NUMBER, al.TIME")
		.append(" from REPORT_THEME_RULE_STAR_ALERT al left join REPORT_THEME_PUB_TXN txn on al.TXNID = txn.TAB_NAME")
		.append(" left join REPORT_THEME_PUB_RULE r on al.RULEID = r.RULE_ID where 1=1 ").append(condition)
		.append(") a2_1 group by a2_1.TXNID) a2 where a1.TXNID = a2.TXNID) a, ")
		.append("(select sum(b1.TRIGGER_NUMBER) TRIGGERSUM, sum(b1.RULEHITSUM) HITSUM from (select al.TRIGGER_NUMBER, al.TXNID, al.TIME, ")
		.append("sum(al.HIT_NUMBER) RULEHITSUM from REPORT_THEME_RULE_STAR_ALERT al left join REPORT_THEME_PUB_TXN txn on ")
		.append("al.TXNID = txn.TAB_NAME left join REPORT_THEME_PUB_RULE r on al.RULEID = r.RULE_ID where 1 = 1 ")
		.append(condition).append(" group by al.TRIGGER_NUMBER, al.TXNID ,al.TIME) b1) b) c");
		return sql.toString();
	}
}
