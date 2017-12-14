package cn.com.higinet.tms.manager.modules.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;

public class FusionChartUtil {

	private static final Log log = LogFactory.getLog(FusionChartUtil.class);

/**
   * 中国地图
   * @param map 存放曲线图的key,value，key的值为"CN.BJ"这样的格式
   * @param conditionStr 原始查询条件
   * @return xml的字符串
   */
public static String buildMap_china(Map<String,Object> dataMap){
	StringBuffer sb = new StringBuffer();
	//showCanvasBorder：是否显示边框1是0否；legendPosition:标注放置在哪个位置；canvasBorderThickness：边框的厚度;showLabels='0':是否显示省份名称0否1是
	//sb.append("<map showCanvasBorder='1' showLabels='"+(showLables?"1":"0")+"' imageSaveURL='"+Config.servletPath+"?actionType=saveChartImage' canvasBorderThickness='2' borderColor='00324A' fillColor='FCFCFC' hoverColor='3333FF' legendPosition='Bottom' bgColor='FFCC00, FFFFFF' bgAlpha='30,30' >");
	sb.append("<map showCanvasBorder='1' showLabels='1' canvasBorderThickness='2' borderColor='00324A' fillColor='FCFCFC' hoverColor='3333FF' legendPosition='Bottom' bgColor='FFCC00, FFFFFF' bgAlpha='30,30' >");
	sb.append("<colorRange>");
	sb.append("<color minValue='0' maxValue='0' displayValue='无报警数的省份' color='ffffff' />");
	sb.append("<color minValue='1' maxValue='"+dataMap.get("minValue")+"' displayValue='报警数最少的省份' color='6ae84d' />");
	sb.append("<color minValue='"+dataMap.get("minValue")+"' maxValue='"+dataMap.get("midValue")+"' displayValue='报警数较多的省份' color='fafc48' />");
	sb.append("<color minValue='"+dataMap.get("midValue")+"' maxValue='"+dataMap.get("maxValue")+"' displayValue='报警数最多的省份' color='fa5f21' />");
	sb.append("</colorRange>");
	sb.append("<data>");
	String c,n,v;
	Float value;
	Map<String,Object> cvmap = (Map<String,Object>)dataMap.get("CVMAP");
	Map<String,Object> cnmap = (Map<String,Object>)dataMap.get("CNMAP");
	Map<String,Object> valuemap = (Map<String,Object>)dataMap.get("VALUE");

	Iterator it=cnmap.keySet().iterator();
	while(it.hasNext()){
		c=(String)it.next();//CN.BJ
		v=(String)cvmap.get(c);
		n=(String)cnmap.get(c);//北京
		value=valuemap.get(v)==null?0:Float.parseFloat(valuemap.get(v).toString());
		if(v.equals("0")){
			sb.append("<entity id='"+c+"' value='"+value+"' displayValue='"+n+"' toolText='"+n+":"+value+"'/>");
		}else{
			sb.append("<entity id='"+c+"' value='"+value+"' displayValue='"+n+"' toolText='"+n+":"+value+"' link='JavaScript:test("+v+");'/>");
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
		log.debug("中国地图XML="+sb.toString());
	return sb.toString();
}

/**
   * 曲线图
   * @param caption 标题
   * @param xAxisName 横向坐标说明
   * @param yAxisName 纵向坐标说明
   * @param map 存放曲线图的key,value
   * @return xml的字符串
   */
	public static String buildLine(String caption,String subcaption,String xAxisName,String yAxisName,Map map){
		StringBuffer sb = new StringBuffer();
		sb.append("<chart imageSave='0' caption='"+caption+"' subcaption='"+subcaption+"' xAxisName='"+xAxisName+"' yAxisName='"+yAxisName+"' formatNumberScale='0' yAxisMinValue='0'  numberPrefix='' showValues='0' alternateHGridColor='D8E5F6' alternateHGridAlpha='20' divLineColor='B6D0F2' divLineAlpha='50' canvasBorderColor='666666' baseFontSize='12' baseFontColor='666666' lineColor='0077AA' lineThickness='2' useRoundEdges='1'>");
		Iterator it=map.keySet().iterator();
//		List<String> list = (List<String>) map.get(StaticParameters.MONITOR_TYPE);
		String key;
		if(map==null || map.size()==0){
			sb.append("<set label='无' value='0' />");//此处不能放中文
			//sb.append("<set label='rule' value='0' />");
		}else{
//			for(int i=0;i<list.size();i++){
//				key=list.get(i);
////				sb.append("<set label='"+key+"' value='"+String.valueOf(map.get(key))+"' />");
//				sb.append("<set label='"+key+"' value='"+MapUtil.getInteger(map, key)+"' />");
//			}
			Iterator iterator = map.keySet().iterator();
			while(iterator.hasNext()){
				key = iterator.next().toString();
				sb.append("<set label='"+key+"' value='"+MapUtil.getInteger(map, key)+"' />");
			}
		}

		sb.append("</chart>");
		if(log.isDebugEnabled())
			log.debug(caption+"XML="+sb.toString());
		return sb.toString();
	}

	public static String buildLine5(String caption,String subcaption,String xAxisName,String yAxisName,Map map,Map hm_w_p2c){
		StringBuffer sb = new StringBuffer();
		sb.append("<chart imageSave='0' caption='"+caption+"' subcaption='"+subcaption+"' xAxisName='"+xAxisName+"' yAxisName='"+yAxisName+"' formatNumberScale='0' yAxisMinValue='0'  numberPrefix='' showValues='0' alternateHGridColor='D8E5F6' alternateHGridAlpha='20' divLineColor='B6D0F2' divLineAlpha='50' canvasBorderColor='666666' baseFontColor='666666' lineColor='0077AA' lineThickness='2' useRoundEdges='1'>");
//		Iterator it=map.keySet().iterator();
		List<String> list = (List<String>) map.get(StaticParameters.MONITOR_TYPE);
		String key;
		if(list==null || list.size()==0){
			sb.append("<set label='无' value='0' />");//此处不能放中文
			//sb.append("<set label='rule' value='0' />");
		}else{
			for(int i=0;i<list.size();i++){
				key=list.get(i);
//				sb.append("<set label='"+key+"' value='"+String.valueOf(map.get(key))+"' />");
				sb.append("<set label='"+key+"' value='"+MapUtil.getInteger(map, key)+"' link='JavaScript:test("+MapUtil.getString(hm_w_p2c, key)+");'/>");
			}
//			Iterator iterator = map.keySet().iterator();
//			while(iterator.hasNext()){
//				key = iterator.next().toString();
//				sb.append("<set label='"+key+"' value='"+MapUtil.getInteger(map, key)+"' />");
//			}
		}

		sb.append("</chart>");
		if(log.isDebugEnabled())
			log.debug(caption+"XML="+sb.toString());
		return sb.toString();
	}

/**
   * 横向柱状图
   * @param caption 标题
   * @param xAxisName 横向坐标说明
   * @param yAxisName 纵向坐标说明
   * @param map 存放曲线图的key,value
   * @return xml的字符串
   * 用到的图表：风险统计数
   * 样例：
   * <chart palette='2' caption='风险统计数（18:15--19:15）' shownames='1' showvalues='0' showSum='1' decimals='0' useRoundEdges='1'>
	<categories>
	<category label='登录用户数' />
	<category label='会话数' />
	<category label='使用IP数' />
	<category label='使用设备数' />
	</categories>
	<dataset seriesName='' color='00FFFF' showValues='0'>
	<set value='25601.34' />
	<set value='20148.82' />
	<set value='17372.76' />
	<set value='17372.76' />
	</dataset>
	</chart>
   */
	public static String buildBar(String caption,String subcaption,String xAxisName,String yAxisName,Map dataMap){

		List<String> list = (List<String>)dataMap.get("categories");

		StringBuffer sb = new StringBuffer();
		sb.append("<chart imageSave='0' caption='"+caption+"' subcaption='"+subcaption+"' xAxisName='"+xAxisName+"' yAxisName='"+yAxisName+"' formatNumberScale='0' yAxisMinValue='0'  numberPrefix='' showValues='0' alternateHGridColor='D8E5F6' alternateHGridAlpha='20' divLineColor='B6D0F2' divLineAlpha='50' canvasBorderColor='666666' baseFontSize='12' baseFontColor='666666' lineColor='0077AA' lineThickness='2' useRoundEdges='1'>");
		sb.append("<categories>");
		for(int i=0;i<list.size();i++){
			sb.append("<category label='"+list.get(i)+"' />");
		}
		sb.append("</categories>");
		sb.append("<dataset seriesName='' color='00FFFF' showValues='0'>");

		Map map = (Map)dataMap.get("dataset");
		for(int i=0;i<list.size();i++){
//			sb.append("<set value='"+map.get(list.get(i))+"' />");
			sb.append("<set value='"+MapUtil.getInteger(map, list.get(i))+"' />");
		}
		sb.append("</dataset></chart>");
		if(log.isDebugEnabled())
			log.debug(caption+"XML="+sb.toString());
		return sb.toString();
	}

/**
   * 柱状图
   * @param caption 标题
   * @param xAxisName 横向坐标说明
   * @param yAxisName 纵向坐标说明
   * @param map 存放曲线图的key,value
   * @return xml的字符串
   * 用到的图表：1 告警数据实时监控 2 交易运行监控
   * 样例：
   * <chart palette='2' caption='告警数实时监控（18:15--19:15）' shownames='1' showvalues='0' showSum='1' decimals='0' useRoundEdges='1'>
	<categories>
	<category label='告警总数' />
	<category label='严重级别告警' />
	<category label='高级别告警' />
	<category label='中级别告警' />
	<category label='低级别告警' />
	</categories>
	<dataset seriesName='成功告警数' color='AFD8F8' showValues='0'>
	<set value='25601.34' />
	<set value='20148.82' />
	<set value='17372.76' />
	<set value='17372.76' />
	<set value='45263.37' />
	</dataset>
	<dataset seriesName='无风险告警数' color='F6BD0F' showValues='0'>
	<set value='57401.85' />
	<set value='41941.19' />
	<set value='45263.37' />
	<set value='45263.37' />
	<set value='17372.76' />
	</dataset>
	</chart>
   */
	public static String buildStack(String caption,String subcaption,String xAxisName,String yAxisName,Map dataMap){
		StringBuffer sb = new StringBuffer();

		List<String> list = (List<String>) dataMap.get("categories");
		sb.append("<chart imageSave='0' caption='"+caption+"' subcaption='"+subcaption+"' xAxisName='"+xAxisName+"' yAxisName='"+yAxisName+"' formatNumberScale='0' yAxisMinValue='0'  numberPrefix='' showValues='0' alternateHGridColor='D8E5F6' alternateHGridAlpha='20' divLineColor='B6D0F2' divLineAlpha='50' canvasBorderColor='666666' baseFontSize='12' baseFontColor='666666' lineColor='0077AA' lineThickness='2' useRoundEdges='1'>");
		sb.append("<categories>");
		for(int i=0;i<list.size();i++){
			sb.append("<category label='"+String.valueOf(list.get(i))+"' />");
		}
		sb.append("</categories>");

		List<Map<String,Object>> dataList = (List<Map<String,Object>>)dataMap.get("dataset");
		Map<String,Object> map = new HashMap();
		String key;
		String[] keys;
		for(int i=0;i<dataList.size();i++){
			map = dataList.get(i);
//			key = (String)map.get(StaticParameters.MONITOR_TYPE);
			key = MapUtil.getString(map, StaticParameters.MONITOR_TYPE);
			keys = key.split(",");
			sb.append("<dataset seriesName='"+keys[0]+"' color='"+keys[1]+"' showValues='0'>");
			for(int index=0;index<list.size();index++){
//				sb.append("<set value='"+map.get(list.get(index))+"' />");
				sb.append("<set value='"+MapUtil.getInteger(map, list.get(index))+"' />");
			}
			sb.append("</dataset>");
		}
		sb.append("</chart>");
		if(log.isDebugEnabled())
			log.debug(caption+"XML="+sb.toString());
		return sb.toString();
	}

/**
   * 可滑动柱状图
   * @param caption 标题
   * @param xAxisName 横向坐标说明
   * @param yAxisName 纵向坐标说明
   * @param map 存放曲线图的key,value
   * @return xml的字符串
   * 用到的图表：
   * 1 规则运行监控
	2 规则运行--平均执行时间
	3 交易运行监控
	4 交易运行--平均执行时间
   *样例：
   *<chart palette='3' caption='交易运行监控--平均执行时间' subcaption='' xaxisname='' Yaxisname='' numdivlines='3' numberSuffix='ms' showSum='1' useRoundEdges='1' legendBorderAlpha='0'>
	<categories font='Arial' fontSize='12' fontColor='000000'>
		<category label='1'/>
		<category label='2'/>
		<category label='3'/>
		<category label='4'/>
		<category label='5'/>
	</categories>
	<dataset>
		<dataSet seriesName='最大执行时间' color='AFD8F8' showValues='0'>
			<set value='130' />
			<set value='126' />
			<set value='129' />
			<set value='131' />
			<set value='124' />
		</dataSet>
	</dataset>
	<dataset>
		<dataSet seriesName='最小执行时间' color='F6BD0F' showValues='0'>
			<set value='21' />
			<set value='28' />
			<set value='39' />
			<set value='41' />
			<set value='24' />
		</dataSet>
	</dataset>
	<dataSet>
		<dataset seriesname='平均执行时间' color='8BBA00'  showValues='0'>
			<set value='27' />
			<set value='25' />
			<set value='28' />
			<set value='26' />
			<set value='10' />
		</dataset>
	</dataSet>

	<trendlines>
      <line startValue='80' color='FF654F' displayValue='Target' showOnTop='1'/>
   </trendlines>
</chart>
   */
	public static String buildScrollStack(String caption,String subcaption,String xAxisName,String yAxisName,Map dataMap){
		StringBuffer sb = new StringBuffer();

		List list = (List)dataMap.get("categories");
		sb.append("<chart imageSave='0' caption='"+caption+"' subcaption='"+subcaption+"' xAxisName='"+xAxisName+"' yAxisName='"+yAxisName+"' formatNumberScale='0' yAxisMinValue='0'  numberPrefix='' showValues='0' alternateHGridColor='D8E5F6' alternateHGridAlpha='20' divLineColor='B6D0F2' divLineAlpha='50' canvasBorderColor='666666' baseFontSize='12' baseFontColor='666666' lineColor='0077AA' lineThickness='2' useRoundEdges='1'>");
		sb.append("<categories>");
		if(list==null || list.size()==0){
			sb.append("<category label='无' />");
		}else {
			for(int i=0;i<list.size();i++){
				sb.append("<category label='"+String.valueOf(list.get(i))+"' />");
			}
		}
		sb.append("</categories>");
		Map<String,Object> map1 = (Map<String,Object>)dataMap.get("NUMTRIGS");
		Map<String,Object> map2 = (Map<String,Object>)dataMap.get("NUMHITS");
		List<String> list1 = (List<String>)dataMap.get("RULENAME");

		for(int i=0;i<list1.size();i++){
			String key = list1.get(i);
			String[] keys = key.split(",");
			sb.append("<dataset seriesName='"+keys[0]+"' color='"+keys[1]+"' showValues='0'>");
//			sb.append("<set value='"+map1.get(keys[0])+"' />");
//			sb.append("<set value='"+map2.get(keys[0])+"' />");
			sb.append("<set value='"+MapUtil.getInteger(map1, keys[0])+"' />");
			sb.append("<set value='"+MapUtil.getInteger(map2, keys[0])+"' />");
			sb.append("</dataset>");
		}

		if(dataMap.get(StaticParameters.MONITOR_TARGET)!=null){
//			sb.append((String)(dataMap.get(StaticParameters.MONITOR_TARGET)));
			sb.append(MapUtil.getString(dataMap, StaticParameters.MONITOR_TARGET));
		}
		sb.append("</chart>");
		if(log.isDebugEnabled())
			log.debug(caption+"XML="+sb.toString());
		return sb.toString();
	}

/**
   * 可滑动柱状图X
   * @param caption 标题
   * @param xAxisName 横向坐标说明
   * @param yAxisName 纵向坐标说明
   * @param map 存放曲线图的key,value
   * @return xml的字符串
   */
	public static String buildScrollStackX(String caption,String subcaption,String xAxisName,String yAxisName,Map dataMap){
		StringBuffer sb = new StringBuffer();

		List list = (List)dataMap.get("categories");
		sb.append("<chart imageSave='0' caption='"+caption+"' subcaption='"+subcaption+"' xAxisName='"+xAxisName+"' yAxisName='"+yAxisName+"' formatNumberScale='0' yAxisMinValue='0'  numberPrefix='' showSum='0' alternateHGridColor='D8E5F6' alternateHGridAlpha='20' divLineColor='B6D0F2' divLineAlpha='50' canvasBorderColor='666666' baseFontSize='12' baseFontColor='666666' lineColor='0077AA' lineThickness='2' useRoundEdges='1'>");
		sb.append("<categories>");
		if(list==null || list.size()==0){
			sb.append("<category label='无' />");
		}else{
			for(int i=0;i<list.size();i++){
				sb.append("<category label='"+String.valueOf(list.get(i))+"' />");
			}
		}
		sb.append("</categories>");

		List<Map<String,Object>> dataList = (List<Map<String,Object>>)dataMap.get("dataset");
		if(dataList==null || dataList.size()==0){
			sb.append("<dataset><dataSet seriesName='无' color='' showValues='0'>");
			sb.append("<set value='0' />");
			sb.append("</dataSet></dataset>");
		}else{
			for(int i=0;i<dataList.size();i++){
				Map<String,Object> map = dataList.get(i);
				String key = (String)map.get(StaticParameters.MONITOR_TYPE);
				String[] keys = key.split(",");
				sb.append("<dataset><dataSet seriesName='"+keys[0]+"' color='"+keys[1]+"' showValues='0'>");
				for(int index=0;index<list.size();index++){
//					sb.append("<set value='"+map.get((String)list.get(index))+"' />");
					sb.append("<set value='"+MapUtil.getInteger(map, list.get(index).toString())+"' />");
				}
				sb.append("</dataSet></dataset>");
			}
		}

		if(dataMap.get(StaticParameters.MONITOR_TARGET)!=null){
//			sb.append((String)(dataMap.get(StaticParameters.MONITOR_TARGET)));
			sb.append(MapUtil.getString(dataMap, StaticParameters.MONITOR_TARGET));
		}
		sb.append("</chart>");
		if(log.isDebugEnabled())
			log.debug(caption+"XML="+sb.toString());
		return sb.toString();
	}

/**
   * 叠加可滑动柱状图
   * @param caption 标题
   * @param xAxisName 横向坐标说明
   * @param yAxisName 纵向坐标说明
   * @param map 存放曲线图的key,value
   * @return xml的字符串
   */
	public static String buildMSScrollColumn(String caption,String subcaption,String xAxisName,String yAxisName,Map dataMap){
		StringBuffer sb = new StringBuffer();

		List list = (List)dataMap.get("categories");
		sb.append("<chart imageSave='0' caption='"+caption+"' subcaption='"+subcaption+"' xAxisName='"+xAxisName+"' yAxisName='"+yAxisName+"' formatNumberScale='0' yAxisMinValue='0'  numberPrefix='' showSum='0' alternateHGridColor='D8E5F6' alternateHGridAlpha='20' divLineColor='B6D0F2' divLineAlpha='50' canvasBorderColor='666666' baseFontSize='12' baseFontColor='666666' lineColor='0077AA' lineThickness='2' useRoundEdges='1'>");
		sb.append("<categories>");
		if(list==null || list.size()==0){
			sb.append("<category label='无' />");
		}else{
			for(int i=0;i<list.size();i++){
				sb.append("<category label='"+String.valueOf(list.get(i))+"' />");
			}
		}
		sb.append("</categories>");

		List<Map<String,Object>> dataList = (List<Map<String,Object>>)dataMap.get("dataset");
		if(dataList==null || dataList.size()==0){
			sb.append("<dataSet><dataset seriesName='无' color='' showValues='0'>");
			sb.append("<set value='0' />");
			sb.append("</dataset></dataSet>");
		}
		for(int i=0;i<dataList.size();i++){
			Map<String,Object> map = dataList.get(i);
			String key = (String)map.get(StaticParameters.MONITOR_TYPE);
			String[] keys = key.split(",");
//			sb.append("<dataset><dataSet seriesName='"+keys[0]+"' color='"+keys[1]+"' showValues='0'>");
			sb.append("<dataSet><dataset seriesName='"+keys[0]+"' showValues='0'>");
			for(int index=0;index<list.size();index++){
//				sb.append("<set value='"+map.get((String)list.get(index))+"' />");
				sb.append("<set value='"+MapUtil.getInteger(map, list.get(index).toString())+"' />");
			}
			sb.append("</dataset></dataSet>");
		}

		if(dataMap.get(StaticParameters.MONITOR_TARGET)!=null){
//			sb.append((String)(dataMap.get(StaticParameters.MONITOR_TARGET)));
			sb.append(MapUtil.getString(dataMap, StaticParameters.MONITOR_TARGET));
		}
		sb.append("</chart>");
		if(log.isDebugEnabled())
			log.debug(caption+"XML="+sb.toString());
		return sb.toString();
	}

	/**
	   * 叠加可滑动柱状图
	   * @param caption 标题
	   * @param xAxisName 横向坐标说明
	   * @param yAxisName 纵向坐标说明
	   * @param map 存放曲线图的key,value
	   * @return xml的字符串
	   */
		public static String buildMSScrollColumn3d(String caption,String subcaption,String xAxisName,String yAxisName,Map dataMap){
			StringBuffer sb = new StringBuffer();

			List list = (List)dataMap.get("categories");
			sb.append("<chart imageSave='0' caption='"+caption+"' subcaption='"+subcaption+"' xAxisName='"+xAxisName+"' yAxisName='"+yAxisName+"' formatNumberScale='0' yAxisMinValue='0'  numberPrefix='' showSum='0' alternateHGridColor='D8E5F6' alternateHGridAlpha='20' divLineColor='B6D0F2' divLineAlpha='50' canvasBorderColor='666666' baseFontSize='12' baseFontColor='666666' lineColor='0077AA' lineThickness='2' useRoundEdges='1'>");
			sb.append("<categories>");
			if(list==null || list.size()==0){
				sb.append("<category label='无' />");
			}else{
				for(int i=0;i<list.size();i++){
					sb.append("<category label='"+String.valueOf(list.get(i))+"' />");
				}
			}
			sb.append("</categories>");

			List<Map<String,Object>> dataList = (List<Map<String,Object>>)dataMap.get("dataset");
			if(dataList==null || dataList.size()==0){
				sb.append("<dataset seriesName='无' color='' showValues='0'>");
				sb.append("<set value='0' />");
				sb.append("</dataset>");
			}
			for(int i=0;i<dataList.size();i++){
				Map<String,Object> map = dataList.get(i);
				String key = (String)map.get(StaticParameters.MONITOR_TYPE);
				String[] keys = key.split(",");
//				sb.append("<dataset><dataSet seriesName='"+keys[0]+"' color='"+keys[1]+"' showValues='0'>");
				sb.append("<dataset seriesName='"+keys[0]+"' showValues='0'>");
				for(int index=0;index<list.size();index++){
//					sb.append("<set value='"+map.get((String)list.get(index))+"' />");
					sb.append("<set value='"+MapUtil.getInteger(map, list.get(index).toString())+"' />");
				}
				sb.append("</dataset>");
			}

			if(dataMap.get(StaticParameters.MONITOR_TARGET)!=null){
//				sb.append((String)(dataMap.get(StaticParameters.MONITOR_TARGET)));
				sb.append(MapUtil.getString(dataMap, StaticParameters.MONITOR_TARGET));
			}
			sb.append("</chart>");
			if(log.isDebugEnabled())
				log.debug(caption+"XML="+sb.toString());
			return sb.toString();
		}

/**
   * 可滑动曲线图（scroll）
   * @param caption 标题
   * @param xAxisName 横向坐标说明
   * @param yAxisName 纵向坐标说明
   * @param map 存放曲线图的key,value
   * @return xml的字符串
   * 用到的图表：告警趋势图
   * 样例：
   * <chart caption='' subCaption='' numdivlines='9' lineThickness='2' showValues='0' anchorRadius='3' anchorBgAlpha='50' showAlternateVGridColor='1' numVisiblePlot='12' animation='0'>
		<categories >
		<category label='13:30' />
		</categories>
		<dataset seriesName='总数' color='008080' anchorBorderColor='FF0080'>
		<set value='9154' />
		</dataset>
		<dataset seriesName='严重' color='FF0080' anchorBorderColor='FF0080'>
		<set value='54' />
		</dataset>
		<dataset seriesName='高' color='FF8040' anchorBorderColor='FF8040'>
		<set value='911' />
		</dataset>
		<dataset seriesName='中' color='FFFF00' anchorBorderColor='FFFF00' >
		<set value='715' />
		</dataset>
		<dataset seriesName='低' color='800080' anchorBorderColor='800080' >
		<set value='98' />
		</dataset>
		</chart>
   *
   */
	public static String buildScrollLine(String caption,String subcaption,String xAxisName,String yAxisName,Map dataMap){
		StringBuffer sb = new StringBuffer();

		List list = (List)dataMap.get("categories");
		sb.append("<chart imageSave='0' caption='"+caption+"' subcaption='"+subcaption+"' xAxisName='"+xAxisName+"' yAxisName='"+yAxisName+"' formatNumberScale='0' yAxisMinValue='0'  numberPrefix='' showValues='0' alternateHGridColor='D8E5F6' alternateHGridAlpha='20' divLineColor='B6D0F2' divLineAlpha='50' canvasBorderColor='666666' baseFontSize='12' baseFontColor='666666' lineColor='0077AA' lineThickness='2' useRoundEdges='1'>");
		sb.append("<categories>");
		for(int i=0;i<list.size();i++){
			sb.append("<category label='"+String.valueOf(list.get(i))+"' />");
		}
		sb.append("</categories>");

		List<Map<String,Object>> dataList = (List<Map<String,Object>>)dataMap.get("dataset");
		if(dataList==null || dataList.size()==0){
			sb.append("<dataset seriesName='无' color='008080' anchorBorderColor='008080'>");
			sb.append("<set value='0' />");
			sb.append("</dataset>");
		}else{
			for(int i=0;i<dataList.size();i++){
				Map<String,Object> mapData = dataList.get(i);
				String key = (String)mapData.get(StaticParameters.MONITOR_TYPE);
				String[] keys = key.split(",");
				sb.append("<dataset seriesName='"+keys[0]+"' color='"+keys[1]+"' anchorBorderColor='"+keys[1]+"'>");
				for(int index = 0;index <list.size();index++){
//					sb.append("<set value='"+mapData.get(list.get(index))+"' />");
					sb.append("<set value='"+MapUtil.getInteger(mapData, list.get(index).toString())+"' />");
				}
				sb.append("</dataset>");
			}
		}

		sb.append("</chart>");
		if(log.isDebugEnabled())
			log.debug(caption+"XML="+sb.toString());
		return sb.toString();
	}

	public static String buildMSSpline(String caption,String subcaption,String xAxisName,String yAxisName,Map dataMap){
		StringBuffer sb = new StringBuffer();

		List list = (List)dataMap.get("categories");
		sb.append("<chart imageSave='0' caption='"+caption+"' subcaption='"+subcaption+"' xAxisName='"+xAxisName+"' yAxisName='"+yAxisName+"' formatNumberScale='0' yAxisMinValue='0'  numberPrefix='' showValues='0' alternateHGridColor='D8E5F6' alternateHGridAlpha='20' divLineColor='B6D0F2' divLineAlpha='50' canvasBorderColor='666666' baseFontSize='12' baseFontColor='666666' lineColor='0077AA' lineThickness='2' useRoundEdges='1'>");
		sb.append("<categories>");
		for(int i=0;i<list.size();i++){
			sb.append("<category label='"+String.valueOf(list.get(i))+"' />");
		}
		sb.append("</categories>");

		List<Map<String,Object>> dataList = (List<Map<String,Object>>)dataMap.get("dataset");
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
//					sb.append("<set value='"+mapData.get(list.get(index))+"' />");
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
	/**
	 * 仪表盘
	 * @param dataMap
	 * @return
	 */
	public static String buildAngular(Map<String,Object> dataMap){
		StringBuffer sb = new StringBuffer();
		sb.append("<chart bgAlpha='0' bgColor='FFFFFF' lowerLimit='0' upperLimit='100' numberSuffix='%25' showBorder='0' basefontColor='FFFFDD' chartTopMargin='25' chartBottomMargin='25'");
		sb.append("chartLeftMargin='25' chartRightMargin='25' toolTipBgColor='80A905' gaugeFillMix='{dark-10},FFFFFF,{dark-10}' gaugeFillRatio='3'>");
		sb.append("<colorRange>");
		sb.append("<color minValue='0' maxValue='45' code='8BBA00'/>");
		sb.append("<color minValue='45' maxValue='80' code='F6BD0F'/>");
		sb.append("<color minValue='80' maxValue='100' code='FF654F'/>");
		sb.append("</colorRange>");
		sb.append("<dials>");

//		sb.append("<dial value='"+(dataMap.get("dial")==null?0:dataMap.get("dial"))+"' rearExtension='10'/>");
		sb.append("<dial value='"+MapUtil.getString(dataMap, StaticParameters.MONITOR_DIAL)+"' rearExtension='10'/>");

		sb.append("</dials>");
		sb.append("<trendpoints>");

//		sb.append("<point value='50' displayValue='"+(dataMap.get(StaticParameters.MONITOR_TYPE)==null?0:dataMap.get(StaticParameters.MONITOR_TYPE))+"' fontcolor='FF4400' useMarker='1' dashed='1' dashLen='2' dashGap='2' valueInside='1' />");
		sb.append("<point value='50' displayValue='"+MapUtil.getString(dataMap, StaticParameters.MONITOR_TYPE)+"' fontcolor='FF4400' useMarker='1' dashed='1' dashLen='2' dashGap='2' valueInside='1' />");

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
			log.debug(MapUtil.getString(dataMap, StaticParameters.MONITOR_TYPE)+"XML="+sb.toString());
		return sb.toString();
	}

}
