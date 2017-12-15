package cn.com.higinet.tms.manager.modules.tran.service.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.modules.common.CommonCheckService;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.PropertiesUtil;
import cn.com.higinet.tms.manager.modules.common.SequenceService;
import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_FD;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_REFFD;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_REFTAB;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.query.common.model.Column;
import cn.com.higinet.tms.manager.modules.query.common.model.Custom;
import cn.com.higinet.tms.manager.modules.query.common.model.JsonDataProcess;
import cn.com.higinet.tms.manager.modules.query.common.model.Result;
import cn.com.higinet.tms.manager.modules.query.service.process.QueryDataProcess;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;
import cn.com.higinet.tms.manager.modules.tran.service.TransDefService;
import cn.com.higinet.tms.manager.modules.tran.service.TransModelService;
import cn.com.higinet.tms35.core.cache.db_fd;
import cn.com.higinet.tms35.core.cache.db_rule;
import cn.com.higinet.tms35.core.cache.db_rule_action;
import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.core.cache.db_strategy;
import cn.com.higinet.tms35.core.cache.db_sw;

/**
 *
 * @author yangk
 */
@SuppressWarnings("all")
@Service("transModelService")
public class TransModelServiceImpl implements TransModelService {

	private static Log log = LogFactory.getLog( TransModelServiceImpl.class );

	@Autowired
	private CommonCheckService commonCheckService;

	@Autowired
	private SimpleDao tmsSimpleDao;

	@Autowired
	@Qualifier("tmpSimpleDao")
	private SimpleDao tmpSimpleDao;

	@Autowired
	private SqlMap tmsSqlMap;

	@Autowired
	private TransDefService transDefService;

	@Autowired
	private SequenceService sequenceService;

	public List<Map<String, Object>> getTranModels( String txnid ) {

		List<Map<String, Object>> txndef = transDefService.getFartherTranDef( txnid );
		String[] ids = TransCommon.cutToIds( txnid );
		String sql = buildUnionSql( ids.length );
		List<Map<String, Object>> allFd = tmsSimpleDao.queryForList( sql, ids );

		String[] idPlusName = addTxnName( ids, txndef );
		List<Map<String, Object>> fd = new ArrayList<Map<String, Object>>();

		for( int i = 0; i < ids.length; i++ ) {

			List<Map<String, Object>> par = new ArrayList<Map<String, Object>>();
			Map<String, Object> subMap = new HashMap<String, Object>();

			for( Map<String, Object> afd : allFd ) {
				// 抽取节点字段,,从最上层开始
				if( ids[i].equals( MapUtil.getObject( afd, TMS_COM_TAB.TAB_NAME ) ) ) {
					String type = MapUtil.getString( afd, TMS_COM_FD.TYPE );
					String src_defvalue = String.valueOf( afd.get( TMS_COM_FD.SRC_DEFAULT ) );
					// lining 当前台默认值输入框中值为""||''时，认为是空字符串，无值时。认为是NULL
					if( !Arrays.asList( tmsNumberType ).equals( type.toLowerCase() ) && "".equals( src_defvalue ) ) {
						afd.put( TMS_COM_FD.SRC_DEFAULT, "\"\"" );
					}
					par.add( afd );
				}
			}

			subMap.put( idPlusName[i], par );
			fd.add( subMap );
		}

		return fd;
	}

	/*
	 * 把数组的主键对应的交易名称查出来 以便前端显示
	 */
	private String[] addTxnName( String[] txnids, List<Map<String, Object>> txndefs ) {

		String[] idPlusName = new String[txnids.length];

		for( int i = 0; i < txnids.length; i++ ) {

			for( Map<String, Object> txndef : txndefs ) {

				// if(txndef.containsValue(txnids[i])) {
				if( MapUtil.getString( txndef, TMS_COM_TAB.TAB_NAME ).equals( txnids[i] ) ) {
					if( i > 0 ) {
						String lastname = idPlusName[i - 1].split( "____" )[1];
						idPlusName[i] = txnids[i] + "____" + lastname + StaticParameters.SIGNPOST + MapUtil.getString( txndef, TMS_COM_TAB.TAB_DESC );
					}
					else idPlusName[i] = txnids[i] + "____" + MapUtil.getString( txndef, TMS_COM_TAB.TAB_DESC );
				}
			}
		}

		return idPlusName;
	}

	/*
	 * 无数据时候,先查询 再补全名字
	 */
	private String[] addTxnName( String[] txnids ) {

		List<Map<String, Object>> txndefs = transDefService.getFartherTranDefName( txnids[txnids.length - 1] );

		String[] idPlusName = new String[txnids.length];

		for( int i = 0; i < txnids.length; i++ ) {

			for( Map<String, Object> txndef : txndefs ) {

				if( MapUtil.getString( txndef, TMS_COM_TAB.TAB_NAME ).equals( txnids[i] ) ) {

					idPlusName[i] = txnids[i] + "____" + MapUtil.getString( txndef, TMS_COM_TAB.TAB_DESC );
				}
			}
		}

		return idPlusName;
	}

	/**
	 * 递归取当前id所有父 交易的定义id,名
	 */
	private List<Map<String, Object>> getFartherTranDef( String txnid ) {

		// --子取父
		// select * from tms_com_tab t start with t.tab_name='T01010101'
		// connect by prior t.parent_tab=t.tab_name
		//
		// --父取子
		// select * from tms_com_tab t start with t.tab_name='T'
		// connect by prior t.tab_name=t.parent_tab

		StringBuffer sb = new StringBuffer();
		sb.append( "select " + TMS_COM_TAB.TAB_NAME + "," + TMS_COM_TAB.TAB_DESC + " from " + TMS_COM_TAB.TABLE_NAME + " where " + TMS_COM_TAB.TAB_NAME + " in("
				+ TransCommon.arr2str( TransCommon.cutToIds( txnid ) ) + ")" );

		return tmsSimpleDao.queryForList( sb.toString(), txnid );
	}

	/*
	 * 组装union sql
	 */
	private String buildUnionSql( int index ) {

		// select * from (select * from TMS_COM_FD where TAB_NAME='T' order by
		// orderby)
		// UNION select * from (select * from TMS_COM_FD where TAB_NAME='T01'
		// order by orderby)

		StringBuffer sb1 = new StringBuffer();
		sb1.append( "select " );
		sb1.append( " t." ).append( TMS_COM_FD.TAB_NAME ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.FD_NAME ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.REF_NAME ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.NAME ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.FD_DESC ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.TYPE ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.DB_TYPE ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.DB_LEN ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.CODE ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.IS_KEY ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.IS_NULL ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.IS_LIST ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.IS_QUERY ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.ORDERBY ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.LINK ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.SRC_ID ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.SRC_TYPE ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.SRC_DEFAULT ).append( "," );
		sb1.append( " t." ).append( TMS_COM_FD.GENESISRUL ).append( "," );

		sb1.append( " t." ).append( TMS_COM_FD.IS_SYS ).append( " IS_SYS," );
		sb1.append( " t." ).append( TMS_COM_FD.TXN_ORDER ).append( " TXN_ORDER " );
		sb1.append( " from " ).append( TMS_COM_FD.TABLE_NAME );
		sb1.append( " t where t." ).append( TMS_COM_FD.TAB_NAME );
		sb1.append( " = ? " );

		StringBuffer sb = new StringBuffer( sb1 );

		for( ; --index > 0; ) {

			sb.append( "UNION " );
			sb.append( sb1 );
		}
		return sb.append( " ORDER BY IS_SYS, TXN_ORDER" ).toString();
	}

	public List<Map<String, Object>> getTranModelsRef( String txnid ) {
		// select ref.*, tab.ref_desc, tab.ref_tab_name, tab.src_expr,
		// (select tmdl.tab_desc from tms_com_tab tmdl where
		// tmdl.tab_name=ref.tab_name) TAB_DESC
		// from tms_com_reffd ref, tms_com_reftab tab where
		// tab.ref_id=ref.ref_id

		// select t.*, ee.tab_desc TAB_DESC from TMS_COM_REFFD t left join
		// tms_com_tab ee on t.tab_name=ee.tab_name

		StringBuffer sb = new StringBuffer();

		sb.append( "select " );
		sb.append( " ref.*, tab.tab_desc TAB_DESC " );
		sb.append( " from " );
		sb.append( TMS_COM_REFFD.TABLE_NAME ).append( " ref left join tms_com_tab tab " );
		sb.append( " on ref.tab_name=tab.tab_name " );
		sb.append( " order by ref." ).append( TMS_COM_REFFD.TAB_NAME );

		List<Map<String, Object>> allRefFd = tmsSimpleDao.queryForList( sb.toString() );

		return allRefFd;
	}

