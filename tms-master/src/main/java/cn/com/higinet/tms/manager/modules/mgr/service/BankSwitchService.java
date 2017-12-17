package cn.com.higinet.tms.manager.modules.mgr.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.util.MapWrap;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;

/**
 * 通道开关管理服务类
 * @author tlh
 */
@Service("bankSwitchService")
public class BankSwitchService {

	@Autowired
	@Qualifier("dynamicSimpleDao")
	private SimpleDao dynamicSimpleDao;

	/**
	 * 通道开关列表查询
	 * @param conds 所有查询条件
	 * @return 通道开关条件查询的所有信息
	 */
	public Page<Map<String, Object>> listBankSwitchPage( Map<String, String> conds ) {
		StringBuilder sql = new StringBuilder();
		sql.append(
				"select s.ID,t.TXNID,t.TAB_DESC,s.BANK_CODE,s.RISK_LEVEL,s.STATUS,s.CREATOR_ID,s.CREATE_TIME,s.UPDATOR_ID,s.UPDATE_TIME from TMS_RUN_BANK_SWITCH s, TMS_COM_TAB t WHERE s.TXNID = t.TXNID" );
		if( !StringUtil.isEmpty( conds.get( "BANK_CODE" ) ) ) {
			sql.append( " AND s.BANK_CODE=:BANK_CODE" );
		}
		if( !StringUtil.isEmpty( conds.get( "RISK_LEVEL" ) ) ) {
			sql.append( " AND s.RISK_LEVEL=:RISK_LEVEL" );
		}
		if( !StringUtil.isEmpty( conds.get( "TXNID" ) ) ) {
			sql.append( " AND s.TXNID=:TXNID" );
		}
		Page<Map<String, Object>> bankSwitchPage = dynamicSimpleDao.pageQuery( sql.toString(), conds, new Order().asc( "BANK_CODE" ) );
		for( Map<String, Object> operMap : bankSwitchPage.getList() ) {
			operMap.put( "OPER_HISTORY", "" );
		}

		return bankSwitchPage;
	}

	/**
	 * 根据id删除通道开关信息
	 * @param conds 包含需删除的id
	 */
	public void delBankSwitchList( Map<String, List<Map<String, String>>> batchMap ) {

		List<Map<String, String>> delList = batchMap.get( "del" );
		for( Map<String, String> delMap : delList ) {
			Map<String, String> conds = new HashMap<String, String>();
			conds.put( "ID", MapUtil.getString( delMap, "ID" ) );
			dynamicSimpleDao.delete( "TMS_RUN_BANK_SWITCH", conds );

			Map<String, String> reqs = new HashMap<String, String>();
			reqs.put( "BANK_SWITCH_ID", MapUtil.getString( delMap, "ID" ) );
			dynamicSimpleDao.delete( "TMS_RUN_BANK_SWITCH_HIS", reqs );
		}
	}

	/**
	 * 插入单条通道开关信息
	 * @param conds 所有需插入的信息
	 */
	public void addBankSwitch( Map<String, String> reqs, HttpServletRequest request ) {
		StringBuilder sql = new StringBuilder();
		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute( ManagerConstants.SESSION_KEY_OPERATOR );
		reqs.put( "CREATOR_ID", (String) operator.get( "OPERATOR_ID" ) );
		reqs.put( "CREATE_TIME", String.valueOf( System.currentTimeMillis() ) );
		reqs.put( "STATUS", "0" );
		sql.append(
				"insert into TMS_RUN_BANK_SWITCH (ID,TXNID,BANK_CODE,RISK_LEVEL,STATUS,CREATOR_ID,CREATE_TIME) values (:ID,:TXNID,:BANK_CODE,:RISK_LEVEL,:STATUS,:CREATOR_ID,:CREATE_TIME)" );
		dynamicSimpleDao.executeUpdate( sql.toString(), reqs );
	}

	/**
	 * 根据id查询单条通道开关信息
	 * @param  所需的id
	 * @return 单条通道开关信息
	 */
	public Map<String, Object> getBankSwitchById( String id ) {
		Map<String, Object> List = dynamicSimpleDao.retrieve( "TMS_RUN_BANK_SWITCH", MapWrap.map( "ID", id ).getMap() );
		return List;
	}

	/**
	 * 根据id更新单条通道开关信息
	 * @param conds 包含所需的id
	 */
	public void updateBankSwitchById( Map<String, String> req, HttpServletRequest request ) {
		StringBuilder sql = new StringBuilder();
		Map<String, Object> List = getBankSwitchById( req.get( "ID" ) );
		req.put( "id", Stringz.randomUUID() );
		req.put( "BANK_SWITCH_ID", (String) List.get( "ID" ) );
		req.put( "STATUS_OLD", String.valueOf( List.get( "STATUS" ) ) );
		int status_new = (Integer.parseInt( String.valueOf( List.get( "STATUS" ) ) ) + 1) % 2;
		req.put( "STATUS_NEW", String.valueOf( status_new ) );

		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute( ManagerConstants.SESSION_KEY_OPERATOR );
		req.put( "UPDATOR_ID", (String) operator.get( "LOGIN_NAME" ) );
		req.put( "UPDATE_TIME", String.valueOf( System.currentTimeMillis() ) );

		sql.append( "update TMS_RUN_BANK_SWITCH set STATUS=:STATUS_NEW where ID=:ID" );
		dynamicSimpleDao.executeUpdate( sql.toString(), req );
		sql.delete( 0, sql.length() );

		sql.append(
				"insert into TMS_RUN_BANK_SWITCH_HIS (ID,BANK_SWITCH_ID,STATUS_OLD,STATUS_NEW,UPDATOR_ID,UPDATE_TIME) values(:id,:BANK_SWITCH_ID,:STATUS_OLD,:STATUS_NEW,:UPDATOR_ID,:UPDATE_TIME)" );
		dynamicSimpleDao.executeUpdate( sql.toString(), req );
	}

	/**
	 * 根据ID查询通道开关状态修改历史
	 * @param conds 所有查询条件
	 * @return 单条通道开关状态修改历史
	 */
	public Page<Map<String, Object>> listBankSwitchHisByIdPage( Map<String, String> conds ) {
		StringBuilder sql = new StringBuilder();
		sql.append( "select ID,BANK_SWITCH_ID,STATUS_OLD,STATUS_NEW,UPDATOR_ID,UPDATE_TIME from TMS_RUN_BANK_SWITCH_HIS where BANK_SWITCH_ID=:ID" );
		Page<Map<String, Object>> bankSwitchHisPage = dynamicSimpleDao.pageQuery( sql.toString(), conds, new Order().desc( "UPDATE_TIME" ) );
		return bankSwitchHisPage;
	}
}
