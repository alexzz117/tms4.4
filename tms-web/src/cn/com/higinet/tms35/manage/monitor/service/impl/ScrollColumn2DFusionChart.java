package cn.com.higinet.tms35.manage.monitor.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms35.manage.common.StaticParameters;
import cn.com.higinet.tms35.manage.monitor.model.FusionChartVO;
import cn.com.higinet.tms35.manage.monitor.service.FusionChart;

/*
 * 规则运行监控
 */
@Service("scrollColumn2DFusionChart")
public class ScrollColumn2DFusionChart implements FusionChart{
	
	private static final Log log = LogFactory.getLog(ScrollColumn2DFusionChart.class);

	
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
		String[] colors = {"AFD8F8","F6BD0F","8BBA00","A66EDD","F984A1","800000","CCCC99","CC9966","7B285B","A83D7F"};
		// 将原来随机取值改为顺序取值 或不用赋值，系统会自动赋值 maxiao 2011-8-30
		//Random randX = new Random(); 
		if(map==null || map.size()==0){
			sb.append("<dataset seriesName='"+StaticParameters.MONITOR_ALARM_NON+"' color='' showValues='0'>");
			sb.append("<set value='0' />");
			sb.append("</dataset>");
		} 
		else {
			Iterator it = map.entrySet().iterator();
			int i=0;
			while(it.hasNext()){
				if(i==9)i=0;
				Map.Entry entry =(Map.Entry)it.next();
				//sb.append("<dataset seriesName='"+entry.getKey()+"' color='"+colors[randX .nextInt(9)]+"' showValues='0'>");
				//sb.append("<dataset seriesName='"+entry.getKey()+"' color='"+colors[i]+"' showValues='0'>");
				sb.append("<dataset seriesName='"+entry.getKey()+"' color='' showValues='0'>");
				sb.append("<set value='"+entry.getValue()+"' />");
				sb.append("</dataset>");
				i++;
			}
		}
		
		sb.append("</chart>");
		if(log.isDebugEnabled())
			log.debug(caption+"XML="+sb.toString());
		return sb.toString();
	}

}
