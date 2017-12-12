package cn.com.higinet.tmsreport.web.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.base.dao.Order;
import cn.com.higinet.rapid.base.dao.Page;
import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.rapid.base.dao.SqlMap;
import cn.com.higinet.tms35.manage.common.StaticParameters;
import cn.com.higinet.tms35.manage.common.util.CalendarUtil;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.monitor.model.FusionChartVO;
import cn.com.higinet.tms35.manage.monitor.service.FusionChart;
import cn.com.higinet.tms35.manage.tran.TransCommon;
import cn.com.higinet.tms4.model.RiskModelEngine;
import cn.com.higinet.tms4.model.RiskModelService;
import cn.com.higinet.tms4.model.common.StringUtil;
import cn.com.higinet.tmsreport.web.service.ModelReportService;

@Service("modelReportService")
public class ModelReportServiceImpl extends ApplicationObjectSupport implements ModelReportService {
	
	@Autowired
	private SqlMap tmsSqlMap;
	
	@Autowired
	private SimpleDao reportSimpleDao;

	@Autowired
	private RiskModelEngine riskModelEngine;
	
	
	@Autowired
	private RiskModelService riskModelService;
	
	
	/**
	 * 根据查询条件查询时间段内(默认当月)每天模型评价数据信息 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> listModelReport(Map<String, String> conds) {
		String txnid = MapUtil.getString(conds, "TXNID");
		
		if(txnid.length() > 0) {
			conds.put("TXNID",txnid.substring(txnid.lastIndexOf(",")+1));
		}
		
		String startTime = MapUtil.getString(conds, "startTime").replaceAll("-","");
		String endTime = MapUtil.getString(conds, "endTime").replaceAll("-", "");
		
		
		String sql = "SELECT M.TXNID,M.STARTDATE,MAX(M.F1SCORE) M_FLSCORE_MAX,MIN(M.F1SCORE) M_FLSCORE_MIN,AVG(M.F1SCORE) M_FLSCORE_AVG" 
					+" , 0 R_FLSCORE_MAX,0 R_FLSCORE_MIN,0 R_FLSCORE_AVG FROM TMS_COM_MTRAINHIS M "
					+" WHERE M.TXNID = :TXNID ";
					if(startTime.length() > 0) {
						sql += " AND M.STARTDATE >= '"+startTime+"' ";
					}
					if(endTime.length() > 0) {
						sql += " AND M.STARTDATE <= '"+endTime+"' ";
					}
					sql += " GROUP BY M.TXNID, M.STARTDATE ";
		Page<Map<String,Object>> mTrain_page = reportSimpleDao.pageQuery(sql, conds, new Order().desc("STARTDATE"));
		
		List<Map<String,Object>> modelList = mTrain_page.getList();
		for (Map<String, Object> map : modelList) {
			String tab_name = MapUtil.getString(map, "TXNID");
			String txnName = fullTxnDescPath(tab_name);
			map.put("TAB_DESC", txnName);
		}
		
		List<Map<String,Object>> mTrain_list = mTrain_page.getList();
		buildRunFlScore(mTrain_list);
		
		return mTrain_page;
	}

	private String fullTxnDescPath(String tab_name) {
		String sql1 = "select * from TMS_COM_TAB where tab_name in ("+TransCommon.arr2str(TransCommon.cutToIds(tab_name))+") order by tab_name";
		List<Map<String,Object>> fullPathList = reportSimpleDao.queryForList(sql1);
		String txnName = "";
		for (Map<String, Object> map2 : fullPathList) {
			String parentTxnName = MapUtil.getString(map2, "tab_desc");
			if(txnName.length()==0) {
				txnName = parentTxnName;
			}else {
				txnName += "-" + parentTxnName;
			}
		}
		return txnName;
	}

	private void buildRunFlScore(List<Map<String, Object>> mTrain_list) {
		// 初始化风险模型缓存
		riskModelService.initCacheTmp();
		
		for (Map<String, Object> map : mTrain_list) {
			double r_flscore_max = 0;
			double r_flscore_min = 0;
			double r_flscore_sum = 0;
			
			
			String model_id = MapUtil.getString(map, "TXNID");
			String start_date = MapUtil.getString(map, "STARTDATE");
			String stat_sql = "SELECT (MRNOT_NUMBER - MRNOT_CR_NUMBER) A0B0," 
							+" MRNOT_CR_NUMBER A0B1,"
							+" (MR_AP_NUMBER + MR_APNOT_CRNOT_NUMBER - MR_AP_CR_NUMBER) A1B0,"
							+" (MR_NUMBER - (MR_AP_NUMBER + MR_APNOT_CRNOT_NUMBER - MR_AP_CR_NUMBER)) A1B1"
							+" FROM TMS_MONITOR_TXN_AREA_STAT S WHERE S.MODEL_ID = ? AND S.STATDATE = ?";
			
			List<Map<String,Object>> stat_list = reportSimpleDao.queryForList(stat_sql, model_id,start_date);
			
			if(stat_list == null || stat_list.size() == 0)
				continue;
			
			int c = 1;
			for (Map<String, Object> stat : stat_list) {
				double[][] confusionMatrix = {{MapUtil.getDouble(stat, "A0B0"),MapUtil.getDouble(stat, "A0B1")},{MapUtil.getDouble(stat, "A1B0"),MapUtil.getDouble(stat, "A1B1")}};
				double tmp_flscor = riskModelEngine.weightedFMeasure(confusionMatrix);
				
				if(c == 1) {
					r_flscore_max = tmp_flscor;
					r_flscore_min = tmp_flscor;
				}
				
				if(tmp_flscor > r_flscore_max) 
					r_flscore_max = tmp_flscor;
				
				if(tmp_flscor < r_flscore_min) 
					r_flscore_min = tmp_flscor;
				
				r_flscore_sum += tmp_flscor;
				c++;
			}
			
			map.put("R_FLSCORE_MAX", r_flscore_max);
			map.put("R_FLSCORE_MIN", r_flscore_min);
			map.put("R_FLSCORE_AVG", stat_list.size()>0 ? (r_flscore_sum/stat_list.size()):0);
		}
	}
	
	
	/**
	 * 根据导出条件，查询时间段内交易告警信息，并将信息导出到excel文件
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> exportList(Map<String, String> conds) {
		String txnid = MapUtil.getString(conds, "TXNID");
		
		if(txnid.length() > 0) {
			conds.put("TXNID",txnid.substring(txnid.lastIndexOf(",")+1));
		}
		
		String startTime = MapUtil.getString(conds, "startTime").replaceAll("-","");
		String endTime = MapUtil.getString(conds, "endTime").replaceAll("-","");
		String sql = tmsSqlMap.getSql("report.riskmodel.export");
		sql += " WHERE M.TXNID = :TXNID ";
			if(startTime.length() > 0) {
//				sql += " AND M.STARTDATE >= '"+startTime+"' ";
				sql += " AND M.STARTDATE >= :STARTTIME ";
				conds.put("STARTTIME", startTime);
			}
			if(endTime.length() > 0) {
//				sql += " AND M.STARTDATE <= '"+endTime+"' ";
				sql += " AND M.STARTDATE >= :ENDTIME ";
				conds.put("ENDTIME", endTime);
			}
			sql += " GROUP BY M.TXNID, M.STARTDATE  ORDER BY M.STARTDATE DESC ";
		List<Map<String,Object>> modelList = reportSimpleDao.queryForList(sql, conds);
		
		for (Map<String, Object> map : modelList) {
			String tab_name = MapUtil.getString(map, "TXNID");
			String txnName = fullTxnDescPath(tab_name);
			map.put("TAB_DESC", txnName);
		}
		
		buildRunFlScore(modelList);
		
		return modelList;
	}
	/**
	 * 根据查询条件，获取相应的数据集，并组装成展示图形所需要的字符串
	 * @param conds			查询条件
	 * @param dataList		数据集，如果数据集不为空，则不再重新查询，否则根据查询条件查询
	 * @return
	 */
	public List<Map<String, Object>> getChartData(Map<String, String> conds) {
		String txnid = MapUtil.getString(conds, "TXNID");
		
		if(txnid.length() > 0) {
			conds.put("TXNID",txnid.substring(txnid.lastIndexOf(",")+1));
		}
		
		String startTime = conds.get("startTime").replaceAll("-", "");
		String endTime = conds.get("endTime").replaceAll("-", "");
		
		String sql = tmsSqlMap.getSql("report.riskmodel.list");/*"SELECT M.TXNID,M.STARTDATE,MAX(M.F1SCORE) M_FLSCORE_MAX,MIN(M.F1SCORE) M_FLSCORE_MIN,DEC(AVG(M.F1SCORE),10,2) M_FLSCORE_AVG" 
				+" , 0 R_FLSCORE_MAX,0 R_FLSCORE_MIN,0 R_FLSCORE_AVG FROM TMS_COM_MTRAINHIS M"
				+" WHERE M.TXNID = :TXNID ";*/
				
				if(startTime.length() > 0) {
//					sql += " AND M.STARTDATE >= '"+startTime+"' ";
					sql += " AND M.STARTDATE >= :STARTTIME ";
					conds.put("STARTTIME", startTime);
				}
				if(endTime.length() > 0) {
//					sql += " AND M.STARTDATE <= '"+endTime+"' ";
					sql += " AND M.STARTDATE >= :ENDTIME ";
					conds.put("ENDTIME", endTime);
				}
				
				sql += " GROUP BY M.TXNID, M.STARTDATE  ORDER BY M.STARTDATE ";
		List<Map<String,Object>> result = reportSimpleDao.queryForList(sql, conds);
			
		buildRunFlScore(result);
		
//		FusionChartVO fChartVO = new FusionChartVO();
//		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
//		Map<String,Object> map1 = new HashMap<String,Object>();
//		Map<String,Object> map2 = new HashMap<String,Object>();
//		List<String> tempList = getScope(startTime,endTime);
//		if(result==null || result.size()==0){
//			map1.put(StaticParameters.MONITOR_ALARM_NON, 0);
//			map2.put(StaticParameters.MONITOR_ALARM_NON, 0);
//		}else {
//			for(Map<String,Object> mapData:result){
//				String m_flscore_avg = MapUtil.getString(mapData, "M_FLSCORE_AVG");
//				String r_flscore_avg = MapUtil.getString(mapData, "R_FLSCORE_AVG");
//				map1.put(MapUtil.getString(mapData, "STARTDATE"), "0E-27".equals(m_flscore_avg)?0:StringUtil.parseToDouble(m_flscore_avg));
//				map2.put(MapUtil.getString(mapData, "STARTDATE"), "0E-27".equals(r_flscore_avg)?0:StringUtil.parseToDouble(r_flscore_avg));
//			}
//		}
//		map1.put(StaticParameters.MONITOR_TYPE,"训练精度,008080");
//		map2.put(StaticParameters.MONITOR_TYPE,"运行精度,FF0080");
//		
//		list.add(map1);
//		list.add(map2);
//		fChartVO.setDataList(list);
//		fChartVO.setCaption("模型评价趋势图");
//		
//		Object[] params = {tempList};//传递地图需要的参数信息
//		String chartData = getApplicationContext().getBean("scrollLine2DFusionChart",
//				FusionChart.class).generateChart(fChartVO,params);
		return result;
	}
	/**
	* 方法描述:
	* @return
	*/
	
	private List<String> getScope(String startDate,String endDate) {
		List<String> scope = new ArrayList<String>();
		do {
			scope.add(startDate);
			startDate = CalendarUtil.getBeforeNaturalDay(startDate, -1, CalendarUtil.FORMAT17);
		}while(startDate.compareTo(endDate)<=0);
		
		return scope;
	}


}
