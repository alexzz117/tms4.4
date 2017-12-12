package cn.com.higinet.tms35.manage.monitor.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.monitor.model.FusionChartVO;
import cn.com.higinet.tms35.manage.monitor.service.FusionChart;

/*
 * 风险统计数
 */
@Service("stackedBar2DFusionChart")
public class StackedBar2DFusionChart implements FusionChart{
	
	private static final Log log = LogFactory.getLog(StackedBar2DFusionChart.class);

	
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
		
		List<String> list = (List<String>)params[0];

		StringBuffer sb = new StringBuffer();
		sb.append("<chart imageSave='0' caption='"+caption+"' subcaption='"+subcaption+"' xAxisName='"+xAxisName+"' yAxisName='"+yAxisName+"' yAxisMinValue='0'  " +
				"numberPrefix='' showValues='0' alternateHGridColor='D8E5F6' alternateHGridAlpha='20' divLineColor='B6D0F2' divLineAlpha='50' canvasBorderColor='666666' " +
				"baseFontSize='12' baseFontColor='666666' lineColor='0077AA' lineThickness='2' useRoundEdges='1'" +
				"formatNumber='0' formatNumberScale='0' adjustDiv='1' yAxisMaxValue='3' numDivLines='2'>");
		sb.append("<categories>");
		for(int i=0;i<list.size();i++){
			sb.append("<category label='"+list.get(i)+"' />");
		}
		sb.append("</categories>");
		sb.append("<dataset seriesName='' color='00FFFF' showValues='0'>");
		
		for(int i=0;i<list.size();i++){
			sb.append("<set value='"+MapUtil.getInteger(map, list.get(i))+"' />");
		}
		sb.append("</dataset></chart>");
		if(log.isDebugEnabled())
			log.debug(caption+"XML="+sb.toString());
		return sb.toString();
	}

}
