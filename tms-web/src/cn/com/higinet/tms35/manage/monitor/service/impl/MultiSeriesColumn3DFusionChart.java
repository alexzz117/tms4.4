package cn.com.higinet.tms35.manage.monitor.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.monitor.model.FusionChartVO;
import cn.com.higinet.tms35.manage.monitor.service.FusionChart;

@Service("multiSeriesColumn3DFusionChart")
public class MultiSeriesColumn3DFusionChart implements FusionChart{

	private static final Log log = LogFactory.getLog(MultiSeriesColumn3DFusionChart.class);
	
	
	public String generateChart(FusionChartVO fChartVO, Object... params) {
		// TODO Auto-generated method stub
		List<Map<String,Object>> dataList = fChartVO.getDataList();
		Map<String,Integer> dMap = fChartVO.getDataMap();
		String caption = fChartVO.getCaption()==null?"":fChartVO.getCaption();
		String subcaption = fChartVO.getSubcaption()==null?"":fChartVO.getSubcaption();
		String xAxisName = fChartVO.getxAxisName()==null?"":fChartVO.getxAxisName();
		String yAxisName = fChartVO.getyAxisName()==null?"":fChartVO.getyAxisName();
		boolean showLables = fChartVO.getShowLables();
		boolean showLinks = fChartVO.getShowLinks();
		
		StringBuffer sb = new StringBuffer();
		
		List<String> list = (List<String>)params[0];
		sb.append("<chart imageSave='0' caption='"+caption+"' subcaption='"+subcaption+"' xAxisName='"+xAxisName+"' yAxisName='"+yAxisName+"' yAxisMinValue='0'  numberPrefix='' showSum='0' alternateHGridColor='D8E5F6' alternateHGridAlpha='20' divLineColor='B6D0F2' divLineAlpha='50' canvasBorderColor='666666' baseFontSize='12' baseFontColor='666666' lineColor='0077AA' lineThickness='2' useRoundEdges='1'>");
		sb.append("<categories>");
		if(list==null || list.size()==0){
			sb.append("<category label='无' />");
		}else{
			for(int i=0;i<list.size();i++){
				sb.append("<category label='"+String.valueOf(list.get(i))+"' />");
			}
		}
		sb.append("</categories>");
		
		if(dataList==null || dataList.size()==0){
			sb.append("<dataset seriesName='无' color='' showValues='0'>");
			sb.append("<set value='0' />");
			sb.append("</dataset>");
		}
		for(int i=0;i<dataList.size();i++){
			Map<String,Object> map = dataList.get(i);
			String key = (String)map.get("NAME");
//			String[] keys = key.split(",");
//			sb.append("<dataset><dataSet seriesName='"+keys[0]+"' color='"+keys[1]+"' showValues='0'>");
			sb.append("<dataset seriesName='"+key+"' showValues='0'>");
			for(int index=0;index<list.size();index++){
//				sb.append("<set value='"+map.get((String)list.get(index))+"' />");
				sb.append("<set value='"+MapUtil.getInteger(map, list.get(index).toString())+"' />");
			}
			sb.append("</dataset>");
		}
		
//		if(dataMap.get(StaticParameters.MONITOR_TARGET)!=null){
////			sb.append((String)(dataMap.get(StaticParameters.MONITOR_TARGET)));
//			sb.append(MapUtil.getString(dataMap, StaticParameters.MONITOR_TARGET));
//		}
		sb.append("</chart>");
		if(log.isDebugEnabled())
			log.debug(caption+"XML="+sb.toString());
		return sb.toString();
	}

}
