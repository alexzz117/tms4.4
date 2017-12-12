package cn.com.higinet.tms35.manage.monitor.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms35.manage.common.StaticParameters;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.monitor.model.FusionChartVO;
import cn.com.higinet.tms35.manage.monitor.service.FusionChart;

/*
 * 交易运行监控--执行时间
 */
@Service("scrollColumn2DFusionChartX")
public class ScrollColumn2DFusionChartX implements FusionChart{
	
	private static final Log log = LogFactory.getLog(ScrollColumn2DFusionChartX.class);

	
	public String generateChart(FusionChartVO fChartVO, Object... params) {
		// TODO Auto-generated method stub
		
		List<Map<String,Object>> dataList = fChartVO.getDataList();
		String caption = fChartVO.getCaption()==null?"":fChartVO.getCaption();
		String subcaption = fChartVO.getSubcaption()==null?"":fChartVO.getSubcaption();
		String xAxisName = fChartVO.getxAxisName()==null?"":fChartVO.getxAxisName();
		String yAxisName = fChartVO.getyAxisName()==null?"":fChartVO.getyAxisName();
		boolean showLables = fChartVO.getShowLables();
		boolean showLinks = fChartVO.getShowLinks();
		
		StringBuffer sb = new StringBuffer();
		
		List<String> list = (List<String>)params[0];
		sb.append("<chart imageSave='0' caption='"+caption+"' subcaption='"+subcaption+"' xAxisName='"+xAxisName+"' " +
				"yAxisName='"+yAxisName+"' yAxisMinValue='0'  numberPrefix='' showValues='0' alternateHGridColor='D8E5F6' " +
						"alternateHGridAlpha='20' divLineColor='B6D0F2' divLineAlpha='50' canvasBorderColor='666666' " +
						"baseFontSize='12' baseFontColor='666666' lineColor='0077AA' lineThickness='2' useRoundEdges='1' " +
						"formatNumber='0' formatNumberScale='0' adjustDiv='1' yAxisMaxValue='3' numDivLines='2'>");
		sb.append("<categories>");
		if(list==null || list.size()==0){
			sb.append("<category label='无' />");
		}else {
			for(int i=0;i<list.size();i++){
				sb.append("<category label='"+String.valueOf(list.get(i))+"' />");
			}
		}
		sb.append("</categories>");
		// 这里没有随机取值，颜色是由key决定的 maxiao 2011-8-31
		//String[] colors = {"AFD8F8","F6BD0F","8BBA00","A66EDD","F984A1","800000","CCCC99","CC9966","7B285B","A83D7F"};
		//Random randX = new Random(); 
		for(Map<String,Object> mapData:dataList){
			String keys = (String)mapData.get(StaticParameters.MONITOR_TYPE);
			String key = keys.split(",")[0];
			String color = keys.split(",")[1];
			//sb.append("<dataset><dataSet seriesName='"+key+"' color='"+color+"' showValues='0'>");
			sb.append("<dataSet seriesName='"+key+"' color='"+color+"' showValues='0'>");
			for(int i=0;i<list.size();i++){
				sb.append("<set value='"+MapUtil.getInteger(mapData, list.get(i).toString())+"' />");
			}
			//sb.append("</dataSet></dataset>");
			sb.append("</dataSet>");
		}
		
//		if(dataMap.get(StaticParameters.MONITOR_TARGET)!=null){
//			sb.append((String)(dataMap.get(StaticParameters.MONITOR_TARGET)));
//			sb.append(MapUtil.getString(dataMap, StaticParameters.MONITOR_TARGET));
//		}
		sb.append("</chart>");
		if(log.isDebugEnabled())
			log.debug(caption+"XML="+sb.toString());
		return sb.toString();
	}

}
