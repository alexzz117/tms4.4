package cn.com.higinet.tms.manager.modules.aml.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.common.ApplicationContextUtil;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.modules.aml.common.AMLConstant;
import cn.com.higinet.tms.manager.modules.aml.common.AMLDataConfig;
import cn.com.higinet.tms.manager.modules.aml.common.AMLMessage;
import cn.com.higinet.tms.manager.modules.aml.function.Function;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_AML_CONFIG;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_AML_MESSAGE;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_FD;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.common.util.XMLUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;

/**
 * 反洗钱数据管理服务类
 * @author wuc
 * @date 2015-1-27
 * 
 * @author zhang.lei
 */
@Service("antiMoneyLaunderingService")
@Transactional
public class AntiMoneyLaunderingService {

	private static final Logger log = LoggerFactory.getLogger( AntiMoneyLaunderingService.class );

	private static Pattern checkQueryParam = Pattern.compile( "(=\\s*:\\s*([\\w$\\.]+))", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE );
	@Autowired
	@Qualifier("tmsSimpleDao")
	private SimpleDao tmsSimpleDao;
	@Autowired
	private SimpleDao officialSimpleDao;
	@Autowired
	private SqlMap tmsSqlMap;

	private static String[] AML_CONFIG_FIELD = {
			TMS_AML_CONFIG.CT_TYPE, TMS_AML_CONFIG.CT_FIELD, TMS_AML_CONFIG.AML_CONFIG
	};

	AMLDataConfig amlDataConfig = AMLDataConfig.getInstance();

	/**
	 * 查询交易模型字段信息
	 * 
	 * @param reqs
	 * @return
	 */
	public List<Map<String, Object>> getTxnModelFields( Map<String, Object> reqs ) {
		String tabName = MapUtil.getString( reqs, TMS_AML_CONFIG.TAB_NAME );
		String sql = "select * from " + TMS_COM_FD.TABLE_NAME + " t where t." + TMS_COM_FD.TAB_NAME + " in (" + TransCommon.cutToIdsForSql( tabName ) + ")" + " order by t."
				+ TMS_COM_FD.TAB_NAME + ", t." + TMS_COM_FD.TXN_ORDER + ", t." + TMS_COM_FD.REF_NAME;
		return officialSimpleDao.queryForList( sql );
	}

	/**
	 * 查询反洗钱配置信息
	 * 
	 * @param reqs
	 * @return
	 */
	public Map<String, Object> queryAmlConfig( Map<String, Object> reqs ) {
		String tabName = MapUtil.getString( reqs, TMS_AML_CONFIG.TAB_NAME );
		String sql = "select * from " + TMS_AML_CONFIG.TABLE_NAME + " where " + TMS_AML_CONFIG.TAB_NAME + " in (" + TransCommon.cutToIdsForSqlInj( tabName ) + ") order by "
				+ TMS_AML_CONFIG.TAB_NAME;
		//List<Map<String, Object>> list = officialSimpleDao.queryForList(sql);

		Map<String, Object> sqlConds = TransCommon.cutToMapIdsForSqlInj( tabName );
		List<Map<String, Object>> list = officialSimpleDao.queryForList( sql, sqlConds );

		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap.put( TMS_AML_CONFIG.TAB_NAME, tabName );
		for( String fdName : AML_CONFIG_FIELD ) {
			resMap.put( fdName, null );
		}

		if( list != null && !list.isEmpty() ) {
			for( Map<String, Object> map : list ) {
				for( String fdName : AML_CONFIG_FIELD ) {
					String fdValue = MapUtil.getString( map, fdName );
					if( !StringUtil.isBlank( fdValue ) ) {
						resMap.put( TMS_AML_CONFIG.TAB_NAME, MapUtil.getObject( map, TMS_AML_CONFIG.TAB_NAME ) );
						resMap.put( fdName, fdValue );
					}
				}
			}
		}
		return resMap;
	}

	/**
	 * 保存反洗钱配置定义
	 * @param reqs
	 * @return
	 */
	public boolean saveAmlConfig( Map<String, Object> reqs ) {
		String tab_name = MapUtil.getString( reqs, "tab_name" );
		String column = MapUtil.getString( reqs, "column" );
		String value = MapUtil.getString( reqs, "value" );
		if( "aml_config".equals( column ) && !StringUtil.isBlank( value ) ) {
			String validate = XMLUtil.validateXMLByXSD( AntiMoneyLaunderingService.class.getResourceAsStream( AMLConstant.AMLCONFIGXSDPATH ),
					new ByteArrayInputStream( value.getBytes() ) );
			if( !StringUtil.isBlank( validate ) ) {
				throw new TmsMgrServiceException( "反洗钱xml配置格式错误: " + validate );
			}
		}
		String[] cols = column.split( "\\," );
		String[] vals = (cols.length > 1 ? value.split( "\\," ) : new String[] {
				value
		});
		String sql = "select 1 from " + TMS_AML_CONFIG.TABLE_NAME + " where " + TMS_AML_CONFIG.TAB_NAME + " = ?";
		List<Map<String, Object>> list = officialSimpleDao.queryForList( sql, tab_name );
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		Object[] params = new Object[cols.length + 1];
		if( list == null || list.isEmpty() ) {
			params[0] = tab_name;
			sql = "insert into " + TMS_AML_CONFIG.TABLE_NAME + "(TAB_NAME,%s) values (?,%s)";
			for( int i = 0, len = cols.length; i < len; i++ ) {
				if( i > 0 ) {
					sb1.append( ',' );
					sb2.append( ',' );
				}
				sb1.append( cols[i] );
				sb2.append( '?' );
				params[i + 1] = vals[i];
			}
		}
		else {
			sql = "update " + TMS_AML_CONFIG.TABLE_NAME + " set %s where %s";
			sb2.append( "TAB_NAME=?" );
			for( int i = 0, len = cols.length; i < len; i++ ) {
				if( i > 0 ) {
					sb1.append( ',' );
				}
				sb1.append( cols[i] ).append( '=' ).append( '?' );
				params[i] = vals[i];
			}
			params[params.length - 1] = tab_name;
		}
		sql = String.format( sql, sb1.toString(), sb2.toString() );
		officialSimpleDao.executeUpdate( sql, params );
		return true;
	}

	/**
	 * 获取交易模型配置
	 */
	protected Map<String, List<Map<String, Object>>> getTransModeConfig() {
		String sql = "select * from " + TMS_COM_TAB.TABLE_NAME + " where " + TMS_COM_TAB.TAB_TYPE + " = '4' order by " + TMS_COM_TAB.TAB_NAME;
		List<Map<String, Object>> tabList = officialSimpleDao.queryForList( sql );
		Map<String, List<Map<String, Object>>> transModeMap = new HashMap<String, List<Map<String, Object>>>();
		if( tabList != null && !tabList.isEmpty() ) {
			sql = "select * from " + TMS_COM_FD.TABLE_NAME + " where " + TMS_COM_FD.TAB_NAME + " = ?";
			for( Map<String, Object> tabMap : tabList ) {
				String tabName = MapUtil.getString( tabMap, TMS_COM_TAB.TAB_NAME );
				List<Map<String, Object>> fdList = officialSimpleDao.queryForList( sql, tabName );
				String parentTab = MapUtil.getString( tabMap, TMS_COM_TAB.PARENT_TAB );
				if( !StringUtil.isBlank( parentTab ) ) {
					List<Map<String, Object>> list = transModeMap.get( parentTab );
					if( list != null && !list.isEmpty() ) {
						fdList.addAll( list );
					}
				}
				transModeMap.put( tabName, fdList );
			}
		}
		return transModeMap;
	}

