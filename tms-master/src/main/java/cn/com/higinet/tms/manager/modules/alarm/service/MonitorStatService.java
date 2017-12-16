package cn.com.higinet.tms.manager.modules.alarm.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.dao.util.MapWrap;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;

/**
 * @author zhang.lei
 */

@Service("monitorStatService")
public class MonitorStatService {

	@Autowired
	@Qualifier("offlineSimpleDao")
	protected SimpleDao offlineSimpleDao;

	@Autowired
	protected SqlMap sqlMap;

	private static final String MONITOR_STAT_IPPORT = "tms-web_serverport";
	private static final Object object = new Object();

	/**
	 * 更新交易数据统计值
	 * @param status   风险人工确认状态
	 * @param traffic  交易流水信息
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateMonitorStat( Map<String, Object> traffic, String status ) {
		boolean isModelRisk = "1".equals( MapUtil.getString( traffic, DBConstant.TMS_RUN_TRAFFICDATA_ISMODELRISK ) );
		if( !isModelRisk ) return;// 非模型进行的风险评估

		String confirRisk = MapUtil.getString( traffic, DBConstant.TMS_RUN_TRAFFICDATA_CONFIRMRISK );// 风险人工确认状态
		if( status.equals( confirRisk ) ) return;

		long modelrisk_authpass_confirmrisk_number = 0;// 模型有风险认证通过人工确认有风险数
		long modelrisk_authpass_confirmnorisk_number = 0;// 模型有风险认证通过人工确认无风险数
		long modelrisk_authnotpass_confirmrisk_number = 0;// 模型有风险认证不通过人工确认有风险数
		long modelrisk_authnotpass_confirmnorisk_number = 0;// 模型有风险认证不通过人工确认无风险数
		long modelnorisk_confirmrisk_number = 0;// 模型评估无风险人工确认有风险数
		long modelnorisk_confirmnorisk_number = 0;// 模型评估无风险人工确认无风险数

		Map<String, Object> txnConds = getTxnStatCondsMap( traffic );// 组织不带地区交易统计的查询条件Map
		Map<String, Object> txnAreaConds = getTxnAreaStatCondsMap( traffic, txnConds );// 组织带地区交易统计的查询条件Map
		Map<String, Object> txnAreaStatMap = offlineSimpleDao.retrieve( "TMS_MONITOR_TXN_AREA_STAT", txnAreaConds );

		String disposal = MapUtil.getString( traffic, DBConstant.TMS_RUN_TRAFFICDATA_DISPOSAL );// 处置方式
		String correct = MapUtil.getString( traffic, DBConstant.TMS_RUN_TRAFFICDATA_ISCORRECT );// 是否认证通过
		if( CmcStringUtil.isBlank( disposal ) || "null".equals( disposal ) || "PS01".equals( disposal ) )// 无风险
		{
			if( "0".equals( status ) ) {// 人工确认无风险
				modelnorisk_confirmnorisk_number = 1;
			}
			else if( "1".equals( status ) ) {// 人工确认有风险
				modelnorisk_confirmrisk_number = 1;
			}

			if( "0".equals( confirRisk ) ) {// 已人工确认无风险的
				modelnorisk_confirmnorisk_number = -1;
			}
			else if( "1".equals( confirRisk ) ) {// 已人工确认有风险的
				modelnorisk_confirmrisk_number = -1;
			}
		}
		else {// 有风险
			if( CmcStringUtil.isBlank( correct ) || "null".equals( correct ) ) return;
			if( "1".equals( correct ) )// 认证通过的
			{
				if( "0".equals( status ) ) {// 人工确认无风险
					modelrisk_authpass_confirmnorisk_number = 1;
				}
				else if( "1".equals( status ) ) {// 人工确认有风险
					modelrisk_authpass_confirmrisk_number = 1;
				}

				if( "0".equals( confirRisk ) ) {// 已人工确认无风险的
					modelrisk_authpass_confirmnorisk_number = -1;
				}
				else if( "1".equals( confirRisk ) ) {// 已人工确认有风险的
					modelrisk_authpass_confirmrisk_number = -1;
				}
			}
			else if( "0".equals( correct ) ) {// 认证不通过的
				if( "0".equals( status ) ) {// 人工确认无风险
					modelrisk_authnotpass_confirmnorisk_number = 1;
				}
				else if( "1".equals( status ) ) {// 人工确认有风险
					modelrisk_authnotpass_confirmrisk_number = 1;
				}

				if( "0".equals( confirRisk ) ) {// 已人工确认无风险的
					modelrisk_authnotpass_confirmnorisk_number = -1;
				}
				else if( "1".equals( confirRisk ) ) {// 已人工确认有风险的
					modelrisk_authnotpass_confirmrisk_number = -1;
				}
			}
		}
		Map<String, Object> newStatMap = new HashMap<String, Object>();
		if( MapUtil.isEmpty( txnAreaStatMap ) ) {
			newStatMap.put( "MODTIME", System.currentTimeMillis() );
			newStatMap.put( "MR_AP_CR_NUMBER", modelrisk_authpass_confirmrisk_number );
			newStatMap.put( "MR_AP_CRNOT_NUMBER", modelrisk_authpass_confirmnorisk_number );
			newStatMap.put( "MR_APNOT_CR_NUMBER", modelrisk_authnotpass_confirmrisk_number );
			newStatMap.put( "MR_APNOT_CRNOT_NUMBER", modelrisk_authnotpass_confirmnorisk_number );
			newStatMap.put( "MRNOT_CR_NUMBER", modelnorisk_confirmrisk_number );
			newStatMap.put( "MRNOT_CRNOT_NUMBER", modelnorisk_confirmnorisk_number );
		}
		else {
			txnAreaStatMap.put( "MODTIME", System.currentTimeMillis() );
		}

		if( MapUtil.isEmpty( txnAreaStatMap ) ) {
			txnAreaConds.putAll( newStatMap );
			offlineSimpleDao.create( "TMS_MONITOR_TXN_AREA_STAT", txnAreaConds );
		}
		else {
			monitorStatReCalculate( txnAreaStatMap, modelrisk_authpass_confirmrisk_number, modelrisk_authpass_confirmnorisk_number, modelrisk_authnotpass_confirmrisk_number,
					modelrisk_authnotpass_confirmnorisk_number, modelnorisk_confirmrisk_number, modelnorisk_confirmnorisk_number );
			offlineSimpleDao.update( "TMS_MONITOR_TXN_AREA_STAT", txnAreaStatMap, txnAreaConds );
		}
	}

	/**
	 * 重新计算统计值
	 * 
	 * @param statMap
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	private void monitorStatReCalculate( Map<String, Object> statMap, long arg0, long arg1, long arg2, long arg3, long arg4, long arg5 ) {
		statMap.put( "MR_AP_CR_NUMBER", MapUtil.getLong( statMap, "MR_AP_CR_NUMBER" ) + arg0 );
		statMap.put( "MR_AP_CRNOT_NUMBER", MapUtil.getLong( statMap, "MR_AP_CRNOT_NUMBER" ) + arg1 );
		statMap.put( "MR_APNOT_CR_NUMBER", MapUtil.getLong( statMap, "MR_APNOT_CR_NUMBER" ) + arg2 );
		statMap.put( "MR_APNOT_CRNOT_NUMBER", MapUtil.getLong( statMap, "MR_APNOT_CRNOT_NUMBER" ) + arg3 );
		statMap.put( "MRNOT_CR_NUMBER", MapUtil.getLong( statMap, "MRNOT_CR_NUMBER" ) + arg4 );
		statMap.put( "MRNOT_CRNOT_NUMBER", MapUtil.getLong( statMap, "MRNOT_CRNOT_NUMBER" ) + arg5 );
	}

	private Map<String, Object> getTxnStatCondsMap( Map<String, Object> traffic ) {
		Map<String, Object> conds = new HashMap<String, Object>();
		long stattime = getMonitorStatTime( MapUtil.getLong( traffic, DBConstant.TMS_RUN_TRAFFICDATA_TXNTIME ) );// 统计时间点
		conds.put( "IPPORT", MONITOR_STAT_IPPORT );
		conds.put( "CHANNELID", MapUtil.getString( traffic, DBConstant.TMS_RUN_TRAFFICDATA_CHANCODE ) );
		conds.put( "TXNID", MapUtil.getString( traffic, DBConstant.TMS_RUN_TRAFFICDATA_TXNID ) );
		conds.put( "TIME", stattime );
		return conds;
	}

	private Map<String, Object> getTxnAreaStatCondsMap( Map<String, Object> traffic, Map<String, Object> conds ) {
		Map<String, Object> areaConds = new HashMap<String, Object>( conds );
		areaConds.put( "COUNTRYCODE", getEmptyValue( traffic, DBConstant.TMS_RUN_TRAFFICDATA_COUNTRYCODE ) );
		areaConds.put( "REGIONCODE", getEmptyValue( traffic, DBConstant.TMS_RUN_TRAFFICDATA_REGIONCODE ) );
		areaConds.put( "CITYCODE", getEmptyValue( traffic, DBConstant.TMS_RUN_TRAFFICDATA_CITYCODE ) );
		return areaConds;
	}

	private long getMonitorStatTime( long txntime ) {
		String sql = "select s.TIME from TMS_MONITOR_TXN_AREA_STAT s " + "where s.TIME <= " + txntime + " order by s.TIME desc";
		List<Map<String, Object>> list = offlineSimpleDao.queryForList( sql );
		if( list == null || list.isEmpty() ) return txntime;
		return Long.valueOf( String.valueOf( list.get( 0 ).get( "TIME" ) ) );
	}

	private String getEmptyValue( Map<String, Object> map, String key ) {
		String value = MapUtils.getString( map, key );
		if( CmcStringUtil.isBlank( value ) ) {
			value = "-1";
		}
		return value;
	}

	/**
	 * 更新工作量信息
	 * @param operId		处理人员ID
	 * @param AssignTime	分派时间
	 * @param isAutoAssign  是否自动分派
	 * @param isProcess		是否处理
	 */

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateAlarmProcessOperatorStat( String operId, long assignTime, boolean isAutoAssign, boolean isProcess ) {
		String sql = "update TMS_MGR_ALARM_OPERATOR_STAT set %s where OPERATOR_ID = ? and ASSIGN_TIME = ? and SRC_STATDATA = ?";
		String tmp = "";
		if( isProcess ) {
			tmp = "PROCESS_NUMBER=PROCESS_NUMBER+1, UNPROCESS_NUMBER=UNPROCESS_NUMBER-1";
		}
		else {
			tmp = "PROCESS_NUMBER=PROCESS_NUMBER-1, UNPROCESS_NUMBER=UNPROCESS_NUMBER+1";
		}
		String _assignTime = CalendarUtil.parseTimeMillisToDateTime( assignTime, CalendarUtil.FORMAT19.toPattern() );
		offlineSimpleDao.executeUpdate( String.format( sql, tmp ), operId, _assignTime, (isAutoAssign ? "0" : "1") );
	}