	public List<Map<String, Object>> getTranModelsRefTab( String txnid ) {

		StringBuffer sb = new StringBuffer();

		sb.append( "select " );
		sb.append( "tab.*, " );

		sb.append( " (SELECT " );
		sb.append( TMS_COM_TAB.TAB_DESC );
		sb.append( " FROM " ).append( TMS_COM_TAB.TABLE_NAME ).append( " tmdl" );
		sb.append( " WHERE " );
		sb.append( " tab." ).append( TMS_COM_REFTAB.TAB_NAME ).append( "=" ).append( "tmdl." ).append( TMS_COM_TAB.TAB_NAME );
		sb.append( " ) TAB_DESC " );

		sb.append( "from " );
		sb.append( TMS_COM_REFTAB.TABLE_NAME ).append( " tab " );

		List<Map<String, Object>> allRefFd = tmsSimpleDao.queryForList( sb.toString() );

		return buildRefTab( allRefFd, txnid );
	}

	private List<Map<String, Object>> buildRefTab( List<Map<String, Object>> allRefFd, String txnid ) {

		String[] txnids = TransCommon.cutToIds( txnid );
		String[] idPlusName = addTxnName( txnids );

		for( Map<String, Object> col : allRefFd ) {

			if( MapUtil.getString( col, TMS_COM_REFTAB.TAB_NAME ).length() <= txnid.length() ) { // 父集||自己

				List<Map<String, Object>> fd = new ArrayList<Map<String, Object>>();

				for( int i = 0; i < txnids.length; i++ ) {

					List<Map<String, Object>> par = new ArrayList<Map<String, Object>>();
					Map<String, Object> subMap = new HashMap<String, Object>();

					for( Map<String, Object> afd : allRefFd ) {

						// 抽取节点字段,,从最上层开始
						if( txnids[i].equals( MapUtil.getString( afd, TMS_COM_REFTAB.TAB_NAME ) ) ) {
							par.add( afd );
						}
					}

					subMap.put( idPlusName[i], par );
					fd.add( subMap );
				}

				return fd;
			}
			else { // 子集

				continue;
			}
		}

		return null;
	}

	public List<Map<String, Object>> getCanRefTable() {
		// select fd.fd_name,fd.name,fd.tab_name, tab.tab_desc
		// from tms_com_fd fd, tms_com_tab tab
		// where fd.tab_name=tab.tab_name and tab.can_ref='1'
		// order by tab.show_order, fd.txn_order
		StringBuffer sb = new StringBuffer();

		sb.append( "select " );
		sb.append( "fd." ).append( TMS_COM_FD.TYPE ).append( ", " );
		sb.append( "fd." ).append( TMS_COM_FD.FD_NAME ).append( ", " );
		sb.append( "fd." ).append( TMS_COM_FD.NAME ).append( ", " );
		sb.append( "fd." ).append( TMS_COM_FD.TAB_NAME ).append( ", " );
		sb.append( "tab." ).append( TMS_COM_TAB.TAB_DESC ).append( " " );
		sb.append( "from " );
		sb.append( TMS_COM_TAB.TABLE_NAME ).append( " tab, " );
		sb.append( TMS_COM_FD.TABLE_NAME ).append( " fd " );
		sb.append( "where " );
		sb.append( "fd." ).append( TMS_COM_FD.TAB_NAME ).append( "=" ).append( "tab." ).append( TMS_COM_TAB.TAB_NAME ).append( " and " );
		sb.append( "tab." ).append( TMS_COM_TAB.CAN_REF ).append( "=" ).append( "1 " );
		sb.append( "order by " );
		sb.append( "tab." ).append( TMS_COM_TAB.SHOW_ORDER ).append( ", " );
		// sb.append("fd.").append(TMS_COM_FD.IS_SYS).append(", ");
		sb.append( "fd." ).append( TMS_COM_FD.TXN_ORDER );

		return tmsSimpleDao.queryForList( sb.toString() );
	}

	private List<Map<String, Object>> buildRefFd( List<Map<String, Object>> allRefFd, String txnid ) {

		String[] txnids = TransCommon.cutToIds( txnid );
		String[] idPlusName = addTxnName( txnids );

		for( Map<String, Object> col : allRefFd ) {

			if( MapUtil.getString( col, TMS_COM_REFTAB.REF_ID ).length() <= txnid.length() ) { // 父集||自己

				List<Map<String, Object>> fd = new ArrayList<Map<String, Object>>();

				for( int i = 0; i < txnids.length; i++ ) {

					List<Map<String, Object>> par = new ArrayList<Map<String, Object>>();
					Map<String, Object> subMap = new HashMap<String, Object>();

					for( Map<String, Object> afd : allRefFd ) {
						// 抽取节点字段,,从最上层开始
						if( txnids[i].equals( MapUtil.getString( afd, TMS_COM_REFFD.TAB_NAME ) ) ) {
							par.add( afd );
						}
					}

					subMap.put( idPlusName[i], par );
					fd.add( subMap );
				}

				return fd;
			}
			else { // 子集

				continue;
			}
		}

		return null;
	}

	public List<Map<String, Object>> getFunc() {

		return tmsSimpleDao.queryForList( "select * from tms_com_func where FUNC_CATALOG='5' order by func_orderby" );
	}

	public List<Map<String, Object>> getFuncParam() {

		return tmsSimpleDao.queryForList(
				"select pa.*, ss.FUNC_CODE from tms_com_funcparam pa,(select t.* from tms_com_func t where t.func_catalog='5') ss where pa.func_id = ss.func_id and pa.param_orderby > 0" );
	}

	private String getLink( String fdName, String type, List<Map<String, Object>> modeldata ) {

		for( Map<String, Object> col : modeldata ) {

			if( fdName.equals( MapUtil.getString( col, TMS_COM_FD.FD_NAME ) ) ) {

				if( type.equals( MapUtil.getString( col, TMS_COM_FD.TYPE ) ) ) {// fdname和type等于原来的,把原来的link返回

					return MapUtil.getString( col, TMS_COM_FD.LINK );
				}
				else {// fdname相等但type变了,返回空
					return "";
				}
			}
		}
		return "";
	}

	private String getOrderBy( String orderby, List<Map<String, Object>> modeldata, int getOrder_count ) {

		int ordertmp = 0;

		if( StringUtil.isNotEmpty( orderby ) ) {
			return orderby;
		}
		else {

			for( Map<String, Object> col : modeldata ) {
				int order = MapUtil.getInteger( col, TMS_COM_FD.ORDERBY );
				ordertmp = ordertmp > order ? ordertmp : order;
			}
			return (ordertmp + getOrder_count) + "";
		}
	}

	/*
	 * type=1 as DB_TYPE type!=1 as DB_LEN
	 */
	private String getDBTypeAndLen( String fdName, List<Map<String, Object>> tfcs, String type ) {

		for( Map<String, Object> tfc : tfcs ) {

			String tfcCol = MapUtil.getString( tfc, TMS_COM_FD.FD_NAME );

			if( fdName.equals( tfcCol ) ) {
				if( "1".equals( type ) ) {
					return MapUtil.getString( tfc, TMS_COM_FD.DB_TYPE );
				}
				else {
					return MapUtil.getString( tfc, TMS_COM_FD.DB_LEN );
				}
			}
		}
		return "";
	}

	/*
	 * 取包含在TMS_COM_FD的x表结构
	 */
	private List<Map<String, Object>> getModel( String tabName ) {

		StringBuffer sb1 = new StringBuffer();
		sb1.append( "select *" );
		sb1.append( " from " ).append( TMS_COM_FD.TABLE_NAME );
		sb1.append( " where " ).append( TMS_COM_FD.TAB_NAME );
		sb1.append( "='" ).append( tabName ).append( "'" );
		sb1.append( " order by " ).append( TMS_COM_FD.TXN_ORDER );

		return tmsSimpleDao.queryForList( sb1.toString() );
	}

