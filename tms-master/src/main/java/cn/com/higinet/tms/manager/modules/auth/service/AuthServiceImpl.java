package cn.com.higinet.tms.manager.modules.auth.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.common.util.AuthDataBusUtil;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.aop.cache.CacheRefresh;
import cn.com.higinet.tms.manager.modules.aop.cache.common.MethodConfig;
import cn.com.higinet.tms.manager.modules.aop.cache.common.ParseCacheConfigXml;
import cn.com.higinet.tms.manager.modules.auth.common.AuthStaticParameters;
import cn.com.higinet.tms.manager.modules.auth.common.ParseTableConfigXML;
import cn.com.higinet.tms.manager.modules.auth.common.TableConfig;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms.manager.modules.common.SqlWhereHelper;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.datamgr.DataOperCenter;
import cn.com.higinet.tms.manager.modules.datamgr.common.ConfigAttrabute;
import cn.com.higinet.tms.manager.modules.mgr.service.NameListService;
import cn.com.higinet.tms.manager.modules.tmsreport.service.DisposalService;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;
import cn.com.higinet.tms.manager.modules.userpattern.service.UserPatternService;

/**
 * 授权中心服务实现类
 * @author zhangfg
 * @version 1.0.0 2012-09-03
 * 
 * @author zhang.lei
 */
@Service("authService")
public class AuthServiceImpl extends ApplicationObjectSupport implements AuthService {

	private static final Logger log = LoggerFactory.getLogger( AuthServiceImpl.class );

	@Autowired
	@Qualifier("offlineSimpleDao")
	private SimpleDao offlineSimpleDao;

	@Autowired
	@Qualifier("onlineSimpleDao")
	private SimpleDao onlineSimpleDao;

	@Autowired
	private UserPatternService userPatternService35;

	@Autowired
	private DataOperCenter dataOperCenter;

	@Autowired
	private TaskExecutor refreshCacheExecutor;

	@Autowired
	private DisposalService disposalService;

	@Autowired
	private NameListService nameListService;

	private int logOrder = 1;

	@Bean()
	public TaskExecutor refreshCacheExecutor() {
		SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
		simpleAsyncTaskExecutor.setDaemon( true );
		simpleAsyncTaskExecutor.setConcurrencyLimit( 100 );
		simpleAsyncTaskExecutor.setThreadNamePrefix( "refreshCacheTaskExecutor" );
		return simpleAsyncTaskExecutor;
	}

	public List<Map<String, Object>> showCenter( Map<String, String> conds ) {
		List<Map<String, Object>> tdataList = new ArrayList<Map<String, Object>>();
		for( int i = 0; i < AuthStaticParameters.TABLENAME.length; i++ ) {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put( "MODULEID", AuthStaticParameters.TABLENAME[i][0] );
			data.put( "MODELNAME", AuthStaticParameters.TABLENAME[i][1] );
			data.put( "COUNT", "0" );
			tdataList.add( data );
		}

		String sql = "SELECT AUTHINFO.MODULE_NAME MODELNAME,COUNT(*) COUNT FROM TMS_MGR_AUTHINFO AUTHINFO WHERE AUTHINFO.AUTH_STATUS = '0' GROUP BY AUTHINFO.MODULE_NAME";
		List<Map<String, Object>> dataList = offlineSimpleDao.queryForList( sql, conds );
		if( dataList != null && !dataList.isEmpty() ) {
			for( Map<String, Object> data : dataList ) {
				for( Map<String, Object> tdata : tdataList ) {
					if( StringUtil.parseToString( data.get( "MODELNAME" ) ).equals( StringUtil.parseToString( tdata.get( "MODELNAME" ) ) ) ) {
						tdata.put( "COUNT", StringUtil.parseToString( data.get( "COUNT" ) ) );
						break;
					}
				}
			}
		}
		return tdataList;
	}

	/**
	 * 分页查询待授权列表信息
	 * 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> dataList( Map<String, String> conds ) {
		String sql = "SELECT AUTH.*, " +
		// "(SELECT AUTHRECORD.OPERATEDATA_VALUE FROM TMS_MGR_AUTHRECORD AUTHRECORD WHERE AUTHRECORD.AUTH_ID = AUTH.AUTH_ID AND AUTHRECORD.IS_MAIN=1) DATAVALUE, " +
				" AUTH.operatedata_value DATAVALUE, "
				+ "(SELECT AUTHRECORD.OPERATE_NAME FROM TMS_MGR_AUTHRECORD AUTHRECORD WHERE AUTHRECORD.AUTH_ID = AUTH.AUTH_ID AND AUTHRECORD.IS_MAIN=1) OPERATENAME, "
				+ "(SELECT COUNT(*) FROM TMS_MGR_AUTHRECORD AUTHRECORD WHERE AUTHRECORD.AUTH_ID = AUTH.AUTH_ID) SUB_OPERATE_NUM, "
				+ "OPERATOR.REAL_NAME FROM TMS_MGR_AUTHINFO AUTH LEFT JOIN CMC_OPERATOR OPERATOR ON AUTH.PROPOSER_ID = OPERATOR.OPERATOR_ID WHERE AUTH.AUTH_STATUS='0' AND AUTH.MODULE_NAME = :modelName";

		Page<Map<String, Object>> page = offlineSimpleDao.pageQuery( sql, conds, new Order().asc( "PROPOSER_TIME" ) );

		List<Map<String, Object>> dataList = page.getList();
		for( Map<String, Object> data : dataList ) {
			String authTxn = StringUtil.parseToString( data.get( "TXN_ID" ) );
			if( authTxn != null && !"".equals( authTxn ) ) {
				data.put( "TXNNAME", getFullTxnPath( authTxn, DBConstant.TMS_COM_TAB.TAB_DESC ) );
			}
			// String qPkvalue = MapUtil.getString(data, "QUERY_PKVALUE");
			// data.put("QUERY_PKVALUE", qPkvalue.split(",")[0]);
		}
		page.setList( dataList );
		return page;
	}

	/**
	 * 获取交易名称，完整路径
	 * 
	 * @param authTxn
	 * @return
	 */
	private String getFullTxnPath( String authTxn, String txnField ) {
		StringBuffer sb = new StringBuffer();

		sb.append( "select " + TMS_COM_TAB.TAB_NAME + ", " + TMS_COM_TAB.TAB_DESC + " from " + TMS_COM_TAB.TABLE_NAME + " where " + TMS_COM_TAB.TAB_NAME + " in("
				+ TransCommon.arr2str( TransCommon.cutToIds( authTxn ) ) + ") order by TAB_NAME DESC" );

		List<Map<String, Object>> fartherTranDef = offlineSimpleDao.queryForList( sb.toString() );

		if( fartherTranDef.size() == 0 ) {
			fartherTranDef = onlineSimpleDao.queryForList( sb.toString() );
		}

		String txnName = "";
		for( Map<String, Object> tran : fartherTranDef ) {
			String parentTxnName = MapUtil.getString( tran, txnField );
			txnName = parentTxnName + "-" + txnName;
		}
		if( !txnName.equals( "" ) ) {
			txnName = txnName.substring( 0, txnName.length() - 1 );
		}
		return txnName;
	}

	public Map<String, Object> getAuthInfoById( String authId ) {
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put( "AUTH_ID", authId );
		return offlineSimpleDao.retrieve( DBConstant.TMS_MGR_AUTHINFO, conds );
	}

	/**
	 * 创建授权信息
	 * 
	 * @param auth
	 * @return
	 */
	public void aopCreateAuthInfo( Map<String, Object> authInfo ) {
		Integer maxAuthId = getMaxAuthId();
		authInfo.put( "AUTH_ID", maxAuthId + 1 );
		offlineSimpleDao.create( "TMS_MGR_AUTHINFO", authInfo );
	}

	public Integer getNextLogOrder( String authId ) {
		String sql = "SELECT MAX(LOG_ORDER) MAX_LOG_ORDER FROM TMS_MGR_AUTHLOG WHERE AUTH_ID =:authId";
		Map<String, Object> sqlConds = new HashMap<String, Object>();
		sqlConds.put( "authId", authId );
		Map<String, Object> idMap = offlineSimpleDao.queryForList( sql, sqlConds ).get( 0 );
		String maxLogOrder = MapUtil.getString( idMap, "MAX_LOG_ORDER" );
		if( StringUtil.isEmpty( maxLogOrder ) ) {
			return new Integer( 1 );
		}
		return Integer.parseInt( maxLogOrder ) + 1;
	}

	private Integer getMaxAuthId() {
		String sql = "SELECT MAX(AUTH_ID) MAX_AUTH_ID FROM TMS_MGR_AUTHINFO";
		Map<String, Object> idMap = offlineSimpleDao.queryForList( sql ).get( 0 );
		if( StringUtil.isEmpty( MapUtil.getString( idMap, "MAX_AUTH_ID" ) ) ) {
			return new Integer( 1 );
		}
		String maxId = MapUtil.getString( idMap, "MAX_AUTH_ID" );
		return Integer.parseInt( maxId );
	}

	/**
	 * 将授权信息与操作日志创建关联
	 * 
	 * @param authId
	 * @param logId
	 * @return
	 */
	public void aopCreateAuthLog( Map<String, Object> logMap ) {
		offlineSimpleDao.create( "TMS_MGR_AUTHLOG ", logMap );
	}