	/**
	 * 查询所有交易的反洗钱配置信息
	 */
	private Map<String, Map<String, Object>> queryAllAmlConfigMap() {
		Map<String, Object> amlConfigMap = amlDataConfig.getAMLConfigMap();
		String sql = "select c." + TMS_AML_CONFIG.CT_TYPE + ", c." + TMS_AML_CONFIG.CT_FIELD + ", c." + TMS_AML_CONFIG.AML_CONFIG + ", t." + TMS_COM_TAB.TAB_NAME + ", t."
				+ TMS_COM_TAB.TAB_DESC + ", t." + TMS_COM_TAB.PARENT_TAB + ", t." + TMS_COM_TAB.TXNID + " from " + TMS_COM_TAB.TABLE_NAME + " t left join "
				+ TMS_AML_CONFIG.TABLE_NAME + " c on t." + TMS_COM_TAB.TAB_NAME + " = c." + TMS_AML_CONFIG.TAB_NAME + " where t." + TMS_COM_TAB.TAB_TYPE + " = '4' order by t."
				+ TMS_COM_TAB.TAB_NAME;
		List<Map<String, Object>> list = officialSimpleDao.queryForList( sql );
		if( list == null || list.isEmpty() ) return null;
		Map<String, Map<String, Object>> allAmlConfigMap = new HashMap<String, Map<String, Object>>();
		for( Map<String, Object> map : list ) {
			String txnName = MapUtil.getString( map, TMS_COM_TAB.TAB_NAME );
			String parentTab = MapUtil.getString( map, TMS_COM_TAB.PARENT_TAB );
			Map<String, Object> parentMap = allAmlConfigMap.get( parentTab );
			String ctType = MapUtil.getString( map, TMS_AML_CONFIG.CT_TYPE );
			String ctField = MapUtil.getString( map, TMS_AML_CONFIG.CT_FIELD );
			String amlConfig = MapUtil.getString( map, TMS_AML_CONFIG.AML_CONFIG );
			Map<String, Object> amlCfgMap = null;
			if( StringUtil.isBlank( parentTab ) ) {
				// 无父交易, 则为交易树的根, 即: T
				amlCfgMap = new HashMap<String, Object>( amlConfigMap );
			}
			else {
				// 有父交易
				amlCfgMap = new HashMap<String, Object>( parentMap );
			}
			amlCfgMap.putAll( amlDataConfig.getAMLConfigMap( amlCfgMap, amlConfig ) );
			if( !StringUtil.isBlank( ctType ) && !StringUtil.isBlank( ctField ) ) {
				Map<String, Object> _ctMap_ = MapUtil.getMap( amlCfgMap, AMLConstant.TXNCT );
				Map<String, Object> ctMap = new HashMap<String, Object>();
				if( _ctMap_ != null ) {
					ctMap.putAll( _ctMap_ );
				}
				ctMap.put( AMLConstant.TYPE, ctType );
				ctMap.put( AMLConstant.VALUE, ctField.split( "\\," ) );
				amlCfgMap.put( AMLConstant.TXNCT, ctMap );
			}
			allAmlConfigMap.put( txnName, amlCfgMap );
		}
		return allAmlConfigMap;
	}

	public Page<Map<String, Object>> queryExportList( Map<String, Object> reqs ) {
		String sql = "SELECT * FROM " + TMS_AML_MESSAGE.TABLE_NAME + " WHERE " + TMS_AML_MESSAGE.GROUPID + " = :groupId";
		return officialSimpleDao.pageQuery( sql, reqs, new Order().desc( "TTNM" ) );
	}

	@SuppressWarnings("unchecked")
	public String generateMessage( Map<String, Object> reqs ) {
		String groupId = "";
		try {
			String msgType = MapUtil.getString( reqs, "msgType" );
			List<Map<String, Object>> transList = queryTransList( reqs );
			if( transList == null || transList.size() <= 0 ) {
				return "0";
			}
			Map<String, Map<String, Object>> allAmlConfigMap = queryAllAmlConfigMap();
			Map<String, Object> ctMap = new HashMap<String, Object>();
			for( Map<String, Object> trans : transList ) {
				String txnType = MapUtil.getString( trans, "TXNTYPE" );
				Map<String, Object> amlConfigMap = allAmlConfigMap.get( txnType );// 获取此类交易的反洗钱配置
				Map<String, Object> ct = MapUtil.getMap( amlConfigMap, AMLConstant.TXNCT );// 可疑交易主体类型
				if( MapUtil.isEmpty( ct ) ) {
					if( "T".equals( txnType ) ) {
						throw new TmsMgrServiceException( "可疑交易主体配置为空." );
					}
					else {
						continue;
					}
				}
				String ctType = MapUtil.getString( ct, AMLConstant.TYPE );// 可疑主体类型
				String[] pkRefName = (String[]) ct.get( AMLConstant.VALUE );// 可疑主体主键
				String[] ctIds = new String[pkRefName.length];
				String ctId = "";
				for( int k = 0, klen = pkRefName.length; k < klen; k++ ) {
					String fdName = getTxnFdByTxnType( txnType, pkRefName[k] );
					ctIds[k] = MapUtil.getString( trans, fdName );
					if( k > 0 ) {
						ctId += ",";
					}
					ctId += ctIds[k];
				}
				Map<String, Object> tMap = MapUtil.getMap( ctMap, ctType );
				if( MapUtil.isEmpty( tMap ) ) {
					tMap = new HashMap<String, Object>();
					ctMap.put( ctType, tMap );
				}

				int ttnm = 0;
				List<Map<String, Object>> tranList = null;
				Map<String, Object> ttMap = MapUtil.getMap( tMap, ctId );
				if( MapUtil.isEmpty( ttMap ) ) {
					ttMap = new HashMap<String, Object>();
					tranList = new ArrayList<Map<String, Object>>( 16 );
					ttMap.put( AMLConstant.TRANS, tranList );
					tMap.put( ctId, ttMap );
				}
				else {
					ttnm = MapUtil.getInteger( ttMap, AMLConstant.TTNM );
					tranList = MapUtil.getList( ttMap, AMLConstant.TRANS );
				}
				tranList.add( trans );
				ttMap.put( AMLConstant.TTNM, ttnm + 1 );
			}

			String createTime = CalendarUtil.FORMAT9.format( new Date() );
			groupId = StringUtil.randomUUID();
			for( String ctType : ctMap.keySet() ) {
				Map<String, Object> cMap = MapUtil.getMap( ctMap, ctType );
				for( String ctId : cMap.keySet() ) {
					Map<String, Object> ttMap = (Map<String, Object>) cMap.get( ctId );
					int ttnm = MapUtil.getInteger( ttMap, AMLConstant.TTNM );
					List<Map<String, Object>> tranList = MapUtil.getList( ttMap, AMLConstant.TRANS );
					AMLMessage am = createAmlMessage( msgType, ctId, ctType, ttnm, tranList, allAmlConfigMap );
					String ctnm = MapUtil.getString( am.getCTIFs().get( 0 ), AMLConstant.CTNM );
					String message = am.toXmlString();
					Map<String, Object> amlMessage = new HashMap<String, Object>();
					amlMessage.put( TMS_AML_MESSAGE.MESSAGEID, StringUtil.randomUUID() );
					amlMessage.put( TMS_AML_MESSAGE.GROUPID, groupId );
					amlMessage.put( TMS_AML_MESSAGE.MESSAGETYPE, msgType );
					amlMessage.put( TMS_AML_MESSAGE.CTID, ctId );
					amlMessage.put( TMS_AML_MESSAGE.CTNM, ctnm );
					amlMessage.put( TMS_AML_MESSAGE.CTTYPE, ctType );
					amlMessage.put( TMS_AML_MESSAGE.SCTN, 1 );
					amlMessage.put( TMS_AML_MESSAGE.TTNM, ttnm );
					amlMessage.put( TMS_AML_MESSAGE.SSTM, "01" );
					amlMessage.put( TMS_AML_MESSAGE.STCR, "01" );
					amlMessage.put( TMS_AML_MESSAGE.MESSAGE, message );
					amlMessage.put( TMS_AML_MESSAGE.CREATETIME, createTime );
					amlMessage.put( TMS_AML_MESSAGE.MODIFYTIME, createTime );
					amlMessage.put( TMS_AML_MESSAGE.ISEXPORT, "0" );
					officialSimpleDao.create( TMS_AML_MESSAGE.TABLE_NAME, amlMessage );
				}
			}
		}
		catch( Exception e ) {
			log.error( "antiMoneyLaunderingService generateMessage error, ", e );
			return null;
		}
		return groupId;
	}