	private String idsToSqlIn( String... ids ) {

		StringBuffer sb = new StringBuffer();
		for( String id : ids ) {
			sb.append( "'" ).append( id ).append( "'" ).append( "," );
		}
		sb.deleteCharAt( sb.length() - 1 );
		return sb.toString();
	}

	public List<Map<String, Object>> getAllStoreFd() {
		List<Map<String, Object>> traffic = getModel( DBConstant.TMS_RUN_TRAFFICDATA );
		return traffic;
	}

	public List<Map<String, Object>> getAvailableStoreFd( String tab_name ) {

		List<Map<String, Object>> traffic = getModel( DBConstant.TMS_RUN_TRAFFICDATA );
		List<Map<String, Object>> modeldata = getSelfAndSubFd( tab_name );

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		// TODO TransDefServiceImpl.getImmediateFamilyAttrInfos refact it
		// and remove,模型字段
		for( Map<String, Object> ec : modeldata ) {
			String db_tab_name = MapUtil.getString( ec, "TAB_NAME" );
			if( !db_tab_name.startsWith( "TMS" ) ) {
				list.add( ec );
			}
		}

		// 引用字段
		List<Map<String, Object>> refFd = getSelfAndSubRefFd( tab_name );
		if( refFd != null && refFd.size() > 0 ) {
			list.addAll( refFd );
		}
		// 统计字段
		List<Map<String, Object>> statFd = getSelfAndSubStatFd( tab_name );
		if( statFd != null && statFd.size() > 0 ) {
			list.addAll( statFd );
		}

		for( Map<String, Object> eachModelCol : list ) {

			String fd_name = MapUtil.getString( eachModelCol, "FD_NAME" );
			boolean hasYet = false;

			for( Map<String, Object> eachTraffic : traffic ) {
				String t_fd_name = MapUtil.getString( eachTraffic, "FD_NAME" );
				if( t_fd_name.equals( fd_name ) ) {
					traffic.remove( eachTraffic );
					break;
				}
			}
		}

		return traffic;
	}

	public List<Map<String, Object>> getCanRefFd( String tab_name ) {

		StringBuffer sb1 = new StringBuffer();
		sb1.append( "select *" );
		sb1.append( " from " ).append( TMS_COM_FD.TABLE_NAME );
		sb1.append( " where " ).append( TMS_COM_FD.TAB_NAME );
		sb1.append( " in (" ).append( TransCommon.cutToIdsForSql( tab_name ) ).append( ")" );

		return tmsSimpleDao.queryForList( sb1.toString() );

	}

	public List<Map<String, Object>> getSelfAndSubFd( String tab_name ) {

		StringBuffer sb1 = new StringBuffer();
		sb1.append( "select *" );
		sb1.append( " from " ).append( TMS_COM_FD.TABLE_NAME );
		sb1.append( " where (" ).append( TMS_COM_FD.TAB_NAME );
		sb1.append( " in (" ).append( TransCommon.cutToIdsForSql( tab_name ) ).append( ")" );
		sb1.append( " or " ).append( TMS_COM_FD.TAB_NAME ).append( " like '" ).append( tab_name ).append( "%')" );

		return tmsSimpleDao.queryForList( sb1.toString() );
	}

	public List<Map<String, Object>> getSelfAndSubRefFd( String tab_name ) {

		StringBuffer sb1 = new StringBuffer();
		sb1.append( "select STORECOLUMN FD_NAME " );
		sb1.append( " from " ).append( TMS_COM_REFFD.TABLE_NAME );
		sb1.append( " where (" ).append( TMS_COM_REFFD.TAB_NAME );
		sb1.append( " in (" ).append( TransCommon.cutToIdsForSql( tab_name ) ).append( ")" );
		sb1.append( " or " ).append( TMS_COM_REFFD.TAB_NAME ).append( " like '" ).append( tab_name ).append( "%')" );

		return tmsSimpleDao.queryForList( sb1.toString() );
	}

	public List<Map<String, Object>> getSelfAndSubStatFd( String tab_name ) {

		StringBuffer sb1 = new StringBuffer();
		sb1.append( "select STORECOLUMN FD_NAME " );
		sb1.append( " from " ).append( "TMS_COM_STAT" );
		sb1.append( " where (" ).append( "STAT_TXN" );
		sb1.append( " in (" ).append( TransCommon.cutToIdsForSql( tab_name ) ).append( ")" );
		sb1.append( " or " ).append( "STAT_TXN" ).append( " like '" ).append( tab_name ).append( "%')" );

		return tmsSimpleDao.queryForList( sb1.toString() );
	}

	@Transactional
	public Map<String, String> saveModel( Map formMap ) {

		List delList = MapUtil.getList( formMap, "del" );
		List modList = MapUtil.getList( formMap, "mod" );
		List addList = MapUtil.getList( formMap, "add" );

		Map m = new HashMap();

		if( delList != null && delList.size() > 0 ) {
			m = deleteModel( delList );
		}
		if( addList != null && addList.size() > 0 ) {
			m = addModel( addList );
		}
		if( modList != null && modList.size() > 0 ) {
			m = updateModel( modList );
		}
		return m;
	}

	@Transactional
	public Map saveModelRefTab( Map formMap ) {

		List delList = MapUtil.getList( formMap, "del" );
		List modList = MapUtil.getList( formMap, "mod" );
		List addList = MapUtil.getList( formMap, "add" );

		Map m = new HashMap();

		if( delList != null && delList.size() > 0 ) {
			m = deleteRefModel( delList );
		}
		if( addList != null && addList.size() > 0 ) {
			m = addRefModel( addList );
		}
		if( modList != null && modList.size() > 0 ) {
			m = updateRefModel( modList );
		}
		return m;
	}

	private Map updateRefModel( List update ) {

		Map m = (Map) update.get( 0 );

		String ref_desc = MapUtil.getString( m, TMS_COM_REFTAB.REF_DESC );
		String tab_name = MapUtil.getString( m, TMS_COM_REFTAB.TAB_NAME );
		String ref_id = MapUtil.getString( m, TMS_COM_REFTAB.REF_ID );
		String txn_order = MapUtil.getString( m, TMS_COM_REFTAB.TXN_ORDER );

		transDefService.checkSingleParentAndAllSubSame( ref_id, ref_desc, tab_name, false, "reftab", false );

		m.put( TMS_COM_REFTAB.REF_ID, ref_id.length() > 0 ? Long.parseLong( ref_id ) : ref_id );
		m.put( TMS_COM_REFTAB.TXN_ORDER, txn_order.length() > 0 ? Long.parseLong( txn_order ) : 0 );

		tmsSimpleDao.batchUpdate( buildUpdateRefModelSql(), update );
		return m;
	}

	private String buildUpdateRefModelSql() {
		StringBuffer mdlSb = new StringBuffer();
		mdlSb.append( "update " ).append( TMS_COM_REFTAB.TABLE_NAME );
		mdlSb.append( " set " );
		mdlSb.append( TMS_COM_REFTAB.REF_DESC ).append( "=:REF_DESC," );
		mdlSb.append( TMS_COM_REFTAB.TAB_NAME ).append( "=:TAB_NAME," );
		mdlSb.append( TMS_COM_REFTAB.REF_TAB_NAME ).append( "=:REF_TAB_NAME," );
		mdlSb.append( TMS_COM_REFTAB.SRC_EXPR ).append( "=:SRC_EXPR," );
		mdlSb.append( TMS_COM_REFTAB.TXN_ORDER ).append( "=:TXN_ORDER" );

		mdlSb.append( " where " );
		mdlSb.append( TMS_COM_REFTAB.REF_ID ).append( "=:REF_ID" );

		return mdlSb.toString();
	}

