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
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.modules.common.IPLocationService;
import cn.com.higinet.tms.manager.modules.common.SqlWhereHelper;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.tmsreport.common.ReportCompare;
import cn.com.higinet.tms.manager.modules.tmsreport.common.ReportConstant;
import cn.com.higinet.tms.manager.modules.tmsreport.service.AreaReportService;
import cn.com.higinet.tms.manager.modules.tmsreport.service.DisposalService;

@Transactional
@Service("areaReportService")
public class AreaReportServiceImpl extends ApplicationObjectSupport implements AreaReportService {
	
	@Autowired
	private SimpleDao reportSimpleDao;
	@Autowired
	private IPLocationService ipLocationService;
	@Autowired
	private DisposalService disposalService;
	public void setReportSimpleDao(SimpleDao reportSimpleDao) {
		this.reportSimpleDao = reportSimpleDao;
	}
	

	@Autowired
	private SqlMap tmsSqlMap;
	
	/**
	 * 根据查询条件查询地区告警数据信息 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> listAreaReport(Map<String, String> conds) {
		Map<String,Object> sql_conds = new HashMap<String, Object>();
//		String target = conds.get("target");
//		String relSql = " GROUP BY re.REGIONNAME, re.REGIONCODE) s WHERE s.ALERTNUMBER > 0) a, ("+tmsSqlMap.getSql("report.date.txn.star.alert")+" ";
//		String relSql = " GROUP BY re.REGIONNAME, re.REGIONCODE,r.DISPOSAL) s WHERE s.TXNNUMBER > 0) c ORDER BY c.TXNNUMBER DESC";
//		String sql = getAreaReportSQL(conds, "report.area.list", relSql);
		String relSql = ") b ON a.REGIONCODE = b.REGIONCODE AND a.DISPOSAL = b.DISPOSAL ) s WHERE s.TXNNUMBER > 0 ORDER BY s.TXNNUMBER DESC ) c ";
		String sql = getAreaReportSQL(conds, "report.area.list","report.area.fraud.list","GROUP BY r.REGIONCODE,r.DISPOSAL", relSql,sql_conds);
//		sql = SqlUtil.makeQuerySqlByOrder(sql, new Order().desc(target));
//		return reportSimpleDao.pageQuery(sql, conds, new Order().desc(target));
//		System.out.println(sql);
		List<Map<String, Object>> reportList = reportSimpleDao.queryForList(sql,sql_conds);
		Page<Map<String, Object>> page = new Page<Map<String,Object>>();
		page.setList(convertTxnList(reportList,"REGIONCODE","REGIONNAME"));
		return page;
	}
	private List<Map<String, Object>> convertTxnList(List<Map<String, Object>> reportList,String id,String name){
		if(reportList ==  null || reportList.size() == 0 ){
			return new  ArrayList<Map<String,Object>>(0); 
		}
		List<Map<String, Object>> list = new  ArrayList<Map<String,Object>>(128);  
		Map<String,Map<String, Object>> reportMap = new  LinkedHashMap<String,Map<String,Object>>(128);  
		//查询所有处置方式
		//获取处置代码key-value
		List<Map<String, Object>> pslist = disposalService.queryList();
		Map<String, Object> total = new HashMap<String, Object>();
		total.put(id, "REPORTAREATOTAL");
		for(Map<String, Object> code :pslist){
			String key = MapUtil.getString(code, "DP_CODE");
			total.put(key+ReportConstant.REPORT_PS_NUM, 0);
			total.put(key+ReportConstant.REPORT_PS_NUM_FD, 0);
			total.put(key+ReportConstant.REPORT_PS_NUM_NFD, 0);
		}
		total.put(name, "总计");
		//动态将纵向数据横向显示		
		for(Map<String, Object> map :reportList){
			String regioncode = MapUtil.getString(map, id);
			Map<String, Object> report = new HashMap<String, Object>(16);
			String disposal = MapUtil.getString(map, "DISPOSAL");
			if (!reportMap.containsKey(regioncode)) 
			{
				report.put(id, regioncode);
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
//				String txnname = getTxnName(txnId,"TAB_DESC", true);
				report.put(name,  MapUtil.getString(map, name));
			}else{
				report = reportMap.get(regioncode);
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

			reportMap.put(regioncode, report);
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
	 * 根据查询条件，获取相应的数据集，并组装成展示图形所需要的字符串
	 * @param conds			查询条件
	 * @param dataList		数据集，如果数据集不为空，则不再重新查询，否则根据查询条件查询
	 * @return
	 */
	public String getChinaChartData(List<Map<String, Object>> dataList,
			Map<String, String> conds) {
//		String target = conds.get("target");
//		String shape = conds.get("shape");
//		
//		List<Map<String, Object>> result = null;
//		if(dataList==null || dataList.size()==0){
//			String relSql = " GROUP BY re.REGIONNAME, re.REGIONCODE,r.DISPOSAL) s WHERE s.ALERTNUMBER > 0) a, ( "+tmsSqlMap.getSql("report.date.txn.star.alert")+" ";
//			String sql = getAreaReportSQL(conds, "report.area.list", relSql);
//			result = reportSimpleDao.queryForList(sql, conds);
//		}else{
//			result = new ArrayList<Map<String,Object>>();
//			result.addAll(dataList);
//		}
//
//		//省份名称
//		Map<String,String> regNameMap = new HashMap<String,String>();
//		//省份简称
//		Map<String,String> regShortMap = new HashMap<String,String>();
//		String sql  = "select * from "+RptDBConstant.REPORT_THEME_PUB_REGION+" where COUNTRYCODE= 'CN'";
//		List<Map<String,Object>> regList =  reportSimpleDao.queryForList(sql);
//		for(Map<String,Object> reg :regList){
//			regNameMap.put(MapUtil.getString(reg, "REGIONCODE"), MapUtil.getString(reg, "REGIONNAME"));
//			regShortMap.put(MapUtil.getString(reg, "REGIONCODE"), MapUtil.getString(reg, "REGIONFORSHORT"));
//		}
//		
//		Map<String, Object> info = new HashMap<String, Object>();
//		info.put("value", target);
//		info.put("regName", regNameMap);
//		info.put("regShort", regShortMap);
//		
//		ChartUtil chartUtil  = getApplicationContext().getBean(shape, ChartUtil.class);
//		String chartStrs = chartUtil.joinDataStr(result, info);
//		
//		return chartStrs;
			return null;
	}
	