	/**
	 * 报警事件分派，更新工作量统计数据
	 * @param oldOperId			原处理人员
	 * @param oldAssignTime		原分派时间
	 * @param oldStatus			原报警单处理状态
	 * @param newOperID			新处理人员
	 * @param newAssignTime		新分派时间
	 */

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateAlarmAssignOperatorStat( String oldOperId, long oldAssignTime, boolean oldStatus, String newOperID, long newAssignTime ) {
		String insert = "insert into TMS_MGR_ALARM_OPERATOR_STAT(OPERATOR_ID,ASSIGN_TIME,SRC_STATDATA,ASSIGN_NUMBER,"
				+ "PROCESS_NUMBER,UNPROCESS_NUMBER,ASSIGN_INFO,ASSIGN_INFO2,ASSIGN_LOB)values(?,?,?,?,?,?,?,?,?)";
		String update = "update TMS_MGR_ALARM_OPERATOR_STAT set %s where OPERATOR_ID = ? and ASSIGN_TIME = ? and SRC_STATDATA = ?";
		if( oldStatus ) {
			// 已进行过分派
			String _oldAssignTime = CalendarUtil.parseTimeMillisToDateTime( oldAssignTime, CalendarUtil.FORMAT19.toPattern() );
			Map<String, Object> oldOper = offlineSimpleDao.retrieve( "TMS_MGR_ALARM_OPERATOR_STAT",
					MapWrap.map( "OPERATOR_ID", oldOperId ).put( "ASSIGN_TIME", _oldAssignTime ).put( "SRC_STATDATA", "1" ).getMap() );
			if( MapUtil.isEmpty( oldOper ) ) {
				offlineSimpleDao.executeUpdate( insert, oldOperId, _oldAssignTime, "1", -1, 0, -1, null, null, null );
			}
			else {
				String tmp = "ASSIGN_NUMBER=ASSIGN_NUMBER-1, UNPROCESS_NUMBER=UNPROCESS_NUMBER-1";
				offlineSimpleDao.executeUpdate( String.format( update, tmp ), oldOperId, _oldAssignTime, "1" );
			}
		}
		//查询新处理人员是否存在记录
		String _newAssignTime = CalendarUtil.parseTimeMillisToDateTime( newAssignTime, CalendarUtil.FORMAT19.toPattern() );
		Map<String, Object> newOper = offlineSimpleDao.retrieve( "TMS_MGR_ALARM_OPERATOR_STAT",
				MapWrap.map( "OPERATOR_ID", newOperID ).put( "ASSIGN_TIME", _newAssignTime ).put( "SRC_STATDATA", "1" ).getMap() );
		if( MapUtil.isEmpty( newOper ) ) {
			offlineSimpleDao.executeUpdate( insert, newOperID, _newAssignTime, "1", 1, 0, 1, null, null, null );
		}
		else {
			String tmp = "ASSIGN_NUMBER=ASSIGN_NUMBER+1, UNPROCESS_NUMBER=UNPROCESS_NUMBER+1";
			offlineSimpleDao.executeUpdate( String.format( update, tmp ), newOperID, _newAssignTime, "1" );
		}
	}