	/**
	 * 分页查询授权日志列表
	 * 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> authLogList( Map<String, String> reqs ) {
		String sql = "SELECT LOG.*," + " OPER.REAL_NAME,OPER.LOGIN_NAME,FUNC.FUNC_NAME"
				+ " FROM CMC_OPERATE_LOG LOG LEFT JOIN CMC_OPERATOR OPER ON LOG.OPERATOR_ID = OPER.OPERATOR_ID"
				+ " LEFT JOIN TMS_MGR_AUTHLOG AUTHLOG ON AUTHLOG.LOG_ID = LOG.PRIMARY_KEY_ID" + " LEFT JOIN CMC_FUNC FUNC ON FUNC.FUNC_ID = LOG.FUNC_ID"
				+ " WHERE LOG.PRIMARY_KEY_ID IN (SELECT AUTHLOG.LOG_ID FROM TMS_MGR_AUTHLOG AUTHLOG WHERE AUTHLOG.AUTH_ID = :AUTH_ID)";
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put( "AUTH_ID", StringUtil.parseToString( reqs.get( "authId" ) ) );

		Page<Map<String, Object>> authLogPage = offlineSimpleDao.pageQuery( sql, conds, new Order().desc( "OPERATE_TIME" ) );
		return authLogPage;
	}

	/**
	 * 根据条件查询授权信息
	 * 
	 * @param conds
	 * @return
	 */
	public Page<Map<String, Object>> historyDataList( Map<String, String> conds ) {
		String modelName = MapUtil.getString( conds, "modelName" );
		String sql = "SELECT AI.ORIG_OPERATENAME, AI.OPERATEDATA_VALUE, AI.AUTH_MSG, AI.AUTH_ID, AI.TXN_ID, AI.AUTH_STATUS, AI.PROPOSER_TIME, AI.REFRESH_STATUS, AI.REFRESH_INFO FROM TMS_MGR_AUTHINFO AI WHERE AI.MODULE_NAME = '"
				+ modelName + "'";
		if( !MapUtil.isEmpty( conds ) ) {
			String txnId = MapUtil.getString( conds, "TXN_ID" );
			if( !StringUtil.isEmpty( txnId ) ) {
				String[] txnIdArr = txnId.split( "," );
				String txnIdStr = SqlWhereHelper.getInWhere( txnIdArr );
				sql += " AND AI.TXN_ID in (" + txnIdStr + ")";
			}
			if( !StringUtil.isEmpty( MapUtil.getString( conds, "AUTH_STATUS" ) ) ) {
				sql += " AND AI.AUTH_STATUS = :AUTH_STATUS ";
			}
			if( !StringUtil.isEmpty( MapUtil.getString( conds, "START_TIME" ) ) ) {
				sql += " AND AI.PROPOSER_TIME >= :START_TIME ";
			}
			if( !StringUtil.isEmpty( MapUtil.getString( conds, "END_TIME" ) ) ) {
				sql += " AND AI.PROPOSER_TIME <= :END_TIME ";
			}
		}

		Page<Map<String, Object>> page = offlineSimpleDao.pageQuery( sql, conds, new Order().desc( "PROPOSER_TIME" ) );

		List<Map<String, Object>> dataList = page.getList();
		for( Map<String, Object> data : dataList ) {
			String authTxn = StringUtil.parseToString( data.get( "TXN_ID" ) );
			if( authTxn != null && !"".equals( authTxn ) ) {
				data.put( "TXNNAME", getFullTxnPath( authTxn, DBConstant.TMS_COM_TAB.TAB_DESC ) );
			}
		}
		page.setList( dataList );
		return page;
	}

	/**
	 * 根据表名，主键及主键值获取正式表数据
	 * 
	 * @param tableName
	 * @param pk
	 * @param pkValue
	 * @return
	 */
	public List<Map<String, Object>> queryOldData( String tableName, String pk, String pkValue ) {
		String sql = " SELECT * FROM " + tableName + " where " + pk + "='" + pkValue.split( "," )[0] + "'";
		List<Map<String, Object>> dataList = onlineSimpleDao.queryForList( sql );
		return dataList;
	}

	/**
	 * 得到新旧对比数据
	 * 
	 * @param reqs
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getDataCompare( Map<String, String> reqs ) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		String table_name = reqs.get( "table_name" );
		String table_pk = reqs.get( "table_pk" );
		String table_pkvalue = reqs.get( "table_pkvalue" );

		Map conds = new HashMap();
		if( table_pk.contains( "," ) ) {
			String[] unionKey = table_pk.split( "," );
			String[] unionKeyValue = table_pkvalue.split( "," );
			for( int i = 0; i < unionKey.length; i++ ) {
				String key = unionKey[i];
				conds.put( key, unionKeyValue[i] );
			}
		}
		else {
			conds.put( table_pk, table_pkvalue );
		}

		Map<String, Object> newMap = null;
		Map<String, Object> oldMap = null;
		if( "TMS_COM_USERPATTERN".equals( table_name ) ) {
			Map conds2 = new HashMap();
			String[] split = table_pkvalue.split( "," );
			if( split.length == 3 ) {
				conds2.put( table_pk, split[0] );
			}
			newMap = offlineSimpleDao.retrieve( table_name, conds2 );
			oldMap = onlineSimpleDao.retrieve( table_name, conds2 );
			list = parseUpDBRecord( newMap, oldMap, table_pkvalue );
		}
		/*
		* else if("TMS_COM_STRATEGY_RULE_REL".equals(table_name)){ List<Map<String,Object>> new_list = tmpSimpleDao.queryForList("select * from TMS_COM_STRATEGY_RULE_REL where st_id=?", table_pkvalue); List<Map<String,Object>> old_list = officialSimpleDao.queryForList("select * from TMS_COM_STRATEGY_RULE_REL where st_id=?", table_pkvalue);
		* 
		* for (Map<String, Object> t_newMap : new_list) { newMap = t_newMap; String new_rule_id = MapUtil.getString(t_newMap, DBConstant.TMS_COM_STRATEGY_RULE_REL_RULE_ID); for (Map<String, Object> t_oldMap : old_list) { String old_rule_id = MapUtil.getString(t_oldMap, DBConstant.TMS_COM_STRATEGY_RULE_REL_RULE_ID); if(old_rule_id.equals(new_rule_id)) { oldMap = t_oldMap; old_list.remove(t_oldMap); break; } } list.addAll(parseDBRecord(newMap,oldMap,table_name));
		* 
		* oldMap = null; }
		* 
		* for (Map<String, Object> map : old_list) { list.addAll(parseDBRecord(null,map,table_name)); }
		* 
		* int i = 1; for (Map<String, Object> map2 : list) { map2.put("COL_NUM", i++); } }
		*/else {
			newMap = offlineSimpleDao.retrieve( table_name, conds );
			oldMap = onlineSimpleDao.retrieve( table_name, conds );
			list = parseDBRecord( newMap, oldMap, table_name );
		}