	@SuppressWarnings("serial")
	private AMLMessage createAmlMessage( String msgType, final String ctId, final String ctType, int ttnm, List<Map<String, Object>> tranList,
			Map<String, Map<String, Object>> allAmlConfigMap ) {
		AMLMessage amlMessage = new AMLMessage();
		Map<String, Object> RBIF = amlMessage.getRBIF();
		List<Map<String, Object>> CTIFs = amlMessage.getCTIFs();
		List<Map<String, Object>> STIFs = amlMessage.getSTIFs();
		Map<String, Object> pstr = amlMessage.getPSTR();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put( AMLConstant.TXNCT, new HashMap<String, Object>() {
			{
				put( AMLConstant.TYPE, ctType );
				put( AMLConstant.VALUE, ctId );
			}
		} );
		// 遍历交易
		for( int i = 0, len = tranList.size(); i < len; i++ ) {
			Map<String, Object> transMap = tranList.get( i );
			String txnType = MapUtil.getString( transMap, "TXNTYPE" );
			Map<String, Object> amlConfigMap = allAmlConfigMap.get( txnType );
			Map<String, Object> common = MapUtil.getMap( amlConfigMap, AMLConstant.COMMON );
			String transAttrName = MapUtil.getString( common, AMLConstant.TRANSDATA );// 获取交易数据存放节点名
			resultMap.put( transAttrName, transMap );// 将交易数据放入对应节点下

			Map<String, Object> messageConfigMap = MapUtil.getMap( amlConfigMap, AMLConstant.MESSAGE );
			String emptyValue = MapUtil.getString( common, AMLConstant.EMPTYVALUE );

			// 获取SQL查询的数据集合
			queryConfigSql( common, resultMap );

			if( i == 0 ) {
				// RBIF-公共信息
				// 设置公共信息, 只设置1次就可以
				Map<String, Object> rbifConfig = MapUtil.getMap( messageConfigMap, AMLConstant.RBIF );
				String targets = MapUtil.getStringByProtocol( common, AMLConstant.RBIF + "." + msgType );
				for( String target : targets.split( "," ) ) {
					Map<String, Object> tMap = MapUtil.getMap( rbifConfig, target );
					String value = getValueByType( common, pstr, resultMap, tMap, emptyValue );
					setValue( RBIF, target, value );
				}
				RBIF.put( AMLConstant.TTNM, String.valueOf( ttnm ) );

				// CTIFs-可疑主体信息
				Map<String, Object> CTIF = new LinkedHashMap<String, Object>();
				Map<String, Object> ctifConfig = MapUtil.getMap( messageConfigMap, AMLConstant.CTIF );
				for( String target : ctifConfig.keySet() ) {
					Map<String, Object> tMap = MapUtil.getMap( ctifConfig, target );
					String value = getValueByType( common, pstr, resultMap, tMap, emptyValue );
					setValue( CTIF, target, value );
				}
				CTIFs.add( CTIF );
			}

			// STIFs-可疑交易信息
			Map<String, Object> STIF = new LinkedHashMap<String, Object>();
			Map<String, Object> stifConfig = MapUtil.getMap( messageConfigMap, AMLConstant.STIF );
			for( String target : stifConfig.keySet() ) {
				Map<String, Object> tMap = MapUtil.getMap( stifConfig, target );
				String value = getValueByType( common, pstr, resultMap, tMap, emptyValue );
				setValue( STIF, target, value );
			}
			STIFs.add( STIF );
		}
		return amlMessage;
	}

	/**
	 * 解析SQL语句中的参数, 组织此SQL的查询条件数值集合
	 * 
	 * @param resultMap
	 * @param sql
	 * @return
	 */
	private Map<String, Object> getSqlQueryCondMap( Map<String, Object> common, Map<String, Object> resultMap, String sql ) {
		Map<String, Object> condMap = new HashMap<String, Object>();
		Matcher matcher = checkQueryParam.matcher( sql );
		while (matcher.find()) {
			String param = matcher.group( 2 );
			condMap.put( param, getParamValue( common, resultMap, param ) );
		}
		return condMap;
	}

