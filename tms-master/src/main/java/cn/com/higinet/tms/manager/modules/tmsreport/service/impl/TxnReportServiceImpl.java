package cn.com.higinet.tms.manager.modules.tmsreport.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import cn.com.higinet.tms.manager.modules.common.IPLocationService;
import cn.com.higinet.tms.manager.modules.common.SqlWhereHelper;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.tmsreport.common.ChartUtil;
import cn.com.higinet.tms.manager.modules.tmsreport.common.ReportCompare;
import cn.com.higinet.tms.manager.modules.tmsreport.common.ReportConstant;
import cn.com.higinet.tms.manager.modules.tmsreport.common.ReportStaticParameter;
import cn.com.higinet.tms.manager.modules.tmsreport.common.RptDBConstant;
import cn.com.higinet.tms.manager.modules.tmsreport.service.DisposalService;
import cn.com.higinet.tms.manager.modules.tmsreport.service.TxnReportService;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;


@Transactional
@Service("txnReportService")
public class TxnReportServiceImpl extends ApplicationObjectSupport implements TxnReportService {

	@Autowired
	private SimpleDao reportSimpleDao;
	
	@Autowired
	private IPLocationService ipLocationService;
	
	@Autowired
	private SqlMap tmsSqlMap;
	
	@Autowired
	private DisposalService disposalService;
	
