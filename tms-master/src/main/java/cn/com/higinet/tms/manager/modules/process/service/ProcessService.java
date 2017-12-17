package cn.com.higinet.tms.manager.modules.process.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.modules.common.CommonCheckService;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_PROCESS;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms.manager.modules.common.SequenceService;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.tran.service.TransDefService;

@Service("processService")
public class ProcessService   {

	@Autowired
	private CommonCheckService commonCheckService;
	
	@Autowired
	@Qualifier("dynamicSimpleDao")
	private SimpleDao dynamicSimpleDao;
	
	@Autowired
	private TransDefService transDefService;
	
	@Autowired
	@Qualifier("tmsSqlMap")
	private SqlMap tmsSqlMap;
	
	@Autowired
	private SequenceService sequenceService;

	/**
	 * 查询某交易的所有处置
	 * @param txnid
	 * @return
	 */
	public List<Map<String, Object>> listAllProcessInOneTxn( String txnid ) {

		StringBuffer sql = new StringBuffer( "select pcs.*, tab.tab_desc " );
		sql.append( " from " );
		sql.append( TMS_COM_PROCESS.TABLE_NAME ).append( " pcs," );
		sql.append( TMS_COM_TAB.TABLE_NAME ).append( " tab" );
		sql.append( " where " );
		sql.append( "pcs." ).append( TMS_COM_PROCESS.PS_TXN ).append( "=tab." ).append( TMS_COM_TAB.TAB_NAME ).append( " AND " );
		sql.append( "pcs." ).append( TMS_COM_PROCESS.PS_TXN );
		sql.append( "=? order by " ).append( TMS_COM_PROCESS.PS_ORDER );
		return dynamicSimpleDao.queryForList( sql.toString(), txnid );
	}

	/**
	 * 保存某交易的所有处置
	 * @param List
	 * @return
	 */
	@Transactional
	public Map saveProcess( Map formMap ) {

		List<Map<String, Object>> delList = MapUtil.getList( formMap, "del" );
		List<Map<String, Object>> modList = MapUtil.getList( formMap, "mod" );
		List<Map<String, Object>> addList = MapUtil.getList( formMap, "add" );
		List<Map<String, Object>> enableList = MapUtil.getList( formMap, "valid-y" );
		List<Map<String, Object>> disbaleList = MapUtil.getList( formMap, "valid-n" );

		Map m = new HashMap();

		if( delList != null && delList.size() > 0 ) {
			m = deleteRecord( delList );
		}
		if( addList != null && addList.size() > 0 ) {
			m = addRecord( addList );
		}
		if( modList != null && modList.size() > 0 ) {
			m = updateRecord( modList );
		}
		if( enableList != null && enableList.size() > 0 ) {
			m = updateRecord( enableList );
		}
		if( disbaleList != null && disbaleList.size() > 0 ) {
			m = updateRecord( disbaleList );
		}

		return m;
	}

	/*
	 * 提交新数据
	 */
	private Map addRecord( List add ) {

		Map m = (Map) add.get( 0 );

		String cond = MapUtil.getString( m, "PS_COND" );
		String desc = MapUtil.getString( m, "PS_NAME" );
		String txnid = MapUtil.getString( m, "PS_TXN" );

		commonCheckService.checkCond( cond, desc, txnid );

		transDefService.checkSingleParentAndAllSubSame( null, MapUtil.getString( m, "PS_NAME" ), MapUtil.getString( m, "PS_TXN" ), false, "ps", true );
		m.put( TMS_COM_PROCESS.PS_ID, Long.parseLong( sequenceService.getSequenceIdToString( DBConstant.SEQ_TMS_COM_PROCESS_ID ) ) );
		m.put( TMS_COM_PROCESS.PS_ORDER, MapUtil.getLong( m, TMS_COM_PROCESS.PS_ORDER ) );
		m.put( TMS_COM_PROCESS.PS_ENABLE, MapUtil.getLong( m, TMS_COM_PROCESS.PS_ENABLE ) );
		dynamicSimpleDao.batchUpdate( buildAddSql(), add );
		return (Map) add.get( 0 );
	}

