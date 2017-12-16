package cn.com.higinet.tms.manager.modules.mgr.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.util.MapWrap;
import cn.com.higinet.tms.manager.modules.common.SequenceService;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;

/**
 * 自动派发服务类
 * @author tlh
 */
@Service("assignAutoService")
public class AssignAutoService {

	@Autowired
	@Qualifier("offlineSimpleDao")
	private SimpleDao offlineSimpleDao;

	@Autowired
	private SequenceService sequenceService;

	@Autowired
	@Qualifier("offlineDataSource")
	private DataSource offlineDataSource;

	public Page<Map<String, Object>> dealerInformationPage( Map<String, String> reqs ) {
		String tab_name = reqs.get( "TAB_NAME" );
		reqs.put( "AS_STRATEGYID", tab_name );
		StringBuffer sql = new StringBuffer();
		sql.append(
				"select o.login_name,o.REAL_NAME,o.operator_id,a.as_id,a.as_strategyid,a.starttime,a.endtime,a.is_enable,a.disposals from cmc_operator o,tms_mgr_alarm_autoassign a where o.operator_id=a.dealerid and a.as_strategyid=:AS_STRATEGYID" );
		Page<Map<String, Object>> dealerInfoPage = offlineSimpleDao.pageQuery( sql.toString(), reqs, new Order().desc( "login_name" ) );
		for( Map<String, Object> dealerMap : dealerInfoPage.getList() ) {
			String disposals = (String) dealerMap.get( "DISPOSALS" );
			if( disposals != null ) {
				String split[] = disposals.split( "," );
				StringBuffer strs = new StringBuffer();
				for( int i = 0; i < split.length; i++ ) {
					Map<String, Object> List = offlineSimpleDao.retrieve( "TMS_COM_DISPOSAL", MapWrap.map( "DP_CODE", split[i] ).getMap() );
					strs.append( List.get( "DP_NAME" ) + "," );
				}
				dealerMap.put( "DISPOSALS_NAME", strs.deleteCharAt( strs.length() - 1 ) );
			}
			dealerMap.put( "OPERID", dealerMap.get( "REAL_NAME" ) + "(" + dealerMap.get( "login_name" ) + ")" );

		}
		return dealerInfoPage;
	}

	public Page<Map<String, Object>> eventTypePage( Map<String, String> reqs ) {
		StringBuffer sql = new StringBuffer();
		sql.append( "select DP_CODE,DP_NAME from TMS_COM_DISPOSAL" );
		Page<Map<String, Object>> eventTypePage = offlineSimpleDao.pageQuery( sql.toString(), reqs, new Order().asc( "dp_order" ) );
		return eventTypePage;
	}

	public void updateUserAssign( Map<String, String> reqs ) {
		StringBuffer sql = new StringBuffer();
		sql.append( "update TMS_MGR_ALARM_AUTOASSIGN a set a.starttime=:STARTTIME, a.endtime=:ENDTIME ,a.is_enable=:IS_ENABLE,a.DISPOSALS=:DISPOSALS where a.as_id=:AS_ID" );
		offlineSimpleDao.executeUpdate( sql.toString(), reqs );
	}

	public void delUserAssign( Map<String, List<Map<String, String>>> batchMap ) {
		List<Map<String, String>> delList = batchMap.get( "del" );
		for( Map<String, String> delMap : delList ) {
			Map<String, String> conds = new HashMap<String, String>();
			if( MapUtil.getString( delMap, "STARTTIME" ) != "" ) {
				conds.put( "AS_STRATEGYID", MapUtil.getString( delMap, "AS_STRATEGYID" ) );
				conds.put( "DEALERID", MapUtil.getString( delMap, "OPERATOR_ID" ) );
				offlineSimpleDao.delete( "TMS_MGR_ALARM_AUTOASSIGN", conds );
			}
		}
	}

	public Page<Map<String, Object>> getAllUserAssign( Map<String, String> reqs ) {
		StringBuffer sql = new StringBuffer();
		sql.append(
				"select o.operator_id,o.login_name,o.REAL_NAME from CMC_OPERATOR o,CMC_OPERATOR_ROLE_REL r where o.operator_id=r.operator_id and o.operator_id not in (select a.dealerid from TMS_MGR_ALARM_AUTOASSIGN a where a.as_strategyid=:TAB_NAME)" );
		Page<Map<String, Object>> userAssign = offlineSimpleDao.pageQuery( sql.toString(), reqs, new Order().desc( "login_name" ) );
		return userAssign;
	}

