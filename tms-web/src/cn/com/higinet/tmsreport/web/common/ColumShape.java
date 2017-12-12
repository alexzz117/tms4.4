package cn.com.higinet.tmsreport.web.common;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import cn.com.higinet.tms35.manage.common.util.MapUtil;

@Service("ColumShape")
public class ColumShape implements ChartUtil {

	public String joinDataStr(List<Map<String, Object>> datalist, Map<String,Object> info) {

		StringBuffer chartStrs  = new StringBuffer();
		String caption = MapUtil.getString(info, "caption");
		String xname= MapUtil.getString(info, "xname");
		String value = MapUtil.getString(info, "value");
		String unit = "%25";
		if("ALERTNUMBER".equals(value)	||	"TXNNUMBER".equals(value)	||	"ALERTSUSNUMBER".equals(value)	||	"TRIGGERNUM".equals(value)	||	"HITNUM".equals(value)){
			unit = "";
		}else if("AVGTIME".equals(value)	||	"MAXTIME".equals(value)	||	"MINTIME".equals(value)){
			unit = "ms";
		}
		
//		chartStrs.append("<graph caption='"+caption+"' xAxisName='"+xname+"' yAxisName='' decimalPrecision='2' formatNumberScale='0'>");
		chartStrs.append("<chart imageSave='0' caption='"+caption+"' subcaption='' xAxisName='"+xname+"' yAxisName='' " +
				"yAxisMinValue='0'  numberPrefix='' numberSuffix='"+unit+"' showValues='0' alternateHGridColor='D8E5F6' alternateHGridAlpha='20' divLineColor='B6D0F2' " +
				"divLineAlpha='50' canvasBorderColor='666666' baseFontColor='666666' baseFontSize='12' lineColor='0077AA' lineThickness='2' useRoundEdges='1' " +
				"formatNumber='0' formatNumberScale='0' adjustDiv='1' yAxisMaxValue='3' numDivLines='2'>");
		
		if(datalist!=null && datalist.size()>0){
			String name = MapUtil.getString(info, "name");
			
			
			for(int i=0;i<datalist.size();i++){
				Map<String,Object> map = datalist.get(i);
				if(map!=null && map.size()>0){
					chartStrs.append("<set label='"+map.get(name)+"' value='"+map.get(value)+"'  />");
//					chartStrs.append("<set name='"+map.get(name)+"' value='"+map.get(value)+"' color='"+ReportStaticParameter.SHAPECOLOR[i]+"' />");
				}
			}
		}else{
			chartStrs.append("<set label='无' value='0' />");//此处不能放中文
//			chartStrs.append("<set name='无' value='0'  />");
		}
		chartStrs.append("</chart>");
//		chartStrs.append("</graph>");

		return chartStrs.toString();
	}

}