	/**
	 * 更新实时统计中欺诈类型统计数据
	 * @param txnCode
	 */

	@Transactional(propagation = Propagation.REQUIRED)
	public void modMonitorFraudTypeStat( Map<String, Object> map ) {
		synchronized (object) {
			long txnTime = MapUtil.getLong( map, "TXNTIME" );
			String modelId = MapUtil.getString( map, "MODELID" );
			Date date = new Date( txnTime );
			SimpleDateFormat format = new SimpleDateFormat( "yyyyMMddHH" );
			String dateTime = format.format( date );
			txnTime = CalendarUtil.parseStringToDate( dateTime, format ).getTime();
			if( CmcStringUtil.isBlank( modelId ) ) {
				map.put( "MODELID", "null" );
			}
			map.put( "TIME", txnTime );
			map = getTxnAreaStatCondsMap( map, map );// 国家、省份、城市代码为NULL时，赋值为-1
			String readSql = "select count(*) STAT_SIZE from TMS_MONITOR_TXN_FRAUD_STAT t where t.COUNTRYCODE = :COUNTRYCODE "
					+ "and t.REGIONCODE = :REGIONCODE and t.CITYCODE = :CITYCODE and t.CHANNELID = :CHANCODE "
					+ "and t.TXNID = :TXNTYPE and t.TIME = :TIME and t.MODEL_ID = :MODELID and t.DISPOSAL = :DISPOSAL " + "and t.FRAUD_TYPE = :FRAUD_TYPE";
			List<Map<String, Object>> list = offlineSimpleDao.queryForList( readSql, map );
			Map<String, Object> statMap = list.get( 0 );
			long statSize = MapUtil.getLong( statMap, "STAT_SIZE" );
			String sql = null;
			if( statSize > 0 ) {
				sql = "update TMS_MONITOR_TXN_FRAUD_STAT t set t.MODTIME = :MODTIME, t.FRAUD_NUMBER = t.FRAUD_NUMBER + 1 "
						+ "where t.COUNTRYCODE = :COUNTRYCODE and t.REGIONCODE = :REGIONCODE and t.CITYCODE = :CITYCODE "
						+ "and t.CHANNELID = :CHANCODE and t.TXNID = :TXNTYPE and t.TIME = :TIME and t.MODEL_ID = :MODELID "
						+ "and t.DISPOSAL = :DISPOSAL and t.FRAUD_TYPE = :FRAUD_TYPE";
			}
			else {
				sql = "insert into TMS_MONITOR_TXN_FRAUD_STAT(COUNTRYCODE, REGIONCODE, CITYCODE, CHANNELID, TXNID, "
						+ "TIME, MODTIME, MODEL_ID, DISPOSAL, FRAUD_TYPE, FRAUD_NUMBER)values(:COUNTRYCODE, :REGIONCODE, "
						+ ":CITYCODE, :CHANCODE, :TXNTYPE, :TIME, :MODTIME, :MODELID, :DISPOSAL, :FRAUD_TYPE, 1)";
			}
			map.put( "MODTIME", System.currentTimeMillis() );
			offlineSimpleDao.executeUpdate( sql, map );
		}
	}
}