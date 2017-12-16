/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.ac.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.util.MapWrap;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.SequenceService;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms35.comm.web_tool;
import cn.com.higinet.tms35.core.cache.cache_init;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

/**
 * 功能/模块:
 * @author 张立群
 * @version 1.0  May 31, 2013
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
@Service("actionService35")
public class AcService {

	private static final Logger log = LoggerFactory.getLogger( AcService.class );

	@Autowired
	@Qualifier("tmsSimpleDao")
	private SimpleDao tmsSimpleDao;
	@Autowired
	@Qualifier("dynamicDataSource")
	private DataSource dynamicDataSource;
	@Autowired
	private SequenceService sequenceService;

	/**
	* 方法描述:查询动作列表
	* @param reqs
	* @return
	*/
	public List<Map<String, Object>> listAction( Map<String, Object> reqs ) {
		return tmsSimpleDao.queryForList( "SELECT a.* FROM TMS_COM_RULE_ACTION a WHERE RULE_ID=? ORDER BY ac_id DESC ", MapUtil.getString( reqs, "rule_id" ) );
	}

	/**
	* 方法描述:
	* @param formList
	*/
	@Transactional
	public Map<String, Object> saveAc( Map<String, List<Map<String, Object>>> formList ) {

		List<Map<String, Object>> delList = MapUtil.getList( formList, "del" );
		List<Map<String, Object>> modList = MapUtil.getList( formList, "mod" );
		List<Map<String, Object>> addList = MapUtil.getList( formList, "add" );
		List<Map<String, Object>> copyList = MapUtil.getList( formList, "copy" );
		List<Map<String, Object>> validYList = MapUtil.getList( formList, "valid-y" );
		List<Map<String, Object>> validNList = MapUtil.getList( formList, "valid-n" );

		Map<String, Object> rmap = new HashMap<String, Object>();

		// 删除
		if( delList != null && delList.size() > 0 ) {
			for( Map<String, Object> map : delList ) {
				deleteAc( map );
				rmap = map;
			}
		}
		// 复制
		if( copyList != null && copyList.size() > 0 ) {
			for( Map<String, Object> map : copyList ) {

				// 校验规则动作不能重复
				checkDuplicateAcName( map );
				// 复制动作
				rmap = createAction( map );

			}
		}

		// 新增
		if( addList != null && addList.size() > 0 ) {
			// 初始化缓存
			cacheInit();

			for( Map<String, Object> map : addList ) {
				checkCond( map );// 校验条件有效性

				// 校验动作名称不能重复
				checkDuplicateAcName( map );

				rmap = createAction( map );
			}
		}

		// 修改
		if( modList != null && modList.size() > 0 ) {
			cacheInit();

			for( Map<String, Object> map : modList ) {
				checkCond( map );// 校验条件有效性

				// 校验动作名称不能重复
				checkDuplicateAcName( map );

				rmap = modAction( map );
			}
		}

		// 有效性-启用
		if( validYList != null && validYList.size() > 0 ) {
			cacheInit();
			for( Map<String, Object> map : validYList ) {
				// 校验条件是否有效
				checkCond( map );

				rmap = modAction( map );
			}
		}

		// 有效性-停用
		if( validNList != null && validNList.size() > 0 ) {
			for( Map<String, Object> map : validNList ) {
				rmap = modAction( map );
			}
		}

		return rmap;
	}

	/* 动作修改
	 * (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.action.controller.service.ActionService#modAction(java.util.Map)
	 */
	private Map<String, Object> modAction( Map<String, ?> reqs ) {
		// 动作修改条件
		Map<String, Object> condData = new HashMap<String, Object>();
		condData.put( DBConstant.TMS_COM_RULE_ACTION_AC_ID, MapUtil.getLong( reqs, DBConstant.TMS_COM_RULE_ACTION_AC_ID ) );

		// 动作修改数据
		Map<String, Object> acData = new HashMap<String, Object>();
		acData.put( DBConstant.TMS_COM_RULE_ACTION_AC_ID, MapUtil.getLong( reqs, DBConstant.TMS_COM_RULE_ACTION_AC_ID ) );
		acData.put( DBConstant.TMS_COM_RULE_ACTION_RULE_ID, MapUtil.getLong( reqs, DBConstant.TMS_COM_RULE_ACTION_RULE_ID ) );
		acData.put( DBConstant.TMS_COM_RULE_ACTION_AC_DESC, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_ACTION_AC_DESC ) );
		acData.put( DBConstant.TMS_COM_RULE_ACTION_AC_COND, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_ACTION_AC_COND ) );
		acData.put( DBConstant.TMS_COM_RULE_ACTION_AC_COND_IN, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_ACTION_AC_COND_IN ) );
		acData.put( DBConstant.TMS_COM_RULE_ACTION_AC_EXPR, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_ACTION_AC_EXPR ) );
		acData.put( DBConstant.TMS_COM_RULE_ACTION_AC_EXPR_IN, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_ACTION_AC_EXPR_IN ) );
		acData.put( DBConstant.TMS_COM_RULE_ACTION_AC_ENABLE, MapUtil.getLong( reqs, DBConstant.TMS_COM_RULE_ACTION_AC_ENABLE ) );

		// 修改
		tmsSimpleDao.update( "TMS_COM_RULE_ACTION", acData, condData );
		return acData;
	}

	/* 动作插入数据库
	 * (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.action.controller.service.ActionService#createAction(java.util.Map)
	 */
	public Map<String, Object> createAction( Map<String, Object> reqs ) {
		Long sequenceId = Long.valueOf( sequenceService.getSequenceIdToString( DBConstant.SEQ_TMS_COM_RULE_ACTION_ID ) );

		Map<String, Object> actionData = new HashMap<String, Object>();
		actionData.put( DBConstant.TMS_COM_RULE_ACTION_AC_ID, sequenceId );
		actionData.put( DBConstant.TMS_COM_RULE_ACTION_RULE_ID, MapUtil.getLong( reqs, DBConstant.TMS_COM_RULE_ACTION_RULE_ID ) );
		actionData.put( DBConstant.TMS_COM_RULE_ACTION_AC_DESC, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_ACTION_AC_DESC ) );
		actionData.put( DBConstant.TMS_COM_RULE_ACTION_AC_COND, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_ACTION_AC_COND ) );
		actionData.put( DBConstant.TMS_COM_RULE_ACTION_AC_COND_IN, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_ACTION_AC_COND_IN ) );
		actionData.put( DBConstant.TMS_COM_RULE_ACTION_AC_EXPR, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_ACTION_AC_EXPR ) );
		actionData.put( DBConstant.TMS_COM_RULE_ACTION_AC_EXPR_IN, MapUtil.getString( reqs, DBConstant.TMS_COM_RULE_ACTION_AC_EXPR_IN ) );
		actionData.put( DBConstant.TMS_COM_RULE_ACTION_AC_ENABLE, MapUtil.getLong( reqs, DBConstant.TMS_COM_RULE_ACTION_AC_ENABLE ) );
		tmsSimpleDao.create( "TMS_COM_RULE_ACTION", actionData );

		reqs.put( DBConstant.TMS_COM_RULE_ACTION_AC_ID, sequenceId );

		return actionData;
	}

	// 校验动作名称不能与当前规则下动作名称重复
	private void checkDuplicateAcName( Map<String, Object> cond ) {
		String ac_desc = MapUtil.getString( cond, "AC_DESC" );
		String rule_id = MapUtil.getString( cond, "RULE_ID" );
		String acid = MapUtil.getString( cond, "AC_ID" );

		String sql = "SELECT a.* FROM TMS_COM_RULE_ACTION a WHERE ac_desc=? " + "and RULE_ID=?";

		if( acid != null && acid.length() > 0 ) {
			sql += " and AC_ID != " + acid;
		}

		List<Map<String, Object>> acList = tmsSimpleDao.queryForList( sql, ac_desc, rule_id );
		if( acList != null && acList.size() > 0 ) {
			throw new TmsMgrServiceException( "动作名称重复" );
		}

	}

	/* 动作删除
	 * (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.action.controller.service.ActionService#delAc(java.lang.String[])
	 */
	private void deleteAc( Map<String, ?> input ) {
		tmsSimpleDao.delete( "TMS_COM_RULE_ACTION", MapWrap.map( DBConstant.TMS_COM_RULE_ACTION_AC_ID, MapUtil.getLong( input, DBConstant.TMS_COM_RULE_ACTION_AC_ID ) ).getMap() );
	}

	// 校验动作条件表达式是否正确
	private void checkCond( Map<String, Object> map ) {
		String ac_cond = MapUtil.getString( map, "AC_COND" );
		String ac_expr = MapUtil.getString( map, "AC_EXPR" );
		String ac_desc = MapUtil.getString( map, "AC_DESC" );
		String txnid = MapUtil.getString( map, "RULE_TXN" );
		StringBuffer error = new StringBuffer();

		if( !StringUtil.isEmpty( ac_cond ) ) {
			// 检查条件正确性
			boolean isTrue = true;
			try {
				isTrue = web_tool.compile_expr( txnid, ac_cond, error );
			}
			catch( Exception e ) {
				log.error( e.getMessage(), e );
				throw new TmsMgrServiceException( "条件表达式不合法，由于" + "条件[" + ac_desc + "]" + e.getLocalizedMessage() );
			}
			// 不正确弹出错误信息
			if( !isTrue ) {
				throw new TmsMgrServiceException( "条件表达式不合法，由于" + "条件[" + ac_desc + "]" + error );
			}
		}
		if( !StringUtil.isEmpty( ac_expr ) ) {
			// 检查条件正确性
			boolean isTrue = true;
			try {
				isTrue = web_tool.compile_expr( txnid, ac_expr, error );
			}
			catch( Exception e ) {
				log.error( e.getMessage(), e );
				throw new TmsMgrServiceException( "条件表达式不合法，由于" + "条件[" + ac_desc + "]" + e.getLocalizedMessage() );
			}
			// 不正确弹出错误信息
			if( !isTrue ) {
				throw new TmsMgrServiceException( "条件表达式不合法，由于" + "条件[" + ac_desc + "]" + error );
			}
		}
	}

	/**
	* 方法描述:通过动作主键获取动作信息
	* @param actionId
	* @return
	*/
	public Map<String, Object> getOneAction( String actionId ) {
		return tmsSimpleDao.retrieve( "TMS_COM_RULE_ACTION", MapWrap.map( DBConstant.TMS_COM_RULE_ACTION_AC_ID, Long.valueOf( actionId ) ).getMap() );
	}

	/**
	* 方法描述:更新动作有效性
	* @param stat_id
	* @param stat_status
	*/
	public void updateValidAc( String[] stat_ids, String stat_status ) {
		String[] sqls = new String[stat_ids.length];
		;

		for( int i = 0; i < stat_ids.length; i++ ) {
			sqls[i] = "UPDATE TMS_COM_RULE_ACTION SET AC_ENABLE=" + stat_status + " WHERE AC_ID=" + stat_ids[i];
		}

		tmsSimpleDao.batchUpdate( sqls );
	}

	/**
	* 方法描述:更新条件
	* @param input
	*/
	public void updateAcCond( Map<String, Object> input ) {
		String ac_cond_column = MapUtil.getString( input, "AC_COND_COLUMN" );
		input.remove( "AC_COND_COLUMN" );
		input.put( "AC_ID", MapUtil.getLong( input, "AC_ID" ) );
		String sql = "UPDATE TMS_COM_RULE_ACTION SET " + ac_cond_column + "=:AC_COND_VALUE WHERE AC_ID=:AC_ID";
		tmsSimpleDao.executeUpdate( sql, input );
	}

	private void cacheInit() {
		try {
			cache_init.init( new data_source( dynamicDataSource ) );
		}
		catch( Exception e ) {
			log.error( e.getMessage(), e );
			throw new TmsMgrServiceException( "条件初始化错误，由于" + e.getLocalizedMessage() );
		}
	}

}
