package cn.com.higinet.tms.manager.modules.monitor.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.common.IPLocationService;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.monitor.model.FusionChartVO;
import cn.com.higinet.tms.manager.modules.monitor.service.FusionChart;

/*
 * 交易运行监控、top5
 */
@Service("column2DFusionChart")
public class Column2DFusionChart implements FusionChart{
	
	@Autowired
	SimpleDao tmsSimpleDao;
	@Autowired
	private IPLocationService ipLocationService;

	private static final Log log = LogFactory.getLog(Column2DFusionChart.class);
	
	// 为了使用地区编码，引入两个map，存放编码和地名的键值对 maxiao 2011-8-29
	private static Map cnmap = new HashMap();//cnmap.put("000000","中国");
	private static Map csmap = new HashMap();//csmap.put("000000","CN");

	@PostConstruct
	void init(){
		String initStrSql = "select REGIONCODE,REGIONNAME,COUNTRYNAME " +
					"from " + ipLocationService.getLocationCurrName("TMS_MGR_REGION") + " region, " + 
					ipLocationService.getLocationCurrName("TMS_MGR_COUNTRY") + " country " +
					"where country.COUNTRYCODE = region.COUNTRYCODE and country.COUNTRYCODE = 'CN'";
		List<Map<String,Object>> initData = tmsSimpleDao.queryForList(initStrSql);
		for(int i=0;i<initData.size();i++){
			cnmap.put(MapUtil.getString(initData.get(i), "REGIONCODE"), MapUtil.getString(initData.get(i), "REGIONNAME"));
			csmap.put(initData.get(i).get("REGIONCODE"),initData.get(i).get("REGIONCODE"));
		}
		if(log.isDebugEnabled())
			log.debug("china map prepare data SQL:"+initStrSql);
	}
	
	
	public String generateChart(FusionChartVO fChartVO, Object... params) {
		// TODO Auto-generated method stub
		List<Map<String,Object>> dataList = fChartVO.getDataList();
		Map<String,Integer> map = fChartVO.getDataMap();
		String caption = fChartVO.getCaption()==null?"":fChartVO.getCaption();
		String subcaption = fChartVO.getSubcaption()==null?"":fChartVO.getSubcaption();
		String xAxisName = fChartVO.getxAxisName()==null?"":fChartVO.getxAxisName();
		String yAxisName = fChartVO.getyAxisName()==null?"":fChartVO.getyAxisName();
		boolean showLables = fChartVO.getShowLables();
		boolean showLinks = fChartVO.getShowLinks();
		
		StringBuffer sb = new StringBuffer();
		sb.append("<chart imageSave='0' caption='"+caption+"' subcaption='"+subcaption+"' xAxisName='"+xAxisName+"' yAxisName='"+yAxisName+"' " +
				"yAxisMinValue='0'  numberPrefix='' showValues='0' alternateHGridColor='D8E5F6' alternateHGridAlpha='20' divLineColor='B6D0F2' " +
				"divLineAlpha='50' canvasBorderColor='666666' baseFontColor='666666' baseFontSize='12' lineColor='0077AA' lineThickness='2' useRoundEdges='1' " +
				"formatNumber='0' formatNumberScale='0' adjustDiv='1' yAxisMaxValue='3' numDivLines='2'>");
//		List<String> list = (List<String>) map.get(StaticParameters.MONITOR_TYPE);
		String key;
		if(map==null || map.size()==0){
			sb.append("<set label='无' value='0' />");//此处不能放中文
		}else{
//			for(int i=0;i<map.size();i++){
//				key=list.get(i);
//				sb.append("<set label='"+key+"' value='"+MapUtil.getInteger(map, key)+"' link='JavaScript:test("+MapUtil.getString(hm_w_p2c, key)+");'/>");
//			}
			Iterator it = map.keySet().iterator();
			while(it.hasNext()){
				key = it.next().toString();
				
				// 原来的map是地区名和值的键值对，后改为地区编码和值的键值对，所以这里要取的地区的名字 maxiao 2011-8-29
				String name = (String)cnmap.get(key);
				// 考虑到原来使用图形的地方没有修改，兼容原来的代码 maxiao 2011-8-30
				if(name == null || "null".equals(name)){
					name = key;
					sb.append("<set label='"+name+"' value='"+MapUtil.getInteger(map, key)+"'/>");
				}
				else {
					sb.append("<set label='"+name+"' value='"+MapUtil.getInteger(map, key)+"' link='JavaScript:test("+key+");'/>");
				}
			}
		}
				
		sb.append("</chart>");
		if(log.isDebugEnabled())
			log.debug(caption+"XML="+sb.toString());
		return sb.toString();
	}

}
