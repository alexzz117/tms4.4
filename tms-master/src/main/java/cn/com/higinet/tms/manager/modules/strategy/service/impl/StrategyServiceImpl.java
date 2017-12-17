/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.strategy.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import cn.com.higinet.tms.manager.modules.strategy.service.StrategyService;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;
import cn.com.higinet.tms.manager.modules.tran.service.TransDefService;
import cn.com.higinet.tms35.comm.web_tool;
import cn.com.higinet.tms35.core.cache.cache_init;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

/**
 * 功能/模块:策略服务类
 * @author zlq
 * @version 1.0  Mar 13, 2015
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
@Service("strategyService35")
@Transactional
public class StrategyServiceImpl implements StrategyService {

	@Autowired
	@Qualifier("dynamicDataSource")
	private DataSource dynamicDataSource;
	
	@Autowired
	@Qualifier("dynamicSimpleDao")
	private SimpleDao dynamicSimpleDao;
	
	@Autowired
	@Qualifier("offlineSimpleDao")
	private SimpleDao offlineSimpleDao;
	
	@Autowired
	private TransDefService transDefService;
	
	@Autowired
	private SequenceService sequenceService;

	private static Log log = LogFactory.getLog( StrategyService.class );

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.rule.service.RuleService#listRule(java.util.Map)
	 */
	public List<Map<String, Object>> listStrategy( Map<String, Object> input ) {
		String txnId = MapUtil.getString( input, DBConstant.TMS_COM_STRATEGY_ST_TXN );
		List<Map<String, Object>> st_list = dynamicSimpleDao.queryForList( "SELECT S.*,(SELECT COUNT(1) FROM TMS_COM_STRATEGY_RULE_REL SRR WHERE SRR.ST_ID=S.ST_ID) RULE_COUNT FROM TMS_COM_STRATEGY S WHERE ST_TXN=? ORDER BY MODIFYTIME DESC ", txnId );

		// 评估策略
		for( Map<String, Object> map : st_list ) {
			map.put( "evalSt", listRuleEvalStrategy( map ) );
		}

		return st_list;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.rule.service.RuleService#listRule(java.util.Map)
	 */
	public List<Map<String, Object>> listStrategyByRuleid( Map<String, Object> input ) {
		String ruleId = MapUtil.getString( input, DBConstant.TMS_COM_STRATEGY_RULE_REL_RULE_ID );
		List<Map<String, Object>> st_list = dynamicSimpleDao.queryForList( "SELECT S.*,(SELECT COUNT(1) FROM TMS_COM_STRATEGY_RULE_REL SRR WHERE SRR.ST_ID=S.ST_ID) RULE_COUNT FROM TMS_COM_STRATEGY S WHERE EXISTS(SELECT 1 FROM TMS_COM_STRATEGY_RULE_REL R WHERE S.ST_ID=R.ST_ID AND R.RULE_ID=?) ORDER BY MODIFYTIME DESC ", ruleId );

		for( Map<String, Object> map : st_list ) {
			map.put( "TAB_DESC", transDefService.getSelfAndParentTranDefAsStr( MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_ST_TXN ) ) );
		}

		return st_list;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.strategy.service.StrategyService#listRuleEvalStrategy(java.util.Map)
	 */
	@Override
	public List<Map<String, Object>> listRuleEvalStrategy( Map<String, Object> evalStrategy ) {
		String st_id = MapUtil.getString( evalStrategy, DBConstant.TMS_COM_STRATEGY_ST_ID );
		List<Map<String, Object>> res_list = dynamicSimpleDao.queryForList( "SELECT * FROM TMS_COM_STRATEGY_RULE_EVAL WHERE ST_ID=? ORDER BY EVAL_TYPE", st_id );
		return res_list;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.rule.service.RuleService#saveRule(java.util.Map)
	 */
	@Transactional
	public Map<String, Object> saveStrategy( Map<String, List<Map<String, ?>>> formList ) {
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
				deleteStrategy( map );
				rmap = map;
			}
		}

		// 复制
		if( copyList != null && copyList.size() > 0 ) {
			for( Map<String, Object> map : copyList ) {
				String old_st_id = MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_ST_ID );

				// 校验策略名称不能重复
				checkDuplicateStrategyName( map );
				// 复制策略
				rmap = createStrategy( map );

				// 复制处置策略
				Map<String, Object> cond_map = new HashMap<String, Object>();
				cond_map.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_ST_ID, old_st_id );
				List<Map<String, Object>> ser_list = listEvalStrategy( cond_map );
				if( ser_list != null && ser_list.size() > 0 ) {
					for( Map<String, Object> map2 : ser_list ) {
						map2.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_ST_ID, MapUtil.getString( rmap, DBConstant.TMS_COM_STRATEGY_ST_ID ) );
						createEvalStrategy( map2 );
					}
				}

				// 复制策略规则
				Map<String, Object> cond_map1 = new HashMap<String, Object>();
				cond_map1.put( DBConstant.TMS_COM_STRATEGY_RULE_REL_ST_ID, old_st_id );
				List<Map<String, Object>> sre_list = listSre( cond_map1 );
				if( sre_list != null && sre_list.size() > 0 ) {
					for( Map<String, Object> map2 : sre_list ) {
						map2.put( DBConstant.TMS_COM_STRATEGY_RULE_REL_ST_ID, MapUtil.getString( rmap, DBConstant.TMS_COM_STRATEGY_ST_ID ) );
						createSre( map2 );
					}
				}
			}
		}

		// 新增
		if( addList != null && addList.size() > 0 ) {
			// 初始化缓存
			cache_init.init( new data_source( dynamicDataSource ) );

			for( Map<String, Object> map : addList ) {
				checkCond( map );// 校验条件有效性

				// 校验规则名称不能重复
				checkDuplicateStrategyName( map );

				rmap = createStrategy( map );

				// 处置策略
				List<Map<String, Object>> ser_list = MapUtil.getList( map, "evalSt" );
				if( ser_list != null && ser_list.size() > 0 ) {
					for( Map<String, Object> map2 : ser_list ) {
						map2.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_ST_ID, MapUtil.getString( rmap, DBConstant.TMS_COM_STRATEGY_ST_ID ) );
						createEvalStrategy( map2 );
					}
				}
			}
		}

		// 修改
		if( modList != null && modList.size() > 0 ) {
			// 初始化缓存
			cache_init.init( new data_source( dynamicDataSource ) );

			for( Map<String, Object> map : modList ) {
				checkCond( map );// 校验条件有效性

				// 校验动作名称不能重复
				checkDuplicateStrategyName( map );

				rmap = modStrategy( map );

				// 处置策略
				List<Map<String, Object>> ser_list = MapUtil.getList( map, "evalSt" );
				if( ser_list != null && ser_list.size() > 0 ) {
					for( Map<String, Object> map2 : ser_list ) {
						modEvalStrategy( map2 );
					}
				}
			}
		}

		// 有效性-启用
		if( validYList != null && validYList.size() > 0 ) {
			// 初始化缓存
			cache_init.init( new data_source( dynamicDataSource ) );

			for( Map<String, Object> map : validYList ) {
				// 校验条件是否有效
				checkCond( map );

				rmap = modStrategy( map );
			}
		}

		// 有效性-停用
		if( validNList != null && validNList.size() > 0 ) {
			for( Map<String, Object> map : validNList ) {
				rmap = modStrategy( map );
			}
		}

		return rmap;
	}

	/**
	* 方法描述:处置策略修改
	* @param map2
	*/
	private Map<String, Object> modEvalStrategy( Map<String, Object> map ) {

		// 处置策略修改条件
		Map<String, Object> condData = new HashMap<String, Object>();
		condData.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_SRE_ID, MapUtil.getLong( map, DBConstant.TMS_COM_STRATEGY_RULE_EVAL_SRE_ID ) );

		// 处置策略修改数据
		Map<String, Object> stData = new HashMap<String, Object>();
		stData.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_SRE_ID, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_RULE_EVAL_SRE_ID ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_ST_ID, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_RULE_EVAL_ST_ID ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_EVAL_TYPE, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_RULE_EVAL_EVAL_TYPE ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_EVAL_MECH, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_RULE_EVAL_EVAL_MECH ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_DIS_STRATEGY, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_RULE_EVAL_DIS_STRATEGY ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_STAT_FUNC, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_RULE_EVAL_STAT_FUNC ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_PS_SCORE, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_RULE_EVAL_PS_SCORE ) );

		// 修改
		dynamicSimpleDao.update( "TMS_COM_STRATEGY_RULE_EVAL", stData, condData );

		return stData;
	}

	/**
	* 方法描述:策略规则关联信息表
	* @param map2
	*/
	private Map<String, Object> createSre( Map<String, Object> map2 ) {

		Map<String, Object> etData = new HashMap<String, Object>();
		etData.put( DBConstant.TMS_COM_STRATEGY_RULE_REL_ST_ID, MapUtil.getLong( map2, DBConstant.TMS_COM_STRATEGY_RULE_REL_ST_ID ) );
		etData.put( DBConstant.TMS_COM_STRATEGY_RULE_REL_RULE_ID, MapUtil.getLong( map2, DBConstant.TMS_COM_STRATEGY_RULE_REL_RULE_ID ) );

		dynamicSimpleDao.create( "TMS_COM_STRATEGY_RULE_REL", etData );

		return etData;
	}

	/**
	* 方法描述:策略规则关联信息表
	* @param cond_map1
	* @return
	*/
	private List<Map<String, Object>> listSre( Map<String, Object> cond_map ) {
		String st_id = MapUtil.getString( cond_map, DBConstant.TMS_COM_STRATEGY_RULE_REL_ST_ID );
		List<Map<String, Object>> sre_list = dynamicSimpleDao.queryForList( "SELECT * FROM TMS_COM_STRATEGY_RULE_REL WHERE ST_ID=?", st_id );
		return sre_list;
	}

	/**
	* 方法描述:规则评估策略信息表
	* @param map2
	*/
	private Map<String, Object> createEvalStrategy( Map<String, Object> map2 ) {
		Long sequenceId = Long.valueOf( sequenceService.getSequenceIdToString( DBConstant.SEQ_TMS_COM_STRATEGY_RULE_EVAL ) );

		Map<String, Object> etData = new HashMap<String, Object>();
		etData.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_SRE_ID, sequenceId );
		etData.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_ST_ID, MapUtil.getLong( map2, DBConstant.TMS_COM_STRATEGY_RULE_EVAL_ST_ID ) );
		etData.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_EVAL_TYPE, MapUtil.getString( map2, DBConstant.TMS_COM_STRATEGY_RULE_EVAL_EVAL_TYPE ) );
		etData.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_EVAL_MECH, MapUtil.getString( map2, DBConstant.TMS_COM_STRATEGY_RULE_EVAL_EVAL_MECH ) );
		etData.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_DIS_STRATEGY, MapUtil.getString( map2, DBConstant.TMS_COM_STRATEGY_RULE_EVAL_DIS_STRATEGY ) );
		etData.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_STAT_FUNC, MapUtil.getString( map2, DBConstant.TMS_COM_STRATEGY_RULE_EVAL_STAT_FUNC ) );
		etData.put( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_PS_SCORE, MapUtil.getString( map2, DBConstant.TMS_COM_STRATEGY_RULE_EVAL_PS_SCORE ) );

		dynamicSimpleDao.create( "TMS_COM_STRATEGY_RULE_EVAL", etData );

		map2.put( DBConstant.TMS_COM_RULE_ACTION_AC_ID, sequenceId );

		return etData;
	}

	/**
	* 方法描述:查询规则评估策略信息表
	* @param cond_map
	* @return
	*/
	private List<Map<String, Object>> listEvalStrategy( Map<String, Object> cond_map ) {
		String st_id = MapUtil.getString( cond_map, DBConstant.TMS_COM_STRATEGY_RULE_EVAL_ST_ID );
		List<Map<String, Object>> ser_list = dynamicSimpleDao.queryForList( "SELECT * FROM TMS_COM_STRATEGY_RULE_EVAL WHERE ST_ID=? ORDER BY eval_type ", st_id );
		return ser_list;
	}

	/**
	* 方法描述:修改策略
	* @param map
	* @return
	*/
	private Map<String, Object> modStrategy( Map<String, Object> map ) {
		// 规则修改条件
		Map<String, Object> condData = new HashMap<String, Object>();
		condData.put( DBConstant.TMS_COM_STRATEGY_ST_ID, MapUtil.getLong( map, DBConstant.TMS_COM_STRATEGY_ST_ID ) );

		// 规则修改数据
		Map<String, Object> stData = new HashMap<String, Object>();
		stData.put( DBConstant.TMS_COM_STRATEGY_ST_ID, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_ST_ID ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_ST_TXN, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_ST_TXN ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_ST_NAME, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_ST_NAME ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_EVAL_COND, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_EVAL_COND ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_EVAL_COND_IN, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_EVAL_COND_IN ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_EVAL_MODE, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_EVAL_MODE ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_RULE_EXEC_MODE, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_RULE_EXEC_MODE ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_ST_ENABLE, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_ST_ENABLE ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_MODIFYTIME, System.currentTimeMillis() );

		// 修改
		dynamicSimpleDao.update( "TMS_COM_STRATEGY", stData, condData );

		return stData;
	}

	/**校验策略名称不能重复
	* 方法描述:
	* @param map
	*/
	private void checkDuplicateStrategyName( Map<String, Object> cond ) {
		String st_name = MapUtil.getString( cond, "ST_NAME" );
		String st_txn = MapUtil.getString( cond, "ST_TXN" );
		String st_id = MapUtil.getString( cond, "ST_ID" );

		String txnids = TransCommon.arr2str( TransCommon.cutToIds( st_txn ) );

		String sql = "SELECT a.* FROM TMS_COM_STRATEGY a WHERE ST_NAME=? " + "and (ST_TXN in (" + txnids + ") OR  exists(select 1 from TMS_COM_TAB where TAB_NAME like '" + st_txn + "%' and a.ST_TXN=TAB_NAME))";

		if( st_id != null && st_id.length() > 0 ) {
			sql += " and ST_ID != " + st_id;
		}

		List<Map<String, Object>> stList = dynamicSimpleDao.queryForList( sql, st_name );
		if( stList != null && stList.size() > 0 ) {
			String txnName = transDefService.getSelfAndParentTranDefAsStr( MapUtil.getString( stList.get( 0 ), DBConstant.TMS_COM_STRATEGY_ST_TXN ) );
			throw new TmsMgrServiceException( "策略[" + st_name + "]与交易[" + txnName + "]策略名称重复" );
		}
	}

	// 校验条件有效性
	private void checkCond( Map<String, Object> map ) {
		String eval_cond = MapUtil.getString( map, "EVAL_COND" );
		String st_name = MapUtil.getString( map, "ST_NAME" );
		String st_txn = MapUtil.getString( map, "ST_TXN" );
		StringBuffer error = new StringBuffer();

		if( !StringUtil.isEmpty( eval_cond ) ) {
			// 检查条件正确性
			boolean isTrue = true;
			try {
				isTrue = web_tool.compile_expr( st_txn, eval_cond, error );
			}
			catch( Exception e ) {
				log.error( e );
				throw new TmsMgrServiceException( "条件表达式不合法，由于" + "条件[" + st_name + "]" + e.getLocalizedMessage() );
			}
			// 不正确弹出错误信息
			if( !isTrue ) {
				throw new TmsMgrServiceException( "条件表达式不合法，由于" + "条件[" + st_name + "]" + error );
			}
		}
	}

	/**
	* 方法描述:删除策略及处置策略
	* @param map
	*/
	private void deleteStrategy( Map<String, Object> map ) {
		dynamicSimpleDao.delete( "TMS_COM_STRATEGY", MapWrap.map( DBConstant.TMS_COM_STRATEGY_ST_ID, MapUtil.getLong( map, DBConstant.TMS_COM_STRATEGY_ST_ID ) ).getMap() );
		dynamicSimpleDao.delete( "TMS_COM_STRATEGY_RULE_EVAL", MapWrap.map( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_ST_ID, MapUtil.getLong( map, DBConstant.TMS_COM_STRATEGY_ST_ID ) ).getMap() );
		dynamicSimpleDao.delete( "TMS_COM_STRATEGY_RULE_REL", MapWrap.map( DBConstant.TMS_COM_STRATEGY_RULE_EVAL_ST_ID, MapUtil.getLong( map, DBConstant.TMS_COM_STRATEGY_ST_ID ) ).getMap() );
	}

	/**
	* 方法描述:策略基本信息存库
	* @param map
	* @return
	*/
	private Map<String, Object> createStrategy( Map<String, Object> map ) {
		Map<String, Object> stData = new HashMap<String, Object>();
		Long sequenceId = Long.valueOf( sequenceService.getSequenceIdToString( DBConstant.SEQ_TMS_COM_STRATEGY ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_ST_ID, sequenceId );
		stData.put( DBConstant.TMS_COM_STRATEGY_ST_NAME, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_ST_NAME ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_ST_TXN, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_ST_TXN ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_EVAL_COND, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_EVAL_COND ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_EVAL_COND_IN, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_EVAL_COND_IN ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_EVAL_MODE, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_EVAL_MODE ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_RULE_EXEC_MODE, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_RULE_EXEC_MODE ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_ST_ENABLE, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_ST_ENABLE ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_CREATETIME, System.currentTimeMillis() );
		stData.put( DBConstant.TMS_COM_STRATEGY_MODIFYTIME, System.currentTimeMillis() );

		dynamicSimpleDao.create( "TMS_COM_STRATEGY", stData );

		map.put( DBConstant.TMS_COM_STRATEGY_ST_ID, sequenceId );

		return stData;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.strategy.service.StrategyService#listStrategyRule(java.util.Map)
	 */
	@Override
	public List<Map<String, Object>> listStrategyRule( Map<String, Object> input ) {
		String stId = MapUtil.getString( input, DBConstant.TMS_COM_STRATEGY_ST_ID );
		// TODO 显示当前策略及父级策略的规则
		List<Map<String, Object>> st_list = dynamicSimpleDao.queryForList( "select s.* from tms_com_strategy_rule_rel srr left join tms_com_rule s on srr.rule_id=s.rule_id where st_id=? ORDER BY eval_type ,disposal DESC,rule_order,rule_timestamp DESC ", stId );

		return st_list;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.strategy.service.StrategyService#saveStrategyRule(java.util.Map)
	 */
	@Override
	public Map<String, Object> saveStrategyRule( Map<String, List<Map<String, String>>> formList ) {
		List<Map<String, Object>> delList = MapUtil.getList( formList, "del" );
		List<Map<String, Object>> addList = MapUtil.getList( formList, "add" );

		Map<String, Object> rmap = new HashMap<String, Object>();

		// 删除
		if( delList != null && delList.size() > 0 ) {
			for( Map<String, Object> map : delList ) {
				deleteStrategyRule( map );
				rmap = map;
			}
		}

		// 新增
		if( addList != null && addList.size() > 0 ) {
			for( Map<String, Object> map : addList ) {
				rmap = createStrategyRule( map );
			}
		}
		return rmap;
	}

	/**
	* 方法描述:增加策略规则关联信息表
	* @param map
	* @return
	*/
	private Map<String, Object> createStrategyRule( Map<String, Object> map ) {

		Map<String, Object> stData = new HashMap<String, Object>();
		stData.put( DBConstant.TMS_COM_STRATEGY_RULE_REL_RULE_ID, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_RULE_REL_RULE_ID ) );
		stData.put( DBConstant.TMS_COM_STRATEGY_RULE_REL_ST_ID, MapUtil.getString( map, DBConstant.TMS_COM_STRATEGY_RULE_REL_ST_ID ) );

		dynamicSimpleDao.create( "TMS_COM_STRATEGY_RULE_REL", stData );

		return stData;
	}

	/**
	* 方法描述:删除策略规则关联信息表
	* @param map
	*/
	private void deleteStrategyRule( Map<String, Object> map ) {
		Map<String, Object> c = new HashMap<String, Object>();
		c.put( DBConstant.TMS_COM_STRATEGY_RULE_REL_RULE_ID, MapUtil.getLong( map, DBConstant.TMS_COM_STRATEGY_RULE_REL_RULE_ID ) );
		c.put( DBConstant.TMS_COM_STRATEGY_RULE_REL_ST_ID, MapUtil.getLong( map, DBConstant.TMS_COM_STRATEGY_RULE_REL_ST_ID ) );
		dynamicSimpleDao.delete( "TMS_COM_STRATEGY_RULE_REL", c );
	}
}
