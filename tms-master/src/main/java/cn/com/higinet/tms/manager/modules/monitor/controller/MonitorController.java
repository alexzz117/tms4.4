package cn.com.higinet.tms.manager.modules.monitor.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.manager.common.CodeDict;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.IPLocationService;
import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.monitor.model.DataVO;
import cn.com.higinet.tms.manager.modules.monitor.model.FusionChartVO;
import cn.com.higinet.tms.manager.modules.monitor.service.AlarmService;
import cn.com.higinet.tms.manager.modules.monitor.service.FusionChart;
import cn.com.higinet.tms.manager.modules.monitor.service.MonitorService;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;

/**
 * 实时监控模块控制类
 * @author wangsch
 * @date 2013-05-06
 * @author zhang.lei
 */

@RestController("monitorController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/monitor")
public class MonitorController extends ApplicationObjectSupport {

	//用于控制交易运行监控中，显示前几个交易
	private static final int NUMTXNTOPS = 5;

	@Autowired
	private AlarmService alarmService;

	@Autowired
	private MonitorService monitorService;

	@Autowired
	private CodeDict codeDict;

	@Autowired
	private IPLocationService ipLocationService;

	@Autowired
	private SqlMap tmsSqlMap;

	/**
	 * 跳转到规则运行监控页面
	 * @return
	 */
	@RequestMapping(value = "/listRule", method = RequestMethod.GET)
	public String listRuleView() {
		return "tms/monitor/monitor_rule";
	}

	/**
	 * 跳转到交易运行监控页面
	 * @return
	 */
	@RequestMapping(value = "/listTrans", method = RequestMethod.GET)
	public String listTransView() {
		return "tms/monitor/monitor_traffic";
	}

	/**
	 * 跳转到实时告警监控页面
	 * @return
	 */
	@RequestMapping(value = "/listAlert", method = RequestMethod.GET)
	public String listAlertView() {
		return "tms/monitor/monitor_alert";
	}

	/**
	 * 跳转到风险统计数页面
	 * @return
	 */
	@RequestMapping(value = "/listRisk", method = RequestMethod.GET)
	public String listRiskView() {
		return "tms/monitor/monitor_risk";
	}

	/**
	 * 跳转到实时告警监控页面
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String listCenterView() {
		return "tms/monitor/monitor_centerNew";
	}

	/**
	 * 跳转到实时告警监控页面
	 * @return
	 */
	@RequestMapping(value = "/listY15", method = RequestMethod.GET)
	public String listCenterY15View() {
		return "tms/monitor/monitor_center_y15";
	}

