package cn.com.higinet.tmsreport.web.common;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import cn.com.higinet.tms35.manage.common.util.MapUtil;

@Service("PieShape")
public class PieShape implements ChartUtil {

	public String joinDataStr(List<Map<String, Object>> datalist, Map<String, Object> info) {
		
		StringBuffer chartStrs  = new StringBuffer();
		String caption = MapUtil.getString(info, "caption");
		String xname= MapUtil.getString(info, "xname");
		chartStrs.append("<graph showNames='2' skipOverlapLabels='0' decimalPrecision='2' basefontsize='12' formatNumberScale='0'> ");
		
		if(datalist!=null && datalist.size()>0){
			String name = MapUtil.getString(info, "name");
			String value = MapUtil.getString(info, "value");
			
			boolean flag = false;
			for(int i=0;i<datalist.size();i++){
				Map<String,Object> map = datalist.get(i);
				
				if(map!=null && map.size()>0	&&	map.get(value)!=null	&&	!"0".equals(MapUtil.getString(map, value))){							//color='"+ReportStaticParameter.SHAPECOLOR[i]+"'
					chartStrs.append("<set name='"+map.get(name)+"' value='"+map.get(value)+"'  />");
					flag = true;
				}
			}
			
			if (!flag) {
				chartStrs.append("<set name='无' value='0'  />");
			}
		}else{
			chartStrs.append("<set name='无' value='0'  />");
		}
		chartStrs.append("</graph>");
		
		return chartStrs.toString();
	}

}