	private Map addRefModel( List add ) {

		Map m = (Map) add.get( 0 );
		String tab_name = MapUtil.getString( m, TMS_COM_REFTAB.TAB_NAME );
		String ref_desc = MapUtil.getString( m, TMS_COM_REFTAB.REF_DESC );

		transDefService.checkSingleParentAndAllSubSame( null, ref_desc, tab_name, false, "reftab", true );

		Long seqid = Long.parseLong( getSeqId( DBConstant.SEQ_TMS_COM_REFTAB_ID ) );
		m.put( TMS_COM_REFTAB.REF_ID, seqid );
		tmsSimpleDao.batchUpdate( buildAddRefModelSql(), add );
		return m;
	}

	private String buildAddRefModelSql() {

		StringBuffer mdlSb = new StringBuffer();
		mdlSb.append( "insert into " ).append( TMS_COM_REFTAB.TABLE_NAME );
		mdlSb.append( " (" );
		mdlSb.append( TMS_COM_REFTAB.REF_ID ).append( "," );
		mdlSb.append( TMS_COM_REFTAB.REF_DESC ).append( "," );
		mdlSb.append( TMS_COM_REFTAB.TAB_NAME ).append( "," );
		mdlSb.append( TMS_COM_REFTAB.REF_TAB_NAME ).append( "," );
		mdlSb.append( TMS_COM_REFTAB.SRC_EXPR );
		mdlSb.append( ") " );

		mdlSb.append( "values (:REF_ID,:REF_DESC,:TAB_NAME,:REF_TAB_NAME,:SRC_EXPR)" );

		return mdlSb.toString();
	}

	private Map deleteRefModel( List delete ) {

		for( int i = 0; i < delete.size(); i++ ) {

			Map m = (Map) delete.get( i );
			String ref_id = MapUtil.getString( m, "REF_ID" );
			checkRefTabRefed( ref_id );
		}

		tmsSimpleDao.executeUpdate( buildDeleteRefModelSql( delete ) );
		return null;
	}

	private void checkRefTabRefed( String ref_id ) {

		StringBuffer sb1 = new StringBuffer();
		sb1.append( "select reffd.*, tab.tab_desc tab_desc " );
		sb1.append( " from " ).append( TMS_COM_REFFD.TABLE_NAME );
		sb1.append( " reffd left join TMS_COM_TAB tab on tab.tab_name=reffd.tab_name where " ).append( TMS_COM_REFFD.REF_ID );
		sb1.append( " = ?" );

		List<Map<String, Object>> ref_fds = tmsSimpleDao.queryForList( sb1.toString(), Long.parseLong( ref_id ) );

		if( ref_fds.size() > 0 ) {
			throw new TmsMgrServiceException( "当前表存在" + MapUtil.getString( ref_fds.get( 0 ), "tab_desc" ) + "引用字段:" + MapUtil.getString( ref_fds.get( 0 ), "REF_NAME" ) );
		}

	}

	private String buildDeleteRefModelSql( List delete ) {

		StringBuffer id_sb = new StringBuffer( "" );
		StringBuffer dlteSqlSb = new StringBuffer();
		dlteSqlSb.append( "DELETE FROM " );
		dlteSqlSb.append( TMS_COM_REFTAB.TABLE_NAME ).append( " " );
		dlteSqlSb.append( "WHERE " );
		dlteSqlSb.append( TMS_COM_REFTAB.REF_ID ).append( " in (" );
		for( int i = 0; i < delete.size(); i++ ) {
			Map dlt = (Map) delete.get( i );
			String id = MapUtil.getString( dlt, TMS_COM_REFTAB.REF_ID );
			id_sb.append( id ).append( "," );
		}
		id_sb.append( ")" );
		id_sb.setCharAt( id_sb.lastIndexOf( "," ), ' ' );
		dlteSqlSb.append( id_sb );

		return dlteSqlSb.toString();
	}

	@Transactional
	public Map saveModelRefFd( Map formMap ) {

		List delList = MapUtil.getList( formMap, "del" );
		List modList = MapUtil.getList( formMap, "mod" );
		List addList = MapUtil.getList( formMap, "add" );

		Map m = new HashMap();

		if( delList != null && delList.size() > 0 ) {
			m = deleteRefFdModel( delList );
		}
		if( addList != null && addList.size() > 0 ) {
			m = addRefFdModel( addList );
		}
		if( modList != null && modList.size() > 0 ) {
			m = updateRefFdModel( modList );
		}
		return m;
	}

	private Map updateRefFdModel( List update ) {

		Map m = (Map) update.get( 0 );
		Map old = (Map) m.get( "old" );

		String src_cond = MapUtil.getString( m, "SRC_COND" );
		String src_expr = MapUtil.getString( m, "SRC_EXPR" );
		String desc = MapUtil.getString( m, "REF_DESC" );
		String txnid = MapUtil.getString( m, "TAB_NAME" );
		String tab_name = MapUtil.getString( m, "TAB_NAME" );
		String ref_name = MapUtil.getString( m, "REF_NAME" ).toUpperCase();
		;
		String ref_id = MapUtil.getString( m, TMS_COM_REFTAB.REF_ID );

		String ref_name_old = MapUtil.getString( old, "REF_NAME" ).toUpperCase();
		String desc_old = MapUtil.getString( old, "REF_DESC" );

		if( StringUtil.isNotEmpty( src_cond ) ) {
			commonCheckService.checkCond( src_cond, desc, "条件", txnid );
		}
		if( StringUtil.isNotEmpty( src_expr ) ) {
			commonCheckService.checkCond( src_expr, desc, txnid );
		}

		if( !ref_name.equals( ref_name_old ) ) { // 修改了ref_name,src_id,fd_name,type,而且

			// 1.旧值引用
			checkRefNameBeRefed( tab_name, ref_name_old, "0" );
			// 2.新值重复
			transDefService.checkSingleParentAndAllSubSame( ref_name_old + "|" + tab_name + "|" + ref_id, ref_name, tab_name, false, "reffd", false );
			transDefService.checkSingleParentAndAllSubSame( ref_name_old + "|" + tab_name, ref_name, tab_name, false, "fd", false );
		}

		if( !desc.equals( desc_old ) ) { // 修改了ref_desc
			transDefService.checkSingleParentAndAllSubSame( ref_name_old + "|" + tab_name + "|" + ref_id, desc, tab_name, false, "reffd_ref_desc", false );
			transDefService.checkSingleParentAndAllSubSame( ref_name_old + "|" + tab_name, desc, tab_name, false, "fd_name", false );
		}

		if( isDuplicateByTransAttrAndRoster( ref_name ) ) {
			throw new TmsMgrServiceException( "属性代码和名单管理中[名单英文名称]重复" );
		}

		m.put( "REF_NAME_OLD", ref_name_old );

		m.put( TMS_COM_REFFD.REF_ID, MapUtil.getLong( m, TMS_COM_REFFD.REF_ID ) );
		m.put( TMS_COM_REFFD.REF_NAME, ref_name );
		tmsSimpleDao.batchUpdate( buildUpdateRefFdModelSql(), update );
		m.remove( "old" );
		m.remove( "REF_NAME_OLD" );
		return m;
	}

	private String buildUpdateRefFdModelSql() {

		StringBuffer mdlSb = new StringBuffer();
		mdlSb.append( "update " ).append( TMS_COM_REFFD.TABLE_NAME );
		mdlSb.append( " set " );
		mdlSb.append( TMS_COM_REFFD.TAB_NAME ).append( "=:TAB_NAME," );
		mdlSb.append( TMS_COM_REFFD.REF_NAME ).append( "=:REF_NAME," );
		mdlSb.append( TMS_COM_REFFD.REF_FD_NAME ).append( "=:REF_FD_NAME," );
		mdlSb.append( TMS_COM_REFFD.SRC_COND ).append( "=:SRC_COND," );
		mdlSb.append( TMS_COM_REFFD.SRC_EXPR ).append( "=:SRC_EXPR," );
		mdlSb.append( TMS_COM_REFFD.SRC_COND_IN ).append( "=:SRC_COND_IN," );
		mdlSb.append( TMS_COM_REFFD.SRC_EXPR_IN ).append( "=:SRC_EXPR_IN," );
		mdlSb.append( TMS_COM_REFFD.STORECOLUMN ).append( "=:STORECOLUMN," );
		mdlSb.append( TMS_COM_REFFD.REF_DESC ).append( "=:REF_DESC" );

		mdlSb.append( " where " );
		mdlSb.append( TMS_COM_REFFD.REF_ID ).append( "=:REF_ID and " );
		mdlSb.append( TMS_COM_REFFD.REF_NAME ).append( "=:REF_NAME_OLD" );

		return mdlSb.toString();
	}

