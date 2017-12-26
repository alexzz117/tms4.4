package cn.com.higinet.tms.manager.modules.mgr.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.common.util.CmcMapUtil;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_MGR_IPLOG;
import cn.com.higinet.tms.manager.modules.common.IPLocationService;
import cn.com.higinet.tms.manager.modules.common.SequenceService;
import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;

@SuppressWarnings("unused")
@Service("tmsIPProtectService")
public class IPProtectService extends ApplicationObjectSupport {

	@Autowired
	@Qualifier("onlineSimpleDao")
	private SimpleDao onlineSimpleDao;
	@Autowired
	@Qualifier("tmsJdbcTemplate")
	private JdbcTemplate tmsJdbcTemplate;
	@Autowired
	private SqlMap tmsSqlMap;
	@Autowired
	private SequenceService sequenceService;
	@Autowired
	private IPLocationService ipLocationService;

	private static Logger log = LoggerFactory.getLogger( IPProtectService.class );
	private static int READ_NUM = 10000;

	private static String[] importTables = {
			"TMS_MGR_IPLOCATION", "TMS_MGR_CITY", "TMS_MGR_CARD", "TMS_MGR_MOBILE", "TMS_MGR_LOB"
	};
	private static String[] importSQL = {
			"insert into %s (IPFROM,IPTO,CITYCODE) values (:IPFROM,:IPTO,:CITYCODE)",
			"insert into %s (CITYCODE,COUNTRYCODE,REGIONCODE,CITYNAME,LATITUDE,LONGITUDE,AREACODE) values (:CITYCODE,:COUNTRYCODE,:REGIONCODE,:CITYNAME,:LATITUDE,:LONGITUDE,:AREACODE)",
			"insert into %s (CARD,CITYCODE) values (:CARD,:CITYCODE)", "insert into %s (MOBILE,CITYCODE) values (:MOBILE,:CITYCODE)"
	};
	private static Object[][] ipFields = new Object[][] {
			{
					"IPFROM", Long.class
			}, {
					"IPTO", Long.class
			}, {
					"CITYCODE", Long.class
			}
	};
	private static Object[][] cityFields = new Object[][] {
			{
					"CITYCODE", Long.class
			}, {
					"COUNTRYCODE", String.class
			}, {
					"REGIONCODE", String.class
			}, {
					"CITYNAME", String.class
			}, {
					"CITYFORSHORT", String.class
			}, {
					"LATITUDE", Double.class
			}, {
					"LONGITUDE", Double.class
			}, {
					"METROCODE", Long.class
			}, {
					"AREACODE", Long.class
			}
	};
	private static Object[][] cdFields = new Object[][] {
			{
					"CARD", String.class
			}, {
					"CITYCODE", Long.class
			}
	};
	private static Object[][] mbFields = new Object[][] {
			{
					"MOBILE", String.class
			}, {
					"CITYCODE", Long.class
			}
	};
	String error = "";
	AtomicLong ipFileProgress = new AtomicLong( 0 );
	AtomicLong cityFileProgress = new AtomicLong( 0 );
	AtomicLong cardFileProgress = new AtomicLong( 0 );
	AtomicLong mobileFileProgress = new AtomicLong( 0 );
	AtomicBoolean ipFileOver = new AtomicBoolean( false );
	AtomicBoolean cityFileOver = new AtomicBoolean( false );
	AtomicBoolean cardFileOver = new AtomicBoolean( false );
	AtomicBoolean mobileFileOver = new AtomicBoolean( false );

	// 初始化进度参数
	public void initialization() {
		ipFileProgress.set( 0 );
		cityFileProgress.set( 0 );
		error = "";
		ipFileOver.set( false );
		cityFileOver.set( false );
		cardFileOver.set( false );
		mobileFileOver.set( false );
	}