	/**
	 * 规则运行监控表格数据展示
	 * @param reqs 请求参数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/grid/all/rule", method = RequestMethod.POST)
	public Model listRuleActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		Page<Map<String, Object>> dataPage = (Page<Map<String, Object>>) getMonitorData( reqs, new String[] {
				StaticParameters.S_TMS_MONITOR_RULE_TEMP_CHART, StaticParameters.S_TMS_MONITOR_RULE_HOUR_CHART
		}, null, true );
		this.getRuleAfterProcess( dataPage.getList() );
		model.setPage( dataPage );
		return model;
	}

	/**
	 * 规则运行监控图表数据展示
	 * @param reqs 请求参数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/grid/all/ruleChart", method = RequestMethod.POST)
	public Model listRuleChartActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		List<String> list = new ArrayList<String>();
		List<Map<String, Object>> dataList = (List<Map<String, Object>>) getMonitorData( reqs, new String[] {
				StaticParameters.S_TMS_MONITOR_RULE_TEMP_CHART, StaticParameters.S_TMS_MONITOR_RULE_HOUR_CHART
		}, null, false );
		list.add( getRMRuleChart( dataList, reqs ) );
		model.setRow( list );
		return model;
	}

	/**
	 * 交易运行监控表格数据展示
	 * @param reqs 请求参数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/grid/all/txn", method = RequestMethod.POST)
	public Model listTxnActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		String extSql = getTrafficExtSql( reqs );
		Page<Map<String, Object>> dataPage = (Page<Map<String, Object>>) getMonitorData( reqs, new String[] {
				StaticParameters.S_TMS_MONITOR_TXN_TEMP_CHART, StaticParameters.S_TMS_MONITOR_TXN_HOUR_CHART
		}, extSql, true );
		getTrafficAfterProcess( dataPage.getList() );
		model.setPage( dataPage );
		return model;
	}

	/**
	 * 运行监控表格数据展示--欺诈类型
	 * @param reqs 请求参数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/grid/all/fraud", method = RequestMethod.POST)
	public Model listFraudActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		String extSql = "";
		//		List<String> list = new ArrayList<String>();
		Page<Map<String, Object>> dataPage = (Page<Map<String, Object>>) getMonitorTmpData( reqs, new String[] {
				StaticParameters.S_TMS_MONITOR_FRAUD_TEMP_CHART, StaticParameters.S_TMS_MONITOR_FRAUD_HOUR_CHART
		}, extSql, true );
		getFraudAfterProcess( dataPage.getList() );
		model.setPage( dataPage );
		return model;
	}

	/**
	 * 运行监控表格数据展示--欺诈类型
	 * @param reqs 请求参数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/grid/all/map", method = RequestMethod.POST)
	public Model listMapActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		//		List<String> list = new ArrayList<String>();
		String extSql = tmsSqlMap.getSql( StaticParameters.S_TMS_MONITOR_MAP_TEMP_CHART );
		extSql = extSql.toString().replace( "TMS_MGR_CITY", ipLocationService.getLocationCurrName( "TMS_MGR_CITY" ) );
		extSql = extSql.toString().replace( "TMS_MGR_REGION", ipLocationService.getLocationCurrName( "TMS_MGR_REGION" ) );

		Page<Map<String, Object>> dataPage = (Page<Map<String, Object>>) getMonitorData( reqs, "", extSql, true );
		getMapAfterProcess( dataPage.getList() );
		model.setPage( dataPage );
		return model;
	}

	/**
	 * 交易运行监控图表数据展示
	 * @param reqs	请求参数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/grid/all/txnChart", method = RequestMethod.POST)
	public Model listTxnChartActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		String extSql = getTrafficExtSql( reqs );
		List<String> list = new ArrayList<String>();
		List<Map<String, Object>> dataList = (List<Map<String, Object>>) getMonitorData( reqs, new String[] {
				StaticParameters.S_TMS_MONITOR_TXN_TEMP_CHART, StaticParameters.S_TMS_MONITOR_TXN_HOUR_CHART
		}, extSql, false );
		list.add( getRMTrafficChart( dataList ) );
		model.setRow( list );
		return model;
	}

	/**
	 * 实时报警监控表格及图表展示
	 * @param reqs 请求参数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/grid/all/alert", method = RequestMethod.POST)
	public Model listAlertActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		List<String> list = new ArrayList<String>();
		List<Map<String, Object>> dataList = (List<Map<String, Object>>) getMonitorData( reqs, new String[] {
				StaticParameters.S_TMS_MONITOR_ALERT_CHART
		}, null, false );
		model.setPage( getAlarmAfterProcess( dataList ) );
		list.add( getRMAlertChart( dataList, reqs ) );
		model.setRow( list );
		return model;
	}

	/**
	 * 风险数据统计表格及图表展示
	 * @param reqs	请求参数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/grid/all/rmriskst", method = RequestMethod.POST)
	public Model listStActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		List<String> list = new ArrayList<String>();
		List<Map<String, Object>> dataList = (List<Map<String, Object>>) getMonitorData( reqs, new String[] {
				StaticParameters.S_TMS_MONITOR_RISK_TEMP_CHART, StaticParameters.S_TMS_MONITOR_RISK_HOUR_CHART
		}, null, false );
		Page<Map<String, Object>> dataPage = new Page<Map<String, Object>>();
		dataPage.setSize( dataList.size() );
		dataPage.setTotal( dataList.size() );
		dataPage.setList( dataList );
		model.setPage( dataPage );
		list.add( getRMRiskChart( dataPage.getList(), reqs ) );
		model.setRow( list );
		return model;
	}

	/**
	 * 单个规则命中率数据展示
	 * @param reqs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/grid/all/onerule", method = RequestMethod.POST)
	public Model oneRuleActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		List<String> list = new ArrayList<String>();
		DataVO dataVO = new DataVO();
		Map<String, Object> conds = new HashMap<String, Object>();
		String frm = MapUtil.getString( reqs, "frm" );
		String ruleId = MapUtil.getString( reqs, "ruleId" );
		if( ruleId != null && !"".equals( ruleId ) ) {
			conds.put( "ruleId", ruleId );
			if( frm.equals( "currentTime" ) ) {
				addTmpCond( conds );
				dataVO.setSqlId( "tms.monitor.s_onerule_temp_chart" );
			}
			else {
				addHourCond( conds, frm );
				dataVO.setSqlId( "tms.monitor.s_onerule_hour_chart" );
			}
			dataVO.setCond( conds );
			dataVO.setDataType( "commonReportData" );
			dataVO = monitorService.getDataList( dataVO, true );
			List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataVO.getData();

			list.add( getRMRuleChart( dataList, reqs ) );
			model.setRow( list );
		}
		return model;
	}

	//获取国家
	@RequestMapping(value = "/alarm/get_all_country", method = RequestMethod.POST)
	public Model listAllCountryAction( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.setRow( alarmService.listAllCountry() );
		return model;
	}

	//获取地区
	@RequestMapping(value = "/alarm/get_region_by_country", method = RequestMethod.POST)
	public Model listRegionAction( @RequestBody Map<String, String> reqs ) {
		String country = (String) (reqs.get( "country" ) == null ? "" : reqs.get( "country" ));
		Model model = new Model();
		model.setRow( alarmService.listRegionByCountry( country ) );
		return model;
	}

	//获取城市
	@RequestMapping(value = "/alarm/get_city_by_region", method = RequestMethod.POST)
	public Model listCityAction( @RequestBody Map<String, String> reqs ) {
		String region = (String) (reqs.get( "region" ) == null ? "" : reqs.get( "region" ));
		Model model = new Model();
		model.setRow( alarmService.listCityByRegion( region ) );
		return model;
	}

	/**
	 * 查询系统时间，返回系统时间及查询倒数开始时间
	 * @return
	 */
	@RequestMapping(value = "/alarm/time", method = RequestMethod.POST)
	public Model getTime() {
		Model model = new Model();

		// 取当前时间
		Timestamp time = CalendarUtil.getCurrentTime();
		String currentTime = CalendarUtil.FORMAT14.format( time );

		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "currentTime", currentTime );
		model.setRow( conds );