	private Map addRefFdModel( List add ) {

		Map m = (Map) add.get( 0 );

		String src_cond = MapUtil.getString( m, "SRC_COND" );
		String src_expr = MapUtil.getString( m, "SRC_EXPR" );
		String desc = MapUtil.getString( m, "REF_DESC" );
		String txnid = MapUtil.getString( m, "TAB_NAME" );
		String tab_name = MapUtil.getString( m, TMS_COM_REFFD.TAB_NAME );
		String ref_name = MapUtil.getString( m, TMS_COM_REFFD.REF_NAME ).toUpperCase();

		if( StringUtil.isNotEmpty( src_cond ) ) {
			commonCheckService.checkCond( src_cond, desc, "条件", txnid );
		}
		if( StringUtil.isNotEmpty( src_expr ) ) {
			commonCheckService.checkCond( src_expr, desc, txnid );
		}

		transDefService.checkSingleParentAndAllSubSame( null, ref_name, tab_name, false, "reffd", true );
		transDefService.checkSingleParentAndAllSubSame( null, ref_name, tab_name, false, "fd", true );
		transDefService.checkSingleParentAndAllSubSame( null, desc, tab_name, false, "reffd_ref_desc", true );
		if( isDuplicateByTransAttrAndRoster( ref_name ) ) {
			throw new TmsMgrServiceException( "属性代码和名单管理中[名单英文名称]重复" );
		}

		m.put( TMS_COM_REFFD.REF_ID, MapUtil.getLong( m, TMS_COM_REFFD.REF_ID ) );// 和数据库数据类型保持一致
		m.put( TMS_COM_REFFD.REF_NAME, ref_name );

		tmsSimpleDao.batchUpdate( buildAddRefFdModelSql(), add );
		return m;
	}

	private String buildAddRefFdModelSql() {

		StringBuffer mdlSb = new StringBuffer();
		mdlSb.append( "insert into " ).append( TMS_COM_REFFD.TABLE_NAME );
		mdlSb.append( " (" );
		mdlSb.append( TMS_COM_REFFD.REF_ID ).append( "," );
		mdlSb.append( TMS_COM_REFFD.TAB_NAME ).append( "," );
		mdlSb.append( TMS_COM_REFFD.REF_NAME ).append( "," );
		mdlSb.append( TMS_COM_REFFD.REF_FD_NAME ).append( "," );
		mdlSb.append( TMS_COM_REFFD.SRC_COND ).append( "," );
		mdlSb.append( TMS_COM_REFFD.SRC_EXPR ).append( "," );
		mdlSb.append( TMS_COM_REFFD.SRC_COND_IN ).append( "," );
		mdlSb.append( TMS_COM_REFFD.SRC_EXPR_IN ).append( "," );
		mdlSb.append( TMS_COM_REFFD.STORECOLUMN ).append( "," );
		mdlSb.append( TMS_COM_REFFD.REF_DESC ).append( ")" );

		mdlSb.append( " values (:REF_ID,:TAB_NAME,:REF_NAME,:REF_FD_NAME,:SRC_COND,:SRC_EXPR,:SRC_COND_IN,:SRC_EXPR_IN,:STORECOLUMN,:REF_DESC)" );

		return mdlSb.toString();
	}

	private Map deleteRefFdModel( List delete ) {

		for( int i = 0; i < delete.size(); i++ ) {

			Map m = (Map) delete.get( i );
			String tab_name = MapUtil.getString( m, "TAB_NAME" );
			String ref_name = MapUtil.getString( m, "REF_NAME" );
			checkRefNameBeRefed( tab_name, ref_name, "1" );
		}

		tmsSimpleDao.executeUpdate( buildDeleteRefFdModelSql( delete ) );
		return null;
	}

	private String buildDeleteRefFdModelSql( List delete ) {

		// FIXME where (t='T' and ref_name='ABC') or (t='T01' and ref_name='bc')
		// ?

		StringBuffer id_sb = new StringBuffer( "" );
		StringBuffer dlteSqlSb = new StringBuffer();
		dlteSqlSb.append( "DELETE FROM " );
		dlteSqlSb.append( TMS_COM_REFFD.TABLE_NAME ).append( " " );
		dlteSqlSb.append( "WHERE " );
		for( int i = 0; i < delete.size(); i++ ) {
			Map dlt = (Map) delete.get( i );
			dlteSqlSb.append( "( " );
			dlteSqlSb.append( TMS_COM_FD.REF_NAME ).append( "='" ).append( dlt.get( TMS_COM_FD.REF_NAME ) ).append( "' " );
			dlteSqlSb.append( "AND " );
			dlteSqlSb.append( TMS_COM_FD.TAB_NAME ).append( "='" ).append( dlt.get( TMS_COM_FD.TAB_NAME ) ).append( "' " );
			dlteSqlSb.append( ") " );
			dlteSqlSb.append( "or " );
		}
		String sql = dlteSqlSb.substring( 0, dlteSqlSb.lastIndexOf( "or" ) );
		// dlteSqlSb.setCharAt(dlteSqlSb.lastIndexOf("or"), ' ');

		return sql;
	}

	private String[] tmsNumberType = {
			"long", "double", "money", "date", "datetime"
	};

	/*
	 * 提交新数据
	 */
	private Map addModel( List add ) {

		Map m = (Map) add.get( 0 );
		String tab_name = MapUtil.getString( m, TMS_COM_FD.TAB_NAME );
		String fdName = MapUtil.getString( m, TMS_COM_FD.FD_NAME );
		String name = MapUtil.getString( m, TMS_COM_FD.NAME );
		String ref_name = MapUtil.getString( m, TMS_COM_FD.REF_NAME ).toUpperCase();
		String src_id = MapUtil.getString( m, TMS_COM_FD.SRC_ID ).toUpperCase();

		transDefService.checkSingleParentAndAllSubSame( null, ref_name, tab_name, false, "fd", true );
		transDefService.checkSingleParentAndAllSubSame( null, ref_name, tab_name, false, "reffd", true );
		transDefService.checkSingleParentAndAllSubSame( null, name, tab_name, false, "fd_name", true );
		if( isDuplicateByTransAttrAndRoster( ref_name ) ) {
			throw new TmsMgrServiceException( "属性代码和名单管理中[名单英文名称]重复" );
		}

		List<Map<String, Object>> traffic = getModel( DBConstant.TMS_RUN_TRAFFICDATA );
		List<Map<String, Object>> modeldata = getModel( tab_name );
		int count = 1;
		String orderby = getOrderBy( MapUtil.getString( m, TMS_COM_FD.ORDERBY ), modeldata, count++ );

		m.put( TMS_COM_FD.DB_TYPE, getDBTypeAndLen( fdName, traffic, "1" ) );
		m.put( TMS_COM_FD.DB_LEN, Integer.parseInt( getDBTypeAndLen( fdName, traffic, "2" ) ) );
		m.put( TMS_COM_FD.ORDERBY, Integer.parseInt( orderby ) );
		m.put( TMS_COM_FD.REF_NAME, ref_name );
		m.put( TMS_COM_FD.SRC_ID, src_id );
		m.put( TMS_COM_FD.TXN_ORDER, Integer.valueOf( orderby ) );
		m.put( TMS_COM_FD.IS_KEY, 0 );
		m.put( TMS_COM_FD.IS_NULL, 1 );
		m.put( TMS_COM_FD.IS_LIST, 1 );
		m.put( TMS_COM_FD.IS_QUERY, 1 );
		m.put( TMS_COM_FD.IS_SYS, 0 );
		m.put( TMS_COM_FD.FD_DESC, "" );
		m.put( TMS_COM_FD.LINK, "" );
		m.put( TMS_COM_FD.SRC_TYPE, "" );
		String type = MapUtil.getString( m, TMS_COM_FD.TYPE );
		String src_defvalue = MapUtil.getString( m, TMS_COM_FD.SRC_DEFAULT );
		// lining 当前台默认值输入框中值为""||''时，认为是空字符串，无值时。认为是NULL
		if( "".equals( src_defvalue ) ) {
			m.put( TMS_COM_FD.SRC_DEFAULT, null );
		}
		else if( "''".equals( src_defvalue ) || "\"\"".equals( src_defvalue ) ) {
			if( Arrays.asList( tmsNumberType ).equals( type.toLowerCase() ) ) {
				m.put( TMS_COM_FD.SRC_DEFAULT, null );
			}
			else {
				m.put( TMS_COM_FD.SRC_DEFAULT, "" );
			}
		}
		tmsSimpleDao.batchUpdate( buildAddModelSql(), add );
		if( "''".equals( src_defvalue ) || "\"\"".equals( src_defvalue ) ) {
			if( !Arrays.asList( tmsNumberType ).equals( type.toLowerCase() ) ) {
				m.put( TMS_COM_FD.SRC_DEFAULT, "\"\"" );
			}
		}
		else {
			m.put( TMS_COM_FD.SRC_DEFAULT, "" );
		}
		return m;
	}