	/**
	 * 根据查询条件查询所有交易告警数据信息 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> listTxnReport(Map<String, String> conds) {
		Map<String,Object> sql_conds = new HashMap<String, Object>();
//		String target = StringUtil.isBlank(conds.get("target")) ? "ALERTNUMBER" : conds.get("target");
		String sql = getTxnReportSQL(conds,sql_conds);
//		sql = SqlUtil.makeQuerySqlByOrder(sql, new Order().desc(target));
//		Page<Map<String, Object>> page = reportSimpleDao.pageQuery(sql, conds, new Order().desc(target));
		List<Map<String, Object>> reportList = reportSimpleDao.queryForList(sql,sql_conds);
//		//对交易名称处理
//		for (Map<String, Object> map : page.getList()) {
//			String txnid = MapUtil.getString(map, "TXNID");
//			String txnname = getTxnName(txnid,"TAB_DESC", true);
//			map.put("TXNNAME", txnname);
//		}
		Page<Map<String, Object>> page = new Page<Map<String,Object>>();
		page.setList(convertTxnList(reportList));
		return page;
	}
	private List<Map<String, Object>> convertTxnList(List<Map<String, Object>> reportList){
		List<Map<String, Object>> list = new  ArrayList<Map<String,Object>>(128);  
		Map<String,Map<String, Object>> reportMap = new  LinkedHashMap<String,Map<String,Object>>(128);  
		//查询所有处置方式
		//获取处置代码key-valuetms_com_disposal
		List<Map<String, Object>> pslist = disposalService.queryList();
		Map<String, Object> total = new HashMap<String, Object>();
		total.put("TXNID", "REPORTTXNTOTAL");
		total.put("TXNNAME", "总计");
		//动态将纵向数据横向显示		
		for(Map<String, Object> map :reportList){
			String txnId = MapUtil.getString(map, "TXNID");
			Map<String, Object> report = new HashMap<String, Object>(16);
			String disposal = MapUtil.getString(map, "DISPOSAL");
			if (!reportMap.containsKey(txnId)) 
			{
				report.put("TXNID", txnId);
				for(Map<String, Object> code :pslist){
					String key = MapUtil.getString(code, "DP_CODE");
					report.put(key+ReportConstant.REPORT_PS_NUM, 0);
					report.put(key+ReportConstant.REPORT_PS_NUM_FD, 0);
					report.put(key+ReportConstant.REPORT_PS_NUM_NFD, 0);
//					report.put(key+"_RATE", "0.00");
//					report.put(key+"_FRAUDRATE", "0.00");
//					report.put(key+"_NONFRAUDRATE", "0.00");
				}
				//对交易名称处理
				String txnname = getTxnName(txnId,"TAB_DESC", true);
				report.put("TXNNAME", txnname);
			}else{
				report = reportMap.get(txnId);
			}
			int txnnumber = MapUtil.getInteger(map, "TXNNUMBER");
			report.put("TXNNUMBER", txnnumber+MapUtil.getInteger(report, "TXNNUMBER"));
			total.put("TXNNUMBER", txnnumber+MapUtil.getInteger(total, "TXNNUMBER"));
//			report.put("DISPOSAL", disposal);
			int psNum = MapUtil.getInteger(map, "DISPOSALNUM");
			int fdNum = MapUtil.getInteger(map, "FRAUDNUMBER");
			int nfdNum = MapUtil.getInteger(map, "NONFRAUDNUMBER");
			String rep_ps_num = disposal+ReportConstant.REPORT_PS_NUM;
			String rep_ps_num_fd = disposal+ReportConstant.REPORT_PS_NUM_FD;
			String rep_ps_num_nfd = disposal+ReportConstant.REPORT_PS_NUM_NFD;
			
			report.put(rep_ps_num, psNum);
			report.put(rep_ps_num_fd, fdNum);
			report.put(rep_ps_num_nfd, nfdNum);
			
			total.put(rep_ps_num, psNum+MapUtil.getInteger(total, rep_ps_num));
			total.put(rep_ps_num_fd, fdNum+MapUtil.getInteger(total, rep_ps_num_fd));
			total.put(rep_ps_num_nfd, nfdNum+MapUtil.getInteger(total, rep_ps_num_nfd));

			reportMap.put(txnId, report);
		}
		list.addAll(reportMap.values());
		ReportCompare.sort(list, "TXNNUMBER", true);
		if (list.size()>0) {
			list.add(total);
		}
		//计算比例		
		for(Map<String, Object> map :list){
//			String disposal = MapUtil.getString(map, "DISPOSAL");
			int txnnumber = MapUtil.getInteger(map, "TXNNUMBER");
			for(Map<String, Object> code :pslist){
				String key = MapUtil.getString(code, "DP_CODE");
				StringBuffer sb = new StringBuffer(32);
				map.put(key+ReportConstant.REPORT_PS_RATE, sb.append(MapUtil.getInteger(map, key+ReportConstant.REPORT_PS_NUM))
									.append("(").append(getRate(MapUtil.getInteger(map, key+ReportConstant.REPORT_PS_NUM),txnnumber)).append(")")
									.toString());
				sb.setLength(0);
				map.put(key+ReportConstant.REPORT_PS_RATE_FD,sb.append(MapUtil.getInteger(map, key+ReportConstant.REPORT_PS_NUM_FD))
									.append("(").append(getRate(MapUtil.getInteger(map, key+ReportConstant.REPORT_PS_NUM_FD),txnnumber)).append(")")
									.toString());
				sb.setLength(0);
				map.put(key+ReportConstant.REPORT_PS_RATE_NFD, sb.append(MapUtil.getInteger(map, key+ReportConstant.REPORT_PS_NUM_NFD))
									.append("(").append(getRate(MapUtil.getInteger(map, key+ReportConstant.REPORT_PS_NUM_NFD),txnnumber)).append(")")
									.toString());
				sb.setLength(0);
			}
		}
		return list;
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
	
	/**
	 * 获取系统所有的渠道列表
	 * @return
	 */
	public List<Map<String, Object>> getChannelList(String channelId) {
		if(channelId==null || "".equals(channelId) || "0".equals(channelId)){
			return reportSimpleDao.listAll(RptDBConstant.REPORT_THEME_PUB_CHANNEL,new Order().asc("ORDERBY"));
		}else{
			String sql = "select CHANNELID,CHANNELNAME,ORDERBY " +
					"from "+RptDBConstant.REPORT_THEME_PUB_CHANNEL+" where CHANNELID = ?";
			return reportSimpleDao.queryForList(sql, channelId);
		}
	}

	/**
	 * 查询所有国家
	 * @return
	 */
	public List<Map<String,Object>> getAllCountry(){
		String sql = "select * from " + ipLocationService.getLocationCurrName(RptDBConstant.REPORT_THEME_PUB_COUNTRY) + " order by COUNTRYORDER asc";
		return reportSimpleDao.queryForList(sql);
	}
	/**
	 * 根据国家代码获取所有的省份列表
	 * @param countryId
	 * @return
	 */
	public List<Map<String, Object>> getAllRegion(String countryId) {
		String sql  = "select * from "+ipLocationService.getLocationCurrName(RptDBConstant.REPORT_THEME_PUB_REGION)+ " where COUNTRYCODE = ? and REGIONCODE is not null order by REGIONNAME asc";
		return reportSimpleDao.queryForList(sql,countryId);
	}
	/**
	 * 根据省份代码获取该省份下所有的地区列表
	 * @param regionId
	 * @return
	 */
	public List<Map<String, Object>> getAllCity(String regionId) {
		String sql  = "select * from "+ipLocationService.getLocationCurrName(RptDBConstant.REPORT_THEME_PUB_CITY)+" where REGIONCODE= ? and CITYCODE is not null order by CITYNAME asc";
		return reportSimpleDao.queryForList(sql, regionId);
	}

