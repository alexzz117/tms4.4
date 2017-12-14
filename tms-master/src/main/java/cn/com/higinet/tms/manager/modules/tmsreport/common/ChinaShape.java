package cn.com.higinet.tms.manager.modules.tmsreport.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.modules.common.util.MapUtil;

@Service("chinaShape")
public class ChinaShape implements ChartUtil {

	public String joinDataStr(List<Map<String, Object>> datalist,
			Map<String, Object> info) {
		

		Map<String,String> regName = (Map<String,String>)info.get("regName");
		Map<String,String> regShort = (Map<String,String>)info.get("regShort");
		String value = info.get("value").toString();
		String unit = "%25";
		if("ALERTNUMBER".equals(value)	|| "TXNNUMBER".equals(value) || "ALERTSUSNUMBER".equals(value)){
			unit = "";
		}else if("AVGTIME".equals(value) ||	"MAXTIME".equals(value)	|| "MINTIME".equals(value)){
			unit = "ms";
		}
		
		Map<String,String> dataMap = new HashMap<String,String>();
		

		float min = new Float("0.00");
		float max = new Float("0.00");
		
		int i=0;
		if(datalist!=null	&&	datalist.size()!=0){
			for(Map<String,Object> data:datalist){
				String val = MapUtil.getString(data, value);
				if (val==null	||	"".equals(val.trim())) {
					val = "0";
				} 
				dataMap.put(MapUtil.getString(data, "REGIONCODE"), val);
				if(i==0){
					min = Float.parseFloat(val);
					max = Float.parseFloat(val);
					i++;
					continue;
				}
				float v= Float.parseFloat(val);
				if(v<min){
					min = v;
				}
				if(v>max){
					max = v;
				}
				i++;
			}
		}
		
		//每一段之间的差值
		float cz = (max-min)/3;
		
		

		StringBuffer  chinaStr = new StringBuffer();
		chinaStr.append(" <map showCanvasBorder='1' showLabels='1' canvasBorderThickness='2' borderColor='00324A' ");
		chinaStr.append(" fillColor='FCFCFC' hoverColor='3333FF' legendPosition='Bottom' bgColor='FFCC00, FFFFFF' ");
		chinaStr.append(" bgAlpha='30,30' basefontsize='12'>");

		chinaStr.append("<colorRange>");
		chinaStr.append("<color minValue='0' maxValue='0' displayValue='无报警数的省份' color='ffffff' />");
		chinaStr.append("<color minValue='$min1' maxValue='$max1' displayValue='报警数较少的省份' color='6ae84d' />");
		chinaStr.append("<color minValue='$min2' maxValue='$max2' displayValue='报警数中等的省份' color='fafc48' />");
		chinaStr.append("<color minValue='$min3' maxValue='$max3' displayValue='报警数较多的省份' color='FF0000' />");
		chinaStr.append("</colorRange>");
		
		
		chinaStr.append("<data>");
		
		Iterator it = regName.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, String> e = (Entry)it.next();
			String k = e.getKey();
			String n = e.getValue();
			String regshort = regShort.get(k);
			String v = dataMap.get(k)==null?"0":dataMap.get(k);
			
			chinaStr.append("<entity id='"+regshort+"' value='"+v+"' displayValue='"+n);
			if(v.equals("0")){
				chinaStr.append("' toolText='"+n+"："+(v+unit)+"' />");
			}else{
				chinaStr.append("' toolText='"+n+"："+(v+unit)+"' link='JavaScript:showCity(%26apos;"+k+"%26apos;);'/>");
			}
			
		}

		chinaStr.append("</data>");

		chinaStr.append("<styles>");
		chinaStr.append("<definition>");
		chinaStr.append("<style type='animation' name='animX' param='_xscale' start='0' duration='1' />");
		chinaStr.append("<style type='animation' name='animY' param='_yscale' start='0' duration='1' />");
		chinaStr.append("</definition>");
		chinaStr.append("<application>");
		chinaStr.append("<apply styles='animX,animY' />");
		chinaStr.append("</application>");
		chinaStr.append("</styles>");
		chinaStr.append("</map>");

		String strs = chinaStr.toString();
		if(cz==0.00){
			cz=Float.parseFloat("0.50");
		}
		strs = strs.replace("$min1", ""+0.0000000000001);
		strs = strs.replace("$max1", ""+(min+cz));
		strs = strs.replace("$min2", ""+(min+cz));
		strs = strs.replace("$max2", ""+(min+cz+cz));
		strs = strs.replace("$min3", ""+(min+cz+cz));
		strs = strs.replace("$max3", ""+(max+cz));
		return strs;
	}

}
