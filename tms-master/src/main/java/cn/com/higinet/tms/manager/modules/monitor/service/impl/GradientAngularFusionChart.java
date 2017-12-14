package cn.com.higinet.tms.manager.modules.monitor.service.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.monitor.model.FusionChartVO;
import cn.com.higinet.tms.manager.modules.monitor.service.FusionChart;

/*
 * 仪表盘所有数值的key都为：StaticParameters.MONITOR_DIAL
 * 命中率
 */
@Service("gradientAngularFusionChart")
public class GradientAngularFusionChart implements FusionChart{

	private static final Log log = LogFactory.getLog(GradientAngularFusionChart.class);
	
	
	public String generateChart(FusionChartVO fChartVO, Object... params) {
		// TODO Auto-generated method stub
		String caption = fChartVO.getCaption()==null?"":fChartVO.getCaption();
		Map<String,Integer> dataMap = fChartVO.getDataMap();
		
		StringBuffer sb = new StringBuffer();
		sb.append("<chart bgAlpha='0' bgColor='FFFFFF' lowerLimit='0' upperLimit='100' numberSuffix='%25' showBorder='0' basefontColor='FFFFDD' chartTopMargin='25' chartBottomMargin='25'");
		sb.append("chartLeftMargin='25' chartRightMargin='25' toolTipBgColor='80A905' gaugeFillMix='{dark-10},FFFFFF,{dark-10}' gaugeFillRatio='3' baseFontSize='12'>");
		sb.append("<colorRange>");
		sb.append("<color minValue='0' maxValue='45' code='8BBA00'/>");
		sb.append("<color minValue='45' maxValue='80' code='F6BD0F'/>");
		sb.append("<color minValue='80' maxValue='100' code='FF654F'/>");
		sb.append("</colorRange>");
		sb.append("<dials>");
		
//		sb.append("<dial value='"+(dataMap.get("dial")==null?0:dataMap.get("dial"))+"' rearExtension='10'/>");
		//sb.append("<dial value='"+MapUtil.getString(dataMap, StaticParameters.MONITOR_DIAL)+"' rearExtension='10'/>");
		// 由于只能传整形，导致比率只能取整 为了保证可以四舍五入，需要转成float型求商 maxiao 2011-9-2
		double dial = 0;
		if(MapUtil.getInteger(dataMap, StaticParameters.MONITOR_DENOMINATOR)!=0){
			dial = MapUtil.getInteger(dataMap, StaticParameters.MONITOR_NUMERATOR)*100.00/MapUtil.getInteger(dataMap, StaticParameters.MONITOR_DENOMINATOR);
		}
		sb.append("<dial value='"+dial+"' rearExtension='10'/>");
		
		sb.append("</dials>");
		sb.append("<trendpoints>");
		
//		sb.append("<point value='50' displayValue='"+(dataMap.get(StaticParameters.MONITOR_TYPE)==null?0:dataMap.get(StaticParameters.MONITOR_TYPE))+"' fontcolor='FF4400' useMarker='1' dashed='1' dashLen='2' dashGap='2' valueInside='1' />");
		sb.append("<point value='50' displayValue='"+caption+"' fontcolor='FF4400' useMarker='1' dashed='1' dashLen='2' dashGap='2' valueInside='1' />");
		
		sb.append("</trendpoints>");
		sb.append("<annotations>");
		sb.append("<annotationGroup id='Grp1' showBelow='1' >");
		sb.append("<annotation type='rectangle' x='5' y='5' toX='345' toY='195' radius='10' color='009999,333333' showBorder='0' />");
		sb.append("</annotationGroup>");
		sb.append("</annotations>");
		sb.append("<styles>");
		sb.append("<definition>");
		sb.append("<style name='RectShadow' type='shadow' strength='3'/>");
		sb.append("</definition>");
		sb.append("<application>");
		sb.append("<apply toObject='Grp1' styles='RectShadow' />");
		sb.append("</application>");
		sb.append("</styles>");
		sb.append("</chart>");
		if(log.isDebugEnabled())
			log.debug(caption+"XML="+sb.toString());
		return sb.toString();
	}

}