	/*
	 * 修改数据
	 */
	private Map updateModel( List update ) {

		Map m = (Map) update.get( 0 );
		Map old = (Map) m.get( "old" );
		/*
		 * 用old值当作联合主键查询被修改记录
		 */
		String ref_name_old = MapUtil.getString( old, "REF_NAME" ).toUpperCase();
		String name_old = MapUtil.getString( old, "NAME" );
		String src_id_old = MapUtil.getString( old, "SRC_ID" ).toUpperCase();
		String fd_name_old = MapUtil.getString( old, "FD_NAME" );
		String type_old = MapUtil.getString( old, "TYPE" );

		String fd_name = MapUtil.getString( m, "FD_NAME" );
		String type = MapUtil.getString( m, "TYPE" );
		String ref_name = MapUtil.getString( m, "REF_NAME" ).toUpperCase();
		String name = MapUtil.getString( m, "NAME" );
		String src_id = MapUtil.getString( m, TMS_COM_FD.SRC_ID ).toUpperCase();

		String tab_name = MapUtil.getString( m, "TAB_NAME" );

		if( !ref_name.equals( ref_name_old ) || !src_id.equals( src_id_old ) || !fd_name.equals( fd_name_old ) || !type.equals( type_old ) ) { // 修改了ref_name,src_id,fd_name,type,而且

			// 1.旧值引用
			checkRefNameBeRefed( tab_name, ref_name_old, "0" );
			// 2.新值重复
			transDefService.checkSingleParentAndAllSubSame( ref_name_old + "|" + tab_name, ref_name, tab_name, false, "fd", false );
			transDefService.checkSingleParentAndAllSubSame( ref_name_old + "|" + tab_name, ref_name, tab_name, false, "reffd", false );
		}
		if( !name.equals( name_old ) ) { // 修改了ref_name
			// 属性名称新值的重复
			transDefService.checkSingleParentAndAllSubSame( ref_name_old + "|" + tab_name, name, tab_name, false, "fd_name", false );
		}

		if( isDuplicateByTransAttrAndRoster( ref_name ) ) {
			throw new TmsMgrServiceException( "属性代码和名单管理中[名单英文名称]重复" );
		}

		m.put( "REF_NAME_OLD", ref_name_old );
		m.put( "SRC_ID", src_id );
		m.put( "REF_NAME", ref_name );
		// lining 当前台默认值输入框中值为""||''时，认为是空字符串，无值时。认为是NULL
		String src_defvalue = MapUtil.getString( m, TMS_COM_FD.SRC_DEFAULT );
		if( "".equals( src_defvalue ) ) {
			m.put( TMS_COM_FD.SRC_DEFAULT, null );
		}
		else if( "''".equals( src_defvalue ) || "\"\"".equals( src_defvalue ) ) {
			if( Arrays.asList( tmsNumberType ).equals( type.toLowerCase() ) ) {
				m.put( TMS_COM_FD.SRC_DEFAULT, null );
			}
			else {
				m.put( TMS_COM_FD.SRC_DEFAULT, "" );
			}
		}
		tmsSimpleDao.batchUpdate( buildUpdateModelSql(), update );
		if( "''".equals( src_defvalue ) || "\"\"".equals( src_defvalue ) ) {
			if( !Arrays.asList( tmsNumberType ).equals( type.toLowerCase() ) ) {
				m.put( TMS_COM_FD.SRC_DEFAULT, "\"\"" );
			}
		}
		else {
			m.put( TMS_COM_FD.SRC_DEFAULT, "" );
		}
		m.remove( "old" );
		m.remove( "REF_NAME_OLD" );
		return m;
	}

	/*
	 * 删除数据
	 */
	private Map deleteModel( List delete ) {

		for( int i = 0; i < delete.size(); i++ ) {

			Map m = (Map) delete.get( i );
			String tab_name = MapUtil.getString( m, "TAB_NAME" );
			String ref_name = MapUtil.getString( m, "REF_NAME" );
			checkRefNameBeRefed( tab_name, ref_name, "1" );
		}

		tmsSimpleDao.executeUpdate( buildDeleteModelSql( delete ), MapUtil.getString( (Map) delete.get( 0 ), "TAB_NAME" ) );
		return null;
	}

	public enum UserActionType {

		Del(1), Mod(2), Add(3);

		private int index;

		UserActionType( int idx ) {
			this.index = idx;
		}

		public int getIndex() {
			return index;
		}
	}

	/*
	 * 1. cond引用 2. 统计和动作引用
	 */
	private void checkRefNameBeRefed( String tab_name, String ref_name, String type ) {
		// 1
		List<db_fd> ref_fd = new ArrayList<db_fd>();
		List<db_rule_action> ref_act = new ArrayList<db_rule_action>();
		List<db_stat> ref_stat = new ArrayList<db_stat>();
		List<db_rule> ref_rule = new ArrayList<db_rule>();
		List<db_strategy> ref_sw = new ArrayList<db_strategy>();

		commonCheckService.findRefField( tab_name, ref_name, ref_fd, ref_act, ref_stat, ref_rule, ref_sw );

		if( ref_fd.size() > 0 || ref_act.size() > 0 || ref_stat.size() > 0 || ref_rule.size() > 0 || ref_sw.size() > 0 ) {
			throw new TmsMgrServiceException( "[" + ref_name + "]被引用，不能" + ("1".equals( type ) ? "删除" : "编辑") );
		}

		// 2
		// String findStatRefSql = "SELECT * FROM TMS_COM_STAT st WHERE
		// st.stat_param=? or st.stat_datafd=? ";
		String findStatRefSql = "SELECT * FROM TMS_COM_STAT WHERE stat_datafd=? and stat_txn like '" + tab_name + "%'";
		// String findAcRefSql = "SELECT * FROM TMS_COM_ACTION WHERE ac_src=?
		// and ac_txn like '" + tab_name + "%'";
		// long acRef = tmsSimpleDao.count(findAcRefSql, ref_name);
		long statRef = tmsSimpleDao.count( findStatRefSql, ref_name );
		// TODO 分别报错
		if( statRef > 0 ) {
			throw new TmsMgrServiceException( "[" + ref_name + "]被引用，不能" + ("1".equals( type ) ? "删除" : "编辑") );
		}
	}

