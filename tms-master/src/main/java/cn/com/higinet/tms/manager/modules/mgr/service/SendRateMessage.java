/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.mgr.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections.list.SynchronizedList;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.dao.util.MapWrap;
import cn.com.higinet.tms.manager.modules.aop.cache.CacheRefresh;
import cn.com.higinet.tms.manager.modules.common.SocketClient;
import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrServiceException;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

/**
 * 功能/模块:
 * @author zhanglq
 * @version 1.0  Mar 26, 2015
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
@Service("sendRateMessage")
public class SendRateMessage {

	private final static Logger log = LoggerFactory.getLogger( SendRateMessage.class );

	@Autowired
	private SimpleDao tmsSimpleDao;
	@Autowired
	private SimpleDao tmpSimpleDao;
	@Autowired
	private TaskExecutor taskExecutor;
	@Autowired
	private CacheRefresh commonCacheRefresh;

	public int count = 0;

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.mgr.service.SendMessageService#listServer(java.util.Map)
	 */
	/**
	 * 商户评分接口
	 * @param transaction	交易数据对象
	 * @param syncFlag		是否启用
	 * @param actionCode	调用的服务接口编号
	 * @param timeout		超时时间
	 * @return
	 */
	public void userRate( Map<String, Object> transaction, String actionCode, Map<Integer, Map<String, Object>> ser_map, String signal ) {
		try {
			String user_id = MapUtil.getString( transaction, "USER_ID" );

			// 组装报文体
			String body = composeRiskEvaluatBody( transaction );
			String head = composeHead( actionCode, body.getBytes( "UTF-8" ).length );
			log.debug( " 发送的报文头+报文体：" + head + body );

			// 发送报文，返回结果
			int index = 0; //ser_map索引下标默认为0
			taskExecutor.execute( new SaveReturnInfoTask( transaction, user_id, ser_map, head, body, index, signal ) );

		}
		catch( Exception e ) {
			throw new TmsMgrServiceException( e.getMessage() );
		}
	}

	//保存数据
	class SaveReturnInfoTask implements Runnable {
		String user_id;
		Map<Integer, Map<String, Object>> ser_map;
		String head;
		String body;
		int index;
		String signal;

		Map<String, Object> transaction;

		public SaveReturnInfoTask( Map<String, Object> transaction, String user_id, Map<Integer, Map<String, Object>> ser_map, String head, String body, int index,
				String signal ) {
			this.user_id = user_id;
			this.ser_map = ser_map;
			this.head = head;
			this.body = body;
			this.index = index;
			this.transaction = transaction;
			this.signal = signal;

		}

		public void run() {
			String resultInfo = sendMessage( user_id, ser_map, head, body, index );
			log.debug( " 返回的报文：" + resultInfo );
			//保存
			saveReturnInfo( transaction, user_id, resultInfo );
			if( "single".equals( signal ) ) {
				refresh( signal + "," + user_id );
			}
			synchronized (SaveReturnInfoTask.class) {
				count++;
			}
			;

		}

		public String refresh( String refreshMsg ) {
			while (commonCacheRefresh.refreshUserCache( refreshMsg ).isEmpty())
				break;
			return "";
		}

	}

	private void saveReturnInfo( Map<String, Object> transaction, String user_id, String resultInfo ) {
		log.debug( " 返回的报文：" + resultInfo );
		Map<String, Object> result = getRiskResult( resultInfo );

		String backcode = MapUtil.getString( result, "BACKCODE" );

		if( backcode.length() == 0 || !backcode.equals( StaticParameters.SYSTEM_SUCCESS ) ) {
			log.debug( "风险评估服务返回错误码：" + backcode + " 错误信息：" + MapUtil.getString( result, "errorInfo" ) );
			return;
		}

		String score = MapUtil.getString( result, "score" );
		//String score = "-10";
		String level_score = MapUtil.getString( transaction, "LEVEL_SCORE" );

		String rs_idp = MapUtil.getString( transaction, "RS_ID" );

		String level = getLevel( score, level_score );

		String rr_id = MapUtil.getString( transaction, "RR_ID" );
		rr_id = rr_id.equals( "null" ) || rr_id.length() == 0 ? "" : rr_id;

		String hitRules = MapUtil.getString( result, "hitRules" );

		//获取查询ruleId的查询条件
		List<Map<String, Object>> conditionListMap = this.getConditionMap( hitRules );

		//获取ruleId及ruleScore
		List<Map<String, Object>> ruleIdList = this.getRuleIdList( conditionListMap );

		//保存命中规则表Tms_Run_RuleTrig及评级历史表TMS_USERRATE_LOG
		this.saveTmsRunRuleTrigAndTmsRateLog( ruleIdList, level, score, user_id );

		//更新用户风险评估等级
		this.updateTmsUserRate( user_id, level, rs_idp );
		String userid = MapUtil.getString( transaction, "USER_ID" );
		String rs_id = MapUtil.getString( transaction, "RS_ID" );
		if( rr_id.length() == 0 && !"".equals( userid ) && !"".equals( rs_id ) ) {
			Map<String, Object> row = new HashMap<String, Object>();
			row.put( "RR_ID", StringUtil.randomUUID() );
			row.put( "RATEKIND_ID", userid );
			row.put( "RS_ID", rs_id );
			row.put( "SCORE", score );
			row.put( "RISKLEVEL", level );
			row.put( "MODTIME", System.currentTimeMillis() );
			tmsSimpleDao.create( "TMS_MGR_RATERESULT", row );
		}
		else {
			Map<String, Object> row = new HashMap<String, Object>();
			row.put( "SCORE", score );
			row.put( "RISKLEVEL", level );
			row.put( "MODTIME", System.currentTimeMillis() );
			tmsSimpleDao.update( "TMS_MGR_RATERESULT", row, MapWrap.map( "RR_ID", MapUtil.getString( transaction, "RR_ID" ) ).getMap() );
		}
	}

	private void updateTmsUserRate( String userId, String level, String rs_idp ) {
		//更新用户风险等级
		if( StringUtil.isBlank( userId ) || StringUtil.isBlank( level ) ) {
			log.debug( "更新用户风险等级：userId:" + userId + " level:" + level );
			log.debug( "更新用户风险等级失败，用户Id：" + userId + "缺失必要信息!" );
			return;
		}
		String currentTimeSql = "SELECT sysdate as currentTime  FROM DUAL";
		List<Map<String, Object>> currentTimeList = tmsSimpleDao.queryForList( currentTimeSql );
		Map<String, Object> currentTimeMap = currentTimeList.get( 0 );
		Map<String, Object> valueMap = new HashMap<String, Object>();
		valueMap.put( "RATING_LEVEL", level );

		Map<String, Object> conditionMap = new HashMap<String, Object>();

		if( rs_idp.equals( "3" ) )//pos
		{
			valueMap.put( "UPDATE_TIME", currentTimeMap.get( "currentTime" ) );

			conditionMap.put( "MRCH_NO", userId );
			tmsSimpleDao.update( "TMS_RUN_MERCHANT_POS", valueMap, conditionMap );
		}
		else {
			valueMap.put( "UPDATED_DATE", currentTimeMap.get( "currentTime" ) );

			conditionMap.put( "USERID", userId );
			tmsSimpleDao.update( "TMS_RUN_USER", valueMap, conditionMap );

		}

	}

	private void saveTmsUserratelog( String userId, String score, String level, String txnCode, String txnType ) {
		//记录用户评级日志
		if( StringUtil.isBlank( userId ) || StringUtil.isBlank( score ) || StringUtil.isBlank( level ) || StringUtil.isBlank( txnCode ) || StringUtil.isBlank( txnType ) ) {
			log.debug( "保存评级日志必要信息：userId:" + userId + " score:" + score + " level:" + level + " txnCode:" + txnCode + " txnType" + txnType );
			log.debug( "保存评级日志失败，用户Id：" + userId + "缺失必要信息!" );
			return;
		}
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put( "USERID", userId );
		paramMap.put( "SCORE", score );
		paramMap.put( "RISKLEVEL", level );
		paramMap.put( "CREATED_DATE", System.currentTimeMillis() );
		paramMap.put( "txnCode", txnCode );
		paramMap.put( "txnType", txnType );
		tmpSimpleDao.create( "TMS_USERRATE_LOG", paramMap );
	}

	public String sendMessage( String user_id, Map<Integer, Map<String, Object>> ser_map, String head, String body, int index ) {
		// 发送报文，返回结果
		String resultInfo = "";
		//		int serv_id = (hash.clac(user_id) >>> 1) % 53777 % ser_map.size();
		if( ser_map.size() > 0 ) {
			Map<String, Object> server = ser_map.get( index );

			String ip = MapUtil.getString( server, "IPADDR" );
			String port = MapUtil.getString( server, "PORT" );
			log.debug( "服务列表： " + ser_map );
			log.debug( "用户号： " + user_id + "发送服务IP地址： " + ip + ": " + port );

			try {
				resultInfo = sendMessage( ip, port, head.getBytes(), body.getBytes(), 1 );
				log.debug( "用户号： " + user_id + "发送评级服务返回结果 " + resultInfo );
			}
			catch( Exception e ) {
				ser_map.remove( index );
				index++;
				if( ser_map.isEmpty() ) {
					throw new TmsMgrServiceException( "没有可用的服务!" );
				}
				resultInfo = sendMessage( user_id, ser_map, head, body, index );
			}
		}
		else {
			throw new TmsMgrServiceException( "没有可用的服务!" );
		}

		return resultInfo;
	}

	/**
	* 方法描述:-100,1|0,2|30,3|50,4|80,5
	* @param score
	* @param level_score
	* @return
	*/
	private static String getLevel( String score, String level_score ) {
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

	/**
	 * 解析返回报文，组装RiskResult对象，并返回
	 * 
	 * @param resultMessage
	 * @return
	 */
	public Map<String, Object> getRiskResult( String resultMessage ) {
		Map<String, Object> riskResult = null;
		try {
			if( resultMessage != null ) {
				riskResult = new HashMap<String, Object>();
				byte[] bm = resultMessage.getBytes();
				byte[] head = new byte[StaticParameters.HEAD_LEN];
				// 截取报文头
				System.arraycopy( bm, StaticParameters.MESSAGE_LEN_LEN, head, 0, StaticParameters.HEAD_LEN );
				String headStr = new String( head );
				// 从报文头中截取返回码
				String backCode = headStr.substring( headStr.length() - 8 );

				String bodyStr = resultMessage.substring( StaticParameters.MESSAGE_LEN_LEN + StaticParameters.HEAD_LEN );

				// 解析返回报文体
				if( bodyStr != null && bodyStr.length() > 0 ) {
					Document doc = DocumentHelper.parseText( bodyStr );
					Element root = doc.getRootElement();
					List<Element> iter = root.elements();
					for( Element element : iter ) {
						riskResult.put( element.getName(), element.getText() );
					}
				}

				riskResult.put( "BACKCODE", backCode );
				return riskResult;
			}
		}
		catch( Exception e ) {
			riskResult = new HashMap<String, Object>();
			riskResult.put( "BACKCODE", StaticParameters.MESSAGE_ERROR );
		}
		return riskResult;
	}

	public String sendMessage( String ip, String port, byte[] b, byte[] ws, int count ) {
		String result = "";
		try {
			result = new SocketClient( ip, Integer.parseInt( port ) ).sendMsg( b, ws );
		}
		catch( Exception e ) {
			if( count <= 0 ) {
				throw new TmsMgrServiceException( ip + "端口" + port + "发送数据失败,由于" + e.getMessage() );
			}
			count--;
			result = sendMessage( ip, port, b, ws, count );
		}
		return result;
	}

	/**
	* 方法描述:
	* @param string
	* @param head
	* @param body
	* @param timeout
	* @return
	*/

	/*private String sendMessage(String runId, String head, String body,
			int timeout) {
		int resendTimes = 3;// TODO 通过配置文件获取
		StringBuffer sb = new StringBuffer(1024);
		int ret = send_request(sb, runId, head, body, timeout, resendTimes);
		if (ret == 0)
			return sb.toString();
		return null;
	}*/

	/**
	* 方法描述:
	* @param transaction
	* @return
	*/
	private String composeRiskEvaluatBody( Map<String, Object> transaction ) {
		log.debug( "组装报文transaction：" + transaction );
		StringBuilder sb = new StringBuilder();

		String rr_id = MapUtil.getString( transaction, "RR_ID" );
		sb.append( getXmlHead() );
		sb.append( appendXmlMessage( "TXNSTATUS", "1" ) );
		sb.append( appendXmlMessage( "RR_ID", rr_id.equals( "null" ) || rr_id.length() == 0 ? "" : rr_id ) );
		sb.append( appendXmlMessage( "RS_ID", MapUtil.getString( transaction, "RS_ID" ) ) );
		sb.append( appendXmlMessage( "TXNID", MapUtil.getString( transaction, "TXNID" ) ) );
		sb.append( appendXmlMessage( "USERID", MapUtil.getString( transaction, "USER_ID" ) ) );
		sb.append( appendXmlMessage( "LEVEL_SCORE", MapUtil.getString( transaction, "LEVEL_SCORE" ) ) );
		sb.append( appendXmlMessage( "DISPATCH", MapUtil.getString( transaction, "USER_ID" ) ) );

		//pos
		String txn = MapUtil.getString( transaction, "TXNID" );

		if( txn.equals( "rate0003" ) ) {
			sb.append( appendXmlMessage( "DEALERID", MapUtil.getString( transaction, "USER_ID" ) ) );
		}

		sb.append( "</Message>" );
		return sb.toString();
	}

	public String getXmlHead() {
		StringBuffer sb = new StringBuffer();
		sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
		sb.append( "<Message>" );
		return sb.toString();
	}

	public static StringBuffer appendXmlMessage( String key, String value ) {
		StringBuffer bf = new StringBuffer();
		if( value != null && value.length() > 0 ) {
			bf.append( "<" ).append( key ).append( ">" ).append( value ).append( "</" ).append( key ).append( ">" );
		}
		return bf;
	}

	public String composeHead( String actionCode, int bodyLength ) {
		StringBuffer sb = new StringBuffer();
		if( actionCode != null && !"".equals( actionCode ) ) {
			String len = String.valueOf( bodyLength );
			len = "00000000".substring( len.length() ) + len;
			sb.append( len );// 报文体长度
			sb.append( "TMS" ).append( " " ).append( " " ).append( " " ).append( " " ).append( " " );// 服务号
			sb.append( actionCode );// 交易号
			sb.append( "XML" ).append( " " );// 报文体类型
			sb.append( " " ).append( " " ).append( " " ).append( " " ).append( " " ).append( " " ).append( " " ).append( " " );// 返回码
		}
		return sb.toString();
	}

	/**
	 * 发送报文
	 * 
	 * @param sb
	 * @param runId
	 * @param head
	 * @param body
	 * @param timeOut
	 * @param resendTimes
	 * @return 返回结果
	 */
	/*public static int send_request(StringBuffer sb, String runId, String head,
			String body, int timeOut, int resendTimes) {
		int return_value = 0;
		try {
			socket_pool sp = socket_pool.Instance();
			int version = 0;
			int serv_count = -1;
			int serv_count_all = -1;
			int send_times = 0;
			QUERY_SERVER:
			for (;;) {
				if (send_times > resendTimes)
				{
					return_value = 1;
					break QUERY_SERVER;
				}
				
				for (; serv_count == -1;) {
					version = sp.get_version();
					serv_count = socket_pool.Instance().size(version);
					serv_count_all = socket_pool.Instance().size_all(version);
					if (serv_count == 0) {
						return_value = 1;
						break QUERY_SERVER;
					}
				}
	
				int serv_id = (hash.clac(runId) >>> 1) % 53777 % serv_count_all;
				int err_id = socket_pool.Instance().send(sb, version, serv_id, head, body, timeOut);
				if (err_id != 0) {
					if (err_id > 0)
						System.out.println("服务器[" + serv_id + "]发生错误：" + sp.error_info(err_id));
					serv_count = -1;
					if (err_id > 1)
						send_times++;
					continue;
				}
	
				if (serv_count == -1)
					continue;
				break;
			}
			if (return_value == 1) {
				//throw new ServerNotFoundException("没有可用的风险评估服务器");
			}
		} catch (Exception e) {
			System.out.println("The method 'sendRequest' of the 'TransApiMessage' class exception.！" + e.getMessage());
		}
		return 0;
	}
	*/
	private void saveTmsRunRuleTrigAndTmsRateLog( List<Map<String, Object>> ruleIdList, String level, String score, String user_id ) {
		String txnCode = StringUtil.randomUUID();
		String txnType = "";

		String sqlSeqNext = ((SqlMap) bean.get( "tmsSqlMap" )).getSql( "tms.common.sequenceid" ).replaceFirst( "\\$\\{SEQUENCENAME\\}", "SEQ_TMS_RUN_RULETRIG_ID" );
		Map<String, Object> params = new HashMap<String, Object>();

		for( Map<String, Object> ruleIdAndRuleScoreMap : ruleIdList ) {
			//			dao_rule_hit dao_rule_hit = new dao_rule_hit();
			List<Map<String, Object>> sequenceValueList = tmsSimpleDao.queryForList( sqlSeqNext, params );
			Map<String, Object> sequenceValueMap = sequenceValueList.get( 0 );
			BigDecimal tmsRuleTrigId = (BigDecimal) sequenceValueMap.get( "SEQUENCEID" );
			txnType = (String) ruleIdAndRuleScoreMap.get( "ruleTxn" );
			String message = "";
			String numTimes = "";
			BigDecimal ruleId = (BigDecimal) ruleIdAndRuleScoreMap.get( "ruleId" );
			BigDecimal ruleScore = (BigDecimal) ruleIdAndRuleScoreMap.get( "ruleScore" );
			long createTime = System.currentTimeMillis();
			Map<String, Object> condsMap = new HashMap<String, Object>();
			condsMap.put( "TRIGID", tmsRuleTrigId );
			condsMap.put( "TXNCODE", txnCode );
			condsMap.put( "TXNTYPE", txnType );
			condsMap.put( "RULEID", ruleId );
			condsMap.put( "MESSAGE", message );
			condsMap.put( "NUMTIMES", numTimes );
			condsMap.put( "CREATETIME", createTime );
			condsMap.put( "RULE_SCORE", ruleScore );
			tmpSimpleDao.create( "TMS_RUN_RULETRIG", condsMap );

		}
		this.saveTmsUserratelog( user_id, score, level, txnCode, txnType );
	}

	public static long db_next( data_source ds, String seq_next ) {
		if( ds == null ) {
			ds = new data_source();
		}

		batch_stmt_jdbc stmt = new batch_stmt_jdbc( ds, seq_next, new int[] {} );
		final AtomicLong seqid = new AtomicLong( 0 );
		try {
			stmt.query( new Object[] {}, new row_fetch() {
				public boolean fetch( ResultSet rs ) throws SQLException {
					seqid.set( rs.getLong( 1 ) );
					return true;
				}
			} );
		}
		catch( SQLException e ) {
			log.error( "-----sequence exception-------", e );
		}
		finally {
			stmt.close();
		}

		return seqid.get();
	}

	/**
	 * 获取ruleId的查询条件
	 * @param hitRules
	 * @param conditionListMap
	 * @return
	 */
	private List<Map<String, Object>> getConditionMap( String hitRules ) {
		List<Map<String, Object>> conditionListMap = new ArrayList<Map<String, Object>>();
		if( StringUtil.isNotEmpty( hitRules ) ) {
			String[] hitRule = hitRules.split( "]," );
			for( int i = 0; i < hitRule.length; i++ ) {
				String[] tmpHitRule = hitRule[i].split( "," );
				//				String ruleScore = tmpHitRule[tmpHitRule.length-1];
				String ruleTxn = tmpHitRule[0].replace( "[", "" );
				String ruleName = tmpHitRule[1];
				Map<String, Object> conditionMap = new HashMap<String, Object>();
				conditionMap.put( "ruleName", ruleName );
				conditionMap.put( "ruleTxn", ruleTxn );
				//				conditionMap.put("ruleScore", ruleScore);
				conditionListMap.add( conditionMap );
			}
		}
		return conditionListMap;
	}

	/**
	 * 获取ruleId及ruleScore
	 * @param conditionListMap
	 * @return
	 */
	private List<Map<String, Object>> getRuleIdList( List<Map<String, Object>> conditionListMap ) {
		List<Map<String, Object>> ruleIdList = new ArrayList<Map<String, Object>>();
		for( Map<String, Object> conditionMap : conditionListMap ) {
			String sql = "SELECT A.RULE_ID ruleId,A.RULE_SCORE ruleScore  FROM TMS_COM_RULE A WHERE A.RULE_NAME=:ruleName AND A.RULE_TXN=:ruleTxn";

			List<Map<String, Object>> ruleIdListMap = tmsSimpleDao.queryForList( sql, conditionMap );
			if( ruleIdListMap != null && ruleIdListMap.size() > 0 ) {
				Map<String, Object> ruleIdMap = ruleIdListMap.get( 0 );
				BigDecimal ruleId = (BigDecimal) ruleIdMap.get( "ruleId" );
				BigDecimal ruleScore = (BigDecimal) ruleIdMap.get( "ruleScore" );
				Map<String, Object> tempConditionMap = new HashMap<String, Object>();
				tempConditionMap.put( "ruleId", ruleId );
				tempConditionMap.put( "ruleScore", ruleScore );
				tempConditionMap.put( "ruleTxn", conditionMap.get( "ruleTxn" ) );
				ruleIdList.add( tempConditionMap );
			}
		}
		return ruleIdList;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return this.count;
	}

}