	/**
	 * 根据查询条件查询各市告警数据信息 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> listCityReport(Map<String, String> conds) {
		Map<String,Object> sql_conds = new HashMap<String, Object>();
//		String target = conds.get("target");
//		String relSql = " group by CITYNAME,r.CITYCODE ) a,( "+tmsSqlMap.getSql("report.date.txn.star.alert")+" ";
//		String relSql = " group by CITYNAME,r.CITYCODE,r.DISPOSAL ) s WHERE s.TXNNUMBER > 0 ) c ORDER BY c.TXNNUMBER DESC";
//		String sql = getAreaReportSQL(conds, "report.city.list", relSql);
		String relSql = ") b ON a.CITYCODE = b.CITYCODE AND a.DISPOSAL = b.DISPOSAL ) s WHERE s.TXNNUMBER > 0 ORDER BY s.TXNNUMBER DESC ) c "; 
		String sql = getAreaReportSQL(conds, "report.city.list","report.city.fraud.list","group by r.CITYCODE,r.DISPOSAL", relSql,sql_conds);
//		sql = SqlUtil.makeQuerySqlByOrder(sql, new Order().desc(target));
//		return reportSimpleDao.pageQuery(sql, conds, new Order().desc(target));
//		System.out.println(sql);
		List<Map<String, Object>> reportList = reportSimpleDao.queryForList(sql,sql_conds);
		Page<Map<String, Object>> page = new Page<Map<String,Object>>();
		page.setList(convertTxnList(reportList,"CITYCODE","CITYNAME"));
		return page;
	}
	
	/**
	 * 根据查询条件，获取相应的数据集，并组装前台图形展示所需要的字符串
	 * @param conds			查询条件
	 * @param dataList		数据集，如果数据集不为空，则不再重新查询，否则根据查询条件查询
	 * @return
	 */
	public String getChartData(List<Map<String, Object>> dataList ,Map<String, String> conds){
//		String target = conds.get("target");
//		String shape = conds.get("shape");
//		List<Map<String, Object>> result = null;
//		if(dataList==null || dataList.size()==0){
//			String relSql = " group by CITYNAME,r.CITYCODE ) a,( "+tmsSqlMap.getSql("report.date.txn.star.alert")+" ";
//			String sql = getAreaReportSQL(conds, "report.city.list", relSql);
//			String tops = StringUtil.isBlank(conds.get("tops")) ? "10" : conds.get("tops");
////			sql = SqlUtil.makeQuerySqlByOrder(sql, new Order().desc(target));
//			Page<Map<String, Object>>  page = reportSimpleDao.pageQuery(sql, conds,1, Integer.parseInt(tops), new Order().desc(target));
//			result = page.getList();
//		}else{
//			result = new ArrayList<Map<String,Object>>();
//			result.addAll(dataList);
//		}
//		
//		Map<String, Object> info = new HashMap<String, Object>();
//		info.put("xname", "地区名称");
//		info.put("name", "CITYNAME");
//		info.put("value", target);
//		info.put("caption", ReportStaticParameter.txnTargetMap.get(target));
//		
//		ChartUtil chartUtil  = getApplicationContext().getBean(shape, ChartUtil.class);
//		String chartStrs = chartUtil.joinDataStr(result, info);
//
//		return chartStrs;
		return null;
	}
	/**
	 * 根据导出条件，查询地区告警信息，并将信息导出到excel文件
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> exportList(Map<String, String> conds) {
		Map<String,Object> sql_conds = new HashMap<String, Object>();
//		String target = conds.get("target");
//		String relSql = " GROUP BY re.REGIONNAME, re.REGIONCODE "//ORDER BY " + target + " DESC
//				+ ") s WHERE s.ALERTNUMBER > 0) a, ( "+tmsSqlMap.getSql("report.date.txn.star.alert")+" ";
//		String relSql = " group by re.REGIONNAME, re.REGIONCODE,r.DISPOSAL ) s WHERE s.TXNNUMBER > 0 ) c ORDER BY c.TXNNUMBER DESC";
		String relSql = ") b ON a.REGIONCODE = b.REGIONCODE AND a.DISPOSAL = b.DISPOSAL ) s WHERE s.TXNNUMBER > 0 ORDER BY s.TXNNUMBER DESC ) c ";
		String sql = getAreaReportSQL(conds, "report.area.list","report.area.fraud.list","group by r.REGIONCODE,r.DISPOSAL", relSql,sql_conds);
//		System.out.println(sql);
		return convertTxnList(reportSimpleDao.queryForList(sql.toString(),sql_conds),"REGIONCODE","REGIONNAME");
	}

	/**
	 * 根据导出条件，查询城市告警信息，并将信息导出到excel文件
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> exportCityList(Map<String, String> conds){
		Map<String,Object> sql_conds = new HashMap<String, Object>();
//		String target = conds.get("target");
//		String relSql = " group by CITYNAME,r.CITYCODE "//order by " + target + " desc
//				+ " ) a,( "+tmsSqlMap.getSql("report.date.txn.star.alert")+" ";
		String relSql = ") b ON a.CITYCODE = b.CITYCODE AND a.DISPOSAL = b.DISPOSAL ) s WHERE s.TXNNUMBER > 0 ORDER BY s.TXNNUMBER DESC ) c "; 
		String sql = getAreaReportSQL(conds, "report.city.list","report.city.fraud.list","group by r.CITYCODE,r.DISPOSAL", relSql,sql_conds);
		return convertTxnList(reportSimpleDao.queryForList(sql.toString(), conds,sql_conds),"CITYCODE","CITYNAME");
	}
	
	private String getAreaReportSQL(Map<String, String> conds, String tmsSqlName, String extSql,String groupBySql,String relSql,Map<String,Object> sql_conds){
		String txnIds = conds.get("txnIds");
		String countrycode = conds.get("countrycode");
		String regioncode = conds.get("regioncode");
		String citycode = conds.get("citycode");
		String reportType = conds.get("reporttype");
		
		StringBuffer sql =new StringBuffer();
		sql.append("select c.* from (");
		sql.append(tmsSqlMap.getSql(tmsSqlName));
		
		sql = new StringBuffer(sql.toString().replace("REPORT_THEME_PUB_CITY", ipLocationService.getLocationCurrName("REPORT_THEME_PUB_CITY")));
		sql = new StringBuffer(sql.toString().replace("REPORT_THEME_PUB_REGION", ipLocationService.getLocationCurrName("REPORT_THEME_PUB_REGION")));
		
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
			String monthDate = conds.get("monthDate")+"%";
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
//		sql.append(condition).append(relSql).append(condition).append(") b) c");
//		sql.append(condition).append(relSql);
		sql.append(condition);
		sql.append(" ").append(groupBySql).append(tmsSqlMap.getSql(extSql))
				.append(" ").append(condition).append(" ").append(groupBySql).append(" ").append(relSql);
//				.append(") b ON a.ALERTDATE = b.ALERTDATE AND a.DISPOSAL = b.DISPOSAL ")
//				.append(") s WHERE s.TXNNUMBER > 0 ORDER BY s.ALERTDATE");
//				sql.append(") c ");
		return sql.toString();
	}

}
