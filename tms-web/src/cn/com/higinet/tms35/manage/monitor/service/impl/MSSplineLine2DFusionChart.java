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
 * 宏观统计 省份告警统计表
 */
@Service("mSSplineLine2DFusionChart")
public class MSSplineLine2DFusionChart implements FusionChart{

	private static final Log log = LogFactory.getLog(MSSplineLine2DFusionChart.class);
	
	
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
		
		List list = (List)params[0];
		sb.append("<chart imageSave='0' caption='"+caption+"' subcaption='"+subcaption+"' xAxisName='"+xAxisName+"' yAxisName='"+yAxisName+"' yAxisMinValue='0'  numberPrefix='' showValues='0' alternateHGridColor='D8E5F6' alternateHGridAlpha='20' divLineColor='B6D0F2' divLineAlpha='50' canvasBorderColor='666666' baseFontSize='12' baseFontColor='666666' lineColor='0077AA' lineThickness='2' useRoundEdges='1'>");
		sb.append("<categories>");
		for(int i=0;i<list.size();i++){
			sb.append("<category label='"+String.valueOf(list.get(i))+"' />");
		}
		sb.append("</categories>");
		
//		List<Map<String,Object>> dataList = (List<Map<String,Object>>)dataMap.get("dataset");
		if(dataList==null || dataList.size()==0){
			sb.append("<dataset seriesName='无' color='008080' anchorBorderColor='008080'>");
			sb.append("<set value='0' />");
			sb.append("</dataset>");
		}else{
			for(int i=0;i<dataList.size();i++){
				Map<String,Object> mapData = dataList.get(i);
				String key = (String)mapData.get(StaticParameters.MONITOR_TYPE);
				String[] keys = key.split(",");
				sb.append("<dataset seriesName='"+keys[0]+"' color='"+keys[1]+"' anchorBorderColor='"+keys[1]+"' anchorRadius='4'>");
				for(int index = 0;index <list.size();index++){
					sb.append("<set value='"+MapUtil.getInteger(mapData, list.get(index).toString())+"' />");
				}
				sb.append("</dataset>");
			}
		}
		sb.append("<styles>").append("<definition>")
			.append("<style name='MyXScaleAnim' type='ANIMATION' duration='0.7' start='0' param='_xScale' />")
			.append("<style name='MyYScaleAnim' type='ANIMATION' duration='0.7' start='0' param='_yscale' />")
			.append("<style name='MyAlphaAnim' type='ANIMATION' duration='0.7' start='0' param='_alpha' />")
			.append("</definition>").append("<application>")
			.append("<apply toObject='DIVLINES' styles='MyXScaleAnim,MyAlphaAnim' />")
			.append("<apply toObject='HGRID' styles='MyYScaleAnim,MyAlphaAnim' />")
			.append("</application>").append("</styles>");
		sb.append("</chart>");
		if(log.isDebugEnabled())
			log.debug(caption+"XML="+sb.toString());
		return sb.toString();
	}
}