	/*
	 * 组装添加switch的sql语句
	 */
	private String buildAddModelSql() {

		StringBuffer mdlSb = new StringBuffer();
		mdlSb.append( "insert into " ).append( TMS_COM_FD.TABLE_NAME );
		mdlSb.append( " (" );
		mdlSb.append( TMS_COM_FD.REF_NAME ).append( "," );
		mdlSb.append( TMS_COM_FD.NAME ).append( "," );
		mdlSb.append( TMS_COM_FD.TYPE ).append( "," );
		mdlSb.append( TMS_COM_FD.CODE ).append( "," );
		mdlSb.append( TMS_COM_FD.SRC_ID ).append( "," );
		mdlSb.append( TMS_COM_FD.SRC_DEFAULT ).append( "," );
		mdlSb.append( TMS_COM_FD.FD_NAME ).append( "," );
		mdlSb.append( TMS_COM_FD.GENESISRUL ).append( "," );
		mdlSb.append( TMS_COM_FD.DB_TYPE ).append( "," );
		mdlSb.append( TMS_COM_FD.DB_LEN ).append( "," );
		mdlSb.append( TMS_COM_FD.ORDERBY ).append( "," );
		mdlSb.append( TMS_COM_FD.TXN_ORDER ).append( "," );
		mdlSb.append( TMS_COM_FD.LINK ).append( "," );
		mdlSb.append( TMS_COM_FD.TAB_NAME ).append( "," );
		mdlSb.append( TMS_COM_FD.IS_KEY ).append( "," );
		mdlSb.append( TMS_COM_FD.IS_NULL ).append( "," );
		mdlSb.append( TMS_COM_FD.IS_LIST ).append( "," );
		mdlSb.append( TMS_COM_FD.IS_QUERY ).append( "," );
		mdlSb.append( TMS_COM_FD.IS_SYS ).append( "," );
		mdlSb.append( TMS_COM_FD.FD_DESC ).append( "," );
		mdlSb.append( TMS_COM_FD.SRC_TYPE ).append( ") " );

		mdlSb.append( "values (" );
		mdlSb.append( ":REF_NAME,:NAME,:TYPE,:CODE,:SRC_ID,:SRC_DEFAULT,:FD_NAME,:GENESISRUL," + ":DB_TYPE,:DB_LEN,:ORDERBY,:TXN_ORDER,:LINK,"
				+ ":TAB_NAME,:IS_KEY,:IS_NULL,:IS_LIST,:IS_QUERY,:IS_SYS,:FD_DESC,:SRC_TYPE)" );

		return mdlSb.toString();
	}

	/*
	 * 组装保存switch的sql语句
	 */
	private String buildUpdateModelSql() {

		StringBuffer mdlSb = new StringBuffer();
		mdlSb.append( "update " ).append( TMS_COM_FD.TABLE_NAME );
		mdlSb.append( " set " );
		mdlSb.append( TMS_COM_FD.REF_NAME ).append( "=:REF_NAME," );
		mdlSb.append( TMS_COM_FD.NAME ).append( "=:NAME," );
		mdlSb.append( TMS_COM_FD.TYPE ).append( "=:TYPE," );
		mdlSb.append( TMS_COM_FD.CODE ).append( "=:CODE," );
		mdlSb.append( TMS_COM_FD.SRC_ID ).append( "=:SRC_ID," );
		mdlSb.append( TMS_COM_FD.SRC_DEFAULT ).append( "=:SRC_DEFAULT," );
		mdlSb.append( TMS_COM_FD.FD_NAME ).append( "=:FD_NAME," );
		mdlSb.append( TMS_COM_FD.GENESISRUL ).append( "=:GENESISRUL," );
		mdlSb.append( TMS_COM_FD.DB_TYPE ).append( "=:DB_TYPE," );
		mdlSb.append( TMS_COM_FD.DB_LEN ).append( "=:DB_LEN," );
		mdlSb.append( TMS_COM_FD.ORDERBY ).append( "=:ORDERBY," );
		mdlSb.append( TMS_COM_FD.TXN_ORDER ).append( "=:TXN_ORDER," );
		mdlSb.append( TMS_COM_FD.LINK ).append( "=:LINK," );
		mdlSb.append( TMS_COM_FD.TAB_NAME ).append( "=:TAB_NAME," );
		mdlSb.append( TMS_COM_FD.IS_KEY ).append( "=:IS_KEY," );
		mdlSb.append( TMS_COM_FD.IS_NULL ).append( "=:IS_NULL," );
		mdlSb.append( TMS_COM_FD.IS_LIST ).append( "=:IS_LIST," );
		mdlSb.append( TMS_COM_FD.IS_QUERY ).append( "=:IS_QUERY," );
		mdlSb.append( TMS_COM_FD.IS_SYS ).append( "=:IS_SYS," );
		mdlSb.append( TMS_COM_FD.FD_DESC ).append( "=:FD_DESC," );
		mdlSb.append( TMS_COM_FD.SRC_TYPE ).append( "=:SRC_TYPE" );

		mdlSb.append( " where " );
		mdlSb.append( TMS_COM_FD.REF_NAME ).append( "=:REF_NAME_OLD and " );
		mdlSb.append( TMS_COM_FD.TAB_NAME ).append( "=:TAB_NAME" );

		return mdlSb.toString();
	}

	/*
	 * 组装删除switch的sql语句
	 */
	private String buildDeleteModelSql( List delete ) {

		StringBuffer id_sb = new StringBuffer( "" );
		StringBuffer dlteSqlSb = new StringBuffer();
		dlteSqlSb.append( "DELETE FROM " );
		dlteSqlSb.append( TMS_COM_FD.TABLE_NAME ).append( " " );
		dlteSqlSb.append( "WHERE " );
		dlteSqlSb.append( TMS_COM_FD.REF_NAME ).append( " in (" );
		for( int i = 0; i < delete.size(); i++ ) {
			Map dlt = (Map) delete.get( i );
			String id = MapUtil.getString( dlt, TMS_COM_FD.REF_NAME );
			id_sb.append( "'" ).append( id ).append( "'," );
		}
		id_sb.append( ")" );
		id_sb.setCharAt( id_sb.lastIndexOf( "," ), ' ' );
		dlteSqlSb.append( id_sb );
		dlteSqlSb.append( " and tab_name=?" );

		return dlteSqlSb.toString();
	}

	/*
	 * 取主键seq
	 */
	private String getSeqId( String seqName ) {
		return sequenceService.getSequenceIdToString( seqName );
	}

	public boolean isDuplicateByTransAttrAndRoster( String attrCode ) {
		String sql = "select * from " + DBConstant.TMS_MGR_ROSTER + " r where upper(r." + DBConstant.TMS_MGR_ROSTER_ROSTERNAME + ") = ? and " + DBConstant.TMS_MGR_ROSTER_STATUS
				+ " = '1'";
		long dupCount = tmsSimpleDao.count( sql, attrCode.toUpperCase() );
		return dupCount > 0;
	}

	/************************************* shuming *************************************************/
	// 交易查询界面，数据模型的查询[入参tab_name]
	@Override
	public List<Map<String, Object>> getTransByTabnameList( Map<String, String> conds ) {

		// 1、根据传来的交易节点(tab_name,chann)查出所有父节点（本身及以上）
		List<Map<String, Object>> list = tmsSimpleDao.queryForList(
				"SELECT t.tab_name,t.tab_desc FROM tms_com_tab t " + " START WITH t.tab_name = ?" + " CONNECT BY PRIOR t.parent_tab=t.tab_name",
				conds.get( "txntype" ).toString() );

		// 2 循环查询出每个交易节点的数据模型
		List<Map<String, Object>> listdb = null; // 本节点及父节点全部数据模型
		for( int i = 0; i < list.size(); i++ ) {
			List<Map<String, Object>> listfor = null;
			if( i == 0 ) {
				listdb = tmsSimpleDao.queryForList( " SELECT * FROM tms_com_fd d WHERE d.tab_name=? ", list.get( i ).get( "TAB_NAME" ).toString() );
			}
			else {
				listfor = tmsSimpleDao.queryForList( " SELECT * FROM tms_com_fd d WHERE d.tab_name=? ", list.get( i ).get( "TAB_NAME" ).toString() );
				if( listfor.size() != 0 ) {
					listdb.addAll( listfor );
				}
			}
		}

		return listdb;
	}

