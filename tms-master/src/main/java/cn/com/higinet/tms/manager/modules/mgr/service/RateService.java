package cn.com.higinet.tms.manager.modules.mgr.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.util.MapWrap;
import cn.com.higinet.tms.manager.modules.aop.cache.CacheRefresh;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.PropertiesUtil;
import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;
import cn.com.higinet.tms.manager.modules.tran.service.TransDefService;

/**
 * 评级管理服务类
 * @author zlq
 */
@Service("rateService")
public class RateService {

	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	@Autowired
	private SimpleDao tmsSimpleDao;

	@Autowired
	private SimpleDao tmpSimpleDao;

	@Autowired
	private TransDefService transDefService;

	@Autowired
	private ServerService serverService;

	@Autowired
	private SendRateMessage sendRateMessage;

	@Autowired
	private DataSource officialTmsDataSource;

	@Autowired
	JdbcTemplate officialJdbcTemplate;
	@Autowired
	private CacheRefresh commonCacheRefresh;

	/**
	* 评级管理数据
	* @param 查询条件
	* @return 名单数据
	*/
	public List<Map<String, Object>> rateList( Map<String, String> conds ) {

		List<Map<String, Object>> rosterPage = tmsSimpleDao.listAll( "TMS_MGR_RATESET", new Order().asc( "RS_ID" ) );

		for( Map<String, Object> map : rosterPage ) {
			Map<String, Object> tab_data = tmsSimpleDao.retrieve( DBConstant.TMS_COM_TAB.TABLE_NAME,
					MapWrap.map( DBConstant.TMS_COM_TAB.TXNID, MapUtil.getString( map, "TXNID" ) ).getMap() );
			if( tab_data == null || tab_data.isEmpty() ) {
				continue;
			}
			map.put( "TAB_NAME", transDefService.getSelfAndParentTranDefAsStr( MapUtil.getString( tab_data, DBConstant.TMS_COM_TAB.TAB_NAME ) ) );
		}

		return rosterPage;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.mgr.service.RateService#rateMod(java.util.Map)
	 */
	public void rateMod( Map<String, String> reqs ) {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put( "LEVEL_SCORE", MapUtil.getString( reqs, "LEVEL_SCORE" ) );
		row.put( "MODTIME", System.currentTimeMillis() );
		tmsSimpleDao.update( "TMS_MGR_RATESET", row, MapWrap.map( "RS_ID", MapUtil.getString( reqs, "RS_ID" ) ).getMap() );
	}

	public Page<Map<String, Object>> levelPage( Map<String, String> reqs ) {
		String isFirstEnter = MapUtil.getString( reqs, "isFirstEnter" );//是否查询
		if( isFirstEnter.equals( "TRUE" ) ) {
			return null;
		}
		String isQuery = MapUtil.getString( reqs, "isQuery" );//是否查询
		String userType = MapUtil.getString( reqs, "userType" );//客户类型
		String userId = MapUtil.getString( reqs, "USER_ID" );//用户编号
		String userName = MapUtil.getString( reqs, "USER_NAME" );//用户名称
		String riskLevel = MapUtil.getString( reqs, "RISKLEVEL" );//风险等级
		String startTime = MapUtil.getString( reqs, "startTime" );//是更新起始时间
		String endTime = MapUtil.getString( reqs, "endTime" );//是更新结束时间
		if( userType.equals( "customer" ) ) {
			StringBuilder sql = new StringBuilder();
			sql.append(
					"SELECT T.USERID USER_ID,T.USERNAME USER_NAME,R.RR_ID,R.RS_ID,R.SCORE,R.RISKLEVEL,R.RATEKIND_ID,R.MODTIME FROM TMS_RUN_USER T LEFT JOIN TMS_MGR_RATERESULT R ON T.USERID=R.RATEKIND_ID where 1=1 and  T.DEALER_FLAG='GENERAL' and T.MEMBER_TYPE='PERSON'" );
			if( isQuery.equals( "false" ) ) {
				return null;
			}

			SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
			Date startTime1 = null;
			Date endTime1 = null;

			if( isQuery.equals( "true" ) ) {
				if( !StringUtil.isEmpty( userId ) ) {
					sql.append( " and USERID=" + "'" + userId + "'" );
				}
				if( !StringUtil.isEmpty( userName ) ) {
					sql.append( " and USERNAME like " + "'%" + userName + "%'" );
				}
				if( !StringUtil.isEmpty( riskLevel ) ) {
					sql.append( " and RISKLEVEL=" + "'" + riskLevel + "'" );
				}

				if( !StringUtil.isEmpty( startTime ) ) {
					//把时间转成毫秒
					try {
						startTime1 = sdf.parse( startTime );
						startTime = String.valueOf( startTime1.getTime() );
					}
					catch( ParseException e ) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					sql.append( " and MODTIME>" + startTime );
				}
				if( !StringUtil.isEmpty( endTime ) ) {
					//把时间转成毫秒
					try {
						endTime1 = sdf.parse( endTime );
						endTime = String.valueOf( endTime1.getTime() );
					}
					catch( ParseException e ) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					sql.append( " and MODTIME<=" + endTime );

				}

			}
			sql.append( " order by MODTIME desc" );
			Page<Map<String, Object>> levelPage = tmsSimpleDao.pageQuery( sql.toString(), reqs, new Order() );
			return levelPage;
		}
		StringBuilder merchantSql = new StringBuilder();
		if( userType.equals( "merchant" ) ) {

			merchantSql.append(
					"SELECT T.member_no USER_ID ,T.login_name USER_NAME, R.RR_ID, R.RS_ID,R.SCORE,R.RISKLEVEL, R.RATEKIND_ID,R.MODTIME FROM TMS_Run_Merchants T LEFT JOIN TMS_MGR_RATERESULT R ON T.member_no = R.RATEKIND_ID where 1 = 1" );
			if( isQuery.equals( "false" ) ) {
				return null;
			}
			SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
			Date startTime1 = null;
			Date endTime1 = null;

			if( isQuery.equals( "true" ) ) {
				if( !StringUtil.isEmpty( userId ) ) {
					merchantSql.append( " and member_no =" + "'" + userId + "'" );
				}
				if( !StringUtil.isEmpty( userName ) ) {
					merchantSql.append( " and login_name like " + "'%" + userName + "%'" );
				}
				if( !StringUtil.isEmpty( riskLevel ) ) {
					merchantSql.append( " and RISKLEVEL=" + "'" + riskLevel + "'" );
				}

				if( !StringUtil.isEmpty( startTime ) ) {
					//把时间转成毫秒
					try {
						startTime1 = sdf.parse( startTime );
						startTime = String.valueOf( startTime1.getTime() );
					}
					catch( ParseException e ) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					merchantSql.append( " and MODTIME>" + startTime );
				}
				if( !StringUtil.isEmpty( endTime ) ) {
					//把时间转成毫秒
					try {
						endTime1 = sdf.parse( endTime );
						endTime = String.valueOf( endTime1.getTime() );
					}
					catch( ParseException e ) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					merchantSql.append( " and MODTIME<=" + endTime );

				}
			}

			merchantSql.append( " order by MODTIME desc" );
			Page<Map<String, Object>> levelPage = tmsSimpleDao.pageQuery( merchantSql.toString(), reqs, new Order() );
			return levelPage;
		}

		StringBuilder posMerchantSql = new StringBuilder();
		if( userType.equals( "posMerchant" ) ) {

			posMerchantSql.append(
					"SELECT T.mrch_no USER_ID,T.MRCH_NAME_C  USER_NAME,R.RR_ID, R.RS_ID,R.SCORE,R.RISKLEVEL,R.RATEKIND_ID,R.MODTIME FROM TMS_RUN_MERCHANT_POS T LEFT JOIN TMS_MGR_RATERESULT R ON T.MRCH_NO = R.RATEKIND_ID  where 1 = 1" );
			if( isQuery.equals( "false" ) ) {
				return null;
			}
			SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
			Date startTime1 = null;
			Date endTime1 = null;

			if( isQuery.equals( "true" ) ) {
				if( !StringUtil.isEmpty( userId ) ) {
					posMerchantSql.append( " and mrch_no =" + "'" + userId + "'" );
				}
				if( !StringUtil.isEmpty( userName ) ) {
					posMerchantSql.append( " and mrch_name_c like " + "'%" + userName + "%'" );
				}
				if( !StringUtil.isEmpty( riskLevel ) ) {
					posMerchantSql.append( " and RISKLEVEL=" + "'" + riskLevel + "'" );
				}

				if( !StringUtil.isEmpty( startTime ) ) {
					//把时间转成毫秒
					try {
						startTime1 = sdf.parse( startTime );
						startTime = String.valueOf( startTime1.getTime() );
					}
					catch( ParseException e ) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					merchantSql.append( " and MODTIME>" + startTime );
				}
				if( !StringUtil.isEmpty( endTime ) ) {
					//把时间转成毫秒
					try {
						endTime1 = sdf.parse( endTime );
						endTime = String.valueOf( endTime1.getTime() );
					}
					catch( ParseException e ) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					posMerchantSql.append( " and MODTIME<=" + endTime );

				}
			}

			posMerchantSql.append( " order by MODTIME desc" );
			Page<Map<String, Object>> levelPage = tmsSimpleDao.pageQuery( posMerchantSql.toString(), reqs, new Order() );
			return levelPage;
		}

		return null;
	}

	public void singleRate( Map<String, Object> reqs, String signal ) {
		String table_name = MapUtil.getString( reqs, "TABLE_NAME" );//TBL_USER
		String table_pk = MapUtil.getString( reqs, "TABLE_PK" );//USER_ID
		String user_id = MapUtil.getString( reqs, "USER_ID" );//TBL_USER

		/* 获取可以评级的服务器*/
		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "SERVTYPE", "1" );
		List<Map<String, Object>> server_list = serverService.listServer( conds );
		if( server_list == null || server_list.size() == 0 ) {
			throw new TmsMgrServiceException( "没有可用的server" );
		}
		Map<Integer, Map<String, Object>> ser_map = new HashMap<Integer, Map<String, Object>>();
		for( int i = 0; i < server_list.size(); i++ ) {
			ser_map.put( i, server_list.get( i ) );
		}
		Map<String, Object> rate_set = tmsSimpleDao.retrieve( "TMS_MGR_RATESET", MapWrap.map( "TABLE_NAME", table_name ).getMap() );
		Map<String, Object> transaction = new HashMap<String, Object>();
		transaction.put( "RR_ID", MapUtil.getString( reqs, "RR_ID" ) );
		transaction.put( "RS_ID", MapUtil.getString( rate_set, "RS_ID" ) );
		transaction.put( "TXNID", MapUtil.getString( rate_set, "TXNID" ) );
		transaction.put( "LEVEL_SCORE", MapUtil.getString( rate_set, "LEVEL_SCORE" ) );
		transaction.put( "USER_ID", user_id );

		sendRateMessage.userRate( transaction, StaticParameters.RISK_EVAL_CODE, ser_map, signal );

		//sendRateMessage.userRate(transaction, StaticParameters.RISK_EVAL_CODE, MapUtil.getString(server, "IPADDR"), MapUtil.getString(server, "PORT"));
	}

