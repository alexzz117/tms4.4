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

import cn.com.higinet.tms.manager.common.CodeDict;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.modules.common.SqlWhereHelper;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.tmsreport.common.ReportConstant;
import cn.com.higinet.tms.manager.modules.tmsreport.service.DisposalService;
import cn.com.higinet.tms.manager.modules.tmsreport.service.FraudReportService;

@Transactional
@Service("fraudReportService")
public class FraudReportServiceImpl extends ApplicationObjectSupport implements FraudReportService {
	@Autowired
	private SimpleDao reportSimpleDao;
	
	public void setReportSimpleDao(SimpleDao reportSimpleDao) {
		this.reportSimpleDao = reportSimpleDao;
	}

	@Autowired
	private CodeDict codeDict;
	@Autowired
	private SqlMap tmsSqlMap;
	@Autowired
	private DisposalService disposalService;
	
	/**
	 * 根据查询条件查询时间段内(默认当月)每天交易告警数据信息 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> listFraudReport(Map<String, String> conds) {
		Map<String,Object> sql_conds = new HashMap<String, Object>();
		String sql = getQuerySql(conds,sql_conds);
//		return reportSimpleDao.pageQuery(sql, conds, new Order().asc("ALERTDATE"));
//		System.out.println(sql);
		List<Map<String, Object>> reportList = reportSimpleDao.queryForList(sql,sql_conds);
		Page<Map<String, Object>> page = new Page<Map<String,Object>>();
		page.setList(convertTxnList(reportList,"FRAUDTYPE",true));
		return page;
	}
	private List<Map<String, Object>> convertTxnList(List<Map<String, Object>> reportList,String id,boolean isTotal){
		List<Map<String, Object>> list = new  ArrayList<Map<String,Object>>(128);  
		Map<String,Map<String, Object>> reportMap = new  LinkedHashMap<String,Map<String,Object>>(128);  
		//查询所有处置方式
		//获取处置代码key-value
		List<Map<String, Object>> pslist = disposalService.queryList();
		Map<String, String> fdmap = codeDict.getCodes("tms.alarm.fraudtype");
		Map<String, Object> total = new HashMap<String, Object>();
		if(isTotal){
			total.put("REPORTTOTAL", "REPORTAREATOTAL");
			for(Map<String, Object> code :pslist){
				String key = MapUtil.getString(code, "DP_CODE");
				total.put(key+ReportConstant.REPORT_PS_NUM, 0);
				total.put(key+ReportConstant.REPORT_PS_NUM_FD, 0);
				total.put(key+ReportConstant.REPORT_PS_NUM_NFD, 0);
			}
			total.put("FRAUDTYPE", "REPORTFRAUDTOTAL");
			total.put("FRAUDNAME", "总计");
		}
		//动态将纵向数据横向显示		
		for(Map<String, Object> map :reportList){
			String fmCode = MapUtil.getString(map, id);
			Map<String, Object> report = new HashMap<String, Object>(16);
			String disposal = MapUtil.getString(map, "DISPOSAL");
			if (!reportMap.containsKey(fmCode)) 
			{
				report.put(id, fmCode);
				String name = MapUtil.getString(fdmap, fmCode);
				report.put("FRAUDNAME", (name == "")?fmCode:name);
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
//				report.put(name,  MapUtil.getString(map, name));
			}else{
				report = reportMap.get(fmCode);
			}
			int txnnumber = MapUtil.getInteger(map, "TXNNUMBER");
			report.put("TXNNUMBER", txnnumber+MapUtil.getInteger(report, "TXNNUMBER"));
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
			if (isTotal) {
				total.put("TXNNUMBER", txnnumber+MapUtil.getInteger(total, "TXNNUMBER"));
				total.put(rep_ps_num, psNum+MapUtil.getInteger(total, rep_ps_num));
				total.put(rep_ps_num_fd, fdNum+MapUtil.getInteger(total, rep_ps_num_fd));
				total.put(rep_ps_num_nfd, nfdNum+MapUtil.getInteger(total, rep_ps_num_nfd));
			}

			reportMap.put(fmCode, report);
		}
		list.addAll(reportMap.values());
		if (isTotal && list.size()>0) {
			list.add(total);
		}
//		//计算比例		
//		for(Map<String, Object> map :list){
////			String disposal = MapUtil.getString(map, "DISPOSAL");
//			int txnnumber = MapUtil.getInteger(map, "TXNNUMBER");
//			for(Map<String, Object> code :pslist){
//				String key = MapUtil.getString(code, "CODE_KEY");
//				StringBuffer sb = new StringBuffer(32);
//				map.put(key+ReportConstant.REPORT_PS_RATE, sb.append(MapUtil.getInteger(map, key+ReportConstant.REPORT_PS_NUM))
//									.append("(").append(getRate(MapUtil.getInteger(map, key+ReportConstant.REPORT_PS_NUM),txnnumber)).append(")")
//									.toString());
//				sb.setLength(0);
//				map.put(key+ReportConstant.REPORT_PS_RATE_FD,sb.append(MapUtil.getInteger(map, key+ReportConstant.REPORT_PS_NUM_FD))
//									.append("(").append(getRate(MapUtil.getInteger(map, key+ReportConstant.REPORT_PS_NUM_FD),txnnumber)).append(")")
//									.toString());
//				sb.setLength(0);
//				map.put(key+ReportConstant.REPORT_PS_RATE_NFD, sb.append(MapUtil.getInteger(map, key+ReportConstant.REPORT_PS_NUM_NFD))
//									.append("(").append(getRate(MapUtil.getInteger(map, key+ReportConstant.REPORT_PS_NUM_NFD),txnnumber)).append(")")
//									.toString());
//				sb.setLength(0);
//			}
//		}
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
	 * 根据导出条件，查询时间段内交易告警信息，并将信息导出到excel文件
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> exportList(Map<String, String> conds) {
		Map<String,Object> sql_conds = new HashMap<String, Object>();
		String sql = getQuerySql(conds,sql_conds);
//		return reportSimpleDao.queryForList(sql.toString(), conds);
		List<Map<String, Object>> reportList = reportSimpleDao.queryForList(sql,sql_conds);
		return convertTxnList(reportList,"FRAUDTYPE",true);
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
		String target = conds.get("target");
		String shape = conds.get("shape");
		String scope = conds.get("scope");

		String startTime = conds.get("startTime");
		String endTime = conds.get("endTime");
		
		List<Map<String, Object>> result = null;
		if(dataList==null || dataList.size()==0){
				result = reportSimpleDao.queryForList(getQuerySql(conds,sql_conds));
		}else{
			result = new ArrayList<Map<String,Object>>();
			result.addAll(dataList);
		}
//		Map<String, Object> info = new HashMap<String, Object>();
//		
//		info.put("xname", "日 期("+startTime+" 至 "+endTime+")");
//		info.put("name", "ALERTDATE");
//		info.put("value", target);
//		info.put("caption", ReportStaticParameter.txnTargetMap.get(target));
		
//		ChartUtil chartUtil  = this.getApplicationContext().getBean(shape, ChartUtil.class);
//		String chartStrs = chartUtil.joinDataStr(result, info);
		
		return convertTxnList(result,"ALERTDATE",false);
	}
	/**
	 * @param conds
	 * @return
	 */
	private String getQuerySql(Map<String, String> conds,Map<String,Object> sql_conds) {
		String txnIds = conds.get("txnIds");
		String countrycode = conds.get("countrycode");
		String regioncode = conds.get("regioncode");
		String citycode = conds.get("citycode");
		String startTime = conds.get("startTime");
		String endTime = conds.get("endTime");
		
		StringBuffer sql = new StringBuffer();
		sql.append("select c.* from (");
		sql.append(tmsSqlMap.getSql("report.fraud.list"));
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
		sql.append(condition).append(" ").append(" group by r.FRAUD_TYPE,r.DISPOSAL ")
		.append(" ORDER BY r.FRAUD_TYPE) s ) c ");
//		.append(tmsSqlMap.getSql("report.date.txn.star.alert"))
//		.append(" ")
//		.append(condition).append(") b) c");
		return sql.toString();
	}
}
