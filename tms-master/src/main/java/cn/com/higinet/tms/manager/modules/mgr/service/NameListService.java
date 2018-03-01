package cn.com.higinet.tms.manager.modules.mgr.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.engine.comm.MD5Util;
import cn.com.higinet.tms.engine.comm.web_tool;
import cn.com.higinet.tms.engine.core.cache.cache_init;
import cn.com.higinet.tms.engine.core.cache.db_rule;
import cn.com.higinet.tms.engine.core.cache.db_rule_action;
import cn.com.higinet.tms.engine.core.cache.db_stat;
import cn.com.higinet.tms.engine.core.cache.db_strategy;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.dao.util.MapWrap;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_FD;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms.manager.modules.common.SequenceService;
import cn.com.higinet.tms.manager.modules.common.SqlWhereHelper;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.mgr.util.MgrDateConvertUtil;

/**
 * 名单管理、查看服务类
 * @author zlq
 * @author zhang.lei
 */
@Service("nameListService")
public class NameListService {
	private static final Logger log = LoggerFactory.getLogger( NameListService.class );

	@Autowired
	@Qualifier("offlineSimpleDao")
	private SimpleDao offlineSimpleDao;

	public SimpleDao getSimpleDao() {
		return offlineSimpleDao;
	}

	@Autowired
	private DataSource officialTmsDataSource;

	@Autowired
	private SequenceService sequenceService;
	@Autowired
	private SqlMap tmsSqlMap;

	/**
	 * 获取一页名单数据
	 * @param 查询条件
	 * @return 名单数据
	 */
	public Page<Map<String, Object>> listNameList( Map<String, String> conds ) {
		StringBuilder sql = new StringBuilder();
		sql.append( "SELECT ROSTER.ROSTERID,ROSTER.ROSTERTYPE, ROSTER.DATATYPE, ROSTER.ROSTERNAME, ROSTER.ROSTERDESC, ROSTER.ISCACHE, ROSTER.CREATETIME, ROSTER.REMARK, " + "(SELECT COUNT(1) FROM TMS_MGR_ROSTERVALUE ROSTERVALUE WHERE ROSTERVALUE.ROSTERID=ROSTER.ROSTERID) VALUECOUNT " + "FROM TMS_MGR_ROSTER ROSTER " + "WHERE STATUS!='2'" );
		if( !StringUtil.isEmpty( conds.get( "datatype" ) ) ) {
			sql.append( " AND ROSTER.DATATYPE=:datatype" );
		}
		if( !StringUtil.isEmpty( conds.get( "rosterdesc" ) ) ) {
			sql.append( " " ).append( tmsSqlMap.getSql( "tms.roster.namelist" ) );
		}
		if( !StringUtil.isEmpty( conds.get( "rostertype" ) ) ) {
			sql.append( " AND ROSTER.ROSTERTYPE=:rostertype" );
		}
		Order order = new Order().desc( "datatype, createtime" );
		Page<Map<String, Object>> rosterPage = getSimpleDao().pageQuery( sql.toString(), conds, order );

		/*for(Map<String,Object> rosterMap: rosterPage.getList()){
			String createtime = rosterMap.get("createtime").toString();
			if(createtime!=null && !createtime.equals("")){
				rosterMap.put("CREATETIME", MgrDateConvertUtil.convert2String(Long.parseLong(createtime), MgrDateConvertUtil.FORMATE1));
			}
		}*/
		return rosterPage;
	}

	/**
	 * 创建名单
	 * @param fields 包含创建名单需要的字段值
	 * @return 创建好的名单
	 */
	public Map<String, Object> createNameList( Map<String, String> fields ) {
		Map<String, Object> rosterMap = new HashMap<String, Object>();

		rosterMap.put( "ROSTERNAME", fields.get( "rostername" ) );
		rosterMap.put( "ROSTERDESC", fields.get( "rosterdesc" ) );
		rosterMap.put( "ROSTERTYPE", fields.get( "rostertype" ) );
		rosterMap.put( "DATATYPE", fields.get( "datatype" ) );
		rosterMap.put( "ISCACHE", fields.get( "iscache" ) );
		rosterMap.put( "REMARK", fields.get( "remark" ) );

		long time = System.currentTimeMillis();
		rosterMap.put( "CREATETIME", time );
		String rosterId = sequenceService.getSequenceIdToString( "SEQ_TMS_ROSTER_ID" );
		rosterMap.put( "ROSTERID", rosterId );
		getSimpleDao().create( DBConstant.TMS_MGR_ROSTER, rosterMap );
		//新建时，必须将主键ID放到Service方法的参数中
		fields.put( "ROSTERID", rosterId );
		return rosterMap;
	}

	/**
	 * 根据名单主键，获取单个名单
	 * @param rosterId 名单主键值
	 * @return 名单
	 */
	public Map<String, Object> getOneNameList( String rosterId ) {
		Map<String, Object> nameList = getSimpleDao().retrieve( DBConstant.TMS_MGR_ROSTER, MapWrap.map( "ROSTERID", rosterId ).getMap() );
		String remark = MapUtil.getString( nameList, "REMARK" );
		if( StringUtil.isEmpty( remark ) ) {
			nameList.put( "REMARK", "" );
		}
		return nameList;
	}

	/**
	 * 根据名单ID查询名单名称
	 * @param rosterId
	 * @return
	 */
	public Map<String, Object> selectById( String rosterId ) {
		return getSimpleDao().retrieve( DBConstant.TMS_MGR_ROSTER, MapWrap.map( "ROSTERID", rosterId ).getMap() );
	}

	public Map<String, Object> selectByIdOld( String rosterIdOld ) {
		return getSimpleDao().retrieve( DBConstant.TMS_MGR_ROSTER, MapWrap.map( "ROSTERID", rosterIdOld ).getMap() );
	}

	/**
	 * 根据IDs查询名单
	 * @param arrs 名单ID组成的数组
	 * @return 名单
	 */
	public List<Map<String, Object>> selectByIds( String[] arrs ) {
		StringBuffer sqlsf = new StringBuffer();
		String Ids = "";
		for( int i = 0; i < arrs.length; i++ ) {
			if( i > 0 ) {
				Ids += ",";
			}
			Ids += "'" + arrs[i] + "'";
		}
		sqlsf.append( "SELECT ROSTERDESC" );
		sqlsf.append( " FROM " + DBConstant.TMS_MGR_ROSTER );
		sqlsf.append( " where " + DBConstant.TMS_MGR_ROSTER_ROSTERID + " in (" + Ids + ")" );
		List<Map<String, Object>> datalist = getSimpleDao().queryForList( sqlsf.toString() );
		return datalist;
	};

