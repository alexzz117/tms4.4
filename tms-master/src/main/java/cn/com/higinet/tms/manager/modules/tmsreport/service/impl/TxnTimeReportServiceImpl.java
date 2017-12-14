package cn.com.higinet.tms.manager.modules.tmsreport.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.modules.common.SqlWhereHelper;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.tmsreport.service.TxnTimeReportService;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;

@Transactional
@Service("txnTimeReportService")
public class TxnTimeReportServiceImpl extends ApplicationObjectSupport implements TxnTimeReportService {
	@Autowired
	private SimpleDao reportSimpleDao;
	
	public void setReportSimpleDao(SimpleDao reportSimpleDao) {
		this.reportSimpleDao = reportSimpleDao;
	}

	@Autowired
	private SqlMap tmsSqlMap;
	
	/**
	 * 根据查询条件查询时间段内(默认当月)每天交易信息 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> listTxnTimeReport(Map<String, String> conds) {
		Map<String,Object> sql_conds = new HashMap<String, Object>();
		String sql = getQuerySql(conds,sql_conds);
		Page<Map<String, Object>> page = reportSimpleDao.pageQuery(sql, sql_conds, new Order().desc("RISK_EVAL_NUMBER"));
		//对交易名称处理
		for (Map<String, Object> map : page.getList()) {
			String txnid = MapUtil.getString(map, "TXNID");
			String txnname = getTxnName(txnid,"TAB_DESC", true);
			map.put("TXNNAME", txnname);
		}
		return page;
	}
	/**
	 * 获取交易名称，完整路径
	 * @param 	txnId		交易名称
	 * @param 	txnField	交易名称字段
	 * @param	fullPath	是否显示全路径
	 * @return
	 */
	private String getTxnName(String txnId, String txnField, boolean fullPath) {
		Map<String,Object> sql_conds = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		sb.append("select " + TMS_COM_TAB.TAB_NAME +", " + TMS_COM_TAB.TAB_DESC +" from " + TMS_COM_TAB.TABLE_NAME);
		if(fullPath){
			String[] split = TransCommon.cutToIds(txnId);
			StringBuffer sbId = new StringBuffer();
			for (int i = 1; i < split.length; i++) {
				sbId.append(":").append("TXNID").append(i);
				if(i<split.length -1){
					sbId.append(",");
				}
				sql_conds.put("TXNID"+i, split[i]);  
			}
			sb.append(" where " + TMS_COM_TAB.TAB_NAME + " in("+sbId.toString()+")");
		}else{
			sb.append(" where " + TMS_COM_TAB.TAB_NAME + " = :TXNID");
			sql_conds.put("TXNID", txnId);
		}
		sb.append(" ORDER BY "+ TMS_COM_TAB.TAB_NAME +" DESC ");
		List<Map<String, Object>> tranDef = reportSimpleDao.queryForList(sb.toString(),sql_conds);
		
		String txnName = "";
		for (Map<String, Object> tran : tranDef) {
			String parentTxnName = MapUtil.getString(tran, txnField);
			txnName = parentTxnName + "-" + txnName;
		}
		if(!CmcStringUtil.isBlank(txnName)){
			txnName = txnName.substring(0, txnName.length()-1);
		}
		return txnName;
	}
	public String getRate(int numerator,int denominator) {
		if (numerator == 0) {
			return "0.00%";
		}
		BigDecimal cc = new BigDecimal((Float
				.intBitsToFloat(numerator)* 100)
				/ Float.intBitsToFloat(denominator)).setScale(2,
				BigDecimal.ROUND_HALF_UP);
		return String.valueOf(cc.floatValue())+"%";
	}
	