	@SuppressWarnings("unchecked")
	public void importIpLocation( Map<String, Object> inputs ) {
		Map<String, Object> ipLog = new HashMap<String, Object>();
		long isTrue = Long.parseLong( StaticParameters.YES ), isFalse = Long.parseLong( StaticParameters.NO );
		ipLog.put( TMS_MGR_IPLOG.IS_SUFFIX, ipLocationService.getLocationOperName().indexOf( "_" ) != -1 ? isTrue : isFalse );
		ipLog.put( TMS_MGR_IPLOG.OPERATE_RESULT, isTrue );

		Map<String, Object> filesMap = ((List<Map<String, Object>>) inputs.get( "mod" )).get( 0 );
		ExecutorService threadPool = Executors.newCachedThreadPool();
		List<Future<String>> reFutures = new ArrayList<Future<String>>();
		AtomicBoolean isError = new AtomicBoolean( false );
		int c = 0;
		reFutures.add( threadPool.submit( new ImportIpAddrThread( isError, ipFileProgress, ipFileOver, (InputStreamReader) filesMap.get( "importIPFile" ), importTables[c++] ) ) );
		reFutures.add(
				threadPool.submit( new ImportCityThread( isError, cityFileProgress, cityFileOver, (InputStreamReader) filesMap.get( "importCityFile" ), importTables[c++] ) ) );
		reFutures.add( threadPool.submit( new ImportFile2TableThread( isError, new AtomicLong[] {
				cardFileProgress, mobileFileProgress
		}, new AtomicBoolean[] {
				cardFileOver, mobileFileOver
		}, new InputStreamReader[] {
				(InputStreamReader) filesMap.get( "importCardFile" ), (InputStreamReader) filesMap.get( "importMobileFile" )
		}, new String[] {
				importTables[c++], importTables[c++]
		}, new String[] {
				"身份证号段", "手机号段"
		}, new String[] {
				importSQL[2], importSQL[3]
		}, new Object[] {
				cdFields, mbFields
		} ) ) );
		reFutures.add(
				threadPool.submit( new ImportIndexThread( isError, (InputStreamReader) filesMap.get( "importIPFileTemp" ), (InputStreamReader) filesMap.get( "importCityFileTemp" ),
						(InputStreamReader) filesMap.get( "importCardFileTemp" ), (InputStreamReader) filesMap.get( "importMobileFileTemp" ), importTables[c++] ) ) );
		try {
			for( int i = 0, len = reFutures.size(); i < len; i++ ) {
				Future<String> future = reFutures.get( i );
				String error = future.get();
				if( error != null ) {
					throw new Exception( error );
				}
			}
		}
		catch( Exception e ) {
			ipLog.put( TMS_MGR_IPLOG.OPERATE_RESULT, isFalse );
			error = e.getLocalizedMessage();
		}
		finally {
			threadPool.shutdown();
			this.createImpIPLog( ipLog );
		}
		ipLocationService.reset();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void importBatchUpdate( String[] sqls, List<Map<String, ?>>... lists ) {
		if( sqls != null && lists != null && sqls.length == lists.length ) {
			for( int i = 0, len = sqls.length; i < len; i++ ) {
				onlineSimpleDao.batchUpdate( sqls[i], lists[i] );
			}
		}
	}

	/**
	 * 关闭读入流
	 * 
	 * @param readers
	 */
	private void closeReader( Reader[] readers ) {
		for( Reader reader : readers ) {
			try {
				if( reader != null ) {
					reader.close();
				}
			}
			catch( IOException e ) {
				if( reader != null ) {
					reader = null;
				}
			}
		}
	}

	/**
	 * 读取文件内容
	 * 
	 * @param br
	 *            文件流
	 * @param fields
	 *            文件字段
	 * @param readNum
	 *            读取文件条数
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({
			"unchecked", "rawtypes"
	})
	public List[] readFile( BufferedReader br, int readNum, Object[][]... fields ) throws Exception {
		String line = null;
		String[] strs = null;
		List[] readLists = new List[fields.length];
		Map[] valMaps = new Map[fields.length];
		for( int i = 0, len = fields.length; i < len; i++ ) {
			readLists[i] = new ArrayList<Map<String, Object>>();
		}
		while ((line = br.readLine()) != null) {
			strs = StringUtil.split( line, "," );
			for( int i = 0, len = fields.length; i < len; i++ ) {
				valMaps[i] = new HashMap<String, Object>();
			}
			for( int i = 0, len = strs.length; i < len; i++ ) {
				String read_value = strs[i].replaceAll( "\"", "" );
				Object[][] curr_fields = fields[0];
				Object[] curr_field = curr_fields[i];
				String curr_fdname = String.valueOf( curr_field[0] );
				Class<?> curr_fdtype = (Class<?>) curr_field[1];
				Object curr_fdvalue = getValueForFieldType( curr_fdtype, read_value );
				valMaps[0].put( curr_fdname, curr_fdvalue );
				for( int f = 1, flen = fields.length; f < flen; f++ ) {
					Object[][] sub_curr_fields = fields[f];
					for( int s = 0, slen = sub_curr_fields.length; s < slen; s++ ) {
						Object[] sub_curr_field = sub_curr_fields[s];
						String sub_curr_fdname = String.valueOf( sub_curr_field[0] );
						if( curr_fdname.equals( sub_curr_fdname ) ) {
							Class<?> sub_curr_fdtype = (Class<?>) sub_curr_field[1];
							Object sub_curr_fdvalue = getValueForFieldType( sub_curr_fdtype, read_value );
							valMaps[f].put( sub_curr_fdname, sub_curr_fdvalue );
						}
					}
				}
			}
			for( int i = 0, len = fields.length; i < len; i++ ) {
				readLists[i].add( valMaps[i] );
			}
			if( readLists[0].size() == readNum ) {
				break;
			}
		}
		return readLists;
	}

	private Object getValueForFieldType( Class<?> fdType, String value ) {
		if( StringUtil.isEmpty( value ) ) {
			return null;
		}
		else {
			if( fdType == Long.class ) {
				return Long.parseLong( value );
			}
			else if( fdType == Double.class ) {
				return Double.parseDouble( value );
			}
			else {
				return value;
			}
		}
	}

	// 生成索引
	@Transactional
	public void insertIndex( InputStreamReader IPFile, InputStreamReader cityFile, InputStreamReader cardFile, InputStreamReader mobileFile ) throws Exception {
		String tabName = ipLocationService.getLocationOperName( importTables[3] );
		String delete = "delete from %s where ID = 1";
		String insert = "insert into %s(ID,NAME,DATA) values (?, ?, ?)";
		tmsJdbcTemplate.execute( String.format( delete, tabName ) );
		final ByteArrayOutputStream bos = new ByteArrayOutputStream( 1 << 20 );
		IPCache.initdb( IPFile, cityFile, cardFile, mobileFile, bos );
		tmsJdbcTemplate.execute( String.format( insert, tabName ), new AbstractLobCreatingPreparedStatementCallback( new DefaultLobHandler() ) {
			@Override
			protected void setValues( PreparedStatement pstm, LobCreator lobCreator ) throws SQLException, DataAccessException {
				pstm.setInt( 1, 1 );
				pstm.setString( 2, "IPData" );
				lobCreator.setBlobAsBytes( pstm, 3, bos.toByteArray() );
			}
		} );
	}

	// 获取进度参数
	public Map<String, Object> getProgress() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put( "errorInfo", error );
		map.put( "ipFileProgress", ipFileProgress.get() );
		map.put( "cityFileProgress", cityFileProgress.get() );
		map.put( "cardFileProgress", cardFileProgress.get() );
		map.put( "mobileFileProgress", mobileFileProgress.get() );
		map.put( "ipFileOver", ipFileOver.get() );
		map.put( "cityFileOver", cityFileOver.get() );
		map.put( "cardFileOver", cardFileOver.get() );
		map.put( "mobileFileOver", mobileFileOver.get() );
		return map;
	}

	/**
	 * 获取truncate数据表的SQL语句
	 * 
	 * @param tabName
	 *            要删除的表名
	 * @return
	 */
	private String getTruncateTableSql( String tabName ) {
		String truncateSql = tmsSqlMap.getSql( "tms.common.truncatetable" );
		return truncateSql.replaceFirst( "\\$\\{TABLENAME\\}", tabName );
	}

	/**
	 * 获取最后一次IP地址导入日志
	 * 
	 * @return
	 */
	private Map<String, Object> lastImportIpAddressLog() {
		String sql = "select max(" + TMS_MGR_IPLOG.IPLOG_ID + ") as " + TMS_MGR_IPLOG.IPLOG_ID + ", " + TMS_MGR_IPLOG.OPERATE_RESULT + ", " + TMS_MGR_IPLOG.IS_SUFFIX + " from "
				+ TMS_MGR_IPLOG.TABLE_NAME + " group by " + TMS_MGR_IPLOG.IS_SUFFIX + ", " + TMS_MGR_IPLOG.OPERATE_RESULT;
		List<Map<String, Object>> iplogList = onlineSimpleDao.queryForList( sql );
		if( iplogList == null || iplogList.size() == 0 ) {
			return null;
		}
		else {
			return iplogList.get( 0 );
		}
	}

	private boolean isOldFile( File file ) {
		long modifiedTime = file.lastModified();
		return false;
	}

	/**
	 * 创建IP地址导入日志信息
	 * 
	 * @param log
	 * @return
	 */
	public Map<String, Object> createImpIPLog( Map<String, Object> log ) {
		if( StringUtil.isEmpty( CmcMapUtil.getString( log, TMS_MGR_IPLOG.IPLOG_ID ) ) ) {
			log.put( TMS_MGR_IPLOG.IPLOG_ID, sequenceService.getSequenceId( "SEQ_TMS_MGR_IPLOG_ID" ) );
		}
		log.put( TMS_MGR_IPLOG.OPERATE_TIME, System.currentTimeMillis() );
		onlineSimpleDao.create( TMS_MGR_IPLOG.TABLE_NAME, log );
		return log;
	}

	class ImportIpAddrThread implements Callable<String> {
		private AtomicBoolean isError;
		private AtomicBoolean status;
		private AtomicLong readRows;
		private InputStreamReader in;
		private String tabName;

		public ImportIpAddrThread( AtomicBoolean isError, AtomicLong readRows, AtomicBoolean status, InputStreamReader in, String tabName ) {
			this.isError = isError;
			this.status = status;
			this.readRows = readRows;
			this.in = in;
			this.tabName = tabName;
		}

		@SuppressWarnings("unchecked")
		@Override
		public String call() {
			BufferedReader ipFileBr = null;
			List<Map<String, ?>> ipList = null;
			String error = null;
			try {
				String tabName = ipLocationService.getLocationOperName( this.tabName );
				String truncateSql = getTruncateTableSql( tabName );
				onlineSimpleDao.executeUpdate( truncateSql );
				ipFileBr = new BufferedReader( this.in, 10 * 1024 * 1024 );
				String sql = String.format( importSQL[0], tabName );
				do {
					ipList = readFile( ipFileBr, READ_NUM, ipFields )[0];
					importBatchUpdate( new String[] {
							sql
					}, ipList );
					readRows.addAndGet( ipList.size() );
				}
				while (!isError.get() && ipList != null && ipList.size() == READ_NUM);
				status.set( true );
			}
			catch( Exception e ) {
				isError.set( true );
				error = "导入IP地址文件失败";
				log.error( error, e );
			}
			return error;
		}
	}

	class ImportCityThread implements Callable<String> {
		private AtomicBoolean isError;
		private AtomicBoolean status;
		private AtomicLong readRows;
		private InputStreamReader in;
		private String tabName;

		public ImportCityThread( AtomicBoolean isError, AtomicLong readRows, AtomicBoolean status, InputStreamReader in, String tabName ) {
			this.isError = isError;
			this.status = status;
			this.readRows = readRows;
			this.in = in;
			this.tabName = tabName;
		}

		@SuppressWarnings("unchecked")
		@Override
		public String call() {
			BufferedReader cityFileBr = null;
			List<Map<String, ?>> cityList = null;
			String error = null;
			try {
				String cityTabName = ipLocationService.getLocationOperName( this.tabName );
				String cTruncateSql = getTruncateTableSql( cityTabName );
				onlineSimpleDao.executeUpdate( cTruncateSql );
				cityFileBr = new BufferedReader( this.in, 10 * 1024 * 1024 );
				String cSql = String.format( importSQL[1], cityTabName );
				do {
					cityList = readFile( cityFileBr, READ_NUM, cityFields )[0];
					importBatchUpdate( new String[] {
							cSql
					}, cityList );
					this.readRows.addAndGet( cityList.size() );
				}
				while (!this.isError.get() && cityList != null && cityList.size() == READ_NUM);
				status.set( true );
			}
			catch( Exception e ) {
				isError.set( true );
				error = "导入城市代码文件失败";
				log.error( error, e );
			}
			return error;
		}
	}

	class ImportFile2TableThread implements Callable<String> {
		private AtomicBoolean isError;
		private AtomicBoolean[] status;
		private AtomicLong[] readRows;
		private InputStreamReader[] ins;
		private String[] tabNames;
		private String[] titles;
		private String[] sqls;
		private Object[] fields;

		public ImportFile2TableThread( AtomicBoolean isError, AtomicLong[] readRows, AtomicBoolean[] status, InputStreamReader[] ins, String[] tabNames, String[] titles,
				String[] sqls, Object[] fileds ) {
			this.isError = isError;
			this.status = status;
			this.readRows = readRows;
			this.ins = ins;
			this.tabNames = tabNames;
			this.titles = titles;
			this.sqls = sqls;
			this.fields = fileds;
		}

		@Override
		@SuppressWarnings("unchecked")
		public String call() {
			BufferedReader fileBr = null;
			List<Map<String, ?>> list = null;
			String error = null;
			int i = 0;
			try {
				for( int len = ins.length; i < len; i++ ) {
					System.out.println( "----------tabNames[i]:" + tabNames[i] + "----len:" + len );
					String tabName = ipLocationService.getLocationOperName( this.tabNames[i] );
					String cTruncateSql = getTruncateTableSql( tabName );
					System.out.println( "----------cTruncateSql----len:" + cTruncateSql );
					onlineSimpleDao.executeUpdate( cTruncateSql );
					fileBr = new BufferedReader( this.ins[i], 5 * 1024 * 1024 );
					String sql = String.format( this.sqls[i], tabName );
					Object[][] field = (Object[][]) fields[i];
					do {
						list = readFile( fileBr, READ_NUM, field )[0];
						importBatchUpdate( new String[] {
								sql
						}, list );
						this.readRows[i].addAndGet( list.size() );
					}
					while (!this.isError.get() && list != null && list.size() == READ_NUM);
					this.status[i].set( true );
				}
			}
			catch( Exception e ) {
				isError.set( true );
				error = "导入" + titles[i] + "文件失败";
				log.error( error, e );
			}
			return error;
		}
	}

	class ImportIndexThread implements Callable<String> {
		private AtomicBoolean isError;
		private InputStreamReader ipReader;
		private InputStreamReader cityReader;
		private InputStreamReader cardReader;
		private InputStreamReader mobileReader;
		private String tabName;

		public ImportIndexThread( AtomicBoolean isError, InputStreamReader ipReader, InputStreamReader cityReader, InputStreamReader cardReader, InputStreamReader mobileReader,
				String tabName ) {
			this.isError = isError;
			this.ipReader = ipReader;
			this.cityReader = cityReader;
			this.cardReader = cardReader;
			this.mobileReader = mobileReader;
			this.tabName = tabName;
		}

		@Override
		public String call() {
			String error = null;
			try {
				String tabName = ipLocationService.getLocationOperName( this.tabName );
				String delete = "delete from %s where ID = 1";
				String insert = "insert into %s(ID,NAME,DATA) values (?, ?, ?)";
				tmsJdbcTemplate.execute( String.format( delete, tabName ) );
				final ByteArrayOutputStream bos = new ByteArrayOutputStream( 1 << 20 );
				IPCache.initdb( this.ipReader, this.cityReader, this.cardReader, this.mobileReader, bos );
				tmsJdbcTemplate.execute( String.format( insert, tabName ), new AbstractLobCreatingPreparedStatementCallback( new DefaultLobHandler() ) {
					@Override
					protected void setValues( PreparedStatement pstm, LobCreator lobCreator ) throws SQLException, DataAccessException {
						pstm.setInt( 1, 1 );
						pstm.setString( 2, "IPData" );
						lobCreator.setBlobAsBytes( pstm, 3, bos.toByteArray() );
					}
				} );
			}
			catch( Exception e ) {
				isError.set( true );
				error = "生成索引失败";
				log.error( error, e );
			}
			return error;
		}
	}
}