		return model;
	}

	@SuppressWarnings("unchecked")
	private Object getMonitorTmpData( Map<String, String> reqs, String[] sqlNames, String extSql, boolean isPage ) {
		DataVO dataVO = new DataVO();
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.putAll( reqs );
		String frm = MapUtil.getString( conds, "frm" );
		if( !CmcStringUtil.isBlank( frm ) && (sqlNames != null && sqlNames.length == 2) ) {
			if( frm.equals( "currentTime" ) ) {
				addTmpCond( conds );
				dataVO.setSqlId( sqlNames[0] );
			}
			else {
				addHourCond( conds, frm );
				dataVO.setSqlId( sqlNames[1] );
			}
		}
		else if( sqlNames != null && sqlNames.length == 1 ) {
			addTmpCond( conds );
			dataVO.setSqlId( sqlNames[0] );
		}
		dataVO.setUseTmp( true );
		dataVO.setCond( conds );
		dataVO.setExtSql( extSql );
		dataVO.setDataType( "commonReportData" );
		dataVO = monitorService.getDataList( dataVO, isPage );
		Object dataInfo = null;
		if( isPage ) {
			dataInfo = (Page<Map<String, Object>>) dataVO.getData();
		}
		else {
			dataInfo = (List<Map<String, Object>>) dataVO.getData();
		}
		return dataInfo;
	}

	private Object getMonitorData( Map<String, String> reqs, String[] sqlNames, String extSql, boolean isPage ) {
		DataVO dataVO = new DataVO();
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.putAll( reqs );
		String frm = MapUtil.getString( conds, "frm" );
		if( !CmcStringUtil.isBlank( frm ) && (sqlNames != null && sqlNames.length == 2) ) {
			if( frm.equals( "currentTime" ) ) {
				addTmpCond( conds );
				dataVO.setSqlId( sqlNames[0] );
			}
			else {
				addHourCond( conds, frm );
				dataVO.setSqlId( sqlNames[1] );
			}
		}
		else if( sqlNames != null && sqlNames.length == 1 ) {
			addTmpCond( conds );
			dataVO.setSqlId( sqlNames[0] );
		}
		dataVO.setCond( conds );
		dataVO.setExtSql( extSql );
		dataVO.setDataType( "commonReportData" );
		dataVO = monitorService.getDataList( dataVO, isPage );
		Object dataInfo = null;
		if( isPage ) {
			dataInfo = (Page<Map<String, Object>>) dataVO.getData();
		}
		else {
			dataInfo = (List<Map<String, Object>>) dataVO.getData();
		}
		return dataInfo;
	}

	@SuppressWarnings("unchecked")
	private Object getMonitorData( Map<String, String> reqs, String sqlId, String extSql, boolean isPage ) {
		DataVO dataVO = new DataVO();
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.putAll( reqs );
		dataVO.setSqlId( sqlId );
		dataVO.setCond( conds );
		dataVO.setExtSql( extSql );
		dataVO.setDataType( "commonReportData" );
		dataVO = monitorService.getDataList( dataVO, isPage );
		Object dataInfo = null;
		if( isPage ) {
			dataInfo = (Page<Map<String, Object>>) dataVO.getData();
		}
		else {
			dataInfo = (List<Map<String, Object>>) dataVO.getData();
		}
		return dataInfo;
	}

	/**
	 * 规则运行监控表格数据
	 * @param dataList
	 * @return
	 */
	private List<Map<String, Object>> getRuleAfterProcess( List<Map<String, Object>> dataList ) {
		for( Map<String, Object> data : dataList ) {
			if( data.get( "RULERATE" ) != null ) {
				data.put( "RULERATE", data.get( "RULERATE" ) + "%" );
			}
			String txntype = MapUtil.getString( data, "TXNTYPE" );
			if( txntype != null && !"".equals( txntype ) ) {
				data.put( "TXNNAME", monitorService.getFullTxnPath( txntype, DBConstant.TMS_COM_TAB.TAB_DESC ) );
			}
		}
		return dataList;
	}

	/**
	 * 交易运行监控表格数据
	 * @param dataList
	 * @return
	 */
	private List<Map<String, Object>> getTrafficAfterProcess( List<Map<String, Object>> dataList ) {
		for( Map<String, Object> data : dataList ) {
			//			data.put("TXNSUCCESSRATE", MapUtil.getString(data, "TXNSUCCESSRATE")+"%");
			//			data.put("TXNALERTRATE", MapUtil.getString(data, "TXNALERTRATE")+"%");
			String txntype = MapUtil.getString( data, "TXNTYPE" );
			if( txntype != null && !"".equals( txntype ) ) {
				data.put( "TXNNAME", monitorService.getFullTxnPath( txntype, DBConstant.TMS_COM_TAB.TAB_DESC ) );
			}
		}
		return dataList;
	}

	/**
	 * 欺诈运行监控表格数据
	 * @param dataList
	 * @return
	 */
	private List<Map<String, Object>> getFraudAfterProcess( List<Map<String, Object>> dataList ) {
		Map<String, String> fdmap = codeDict.getCodes( "tms.alarm.fraudtype" );
		for( int i = 0; i < dataList.size(); i++ ) {
			String fmCode = MapUtil.getString( dataList.get( i ), "FRAUDTYPE" );
			String name = MapUtil.getString( fdmap, fmCode );
			dataList.get( i ).put( "FRAUDNAME", (name == "") ? fmCode : name );
		}
		return dataList;
	}

	/**
	 * 地图运行监控表格数据
	 * @param list
	 * @return
	 */
	private List<Map<String, Object>> getMapAfterProcess( List<Map<String, Object>> dataList ) {
		Map<String, Map<String, Object>> dataMap = new LinkedHashMap<String, Map<String, Object>>();
		for( Map<String, Object> map : dataList ) {
			String cityCode = MapUtil.getString( map, "CITYCODE" );
			String disposal = MapUtil.getString( map, "DISPOSAL" );
			String key = cityCode + disposal;
			if( dataMap.containsKey( key ) ) {
				Map<String, Object> value = dataMap.get( key );
				value.put( "CITYNUM", (MapUtil.getInteger( value, "CITYNUM" )) + 1 );
			}
			else {
				Map<String, Object> value = new HashMap<String, Object>();
				value.putAll( map );
				value.put( "CITYNUM", 1 );
				dataMap.put( key, value );
			}
		}
		dataList.clear();
		dataList.addAll( dataMap.values() );
		return dataList;
	}

	/**
	 * 实时告警监控表格数据，只显示一行，即个分数段总报警数是多少
	 * @param dataList 数据List
	 * @return
	 */
	private Page<Map<String, Object>> getAlarmAfterProcess( List<Map<String, Object>> dataList ) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> dataMap = new HashMap<String, Object>();
		Page<Map<String, Object>> dataPage = new Page<Map<String, Object>>();
		if( dataList != null && dataList.size() > 0 ) {
			for( Map<String, Object> map : dataList ) {
				dataMap.put( "NUMALL", MapUtil.getInteger( dataMap, "NUMALL" ) + MapUtil.getInteger( map, "NUMALL" ) );//总告警数
				dataMap.put( "NUM0", MapUtil.getInteger( dataMap, "NUM0" ) + MapUtil.getInteger( map, "NUM0" ) );//0-20分告警数
				dataMap.put( "NUM2", MapUtil.getInteger( dataMap, "NUM2" ) + MapUtil.getInteger( map, "NUM2" ) );//21-50分告警数
				dataMap.put( "NUM5", MapUtil.getInteger( dataMap, "NUM5" ) + MapUtil.getInteger( map, "NUM5" ) );//51-70分告警数
				dataMap.put( "NUM7", MapUtil.getInteger( dataMap, "NUM7" ) + MapUtil.getInteger( map, "NUM7" ) );//71-90分告警数
				dataMap.put( "NUM9", MapUtil.getInteger( dataMap, "NUM9" ) + MapUtil.getInteger( map, "NUM9" ) );//大于90分告警数
			}
		}
		else {
			dataMap.put( "NUMALL", 0 );
			dataMap.put( "NUM0", 0 );
			dataMap.put( "NUM2", 0 );
			dataMap.put( "NUM5", 0 );
			dataMap.put( "NUM7", 0 );
			dataMap.put( "NUM9", 0 );
		}
		list.add( dataMap );
		dataPage.setList( list );
		return dataPage;
	}

	//规则命中率仪表图，modified by wangsch 2013-06-02
	private String getRMRuleChart( List<Map<String, Object>> dataList, Map<String, String> reqs ) {
		FusionChartVO fChartVO = new FusionChartVO();
		Map<String, Integer> dataMap = new HashMap<String, Integer>();

		//zhangfg 2012-1-4 如果前台选择了某个规则，那么规则命中率仪表盘只表示该规则的命中率
		String ruleId = reqs.get( "ruleId" );
		for( Map<String, Object> map : dataList ) {
			if( ruleId != null && !"".equals( ruleId ) ) {
				if( ruleId.equals( MapUtil.getString( map, "RULEID" ) ) ) {
					dataMap.put( "NUMHITS", MapUtil.getInteger( dataMap, "NUMHITS" ) + MapUtil.getInteger( map, "NUMHITS" ) );
					dataMap.put( "NUMTXNS", MapUtil.getInteger( dataMap, "NUMTXNS" ) + MapUtil.getInteger( map, "NUMTXNS" ) );
				}
			}
			else {
				dataMap.put( "NUMHITS", MapUtil.getInteger( dataMap, "NUMHITS" ) + MapUtil.getInteger( map, "NUMHITS" ) );
				dataMap.put( "NUMTXNS", MapUtil.getInteger( dataMap, "NUMTXNS" ) + MapUtil.getInteger( map, "TXNNUM" ) );
			}
		}

		dataMap.put( StaticParameters.MONITOR_DENOMINATOR, MapUtil.getInteger( dataMap, "NUMTXNS" ) );
		dataMap.put( StaticParameters.MONITOR_NUMERATOR, MapUtil.getInteger( dataMap, "NUMHITS" ) );

		fChartVO.setDataMap( dataMap );
		fChartVO.setCaption( "总规则命中率" );

		Object[] params = {};
		String chartData = getApplicationContext().getBean( "gradientAngularFusionChart", FusionChart.class ).generateChart( fChartVO, params );

		return chartData;
	}

	/**
	 * 交易运行监控，柱状图
	 * @param dataList
	 * @return
	 */
	private String getRMTrafficChart( List<Map<String, Object>> dataList ) {
		FusionChartVO fChartVO = new FusionChartVO();
		Map<String, Integer> dataMap = new LinkedHashMap<String, Integer>();

		//取交易数前5名的数据
		int i = 1;
		for( Map<String, Object> map : dataList ) {
			dataMap.put( MapUtil.getString( map, "TXNNAME" ), MapUtil.getInteger( map, "NUMTXNS" ) );
			if( ++i > NUMTXNTOPS ) {
				break;
			}
		}

		fChartVO.setDataMap( dataMap );
		fChartVO.setCaption( "交易运行监控图" );

		Object[] params = {};
		String chartData = getApplicationContext().getBean( "column2DFusionChart", FusionChart.class ).generateChart( fChartVO, params );
		return chartData;
	}

	/**
	 * 风险统计图表数据
	 * @param dataList
	 * @param reqs
	 * @return
	 */
	private String getRMRiskChart( List<Map<String, Object>> dataList, Map<String, String> reqs ) {
		FusionChartVO fChartVO = new FusionChartVO();
		Map<String, Integer> dataMap = new HashMap<String, Integer>();
		List<String> tempList = new ArrayList<String>();
		for( Map<String, Object> map : dataList ) {
			dataMap.put( MapUtil.getString( map, "STNAME" ), MapUtil.getInteger( map, "STVALUE" ) );
			tempList.add( MapUtil.getString( map, "STNAME" ) );
		}
		fChartVO.setDataMap( dataMap );
		fChartVO.setCaption( "风险统计数" + "(" + getTimeScope( reqs ) + ")" );
		Object[] params = {
				tempList
		};//传递地图需要的参数信息
		String chartData = getApplicationContext().getBean( "stackedBar2DFusionChart", FusionChart.class ).generateChart( fChartVO, params );
		return chartData;
	}

	/**
	 * 实时告警监控报警数趋势图数据
	 * @param dataList 数据列表
	 * @param reqs 请求参数
	 * @return
	 */
	private String getRMAlertChart( List<Map<String, Object>> dataList, Map<String, String> reqs ) {
		FusionChartVO fChartVO = new FusionChartVO();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> mapall = new HashMap<String, Object>(); //总告警数
		Map<String, Object> map0 = new HashMap<String, Object>(); //0-20分告警数
		Map<String, Object> map2 = new HashMap<String, Object>(); //21-50分告警数
		Map<String, Object> map5 = new HashMap<String, Object>(); //51-70分告警数
		Map<String, Object> map7 = new HashMap<String, Object>(); //71-90分告警数
		Map<String, Object> map9 = new HashMap<String, Object>(); //大于90分告警数

		List<String> tempList = getScope(); //获取一小时的时间刻度，作为趋势图展示时的参数
		if( dataList == null || dataList.size() == 0 ) { //没有数据时
			mapall.put( StaticParameters.MONITOR_ALARM_NON, 0 );
			map0.put( StaticParameters.MONITOR_ALARM_NON, 0 );
			map2.put( StaticParameters.MONITOR_ALARM_NON, 0 );
			map5.put( StaticParameters.MONITOR_ALARM_NON, 0 );
			map7.put( StaticParameters.MONITOR_ALARM_NON, 0 );
			map9.put( StaticParameters.MONITOR_ALARM_NON, 0 );
		}
		else { //有数据时
			for( Map<String, Object> mapData : dataList ) {
				mapall.put( MapUtil.getString( mapData, "TIMEID" ), MapUtil.getInteger( mapData, "NUMALL" ) );
				map0.put( MapUtil.getString( mapData, "TIMEID" ), MapUtil.getInteger( mapData, "NUM0" ) );
				map2.put( MapUtil.getString( mapData, "TIMEID" ), MapUtil.getInteger( mapData, "NUM2" ) );
				map5.put( MapUtil.getString( mapData, "TIMEID" ), MapUtil.getInteger( mapData, "NUM5" ) );
				map7.put( MapUtil.getString( mapData, "TIMEID" ), MapUtil.getInteger( mapData, "NUM7" ) );
				map9.put( MapUtil.getString( mapData, "TIMEID" ), MapUtil.getInteger( mapData, "NUM9" ) );
			}
		}

		//图例显示，名称和颜色
		mapall.put( StaticParameters.MONITOR_TYPE, "总报警数" + ",008080" );
		map0.put( StaticParameters.MONITOR_TYPE, "0-20分报警数" + ",FF0080" );
		map2.put( StaticParameters.MONITOR_TYPE, "21-50分报警数" + ",FFEE40" );
		map5.put( StaticParameters.MONITOR_TYPE, "51-70分报警数" + ",AAEA30" );
		map7.put( StaticParameters.MONITOR_TYPE, "71-90分报警数" + ",DD00080" );
		map9.put( StaticParameters.MONITOR_TYPE, "大于90分报警数" + ",CCAADD" );

		//将数据填充到ChartVO中
		list.add( mapall );
		list.add( map0 );
		list.add( map2 );
		list.add( map5 );
		list.add( map7 );
		list.add( map9 );
		fChartVO.setDataList( list );

		//设置图表的标题
		fChartVO.setCaption( "报警数趋势图" + "(" + getTimeScope( reqs ) + ")" );

		Object[] params = {
				tempList
		}; //传递时间刻度参数
		String chartData = getApplicationContext().getBean( "scrollLine2DFusionChart", FusionChart.class ).generateChart( fChartVO, params ); //返回图表xml，前端解析成图表

		return chartData;
	}

	//取得当前毫秒数和count个小时前的毫秒数，添加到查询条件中
	private void addHourCond( Map<String, Object> conds, String count ) {
		long currentTime = System.currentTimeMillis();
		long hourAgoTime = currentTime - 3600000 * Integer.parseInt( count );
		conds.put( "currentTime", currentTime + "" );
		conds.put( "hourAgoTime", hourAgoTime + "" );
	}

	//计算当前时间毫秒数和一小时前毫秒数，添加到查询条件中
	private void addTmpCond( Map<String, Object> conds ) {
		long currentTime = System.currentTimeMillis();
		long oneHourAgoTime = currentTime - 3600000;
		conds.put( "currentTime", currentTime + "" );
		conds.put( "oneHourAgoTime", oneHourAgoTime + "" );
	}

	//趋势图时间刻度
	private List<String> getScope() {
		Timestamp t = new Timestamp( System.currentTimeMillis() );

		List<String> list = new ArrayList<String>();
		List<String> listTime = new ArrayList<String>();

		for( int i = 0; i < 60; i++ ) {
			if( i < 10 ) {
				listTime.add( "0" + i );
			}
			else {
				listTime.add( "" + i );
			}
		}

		SimpleDateFormat FORMAT5 = new SimpleDateFormat( "mm" );
		SimpleDateFormat FORMAT4 = new SimpleDateFormat( "HH" );

		String minute = FORMAT5.format( t );
		String hour = FORMAT4.format( t ) == "00" ? "24" : FORMAT4.format( t );

		int index = 0;
		index = Integer.parseInt( minute );

		for( int i = 0; i < 61; i++ ) {
			int nowHour = Integer.parseInt( hour );
			if( index > 59 ) {
				list.add( (nowHour < 10 ? ("0" + nowHour) : nowHour) + ":" + listTime.get( index % 60 ) );
			}
			else {
				list.add( (nowHour - 1 < 10 ? ("0" + (nowHour - 1)) : nowHour - 1) + ":" + listTime.get( index % 60 ) );
			}
			index++;
		}
		return list;
	}

	/**
	 * 图表标题中的时间
	 * @param conds
	 * @return
	 */
	private String getTimeScope( Map<String, String> conds ) {
		int scope = 1;
		String temp = "";
		if( !("".equalsIgnoreCase( MapUtil.getString( conds, "frm" ) )) && MapUtil.getString( conds, "frm" ) != null ) {
			temp = MapUtil.getString( conds, "frm" );
		}
		// 带上日期 maxiao 2011-8-30
		String newDay = CalendarUtil.getBeforeDateBySecond( CalendarUtil.FORMAT14, 0 ).substring( 0, 16 );
		String oldDay = "";
		if( temp.equalsIgnoreCase( StaticParameters.MONITOR_TODAY ) ) {
			newDay = CalendarUtil.getBeforeDateBySecond( CalendarUtil.FORMAT14, 0 ).substring( 0, 16 );
			oldDay = CalendarUtil.getBeforeDateBySecond( CalendarUtil.FORMAT11, 0 ) + " 00:00";
		}
		else if( temp.equalsIgnoreCase( StaticParameters.MONITOR_YESTERDAY ) ) {
			newDay = CalendarUtil.getBeforeDateBySecond( CalendarUtil.FORMAT11, 0 ) + " 00:00";
			oldDay = CalendarUtil.getBeforeDateBySecond( CalendarUtil.FORMAT11, 3600 * 24 ) + " 00:00";
		}
		else if( temp.equalsIgnoreCase( "4" ) || temp.equalsIgnoreCase( "12" ) || temp.equalsIgnoreCase( "24" ) ) {
			//newDay = newDay.substring(0,13)+":00";
			//oldDay = CalendarUtil.getBeforeDateBySecond(CalendarUtil.FORMAT14, Integer.parseInt(temp)*60*60).substring(0,13)+":00";
			oldDay = CalendarUtil.getBeforeDateBySecond( CalendarUtil.FORMAT14, Integer.parseInt( temp ) * 60 * 60 ).substring( 0, 16 );
		}
		else {
			oldDay = CalendarUtil.getBeforeDateBySecond( CalendarUtil.FORMAT16, scope * 60 * 60 ).substring( 0, 16 );
		}

		return oldDay + "--" + newDay;
	}

	private String getTrafficExtSql( Map<String, String> reqs ) {
		String txnIds = MapUtil.getString( reqs, "txnIds" );
		if( !CmcStringUtil.isBlank( txnIds ) ) {
			return " where txntype in (" + TransCommon.arr2str( txnIds.split( "," ) ) + ")";
		}
		return null;
	}

	public static void main( String[] args ) {
		System.out.println( System.currentTimeMillis() );
		System.out.println( new Date( System.currentTimeMillis() ) );
	}
}