	/**
	 * 根据rosterValueId查询名单值
	 * @param rosterValueId
	 * @return
	 */

	public Map<String, Object> selectByValueId( String rosterValueId ) {
		return getSimpleDao().retrieve( DBConstant.TMS_MGR_ROSTERVALUE, MapWrap.map( "ROSTERVALUEID", rosterValueId ).getMap() );
	}

	/**
	 * 根据rosterValueId查询名单值
	 * @param rosterValueId
	 * @return
	 */
	public List<Map<String, Object>> selectByValueIds( String[] arrs ) {
		StringBuffer sqlsf = new StringBuffer();
		String Ids = "";
		for( int i = 0; i < arrs.length; i++ ) {
			if( i > 0 ) {
				Ids += ",";
			}
			Ids += "'" + arrs[i] + "'";
		}
		sqlsf.append( "SELECT " );
		sqlsf.append( DBConstant.TMS_MGR_ROSTERVALUE_ROSTERVALUE );
		sqlsf.append( " FROM " + DBConstant.TMS_MGR_ROSTERVALUE );
		sqlsf.append( " where " + DBConstant.TMS_MGR_ROSTERVALUE_ROSTERVALUEID + " in (" + Ids + ")" );
		List<Map<String, Object>> datalist = getSimpleDao().queryForList( sqlsf.toString() );
		return datalist;
	};

	/**
	 * 更新名单
	 * @param reqs 更新名单需要的字段值
	 */
	public void updateOneNameList( Map<String, String> reqs ) {
		Map<String, Object> nameList = new HashMap<String, Object>();
		nameList.put( "ISCACHE", reqs.get( "iscache" ) );
		nameList.put( "REMARK", reqs.get( "remark" ) );

		long time = System.currentTimeMillis();
		nameList.put( "MODIFYTIME", time );

		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put( "ROSTERID", reqs.get( "rosterid" ) );

		getSimpleDao().update( DBConstant.TMS_MGR_ROSTER, nameList, conds );
	}

	public void deleteNameList( Map<String, List<Map<String, String>>> batchMap ) {
		List<Map<String, String>> delList = batchMap.get( "del" );

		//检验名单是否被引用
		checkRosterRef( delList );
		//		checkRosterRefTmp(delList);

		String rosterIds = "";
		for( Map<String, String> delMap : delList ) {
			int rosterId = MapUtil.getInteger( delMap, "rosterid" );
			rosterIds += ",'" + rosterId + "'";
		}
		rosterIds = rosterIds.substring( 1 );

		// 删除名单列表SQL
		String rosterSql = "UPDATE " + DBConstant.TMS_MGR_ROSTER + " SET " + DBConstant.TMS_MGR_ROSTER_STATUS + "='2', " + DBConstant.TMS_MGR_ROSTER_MODIFYTIME + "=" + System.currentTimeMillis() + " WHERE " + DBConstant.TMS_MGR_ROSTER_ROSTERID + " IN (" + rosterIds + ")";
		// 删除名单值列表SQL
		String rosterValueSql = "DELETE FROM " + DBConstant.TMS_MGR_ROSTERVALUE + " WHERE " + DBConstant.TMS_MGR_ROSTER_ROSTERID + " IN (" + rosterIds + ")";

		String[] sql = new String[] {
				rosterSql, rosterValueSql
		};

		getSimpleDao().batchUpdate( sql );
	}

	/**
	 * 校验名单是否被引用，临时库
	 * @param delList
	 */
	//	private void checkRosterRefTmp(List<Map<String, String>> delList) {
	//		for (Map<String,String> delMap : delList) {
	//			cache_init.init(new data_source(tmpTmsDataSource));
	//			List<db_stat> ref_stat = new ArrayList<db_stat>();
	//			List<db_rule> ref_rule = new ArrayList<db_rule>();
	//			List<db_sw> ref_sw = new ArrayList<db_sw>();
	//			List<db_action> ref_act = new ArrayList<db_action>();
	//			String roster_name = MapUtil.getString(delMap, "ROSTERNAME");
	//			String roster_desc = MapUtil.getString(delMap, "ROSTERDESC");
	//			boolean isRefed = web_tool.find_ref_roster(roster_name, ref_stat, ref_rule, ref_sw, ref_act);
	//			if(isRefed){
	//				throw new TmsMgrServiceException("["+roster_desc+"]被引用，不能删除");
	//			}
	//		}
	//	}

	/**
	 * 校验名单是否被引用，正式库
	 * @param delList
	 */
	private void checkRosterRef( List<Map<String, String>> delList ) {
		for( Map<String, String> delMap : delList ) {
			cache_init.init( new data_source( officialTmsDataSource ) );
			List<db_stat> ref_stat = new ArrayList<db_stat>();
			List<db_rule> ref_rule = new ArrayList<db_rule>();
			List<db_strategy> ref_sw = new ArrayList<db_strategy>();
			List<db_rule_action> ref_act = new ArrayList<db_rule_action>();
			String roster_name = MapUtil.getString( delMap, "rostername" );
			String roster_desc = MapUtil.getString( delMap, "rosterdesc" );
			boolean isRefed = web_tool.find_ref_roster( roster_name, ref_stat, ref_rule, ref_sw, ref_act );
			if( isRefed ) {
				throw new TmsMgrServiceException( "[" + roster_desc + "]被引用，不能删除" );
			}
			else {
				// TMS_COM_ACTION表已删除
				/*String sql = "select * from TMS_COM_ACTION a where a." + DBConstant.TMS_COM_ACTION_AC_DST + " = ?";
				long refActCount = getSimpleDao().count(sql, roster_name);
				if(refActCount > 0){
					throw new TmsMgrServiceException("["+roster_desc+"]在[交易动作]的[名单名称]中被引用，不能删除");
				}*/
			}
		}
	}