	public void addUserAssign( Map<String, String> reqs ) {
		StringBuffer sql = new StringBuffer();
		String as_id = sequenceService.getSequenceIdToString( "SEQ_TMS_ASSIGN_ID", offlineDataSource );
		reqs.put( "AS_ID", as_id );
		sql.append(
				"insert into TMS_MGR_ALARM_AUTOASSIGN (AS_ID,AS_STRATEGYID,DEALERID,STARTTIME,ENDTIME,IS_ENABLE,DISPOSALS) values (:AS_ID,:AS_STRATEGYID,:OPERID,:STARTTIME,:ENDTIME,:IS_ENABLE,:DISPOSALS) " );
		offlineSimpleDao.executeUpdate( sql.toString(), reqs );
	}

	public void modStatusUserAssign( Map<String, String> reqs ) {
		StringBuffer sql = new StringBuffer();
		Map<String, Object> List = offlineSimpleDao.retrieve( "TMS_MGR_ALARM_AUTOASSIGN", MapWrap.map( "AS_ID", reqs.get( "AS_ID" ) ).getMap() );
		int is_enable = (Integer.parseInt( String.valueOf( List.get( "IS_ENABLE" ) ) ) + 1) % 2;
		reqs.put( "IS_ENABLE", String.valueOf( is_enable ) );
		sql.append( "update TMS_MGR_ALARM_AUTOASSIGN a set a.IS_ENABLE=:IS_ENABLE where a.as_id=:AS_ID" );
		offlineSimpleDao.executeUpdate( sql.toString(), reqs );
	}

	public void modAutoAssignSwitch( Map<String, String> reqs ) {
		StringBuffer sql = new StringBuffer();
		reqs.put( "SYSPARAMNAME", "autoAssign_switch" );
		sql.append( "update TMS_MGR_SYSPARAM s set s.STARTVALUE=:STARTVALUE where s.SYSPARAMNAME=:SYSPARAMNAME" );
		offlineSimpleDao.executeUpdate( sql.toString(), reqs );
	}

	public String getAssignSwitchStatus( Map<String, String> reqs ) {
		StringBuffer sql = new StringBuffer();
		reqs.put( "SYSPARAMNAME", "autoAssign_switch" );
		sql.append( "select s.startvalue from TMS_MGR_SYSPARAM s where s.SYSPARAMNAME=:SYSPARAMNAME" );
		Page<Map<String, Object>> startvalue = offlineSimpleDao.pageQuery( sql.toString(), reqs, new Order().desc( "startvalue" ) );
		return (String) startvalue.getList().get( 0 ).get( "STARTVALUE" );
	}

	public Page<Map<String, Object>> getAllPaySuspendList( Map<String, String> reqs ) {
		StringBuffer sql = new StringBuffer();
		sql.append(
				"select u.mobile,u.cusname,u.userid,p.ps_time from TMS_MGR_ALARM_PROCESS p,TMS_RUN_TRAFFICDATA t,TMS_RUN_USER u where p.txn_code=t.txncode and t.userid=u.userid" );
		if( !StringUtil.isEmpty( reqs.get( "MOBILE" ) ) ) sql.append( " and u.MOBILE like '%'||:MOBILE||'%'" );
		if( !StringUtil.isEmpty( reqs.get( "CUSNAME" ) ) ) sql.append( " and u.CUSNAME like '%'||:CUSNAME||'%'" );
		if( !StringUtil.isEmpty( reqs.get( "USERID" ) ) ) sql.append( " and u.USERID like '%'||:USERID||'%'" );
		if( !StringUtil.isEmpty( reqs.get( "startTime" ) ) && !StringUtil.isEmpty( reqs.get( "endTime" ) ) ) {
			SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
			Date startTime = null, endTime = null;
			try {
				startTime = sdf.parse( reqs.get( "startTime" ) );
				endTime = sdf.parse( reqs.get( "endTime" ) );
			}
			catch( ParseException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			reqs.put( "startTime", String.valueOf( startTime.getTime() ) );
			reqs.put( "endTime", String.valueOf( endTime.getTime() ) );
			sql.append( " and p.PS_TIME>=:startTime and p.PS_TIME<= :endTime" );
		}
		Page<Map<String, Object>> PaySuspendPage = offlineSimpleDao.pageQuery( sql.toString(), reqs, new Order().asc( "ps_time" ) );
		for( Map<String, Object> payMap : PaySuspendPage.getList() ) {
			payMap.put( "SUSPEND", "0" );
			payMap.put( "BLOCK", "0" );
			payMap.put( "BLACKLIST", "0" );
		}
		return PaySuspendPage;
	}
}