		return list;
	}

	/**
	 * 用户行为习惯数据对比数据生成
	 * 
	 * @param newMap
	 * @param oldMap
	 * @param table_pkvalue
	 * @return
	 */
	private List<Map<String, Object>> parseUpDBRecord( Map<String, Object> newMap, Map<String, Object> oldMap, String table_pkvalue ) {
		// table_pkvalue分割成userid、statid、patternid
		String[] split = table_pkvalue.split( "," );
		if( split.length == 3 ) {
			String up_s = MapUtil.getString( newMap, "USER_PATTERN" ); // USER_PATTERN，临时数据
			Map<String, Object> upNewMap = userPatternService35.getOneUserPattern( split[0], split[1], split[2], up_s );

			String up_s_o = MapUtil.getString( oldMap, "USER_PATTERN" ); // USER_PATTERN，正式数据
			Map<String, Object> upOldMap = userPatternService35.getOneUserPattern( split[0], split[1], split[2], up_s_o );

			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			Map<String, Object> upValue = new HashMap<String, Object>(); // "行为习惯值"
			upValue.put( "COL_NUM", 1 );
			upValue.put( "FIELD_NAME", "行为习惯值" );
			upValue.put( "NEW_VALUE", upNewMap.get( "UPVALUE" ) );
			upValue.put( "OLD_VALUE", upOldMap.get( "UPVALUE" ) );
			list.add( upValue );

			Map<String, Object> upStartDate = new HashMap<String, Object>(); // "开始日期"
			upStartDate.put( "COL_NUM", 2 );
			upStartDate.put( "FIELD_NAME", "开始日期" );
			upStartDate.put( "NEW_VALUE", upNewMap.get( "STARTDATE" ) );
			upStartDate.put( "OLD_VALUE", upOldMap.get( "STARTDATE" ) );
			list.add( upStartDate );

			Map<String, Object> upEndDate = new HashMap<String, Object>(); // "结束日期"
			upEndDate.put( "COL_NUM", 3 );
			upEndDate.put( "FIELD_NAME", "结束日期" );
			upEndDate.put( "NEW_VALUE", upNewMap.get( "ENDDATE" ) );
			upEndDate.put( "OLD_VALUE", upOldMap.get( "ENDDATE" ) );
			list.add( upEndDate );

			return list;
		}
		return null;
	}

	/**
	 * 将数据库中的两条记录组织成可以显示新旧数据对比的形式
	 * 
	 * @param newMap
	 * @param oldMap
	 * @param tableId
	 * @return
	 */
	private List<Map<String, Object>> parseDBRecord( Map<String, Object> newMap, Map<String, Object> oldMap, String tableId ) {
		// 新旧数据对比，list集合，包含多个Map。每个Map包含，新值、旧值、序号、字段名等
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		// keySet：数据库字段名
		Set<String> keySet = null;
		if( newMap != null && newMap.size() != 0 ) {
			keySet = newMap.keySet();
		}
		else if( oldMap != null && oldMap.size() != 0 ) {
			keySet = oldMap.keySet();
		}

		if( keySet == null ) {
			return list;
		}

		// 数据对比表配置
		TableConfig tc = ParseTableConfigXML.getInstance().getTableConfig( tableId );
		List<Map<String, String>> fieldsList = tc.getFields();

		// 遍历表配置中的每个字段配置
		int i = 1; // 序号
		for( Map<String, String> fieldMap : fieldsList ) {
			Map<String, Object> row = new HashMap<String, Object>(); // 新旧数据对比的Map

			for( String key : keySet ) { // 循环，构成新旧数据对比的Map
				String fieldId = fieldMap.get( "id" ); // id代表表配置中的字段名
				if( fieldId.equals( key ) ) { // 如果数据库字段和字段配置相等
					row.put( "COL_NUM", i ); // 序号
					String fieldName = fieldMap.get( "name" ); // 字段名
					row.put( "FIELD_NAME", fieldName );

					String fieldCode = fieldMap.get( "code" ); // 获取字段引用的代码
					String type = fieldMap.get( "type" ); // 获取字段类型
					try {
						if( newMap != null && newMap.size() != 0 ) {
							if( !StringUtil.isEmpty( fieldCode ) ) { // 字段值，新
								// String sql = "select code_value from cmc_code where code_key='"+newMap.get(key)+"' and category_id = '"+fieldCode+"'";
								// List<Map<String, Object>> codeValueList = tmpSimpleDao.queryForList(sql);

								String sql = "select code_value from cmc_code where code_key=:codeKey and category_id =:fieldCode";
								Map<String, Object> conds = new HashMap<String, Object>();
								conds.put( "codeKey", String.valueOf( newMap.get( key ) ) );
								conds.put( "fieldCode", fieldCode );
								List<Map<String, Object>> codeValueList = offlineSimpleDao.queryForList( sql, conds );
								if( codeValueList != null ) {
									row.put( "NEW_VALUE", codeValueList.get( 0 ).get( "code_value" ) );
								}
							}
							else if( !StringUtil.isEmpty( type ) && type.equals( "query" ) ) {
								fillQueryType( newMap, key, row, fieldMap, "new" );
							}
							else if( !StringUtil.isEmpty( type ) && type.equals( "custom" ) ) {
								fillCustomType( newMap, key, row, "new" );
							}
							else {
								row.put( "NEW_VALUE", newMap.get( key ) );
							}
						}
					}
					catch( Exception e ) {
						e.printStackTrace();
					}

					try {
						if( oldMap != null && oldMap.size() != 0 ) {
							if( !StringUtil.isEmpty( fieldCode ) ) { // 字段值，旧
								// String sql = "select code_value from cmc_code where code_key='"+oldMap.get(key)+"' and category_id = '"+fieldCode+"'";
								// List<Map<String, Object>> codeValueList = tmpSimpleDao.queryForList(sql);

								String sql = "select code_value from cmc_code where code_key=:codeKey and category_id =:fieldCode";
								Map<String, Object> conds = new HashMap<String, Object>();
								conds.put( "codeKey", String.valueOf( oldMap.get( key ) ) );
								conds.put( "fieldCode", fieldCode );
								List<Map<String, Object>> codeValueList = offlineSimpleDao.queryForList( sql, conds );
								if( codeValueList != null && codeValueList.size() > 0 ) {
									row.put( "OLD_VALUE", codeValueList.get( 0 ).get( "code_value" ) );
								}
							}
							else if( !StringUtil.isEmpty( type ) && type.equals( "query" ) ) {
								fillQueryType( oldMap, key, row, fieldMap, "old" );
							}
							else if( !StringUtil.isEmpty( type ) && type.equals( "custom" ) ) {
								fillCustomType( oldMap, key, row, "old" );
							}
							else {
								row.put( "OLD_VALUE", oldMap.get( key ) );
							}
						}
					}
					catch( Exception e ) {
						e.printStackTrace();
					}

					if( !StringUtil.isEmpty( type ) ) { // 字段类型是否为日期
						if( type.equals( "date" ) ) {
							row.put( "TYPE", "date" );
						}
						else if( type.equals( "datetime" ) ) {
							row.put( "TYPE", "datetime" );
						}
					}
					break;
				}
			}

			// 将数据对比Map添加到新旧数据对比的list集合
			if( row.size() != 0 ) {
				list.add( row );
				i++;
			}
		}

		return list;
	}

	/**
	 * 对type为custom的数据对比项处理
	 * 
	 * @param newMap
	 * @param key
	 * @param row
	 * @param fieldMap
	 * @param string
	 */
	private void fillCustomType( Map<String, Object> dataMap, String key, Map<String, Object> row, String flag ) {
		if( "PS_VALUE".equals( key ) ) { // 解析处置值
			String value = MapUtil.getString( dataMap, key ); // 原始处置值，形如：0,PS01|30,PS02|50,PS04|80,PS05
			String[] arr = value.split( "\\|" );

			// 获取处置代码key-value
			/*
			 * String sql = "select code_key, code_value from cmc_code where category_id = 'tms.rule.disposal'"; List<Map<String, Object>> disList = tmpSimpleDao.queryForList(sql);
			 */
			List<Map<String, Object>> disList = disposalService.queryList();

			StringBuffer sb = new StringBuffer(); // 解析后的处置值
			for( int i = 0; i < arr.length; i++ ) { // 对每一段处置值进行解析
				String disCode = arr[i].split( "," )[1]; // 处置代码key
				for( Map<String, Object> dis : disList ) {
					String codeKey = MapUtil.getString( dis, "DP_CODE" );
					if( codeKey.equals( disCode ) ) {
						String codeValue = MapUtil.getString( dis, "DP_NAME" );// 处置代码value
						sb.append( codeValue );
					}
				}

				String v = arr[i].split( "," )[0]; // 处置值，开始
				String nextV = ""; // 处置值，结束
				if( i != arr.length - 1 ) {
					nextV = arr[i + 1].split( "," )[0];
					sb.append( "[" + v + "-" + nextV + "]," );
				}
				else {
					nextV = "100";
					sb.append( "[" + v + "-" + nextV + "]" );
				}
			}

			if( "new".equals( flag ) ) {
				row.put( "NEW_VALUE", sb.toString() );
			}
			else if( "old".equals( flag ) ) {
				row.put( "OLD_VALUE", sb.toString() );
			}
		}
	}

	/**
	 * 填充查询类型为query的新旧数据字段
	 * 
	 * @param dataMap
	 * @param key
	 * @param row
	 * @param fieldMap
	 * @param flag
	 */
	private void fillQueryType( Map<String, Object> dataMap, String key, Map<String, Object> row, Map<String, String> fieldMap, String flag ) {
		String tableName = fieldMap.get( "refTable" );
		String field = fieldMap.get( "refField" );
		String pk = fieldMap.get( "refPk" );
		if( !StringUtil.isEmpty( pk ) ) {
			String sql = "select " + field + " from " + tableName;
			String[] refPkArr = pk.split( "," );
			for( int j = 0; j < refPkArr.length; j++ ) {
				String refPk = refPkArr[j];
				if( j == 0 ) {
					String vArr = MapUtil.getString( dataMap, key );
					String inStr = SqlWhereHelper.getInWhere( vArr.split( "," ) );
					sql += " where " + refPk + " in (" + inStr + ")";
				}
				else {
					String refPkValue = MapUtil.getString( dataMap, refPk );
					String fullTxnPath = getFullTxnPath( refPkValue, "tab_name" );
					String fullTxns = SqlWhereHelper.getInWhere( fullTxnPath.split( "-" ) );
					sql += " and tab_name in (" + fullTxns + ")";
				}
			}

			if( "new".equals( flag ) ) {
				List<Map<String, Object>> newValueList = offlineSimpleDao.queryForList( sql );
				if( newValueList != null && newValueList.size() != 0 ) {
					String newValue = "";
					for( Map<String, Object> map : newValueList ) {
						newValue = newValue + MapUtil.getString( map, field ) + ",";
					}
					row.put( "NEW_VALUE", newValue.substring( 0, newValue.length() - 1 ) );
				}
			}
			else if( "old".equals( flag ) ) {
				List<Map<String, Object>> oldValueList = onlineSimpleDao.queryForList( sql );
				if( oldValueList != null && oldValueList.size() != 0 ) {
					String oldValue = "";
					for( Map<String, Object> map : oldValueList ) {
						oldValue = oldValue + MapUtil.getString( map, field ) + ",";
					}
					row.put( "OLD_VALUE", oldValue.substring( 0, oldValue.length() - 1 ) );
				}
			}
		}
	}

	/**
	 * 执行批量授权
	 */
	@Transactional("tmp")
	// 授权都改为离线库
	public void batchUpadteAuth( Map<String, String> reqs ) {
		String authIds = StringUtil.parseToString( reqs.get( "AUTH_IDS" ) );
		if( !StringUtil.isEmpty( authIds ) ) {
			String authMsg = StringUtil.parseToString( reqs.get( "AUTH_MSG" ) );
			String authStatus = StringUtil.parseToString( reqs.get( "AUTH_STATUS" ) );

			String authIdsStr = SqlWhereHelper.getInWhere( authIds.split( "," ) );
			String sql = "UPDATE TMS_MGR_AUTHINFO SET AUTH_MSG = '" + authMsg + "'" + ",AUTH_STATUS = " + authStatus + " WHERE AUTH_ID in (" + authIdsStr + ")";

			offlineSimpleDao.executeUpdate( sql );
		}

	}

	/**
	 * 获取旧的操作数据
	 */
	public String getOperateData( MethodConfig mc, String tablePkValue ) {
		String tableName = mc.getTableName();
		String operateDataFiled = mc.getOperateData();
		String tablePk = mc.getTablePk();
		String tablePkType = mc.getTablePkType();
		// 生成sql语句和查询条件
		String sql = "SELECT * FROM " + tableName + " where ";
		Map<String, Object> conds = new HashMap<String, Object>();
		if( tablePk.contains( "," ) ) {
			String[] unionKey = tablePk.split( "," );
			String[] tablePkType_arr = tablePkType.split( "," );
			String[] unionKeyValue = tablePkValue.split( "," );
			for( int i = 0; i < unionKey.length; i++ ) {
				String key = unionKey[i];
				String tablePkType_i = tablePkType_arr[i];
				String unionKeyValue_i = unionKeyValue[i];
				conds.put( key, unionKeyValue_i.length() > 0 && "NUMBER".equals( tablePkType_i ) ? Long.parseLong( unionKeyValue_i ) : unionKeyValue_i );// 数据库类型是NUMBER需要转成LONG
				if( i == 0 ) {
					sql += key + "=:" + key;
				}
				else {
					sql += " and " + key + "=:" + key;
				}
			}
		}
		else {
			conds.put( tablePk, tablePkValue.length() > 0 && "NUMBER".equals( tablePkType ) ? Long.parseLong( tablePkValue ) : tablePkValue );
			sql += tablePk + "=:" + tablePk;
		}

		List<Map<String, Object>> oldDataList = onlineSimpleDao.queryForList( sql, conds );
		Map<String, Object> oldDataMap = oldDataList.size() != 0 ? oldDataList.get( 0 ) : null;

		String operDataValue = null; // 操作数据，从正式表中获取
		if( !StringUtil.isEmpty( operateDataFiled ) && oldDataMap != null ) {
			operDataValue = MapUtil.getString( oldDataMap, operateDataFiled );
		}

		return operDataValue;
	}

	public void aopUpdateAuthInfo( Map<String, Object> authInfo ) {
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put( DBConstant.TMS_MGR_AUTHINFO_AUTH_ID, authInfo.get( DBConstant.TMS_MGR_AUTHINFO_AUTH_ID ) );
		offlineSimpleDao.update( DBConstant.TMS_MGR_AUTHINFO, authInfo, conds );
	}

	public void aopCreateAuthRecord( Map<String, Object> authRecord ) {
		authRecord.put( "RECORD_ID", Stringz.randomUUID().toUpperCase() );
		offlineSimpleDao.create( "TMS_MGR_AUTHRECORD", authRecord );
	}

	public void aopDeleteAuthInfo( String authId, String authStatus ) {
		// String sql = "update tms_mgr_authinfo set auth_status = '"+authStatus+"' where auth_id = '"+authId+"'";
		// tmpSimpleDao.executeUpdate(sql);

		// String sql2 = "delete from tms_mgr_authrecord where auth_id = '"+authId+"'";
		// tmpSimpleDao.executeUpdate(sql2);

		String sql = "update tms_mgr_authinfo set auth_status =:authStatus where auth_id =:authId";
		Map<String, Object> cond1 = new HashMap<String, Object>();
		cond1.put( "authStatus", authStatus );
		cond1.put( "authId", authId );
		offlineSimpleDao.executeUpdate( sql, cond1 );

		String sql2 = "delete from tms_mgr_authrecord where auth_id =:authId";
		Map<String, Object> cond2 = new HashMap<String, Object>();
		cond2.put( "authId", authId );
		offlineSimpleDao.executeUpdate( sql2, cond2 );
	}

	public Page<Map<String, Object>> subDataList( Map<String, String> reqs ) {
		String authId = MapUtil.getString( reqs, "authId" );
		String sql2 = "SELECT AUTHRECORD.*,OPER.REAL_NAME FROM TMS_MGR_AUTHRECORD AUTHRECORD LEFT JOIN CMC_OPERATOR OPER ON AUTHRECORD.OPERATOR_ID = OPER.OPERATOR_ID WHERE AUTHRECORD.AUTH_ID = :AUTH_ID";
		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "AUTH_ID", authId );
		return offlineSimpleDao.pageQuery( sql2, conds, new Order().desc( "OPERATE_TIME" ) );
	}

	public Map<String, Object> getAuthInfoByQueryPkValue( String queryPkvalue, MethodConfig mc ) {
		// String sql = "SELECT * FROM TMS_MGR_AUTHINFO AUTHINFO" + " WHERE AUTHINFO.MODULE_ID = '" + mc.getModelId() + "'" + " AND AUTHINFO.AUTH_STATUS = '0' AND AUTHINFO.QUERY_PKVALUE='" + queryPkvalue + "'" + " AND AUTHINFO.QUERY_TABLE_NAME ='" + mc.getQueryTableName() + "'"
		// + " AND AUTHINFO.QUERY_TABLE_PK = '" + mc.getQueryTablePk() + "'";

		String sql = "SELECT * FROM TMS_MGR_AUTHINFO AUTHINFO" + " WHERE AUTHINFO.MODULE_ID =:MODULE_ID AND AUTHINFO.AUTH_STATUS = '0' AND AUTHINFO.QUERY_PKVALUE=:QUERY_PKVALUE "
				+ "AND AUTHINFO.QUERY_TABLE_NAME =:QUERY_TABLE_NAME" + " AND AUTHINFO.QUERY_TABLE_PK =:QUERY_TABLE_PK";
		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "MODULE_ID", mc.getModelId() );
		conds.put( "QUERY_PKVALUE", queryPkvalue );
		conds.put( "QUERY_TABLE_NAME", mc.getQueryTableName() );
		conds.put( "QUERY_TABLE_PK", mc.getQueryTablePk() );
		List<Map<String, Object>> authInfoList = offlineSimpleDao.queryForList( sql, conds );
		return authInfoList.size() == 0 ? null : authInfoList.get( 0 );
	}

	public Map<String, Object> getAuthRecordByMc( MethodConfig mc, String tablePkValue, boolean queryMain ) {
		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "TABLE_NAME", mc.getTableName() );
		conds.put( "TABLE_PK", mc.getTablePk() );
		conds.put( "TABLE_PKVALUE", tablePkValue );
		if( queryMain ) {
			conds.put( "IS_MAIN", "1" );
		}
		String sql = "select * from TMS_MGR_AUTHRECORD c where exists (select 1 from TMS_MGR_AUTHINFO i where i.AUTH_ID=c.AUTH_ID and i.AUTH_STATUS='0')"
				+ " AND C.TABLE_NAME=:TABLE_NAME AND TABLE_PK=:TABLE_PK AND TABLE_PKVALUE=:TABLE_PKVALUE";
		if( queryMain ) {
			sql += " AND IS_MAIN='1'";
		}
		List<Map<String, Object>> l = offlineSimpleDao.queryForList( sql, conds );
		if( l == null || l.size() == 0 ) {
			return null;
		}
		return l.get( 0 );
	}

	public void aopUpdateAuthRecord( Map<String, Object> authRecord ) {
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put( "RECORD_ID", MapUtil.getString( authRecord, "RECORD_ID" ) );
		offlineSimpleDao.update( "TMS_MGR_AUTHRECORD", authRecord, conds );
	}

	public String getDependencyInfo( String authId ) {
		// 根据授权信息ID，获取授权信息
		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "AUTH_ID", authId );
		Map<String, Object> authInfo = offlineSimpleDao.retrieve( "TMS_MGR_AUTHINFO", conds );

		List<Map<String, Object>> depInfoList = null; // 依赖信息List

		// 交易配置的依赖，交易配置依赖于所有直属的上级交易
		String moduleId = MapUtil.getString( authInfo, "MODULE_ID" );
		if( "tranConf".equals( moduleId ) ) {
			String authTxn = MapUtil.getString( authInfo, "QUERY_PKVALUE" );
			String txnFullPath = getFullTxnPath( authTxn, DBConstant.TMS_COM_TAB.TAB_NAME );

			if( txnFullPath.lastIndexOf( "-" ) != -1 ) { // 如果不是顶级交易
				txnFullPath = txnFullPath.substring( 0, txnFullPath.lastIndexOf( "-" ) );
				String fatherTxnStr = SqlWhereHelper.getInWhere( txnFullPath.split( "-" ) );// 上级交易组成的字符串，用在过滤条件的in子句中
				String sql = "SELECT MODULE_NAME, AUTH_ID " + "FROM TMS_MGR_AUTHINFO " + "WHERE MODULE_ID = 'tranConf' AND AUTH_STATUS = '0' and QUERY_PKVALUE in (" + fatherTxnStr
						+ ")";

				depInfoList = offlineSimpleDao.queryForList( sql );
			}

		}
		else { // 其他模块的依赖
			String depPkValues = MapUtil.getString( authInfo, "DEP_PKVALUES" ); // 依赖主键值
			if( StringUtil.isEmpty( depPkValues ) ) {
				return null;
			}
			String[] depPkValueArr = depPkValues.split( "," );
			String depValuesInWhere = SqlWhereHelper.getInWhere( depPkValueArr );

			String depModules = MapUtil.getString( authInfo, "DEP_MODULES" ); // 依赖模块
			String[] depModuleArr = depModules.split( "," );
			String depModulesInWhere = SqlWhereHelper.getInWhere( depModuleArr );

			String sql = "SELECT MODULE_NAME, AUTH_ID FROM TMS_MGR_AUTHINFO" + " WHERE AUTH_STATUS = '0' AND QUERY_PKVALUE IN (" + depValuesInWhere + ")" + " AND MODULE_ID IN ("
					+ depModulesInWhere + ")";
			depInfoList = offlineSimpleDao.queryForList( sql );
		}

		// 构造依赖信息
		if( depInfoList != null && depInfoList.size() != 0 ) {
			StringBuffer sb = new StringBuffer();
			for( Map<String, Object> depInfoMap : depInfoList ) {
				sb.append( "模块名->" + MapUtil.getString( depInfoMap, "MODULE_NAME" ) + "，" );
				sb.append( "授权编号->" + MapUtil.getString( depInfoMap, "AUTH_ID" ) + "；" );
			}
			return sb.toString();
		}
		return null;
	}

	public String checkTranCreate( Map data, MethodConfig mc ) {
		String realOper = MapUtil.getString( data, "real_oper" );
		if( "c".equals( realOper ) || "p".equals( realOper ) ) {
			String tablePk = mc.getTablePk();
			String tabName = MapUtil.getString( data, tablePk );
			// String sql = "select * from tms_mgr_authinfo where QUERY_PKVALUE = '" + tabName + "' and MODULE_ID='tranConf'";
			String sql = "select * from tms_mgr_authinfo where QUERY_PKVALUE =:QUERY_PKVALUE and MODULE_ID='tranConf'";
			Map<String, String> conds = new HashMap<String, String>();
			conds.put( "QUERY_PKVALUE", tabName );
			List<Map<String, Object>> list = offlineSimpleDao.queryForList( sql, conds );

			StringBuffer sb = new StringBuffer();
			for( Map<String, Object> depInfoMap : list ) {
				sb.append( "模块名->" + MapUtil.getString( depInfoMap, "MODULE_NAME" ) + "，" );
				sb.append( "授权编号->" + MapUtil.getString( depInfoMap, "AUTH_ID" ) + "；" );
			}
			return sb.toString();
		}
		return null;
	}

	// 查询名单相关联的名单值的授权记录信息
	public List<Map<String, Object>> getNoneMainAuthRecordByAuthId( String authId ) {
		// String sql = "select * from tms_mgr_authrecord where auth_id = '" + authId + "'" + " and is_main != 1";
		String sql = "select * from tms_mgr_authrecord where auth_id =:AUTHID and is_main != 1";

		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "AUTHID", authId );
		List<Map<String, Object>> authRecordList = offlineSimpleDao.queryForList( sql, conds );
		return authRecordList;
	}

	// 查询名单值相关联的名单的授权记录信息
	public List<Map<String, Object>> getNoneMainAuthRecordByAuthId2( String authId ) {
		// String sql = "select * from tms_mgr_authrecord where auth_id = '" + authId + "'" + " and TABLE_NAME = 'TMS_MGR_ROSTER' ";
		String sql = "select * from tms_mgr_authrecord where auth_id =:AUTHID and TABLE_NAME = 'TMS_MGR_ROSTER' ";
		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "AUTHID", authId );
		List<Map<String, Object>> authRecordList = offlineSimpleDao.queryForList( sql, conds );
		return authRecordList;
	}

	/* ////////////////////////授权查看模块///////////////////////////// */
	public List<Map<String, Object>> listAuthQueryModel() {
		String sql = "SELECT AUTHINFO.MODULE_NAME MODELNAME,COUNT(*) COUNT FROM TMS_MGR_AUTHINFO AUTHINFO GROUP BY AUTHINFO.MODULE_NAME";
		List<Map<String, Object>> dataList = offlineSimpleDao.queryForList( sql );

		List<Map<String, Object>> tdataList = new ArrayList<Map<String, Object>>();
		for( int i = 0; i < AuthStaticParameters.TABLENAME.length; i++ ) {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put( "MODULEID", AuthStaticParameters.TABLENAME[i][0] );
			data.put( "MODELNAME", AuthStaticParameters.TABLENAME[i][1] );
			data.put( "COUNT", "0" );
			tdataList.add( data );
		}

		if( dataList != null && !dataList.isEmpty() ) {
			for( Map<String, Object> data : dataList ) {
				for( Map<String, Object> tdata : tdataList ) {
					if( StringUtil.parseToString( data.get( "MODELNAME" ) ).equals( StringUtil.parseToString( tdata.get( "MODELNAME" ) ) ) ) {
						tdata.put( "COUNT", StringUtil.parseToString( data.get( "COUNT" ) ) );
						break;
					}
				}
			}
		}
		return tdataList;
	}

	public void aopdeleteAuthRecord( String recordId ) {
		String sql = "delete from tms_mgr_authrecord where record_id =:recordId";
		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "recordId", recordId );
		offlineSimpleDao.executeUpdate( sql, conds );
	}

	private void aopdeleteAuthInfo( String recordId, String authId ) {
		// String sql = "delete FROM tms_mgr_authinfo WHERE auth_id not IN (SELECT auth_id FROM tms_mgr_authrecord r where r.record_id != '" + recordId + "') AND auth_id='" + auth_id + "'";
		String sql = "delete FROM tms_mgr_authinfo WHERE auth_id not IN (SELECT auth_id FROM tms_mgr_authrecord r where r.record_id !=:recordId) AND auth_id=:authId";
		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "recordId", recordId );
		conds.put( "authId", authId );
		offlineSimpleDao.executeUpdate( sql, conds );
	}

	public String[] getLogByLogId( String logId ) {
		// String sql = "SELECT PRIMARY_KEY_ID FROM CMC_OPERATE_LOG WHERE LOG_ID = '" + logId + "'";
		String sql = "SELECT PRIMARY_KEY_ID FROM CMC_OPERATE_LOG WHERE LOG_ID =:logId";
		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "logId", logId );
		List<Map<String, Object>> logList = offlineSimpleDao.queryForList( sql, conds );

		String[] lIds = new String[logList.size()];
		int i = 0;
		for( Map<String, Object> log : logList ) {
			lIds[i++] = MapUtil.getString( log, "PRIMARY_KEY_ID" );
		}

		return lIds;
	}

	public String[] getTmpUserPatternName( String userId, String statId, String patternValue ) {
		// String sql = "SELECT CUSNAME FROM TMS_RUN_USER WHERE USERID = '" + userId + "'";
		// List<Map<String, Object>> userList = officialSimpleDao.queryForList(sql);

		// String sql2 = "SELECT STAT_DESC,STAT_TXN FROM TMS_COM_STAT WHERE STAT_ID = " + statId;
		// List<Map<String, Object>> statList = officialSimpleDao.queryForList(sql2);

		String sql = "SELECT CUSNAME FROM TMS_RUN_USER WHERE USERID =:userId";
		Map<String, String> conds1 = new HashMap<String, String>();
		conds1.put( "userId", userId );
		List<Map<String, Object>> userList = onlineSimpleDao.queryForList( sql, conds1 );

		String sql2 = "SELECT STAT_DESC,STAT_TXN FROM TMS_COM_STAT WHERE STAT_ID =:statId";
		Map<String, String> conds2 = new HashMap<String, String>();
		conds2.put( "statId", statId );
		List<Map<String, Object>> statList = onlineSimpleDao.queryForList( sql2, conds2 );

		String statdesc = "";
		String statTxn = "";
		if( userList != null && userList.size() == 1 ) {
			statdesc = MapUtil.getString( statList.get( 0 ), "STAT_DESC" );
			statTxn = MapUtil.getString( statList.get( 0 ), "STAT_TXN" );
		}
		String[] result = {
				statTxn, "(客户号：" + userId + ")-(统计描述：" + statdesc + ")-(行为习惯值：" + patternValue + ")"
		};
		return result;
	}

	/**
	 * 获取用户行为习惯“操作数据”数据项的值，正式表中
	 */
	public String getUserPatternName( MethodConfig mc, Map dataMap ) {
		String userid = MapUtil.getString( dataMap, "userid" );
		String statid = MapUtil.getString( dataMap, "statid" );
		String pattern_id = MapUtil.getString( dataMap, "patternid" );

		String sql = "SELECT CUSNAME FROM TMS_RUN_USER WHERE USERID =:userId";
		Map<String, String> conds1 = new HashMap<String, String>();
		conds1.put( "userId", userid );
		List<Map<String, Object>> userList = onlineSimpleDao.queryForList( sql, conds1 );

		String sql2 = "SELECT STAT_DESC,STAT_TXN FROM TMS_COM_STAT WHERE STAT_ID =:statId";
		Map<String, String> conds2 = new HashMap<String, String>();
		conds2.put( "statId", statid );
		List<Map<String, Object>> statList = onlineSimpleDao.queryForList( sql2, conds2 );
		String statdesc = "";
		if( userList != null && userList.size() == 1 ) {
			statdesc = MapUtil.getString( statList.get( 0 ), "STAT_DESC" );
		}

		String sql3 = "SELECT USER_PATTERN FROM TMS_COM_USERPATTERN WHERE USERID = :userId";
		Map<String, String> conds3 = new HashMap<String, String>();
		conds2.put( "userId", userid );
		List<Map<String, Object>> upList = onlineSimpleDao.queryForList( sql3, conds3 );
		String up_s = "";
		if( upList != null && upList.size() == 1 ) {
			up_s = MapUtil.getString( upList.get( 0 ), "USER_PATTERN" );
		}

		Map<String, Object> upMap = userPatternService35.getOneUserPattern( userid, statid, pattern_id, up_s );
		String patternValue = MapUtil.getString( upMap, "UPVALUE" );

		return "(客户号：" + userid + ")-(统计描述：" + statdesc + ")-(行为习惯值：" + patternValue + ")";
	}

	/**
	 * 获取交易路由的“操作数据”
	 */
	public String getRuleChildName( Map dataMap ) {
		String line = MapUtil.getString( dataMap, "line" );
		String[] rIds = line.split( "a" );
		if( StringUtil.isEmpty( rIds[0] ) || StringUtil.isEmpty( rIds[1] ) ) {
			return null;
		}

		// String sql = "select t.rule_shortdesc from tms_com_rule t where t.rule_id = " + rIds[0];// 起点
		// String sql2 = "select t.rule_shortdesc from tms_com_rule t where t.rule_id = " + rIds[1];// 终点

		// List<Map<String, Object>> list = tmpSimpleDao.queryForList(sql);
		// List<Map<String, Object>> list2 = tmpSimpleDao.queryForList(sql2);

		String sql = "select t.rule_shortdesc from tms_com_rule t where t.rule_id =:ruleId";// 起点
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put( "ruleId", rIds[0] );
		List<Map<String, Object>> list = offlineSimpleDao.queryForList( sql, conds );

		String sql2 = "select t.rule_shortdesc from tms_com_rule t where t.rule_id =:ruleId";// 终点
		Map<String, Object> conds2 = new HashMap<String, Object>();
		conds2.put( "ruleId", rIds[1] );
		List<Map<String, Object>> list2 = offlineSimpleDao.queryForList( sql2, conds2 );

		if( list.size() == 1 && list2.size() == 1 ) {
			String fName = MapUtil.getString( list.get( 0 ), "rule_shortdesc" );
			String cName = MapUtil.getString( list2.get( 0 ), "rule_shortdesc" );
			return fName + " 指向  " + cName;
		}

		return null;
	}

	/**
	 * 产生授权信息和授权记录信息
	 * 
	 * @param arg
	 * @param mc
	 */
	public void doApplyByMc( Object arg, MethodConfig mc ) {
		List dataList = parseArgByMc( arg, mc ); // 获取主键值List
		String authId = ""; // 授权信息Id
		boolean isNewAuth = true;
		if( "true".equals( mc.getIsAuth() ) ) { // 需要授权
			for( int i = 0; i < dataList.size(); i++ ) {
				// 解析授权需要的数据
				Object dataValue = dataList.get( i );
				String queryPkvalue = "";
				String realOper = "";
				if( dataValue instanceof Map ) {
					Map<String, String> dataMap = (Map<String, String>) dataValue;
					queryPkvalue = dataMap.get( "query_pkvalue" );
					realOper = dataMap.get( "real_oper" );
				}
				// 查看是否已经存在授权信息
				Map<String, Object> authInfoMap = getAuthInfoByQueryPkValue( queryPkvalue, mc );
				if( authInfoMap == null || authInfoMap.size() == 0 ) {// 不存在的话，生成授权信息和主表的授权记录信息
					Map<String, Object> authInfo = getAuthInfo( mc, dataValue, AuthStaticParameters.AUTH_STATUS_0 );
					aopCreateAuthInfo( authInfo );

					authId = MapUtil.getString( authInfo, "AUTH_ID" ); // 授权信息主键值

					Map<String, Object> authRecord = getAuthRecord( mc, AuthStaticParameters.IS_MAIN_TRUE, authId, dataValue, true );
					aopCreateAuthRecord( authRecord );
				}
				else { // 存在的话
					isNewAuth = false;

					// 查询主表记录
					String mainTableName = MapUtil.getString( authInfoMap, "QUERY_TABLE_NAME" );
					String mainTablePk = MapUtil.getString( authInfoMap, "QUERY_TABLE_PK" );
					String mainTablePkValue = MapUtil.getString( authInfoMap, "QUERY_PKVALUE" );
					// 查询正式表中数据
					List<Map<String, Object>> oldList = queryOldData( mainTableName, mainTablePk, mainTablePkValue );

					authId = MapUtil.getString( authInfoMap, "AUTH_ID" ); // 授权信息主键值

					// 是否为主表删除操作
					String mRealOper = mc.getRealOper();
					boolean flag = ("batch".equals( mRealOper ) && "d".equals( realOper )) || "d".equals( mRealOper );

					// 对启用停用判断 TODO

					// 对编辑做判断 TODO

					// 对删除操作进行处理
					if( flag && (oldList == null || oldList.size() == 0) ) {
						aopDeleteAuthInfo( authId, AuthStaticParameters.AUTH_STATUS_9 );
					}
					else {
						Map<String, Object> authRecord = getAuthRecord( mc, AuthStaticParameters.IS_MAIN_FALSE, authId, dataValue, false );
						String recordId = MapUtil.getString( authRecord, "RECORD_ID" );
						if( recordId != null && !"".equals( recordId ) ) { // 更新授权记录信息
							// 是否为子表删除
							boolean flag2 = ("subbatch".equals( mRealOper ) && "d".equals( realOper )) || "d".equals( mRealOper );
							if( flag2 ) {// TODO 是子表删除并且在正式库没数据
								aopdeleteAuthRecord( recordId );
								if( authId.length() > 0 ) {
									aopdeleteAuthInfo( recordId, authId );
								}
							}
							else {
								aopUpdateAuthRecord( authRecord );
							}
						}
						else { // 创建授权记录信息
							authRecord.put( "AUTH_ID", authId );
							aopCreateAuthRecord( authRecord );
						}
					}
				}

				// 将授权记录信息与操作日志创建关联
				String logId = StringUtil.parseToString( AuthDataBusUtil.get( "LOG_ID" ) );
				if( !StringUtil.isEmpty( logId ) && !StringUtil.isEmpty( authId ) ) {
					Map<String, Object> logMap = getAuthLog( isNewAuth, authId, logId );
					aopCreateAuthLog( logMap );
				}
			}

			if( !(mc.getId().equals( "saveRule" )) ) {
				logOrder = 1;
			}
		}
		else { // 不需要授权
				// 只生成授权信息，不添加授权记录信息
			for( int i = 0; i < dataList.size(); i++ ) {
				Object pkValue = dataList.get( i );
				Map<String, Object> authInfo = getAuthInfo( mc, pkValue, AuthStaticParameters.AUTH_STATUS_3 );
				aopCreateAuthInfo( authInfo );

				authId = MapUtil.getString( authInfo, "AUTH_ID" );

				// 刷新缓存
				if( "true".equals( mc.getIsRefresh() ) ) {
					refreshCache( authInfo, mc, null, null );
				}

				// 将授权记录信息与操作日志创建关联
				String logId = StringUtil.parseToString( AuthDataBusUtil.get( "LOG_ID" ) );
				if( !StringUtil.isEmpty( logId ) && !StringUtil.isEmpty( authId ) ) {
					Map<String, Object> logMap = getAuthLog( isNewAuth, authId, logId );
					aopCreateAuthLog( logMap );
				}
			}

			if( !(mc.getId().equals( "saveRule" )) ) {
				logOrder = 1;
			}
		}
	}

	/**
	 * 生成授权记录信息
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private Map<String, Object> getAuthRecord( MethodConfig mc, String isMain, String authId, Object pkValue, boolean queryMain ) {
		String tablePkValue = ""; // 表主键值
		String operateName = ""; // 操作功能
		String dbRealOper = ""; // 实际操作
		String operDataValue = ""; // 操作数据

		if( pkValue instanceof Map ) {
			// 表主键值
			Map dataMap = (Map) pkValue;
			tablePkValue = MapUtil.getString( dataMap, "table_pkvalue" );

			// 实际操作
			String mRealOper = mc.getRealOper();
			if( mRealOper.equals( "batch" ) || mRealOper.equals( "subbatch" ) ) {
				dbRealOper = MapUtil.getString( dataMap, "real_oper" );
			}
			else {
				operateName = mc.getName();
				dbRealOper = mRealOper;
			}

			// 获取操作数据
			dbRealOper = MapUtil.getString( dataMap, "real_oper" );
			if( "d".equals( dbRealOper ) ) {
				if( "USERID".equals( mc.getTablePk() ) ) {
					operDataValue = getUserPatternName( mc, dataMap );
				}
				else if( "RC_ID".equals( mc.getTablePk() ) ) {
					operDataValue = MapUtil.getString( dataMap, "oper_datavalue" );
				}
				else {
					operDataValue = getOperateData( mc, tablePkValue );
				}
			}
			else {
				operDataValue = MapUtil.getString( dataMap, "oper_datavalue" );
			}

			if( operDataValue == null || operDataValue.length() == 0 ) {
				operDataValue = MapUtil.getString( dataMap, "oper_datavalue" );
			}

			// 操作功能
			String txnType = MapUtil.getString( dataMap, "txntype" );
			operateName = getOperateName( txnType, mc, dbRealOper );
		}

		// 是否已存在授权记录信息
		Map<String, Object> authRecord = getAuthRecordByMc( mc, tablePkValue, queryMain );
		if( authRecord != null && authRecord.size() != 0 ) {
			long currentTimeMillis = System.currentTimeMillis();
			authRecord.put( "OPERATE_TIME", currentTimeMillis );
			String OPERATOR_ID = StringUtil.parseToString( AuthDataBusUtil.get( "OPERATOR_ID" ) );
			authRecord.put( "OPERATOR_ID", OPERATOR_ID );
			authRecord.put( "AUTH_ID", authId );

			if( mc.getModelId().equals( "rosterconvert" ) ) {
				authRecord.put( "OPERATE_NAME", operateName );
				authRecord.put( "IS_MAIN", isMain );
				authRecord.put( "TABLE_NAME", mc.getTableName() );
				authRecord.put( "TABLE_PK", mc.getTablePk() );
				authRecord.put( "TABLE_PKVALUE", tablePkValue );
				authRecord.put( "OPERATE_TIME", System.currentTimeMillis() );
				authRecord.put( "OPERATOR_ID", StringUtil.parseToString( AuthDataBusUtil.get( "OPERATOR_ID" ) ) );
				authRecord.put( "REAL_OPER", dbRealOper );
				authRecord.put( "OPERATEDATA_VALUE", operDataValue );
			}

			// 需要改动所属操作和操作类型
			String realOper = MapUtil.getString( authRecord, "REAL_OPER" );
			if( "u".equals( realOper ) && "d".equals( dbRealOper ) ) { // 编辑转删除
				authRecord.put( "OPERATE_NAME", operateName );
				authRecord.put( "REAL_OPER", dbRealOper );
			}
			else if( ("valid-y".equals( realOper ) || "valid-n".equals( realOper )) // 启或停用转编辑或删除
					&& ("u".equals( dbRealOper ) || "d".equals( dbRealOper )) ) {
				authRecord.put( "OPERATE_NAME", operateName );
				authRecord.put( "REAL_OPER", dbRealOper );
			} /*
				* else if(("c".equals(realOper) && "u".equals(dbRealOper))){// 新建转值转换 authRecord.put("OPERATE_NAME", operateName); authRecord.put("AUTH_ID", authId); authRecord.put("IS_MAIN", isMain); authRecord.put("TABLE_NAME",mc.getTableName()); authRecord.put("TABLE_PK", mc.getTablePk()); authRecord.put("TABLE_PKVALUE", tablePkValue); authRecord.put("OPERATE_TIME", System.currentTimeMillis()); authRecord.put("OPERATOR_ID", StringUtil.parseToString(AuthDataBusUtil.get("OPERATOR_ID"))); authRecord.put("REAL_OPER", dbRealOper); authRecord.put("OPERATEDATA_VALUE",
				* operDataValue); }
				*/
		}
		else {
			authRecord = new HashMap<String, Object>();
			authRecord.put( "OPERATE_NAME", operateName );
			authRecord.put( "AUTH_ID", authId );
			authRecord.put( "IS_MAIN", isMain );
			authRecord.put( "TABLE_NAME", mc.getTableName() );
			authRecord.put( "TABLE_PK", mc.getTablePk() );
			authRecord.put( "TABLE_PKVALUE", tablePkValue );
			long currentTimeMillis = System.currentTimeMillis();
			authRecord.put( "OPERATE_TIME", currentTimeMillis );
			String OPERATOR_ID = StringUtil.parseToString( AuthDataBusUtil.get( "OPERATOR_ID" ) );
			authRecord.put( "OPERATOR_ID", OPERATOR_ID );
			authRecord.put( "REAL_OPER", dbRealOper );
			authRecord.put( "OPERATEDATA_VALUE", operDataValue );
		}

		return authRecord;
	}

	@SuppressWarnings("rawtypes")
	private Map<String, Object> getAuthInfo( MethodConfig mc, Object pkValue, String authStatus ) {
		// 获取主键值
		String queryPkValue = ""; // 主表主键值
		String syncPkValue = ""; // 数据同步主键值
		String depPkValues = ""; // 依赖的主表主键值
		String txnId = ""; // 所属交易的主键值
		String operDataValue = ""; // 操作数据
		String origOperatename = ""; // 初始操作
		if( pkValue instanceof Map ) {
			Map dataMap = (Map) pkValue;
			queryPkValue = MapUtil.getString( dataMap, "query_pkvalue" );
			syncPkValue = MapUtil.getString( dataMap, "sync_pkvalue" );
			depPkValues = MapUtil.getString( dataMap, "dep_pkvalues" );
			txnId = MapUtil.getString( dataMap, "txn_id" );

			// 获取操作数据
			String tablePkvalue = MapUtil.getString( dataMap, "table_pkvalue" );
			String dbRealOper = MapUtil.getString( dataMap, "real_oper" );
			if( "d".equals( dbRealOper ) ) {
				if( "USERID".equals( mc.getTablePk() ) ) {
					operDataValue = getUserPatternName( mc, dataMap );
				}
				else if( "RC_ID".equals( mc.getTablePk() ) ) {
					operDataValue = MapUtil.getString( dataMap, "oper_datavalue" );
				}
				else {
					operDataValue = getOperateData( mc, tablePkvalue );
				}
			}
			else {
				operDataValue = MapUtil.getString( dataMap, "oper_datavalue" );
			}

			if( operDataValue == null || operDataValue.length() == 0 ) {
				operDataValue = MapUtil.getString( dataMap, "oper_datavalue" );
			}

			// 初始操作
			String txnType = MapUtil.getString( dataMap, "txn_type" );
			origOperatename = getOperateName( txnType, mc, dbRealOper );
		}

		// 构造授权信息
		Map<String, Object> auth = new HashMap<String, Object>();

		String OPERATOR_ID = StringUtil.parseToString( AuthDataBusUtil.get( "OPERATOR_ID" ) );
		auth.put( "METHOD_ID", mc.getId() );
		auth.put( "PROPOSER_ID", OPERATOR_ID );
		auth.put( "TXN_ID", txnId );
		auth.put( "AUTH_STATUS", authStatus );
		auth.put( "PROPOSER_TIME", System.currentTimeMillis() );
		auth.put( "QUERY_PKVALUE", queryPkValue );
		auth.put( "SYNC_PKVALUE", syncPkValue );
		auth.put( "DEP_PKVALUES", depPkValues );
		auth.put( "MODULE_ID", mc.getModelId() );
		auth.put( "MODULE_NAME", mc.getModelName() );
		auth.put( "QUERY_TABLE_NAME", mc.getQueryTableName() );
		auth.put( "QUERY_TABLE_PK", mc.getQueryTablePk() );
		auth.put( "ORIG_OPERATENAME", origOperatename );
		auth.put( "DEP_MODULES", mc.getDepModels() );

		auth.put( "OPERATEDATA_VALUE", operDataValue );

		return auth;
	}

	/**
	 * 获取操作名称
	 * 
	 * @param mc
	 * @param dbRealOper
	 * @return
	 */
	private String getOperateName( String txnType, MethodConfig mc, String dbRealOper ) {
		String mRealOper = mc.getRealOper();
		String operatename = "";
		if( mRealOper.equals( "batch" ) || mRealOper.equals( "subbatch" ) ) {
			String[] operateNames = mc.getName().split( "," );
			if( "saveTranDef".equals( mc.getId() ) && "0".equals( txnType ) ) { // 区分交易和交易组
				if( "c".equals( dbRealOper ) ) {
					operatename = operateNames[3];
				}
				else if( "u".equals( dbRealOper ) ) {
					operatename = operateNames[4];
				}
				else if( "d".equals( dbRealOper ) ) {
					operatename = operateNames[5];
				}
			}
			else {
				if( "c".equals( dbRealOper ) ) {
					operatename = operateNames[0];
				}
				else if( "u".equals( dbRealOper ) ) {
					operatename = operateNames[1];
				}
				else if( "d".equals( dbRealOper ) ) {
					operatename = operateNames[2];
				}
				else if( "valid-y".equals( dbRealOper ) ) {
					operatename = operateNames[3];
				}
				else if( "valid-n".equals( dbRealOper ) ) {
					operatename = operateNames[4];
				}
				else if( "p".equals( dbRealOper ) ) {
					operatename = operateNames[5];
				}
			}

		}
		else {
			operatename = mc.getName();
		}
		return operatename;
	}

	/**
	 * 申请授权
	 * 
	 * @param args
	 * @param methodName
	 */

	@Transactional("tmp")
	public void doApply( Object[] args, String methodName ) {
		MethodConfig mc = ParseCacheConfigXml.getInstance().getMethod( methodName );
		if( mc == null ) {
			return;
		}

		// 如果是保存规则，需要为规则和路由分别生成授权信息
		/*
		 * if("saveRule".equals(mc.getId())){ //将所属交易的Id放入到保存规则的参数中 String txnId = (String)(args[2]); Map ruleMap = (Map)(args[0]); putTxnIdToRuleOrRuleChild(txnId,ruleMap,"del");
		 * 
		 * Map childMap = (Map)(args[1]); putTxnIdToRuleOrRuleChild(txnId,childMap,"add","del");
		 * 
		 * doApplyByMc(args[0], mc); //为规则生成授权信息
		 * 
		 * MethodConfig mc2 = ParseCacheConfigXml.getInstance().getMethod("saveChild"); doApplyByMc(args[1], mc2); //为路由生成授权信息 } else {
		 */// 不是保存规则
		doApplyByMc( args[0], mc );
		// }
	}

	/**
	 * 将所属交易放到规则和规则路由的service参数中
	 * 
	 * @param txnId
	 * @param ruleMap
	 * @param opers
	 */
	@SuppressWarnings({
			"unchecked", "rawtypes"
	})
	private void putTxnIdToRuleOrRuleChild( String txnId, Map ruleMap, String... opers ) {
		for( String oper : opers ) {
			List<Map<String, Object>> ruleList = (List<Map<String, Object>>) (ruleMap.get( oper ));
			if( ruleList != null ) {
				for( Map<String, Object> map : ruleList ) {
					map.put( "RULE_TXN", txnId );
				}
			}
		}
	}

	/**
	 * 执行授权
	 * 
	 * @param authIds
	 * @param refuseInfo
	 * @param authorStatus
	 */
	@Transactional("tmp")
	public void doAuth( Object[] args ) {
		Map<String, String> arg = (Map<String, String>) args[0];
		String authIds = MapUtil.getString( arg, "AUTH_IDS" ); // 授权信息Id，多个Id用逗号隔开

		if( !StringUtil.isEmpty( authIds ) ) {
			String[] authIdArr = authIds.split( "," );

			List<Map<String, Object>> rosterList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> rosterValueList = new ArrayList<Map<String, Object>>();

			// 遍历，对每条授权信息授权
			for( String authId : authIdArr ) {
				// 获取授权信息
				Map<String, Object> authInfo = getAuthInfoById( authId );
				String methodName = MapUtil.getString( authInfo, "METHOD_ID" );
				MethodConfig mc = ParseCacheConfigXml.getInstance().getMethod( methodName );
				String modelId = mc.getModelId();
				String syncPkValue = MapUtil.getString( authInfo, "SYNC_PKVALUE" );
				String authStatus = StringUtil.parseToString( arg.get( "AUTH_STATUS" ) );

				if( authStatus.equals( AuthStaticParameters.AUTH_STATUS_1 ) ) { // 授权通过
					// 进行数据更新，从临时表更新到正式表
					dataOperCenter.operData( modelId, ConfigAttrabute.METHOD_DATASYNC, syncPkValue, AuthStaticParameters.AUTH_STATUS_1 );

					// 刷新缓存
					if( "true".equals( mc.getIsRefresh() ) ) {
						refreshCache( authInfo, mc, rosterList, rosterValueList );
					}
				}
				else {
					// 进行数据更新，从正式表更新到临时表
					dataOperCenter.operData( modelId, ConfigAttrabute.METHOD_DATASYNC, syncPkValue, AuthStaticParameters.AUTH_STATUS_2 );

					// TODO：通知依赖于当前模块的模块进行数据同步
				}

				authInfo.put( "AUTH_STATUS", authStatus );
				authInfo.put( "AUTHOR_ID", StringUtil.parseToString( AuthDataBusUtil.get( "OPERATOR_ID" ) ) );
				authInfo.put( "AUTHOR_TIME", System.currentTimeMillis() );
				String authMsg = StringUtil.parseToString( arg.get( "AUTH_MSG" ) );
				authInfo.put( "AUTH_MSG", authMsg );
				aopUpdateAuthInfo( authInfo );

				// 将授权信息与操作日志创建关联
				String logId = StringUtil.parseToString( AuthDataBusUtil.get( "LOG_ID" ) );
				if( !StringUtil.isEmpty( logId ) && !StringUtil.isEmpty( authId ) ) {
					Map<String, Object> logMap = getAuthLog( false, authId, logId );
					aopCreateAuthLog( logMap );
				}

				// 更新授权信息，删除授权记录信息
				aopDeleteAuthInfo( authId, authStatus );
			}

			// 刷新名单或名单值
			refreshNameListCache( rosterList, rosterValueList );

		}
	}

	/**
	 * 根据授权ID获取所有的名单值内容
	 * 
	 * @param authId
	 * @return List
	 */
	private List<Map<String, Object>> getRosterValueListByAuthId( String authId, Map<String, Object> authInfo, String refreshService ) {
		List<Map<String, Object>> _listMap = new ArrayList<Map<String, Object>>();

		String sql = "select table_pkvalue,real_oper from TMS_MGR_AUTHRECORD where table_pk = 'ROSTERVALUEID' and AUTH_ID = ? ";

		List<Map<String, Object>> listMap = offlineSimpleDao.queryForList( sql, authId );

		String rosterId = (String) authInfo.get( "QUERY_PKVALUE" );

		for( int i = 0; i < listMap.size(); i++ ) {
			Map<String, Object> _map = new HashMap<String, Object>();
			String id = (String) listMap.get( i ).get( "table_pkvalue" );

			Map<String, Object> _rosterValueMap = nameListService.selectByValueId( id );
			_map.put( "type", "TMS_MGR_ROSTERVALUE" );

			_map.put( "rosterId", rosterId );
			_map.put( "authInfo", authInfo );
			_map.put( "operatevalue", _rosterValueMap.get( "rostervalue" ) );
			_map.put( "enabletime", _rosterValueMap.get( "enabletime" ) );
			_map.put( "disabletime", _rosterValueMap.get( "disabletime" ) );
			_map.put( "id", id );
			_map.put( "method", listMap.get( i ).get( "real_oper" ) );
			// _map.put("AUTH_ID", authId);
			_map.put( "refreshService", refreshService );

			_listMap.add( _map );
		}

		return _listMap;
	}

	/**
	 * 根据授权ID获取所有的名单内容
	 * 
	 * @param authId
	 * @return List
	 */
	private List<Map<String, Object>> getRosterListByAuthId( String authId, Map<String, Object> authInfo, String refreshService ) {
		List<Map<String, Object>> _listMap = new ArrayList<Map<String, Object>>();
		String sql = "select table_pkvalue,real_oper from TMS_MGR_AUTHRECORD where table_pk = 'ROSTERID' and AUTH_ID = ? ";

		List<Map<String, Object>> listMap = offlineSimpleDao.queryForList( sql, authId );

		for( int i = 0; i < listMap.size(); i++ ) {
			Map<String, Object> _map = new HashMap<String, Object>();
			String id = (String) listMap.get( i ).get( "table_pkvalue" );

			Map<String, Object> _rosterMap = nameListService.selectById( id );
			_map.put( "type", "TMS_MGR_ROSTER" );
			_map.put( "authInfo", authInfo );// 授权信息
			_map.put( "rostername", _rosterMap.get( "rostername" ) );
			_map.put( "datatype", _rosterMap.get( "datatype" ) );
			_map.put( "iscache", _rosterMap.get( "iscache" ) );

			_map.put( "id", id );
			_map.put( "method", listMap.get( i ).get( "real_oper" ) );
			_map.put( "refreshService", refreshService );
			_listMap.add( _map );
		}

		return _listMap;
	}

	/**
	 * @param authId
	 * @param operateName
	 * @param logId
	 * @return
	 */
	private Map<String, Object> getAuthLog( boolean isNewAuth, String authId, String logId ) {
		Map<String, Object> logMap = new HashMap<String, Object>();
		if( isNewAuth ) {
			logMap.put( "LOG_ORDER", String.valueOf( logOrder++ ) );
		}
		else {
			Integer maxLogOrder = getNextLogOrder( authId );
			logMap.put( "LOG_ORDER", String.valueOf( maxLogOrder ) );
		}
		logMap.put( "AUTH_ID", authId );
		logMap.put( "LOG_ID", logId );
		return logMap;
	}

	/**
	 * 获取主键值
	 * 
	 * @param
	 * @param
	 * @return
	 */
	@SuppressWarnings("all")
	private List parseArgByMc( Object arg, MethodConfig mc ) {
		String tablePk = mc.getTablePk();
		String queryPk = mc.getQueryTablePk();
		String syncPk = mc.getSyncPk();
		String depPks = mc.getDepPks();
		String txnId = mc.getTxnId();
		String operateDataField = mc.getOperateData();

		List dataList = new ArrayList();
		if( arg instanceof Map ) {
			Map mapArg = (Map) arg;
			Object add = mapArg.get( "add" );
			Object copy = mapArg.get( "copy" );
			Object mod = mapArg.get( "mod" );
			Object del = mapArg.get( "del" );
			Object valid_y = mapArg.get( "valid-y" );
			Object valid_n = mapArg.get( "valid-n" );

			if( add instanceof List || mod instanceof List || del instanceof List || valid_y instanceof List || valid_n instanceof List || copy instanceof List ) { // 如果是批量操作的Map
				if( add != null ) {
					for( Map<String, Object> data : (List<Map<String, Object>>) add ) {
						Map<String, String> addMap = generateDataMap( operateDataField, tablePk, queryPk, syncPk, depPks, txnId, data, "c" );
						dataList.add( addMap );
					}
				}
				if( copy != null ) {
					for( Map<String, Object> data : (List<Map<String, Object>>) copy ) {
						Map<String, String> copyMap = generateDataMap( operateDataField, tablePk, queryPk, syncPk, depPks, txnId, data, "p" );
						dataList.add( copyMap );
					}
				}
				if( mod != null ) {
					for( Map<String, Object> data : (List<Map<String, Object>>) mod ) {
						Map<String, String> modMap = generateDataMap( operateDataField, tablePk, queryPk, syncPk, depPks, txnId, data, "u" );
						dataList.add( modMap );
					}
				}
				if( del != null ) {
					for( Map<String, Object> data : (List<Map<String, Object>>) del ) {
						Map<String, String> delMap = generateDataMap( operateDataField, tablePk, queryPk, syncPk, depPks, txnId, data, "d" );
						dataList.add( delMap );
					}
				}
				if( valid_y != null ) {
					for( Map<String, Object> data : (List<Map<String, Object>>) valid_y ) {
						Map<String, String> yMap = generateDataMap( operateDataField, tablePk, queryPk, syncPk, depPks, txnId, data, "valid-y" );
						dataList.add( yMap );
					}
				}
				if( valid_n != null ) {
					for( Map<String, Object> data : (List<Map<String, Object>>) valid_n ) {
						Map<String, String> nMap = generateDataMap( operateDataField, tablePk, queryPk, syncPk, depPks, txnId, data, "valid-n" );
						dataList.add( nMap );
					}
				}
			}
			else {
				if( mapArg.get( queryPk ) != null && !"".equals( mapArg.get( queryPk ) ) ) {
					String realOper = mc.getRealOper();
					Map<String, String> normalMap = generateDataMap( operateDataField, tablePk, queryPk, syncPk, depPks, txnId, mapArg, realOper );
					dataList.add( normalMap );
				}
			}
		}

		return dataList;
	}

	/**
	 * 产生由各个主键值组成的Map
	 * 
	 * @return
	 */
	private Map<String, String> generateDataMap( String operateDataField, String tablePk, String queryPk, String syncPk, String depPks, String txnIdField, Map mapArg,
			String realOper ) {
		Map<String, String> dataMap = new CaseInsensitiveMap();
		dataMap.put( "real_oper", realOper );

		String depPkValues = getSeparatePkValues( depPks, mapArg );
		dataMap.put( "dep_pkvalues", depPkValues );
		dataMap.put( "txn_type", MapUtil.getString( mapArg, "txn_type" ) );

		String operDataValue = "";
		String syncPkValue = "";
		String txnid = "";
		String queryPkValue = "";
		String tablePkValue = "";
		if( "USERID".equals( tablePk ) ) { // 如果是用户行为习惯管理
			String userId = MapUtil.getString( mapArg, "user_id" );
			String statId = MapUtil.getString( mapArg, "stat_id" );
			String patternId = MapUtil.getString( mapArg, "up_id" );
			syncPkValue = userId + "," + statId + "," + patternId;
			dataMap.put( "userid", userId );
			dataMap.put( "statid", statId );
			dataMap.put( "patternid", patternId );
			String patternValue = MapUtil.getString( mapArg, "up_text" );

			String[] tmpArr = getTmpUserPatternName( userId, statId, patternValue );
			operDataValue = tmpArr[1];
			txnid = tmpArr[0];

			queryPkValue = syncPkValue;
			tablePkValue = syncPkValue;
		}
		else {
			if( "RC_ID".equals( tablePk ) ) { // 如果是规则路由
				operDataValue = getRuleChildName( mapArg );
			}
			else {
				operDataValue = MapUtil.getString( mapArg, operateDataField );
			}
			syncPkValue = MapUtil.getString( mapArg, syncPk );
			txnid = MapUtil.getString( mapArg, txnIdField );
			queryPkValue = MapUtil.getString( mapArg, queryPk );
			tablePkValue = getSeparatePkValues( tablePk, mapArg );
		}
		dataMap.put( "table_pkvalue", tablePkValue );
		dataMap.put( "query_pkvalue", queryPkValue );
		dataMap.put( "oper_datavalue", operDataValue );
		dataMap.put( "sync_pkvalue", syncPkValue );
		dataMap.put( "txn_id", txnid );

		return dataMap;
	}

	/**
	 * 获取包含逗号分隔的主键值
	 * 
	 * @param pks
	 * @param mapArg
	 * @return
	 */
	private String getSeparatePkValues( String pks, Map mapArg ) {
		String pkValues = "";
		if( pks.contains( "," ) ) {
			String[] depPkArr = pks.split( "," );
			for( String depPk : depPkArr ) {
				String depPkValue = MapUtil.getString( mapArg, depPk );
				pkValues += "," + depPkValue;
			}
			pkValues = !StringUtil.isEmpty( pkValues ) ? pkValues.substring( 1 ) : "";
		}
		else {
			pkValues = MapUtil.getString( mapArg, pks );
		}
		return pkValues;
	}

	/**
	 * 刷新缓存，单个授权记录
	 * 
	 * @param authInfo
	 * @param mc
	 */
	private void refreshCache( Map<String, Object> authInfo, MethodConfig mc, List<Map<String, Object>> rosterList, List<Map<String, Object>> rosterValueList ) {
		String refreshService = mc.getCacheRefreshService();
		String authId = MapUtil.getString( authInfo, "AUTH_ID" );

		// 名单和名单值缓存刷新要进行特殊处理
		if( "TMS_MGR_ROSTER".equals( mc.getTableName() ) ) { // 是对名单的操作
			rosterList.addAll( getRosterListByAuthId( authId, authInfo, refreshService ) );// 刷新名单
			List<Map<String, Object>> authRecordList = getNoneMainAuthRecordByAuthId( authId );// 查询名单相关联的名单值的授权记录信息
			if( authRecordList != null && !authRecordList.isEmpty() ) { // 如果存在，添加到rosterValueList
				rosterValueList.addAll( getRosterValueListByAuthId( authId, authInfo, refreshService ) );
			}
		}
		else if( "TMS_MGR_ROSTERVALUE".equals( mc.getTableName() ) ) { // 是对名单值的操作，直接刷新

			rosterValueList.addAll( getRosterValueListByAuthId( authId, authInfo, refreshService ) );
			List<Map<String, Object>> authRecordList = getNoneMainAuthRecordByAuthId2( authId );// 查询名单值相关联的名单的授权记录信息
			if( authRecordList != null && !authRecordList.isEmpty() ) {// 如果存在，添加到rosterList
				rosterList.addAll( getRosterListByAuthId( authId, authInfo, refreshService ) );
			}

		}
		else if( "TMS_COM_USERPATTERN".equals( mc.getTableName() ) ) {
			refreshUserPattern( authInfo, refreshService );
		}
		else {
			String msg = mc.getRefreshMsg();
			if( !StringUtil.isBlank( msg ) && msg.indexOf( "TMS_COM" ) != -1 ) {
				// 交易模型相关修改, 变更版本号和更新时间
				String paramName = "transModelVersion";
				onlineSimpleDao.executeUpdate( "update TMS_MGR_SYSPARAM set STARTVALUE=STARTVALUE+1 where SYSPARAMNAME=?", paramName );
			}
			refreshCacheExecutor.execute( new RefreshCacheTask( refreshService, mc.getRefreshMsg(), authInfo ) );
		}

	}

	/**
	 * 刷新缓存，名单和名单值O
	 * 
	 * @param authInfo
	 * @param mc
	 */
	private void refreshNameListCache( List<Map<String, Object>> rosterList, List<Map<String, Object>> rosterValueList ) {
		String refreshService1 = null;
		String refreshService2 = null;
		for( int i = 0; i < rosterList.size(); i++ ) {
			Map<String, Object> map = rosterList.get( i );
			refreshService1 = (String) map.get( "refreshService" );
			break;
		}

		for( int i = 0; i < rosterValueList.size(); i++ ) {
			Map<String, Object> map = rosterValueList.get( i );
			refreshService2 = (String) map.get( "refreshService" );
			break;
		}

		String refreshService = refreshService1 == null ? refreshService2 : refreshService1;

		refreshRosterValue( rosterList, rosterValueList, refreshService );

	}

	/**
	 * 刷新用户行为模式的缓存
	 * 
	 * @param authInfo
	 * @param refreshService
	 */
	private void refreshUserPattern( Map<String, Object> authInfo, String refreshService ) {
		String syncPkvalue = MapUtil.getString( authInfo, "SYNC_PKVALUE" );
		String[] split = syncPkvalue.split( "," );
		if( split.length == 3 ) {
			String userId = split[0];
			String refreshMsg = "TMS_COM_USERPATTERN," + userId;
			refreshCacheExecutor.execute( new RefreshCacheTask( refreshService, refreshMsg, authInfo ) );
		}
	}

	/**
	 * 刷新名单值的缓存
	 * 
	 * @param authInfo
	 * @param refreshService
	 */
	private void refreshRosterValue( List<Map<String, Object>> rosterList, List<Map<String, Object>> rosterValueList, String refreshService ) {
		// //对名单值转换进行特殊处理
		// String syncPkValue = MapUtil.getString(authInfo, "SYNC_PKVALUE");
		// String[] vArr = syncPkValue.split(",");
		//
		// for (String rosterId : vArr) {
		// if(!StringUtil.isEmpty(rosterId)){
		// String refreshMsg2 = "TMS_MGR_ROSTERVALUE,"+rosterId;
		// refreshCacheExecutor.execute(new RefreshCacheTask(refreshService, refreshMsg2, authInfo));
		// }
		// }
		refreshCacheExecutor.execute( new RefreshNameListCacheTask( refreshService, rosterList, rosterValueList ) );

	}

	class RefreshCacheTask implements Runnable {
		private String refreshService;
		private String msgArr;
		private Map<String, Object> authInfo;

		public RefreshCacheTask( String refreshService, String msgArr, Map<String, Object> authInfo ) {
			this.refreshService = refreshService;
			this.msgArr = msgArr;
			this.authInfo = authInfo;
		}

		public void run() {
			String refreshInfo = getApplicationContext().getBean( refreshService, CacheRefresh.class ).refresh( msgArr );

			if( StringUtil.isEmpty( refreshInfo ) ) { // 如果返回信息为空，表示缓存刷新成功
				authInfo.put( "REFRESH_STATUS", "1" );
				authInfo.put( "REFRESH_INFO", "缓存刷新成功" );
				aopUpdateAuthInfo( authInfo );
			}
			else { // 如果返回信息不为空，表示缓存刷新失败
				authInfo.put( "REFRESH_STATUS", "0" );
				authInfo.put( "REFRESH_INFO", refreshInfo );
				aopUpdateAuthInfo( authInfo );
			}
		}
	}

	// 刷新名单和名单值任务
	class RefreshNameListCacheTask implements Runnable {
		private String refreshService;
		private List<Map<String, Object>> rosterList;
		private List<Map<String, Object>> rosterValueList;

		public RefreshNameListCacheTask( String refreshService, List<Map<String, Object>> rosterList, List<Map<String, Object>> rosterValueList ) {
			this.refreshService = refreshService;
			this.rosterList = rosterList;
			this.rosterValueList = rosterValueList;
		}

		public void run() {
			//防止出现Exception in thread "refreshCacheTaskExecutor2" java.lang.IllegalArgumentException: 'name' must not be null错误
			if( null == refreshService ) {
				log.debug( "RefreshNameListCacheTask refreshService为空，不执行名单刷新！" );
				return;
			}

			Set<Map<String, Object>> authInfoSet = getApplicationContext().getBean( refreshService, CacheRefresh.class ).refreshNameList( rosterList, rosterValueList );

			if( authInfoSet == null || authInfoSet.isEmpty() ) return;

			// 更新授权信息
			for( Map<String, Object> authInfo : authInfoSet ) {
				String refreshInfo = (String) authInfo.get( "REFRESH_INFO" );
				if( StringUtil.isEmpty( refreshInfo ) ) {// 如果返回信息为空，表示缓存刷新成功
					authInfo.put( "REFRESH_STATUS", "1" );
					authInfo.put( "REFRESH_INFO", "缓存刷新成功" );
					aopUpdateAuthInfo( authInfo );
				}
				else { // 如果返回信息不为空，表示缓存刷新失败
					try {
						byte[] bs = refreshInfo.getBytes( "UTF-8" );
						if( bs.length > 2048 ) {// 如果授权信息超过数据库字段长度，截取最后一个报错信息保存数据库
							refreshInfo = refreshInfo.substring( refreshInfo.lastIndexOf( ";" ) + 1 );
							authInfo.put( "REFRESH_STATUS", "0" );
							authInfo.put( "REFRESH_INFO", refreshInfo );
							aopUpdateAuthInfo( authInfo );
						}
					}
					catch( Exception e ) {
						log.error( "REFRESH_INFO字段超长，auth_id=[" + authInfo.get( "AUTH_ID" ) + "]refreshInfo=[" + refreshInfo + "]" );
					}
					authInfo.put( "REFRESH_STATUS", "0" );
					authInfo.put( "REFRESH_INFO", refreshInfo );
					aopUpdateAuthInfo( authInfo );
				}
			}
		}
	}
}