	/**
	 * 查询名单值，分页查询
	 * @param conds 查询条件
	 * @return 一页名单值
	 */
	public Page<Map<String, Object>> listValueListByPage( Map<String, String> conds ) {
		String sql = "SELECT * FROM TMS_MGR_ROSTERVALUE";
		String rostervalue = conds.get( "rostervalue" );
		conds.put("rostervalue", '%' + rostervalue + '%');
		
		sql += SqlWhereHelper.getPatternWhere( conds, SqlWhereHelper.valueListConds );

		StringBuffer sb = new StringBuffer( sql );
		if( !StringUtil.isEmpty( conds.get( "rostervalue" ) ) ) {
			sb.append( " AND (ROSTERVALUE LIKE :rostervalue OR ROSTERVALUE=:MD5ROSTERVALUE)" );
			conds.put( "MD5ROSTERVALUE", MD5Util.getMD5Hex16( conds.get( "rostervalue" ) ) );
		}

		Order order = new Order().desc( "CREATETIME" );

		Page<Map<String, Object>> rosterPage = getSimpleDao().pageQuery( sb.toString(), conds, order );
		for( Map<String, Object> rosterMap : rosterPage.getList() ) {
			String enabletime = rosterMap.get( "enabletime" ).toString();
			String disabletime = rosterMap.get( "disabletime" ) == null ? "" : rosterMap.get( "disabletime" ).toString();
			String createtime = rosterMap.get( "createtime" ).toString();

			if( enabletime != null && !enabletime.equals( "" ) ) {
				rosterMap.put( "ENABLETIME", MgrDateConvertUtil.convert2String( Long.parseLong( enabletime ), MgrDateConvertUtil.FORMATE1 ) );
			}
			if( !disabletime.equals( "0" ) ) {
				rosterMap.put( "DISABLETIME", MgrDateConvertUtil.convert2String( Long.parseLong( disabletime ), MgrDateConvertUtil.FORMATE1 ) );
			}
			else {
				rosterMap.put( "DISABLETIME", "" );
			}
			if( createtime != null && !createtime.equals( "" ) ) {
				rosterMap.put( "CREATETIME", MgrDateConvertUtil.convert2String( Long.parseLong( createtime ), MgrDateConvertUtil.FORMATE1 ) );
			}
		}
		return rosterPage;
	}

	/**
	 * 创建名单值
	 * @paran req 创建名单值需要的字段
	 * @return 创建好的名单值
	 */
	public Map<String, Object> createValueList( Map<String, Object> req ) {
		Map<String, Object> insertMap = new HashMap<String, Object>();
		String rosterId = String.valueOf(req.get( "rosterid" ));

		String rosterValueId = "2" + sequenceService.getSequenceIdToString( "SEQ_TMS_ROSTERVALUE_ID" );// sequence前加1，用来防止与规则引擎加入名单的主键重复，规则引擎加入名单的主键是“1”+seq
		insertMap.put( "ROSTERVALUEID", rosterValueId );
		insertMap.put( "ROSTERID", rosterId );
		insertMap.put( "ROSTERVALUE", req.get( "rostervalue" ) );
		insertMap.put( "REMARK", req.get( "remark" ) );

		//授权需要
		req.put( "ROSTERVALUEID", rosterValueId );

		long time = System.currentTimeMillis();
		insertMap.put( "CREATETIME", time );

		String enabletime = String.valueOf(req.get( "enabletime" ));
		String disabletime = String.valueOf(req.get( "disabletime" ));
		insertMap.put( "ENABLETIME", MgrDateConvertUtil.convert2Millisr( enabletime, MgrDateConvertUtil.FORMATE1 ) );
		insertMap.put( "DISABLETIME", MgrDateConvertUtil.convert2Millisr( disabletime, MgrDateConvertUtil.FORMATE1 ) );

		getSimpleDao().create( DBConstant.TMS_MGR_ROSTERVALUE, insertMap );

		return insertMap;
	}
	/*@Transactional
	public Map<String, Object> createValueList(Map<String, Object> req) {
		String rosterType = (String) req.get("datatype");
		boolean isBatch = "1".equals(((String) req.get("isbatch")));
		
		Map<String, Object> insertMap = new HashMap<String, Object>();
		String rosterId = (String) req.get("ROSTERID");
		insertMap.put("ROSTERID", rosterId);
		insertMap.put("REMARK", req.get("remark"));
		long time = System.currentTimeMillis();
		insertMap.put("CREATETIME", time);
		
		String enabletime = req.get("enabletime").toString();
		String disabletime =req.get("disabletime").toString();
		insertMap.put("ENABLETIME", MgrDateConvertUtil.convert2Millisr(enabletime, MgrDateConvertUtil.FORMATE1));
		insertMap.put("DISABLETIME", MgrDateConvertUtil.convert2Millisr(disabletime, MgrDateConvertUtil.FORMATE1));
		
		if (isBatch) {
			String valuestart = (String) req.get("valuestart");
			String valueend = (String) req.get("valueend");
			if ("ip".equals(rosterType)) {
				long startip = IpAddrUtil.ip2long(valuestart);
				long endip = IpAddrUtil.ip2long(valueend);
				for (long i = startip; i <= endip; i++) {
					insertMap.put("ROSTERVALUE", IpAddrUtil.long2ip(i));
					addRosterValue(insertMap);
				}
			} else if ("long".equals(rosterType)) {
				long start_long = Long.parseLong(valuestart);
				long end_long = Long.parseLong(valueend);
				for (long i = start_long; i <= end_long; i++) {
					insertMap.put("ROSTERVALUE", String.valueOf(i));
					addRosterValue(insertMap);
				}
			}
		} else {
			insertMap.put("ROSTERVALUE", req.get("rostervalue"));
			addRosterValue(insertMap);
		}
		return insertMap;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Map<String, Object> addRosterValue(Map<String, Object> map) {
		String rosterValueId = "2"+sequenceService.getSequenceIdToString("SEQ_TMS_ROSTERVALUE_ID");// sequence前加1，用来防止与规则引擎加入名单的主键重复，规则引擎加入名单的主键是“1”+seq
		map.put("ROSTERVALUEID", rosterValueId);
		getSimpleDao().create(DBConstant.TMS_MGR_ROSTERVALUE, map);
		return map;
	}*/

	/**
	 * 根据主键ID，获取名单值
	 * @param rosterValueId 名单值主键ID
	 * @return 名单值
	 */
	public Map<String, Object> getOneValueList( String rosterValueId ) {
		//Map<String, Object> rosterValue = getSimpleDao().retrieve(DBConstant.TMS_MGR_ROSTERVALUE, MapWrap.map("ROSTERVALUEID", rosterValueId).getMap());
		String sql = "select rostervalue.* " + "from TMS_MGR_ROSTERVALUE rostervalue " + "where rostervalue.ROSTERVALUEID = :rostervalueId";

		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "rostervalueId", rosterValueId );
		List<Map<String, Object>> rosterValueList = getSimpleDao().queryForList( sql, conds );