	/*
	 * 修改数据
	 */
	private Map updateRecord( List update ) {

		if( update != null && update.size() == 1 ) {
			Map m = (Map) update.get( 0 );

			String cond = MapUtil.getString( m, "PS_COND" );
			String desc = MapUtil.getString( m, "PS_NAME" );
			String txnid = MapUtil.getString( m, "PS_TXN" );

			commonCheckService.checkCond( cond, desc, txnid );

			m.put( TMS_COM_PROCESS.PS_ID, MapUtil.getLong( m, TMS_COM_PROCESS.PS_ID ) );
			m.put( TMS_COM_PROCESS.PS_ORDER, MapUtil.getLong( m, TMS_COM_PROCESS.PS_ORDER ) );
			m.put( TMS_COM_PROCESS.PS_ENABLE, MapUtil.getLong( m, TMS_COM_PROCESS.PS_ENABLE ) );

			transDefService.checkSingleParentAndAllSubSame( MapUtil.getString( m, "PS_ID" ), MapUtil.getString( m, "PS_NAME" ), MapUtil.getString( m, "PS_TXN" ), false, "ps", false );
		}

		dynamicSimpleDao.batchUpdate( buildUpdateSql(), update );
		return (Map) update.get( 0 );
	}

	/*
	 * 删除数据
	 */
	private Map deleteRecord( List delete ) {
		dynamicSimpleDao.executeUpdate( buildDeleteSql( delete ) );
		return null;
	}

	/*
	 * 组装添加switch的sql语句
	 */
	private String buildAddSql() {

		StringBuffer sb = new StringBuffer();

		sb.append( "INSERT INTO " );
		sb.append( TMS_COM_PROCESS.TABLE_NAME );
		sb.append( "(PS_ID, PS_TXN, PS_NAME, PS_COND, PS_COND_IN, PS_VALUE, PS_ORDER, PS_ENABLE) " );
		sb.append( "VALUES(:PS_ID,:PS_TXN,:PS_NAME,:PS_COND,:PS_COND_IN,:PS_VALUE,:PS_ORDER,:PS_ENABLE)" );

		return sb.toString();
	}

	/*
	 * 组装保存switch的sql语句
	 */
	private String buildUpdateSql() {

		StringBuffer sb = new StringBuffer();

		sb.append( "UPDATE " );
		sb.append( TMS_COM_PROCESS.TABLE_NAME );
		sb.append( " SET PS_TXN=:PS_TXN, PS_NAME=:PS_NAME, PS_COND=:PS_COND, PS_COND_IN=:PS_COND_IN, PS_VALUE=:PS_VALUE, PS_ORDER=:PS_ORDER, PS_ENABLE=:PS_ENABLE " );
		sb.append( " WHERE " );
		sb.append( "PS_ID=:PS_ID" );

		return sb.toString();
	}

	/*
	 * 组装删除switch的sql语句
	 */
	private String buildDeleteSql( List delete ) {

		StringBuffer id_sb = new StringBuffer( "" );
		StringBuffer dlteSqlSb = new StringBuffer();
		dlteSqlSb.append( "DELETE FROM " );
		dlteSqlSb.append( TMS_COM_PROCESS.TABLE_NAME ).append( " " );
		dlteSqlSb.append( "WHERE " );
		dlteSqlSb.append( TMS_COM_PROCESS.PS_ID ).append( " in (" );
		for( int i = 0; i < delete.size(); i++ ) {
			Map dlt = (Map) delete.get( i );
			String id = MapUtil.getString( dlt, TMS_COM_PROCESS.PS_ID );
			id_sb.append( id ).append( "," );
		}
		id_sb.append( ")" );
		id_sb.setCharAt( id_sb.lastIndexOf( "," ), ' ' );
		dlteSqlSb.append( id_sb );

		return dlteSqlSb.toString();
	}

	/*
	 * 1. 当有新数据时,插入主键
	 * 2. format date
	 */
	private void clearAndBuild( List formMap, List add, List update, List delete ) {

		for( int i = 0; i < formMap.size(); i++ ) {

			Map oneCol = (Map) formMap.get( i );
			String action = MapUtil.getString( oneCol, "ACTION" );
			String ds = MapUtil.getString( oneCol, "DS" );

			if( action.equals( "4" ) || // 无变化
					(action.equals( "3" ) && ds.equals( "1" )) ) { // 新建删除
				continue;
			}

			String id = MapUtil.getString( oneCol, TMS_COM_PROCESS.PS_ID );
			if( id.startsWith( "NEW" ) ) {
				String newId = sequenceService.getSequenceIdToString( DBConstant.SEQ_TMS_COM_PROCESS_ID );
				oneCol.put( TMS_COM_PROCESS.PS_ID, Long.parseLong( newId ) );
			}

			if( action.equals( "1" ) ) {
				add.add( oneCol );
			}
			else if( action.equals( "2" ) ) {
				if( ds.equals( "1" ) ) {
					add.add( oneCol );
				}
				else update.add( oneCol );
			}
			else {
				delete.add( oneCol );
			}
		}
	}

	/*
	 * 是否需要保存
	 * 如果form的list为空,表示已经把该交易的全部switch删除了
	 */
	private boolean needSaveNew( List formMap ) {
		return formMap != null && formMap.size() > 0;
	}

}
