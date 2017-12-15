package cn.com.higinet.tms.manager.modules.datamgr.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.modules.auth.exception.TmsMgrAuthDataSyncException;
import cn.com.higinet.tms.manager.modules.common.SqlWhereHelper;
import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.datamgr.common.ConfigAttrabute;
import cn.com.higinet.tms.manager.modules.datamgr.common.DataConfig;

@Service("commonTableOperService")
public class CommonTableOperService implements TableOperService {

	private final static Log logger = LogFactory.getLog( CommonTableOperService.class );
	@Autowired
	private SimpleDao officialSimpleDao;

	@Autowired
	@Qualifier("tmpSimpleDao")
	private SimpleDao tmpSimpleDao;

	@Autowired
	private SqlMap tmsSqlMap;

	public List<Map<String, Object>> queryDataList( DataConfig tableConfig, DataConfig dataconfig, String authFlag ) {

		String tableName = tableConfig.getAttrabute( ConfigAttrabute.TABLE_NAME );
		String pk = tableConfig.getAttrabute( ConfigAttrabute.TABLE_PK );

		String sqlType = tableConfig.getAttrabute( ConfigAttrabute.SQL_TYPE );
		String sqlId = tableConfig.getAttrabute( ConfigAttrabute.SQL_ID );
		if( ConfigAttrabute.SQL_TYPE_VALUE_SQL.equals( sqlType ) ) {
			sqlId = tmsSqlMap.getSql( sqlId );
		}
		else {
			sqlId = "SELECT * FROM " + tableName;
		}

		if( StringUtil.isEmpty( sqlId ) ) {
			logger.error( tableConfig.getAttrabute( ConfigAttrabute.TABLE_NAME ) + "没有相应的数据查询语句。" );
			return null;
		}

		String whereSql = "";
		//filter相应配置组成的查询条件
		String filterWhere = getFilterCondition( tableConfig, dataconfig );
		if( !StringUtil.isEmpty( filterWhere ) ) {
			whereSql += "(" + filterWhere + ")";
		}

		//reference 配置对应的值
		String referWhere = getReferenceCondition( tableConfig, dataconfig );
		if( !StringUtil.isEmpty( referWhere ) ) {
			if( !StringUtil.isEmpty( whereSql ) ) {
				whereSql += " and ";
			}
			whereSql += tableConfig.getParentReferenceFk() + " in (" + referWhere + ")";
		}

		if( !StringUtil.isEmpty( whereSql.trim() ) ) {
			sqlId = getWhereSql( sqlId );
		}
		else {//如果没有查询条件：1、如果是主表，可能没有配置相关过滤条件；2、如果是子表，可能其父表的记录为空；
			return null;
		}
		String sql = sqlId + whereSql;

		List<Map<String, Object>> dataList = null;

		if( StaticParameters.AUTH_STATUS_1.equals( authFlag ) ) {
			//如果授权通过查询新数据，用于更新旧(正式表)数据
			dataList = tmpSimpleDao.queryForList( sql );
			syncDataNewToOld( dataList, tableName, whereSql );
		}
		else if( StaticParameters.AUTH_STATUS_2.equals( authFlag ) ) {

			//如果授权未通过查询旧数据，用于将新(临时表)数据覆盖
			//先查询正式表
			dataList = officialSimpleDao.queryForList( sql );
			if( dataList != null && !dataList.isEmpty() ) {
				//如果存在记录那么将新(临时表)数据覆盖
				syncDataOldToNew( dataList, tableName, whereSql );
			}
			else {
				//如果不存在记录，如果是主表说明新数据是新增的，则将其状态改为删除；如果是从表则不能配置状态字段
				if( !StringUtil.isEmpty( tableConfig.getStatusKey() ) ) {
					List<Map<String, Object>> newDataList = tmpSimpleDao.queryForList( sql );
					for( int i = 0; i < newDataList.size(); i++ ) {
						Map<String, Object> data = newDataList.get( i );
						data.put( tableConfig.getStatusKey(), ConfigAttrabute.STATUSDEL );
						Map<String, String> conds = new HashMap<String, String>();
						conds.put( pk, StringUtil.parseToString( data.get( pk ) ) );
						tmpSimpleDao.update( tableName, data, conds );
					}
				}
				else {
					syncDataOldToNew( dataList, tableName, whereSql );
				}
			}
		}

		//保存当前记录集的查询条件
		Map<String, String> whereMap = dataconfig.getWhereMap();
		if( whereMap == null ) whereMap = new HashMap<String, String>();
		whereMap.put( tableName, whereSql );
		dataconfig.setWhereMap( whereMap );

		//保存当前记录集数据
		Map<String, List<Map<String, Object>>> operData = dataconfig.getOperData();
		if( operData == null ) operData = new HashMap<String, List<Map<String, Object>>>();
		operData.put( tableName, dataList );
		dataconfig.setOperData( operData );

		return dataList;
	}