	private Object getParamValue( Map<String, Object> common, Map<String, Object> resultMap, String param ) {
		Object ret = null;
		int index = param.indexOf( "(" );
		if( index == -1 ) {
			// 无转换函数
			ret = getValue( common, resultMap, param );
		}
		else {
			// 有转换函数
			String funcName = param.substring( 0, index );
			param = param.substring( index + 1, param.indexOf( ")" ) );
			String[] params = param.split( "\\," );
			String name = params[0].trim();
			Object[] funcParams = new String[params.length - 1];
			System.arraycopy( params, 1, funcParams, 0, funcParams.length );
			Function func = ApplicationContextUtil.getBean( funcName, Function.class );
			ret = func.execute( getValue( common, resultMap, name ), funcParams );
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private Object getValue( Map<String, Object> common, Map<String, Object> resultMap, String name ) {
		String[] params = name.split( "\\." );
		Object ret = null;
		boolean isTrans = false;
		for( int i = 0, len = params.length; i < len; i++ ) {
			if( ret == null ) {
				if( params[i].equals( MapUtil.getString( common, AMLConstant.TRANSDATA ) ) ) {
					isTrans = true;
				}
				ret = resultMap.get( params[i] );
			}
			else {
				if( isTrans ) {
					Map<String, Object> trans = (Map<String, Object>) ret;
					String fdName = getTxnFdByTxnType( MapUtil.getString( trans, "TXNTYPE" ), params[i] );
					ret = MapUtil.getObject( trans, fdName );
				}
				else {
					ret = ((Map<String, Object>) ret).get( params[i] );
				}
			}
		}
		return ret;
	}

	/**
	 * 执行配置的SQL语句,并将结果放入resultMap中
	 * 
	 * @param common
	 * @param resultMap
	 */
	private void queryConfigSql( Map<String, Object> common, Map<String, Object> resultMap ) {
		Map<String, Object> sqlMap = MapUtil.getMap( common, AMLConstant.QUERY );
		if( MapUtil.isEmpty( sqlMap ) ) return;
		SimpleDao simpleDao = null;
		for( String key : sqlMap.keySet() ) {
			Map<String, Object> map = MapUtil.getMap( sqlMap, key );
			if( MapUtil.isEmpty( map ) ) {
				continue;
			}
			String sql = MapUtil.getString( map, AMLConstant.SQL );
			if( StringUtil.isBlank( sql ) ) {
				continue;
			}
			String ds = MapUtil.getString( map, AMLConstant.DS );
			if( StringUtil.isBlank( ds ) ) {
				simpleDao = tmsSimpleDao;
			}
			else {
				ds += "SimpleDao";
				simpleDao = ApplicationContextUtil.getBean( ds, SimpleDao.class );
			}
			Map<String, Object> condMap = getSqlQueryCondMap( common, resultMap, sql );
			List<Map<String, Object>> list = simpleDao.queryForList( sql, condMap );
			if( list == null || list.isEmpty() ) {
				throw new TmsMgrServiceException( "can not query data, {name: " + key + ", sql: " + sql + ", value: " + condMap + ", ds: " + ds + "}" );
			}
			resultMap.put( key, list.get( 0 ) );
		}
	}

	private void setValue( Map<String, Object> map, String target, String value ) {
		if( target.contains( "." ) ) {
			String[] ttars = target.split( "\\." );
			Map<String, Object> tmap = map;
			for( int i = 0; i < ttars.length; i++ ) {
				if( i == ttars.length - 1 ) {
					tmap.put( ttars[i], value );
				}
				else {
					tmap = getSubMap( tmap, ttars[i] );
				}
			}
		}
		else {
			map.put( target, value );
		}
	}

	private Map<String, Object> getSubMap( Map<String, Object> map, String key ) {
		Map<String, Object> tmap;
		if( map.containsKey( key ) ) {
			tmap = MapUtil.getMap( map, key );
		}
		else {
			tmap = new LinkedHashMap<String, Object>();
		}
		map.put( key, tmap );
		return tmap;
	}

	private String getValueByType( Map<String, Object> common, Map<String, Object> pstr, Map<String, Object> resultMap, Map<String, Object> tMap, String emptyValue ) {
		String retValue = "";

		String type = MapUtil.getString( tMap, AMLConstant.TYPE );
		String value = MapUtil.getString( tMap, AMLConstant.VALUE );
		// 根据不同类型对值进行转换
		if( AMLConstant.CONST.equals( type ) ) {
			retValue = value;
		}
		else if( AMLConstant.DB.equals( type ) ) {
			String sql = MapUtil.getString( tMap, AMLConstant.SQL );
			Map<String, Object> result = MapUtil.getMap( resultMap, sql );
			Object retObject = getParamValue( common, result, value );
			retValue = retObject == null ? null : String.valueOf( retObject );
			if( StringUtil.isEmpty( retValue ) ) {
				String defaultValue = MapUtil.getString( tMap, AMLConstant.DEFAULT );
				retValue = defaultValue;
			}
		}
		else if( AMLConstant.REF.equals( type ) ) {
			String obj = MapUtil.getString( tMap, AMLConstant.OBJ );
			String index = MapUtil.getString( tMap, AMLConstant.INDEX );

			Map<String, Object> map;
			if( StringUtil.isEmpty( index ) ) {
				map = MapUtil.getMap( pstr, obj );
			}
			else {
				List<Map<String, Object>> list = MapUtil.getList( pstr, obj );
				map = list.get( Integer.parseInt( index ) );
			}
			retValue = MapUtil.getString( map, value );
		}
		else {
			Object retObject = getParamValue( common, resultMap, value );
			retValue = retObject == null ? null : String.valueOf( retObject );
		}

		if( StringUtil.isEmpty( retValue ) ) {
			retValue = emptyValue;
		}
		return retValue;
	}

	public void exportFile( Map<String, Object> reqs, HttpServletResponse response ) {
		try {
			Map<String, Object> amlConfigMap = amlDataConfig.getAMLConfigMap();
			Map<String, Object> common = MapUtil.getMap( amlConfigMap, AMLConstant.COMMON );

			String msgType = MapUtil.getString( reqs, "msgType" );
			String groupId = MapUtil.getString( reqs, "groupId" );
			String path = MapUtil.getString( common, AMLConstant.PATH );
			String sql = "SELECT * FROM " + TMS_AML_MESSAGE.TABLE_NAME + " WHERE " + TMS_AML_MESSAGE.GROUPID + " = ?";
			List<Map<String, Object>> msgList = officialSimpleDao.queryForList( sql, groupId );

			String rootfilePath = this.getClass().getClassLoader().getResource( "" ).getPath().replace( "%20", " " );
			String fPath = toFilepath( rootfilePath + path );

			String zipName = getZipName( common, msgType );
			File zipFile = new File( fPath + File.separator + zipName );
			List<File> xmlFiles = new ArrayList<File>();
			List<File> zipFiles = new ArrayList<File>();
			long size = 0;
			int index = 0;
			for( int i = 0; i < msgList.size(); i++ ) {
				Map<String, Object> msg = msgList.get( i );
				index++;
				String fileName = getFileName( common, msgType, zipName, index, msg );
				updateAmlMessage( msgType, zipName, fileName, msg );
				String message = MapUtil.getString( msg, TMS_AML_MESSAGE.MESSAGE );
				File file = new File( fPath + File.separator + fileName );
				FileOutputStream fos = new FileOutputStream( file );
				fos.write( message.getBytes() );
				fos.close();
				if( size + file.length() <= (5 << 20) ) {
					size += file.length();
					xmlFiles.add( file );
				}
				else {
					if( index == 1 ) {
						throw new TmsMgrServiceException( "single file too large, fileName=" + fileName + ", actual size:" + file.length() );
					}
					zipFiles( xmlFiles, zipFile );
					zipFiles.add( zipFile );
					// 获取新包名
					zipName = getZipName( common, msgType );
					zipFile = new File( fPath + File.separator + zipName );
					index = 1;
					xmlFiles = new ArrayList<File>();
					// 重新生成文件
					fileName = getFileName( common, msgType, zipName, index, msg );
					updateAmlMessage( msgType, zipName, fileName, msg );
					message = MapUtil.getString( msg, TMS_AML_MESSAGE.MESSAGE );
					file = new File( fPath + File.separator + fileName );
					fos = new FileOutputStream( file );
					fos.write( message.getBytes() );
					fos.close();

					size = file.length();
					xmlFiles.add( file );
				}
			}
			zipFiles( xmlFiles, zipFile );
			zipFiles.add( zipFile );

			File file;
			if( zipFiles.size() == 1 ) {
				file = zipFiles.get( 0 );
			}
			else if( zipFiles.size() > 1 ) {
				String tzipName = zipName.substring( 0, zipName.lastIndexOf( "-" ) );
				file = new File( fPath + File.separator + tzipName );
				zipFiles( zipFiles, file );
			}
			else {
				throw new TmsMgrServiceException( "export file error, zipFiles is empty." );
			}

			// 把压缩包流的形式返回页面
			String filename = zipFile.getName();
			InputStream fis = new BufferedInputStream( new FileInputStream( zipFile.getPath() ) );

			byte[] buffer = new byte[fis.available()];
			fis.read( buffer );
			fis.close();

			response.reset();
			response.addHeader( "Content-Disposition", "attachment;filename=" + filename );
			response.addHeader( "Content-Length", "" + zipFile.length() );

			OutputStream os = new BufferedOutputStream( response.getOutputStream() );
			response.setContentType( "application/octet-stream" );
			os.write( buffer );
			os.flush();
			os.close();
		}
		catch( Exception e ) {
			log.error( "exportFile error, caused by ", e );
			throw new TmsMgrServiceException( "exportFile error, caused by " + e, e );
		}
	}

	private String getFileName( Map<String, Object> common, String msgType, String zipName, int index, Map<String, Object> msg ) {
		String fileName = "";

		if( AMLConstant.NPS.equals( msgType ) || AMLConstant.NPC.equals( msgType ) || AMLConstant.APC.equals( msgType ) ) {
			String indexStr = String.valueOf( index );
			indexStr = "0000".substring( indexStr.length() ) + indexStr;
			zipName = StringUtil.split( zipName, "." )[0];
			fileName = msgType + zipName.substring( 3 ) + "-" + indexStr + ".xml";
		}
		else {
			String oFileName = MapUtil.getString( msg, TMS_AML_MESSAGE.FILENAME );
			fileName = msgType + oFileName.substring( 3 );
		}
		return fileName.toUpperCase();
	}

	public boolean syncCommonMessage( Map<String, Object> reqs ) {
		try {
			String messageIds = MapUtil.getString( reqs, "messageIds" );
			String msgType = MapUtil.getString( reqs, "msgType" );
			String rinm = MapUtil.getString( reqs, "RINM" );
			String firc = MapUtil.getString( reqs, "FIRC" );

			String sql = "SELECT * FROM " + TMS_AML_MESSAGE.TABLE_NAME + " WHERE " + TMS_AML_MESSAGE.MESSAGEID + " in (" + messageIds + ")";
			List<Map<String, Object>> list = officialSimpleDao.queryForList( sql );
			String modifyTime = CalendarUtil.FORMAT9.format( new Date() );
			for( Map<String, Object> map : list ) {
				String messageId = MapUtil.getString( map, TMS_AML_MESSAGE.MESSAGEID );
				String message = MapUtil.getString( map, TMS_AML_MESSAGE.MESSAGE );
				AMLMessage amlMessage = AMLMessage.parseXmlString( message );
				Map<String, Object> RBIF = amlMessage.getRBIF();
				if( !StringUtil.isEmpty( rinm ) ) {
					RBIF.put( AMLConstant.RINM, rinm );
				}
				if( !StringUtil.isEmpty( firc ) ) {
					RBIF.put( AMLConstant.FIRC, firc );
				}
				if( AMLConstant.NPS.equals( msgType ) || AMLConstant.RPS.equals( msgType ) || AMLConstant.CPS.equals( msgType ) ) {
					String ficd = MapUtil.getString( reqs, "FICD" );
					String rfsg = MapUtil.getString( reqs, "RFSG" );
					String orxn = MapUtil.getString( reqs, "ORXN" );
					String sstm = MapUtil.getString( reqs, "SSTM" );
					String stcr = MapUtil.getString( reqs, "STCR" );
					String ssds = MapUtil.getString( reqs, "SSDS" );
					String udsi = MapUtil.getString( reqs, "UDSI" );
					if( !StringUtil.isEmpty( ficd ) ) {
						RBIF.put( AMLConstant.FICD, ficd );
					}
					if( !StringUtil.isEmpty( rfsg ) ) {
						RBIF.put( AMLConstant.RFSG, rfsg );
					}
					if( !StringUtil.isEmpty( orxn ) ) {
						RBIF.put( AMLConstant.ORXN, orxn );
					}
					if( !StringUtil.isEmpty( sstm ) ) {
						RBIF.put( AMLConstant.SSTM, sstm );
					}
					if( !StringUtil.isEmpty( stcr ) ) {
						RBIF.put( AMLConstant.STCR, stcr );
					}
					if( !StringUtil.isEmpty( ssds ) ) {
						RBIF.put( AMLConstant.SSDS, ssds );
					}
					if( !StringUtil.isEmpty( udsi ) ) {
						RBIF.put( AMLConstant.UDSI, udsi );
					}
				}
				else {
					String cimc = MapUtil.getString( reqs, "CIMC" );
					if( !StringUtil.isEmpty( cimc ) ) {
						map.put( TMS_AML_MESSAGE.CIMC, cimc );
						RBIF.put( TMS_AML_MESSAGE.CIMC, cimc );
					}
				}

				map.put( TMS_AML_MESSAGE.MESSAGE, amlMessage.toXmlString() );
				map.put( TMS_AML_MESSAGE.MODIFYTIME, modifyTime );
				Map<String, Object> cond = new HashMap<String, Object>();
				cond.put( TMS_AML_MESSAGE.MESSAGEID, messageId );
				officialSimpleDao.update( TMS_AML_MESSAGE.TABLE_NAME, map, cond );
			}
		}
		catch( Exception e ) {
			log.error( "sync Common Messgae error, ", e );
			return false;
		}
		return true;
	}

	public Page<Map<String, Object>> queryAmlList( Map<String, Object> reqs ) {
		Map<String, Object> sqlConds = new HashMap<String, Object>();

		String CTID = MapUtil.getString( reqs, "CTID" );
		String CTNM = MapUtil.getString( reqs, "CTNM" );
		String MSGTYPE = MapUtil.getString( reqs, "MSGTYPE" );
		String FILENAME = MapUtil.getString( reqs, "FILENAME" );
		String startTime = MapUtil.getString( reqs, "startTime" );
		String endTime = MapUtil.getString( reqs, "endTime" );
		String sql = tmsSqlMap.getSql( "tms.aml.amllist" );

		if( !StringUtil.isEmpty( CTID ) ) {
			// sql += " AND t." + TMS_AML_MESSAGE.CTID + " = '" + CTID.trim() + "'";
			sql += " AND t." + TMS_AML_MESSAGE.CTID + " =:CTID";
			sqlConds.put( "CTID", CTID );
		}
		if( !StringUtil.isEmpty( CTNM ) ) {
			// sql += " AND t." + TMS_AML_MESSAGE.CTNM + " = '" + CTNM.trim() + "'";
			sql += " AND t." + TMS_AML_MESSAGE.CTNM + " =:CTNM";
			sqlConds.put( "CTNM", CTNM );
		}
		if( !StringUtil.isEmpty( MSGTYPE ) ) {
			// sql += " AND t." + TMS_AML_MESSAGE.MESSAGETYPE + " = '" + MSGTYPE + "'";
			sql += " AND t." + TMS_AML_MESSAGE.MESSAGETYPE + " =:MSGTYPE ";
			sqlConds.put( "MSGTYPE", MSGTYPE );
		}
		if( !StringUtil.isEmpty( FILENAME ) ) {
			// sql += " AND t." + TMS_AML_MESSAGE.FILENAME + " like '%" + trans2NewStr(FILENAME) + "%'";
			sql += " AND t." + TMS_AML_MESSAGE.FILENAME + " like :FILENAME";
			String fn = "'%' + trans2NewStr(FILENAME) + '%'";
			sqlConds.put( "FILENAME", fn );
		}
		if( !StringUtil.isEmpty( startTime ) ) {
			// sql += " AND t." + TMS_AML_MESSAGE.CREATETIME + " >= " + CalendarUtil.parseTimeMillisToDateTime(CalendarUtil.parseStringToTimeMillis(startTime, CalendarUtil.FORMAT14.toPattern()), CalendarUtil.FORMAT10.toPattern());
			sql += " AND t." + TMS_AML_MESSAGE.CREATETIME + " >=:STARTTIME";
			sqlConds.put( "STARTTIME", CalendarUtil.parseTimeMillisToDateTime( CalendarUtil.parseStringToTimeMillis( startTime, CalendarUtil.FORMAT14.toPattern() ),
					CalendarUtil.FORMAT10.toPattern() ) );
		}
		if( !StringUtil.isEmpty( endTime ) ) {
			// sql += " AND t." + TMS_AML_MESSAGE.CREATETIME + " <= " + CalendarUtil.parseTimeMillisToDateTime(CalendarUtil.parseStringToTimeMillis(endTime, CalendarUtil.FORMAT14.toPattern()), CalendarUtil.FORMAT10.toPattern());
			sql += " AND t." + TMS_AML_MESSAGE.CREATETIME + " <=:ENDTIME";
			sqlConds.put( "ENDTIME", CalendarUtil.parseTimeMillisToDateTime( CalendarUtil.parseStringToTimeMillis( endTime, CalendarUtil.FORMAT14.toPattern() ),
					CalendarUtil.FORMAT10.toPattern() ) );
		}

		return officialSimpleDao.pageQuery( sql, sqlConds, new Order().desc( TMS_AML_MESSAGE.TTNM ) );
	}

	private String getZipName( Map<String, Object> common, String msgType ) {
		String zipName = "";
		Map<String, Object> packetMapping = MapUtil.getMap( common, AMLConstant.PACKETMAPPING );
		String ficd = MapUtil.getString( common, AMLConstant.FICD );
		String date = CalendarUtil.FORMAT17.format( new Date() );
		String pt = MapUtil.getString( packetMapping, msgType.substring( 0, 1 ) );
		String packetType = pt + msgType.substring( 1 );
		String pZipName = packetType + ficd + "-" + date;
		String sql = "SELECT * FROM " + TMS_AML_MESSAGE.TABLE_NAME + " WHERE " + TMS_AML_MESSAGE.ISEXPORT + " = '1'" + " AND " + TMS_AML_MESSAGE.ZIPNAME + " like '" + pZipName
				+ "%'" + " order by " + TMS_AML_MESSAGE.ZIPNAME + " desc";
		List<Map<String, Object>> list = officialSimpleDao.queryForList( sql );
		if( list == null || list.size() == 0 ) {
			zipName = pZipName + "-0001.zip";
		}
		else {
			String oZipName = MapUtil.getString( list.get( 0 ), TMS_AML_MESSAGE.ZIPNAME );
			String oIndex = StringUtil.split( oZipName, "." )[0].split( "-" )[2];

			String indexStr = String.valueOf( Integer.parseInt( oIndex ) + 1 );
			indexStr = "0000".substring( indexStr.length() ) + indexStr;
			zipName = pZipName + "-" + indexStr + ".zip";
		}
		return zipName.toUpperCase();
	}

	private void updateAmlMessage( String msgType, String zipName, String fileName, Map<String, Object> msg ) {
		String messageId = MapUtil.getString( msg, TMS_AML_MESSAGE.MESSAGEID );
		String filenName = MapUtil.getString( msg, TMS_AML_MESSAGE.FILENAME );
		String message = MapUtil.getString( msg, TMS_AML_MESSAGE.MESSAGE );
		if( AMLConstant.NPS.equals( msgType ) || AMLConstant.NPC.equals( msgType ) || AMLConstant.APC.equals( msgType ) ) {
			msg.put( TMS_AML_MESSAGE.FILENAME, fileName );
		}
		else {
			AMLMessage amlMessage = AMLMessage.parseXmlString( message );
			Map<String, Object> RBIF = amlMessage.getRBIF();
			String RFSG = MapUtil.getString( RBIF, AMLConstant.RFSG );
			if( StringUtil.isEmpty( RFSG ) ) {
				RFSG = "0";
			}
			RBIF.put( AMLConstant.RFSG, Integer.parseInt( RFSG ) + 1 );
			RBIF.put( AMLConstant.ORXN, StringUtil.split( filenName, "." )[0] );

			message = amlMessage.toXmlString();
			msg.put( TMS_AML_MESSAGE.MESSAGE, message );
		}
		String modifyTime = CalendarUtil.FORMAT9.format( new Date() );
		msg.put( TMS_AML_MESSAGE.ZIPNAME, zipName );
		msg.put( TMS_AML_MESSAGE.ISEXPORT, "1" );
		msg.put( TMS_AML_MESSAGE.MODIFYTIME, modifyTime );
		Map<String, Object> cond = new HashMap<String, Object>();
		cond.put( TMS_AML_MESSAGE.MESSAGEID, messageId );
		officialSimpleDao.update( TMS_AML_MESSAGE.TABLE_NAME, msg, cond );
	}

	private String getTxnFdByTxnType( String tabName, String refName ) {
		String sql = "select * from  tms_com_fd f where f.ref_name = '" + refName + "' and " + "f.tab_name in (" + TransCommon.cutToIdsForSql( tabName ) + ")";
		List<Map<String, Object>> list = officialSimpleDao.queryForList( sql );
		if( list != null && list.size() > 0 ) {
			return MapUtil.getString( list.get( 0 ), "FD_NAME" );
		}
		return null;
	}

	private List<Map<String, Object>> queryTransList( Map<String, Object> reqs ) {

		Map<String, Object> sqlConds = new HashMap<String, Object>();

		String queryType = MapUtil.getString( reqs, "queryType" );
		String txncode = MapUtil.getString( reqs, "txncode" );
		String userid = MapUtil.getString( reqs, "userid" );
		String username = MapUtil.getString( reqs, "username" );
		String ipaddr = MapUtil.getString( reqs, "ipaddr" );
		String deviceid = MapUtil.getString( reqs, "deviceid" );
		String sessionid = MapUtil.getString( reqs, "sessionid" );
		String txntype = MapUtil.getString( reqs, "txntype" );
		String disposal = MapUtil.getString( reqs, "disposal" );
		String iseval = MapUtil.getString( reqs, "iseval" );
		String iscorrect = MapUtil.getString( reqs, "iscorrect" );
		String confirmrisk = MapUtil.getString( reqs, "confirmrisk" );
		String txnstatus = MapUtil.getString( reqs, "txnstatus" );
		String ruleid = MapUtil.getString( reqs, "ruleid" );
		String min_score = MapUtil.getString( reqs, "min_score" );
		String max_score = MapUtil.getString( reqs, "max_score" );
		String operate_time = MapUtil.getString( reqs, "operate_time" );
		String end_time = MapUtil.getString( reqs, "end_time" );
		String countrycode = MapUtil.getString( reqs, "countrycode" );
		String regioncode = MapUtil.getString( reqs, "regioncode" );
		String citycode = MapUtil.getString( reqs, "citycode" );

		String sql = tmsSqlMap.getSql( "tms.aml.translist" );
		// 根据查询类型查询交易
		if( "1".equals( queryType ) ) {
			// 勾选交易，则按勾选交易id进行查询
			String txncodes = MapUtil.getString( reqs, "txncodes" );
			sql += " AND traffic.txncode in (" + txncodes + ") ";
		}
		else {
			// 未勾选，则查询符合查询条件的交易
			if( !StringUtil.isEmpty( txncode ) ) {
				// sql += " AND traffic.txncode = '" + txncode + "'";
				sql += " AND traffic.txncode =:txncode";
				sqlConds.put( "txncode", txncode );
			}
			if( !StringUtil.isEmpty( userid ) ) {
				// sql += " AND traffic.userid = '" + userid + "'";
				sql += " AND traffic.userid =:userid";
				sqlConds.put( "userid", userid );
			}
			if( !StringUtil.isEmpty( username ) ) {
				// sql += " AND username = '" + username + "'";
				sql += " AND username =:username";
				sqlConds.put( "username", username );
			}
			if( !StringUtil.isEmpty( ipaddr ) ) {
				// sql += " AND traffic.ipaddr = '" + ipaddr + "'";
				sql += " AND traffic.ipaddr =:ipaddr";
				sqlConds.put( "ipaddr", ipaddr );
			}
			if( !StringUtil.isEmpty( deviceid ) ) {
				// sql += " AND traffic.deviceid = '" + deviceid + "'";
				sql += " AND traffic.deviceid =:deviceid";
				sqlConds.put( "deviceid", deviceid );
			}
			if( !StringUtil.isEmpty( sessionid ) ) {
				// sql += " AND traffic.sessionid = '" + sessionid + "'";
				sql += " AND traffic.sessionid =:sessionid";
				sqlConds.put( "sessionid", sessionid );
			}
			if( !StringUtil.isEmpty( txntype ) ) {
				String[] strs = txntype.split( "," );
				txntype = "";
				for( String str : strs ) {
					txntype += "'" + str + "',";
				}
				txntype = txntype.substring( 0, txntype.length() - 1 );
				sql += " AND traffic.txntype in (" + txntype + ")";
			}
			if( !StringUtil.isEmpty( disposal ) ) {
				// sql += " AND traffic.disposal = '" + disposal + "'";
				sql += " AND traffic.disposal =:disposal";
				sqlConds.put( "disposal", disposal );
			}
			if( !StringUtil.isEmpty( iseval ) ) {
				// sql += " AND traffic.iseval = '" + iseval + "'";
				sql += " AND traffic.iseval =:iseval";
				sqlConds.put( "iseval", iseval );
			}
			if( !StringUtil.isEmpty( iscorrect ) ) {
				// sql += " AND nvl(traffic.iscorrect,2) = '" + iscorrect + "'";
				sql += " AND nvl(traffic.iscorrect,2) =:iscorrect";
				sqlConds.put( "iscorrect", iscorrect );
			}
			if( !StringUtil.isEmpty( confirmrisk ) ) {
				// sql += " AND nvl(traffic.confirmrisk,2) = '" + confirmrisk + "'";
				sql += " AND nvl(traffic.confirmrisk,2) =:confirmrisk";
				sqlConds.put( "confirmrisk", confirmrisk );
			}
			if( !StringUtil.isEmpty( txnstatus ) ) {
				// sql += " AND traffic.txnstatus = '" + txnstatus + "'";
				sql += " AND traffic.txnstatus =:txnstatus";
				sqlConds.put( "txnstatus", txnstatus );
			}
			if( !StringUtil.isEmpty( ruleid ) ) {
				String[] strs = ruleid.split( "," );
				ruleid = "";
				for( String str : strs ) {
					ruleid += "'" + str + "',";
				}
				ruleid = txntype.substring( 0, ruleid.length() - 1 );
				sql += " AND ruletrig.ruleid in (" + ruleid + ")";
			}
			if( !StringUtil.isEmpty( min_score ) ) {
				// sql += " AND ruletrig.rule_score >= " + min_score;
				sql += " AND ruletrig.rule_score >=:min_score";
				sqlConds.put( "min_score", min_score );
			}
			if( !StringUtil.isEmpty( max_score ) ) {
				// sql += " AND ruletrig.rule_score <= " + max_score;
				sql += " AND ruletrig.rule_score <=:max_score";
				sqlConds.put( "max_score", max_score );
			}
			if( !StringUtil.isEmpty( operate_time ) ) {
				// sql += " AND traffic.txntime >= " + CalendarUtil.parseStringToTimeMillis(operate_time, CalendarUtil.FORMAT14.toPattern());
				sql += " AND traffic.txntime >=:operate_time";
				sqlConds.put( "operate_time", CalendarUtil.parseStringToTimeMillis( operate_time, CalendarUtil.FORMAT14.toPattern() ) );
			}
			if( !StringUtil.isEmpty( end_time ) ) {
				// sql += " AND traffic.txntime <= " + CalendarUtil.parseStringToTimeMillis(end_time, CalendarUtil.FORMAT14.toPattern());
				sql += " AND traffic.txntime <=:end_time";
				sqlConds.put( "end_time", CalendarUtil.parseStringToTimeMillis( end_time, CalendarUtil.FORMAT14.toPattern() ) );
			}
			if( !StringUtil.isEmpty( countrycode ) ) {
				// sql += " AND traffic.countrycode = '" + countrycode + "'";
				sql += " AND traffic.countrycode =:countrycode";
				sqlConds.put( "countrycode", countrycode );
			}
			if( !StringUtil.isEmpty( regioncode ) ) {
				// sql += " AND traffic.regioncode = '" + regioncode + "'";
				sql += " AND traffic.regioncode =:regioncode";
				sqlConds.put( "regioncode", regioncode );
			}
			if( !StringUtil.isEmpty( citycode ) ) {
				// sql += " AND traffic.citycode = '" + citycode + "'";
				sql += " AND traffic.citycode = :citycode";
				sqlConds.put( "citycode", citycode );
			}
		}
		return tmsSimpleDao.queryForList( sql );
	}

	/**
	 * 功能:压缩多个文件成一个zip文件
	 * 
	 * @param srcfile
	 *            ：源文件列表
	 * @param zipfile
	 *            ：压缩后的文件
	 */
	private void zipFiles( List<File> srcfile, File zipfile ) {
		byte[] buf = new byte[1024];
		try {
			// ZipOutputStream类：完成文件或文件夹的压缩
			ZipOutputStream out = new ZipOutputStream( new FileOutputStream( zipfile ) );
			for( int i = 0; i < srcfile.size(); i++ ) {
				FileInputStream in = new FileInputStream( srcfile.get( i ) );
				out.putNextEntry( new ZipEntry( srcfile.get( i ).getName() ) );
				int len;
				while ((len = in.read( buf )) > 0) {
					out.write( buf, 0, len );
				}
				out.closeEntry();
				in.close();
			}
			out.close();
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
	}

	/**
	 * 构建路径
	 * 
	 * @param MultipartFile
	 * @return
	 */
	private String toFilepath( String conpathfilePath ) {
		String pathfilePath = conpathfilePath + File.separator + "";
		File filePath = new File( pathfilePath );
		if( !filePath.exists() ) {
			if( !filePath.mkdir() ) {
				throw new TmsMgrServiceException( "创建目录错误" );
			}
		}
		return pathfilePath;
	}

	public Map<String, Object> queryMessageInfo( Map<String, Object> reqs ) {
		String messageId = MapUtil.getString( reqs, "messageId" );
		Map<String, Object> msg = queryMessageById( messageId );
		String message = MapUtil.getString( msg, TMS_AML_MESSAGE.MESSAGE );
		AMLMessage amlMessage = AMLMessage.parseXmlString( message );
		return amlMessage.getPSTR();
	}

	private Map<String, Object> queryMessageById( String messageId ) {
		Map<String, Object> cond = new HashMap<String, Object>();
		cond.put( TMS_AML_MESSAGE.MESSAGEID, messageId );
		return officialSimpleDao.retrieve( TMS_AML_MESSAGE.TABLE_NAME, cond );
	}

	public boolean updateAmlMessage( Map<String, Object> reqs ) {
		try {
			String messageId = MapUtil.getString( reqs, "messageId" );
			String msgType = MapUtil.getString( reqs, "msgType" );

			Map<String, Object> msg = queryMessageById( messageId );
			String message = MapUtil.getString( msg, TMS_AML_MESSAGE.MESSAGE );
			AMLMessage amlMessage = AMLMessage.parseXmlString( message );
			// 公共信息
			Map<String, Object> rbif = amlMessage.getRBIF();
			String rinm = convertValue( MapUtil.getString( reqs, "RINM" ) );
			String firc = convertValue( MapUtil.getString( reqs, "FIRC" ) );
			if( !StringUtil.isEmpty( rinm ) ) {
				rbif.put( AMLConstant.RINM, rinm );
			}
			if( !StringUtil.isEmpty( firc ) ) {
				rbif.put( AMLConstant.FIRC, firc );
			}
			if( AMLConstant.NPS.equals( msgType ) || AMLConstant.RPS.equals( msgType ) || AMLConstant.CPS.equals( msgType ) ) {
				String ficd = convertValue( MapUtil.getString( reqs, "FICD" ) );
				String rfsg = convertValue( MapUtil.getString( reqs, "RFSG" ) );
				String orxn = convertValue( MapUtil.getString( reqs, "ORXN" ) );
				String sstm = convertValue( MapUtil.getString( reqs, "SSTM" ) );
				String stcr = convertValue( MapUtil.getString( reqs, "STCR" ) );
				String ssds = convertValue( MapUtil.getString( reqs, "SSDS" ) );
				String udsi = convertValue( MapUtil.getString( reqs, "UDSI" ) );
				if( !StringUtil.isEmpty( ficd ) ) {
					rbif.put( AMLConstant.FICD, ficd );
				}
				if( !StringUtil.isEmpty( rfsg ) ) {
					rbif.put( AMLConstant.RFSG, rfsg );
				}
				if( !StringUtil.isEmpty( orxn ) ) {
					rbif.put( AMLConstant.ORXN, orxn );
				}
				if( !StringUtil.isEmpty( sstm ) ) {
					msg.put( TMS_AML_MESSAGE.SSTM, sstm );
					rbif.put( AMLConstant.SSTM, sstm );
				}
				if( !StringUtil.isEmpty( stcr ) ) {
					msg.put( TMS_AML_MESSAGE.STCR, stcr );
					rbif.put( AMLConstant.STCR, stcr );
				}
				if( !StringUtil.isEmpty( ssds ) ) {
					rbif.put( AMLConstant.SSDS, ssds );
				}
				if( !StringUtil.isEmpty( udsi ) ) {
					rbif.put( AMLConstant.UDSI, udsi );
				}
			}
			else {
				String cimc = convertValue( MapUtil.getString( reqs, "CIMC" ) );
				if( !StringUtil.isEmpty( cimc ) ) {
					msg.put( TMS_AML_MESSAGE.CIMC, cimc );
					rbif.put( AMLConstant.CIMC, cimc );
				}
			}

			// 可疑主体信息
			Map<String, Object> ctif = amlMessage.getCTIFs().get( 0 );
			String ctnm = convertValue( MapUtil.getString( reqs, "CTNM" ) );
			String smid = convertValue( MapUtil.getString( reqs, "SMID" ) );
			String citp = convertValue( MapUtil.getString( reqs, "CITP" ) );
			String ctid = convertValue( MapUtil.getString( reqs, "CTID" ) );
			String ctar = convertValue( MapUtil.getString( reqs, "CTAR" ) );
			String cctl = convertValue( MapUtil.getString( reqs, "CCTL" ) );
			String ceml = convertValue( MapUtil.getString( reqs, "CEML" ) );
			String ctvc = convertValue( MapUtil.getString( reqs, "CTVC" ) );
			String crnm = convertValue( MapUtil.getString( reqs, "CRNM" ) );
			String crit = convertValue( MapUtil.getString( reqs, "CRIT" ) );
			String crid = convertValue( MapUtil.getString( reqs, "CRID" ) );

			if( !StringUtil.isEmpty( ctnm ) ) {
				ctif.put( AMLConstant.CTNM, ctnm );
			}
			if( !StringUtil.isEmpty( smid ) ) {
				ctif.put( AMLConstant.SMID, smid );
			}
			if( !StringUtil.isEmpty( citp ) ) {
				ctif.put( AMLConstant.CITP, citp );
			}
			if( !StringUtil.isEmpty( ctid ) ) {
				ctif.put( AMLConstant.CTID, ctid );
			}
			if( !StringUtil.isEmpty( ctar ) ) {
				ctif.put( AMLConstant.CTAR, ctar );
			}
			if( !StringUtil.isEmpty( cctl ) ) {
				ctif.put( AMLConstant.CCTL, cctl );
			}
			if( !StringUtil.isEmpty( ceml ) ) {
				ctif.put( AMLConstant.CEML, ceml );
			}
			if( !StringUtil.isEmpty( ctvc ) ) {
				ctif.put( AMLConstant.CTVC, ctvc );
			}
			if( !StringUtil.isEmpty( crnm ) ) {
				ctif.put( AMLConstant.CRNM, crnm );
			}
			if( !StringUtil.isEmpty( crit ) ) {
				ctif.put( AMLConstant.CRIT, crit );
			}
			if( !StringUtil.isEmpty( crid ) ) {
				ctif.put( AMLConstant.CRID, crid );
			}

			String modifyTime = CalendarUtil.FORMAT9.format( new Date() );
			msg.put( TMS_AML_MESSAGE.MESSAGE, amlMessage.toXmlString() );
			msg.put( TMS_AML_MESSAGE.MODIFYTIME, modifyTime );
			Map<String, Object> cond = new HashMap<String, Object>();
			cond.put( TMS_AML_MESSAGE.MESSAGEID, messageId );
			officialSimpleDao.update( TMS_AML_MESSAGE.TABLE_NAME, msg, cond );
			return true;
		}
		catch( Exception e ) {
			log.error( "update amlMessage error, ", e );
		}
		return false;
	}

	public boolean updateStifMessage( Map<String, Object> reqs ) {
		try {
			String messageId = MapUtil.getString( reqs, "messageId" );
			String index = MapUtil.getString( reqs, "index" );

			String ctnm = convertValue( MapUtil.getString( reqs, "CTNM" ) );
			String citp = convertValue( MapUtil.getString( reqs, "CITP" ) );
			String ctid = convertValue( MapUtil.getString( reqs, "CTID" ) );
			String cbat = convertValue( MapUtil.getString( reqs, "CBAT" ) );
			String cbac = convertValue( MapUtil.getString( reqs, "CBAC" ) );
			String cabm = convertValue( MapUtil.getString( reqs, "CABM" ) );
			String ctat = convertValue( MapUtil.getString( reqs, "CTAT" ) );
			String ctac = convertValue( MapUtil.getString( reqs, "CTAC" ) );
			String cpin = convertValue( MapUtil.getString( reqs, "CPIN" ) );
			String cpba = convertValue( MapUtil.getString( reqs, "CPBA" ) );
			String cpbn = convertValue( MapUtil.getString( reqs, "CPBN" ) );
			String ctip = convertValue( MapUtil.getString( reqs, "CTIP" ) );
			String tstm = convertValue( MapUtil.getString( reqs, "TSTM" ) );
			String cttp = convertValue( MapUtil.getString( reqs, "CTTP" ) );
			String tsdr = convertValue( MapUtil.getString( reqs, "TSDR" ) );
			String crpp = convertValue( MapUtil.getString( reqs, "CRPP" ) );
			String crtp = convertValue( MapUtil.getString( reqs, "CRTP" ) );
			String crat = convertValue( MapUtil.getString( reqs, "CRAT" ) );
			String tcnm = convertValue( MapUtil.getString( reqs, "TCNM" ) );
			String tsmi = convertValue( MapUtil.getString( reqs, "TSMI" ) );
			String tcit = convertValue( MapUtil.getString( reqs, "TCIT" ) );
			String tcid = convertValue( MapUtil.getString( reqs, "TCID" ) );
			String tcat = convertValue( MapUtil.getString( reqs, "TCAT" ) );
			String tcba = convertValue( MapUtil.getString( reqs, "TCBA" ) );
			String tcbn = convertValue( MapUtil.getString( reqs, "TCBN" ) );
			String tctt = convertValue( MapUtil.getString( reqs, "TCTT" ) );
			String tcta = convertValue( MapUtil.getString( reqs, "TCTA" ) );
			String tcpn = convertValue( MapUtil.getString( reqs, "TCPN" ) );
			String tcpa = convertValue( MapUtil.getString( reqs, "TCPA" ) );
			String tpbn = convertValue( MapUtil.getString( reqs, "TPBN" ) );
			String tcip = convertValue( MapUtil.getString( reqs, "TCIP" ) );
			String tmnm = convertValue( MapUtil.getString( reqs, "TMNM" ) );
			String bptc = convertValue( MapUtil.getString( reqs, "BPTC" ) );
			String pmtc = convertValue( MapUtil.getString( reqs, "PMTC" ) );
			String ticd = convertValue( MapUtil.getString( reqs, "TICD" ) );

			Map<String, Object> msg = queryMessageById( messageId );
			String message = MapUtil.getString( msg, TMS_AML_MESSAGE.MESSAGE );
			AMLMessage amlMessage = AMLMessage.parseXmlString( message );
			// 可疑交易信息
			Map<String, Object> stif = amlMessage.getSTIFs().get( Integer.parseInt( index ) );
			if( !StringUtil.isEmpty( ctnm ) ) {
				stif.put( AMLConstant.CTNM, ctnm );
			}
			if( !StringUtil.isEmpty( citp ) ) {
				stif.put( AMLConstant.CITP, citp );
			}
			if( !StringUtil.isEmpty( ctid ) ) {
				stif.put( AMLConstant.CTID, ctid );
			}
			if( !StringUtil.isEmpty( cbat ) ) {
				stif.put( AMLConstant.CBAT, cbat );
			}
			if( !StringUtil.isEmpty( cbac ) ) {
				stif.put( AMLConstant.CBAC, cbac );
			}
			if( !StringUtil.isEmpty( cabm ) ) {
				stif.put( AMLConstant.CABM, cabm );
			}
			if( !StringUtil.isEmpty( ctat ) ) {
				stif.put( AMLConstant.CTAT, ctat );
			}
			if( !StringUtil.isEmpty( ctac ) ) {
				stif.put( AMLConstant.CTAC, ctac );
			}
			if( !StringUtil.isEmpty( cpin ) ) {
				stif.put( AMLConstant.CPIN, cpin );
			}
			if( !StringUtil.isEmpty( cpba ) ) {
				stif.put( AMLConstant.CPBA, cpba );
			}
			if( !StringUtil.isEmpty( cpbn ) ) {
				stif.put( AMLConstant.CPBN, cpbn );
			}
			if( !StringUtil.isEmpty( ctip ) ) {
				stif.put( AMLConstant.CTIP, ctip );
			}
			if( !StringUtil.isEmpty( tstm ) ) {
				stif.put( AMLConstant.TSTM, tstm );
			}
			if( !StringUtil.isEmpty( cttp ) ) {
				stif.put( AMLConstant.CTTP, cttp );
			}
			if( !StringUtil.isEmpty( tsdr ) ) {
				stif.put( AMLConstant.TSDR, tsdr );
			}
			if( !StringUtil.isEmpty( crpp ) ) {
				stif.put( AMLConstant.CRPP, crpp );
			}
			if( !StringUtil.isEmpty( crtp ) ) {
				stif.put( AMLConstant.CRTP, crtp );
			}
			if( !StringUtil.isEmpty( crat ) ) {
				stif.put( AMLConstant.CRAT, crat );
			}
			if( !StringUtil.isEmpty( tcnm ) ) {
				stif.put( AMLConstant.TCNM, tcnm );
			}
			if( !StringUtil.isEmpty( tsmi ) ) {
				stif.put( AMLConstant.TSMI, tsmi );
			}
			if( !StringUtil.isEmpty( tcit ) ) {
				stif.put( AMLConstant.TCIT, tcit );
			}
			if( !StringUtil.isEmpty( tcid ) ) {
				stif.put( AMLConstant.TCID, tcid );
			}
			if( !StringUtil.isEmpty( tcat ) ) {
				stif.put( AMLConstant.TCAT, tcat );
			}
			if( !StringUtil.isEmpty( tcba ) ) {
				stif.put( AMLConstant.TCBA, tcba );
			}
			if( !StringUtil.isEmpty( tcbn ) ) {
				stif.put( AMLConstant.TCBN, tcbn );
			}
			if( !StringUtil.isEmpty( tctt ) ) {
				stif.put( AMLConstant.TCTT, tctt );
			}
			if( !StringUtil.isEmpty( tcta ) ) {
				stif.put( AMLConstant.TCTA, tcta );
			}
			if( !StringUtil.isEmpty( tcpn ) ) {
				stif.put( AMLConstant.TCPN, tcpn );
			}
			if( !StringUtil.isEmpty( tcpa ) ) {
				stif.put( AMLConstant.TCPA, tcpa );
			}
			if( !StringUtil.isEmpty( tpbn ) ) {
				stif.put( AMLConstant.TPBN, tpbn );
			}
			if( !StringUtil.isEmpty( tcip ) ) {
				stif.put( AMLConstant.TCIP, tcip );
			}
			if( !StringUtil.isEmpty( tmnm ) ) {
				stif.put( AMLConstant.TMNM, tmnm );
			}
			if( !StringUtil.isEmpty( bptc ) ) {
				stif.put( AMLConstant.BPTC, bptc );
			}
			if( !StringUtil.isEmpty( pmtc ) ) {
				stif.put( AMLConstant.PMTC, pmtc );
			}
			if( !StringUtil.isEmpty( ticd ) ) {
				stif.put( AMLConstant.TICD, ticd );
			}

			String modifyTime = CalendarUtil.FORMAT9.format( new Date() );
			msg.put( TMS_AML_MESSAGE.MESSAGE, amlMessage.toXmlString() );
			msg.put( TMS_AML_MESSAGE.MODIFYTIME, modifyTime );
			Map<String, Object> cond = new HashMap<String, Object>();
			cond.put( TMS_AML_MESSAGE.MESSAGEID, messageId );
			officialSimpleDao.update( TMS_AML_MESSAGE.TABLE_NAME, msg, cond );
			return true;
		}
		catch( Exception e ) {
			log.error( "update stifMessage error, ", e );
		}
		return false;
	}

	public String updateGroup( Map<String, Object> reqs ) {
		try {
			String messageIds = MapUtil.getString( reqs, "messageIds" );

			String sql = "UPDATE " + TMS_AML_MESSAGE.TABLE_NAME + " SET " + TMS_AML_MESSAGE.GROUPID + " = :groupId, " + TMS_AML_MESSAGE.MODIFYTIME + " = :modifyTime " + " WHERE "
					+ TMS_AML_MESSAGE.MESSAGEID + " in (" + messageIds + ")";

			String groupId = StringUtil.randomUUID();
			String modifyTime = CalendarUtil.FORMAT9.format( new Date() );
			Map<String, Object> cond = new HashMap<String, Object>();
			cond.put( "groupId", groupId );
			cond.put( "modifyTime", modifyTime );
			officialSimpleDao.executeUpdate( sql, cond );
			return groupId;
		}
		catch( Exception e ) {
			log.error( "updateGroup error, ", e );
		}
		return null;
	}

	private String convertValue( String value ) {
		Map<String, Object> amlConfigMap = amlDataConfig.getAMLConfigMap();
		Map<String, Object> common = MapUtil.getMap( amlConfigMap, AMLConstant.COMMON );
		String emptyValue = MapUtil.getString( common, AMLConstant.EMPTYVALUE );

		if( StringUtil.isEmpty( value ) ) {
			value = emptyValue;
		}
		return value;
	}

	/**
	 * 处理输入的文件名称
	 * 
	 * @param inputstr
	 *            :输入文件名
	 * @return outputstr:输出文件名
	 */
	private static String trans2NewStr( String inputstr ) {
		String regex = "^[a-zA-Z]{4}.*";
		inputstr = inputstr.trim();
		String outputstr = "";
		int len = ("".equals( inputstr ) || inputstr == null) == true ? 0 : inputstr.length();

		if( len > 4 ) {
			if( inputstr.matches( regex ) ) {
				outputstr = inputstr.substring( 0, 4 ).toUpperCase() + inputstr.substring( 4 );
			}
			else {
				outputstr = inputstr.toUpperCase();
			}
		}
		else {
			outputstr = inputstr.toUpperCase();
		}
		return outputstr;
	}

	public static void main( String[] args ) {
		Pattern checkQueryParam = Pattern.compile( "(=\\s*:\\s*([\\w$\\.]+))", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE );
		Matcher matcher = checkQueryParam.matcher(
				"select case rfsg when 100 then 00 else rfsg end as rfsg from (select case count(1) + 1 rfsg from tms_aml_message where ctid = :txnCT.value and cttype = :txnCT.type and isexport = '1') a" );
		while (matcher.find()) {
			System.out.println( matcher.group() );
			System.out.println( matcher.group( 1 ) );
			System.out.println( matcher.group( 2 ) );
		}
	}
}