	//Map<String,DispatcherExecutor> dispatcherExecutorMap = new HashMap<String, DispatcherExecutor>();
	Map<String, Long> totalMap = new HashMap<String, Long>();
	Map<String, Long> completeMap = new HashMap<String, Long>();
	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.mgr.service.RateService#allRate(java.util.Map)
	 */

	public void allRate( Map<String, Object> reqs, Map<Integer, Map<String, Object>> ser_map ) {
		String table_name = MapUtil.getString( reqs, "TABLE_NAME" );//TBL_USER
		String table_pk = MapUtil.getString( reqs, "TABLE_PK" );//USER_ID
		long completeTotal = 0;
		if( table_name.equals( "tms_run_user" ) ) {
			String countsql = "select count(*) from tms_run_user where DEALER_FLAG='GENERAL' ";
			long total = officialJdbcTemplate.queryForObject( countsql, Long.class );
			if( total == 0 ) {
				totalMap.put( table_name + "_total", -1L );
				return;
			}
			totalMap.put( table_name + "_total", total );
		}
		if( table_name.equals( "tms_run_merchants" ) ) {
			String countsql = "select count(*) from tms_run_merchants";
			long total = officialJdbcTemplate.queryForObject( countsql, Long.class );
			if( total == 0 ) {
				totalMap.put( table_name + "_total", -1L );
				return;
			}
			totalMap.put( table_name + "_total", total );
		}

		//POS
		if( table_name.equals( "tms_run_merchant_pos" ) ) {
			String countsql = "select count(*) from TMS_RUN_MERCHANT_POS";
			long total = officialJdbcTemplate.queryForObject( countsql, Long.class );
			if( total == 0 ) {
				totalMap.put( table_name + "_total", -1L );
				return;
			}
			totalMap.put( table_name + "_total", total );
		}

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = null;
		try {
			if( table_name.equals( "tms_run_user" ) ) {
				/*查询需要评级的数据，循环发送*/
				sql = "select /*+FIRST_ROWS*/ t1.USER_ID,t1.RR_ID,t2.RS_ID,t2.TXNID,t2.TABLE_NAME,t2.TABLE_PK,t2.LEVEL_SCORE, t1.SCORE, t1.RISKLEVEL, t1.MODTIME "
						+ "from  (SELECT T." + table_pk + " USER_ID,r.modtime,r.risklevel,r.score,r.RR_ID "
						+ "FROM tms_run_user T LEFT JOIN TMS_MGR_RATERESULT R ON T.USERID = R.RATEKIND_ID where  T.DEALER_FLAG='GENERAL') t1,"
						+ "(SELECT  RS_ID,TXNID,TABLE_NAME,TABLE_PK,LEVEL_SCORE FROM TMS_MGR_RATESET WHERE TABLE_NAME = '" + table_name + "') t2 ";
			}
			if( table_name.equals( "tms_run_merchants" ) ) {
				sql = "select /*+FIRST_ROWS*/ t1.USER_ID USER_ID,t1.RR_ID,t2.RS_ID,t2.TXNID,t2.TABLE_NAME,t2.TABLE_PK,t2.LEVEL_SCORE, t1.SCORE, t1.RISKLEVEL, t1.MODTIME "
						+ "from  (SELECT T." + table_pk + " USER_ID,r.modtime,r.risklevel,r.score,r.RR_ID "
						+ "FROM tms_run_merchants T LEFT JOIN TMS_MGR_RATERESULT R ON T.MEMBER_NO = R.RATEKIND_ID ) t1,"
						+ "(SELECT  RS_ID,TXNID,TABLE_NAME,TABLE_PK,LEVEL_SCORE FROM TMS_MGR_RATESET WHERE TABLE_NAME = '" + table_name + "') t2";
			}
			if( table_name.equals( "tms_run_merchant_pos" ) ) {
				/*pos*/
				sql = "select /*+FIRST_ROWS*/ t1.USER_ID USER_ID,t1.RR_ID,t2.RS_ID,t2.TXNID,t2.TABLE_NAME,t2.TABLE_PK,t2.LEVEL_SCORE, t1.SCORE, t1.RISKLEVEL, t1.MODTIME "
						+ "from  (SELECT T." + table_pk + " USER_ID,r.modtime,r.risklevel,r.score,r.RR_ID "
						+ "FROM tms_run_merchant_pos T LEFT JOIN TMS_MGR_RATERESULT R ON MRCH_NO = R.RATEKIND_ID ) t1,"
						+ "(SELECT  RS_ID,TXNID,TABLE_NAME,TABLE_PK,LEVEL_SCORE FROM TMS_MGR_RATESET WHERE TABLE_NAME = '" + table_name + "') t2";
			}

			long totalProcessed = 0L;
			long lastTime = System.currentTimeMillis();
			long firstTime = lastTime;
			//查询数据
			con = officialTmsDataSource.getConnection();
			log.info( "业务数据查询" + sql );
			ps = (PreparedStatement) con.prepareStatement( sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY );
			ps.setFetchDirection( ResultSet.FETCH_REVERSE );
			rs = ps.executeQuery();
			ResultSetMetaData dat = rs.getMetaData();
			HashMap<String, Object> transaction = null;
			while (rs.next()) {
				totalProcessed++;
				transaction = new HashMap<String, Object>();
				for( int j = 1; j <= rs.getMetaData().getColumnCount(); j++ ) {
					transaction.put( dat.getColumnName( j ).toString(), rs.getObject( j ) );
				}
				String signal = "all";
				sendRateMessage.userRate( transaction, StaticParameters.RISK_EVAL_CODE, ser_map, signal );
				if( totalProcessed % 10000 == 0 ) {
					long currentTime = System.currentTimeMillis();
					log.info( "评估交易数:" + totalProcessed + ", 用时:" + (currentTime - lastTime) );
					lastTime = currentTime;
				}
				completeTotal++;
				completeMap.put( table_name + "_complete", completeTotal );

			}

			while (sendRateMessage.getCount() < completeTotal) {
				Thread.sleep( 8000 );
			}

			String all = "all";
			refresh( all );

			log.info( "评估交易总数:" + totalProcessed + ", 总共用时:" + (System.currentTimeMillis() - firstTime) );
		}
		catch( Exception e ) {
			log.error( "评分异常，由于", e );
			totalMap.put( table_name + "_total", -2L );
		}
		finally {
			try {
				ps.close();
				rs.close();
				con.close();
			}
			catch( SQLException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	//自定义批量评级
	public void queryRate( Map<String, Object> reqs, Map<Integer, Map<String, Object>> ser_map ) {
		String table_name = MapUtil.getString( reqs, "TABLE_NAME" );//TBL_USER

		long total = MapUtil.getLong( totalMap, table_name );// 任务总数
		long completeTotal = 0;
		String userId = MapUtil.getString( reqs, "USREID" );//用户编号
		String userName = MapUtil.getString( reqs, "USERNAME" );//用户名称
		String riskLevel = MapUtil.getString( reqs, "RISKLEVEL" );//风险等级
		String startTime = MapUtil.getString( reqs, "startTime" );//是更新起始时间
		String endTime = MapUtil.getString( reqs, "endTime" );//是更新结束时间

		/*查询需要评级的数据，循环发送*/
		int pageIndex = 0;
		String rate_pageSize = PropertiesUtil.getPropInstance().get( "rate_pageSize" );
		int pageSize = rate_pageSize == null || rate_pageSize.length() == 0 ? 1000 : Integer.valueOf( rate_pageSize );

		StringBuilder sql = new StringBuilder();
		if( table_name.equals( "tms_run_user" ) ) {
			sql.append(
					"SELECT T2.*,R.RR_ID,R.SCORE,R.RISKLEVEL,R.MODTIME FROM (SELECT T.USERID USER_ID,T.USERNAME USERNAME,T1.RS_ID,T1.TXNID,T1.TABLE_NAME,T1.TABLE_PK,T1.LEVEL_SCORE FROM TMS_RUN_USER"
							+ " T, (SELECT * FROM TMS_MGR_RATESET WHERE TABLE_NAME='tms_run_user') T1)T2 LEFT JOIN TMS_MGR_RATERESULT R ON T2.USER_ID=R.RATEKIND_ID where 1=1" );

		}
		if( table_name.equals( "tms_run_merchants" ) ) {
			sql.append(
					"SELECT T2.*,R.RR_ID,R.SCORE,R.RISKLEVEL,R.MODTIME FROM (SELECT T.member_no USER_ID,T.login_name login_name,T1.RS_ID,T1.TXNID,T1.TABLE_NAME,T1.TABLE_PK,T1.LEVEL_SCORE FROM TMS_RUN_MERCHANTS"
							+ " T, (SELECT * FROM TMS_MGR_RATESET WHERE TABLE_NAME='tms_run_merchants') T1)T2 LEFT JOIN TMS_MGR_RATERESULT R ON T2.USER_ID=R.RATEKIND_ID where 1=1" );

		}
		//pos评级
		if( table_name.equals( "tms_run_merchant_pos" ) ) {
			sql.append(
					"SELECT T2.*,R.RR_ID,R.SCORE,R.RISKLEVEL,R.MODTIME FROM (SELECT T.mrch_no USER_ID,t.mrch_name_c MRCH_NAME_C,T1.RS_ID,T1.TXNID,T1.TABLE_NAME,T1.TABLE_PK,T1.LEVEL_SCORE FROM TMS_RUN_MERCHANT_POS"
							+ " T, (SELECT * FROM TMS_MGR_RATESET WHERE TABLE_NAME='tms_run_merchant_pos') T1)T2 LEFT JOIN TMS_MGR_RATERESULT R ON T2.USER_ID=R.RATEKIND_ID where 1=1" );

		}
		Page<Map<String, Object>> rate_page = null;

		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		Date startTime1 = null;
		Date endTime1 = null;
		if( table_name.equals( "tms_run_user" ) ) {
			if( !StringUtil.isEmpty( userId ) ) {
				sql.append( " and USER_ID=" + "'" + userId + "'" );
			}
			if( !StringUtil.isEmpty( userName ) ) {
				sql.append( "and USERNAME like " + "'%" + userName + "%'" );
			}
			if( !StringUtil.isEmpty( riskLevel ) ) {
				sql.append( " and RISKLEVEL=" + "'" + riskLevel + "'" );
			}
		}
		if( table_name.equals( "tms_run_merchants" ) ) {
			if( !StringUtil.isEmpty( userId ) ) {
				sql.append( " and USER_ID=" + "'" + userId + "'" );
			}
			if( !StringUtil.isEmpty( userName ) ) {
				sql.append( "and LOGIN_NAME like " + "'%" + userName + "%'" );
			}
			if( !StringUtil.isEmpty( riskLevel ) ) {
				sql.append( " and RISKLEVEL=" + "'" + riskLevel + "'" );
			}
		}
		if( table_name.equals( "tms_run_merchant_pos" ) ) {
			if( !StringUtil.isEmpty( userId ) ) {
				sql.append( " and USER_ID=" + "'" + userId + "'" );
			}
			if( !StringUtil.isEmpty( userName ) ) {
				sql.append( "and MRCH_NAME_C like " + "'%" + userName + "%'" );
			}
			if( !StringUtil.isEmpty( riskLevel ) ) {
				sql.append( " and RISKLEVEL=" + "'" + riskLevel + "'" );
			}
		}
		if( !StringUtil.isEmpty( startTime ) ) {
			//把时间转成毫秒
			try {
				startTime1 = sdf.parse( startTime );
				startTime = String.valueOf( startTime1.getTime() );
			}
			catch( ParseException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sql.append( " and MODTIME>" + startTime );
		}
		if( !StringUtil.isEmpty( endTime ) ) {
			//把时间转成毫秒
			try {
				endTime1 = sdf.parse( endTime );
				endTime = String.valueOf( endTime1.getTime() );
			}
			catch( ParseException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sql.append( " and MODTIME<=" + endTime );

		}
		try {
			rate_page = tmsSimpleDao.pageQuery( sql.toString(), new HashMap<String, Object>(), 1, pageSize, new Order() );
			total = rate_page.getTotal();
			if( total == 0 || ser_map == null || ser_map.isEmpty() ) {
				totalMap.put( table_name + "_total", -1L );
				rate_page = null;
				return;
			}

			totalMap.put( table_name + "_total", total );
			pageIndex = (int) (total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1);
			String signal = "single";
			do {
				rate_page = tmsSimpleDao.pageQuery( sql.toString(), new HashMap<String, Object>(), pageIndex, pageSize, new Order() );
				List<Map<String, Object>> rate_list = rate_page.getList();

				for( Map<String, Object> transaction : rate_list ) {

					if( ser_map == null || ser_map.isEmpty() ) {
						break;
					}
					sendRateMessage.userRate( transaction, StaticParameters.RISK_EVAL_CODE, ser_map, signal );
					completeTotal++;
					completeMap.put( table_name + "_complete", completeTotal );

				}
			}
			while ((--pageIndex > 0));
		}
		catch( Exception e ) {
			log.error( "评分异常，由于", e );
			totalMap.put( table_name + "_total", -2L );
		}
		rate_page = null;

	}

	public double completePercent( Map<String, Object> reqs ) {
		String table_name = MapUtil.getString( reqs, "TABLE_NAME" );//TBL_USER

		long total = MapUtil.getLong( totalMap, table_name + "_total" );
		long completeTotal = MapUtil.getLong( completeMap, table_name + "_complete" );

		if( total == -2 ) return -2;// 评级过程中发生异常
		if( total == -1 ) return 100;// 没有数据
		if( total == 0 ) return total;

		int percent = (int) ((((double) completeTotal / total)) * 100);
		if( percent >= 100 ) {
			totalMap.put( table_name + "_total", 0L );
			completeMap.put( table_name + "_complete", 0L );
			return 100;
		}

		return percent;
	}

	public void updateRiskLevel( Map<String, String> reqs ) {
		String score = MapUtil.getString( reqs, "SCORE" );
		if( StringUtil.isEmpty( score ) ) {
			return;
		}
		if( "".equals( reqs.get( "RATEKIND_ID" ) ) || reqs.get( "RATEKIND_ID" ) == null || "".equals( reqs.get( "RS_ID" ) ) || reqs.get( "RS_ID" ) == null ) {
			return;
		}
		String sql = "SELECT A.LEVEL_SCORE LEVELSCORE FROM TMS_MGR_RATESET A ORDER BY A.RS_ID ";
		String currentTimeSql = "SELECT sysdate as currentTime  FROM DUAL";

		List<Map<String, Object>> levelScoreList = tmsSimpleDao.queryForList( sql );
		if( levelScoreList == null || levelScoreList.size() == 0 ) {
			return;
		}
		String userType = reqs.get( "userType" );
		String riskLevel;
		if( userType.equals( "customer" ) ) {
			Map<String, Object> levelScoreMap = levelScoreList.get( 0 );
			String level_score = MapUtil.getString( levelScoreMap, "LEVELSCORE" );
			riskLevel = this.getLevel( score, level_score );
		}
		else if( userType.equals( "merchant" ) ) {
			Map<String, Object> levelScoreMap = levelScoreList.get( 1 );
			String level_score = MapUtil.getString( levelScoreMap, "LEVELSCORE" );
			riskLevel = this.getLevel( score, level_score );
		}
		else {
			Map<String, Object> levelScoreMap = levelScoreList.get( 2 );
			String level_score = MapUtil.getString( levelScoreMap, "LEVELSCORE" );
			riskLevel = this.getLevel( score, level_score );
		}

		Map<String, Object> row = new HashMap<String, Object>();
		row.put( "SCORE", score );
		row.put( "RISKLEVEL", riskLevel );
		row.put( "MODTIME", System.currentTimeMillis() );
		Map<String, Object> conditons = new HashMap<String, Object>();

		conditons.put( "RATEKIND_ID", reqs.get( "RATEKIND_ID" ) );
		conditons.put( "RS_ID", reqs.get( "RS_ID" ) );
		tmsSimpleDao.update( "TMS_MGR_RATERESULT", row, conditons );

		Map<String, Object> values = new HashMap<String, Object>();
		List<Map<String, Object>> currentTimeList = tmsSimpleDao.queryForList( currentTimeSql );
		Map<String, Object> currentTimeMap = currentTimeList.get( 0 );
		values.put( "RATING_LEVEL", riskLevel );

		Map<String, Object> conditions = new HashMap<String, Object>();

		//pos
		if( userType.equals( "posMerchant" ) ) {
			conditions.put( "MRCH_NO", reqs.get( "RATEKIND_ID" ) );
			values.put( "UPDATE_TIME", currentTimeMap.get( "currentTime" ) );
			tmsSimpleDao.update( "TMS_RUN_MERCHANT_POS", values, conditions );
		}
		else {
			conditions.put( "USERID", reqs.get( "RATEKIND_ID" ) );
			values.put( "UPDATED_DATE", currentTimeMap.get( "currentTime" ) );
			tmsSimpleDao.update( "TMS_RUN_USER", values, conditions );
		}

		String userId = (String) reqs.get( "USER_ID" );
		String signal = "single";//单个评级标识
		refresh( signal + "," + userId );

	}

	/**
	* 方法描述:-100,1|0,2|30,3|50,4|80,5
	* @param score
	* @param level_score
	* @return
	*/
	private String getLevel( String score, String level_score ) {
		if( level_score.length() == 0 ) return "";
		String[] levels = level_score.split( "\\|" );
		// 大于等于最大值
		if( Double.valueOf( score ) >= Double.valueOf( "100" ) ) {
			return levels[0].split( "," )[1];
		}

		// 是否在最大区间
		Double tempScore = Double.valueOf( levels[0].split( "," )[0] );
		if( (Double.valueOf( score ) >= tempScore) && (Double.valueOf( score ) < Double.valueOf( "100" )) ) {
			return levels[0].split( "," )[1];
		}

		// 小于等于最小值
		if( Double.valueOf( score ) <= Double.valueOf( levels[levels.length - 1].split( "," )[0] ) ) {
			return levels[levels.length - 1].split( "," )[1];
		}
		String level = "";
		for( int i = levels.length - 1; i >= 0; i-- ) {
			String start_level = levels[i];
			String end_level = levels[i - 1];
			if( Double.valueOf( score ) >= Double.valueOf( start_level.split( "," )[0] ) && Double.valueOf( score ) < Double.valueOf( end_level.split( "," )[0] ) ) {
				level = start_level.split( "," )[1];
				break;
			}
		}
		return level;
	}

	public List<Map<String, Object>> getTransHitRuleList( String txnCode, String txnType ) {
		String ruleHitsql = "select TRIGID, TXNCODE, TXNTYPE, RULEID, MESSAGE, NUMTIMES, " + "CREATETIME, RULE_SCORE from TMS_RUN_RULETRIG where TXNCODE = ?";
		List<Map<String, Object>> trigList = tmpSimpleDao.queryForList( ruleHitsql, txnCode );
		if( trigList == null || trigList.isEmpty() ) {
			return null;
		}

		//先从规则表中查询规则，再从规则备份表中查询
		String ruleSql = "select RULE_ID, RULE_NAME, RULE_DESC, RULE_COND, RULE_COND_IN, " + "RULE_SHORTDESC, EVAL_TYPE, DISPOSAL from TMS_COM_RULE where RULE_TXN in ("
				+ TransCommon.arr2str( TransCommon.cutToIds( txnType ) ) + ")";
		List<Map<String, Object>> ruleList = tmsSimpleDao.queryForList( ruleSql );
		String ruleTempSql = "";
		int totalTrigRules = 0;//所有的规则命中数，如果totalTrigRules不等于trigList.size()说明规则表中的数据被删除，再从规则备份表中查询
		if( !ruleList.isEmpty() ) {
			for( int i = 0, len = trigList.size(); i < len; i++ ) {
				Map<String, Object> trigMap = trigList.get( i );
				String ruleId = MapUtil.getString( trigMap, "RULEID" );
				for( int j = 0, jlen = ruleList.size(); j < jlen; j++ ) {
					Map<String, Object> ruleMap = ruleList.get( j );
					String rule_id = MapUtil.getString( ruleMap, "RULE_ID" );
					if( ruleId.equals( rule_id ) ) {
						totalTrigRules++;
						trigMap.putAll( ruleMap );
						break;
					}
				}
			}
		}

		if( totalTrigRules != trigList.size() ) {
			ruleTempSql = "select RULE_ID, RULE_NAME, RULE_DESC, RULE_COND, RULE_COND_IN, " + "RULE_SHORTDESC, EVAL_TYPE, DISPOSAL from TMS_COM_RULE_TEMP where RULE_TXN in ("
					+ TransCommon.arr2str( TransCommon.cutToIds( txnType ) ) + ")";
			List<Map<String, Object>> ruleTempList = tmpSimpleDao.queryForList( ruleTempSql );
			if( !ruleTempList.isEmpty() ) {
				for( int i = 0, len = trigList.size(); i < len; i++ ) {
					Map<String, Object> trigMap = trigList.get( i );
					String ruleId = MapUtil.getString( trigMap, "RULEID" );
					for( int j = 0, jlen = ruleTempList.size(); j < jlen; j++ ) {
						Map<String, Object> ruleMap = ruleTempList.get( j );
						String rule_id = MapUtil.getString( ruleMap, "RULE_ID" );
						if( ruleId.equals( rule_id ) ) {
							trigMap.putAll( ruleMap );
							break;
						}
					}
				}
			}
		}

		return trigList;
	}

	public String refresh( String refreshMsg ) {
		while (commonCacheRefresh.refreshUserCache( refreshMsg ).isEmpty())
			break;
		return "";
	}

	public Page<Map<String, Object>> rateHistoryList( Map<String, String> reqs ) {
		String userId = MapUtil.getString( reqs, "USERID" );//用户编号
		StringBuilder sql = new StringBuilder();
		sql.append( "select userid ,score,risklevel,created_date from tms_userrate_log where 1=1" );
		if( !StringUtil.isEmpty( userId ) ) {
			sql.append( " and userid=" + "'" + userId + "'" );
		}
		Page<Map<String, Object>> rateHistoryListPage = tmpSimpleDao.pageQuery( sql.toString(), reqs, new Order() );
		return rateHistoryListPage;
	}

}