	/**
	 * 根据导出条件，查询时间段内交易告警信息，并将信息导出到excel文件
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> exportList(Map<String, String> conds) {
		Map<String,Object> sql_conds = new HashMap<String, Object>();
		String sql = getQuerySql(conds,sql_conds);
		sql += " order by RISK_EVAL_NUMBER desc";
		List<Map<String, Object>> result =  reportSimpleDao.queryForList(sql.toString(), sql_conds);
		
		//对交易名称处理
		for (Map<String, Object> map : result) {
			String txnid = MapUtil.getString(map, "TXNID");
			String txnname = getTxnName(txnid,"TAB_DESC", true);
			map.put("TXNNAME", txnname);
		}
		return result;
	}
	/**
	 * 根据查询条件，获取相应的数据集，并组装成展示图形所需要的字符串
	 * @param conds			查询条件
	 * @param dataList		数据集，如果数据集不为空，则不再重新查询，否则根据查询条件查询
	 * @return
	 */
	public List<Map<String, Object>> getChartData(List<Map<String, Object>> dataList,
			Map<String, String> conds) {
		Map<String,Object> sql_conds = new HashMap<String, Object>();
		String target = CmcStringUtil.isBlank(conds.get("target")) ? "_NUMBER" : conds.get("target");
//		String shape = StringUtil.isBlank(conds.get("shape")) ? "ColumShape" : conds.get("shape");
		String tops = CmcStringUtil.isBlank(conds.get("tops")) ? "10" : conds.get("tops");
		List<Map<String, Object>> result = null;
		String[] target_arr = target.split(",");
		Order order = new Order();
		for (int i = 0; i < target_arr.length; i++) {
			order.desc("RISK_EVAL"+target_arr[i]);
			order.desc("RISK_CFM"+target_arr[i]);
		}
		if(dataList == null || dataList.isEmpty()){
			String sql = getQuerySql(conds,sql_conds);
//			sql = SqlUtil.makeQuerySqlByOrder(sql, new Order().desc(target));
			Page<Map<String, Object>>  page = reportSimpleDao.pageQuery(sql, sql_conds, 1, Integer.parseInt(tops), order);
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
//		Map<String, Object> info = new HashMap<String, Object>();
//		
//		info.put("xname", "日 期("+startTime+" 至 "+endTime+")");
//		info.put("name", "ALERTDATE");
//		info.put("value", target);
//		info.put("caption", ReportStaticParameter.txnTargetMap.get(target));
		
//		ChartUtil chartUtil  = this.getApplicationContext().getBean(shape, ChartUtil.class);
//		String chartStrs = chartUtil.joinDataStr(result, info);
		//对交易名称处理
		for (Map<String, Object> map : result) {
			String txnid = MapUtil.getString(map, "TXNID");
			String txnname = getTxnName(txnid,"TAB_DESC", true);
			map.put("TXNNAME", txnname);
		}
		return result;
	}
	/**
	 * @param conds
	 * @return
	 */
	private String getQuerySql(Map<String, String> conds ,Map<String,Object> sql_conds) {
		String txnIds = conds.get("txnIds");
		String countrycode = conds.get("countrycode");
		String regioncode = conds.get("regioncode");
		String citycode = conds.get("citycode");
		String startTime = conds.get("startTime");
		String endTime = conds.get("endTime");
		
		StringBuffer sql = new StringBuffer();
		sql.append("select s.* from (");
		sql.append(tmsSqlMap.getSql("report.txn.time.list"));
		StringBuffer condition = new StringBuffer();

		if(!CmcStringUtil.isBlank(txnIds)){
			String[] split = txnIds.split(",");
			StringBuffer sb = new StringBuffer();
			for (int i = 1; i < split.length; i++) {
				sb.append(":").append("TXNID").append(i);
				if(i<split.length -1){
					sb.append(",");
				}
				sql_conds.put("TXNID"+i, split[i]);
			}
			condition.append(" and r.TXNID in ( "+sb.toString() +" )  ");
		}
		if(!CmcStringUtil.isBlank(countrycode) && !"0".equals(countrycode)){
			condition.append(" and r.COUNTRYCODE =:COUNTRYCODE ");
			sql_conds.put("COUNTRYCODE", countrycode);
		}
		if(!CmcStringUtil.isBlank(regioncode) && !"0".equals(regioncode)){
			condition.append(" and r.REGIONCODE =:REGIONCODE  ");
			sql_conds.put("REGIONCODE", regioncode);
		}
		if(!CmcStringUtil.isBlank(citycode) && !"0".equals(citycode)){
			condition.append(" and r.CITYCODE =:CITYCODE ");
			sql_conds.put("CITYCODE", citycode);
		}
		if(!CmcStringUtil.isBlank(startTime)){
			startTime = CalendarUtil.transFormFormatToAnother(startTime, CalendarUtil.FORMAT11, CalendarUtil.FORMAT17);
			condition.append(" and r.TIME >= :STARTTIME  ");
			sql_conds.put("STARTTIME", startTime);
		}
		if(!CmcStringUtil.isBlank(endTime)){
			endTime = CalendarUtil.transFormFormatToAnother(endTime, CalendarUtil.FORMAT11, CalendarUtil.FORMAT17);
			condition.append(" and r.TIME <= :ENDTIME  ");
			sql_conds.put("ENDTIME", endTime);
		}
		sql.append(condition).append(" ").append(" GROUP BY r.TXNID ")
		.append(" ORDER BY r.TXNID) s ");
//		.append(tmsSqlMap.getSql("report.date.txn.star.alert"))
//		.append(" ")
//		.append(condition).append(") b) c");
		return sql.toString();
	}
}