	@Override
	public Page<Map<String, Object>> getTrandataPage( Map<String, String> conds, List<Map<String, Object>> listdb, boolean isPage ) {
		StringBuilder strsql = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss" );
		Date starTimeDb = null;
		Date endTimeDb = null;

		// 准备国家代码、地区代码及城市代码表名称
		StringBuilder country = new StringBuilder( "TMS_MGR_COUNTRY" );
		StringBuilder region = new StringBuilder( "TMS_MGR_REGION" );
		StringBuilder city = new StringBuilder( "TMS_MGR_CITY" );

		Object suffix = tmsSimpleDao
				.queryForList( "SELECT CASE c.is_suffix WHEN 0 THEN '' WHEN 1 THEN '_N' else '_N' END tab_name "
						+ "FROM (SELECT a.is_suffix FROM tms_mgr_iplog a where a.iplog_id=(SELECT max(b.iplog_id) " + "FROM tms_mgr_iplog b where b.operate_result=1)) c" )
				.get( 0 ).get( "TAB_NAME" );

		if( !StringUtil.isEmpty( suffix.toString() ) ) {
			country.append( "_N" );
			region.append( "_N" );
			city.append( "_N" );
		}

		strsql.append( "SELECT traffic.*," );
		strsql.append( " NVL((SELECT country.countryname FROM " ).append( country ).append( " country" );
		strsql.append( " WHERE traffic.countrycode = country.countrycode),'未知') || '-' ||" );
		strsql.append( " NVL((SELECT region.regionname FROM " ).append( region ).append( " region" );
		strsql.append( " WHERE traffic.countrycode = region.countrycode AND traffic.regioncode = region.regioncode), '未知') || '-' ||" );
		strsql.append( " NVL((SELECT city.cityname FROM " ).append( city ).append( " city" );
		strsql.append( " WHERE traffic.countrycode = city.countrycode AND traffic.regioncode = city.regioncode" );
		strsql.append( " AND traffic.citycode = city.citycode), '未知') LOCATION," );

		strsql.append( " (SELECT channel.channelname || '--' || tab.tab_desc" );
		strsql.append( " FROM tms_dp_channel channel, tms_com_tab tab" );
		strsql.append( "  WHERE traffic.txntype = tab.tab_name" );
		strsql.append( "  AND tab.chann = channel.channelid) txnname" );
		strsql.append( " FROM tms_run_trafficdata traffic WHERE 1 = 1" );

		// 设置交易开始时间和交易完成时间条件
		String transBeginTime = conds.get( "startTime" );
		String transEndTime = conds.get( "endTime" );
		if( !StringUtil.isEmpty( transBeginTime ) ) {
			try {
				starTimeDb = sdf.parse( transBeginTime );
				transBeginTime = String.valueOf( starTimeDb.getTime() );
			}
			catch( ParseException e ) {
				e.printStackTrace();
			}
		}

		if( !StringUtil.isEmpty( transEndTime ) ) {

			// 把时间转成毫秒
			try {
				endTimeDb = sdf.parse( transEndTime );
				transEndTime = String.valueOf( endTimeDb.getTime() );
			}
			catch( ParseException e ) {
				e.printStackTrace();
			}
		}
		conds.put( "startTime", transBeginTime );
		conds.put( "endTime", transEndTime );

		for( int i = 0; i < listdb.size(); i++ ) {
			String fdName = listdb.get( i ).get( "FD_NAME" ).toString().toLowerCase();

			// 判断条件中是否有这个字段
			if( StringUtil.isNotEmpty( conds.get( fdName ) ) && conds.containsKey( fdName ) && !"txntime".equals( fdName ) ) {
				strsql.append( " AND " ).append( fdName ).append( "=:" ).append( fdName );
			}

			if( "txntime".equals( fdName ) ) {
				if( transEndTime != null && !"".equals( transEndTime ) ) {
					strsql.append( " AND " ).append( fdName ).append( "<=:endTime" );
				}
				if( transBeginTime != null && !"".equals( transBeginTime ) ) {
					strsql.append( " AND " ).append( fdName ).append( ">=:startTime" );
				}
			}

		}

		Order order = new Order().desc( "TXNTIME" );
		Page<Map<String, Object>> trafficDataPage = null;
		if( isPage ) {
			trafficDataPage = tmpSimpleDao.pageQuery( strsql.toString(), conds, order );
		}
		else {
			// 每页查询10000条
			trafficDataPage = tmpSimpleDao.pageQuery( strsql.toString(), conds, 1, 10000, order );
		}
		return trafficDataPage;
	}

	/******************************add by wuruiqi******************************************/
	@SuppressWarnings("unchecked")
	public void exportList( String exportType, HttpServletRequest request, HttpServletResponse response ) {
		OutputStream os = null;
		try {
			Map<String, String[]> parms = request.getParameterMap();
			Map<String, String> conds = new HashMap<String, String>();

			// 遍历请求参数，生成查询条件
			for( Iterator iter = parms.entrySet().iterator(); iter.hasNext(); ) {
				Map.Entry element = (Map.Entry) iter.next();
				// key值
				String strKey = (String) element.getKey();
				// value,数组形式
				String[] value = (String[]) element.getValue();

				if( !StringUtil.isEmpty( value[0] ) ) {
					conds.put( strKey, value[0].toString() );
				}
			}

			List<Map<String, Object>> titleList = getTransByTabnameList( conds );
			Page<Map<String, Object>> datePage = getTrandataPage( conds, titleList, false );
			List<Map<String, Object>> dateList = datePage.getList();

			// 补充地理位置信息
			Map<String, Object> location = new HashMap<String, Object>();
			location.put( "NAME", "地理位置" );
			location.put( "FD_NAME", "LOCATION" );
			titleList.add( location );

			if( dateList == null || dateList.isEmpty() ) {
				throw new TmsMgrServiceException( "无可导出的数据." );
			}

			// 生成文件名称：交易事件分类查询-yyyyMMddhhmmssSSS
			long time = System.currentTimeMillis();
			Date date = new Date( time );
			SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyyMMddhhmmssSSS" );
			StringBuilder fileName = new StringBuilder( "交易事件分类查询-" );
			fileName.append( dateFormat.format( date ) );
			//			System.out.println("fileName:" + fileName);

			// 防止中文文件名称丢失
			String file_name = new String( fileName.toString().getBytes(), "ISO-8859-1" );

			response.reset();
			os = response.getOutputStream();
			if( "xlsx".equalsIgnoreCase( exportType ) || "xls".equalsIgnoreCase( exportType ) ) {
				response.setContentType( "application/msexcel" );
				if( "xlsx".equalsIgnoreCase( exportType ) ) {
					XSSFWorkbook workbook = createExcel2k7Workbook( titleList, dateList );
					response.setHeader( "Content-disposition", "attachment; filename=" + file_name + ".xlsx" );
					workbook.write( os );
				}
			}
		}
		catch( TmsMgrServiceException e ) {
			throw e;
		}
		catch( Exception e ) {
			throw new TmsMgrServiceException( "数据导出失败.", e );
		}
		finally {
			try {
				if( os != null ) os.close();
			}
			catch( IOException e ) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param titleList 表头
	 * @param dateList  数据
	 * @return
	 */
	private XSSFWorkbook createExcel2k7Workbook( List<Map<String, Object>> titleList, List<Map<String, Object>> dateList ) {
		XSSFWorkbook workbook = new XSSFWorkbook();// 创建工作簿
		XSSFSheet sheet = workbook.createSheet();// 创建工作表
		// 创建字体格式
		XSSFFont font = workbook.createFont();
		font.setBold( true );
		// 设置样式
		XSSFCellStyle cs = workbook.createCellStyle();
		cs.setFont( font );
		XSSFRow titleRow = sheet.createRow( 0 );
		int t = 0;
		for( int i = 0; i < titleList.size(); i++ ) { // 产生表头
			Map<String, Object> title = titleList.get( i );
			String name = title.get( "NAME" ).toString();
			sheet.setColumnWidth( t, name.length() * 7 * 100 );
			XSSFCell cell = titleRow.createCell( t );
			cell.setCellValue( name );
			cell.setCellStyle( cs );
			t++;
		}
		for( int i = 0, len = dateList.size(); i < len; i++ ) { // 产生数据行
			Map<String, Object> dataMap = dateList.get( i );
			XSSFRow dataRow = sheet.createRow( i + 1 );
			for( int j = 0; j < titleList.size(); j++ ) {
				XSSFCell cell = dataRow.createCell( j );
				cell.setCellValue( MapUtil.getString( dataMap, titleList.get( j ).get( "FD_NAME" ).toString() ) );
			}
		}
		return workbook;
	}
}
