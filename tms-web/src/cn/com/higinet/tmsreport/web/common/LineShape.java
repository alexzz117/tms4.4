package cn.com.higinet.tmsreport.web.common;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import cn.com.higinet.tms35.manage.common.util.MapUtil;

@Service("lineShape")
public class LineShape implements ChartUtil {

	public String joinDataStr(List<Map<String, Object>> datalist, Map<String, Object> info) {
		
		StringBuffer chartStrs  = new StringBuffer();
		
		String caption = MapUtil.getString(info, "caption");
		String xname= MapUtil.getString(info, "xname");
		chartStrs.append("<graph caption='"+caption+"' subcaption='' xAxisName='"+xname+"' yAxisMinValue='$min' ");
		chartStrs.append(" yAxisName='' decimalPrecision='0' formatNumberScale='0' numberPrefix='' showNames='1'");
		chartStrs.append("showValues='0'  showAlternateHGridColor='1' AlternateHGridColor='ff5904' divLineColor='ff5904' ");
		chartStrs.append(" divLineAlpha='20' alternateHGridAlpha='5' baseFontSize='12' chartRightMargin='50' chartLeftMargin='30' rotateNames='1'>");
		

		float min = new Float("0.00");
		if(datalist!=null && datalist.size()>0){
			String name = MapUtil.getString(info, "name");
			String value = MapUtil.getString(info, "value");
			
			for(int i=0;i<datalist.size();i++){
				Map<String,Object> map = datalist.get(i);
				if(map!=null && map.size()>0){	
					String val = MapUtil.getString(map, value);
					chartStrs.append("<set name='"+MapUtil.getString(map, name)+"' value='"+val+"' hoverText='"+MapUtil.getString(map, name)+"' />");
					if (val!=null	&&	!"".equals(val.trim())) {
						if(i==0){
							min = Float.parseFloat(val);
						}else if(Float.parseFloat(val)<min){
							min = Float.parseFloat(val);
						}
					}else{
						min = 0;
					}

				}
			}
			min = min*4/5;
		}else{
			chartStrs.append("<set name='' value='0' hoverText='无' />");
		}
		chartStrs.append("</graph>");
		String strs = chartStrs.toString().replace("$min", ""+min);
		return strs;
	}

}