	/**
	 * 获得filter配置对应的查询条件
	 * @param tableConfig
	 * @param dataconfig
	 * @return
	 */
	private String getFilterCondition( DataConfig tableConfig, DataConfig dataconfig ) {
		String whereSql = "";

		Map<String, Object> dataValues = dataconfig.getDataValues();

		List filterList = tableConfig.getFilters();
		if( filterList != null && !filterList.isEmpty() ) {
			for( int i = 0; i < filterList.size(); i++ ) {
				DataConfig filter = (DataConfig) filterList.get( i );
				String filterType = filter.getAttrabute( ConfigAttrabute.TYPE );
				String paramName = filter.getAttrabute( ConfigAttrabute.PARAMETER_NAME );
				String paramValue = filter.getAttrabute( ConfigAttrabute.PARAMETER_VALUE );

				String filterValue = "";

				if( ConfigAttrabute.FILTER_TYPE_VALUE_DEFAULT.equals( filterType ) ) {
					dataValues.put( paramName, dataconfig.getPkValue() );
					filterValue = dataconfig.getPkValue();
				}
				else if( ConfigAttrabute.FILTER_TYPE_VALUE_FK.equals( filterType ) ) {
					dataValues.put( paramName, StringUtil.parseToString( dataValues.get( paramValue ) ) );
					filterValue = StringUtil.parseToString( dataValues.get( paramValue ) );
				}
				else if( ConfigAttrabute.FILTER_TYPE_VALUE_CUSTOM.equals( filterType ) ) {
					//TODO 自定义方法的实现
					//					String customClass = filter.getAttrabute(ConfigAttrabute.CUSTOM_CLASS);
					//					String customFunc = filter.getAttrabute(ConfigAttrabute.CUSTOM_FUNC);
					//					Object classObject = ClassFactory.getClass(customClass);
					//					
					//					String value = "";
					//					dataValues.put(paramName, value);
					//					filterValue = value;
				}
				else {
					dataValues.put( paramName, paramValue );
					filterValue = paramValue;
				}
				dataconfig.setDataValues( dataValues );

				if( StringUtil.isEmpty( filterValue ) ) return whereSql;
				//将filter的值拼装条件
				String[] filters = filterValue.split( ConfigAttrabute.REFERENCE_APLIT );
				if( filters != null && filters.length > 0 ) {
					String filterSql = "";
					int count = 0;
					for( String fstr : filters ) {
						if( count > 0 ) {
							whereSql += " or ";
						}
						filterSql = "'" + fstr + "'";
						whereSql += paramName + "=" + filterSql;
						count++;
					}
				}
			}
		}
		return whereSql;
	}

	/**
	 * 拼装renference配置对应的条件
	 * @param tableConfig
	 * @param dataconfig
	 * @return
	 */
	private String getReferenceCondition( DataConfig tableConfig, DataConfig dataconfig ) {
		String referenceSql = "";
		String referenceFk = tableConfig.getParentReferenceFk();
		String referenceSqlValue = "";
		if( !StringUtil.isEmpty( referenceFk ) ) {//如果当前层配置有上一层的reference的配置
			referenceSqlValue = StringUtil.parseToString( dataconfig.getDataValues().get( referenceFk ) );

			if( !StringUtil.isEmpty( referenceSqlValue ) ) {
				String[] references = referenceSqlValue.split( ConfigAttrabute.REFERENCE_APLIT );
				if( references != null && references.length > 0 ) {
					referenceSql = SqlWhereHelper.getInWhere( references );
				}
			}
		}
		return referenceSql;
	}

	private String getWhereSql( String whereSql ) {
		if( whereSql.indexOf( "where" ) > -1 || whereSql.indexOf( "WHERE" ) > -1 ) {
			return whereSql;
		}
		else {
			return whereSql += " where ";
		}
	}

	/**
	 * 授权通过，将要操作的数据从临时表同步到正式表
	 * @param dataconfig
	 * @param authFlag
	 */
	@Transactional
	private void syncDataNewToOld( List<Map<String, Object>> dataList, String tableName, String whereSql ) {
		try {
			String delSql = "delete from " + tableName + " where " + whereSql;
			int i = officialSimpleDao.executeUpdate( delSql );
			if( i > -1 ) {
				if( dataList != null && dataList.size() > 0 ) {
					for( Map<String, Object> data : dataList ) {
						officialSimpleDao.create( tableName, data );
					}
				}
			}
		}
		catch( Exception e ) {
			e.printStackTrace();
			throw new TmsMgrAuthDataSyncException( "未能成功将临时数据同步到正式库，原因：" + e.getMessage() );
		}
	}

	/**
	 * 对原有数据进行编辑后，没有通过授权
	 * @param dataList
	 * @param tableName
	 * @param pk
	 */
	@Transactional("syncOldtoNew")
	private void syncDataOldToNew( List<Map<String, Object>> dataList, String tableName, String whereSql ) {
		if( dataList != null ) {
			try {
				String delSql = "delete from " + tableName + " where " + whereSql;
				int i = tmpSimpleDao.executeUpdate( delSql );
				if( i > -1 ) {
					for( Map<String, Object> data : dataList ) {
						tmpSimpleDao.create( tableName, data );
					}
				}
			}
			catch( Exception e ) {
				e.printStackTrace();
				throw new TmsMgrAuthDataSyncException( "未能成功将正式数据同步到临时库，原因：" + e.getMessage() );
			}
		}
	}
}