		Map<String, Object> rosterValue = null;
		if( rosterValueList != null && rosterValueList.size() != 0 ) {
			rosterValue = rosterValueList.get( 0 );
			String enabletime = rosterValue.get( "enabletime" ).toString();
			String disabletime = rosterValue.get( "disabletime" ) == null ? "" : rosterValue.get( "disabletime" ).toString();

			rosterValue.put( "enabletime", MgrDateConvertUtil.convert2String( Long.parseLong( enabletime ), MgrDateConvertUtil.FORMATE1 ) );

			if( !disabletime.equals( "0" ) ) {
				rosterValue.put( "disabletime", MgrDateConvertUtil.convert2String( Long.parseLong( disabletime ), MgrDateConvertUtil.FORMATE1 ) );
			}
			else {
				rosterValue.put( "disabletime", "" );
			}
		}
		String remark = MapUtil.getString( rosterValue, "REMARK" );
		if( StringUtil.isEmpty( remark ) ) {
			rosterValue.put( "REMARK", "" );
		}
		return rosterValue;
	}

	/**
	 * 更新单个名单值
	 * @param req 更新名单值需要的字段
	 */
	public void updateOneValueList( Map<String, Object> req ) {
		Map<String, Object> conds = new HashMap<String, Object>();
		String rosterValueId = req.get( "rostervalueid" ).toString();
		conds.put( "ROSTERVALUEID", rosterValueId );

		Map<String, Object> updateMap = new HashMap<String, Object>();
		updateMap.put( "ROSTERVALUE", req.get( "rostervalue" ) );
		updateMap.put( "REMARK", req.get( "remark" ) );
		long time = System.currentTimeMillis();
		updateMap.put( "MODIFYTIME", time );
		String enabletime = req.get( "enabletime" ).toString();
		String disabletime = req.get( "disabletime" ).toString();
		updateMap.put( "ENABLETIME", MgrDateConvertUtil.convert2Millisr( enabletime, MgrDateConvertUtil.FORMATE1 ) );
		updateMap.put( "DISABLETIME", MgrDateConvertUtil.convert2Millisr( disabletime, MgrDateConvertUtil.FORMATE1 ) );

		//添加名单主键值，授权需要
		String rosterId = MapUtil.getString( req, "rosterid" );
		req.put( "ROSTERID", rosterId );

		getSimpleDao().update( DBConstant.TMS_MGR_ROSTERVALUE, updateMap, conds );
	}

	/**
	 * 名单值转换
	 * @param req 值转换需要的参数
	 */
	public void updateOneValueListForConvert( Map<String, Object> req ) {
		Map<String, Object> conds = new HashMap<String, Object>();
		String rosterValueId = String.valueOf(req.get( "rostervalueid" ));
		conds.put( "ROSTERVALUEID", rosterValueId );
		conds.put( "ROSTERID", String.valueOf(req.get( "rosteridold" )) );

		Map<String, Object> updateMap = new HashMap<String, Object>();
		String rosterId = (String) req.get( "rosterid" );
		updateMap.put( "ROSTERID", rosterId );
		long time = System.currentTimeMillis();
		updateMap.put( "MODIFYTIME", time );

		//授权需要
		String rosterOldId = MapUtil.getString( req, "rosteridold" );
		req.put( "ROSTERVALUEID", rosterValueId );
		req.put( "ROSTERSYNCID", rosterOldId + "," + rosterId );
		req.put( "ROSTERDEPID", rosterOldId + "," + rosterId );
		req.put( "ROSTERNEWID", rosterId );
		req.put( "ROSTERID", rosterOldId );

		getSimpleDao().update( DBConstant.TMS_MGR_ROSTERVALUE, updateMap, conds );
	}

	//	public void deleteValueList(String[] valueIds) {
	public void deleteValueList( Map<String, List<Map<String, String>>> batchMap ) {

		List<Map<String, String>> delList = batchMap.get( "del" );
		String rosterValueIdStr = "";
		for( Map<String, String> delMap : delList ) {
			int rosterValueId = MapUtil.getInteger( delMap, "rostervalueid" );
			rosterValueIdStr += ",'" + rosterValueId + "'";
		}
		rosterValueIdStr = rosterValueIdStr.substring( 1 );
		// 删除名单值列表
		String rosterValueSql = "DELETE FROM TMS_MGR_ROSTERVALUE WHERE ROSTERVALUEID IN (" + rosterValueIdStr + ")";
		String[] sql = new String[] {
				rosterValueSql
		};
		getSimpleDao().batchUpdate( sql );

	}

	/**
	 * 值转换时，获取可用于转换的名单
	 * @param conds 转换参数
	 * @return 名单
	 */
	public List<Map<String, Object>> getRosterTypeList( Map<String, String> conds ) {

		String datatype = conds.get( "DATATYPE" );
		String rosterId = conds.get( "ROSTERID" );

		String sql = "SELECT ROSTER.ROSTERID,ROSTER.ROSTERTYPE,ROSTER.DATATYPE,ROSTER.ROSTERDESC FROM TMS_MGR_ROSTER ROSTER WHERE ROSTER.STATUS!=2 AND ROSTER.ROSTERID!=? AND ROSTER.DATATYPE=?";
		return getSimpleDao().queryForList( sql, rosterId, datatype );
	}

	/**
	 * 根据数据类型和名单类型，查询名单
	 * @param conds 查询条件
	 * @return 名单
	 */
	public List<Map<String, Object>> getRosterListForAdd( Map<String, String> conds ) {
		String datatype = conds.get( "datatype" );
		String rostertype = conds.get( "rostertype" );
		String sql = "SELECT * FROM TMS_MGR_ROSTER ROSTER WHERE DATATYPE = ? and ROSTERTYPE = ? and STATUS!='2'";
		return getSimpleDao().queryForList( sql, datatype, rostertype );
	}

	public long getRosterListForExist( Map<String, Object> conds ) {
		String strSql = "SELECT 1 FROM TMS_MGR_ROSTERVALUE WHERE ROSTERID = ? AND  ? >= TO_NUMBER(NUMSTART) AND ? <= TO_NUMBER(NUMEND)";
		return getSimpleDao().count( strSql, conds.get( DBConstant.TMS_MGR_ROSTERVALUE_ROSTERID ), conds.get( DBConstant.TMS_MGR_ROSTERVALUE_NUMSTART ), conds.get( DBConstant.TMS_MGR_ROSTERVALUE_NUMSTART ) );
	}

	/**
	 * 查询指定名称的名单
	 * @param 名单名称
	 * @return 名单
	 */
	public List<Map<String, Object>> getNameListByName( String rosterName ) {
		String strSql = "SELECT 1 FROM TMS_MGR_ROSTER WHERE ROSTERNAME = ? AND STATUS!=2";
		return getSimpleDao().queryForList( strSql, rosterName );
	}

	/**
	 * 根据名单ID获取名单
	 * @param rosterid 名单ID
	 * @return 名单
	 */
	public Map<String, Object> getOneNameListById( String rosterid ) {
		String strSql = "SELECT ROSTERID, ROSTERNAME, DATATYPE, ROSTERTYPE, ISCACHE FROM TMS_MGR_ROSTER WHERE ROSTERID = ?";
		return getSimpleDao().queryForList( strSql, rosterid ).get( 0 );
	}

	public Object importRoster( Map<String, List<Map<String, ?>>> batchUpdateMap ) {
		List<Map<String, ?>> reqsList = batchUpdateMap.get( "reqsList" );
		Map<String, Object> reqsMap = (Map<String, Object>) reqsList.get( 0 );
		Map<String, Object> reqs = (Map<String, Object>) reqsMap.get( "reqs" );

		MultipartFile file = (MultipartFile) reqs.get( "importFile" );
		String rosterId = (String) reqs.get( "ROSTERID" );
		String rosterDesc = (String) reqs.get( "ROSTERDESC" );
		Map<String, Object> data = getOneNameListById( rosterId ); //获取数据类型
		String datatype = MapUtil.getString( data, "DATATYPE" );
		List<Map<String, ?>> rosterValueList = new ArrayList<Map<String, ?>>(); //名单值列表
		String type = MapUtil.getString( reqs, "TYPE" );
		String errorInfo = null;
		try {
			InputStream sin = file.getInputStream();
			if( "excel2k7".equals( type ) ) { //Excel2007及以上
				XSSFWorkbook workbook = new XSSFWorkbook( sin );
				XSSFSheet sheet = workbook.getSheetAt( 0 );
				int lastRowNum = sheet.getLastRowNum();
				DateFormat formater = new SimpleDateFormat( CalendarUtil.FORMAT14.toPattern() );
				for( int i = 0; i < lastRowNum; i++ ) {
					XSSFRow row = sheet.getRow( i + 1 );
					if( isEmptyRow( row ) ) {
						continue;
					}
					//读取Excel的每一行的单元格
					String enabletime = ""; //开始时间
					String disabletime = ""; //结束时间
					XSSFCell enabletimeCell = row.getCell( 1 );
					XSSFCell disabletimeCell = row.getCell( 2 );
					//Excel中开始时间类型验证
					if( enabletimeCell == null ) { //是否为null
						throw new TmsMgrWebException( "第" + (i + 2) + "行开始时间不能为空" );
					}
					else if( XSSFCell.CELL_TYPE_NUMERIC == enabletimeCell.getCellType() ) {
						if( HSSFDateUtil.isCellDateFormatted( enabletimeCell ) ) { //HSSFDateUtil判断是否为日期类型
							Date enabletimeDate = enabletimeCell.getDateCellValue();
							enabletime = formater.format( enabletimeDate );
						}
						else {
							throw new TmsMgrWebException( "第" + (i + 2) + "行开始时间格式不正确" );
						}
					}
					else {
						enabletime = enabletimeCell.getStringCellValue();
					}
					//Excel中结束时间类型验证，如果不为null
					if( disabletimeCell != null && XSSFCell.CELL_TYPE_NUMERIC == disabletimeCell.getCellType() ) {
						if( HSSFDateUtil.isCellDateFormatted( disabletimeCell ) ) { //HSSFDateUtil判断是否为日期类型
							Date disabletimeDate = disabletimeCell.getDateCellValue();
							disabletime = formater.format( disabletimeDate );
						}
						else {
							throw new TmsMgrWebException( "第" + (i + 2) + "行结束时间格式不正确" );
						}
					}
					else if( disabletimeCell != null ) {
						disabletime = disabletimeCell.getStringCellValue();
					}
					String value = getCellValueAsString( row.getCell( 0 ) );
					String remark = getCellValueAsString( row.getCell( 3 ) );
					//校验，如果存在错误直接返回
					errorInfo = checkRosterValue( value, remark, enabletime, disabletime, datatype, i + 2 );
					if( StringUtil.isNotEmpty( errorInfo ) ) {
						throw new TmsMgrWebException( errorInfo );
					}
					//构造名单值
					Map<String, ?> valueM = generateRosterValue( value, remark, enabletime, disabletime, rosterId, rosterDesc );
					rosterValueList.add( valueM );
				}
			}
			else if( "excel2k3".equals( type ) ) { //Excel2003
				HSSFWorkbook workbook = new HSSFWorkbook( sin );
				HSSFSheet sheet = workbook.getSheetAt( 0 );
				int lastRowNum = sheet.getLastRowNum();
				DateFormat formater = new SimpleDateFormat( CalendarUtil.FORMAT14.toPattern() );
				for( int i = 0; i < lastRowNum; i++ ) {
					HSSFRow row = sheet.getRow( i + 1 );
					if( isEmptyRow( row ) ) {
						continue;
					}
					//读取Excel的每一行的单元格
					String enabletime = ""; //开始时间
					String disabletime = ""; //结束时间
					HSSFCell enabletimeCell = row.getCell( 1 );
					HSSFCell disabletimeCell = row.getCell( 2 );
					if( enabletimeCell == null ) {
						throw new TmsMgrWebException( "第" + (i + 2) + "行开始时间不能为空" );
					}
					else if( XSSFCell.CELL_TYPE_NUMERIC == enabletimeCell.getCellType() ) {
						if( HSSFDateUtil.isCellDateFormatted( enabletimeCell ) ) { //HSSFDateUtil判断是否为日期类型
							Date enabletimeDate = enabletimeCell.getDateCellValue();
							enabletime = formater.format( enabletimeDate );
						}
						else {
							throw new TmsMgrWebException( "第" + (i + 2) + "行开始时间格式不正确" );
						}
					}
					else {
						enabletime = enabletimeCell.getStringCellValue();
					}
					if( disabletimeCell != null && XSSFCell.CELL_TYPE_NUMERIC == disabletimeCell.getCellType() ) {
						if( HSSFDateUtil.isCellDateFormatted( disabletimeCell ) ) { //HSSFDateUtil判断是否为日期类型
							Date disabletimeDate = disabletimeCell.getDateCellValue();
							disabletime = formater.format( disabletimeDate );
						}
						else {
							throw new Exception( "第" + (i + 2) + "行结束时间格式不正确" );
						}
					}
					else if( disabletimeCell != null ) {
						disabletime = disabletimeCell.getStringCellValue();
					}
					String value = getCellValueAsString( row.getCell( 0 ) );
					String remark = getCellValueAsString( row.getCell( 3 ) );
					//校验，如果存在错误直接返回
					errorInfo = checkRosterValue( value, remark, enabletime, disabletime, datatype, i + 2 );
					if( StringUtil.isNotEmpty( errorInfo ) ) {
						throw new TmsMgrWebException( errorInfo );
					}
					//构造名单值
					Map<String, ?> valueM = generateRosterValue( value, remark, enabletime, disabletime, rosterId, rosterDesc );
					rosterValueList.add( valueM );
				}
			}
			else if( "text".equals( type ) ) { //如果是导入txt、csv
				String code = getEncoding( sin );
				InputStreamReader isr = new InputStreamReader( sin, code );
				BufferedReader bfr = new BufferedReader( isr );
				String line = "";
				int linenum = 0;
				while ((line = bfr.readLine()) != null) {
					if( ++linenum == 1 ) continue;
					String[] st = line.split( ",", 4 );
					if( isEmptyRow( st ) ) {
						continue;
					}
					else if( st.length < 4 ) {
						String[] _st = new String[4];
						/*for(int s=0,l=st.length;s<l;s++){
							_st[s] = st[s];
						}
						st = _st;*/
						System.arraycopy( st, 0, _st, 0, st.length );
						st = _st;
					}
					String values = StringUtil.isEmpty( st[0] ) ? "" : st[0].trim();
					String enabletime = StringUtil.isEmpty( st[1] ) ? "" : st[1].trim();
					String disabletime = StringUtil.isEmpty( st[2] ) ? "" : st[2].trim();
					String remark = StringUtil.isEmpty( st[3] ) ? "" : st[3].trim();
					//解决csv文件用Excel保存后会加上双引号的问题
					if( values.contains( "\"" ) ) {
						values = values.substring( 1, values.length() - 1 ).trim();
					}
					if( enabletime.contains( "\"" ) ) {
						enabletime = enabletime.substring( 1, enabletime.length() - 1 ).trim();
					}
					if( disabletime.contains( "\"" ) ) {
						disabletime = disabletime.substring( 1, disabletime.length() - 1 ).trim();
					}
					if( remark.contains( "\"" ) ) {
						remark = remark.substring( 1, remark.length() - 1 ).trim();
					}
					errorInfo = checkRosterValue( values, remark, enabletime, disabletime, datatype, linenum );
					if( StringUtil.isNotEmpty( errorInfo ) ) {
						throw new TmsMgrWebException( errorInfo );
					}
					//构造名单值
					Map<String, ?> valueM = generateRosterValue( values, remark, enabletime, disabletime, rosterId, rosterDesc );
					rosterValueList.add( valueM );
				}
			}
			//插入到数据库
			if( rosterValueList.size() > 0 ) {
				String rosterValueSql = "INSERT INTO TMS_MGR_ROSTERVALUE (ROSTERVALUEID,ROSTERID,ROSTERVALUE,ENABLETIME,DISABLETIME,REMARK,CREATETIME) VALUES (:ROSTERVALUEID,:ROSTERID,:ROSTERVALUE,:ENABLETIME,:DISABLETIME,:REMARK,:CREATETIME)";
				getSimpleDao().batchUpdate( rosterValueSql, rosterValueList );

				batchUpdateMap.put( "mod", rosterValueList );

			}
			else {
				throw new TmsMgrWebException( "导入数据不能为空！" );
			}
		}
		catch( Exception e ) {
			log.error( e.getMessage(), e );
			return e.getMessage();
		}
		return rosterValueList.size();
	}

	/**
	* 方法描述: 计数字符串的长度
	* @param str
	* @return
	 */
	private int length( String str ) {
		return str.replaceAll( "[\u0080-\u07ff]", "**" ).replaceAll( "[\u0800-\uffff]", "***" ).length();
	}

	private boolean isEmptyRow( String[] st ) {
		for( int i = 0; i < st.length; i++ ) {
			if( !StringUtil.isEmpty( st[i].trim() ) ) {
				return false;
			}
		}
		return true;
	}

	private String getCellValueAsString( XSSFCell cell ) {
		if( cell == null ) {
			return "";
		}

		String value = "";
		if( XSSFCell.CELL_TYPE_NUMERIC == cell.getCellType() ) {
			value = cell.getNumericCellValue() == 0 ? "" : cell.getNumericCellValue() + "";
			if( value.indexOf( ".0" ) == value.length() - 2 ) {
				value = value.substring( 0, value.indexOf( ".0" ) );
			}
		}
		else if( XSSFCell.CELL_TYPE_STRING == cell.getCellType() ) {
			value = cell.getStringCellValue();
		}
		return value;
	}

	private String getCellValueAsString( HSSFCell cell ) {
		if( cell == null ) {
			return "";
		}

		String value = "";
		if( HSSFCell.CELL_TYPE_NUMERIC == cell.getCellType() ) {
			value = cell.getNumericCellValue() == 0 ? "" : cell.getNumericCellValue() + "";
		}
		else if( HSSFCell.CELL_TYPE_STRING == cell.getCellType() ) {
			value = cell.getStringCellValue();
		}
		return value;
	}

	/**
	 * 是否为空记录
	 * @param row
	 * @return
	 */
	private boolean isEmptyRow( XSSFRow row ) {
		if( row == null ) {
			return true;
		}
		for( int i = 0; i < 5; i++ ) {
			switch ( i ) {
				case 0 :
				case 1 :
				case 4 :
					XSSFCell cell = row.getCell( i );
					if( cell != null ) {
						if( XSSFCell.CELL_TYPE_NUMERIC == cell.getCellType() && cell.getNumericCellValue() != 0 ) {
							return false;
						}
						else if( XSSFCell.CELL_TYPE_STRING == cell.getCellType() && !StringUtil.isEmpty( cell.getStringCellValue() ) ) {
							return false;
						}
					}
					break;
				case 2 :
				case 3 :
					XSSFCell cell2 = row.getCell( i );
					if( cell2 != null ) {
						if( XSSFCell.CELL_TYPE_NUMERIC == cell2.getCellType() && cell2.getDateCellValue() != null ) {
							return false;
						}
						else if( HSSFCell.CELL_TYPE_STRING == cell2.getCellType() && !StringUtil.isEmpty( cell2.getStringCellValue() ) ) {
							return false;
						}
					}
					break;
				default :
					break;
			}
		}
		//		for (int i = 0; i < 5; i++) {
		//			switch (i) {
		//			case 0:case 1:case 4:
		//				XSSFCell cell = row.getCell(i);
		//				if(cell!=null){
		//					if(XSSFCell.CELL_TYPE_NUMERIC == row.getCell(i).getCellType() && cell.getNumericCellValue()!=0){
		//						return false;
		//					} else if(XSSFCell.CELL_TYPE_STRING == row.getCell(i).getCellType() && !StringUtil.isEmpty(cell.getStringCellValue())){
		//						return false;
		//					}
		//				}
		//				break;
		//			case 2:case 3:
		//				if(row.getCell(i)!=null){
		//					if(XSSFCell.CELL_TYPE_NUMERIC == row.getCell(i).getCellType() && row.getCell(i).getDateCellValue()!=null){
		//						return false;
		//					} else if(row.getCell(i).getStringCellValue()!=null){
		//						return false;
		//					}
		//				}
		//				break;
		//			default:
		//				break;
		//			}
		//		}
		//		for (int i = 0; i < 5; i++) {
		//			XSSFCell cell = row.getCell(i);
		//			if(cell!=null && XSSFCell.CELL_TYPE_BLANK != row.getCell(i).getCellType()){
		//				return false;
		//			}
		//		}
		return true;
	}

	/**
	 * 是否为空记录
	 * @param row
	 * @return
	 */
	private boolean isEmptyRow( HSSFRow row ) {
		if( row == null ) {
			return true;
		}
		for( int i = 0; i < 5; i++ ) {
			switch ( i ) {
				case 0 :
				case 1 :
				case 4 :
					HSSFCell cell = row.getCell( i );
					if( cell != null ) {
						if( HSSFCell.CELL_TYPE_NUMERIC == cell.getCellType() && cell.getNumericCellValue() != 0 ) {
							return false;
						}
						else if( HSSFCell.CELL_TYPE_STRING == cell.getCellType() && !StringUtil.isEmpty( cell.getStringCellValue() ) ) {
							return false;
						}
					}
					break;
				case 2 :
				case 3 :
					HSSFCell cell2 = row.getCell( i );
					if( cell2 != null ) {
						if( HSSFCell.CELL_TYPE_NUMERIC == cell2.getCellType() && cell2.getDateCellValue() != null ) {
							return false;
						}
						else if( HSSFCell.CELL_TYPE_STRING == cell2.getCellType() && !StringUtil.isEmpty( cell2.getStringCellValue() ) ) {
							return false;
						}
					}
					break;
				default :
					break;
			}
		}
		//		for (int i = 0; i < 5; i++) {
		//			HSSFCell cell = row.getCell(i);
		//			if(cell!=null && HSSFCell.CELL_TYPE_BLANK != row.getCell(i).getCellType()){
		//				return false;
		//			}
		//		}
		return true;
	}

	/**
	 * @param value
	 * @param remark
	 * @param enabletime
	 * @param disabletime
	 * @param rosterId
	 * @return
	 * @throws ParseException 
	 */
	private Map<String, ?> generateRosterValue( String value, String remark, String enabletime, String disabletime, String rosterId, String rosterDesc ) throws ParseException {
		Map<String, Object> valueM = new HashMap<String, Object>();
		valueM.put( "ROSTERVALUE", value ); //名单值
		String rostervalueid = "2" + sequenceService.getSequenceIdToString( "SEQ_TMS_ROSTERVALUE_ID" ); // sequence前加1，用来防止与规则引擎加入名单的主键重复，规则引擎加入名单的主键是“1”+seq
		valueM.put( "ROSTERVALUEID", rostervalueid );
		valueM.put( "ROSTERID", rosterId ); //所属名单主键Id
		valueM.put( "ROSTERDESC", rosterDesc );
		/*SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		Calendar calendar = Calendar.getInstance();
		if(!StringUtil.isEmpty(enabletime)){	//开始时间
			calendar.setTime(sdf.parse(enabletime));
			valueM.put("ENABLETIME", calendar.getTimeInMillis());	
		}
		if(!StringUtil.isEmpty(disabletime)){	//结束时间
			calendar.setTime(sdf.parse(disabletime));
			valueM.put("DISABLETIME", calendar.getTimeInMillis());
		} else {
			valueM.put("DISABLETIME","0");
		}*/
		valueM.put( "ENABLETIME", this.getMillisTime( enabletime ) );
		if( !StringUtil.isEmpty( disabletime ) ) {
			valueM.put( "DISABLETIME", this.getMillisTime( disabletime ) );
		}
		else {
			valueM.put( "DISABLETIME", "0" );
		}
		valueM.put( "REMARK", remark ); //备注
		valueM.put( "CREATETIME", System.currentTimeMillis() ); //创建时间
		return valueM;
	}

	/**
	 * @param value
	 * @param remark
	 * @param enabletime
	 * @param disabletime
	 * @param datatype
	 * @throws ParseException
	 */
	private String checkRosterValue( String value, String remark, String enabletime, String disabletime, String datatype, int rowNum ) {
		String error = null;
		//非空验证
		if( StringUtil.isEmpty( value ) ) {
			error = "第" + rowNum + "行名单值不能为空";
			return error;
		}
		if( StringUtil.isEmpty( enabletime ) ) {
			error = "第" + rowNum + "行开始时间值不能为空";
			return error;
		}

		//长度验证
		if( length( value ) > 50 ) {
			error = "第" + rowNum + "行名单值长度过长";
			return error;
		}
		if( !StringUtil.isEmpty( remark ) && length( remark ) > 512 ) {
			error = "第" + rowNum + "行备注值长度过长";
			return error;
		}

		//校验数据内容
		if( datatype.equals( "ip" ) ) { //IP校验
			boolean flag = Pattern.matches( "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$", value );
			if( flag ) {
				for( String s : value.split( "\\." ) ) {
					if( Integer.parseInt( s ) > 255 || Integer.parseInt( s ) < 0 ) {
						error = "第" + rowNum + "行IP地址名单值格式不正确";
						return error;
					}
				}
			}
			else {
				error = "第" + rowNum + "行IP地址名单值格式不正确";
				return error;
			}
		}
		else { //特殊字符验证
			String reg = "~`#$￥%^&*{}][【】《》><?？/\\'=";
			for( int j = 0; j < reg.length(); j++ ) {
				if( value.indexOf( reg.charAt( j ) ) != -1 ) {
					error = "第" + rowNum + "行名单值不能包含特殊字符";
					return error;
				}
				if( !StringUtil.isEmpty( remark ) && remark.indexOf( reg.charAt( j ) ) != -1 ) {
					error = "第" + rowNum + "行备注不能包含特殊字符";
					return error;
				}
			}
		}

		//日期验证
		long enable = -1, disable = -1;
		if( (enable = getMillisTime( enabletime )) == -1 ) {
			error = "第" + rowNum + "行开始时间格式错误";
			return error;
		}
		if( !StringUtil.isEmpty( disabletime ) ) {
			if( (disable = getMillisTime( disabletime )) == -1 ) {
				error = "第" + rowNum + "行结束时间格式错误";
				return error;
			}
			if( enable > disable ) {
				error = "第" + rowNum + "行开始时间不能大于结束时间";
				return error;
			}
		}
		return error;
	}

	private long getMillisTime( String date ) {
		long time = -1;
		SimpleDateFormat sdf = null;
		try {
			sdf = new SimpleDateFormat( CalendarUtil.FORMAT14.toPattern() );
			sdf.parse( date );//时间校验yyyy-MM-dd HH:mm:ss
			time = CalendarUtil.parseStringToTimeMillis( date, CalendarUtil.FORMAT14.toPattern() );
		}
		catch( ParseException e ) {
			try {
				sdf = new SimpleDateFormat( CalendarUtil.FORMAT11.toPattern() );//日期校验yyyy-MM-dd
				sdf.parse( date );
				time = CalendarUtil.parseStringToTimeMillis( date, CalendarUtil.FORMAT11.toPattern() );
			}
			catch( ParseException e1 ) {
			}
		}
		return time;
	}

	/**
	 * 获取Excel实际的行数，忽略空行
	 * @param sheet
	 * @return
	 */
	//	private int getRightRowNum(Sheet sheet) {
	//		int rsCols = sheet.getColumns(); //列数
	//		int rsRows = sheet.getRows(); //行数
	//		int nullCellNum;
	//		int afterRows = rsRows;
	//		
	//		for (int i = 1; i < rsRows; i++) { //统计行中为空的单元格数
	//		   nullCellNum = 0;
	//		    for (int j = 0; j < rsCols; j++) {
	//		        String val = sheet.getCell(j, i).getContents();
	//		        val = val==null?"":val.trim();
	//		        if (StringUtil.isEmpty(val)){
	//		        	nullCellNum++;
	//		        }
	//		    }
	//		    if (nullCellNum >= rsCols) { //如果nullCellNum大于或等于总的列数
	//		     afterRows--;          //行数减一
	//		   }
	//		}
	//		return afterRows;
	//	}

	/**
	 * 根据名单主键ID获取名单下的所有名单值
	 * @param conds 查询条件
	 * @return 查询到的名单值列表
	 */
	public List<Map<String, Object>> listValueListById( Map<String, String> conds ) {
		String sql = "SELECT ROSTER.ROSTERDESC,ROSTERVALUE.ROSTERVALUE,ROSTERVALUE.ENABLETIME,ROSTERVALUE.DISABLETIME,ROSTERVALUE.REMARK" + " FROM TMS_MGR_ROSTERVALUE ROSTERVALUE,TMS_MGR_ROSTER ROSTER" + " WHERE ROSTERVALUE.ROSTERID=ROSTER.ROSTERID AND ROSTERVALUE.ROSTERID=" + conds.get( "rosterid" );

		List<Map<String, Object>> rosterValueList = getSimpleDao().queryForList( sql );
		for( Map<String, Object> rosterValue : rosterValueList ) {
			//SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

			long enabletime = MapUtil.getLong( rosterValue, "enabletime" );
			long disabletime = MapUtil.getLong( rosterValue, "disabletime" );
			//rosterValue.put("enabletime", sdf.format(new Date(enabletime)));
			rosterValue.put( "ENABLETIME", CalendarUtil.parseTimeMillisToDateTime( enabletime, CalendarUtil.FORMAT14.toPattern() ) );
			if( disabletime > 0 ) {
				//rosterValue.put("DISABLETIME", sdf.format(disabletime));
				rosterValue.put( "DISABLETIME", CalendarUtil.parseTimeMillisToDateTime( disabletime, CalendarUtil.FORMAT14.toPattern() ) );
			}
			else {
				rosterValue.put( "DISABLETIME", "" );
			}
		}

		return rosterValueList;
	}

	public String getEncoding( InputStream inputStream ) {
		String code = "";
		try {

			byte[] head = new byte[3];
			inputStream.read( head );

			code = "gb2312";
			if( head[0] == -1 && head[1] == -2 ) code = "UTF-16";
			if( head[0] == -2 && head[1] == -1 ) code = "Unicode";
			if( head[0] == -17 && head[1] == -69 && head[2] == -65 ) code = "UTF-8";
		}
		catch( Exception e ) {
			log.error( "Get File Character Error .", e );
		}
		return code;
	}

	/**
	 * 根据名单名称，rosterdesc查询名单
	 */
	public List<Map<String, Object>> getNameListByDesc( String rosterdesc ) {
		String strSql = "SELECT 1 FROM TMS_MGR_ROSTER WHERE ROSTERDESC = ? AND STATUS!=2";
		return getSimpleDao().queryForList( strSql, rosterdesc );
	}

	/**
	 * 判断名单名称是否和交易属性代码重复
	 * @return boolean 重复:true,不重复:false
	 */
	public boolean isDuplicateByRosterAndTransAttr( String rosterName ) {
		String sql = "select 1 from " + TMS_COM_FD.TABLE_NAME + " fd inner join " + TMS_COM_TAB.TABLE_NAME + " tab on fd." + TMS_COM_FD.TAB_NAME + " = tab." + TMS_COM_TAB.TAB_NAME + " where fd." + TMS_COM_FD.REF_NAME + " = ? and tab." + TMS_COM_TAB.TAB_TYPE + " = '4'";
		long dupCount = getSimpleDao().count( sql, rosterName.toUpperCase() );
		return dupCount > 0;
	}
	
	/**
	 * 按名单值名称获取名单值列表
	 * 
	 * @param conds
	 *            条件参数 （Map<列名称, 列值>）
	 * @return 记录行的值列表 （列表中存放 Map<列名称, 列值>）
	 */
	public boolean listRostervalue(Map<String, String> conds) {
		String sql = "select * from " + DBConstant.TMS_MGR_ROSTERVALUE + " where 1=1";
		if (conds.get("rostervalue") != null && !"".equals(conds.get("rostervalue"))) {
			sql += " and rostervalue = ? ";
		}
		List<Map<String, Object>> list = getSimpleDao().queryForList(sql, conds.get("rostervalue"));
		if (list != null && list.size() > 0) {
			return false;
		}
		return true;
	}
}