	/**
	 * 根据查询条件，获取相应的数据集，并组装成展示图形所需要的字符串
	 * @param conds
	 * @return
	 */
	public String getChartData(List<Map<String, Object>> dataList,Map<String, String> conds) {
		Map<String,Object> sql_conds = new HashMap<String, Object>();
		String target = CmcStringUtil.isBlank(conds.get("target")) ? "TXNNUMBER" : conds.get("target");
		String shape = CmcStringUtil.isBlank(conds.get("shape")) ? "ColumShape" : conds.get("shape");
		String tops = CmcStringUtil.isBlank(conds.get("tops")) ? "10" : conds.get("tops");
		List<Map<String, Object>> result = null;
		
		if(dataList == null || dataList.isEmpty()){
			String sql = getTxnReportSQL(conds,sql_conds);
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
		
		for(Map<String,Object> txn : result){
			String txnid = MapUtil.getString(txn, "TXNID");
			String txnname = getTxnName(txnid,"TAB_DESC", false);
			txn.put("TXNNAME", txnname);
		}
		
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("xname", "交易名称");
		info.put("name", "TXNNAME");
		info.put("value", target);
		info.put("caption", ReportStaticParameter.txnTargetMap.get(target));
		
		ChartUtil chartUtil  = getApplicationContext().getBean(shape, ChartUtil.class);
		String chartStrs = chartUtil.joinDataStr(result, info);
		
		return chartStrs;
	}

	/**
	 * 根据导出条件，查询交易告警信息，并将信息导出到excel文件
	 * @param conds
	 * @return
	 */
	public List<Map<String,Object>> exportList(Map<String, String> conds){
		Map<String,Object> sql_conds = new HashMap<String, Object>();
		String sql = getTxnReportSQL(conds,sql_conds);
		List<Map<String, Object>> txnlist = reportSimpleDao.queryForList(sql, sql_conds);
		return convertTxnList(txnlist);
	}
	
	private String getTxnReportSQL(Map<String, String> conds,Map<String,Object> sql_conds){
		String txnIds = conds.get("txnIds");
		String countrycode = conds.get("countrycode");
		String regioncode = conds.get("regioncode");
		String citycode = conds.get("citycode");
		String reportType = conds.get("reporttype");
		
		StringBuffer sql =new StringBuffer();
		sql.append("select c.* from (");
		sql.append(tmsSqlMap.getSql("report.txn.list"));
		StringBuffer condition = new StringBuffer();
		
		if(!CmcStringUtil.isBlank(txnIds)){
			String[] split = txnIds.split(",");
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < split.length; i++) {
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
		if("dayreport".equals(reportType)){//日报
			String startTime = conds.get("startTime");
			String endTime = conds.get("endTime");
			if(!CmcStringUtil.isBlank(startTime)){
				startTime = CalendarUtil.transFormFormatToAnother(startTime, CalendarUtil.FORMAT11, CalendarUtil.FORMAT17);
				condition.append(" and r.TIME >= :STARTTIME  ");
				sql_conds.put("STARTTIME", startTime);
			}
			if(!CmcStringUtil.isBlank(endTime)){
				endTime = CalendarUtil.transFormFormatToAnother(endTime, CalendarUtil.FORMAT11, CalendarUtil.FORMAT17);
				condition.append(" and r.TIME <= :ENDTIME ");
				sql_conds.put("ENDTIME", endTime);
			}
		}else if("monthreport".equals(reportType)){//月报
			String monthDate = conds.get("monthDate") +"%";
			if(!CmcStringUtil.isBlank(monthDate)){
				condition.append(" and r.TIME like :MONTHDATE  ");
				sql_conds.put("MONTHDATE", monthDate);
			}
		}else if("yearreport".equals(reportType)){//年报
			String yearDate = conds.get("yearDate")+"%";
			if(!CmcStringUtil.isBlank(yearDate)){
				condition.append(" and r.TIME like :YEARDATE  ");
				sql_conds.put("YEARDATE", yearDate);
			}
		}
		sql.append(condition);
		sql.append(" ").append(tmsSqlMap.getSql("report.txn.fraud.list"))
				.append(" ").append(condition)
				.append(" GROUP BY  r.TXNID,r.DISPOSAL ) b ON a.TXNID = b.TXNID AND a.DISPOSAL = b.DISPOSAL ")
				.append(") s WHERE s.TXNNUMBER > 0 ORDER BY s.TXNNUMBER desc");
		sql.append(") c ");
//		logger.debug(sql.toString());
		return sql.toString();
	}
}