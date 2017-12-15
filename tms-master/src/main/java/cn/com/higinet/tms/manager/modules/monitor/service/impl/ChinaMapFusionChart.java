package cn.com.higinet.tms.manager.modules.monitor.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.common.IPLocationService;
import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.monitor.model.FusionChartVO;
import cn.com.higinet.tms.manager.modules.monitor.service.FusionChart;

/**
 * @author zhang.lei
 */
@Service("chinaMapFusionChart")
public class ChinaMapFusionChart implements FusionChart{

	@Autowired
	@Qualifier("tmsSimpleDao")
	SimpleDao tmsSimpleDao;
	@Autowired
	private IPLocationService ipLocationService;
	
	private static final Log log = LogFactory.getLog(ChinaMapFusionChart.class);
	
	private static Map cnmap = new HashMap();//cnmap.put("000000","中国");
	private static Map csmap = new HashMap();//csmap.put("000000","CN");

	@PostConstruct
	void init(){
		String initStrSql = "select REGIONCODE,REGIONNAME,COUNTRYNAME " +
					"from " + ipLocationService.getLocationCurrName("TMS_MGR_REGION") + " region, " +
					ipLocationService.getLocationCurrName("TMS_MGR_COUNTRY") + " country " +
					"where country.COUNTRYCODE = region.COUNTRYCODE and country.COUNTRYCODE = 'CN'";
		List<Map<String,Object>> initData = tmsSimpleDao.queryForList(initStrSql);
		for(int i=0;i<initData.size();i++){
			cnmap.put(MapUtil.getString(initData.get(i), "REGIONCODE"), MapUtil.getString(initData.get(i), "REGIONNAME"));
			csmap.put(initData.get(i).get("REGIONCODE"),initData.get(i).get("REGIONCODE"));
		}
		if(log.isDebugEnabled())
			log.debug("china map prepare data SQL:"+initStrSql);
	}
	
	/*
	 * 自定义参数说明：
	 * 0  minDisValue String 最小值展示信息
	 * 1 midDisValue String 中间值展示信息
	 * 2 maxDisValue String 最大值展示信息
	 * 3 noDisValue String 无数据展示信息
	 * 
	 */
	
	public String generateChart(FusionChartVO fChartVO,Object... params) {
		// TODO Auto-generated method stub
		List<Map<String,Object>> dataList = fChartVO.getDataList();
		Map<String,Integer> dataMap = fChartVO.getDataMap();
		String caption = fChartVO.getCaption()==null?"":fChartVO.getCaption();
		String subcaption = fChartVO.getSubcaption()==null?"":fChartVO.getSubcaption();
		String xAxisName = fChartVO.getxAxisName()==null?"":fChartVO.getxAxisName();
		String yAxisName = fChartVO.getyAxisName()==null?"":fChartVO.getyAxisName();
		boolean showLables = fChartVO.getShowLables();
		boolean showLinks = fChartVO.getShowLinks();
		
		String minDisValue = (String)params[0];
		String midDisValue = (String)params[1];
		String maxDisValue = (String)params[2];
		String noDisValue = (String)params[3];
		StringBuffer sb = new StringBuffer();
		
		float minValue = 0;
		float midValue = 0;
		float maxValue = 0;
		if(dataList.size()>20){
			minValue = MapUtil.getInteger(dataList.get(dataList.size()-21), StaticParameters.TMS_REPORT_CHINAMAP_MAPVALUE)+1;
			midValue = MapUtil.getInteger(dataList.get(dataList.size()-11), StaticParameters.TMS_REPORT_CHINAMAP_MAPVALUE)+1;
			maxValue = MapUtil.getInteger(dataList.get(dataList.size()-1), StaticParameters.TMS_REPORT_CHINAMAP_MAPVALUE)+1;
		}else if(dataList.size()>10){
			midValue = MapUtil.getInteger(dataList.get(dataList.size()-11), StaticParameters.TMS_REPORT_CHINAMAP_MAPVALUE)+1;
			maxValue = MapUtil.getInteger(dataList.get(dataList.size()-1), StaticParameters.TMS_REPORT_CHINAMAP_MAPVALUE)+1;
		}else if(dataList.size()>0){
			maxValue = MapUtil.getInteger(dataList.get(dataList.size()-1), StaticParameters.TMS_REPORT_CHINAMAP_MAPVALUE)+1;
		}
		
		sb.append("<map showCanvasBorder='1' showLabels='1' canvasBorderThickness='2' borderColor='00324A' fillColor='FCFCFC' hoverColor='3333FF' legendPosition='Bottom' bgColor='FFCC00, FFFFFF' bgAlpha='30,30' >");
		sb.append("<colorRange>");
		sb.append("<color minValue='0' maxValue='0' displayValue='").append(noDisValue).append("' color='ffffff' />");
		sb.append("<color minValue='1' maxValue='").append(minValue).append("' displayValue='").append(minDisValue).append("' color='6ae84d' />");
		sb.append("<color minValue='").append(minValue).append("' maxValue='").append(midValue).append("' displayValue='").append(midDisValue).append("' color='fafc48' />");
		sb.append("<color minValue='").append(midValue).append("' maxValue='").append(maxValue).append("' displayValue='").append(maxDisValue).append("' color='fa5f21' />");
		sb.append("</colorRange>");
		sb.append("<data>");
		String c,n,v,value;
		
		Iterator it=cnmap.keySet().iterator();
		while(it.hasNext()){
			v=(String)it.next();//000000
			c = MapUtil.getString(csmap, v);//"CN"
			n = MapUtil.getString(cnmap, v);//"北京"
			value=MapUtil.getString(dataMap,v);
			if(showLinks ){	
				sb.append("<entity id='"+c+"' value='"+value+"' displayValue='"+n+"' toolText='"+n+":"+value+"' link='JavaScript:test("+v+");'/>");
			}else{//地图上展现是0的话，是不是应该也没有链接?
				sb.append("<entity id='"+c+"' value='"+value+"' displayValue='"+n+"' toolText='"+n+":"+value+"'/>");
			}
		}
		sb.append("</data>");
		sb.append("<styles>");
		sb.append("<definition>");
		sb.append("<style type='animation' name='animX' param='_xscale' start='0' duration='1' />");
		sb.append("<style type='animation' name='animY' param='_yscale' start='0' duration='1' />");
		sb.append("</definition>");
		sb.append("<application>");
		sb.append("<apply styles='animX,animY' />");
		sb.append("</application>");
		sb.append("</styles>");
		sb.append("</map>");
		if(log.isDebugEnabled())
			log.debug("china map XML:"+sb.toString());
		return sb.toString();
	}
	
	public static void main(String[] args){
		FusionChart functionChart = new ChinaMapFusionChart();
		Map<String,Object> map = new HashMap<String,Object>();
		Object[] params = {"","","","",false,false,map
				,"100","告警数最少的省份","1000","告警数较多的省份","10000","告警数最多的省份","无告警数的省份"};
		
//		String sb = functionChart.generateChart(params);
//		System.out.println(sb);
		
	}
}
