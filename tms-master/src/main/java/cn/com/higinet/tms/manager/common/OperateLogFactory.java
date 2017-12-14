/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.common.service.LogService;
import cn.com.higinet.tms.manager.common.util.AuthDataBusUtil;
import cn.com.higinet.tms.manager.common.util.CmcMacUtil;
import cn.com.higinet.tms.manager.common.util.CmcMapUtil;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.SimpleDao;

/**
 * 获取操作日志记录类工厂实现类
 * @author chenr
 * @version 2.0.0, 2011-6-30
 */
@Service("operateLogFactory")
public class OperateLogFactory {
	private final Log logger = LogFactory.getLog( OperateLogFactory.class );

	@Autowired
	Func cmcFunc;

	@Autowired
	private LogService cmcLogService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private SimpleDao tmsSimpleDao;

	/**
	 * 获取操作日志记录类对象
	 * @param uri			记录日志路径
	 * @param method		请求方式：POST/GET
	 * @param request		HttpServletRequest 对象
	 * @return 日志记录类对象
	 */
	@SuppressWarnings({
			"unchecked", "rawtypes"
	})
	public OperateLog newLog( String uri, String method, HttpServletRequest request ) {

		OperateLog operatelog = new OperateLog( cmcLogService );

		//		Map<String,Object> funmap = func.getFuncByUri(uri,method);
		//zhangfg   2012-08-29  在日志拦截器中的处理前就进行了查询，如果不为空放入了request中，所以此处从request中获取
		//		Map<String,Object> funmap = (Map<String,Object>)request.getAttribute(DBConstant.CMC_FUNC);

		Map<String, Object> funmap = (Map<String, Object>) AuthDataBusUtil.get( DBConstant.CMC_FUNC );
		if( funmap != null && !funmap.isEmpty() ) {

			//			String operId = StringUtil.objToString(request.getAttribute(DBConstant.CMC_OPERATE_LOG_LOG_ID));
			String logId = CmcStringUtil.objToString( AuthDataBusUtil.get( DBConstant.CMC_OPERATE_LOG_LOG_ID ) );
			operatelog.setLogId( logId );

			Map<String, String> operator = new HashMap<String, String>();
			String operatedata = "";
			operatedata += method + ": " + uri + ";\r\nDATA:  ";
			String log_mac = "";
			String operateresult = "1";

			// add by yb --beg
			String postDataVal = "";
			Object attrPostData = request.getAttribute( "postData" );
			String paramPostData = request.getParameter( "postData" );

			if( attrPostData != null && !attrPostData.equals( "" ) ) {
				postDataVal = attrPostData.toString();
			}
			else {
				postDataVal = paramPostData;
			}
			StringBuffer operateLogData = new StringBuffer();

			if( postDataVal != null && postDataVal.length() > 0 ) {

				try {
					String config = CmcStringUtil.objToString( funmap.get( DBConstant.CMC_FUNC_INFO ) );
					if( !CmcStringUtil.isBlank( config ) ) {

						String configStr[] = config.split( ";" );
						Map<String, List<Map<String, ?>>> formMap = null;
						try {
							formMap = objectMapper.readValue( postDataVal, Map.class );
						}
						catch( Exception e ) {
							System.out.println( "exception..." );
						}

						handOperateLogInfo( operateLogData, configStr, formMap, funmap );
					}
				}
				catch( Exception e ) {
					logger.error( "解析日志配置有误！", e );
					operateLogData.append( "解析日志配置有误！" );
				}

			}
			else { // add by yb --end
				replaceTemplateValReq( request, funmap, operateLogData );
			}

			if( uri.contains( "logout" ) ) { //用户退出操作
				operator = (Map<String, String>) request.getAttribute( Constant.SESSION_KEY_OPERATOR );
				//				operatedata+="username="+operator.get(DBConstant.CMC_OPERATOR_LOGIN_NAME)+" ";
				//				operateLogData.append("username="+operator.get(DBConstant.CMC_OPERATOR_LOGIN_NAME)+" ");
			}
			else {
				operator = (Map<String, String>) request.getSession().getAttribute( Constant.SESSION_KEY_OPERATOR );
			}
			if( operator != null && !operator.isEmpty() ) { //登录成功后的操作

				try {
					log_mac = CmcMacUtil.md5( operator.get( DBConstant.CMC_OPERATOR_OPERATOR_ID ) + funmap.get( DBConstant.CMC_FUNC_FUNC_ID ) + operateresult + new Date() );
				}
				catch( NoSuchAlgorithmException e1 ) {
					logger.error( "OperateLogFactoryImpl error.", e1 );
				}
				operatelog.setOperatorId( operator.get( DBConstant.CMC_OPERATOR_OPERATOR_ID ) );
				operatelog.setOperateData( operateLogData.toString() );
			}
			else if( uri.contains( "login" ) && !"GET".equalsIgnoreCase( method ) ) { //登录失败操作		
				String username[] = (String[]) request.getParameterValues( "username" );
				String sql = "select * from " + DBConstant.CMC_OPERATOR + " where flag<>9  ";
				if( username[0] != null && !"".equals( username[0] ) ) {
					sql += " and login_name ='" + username[0] + "'";
				}
				List<Map<String, Object>> list = tmsSimpleDao.queryForList( sql );
				String operatorId = "";
				if( list != null && list.size() > 0 ) {
					operatorId = ((Map) list.get( 0 )).get( DBConstant.CMC_OPERATOR_OPERATOR_ID ).toString();
				}
				operatelog.setOperatorId( operatorId );
				operatelog.setOperateData( operateLogData.toString() );
				try {
					log_mac = CmcMacUtil.md5( operatorId + funmap.get( DBConstant.CMC_FUNC_FUNC_ID ) + operateresult + new Date() );
				}
				catch( NoSuchAlgorithmException e1 ) {
					logger.error( "OperateLogFactoryImpl error.", e1 );
				}
			}

			operatelog.setOperateResult( operateresult );
			operatelog.setFuncId( funmap.get( DBConstant.CMC_FUNC_FUNC_ID ).toString() );
			operatelog.setOperateTime( new Date() );
			operatelog.setLogMac( log_mac );

		}
		else {
			operatelog = null;
		}
		return operatelog;
	}

	public void handOperateLogInfo( StringBuffer operateLogData, String[] configStr, Map<String, List<Map<String, ?>>> formMap, Map<String, Object> funmap ) {

		List<Map<String, Object>> addList = CmcMapUtil.getList( formMap, "add" );
		List<Map<String, Object>> modList = CmcMapUtil.getList( formMap, "mod" );
		List<Map<String, Object>> delList = CmcMapUtil.getList( formMap, "del" );
		List<Map<String, Object>> copyList = CmcMapUtil.getList( formMap, "copy" );
		List<Map<String, Object>> lineAddList = CmcMapUtil.getList( formMap, "lineAdd" );
		List<Map<String, Object>> lineDelList = CmcMapUtil.getList( formMap, "lineDel" );
		List<Map<String, Object>> enableList = CmcMapUtil.getList( formMap, "valid-y" );
		List<Map<String, Object>> disbaleList = CmcMapUtil.getList( formMap, "valid-n" );

		int fag = 0;
		if( addList != null && addList.size() > 0 ) {

			for( int i = 0; i < addList.size(); i++ ) {
				fag++;
				Map<String, Object> addMap = addList.get( i );
				addMap.put( "method", "新建" );
				replaceTemplateValMap( addMap, funmap, operateLogData, fag );
			}
		}
		if( copyList != null && copyList.size() > 0 ) {

			for( int i = 0; i < copyList.size(); i++ ) {
				fag++;
				Map<String, Object> addMap = copyList.get( i );
				addMap.put( "method", "复制" );
				replaceTemplateValMap( addMap, funmap, operateLogData, fag );
			}
		}
		if( modList != null && modList.size() > 0 ) {

			for( int i = 0; i < modList.size(); i++ ) {
				fag++;
				Map<String, Object> modMap = modList.get( i );
				modMap.put( "method", "编辑" );
				replaceTemplateValMap( modMap, funmap, operateLogData, fag );
			}
		}
		if( delList != null && delList.size() > 0 ) {

			for( int i = 0; i < delList.size(); i++ ) {
				fag++;
				Map<String, Object> delMap = delList.get( i );
				delMap.put( "method", "删除" );
				replaceTemplateValMap( delMap, funmap, operateLogData, fag );
			}
		}
		if( lineAddList != null && lineAddList.size() > 0 ) {

			for( int i = 0; i < lineAddList.size(); i++ ) {
				fag++;
				Map<String, Object> lineAddMap = lineAddList.get( i );
				lineAddMap.put( "method", "线新建" );
				replaceTemplateValMap( lineAddMap, funmap, operateLogData, fag );
			}
		}

		if( lineDelList != null && lineDelList.size() > 0 ) {

			for( int i = 0; i < lineDelList.size(); i++ ) {
				fag++;
				Map<String, Object> lineDelMap = lineDelList.get( i );
				lineDelMap.put( "method", "线删除" );
				replaceTemplateValMap( lineDelMap, funmap, operateLogData, fag );
			}
		}

		if( enableList != null && enableList.size() > 0 ) {

			for( int i = 0; i < enableList.size(); i++ ) {
				fag++;
				Map<String, Object> enaMap = enableList.get( i );
				enaMap.put( "method", "启用" );
				replaceTemplateValMap( enaMap, funmap, operateLogData, fag );
			}
		}
		if( disbaleList != null && disbaleList.size() > 0 ) {

			for( int i = 0; i < disbaleList.size(); i++ ) {
				fag++;
				Map<String, Object> disMap = disbaleList.get( i );
				disMap.put( "method", "停用" );
				replaceTemplateValMap( disMap, funmap, operateLogData, fag );
			}
		}
	}

	private void replaceTemplateValMap( Map<String, Object> map, Map<String, Object> funmap, StringBuffer operateLogData, int fag ) {
		try {
			String config = CmcStringUtil.objToString( funmap.get( DBConstant.CMC_FUNC_INFO ) );
			if( !CmcStringUtil.isBlank( config ) ) {
				String configStr[] = config.split( ";" );

				for( int i = 0; i < configStr.length; i++ ) {
					String eachConfig[] = configStr[i].split( ":" );
					operateLogData.append( eachConfig[0] ).append( ":" );

					String infofromStr = eachConfig[1].substring( 1, eachConfig[1].length() - 1 );
					String fromconfig[] = infofromStr.split( "," );
					if( "request".equals( fromconfig[0] ) ) {
						if( "attrabute".equals( fromconfig[1] ) ) {

							String dataValue = "";
							String formConfigVal = (String) map.get( fromconfig[2] );

							if( "tab_name".equals( fromconfig[2] ) ) {
								if( "".equals( formConfigVal ) && "add".equals( map.get( "op" ) ) ) {
									dataValue = getSelfAndParentTranDefAsStr( (String) map.get( "parent_tab" ) ) + " > " + (String) map.get( "tab_desc" );
								}
								else if( "del".equals( map.get( "op" ) ) ) {
									dataValue = getSelfAndParentTranDefAsStr( formConfigVal ) + " > " + (String) map.get( "tab_desc" );
								}
								else {
									dataValue = getSelfAndParentTranDefAsStr( formConfigVal );
								}

							}
							else if( "TAB_NAME".equals( fromconfig[2] ) ) {
								dataValue = getSelfAndParentTranDefAsStr( formConfigVal );
							}
							else if( "SW_TXN".equals( fromconfig[2] ) ) {
								dataValue = getSelfAndParentTranDefAsStr( formConfigVal );
							}
							else if( "PS_TXN".equals( fromconfig[2] ) ) {
								dataValue = getSelfAndParentTranDefAsStr( formConfigVal );
							}
							else if( "STAT_TXN".equals( fromconfig[2] ) ) {
								dataValue = getSelfAndParentTranDefAsStr( formConfigVal );
							}
							else if( "AC_TXN".equals( fromconfig[2] ) ) {
								dataValue = getSelfAndParentTranDefAsStr( formConfigVal );
							}
							else if( "RULE_TXN".equals( fromconfig[2] ) ) {
								dataValue = getSelfAndParentTranDefAsStr( formConfigVal );
							}
							else if( "ST_TXN".equals( fromconfig[2] ) ) {
								dataValue = getSelfAndParentTranDefAsStr( formConfigVal );
							}
							else {
								dataValue = formConfigVal;
							}
							operateLogData.append( dataValue );
						}
					}
					operateLogData.append( ";" );
				}
				operateLogData.append( " &&&& " + fag + " |||| " );
			}
		}
		catch( Exception e ) {
			logger.error( "解析日志配置有误！", e );
			operateLogData.append( "解析日志配置有误！" );
		}
	}

	private void replaceTemplateValReq( HttpServletRequest request, Map<String, Object> funmap, StringBuffer operateLogData ) {
		try {
			String config = CmcStringUtil.objToString( funmap.get( DBConstant.CMC_FUNC_INFO ) );
			if( !CmcStringUtil.isBlank( config ) ) {
				String configStr[] = config.split( ";" );

				for( int i = 0; i < configStr.length; i++ ) {
					String eachConfig[] = configStr[i].split( ":" );
					operateLogData.append( eachConfig[0] ).append( ":" );

					String infofromStr = eachConfig[1].substring( 1, eachConfig[1].length() - 1 );
					String fromconfig[] = infofromStr.split( "," );
					if( "request".equals( fromconfig[0] ) ) {
						if( "param".equals( fromconfig[1] ) ) {
							String[] data = request.getParameterValues( fromconfig[2] );
							if( data != null ) {
								for( int j = 0; j < data.length; j++ ) {
									if( j > 0 ) {
										operateLogData.append( "," );
									}
									operateLogData.append( data[j] );
								}
							}
						}
						else if( "attrabute".equals( fromconfig[1] ) ) {
							String dataValue = (String) request.getAttribute( fromconfig[2] );
							operateLogData.append( dataValue );
						}
					}
					operateLogData.append( ";" );
				}
				operateLogData.append( " &&&& " + "1" + " |||| " );
			}
		}
		catch( Exception e ) {
			logger.error( "解析日志配置有误！", e );
			operateLogData.append( "解析日志配置有误！" );
		}
	}

	public String getSelfAndParentTranDefAsStr( String tab_name ) {

		if( null != tab_name && !"".equals( tab_name ) ) {
			StringBuffer signPost = new StringBuffer();
			List<Map<String, Object>> txnDef = getSelfAndParentTranDef( tab_name );

			for( Map<String, Object> map : txnDef ) {
				signPost.append( CmcMapUtil.getString( map, "TAB_DESC" ) + " > " );
			}

			signPost.delete( signPost.lastIndexOf( " > " ), signPost.length() );

			return signPost.toString();
		}
		return "";

	}

	public List<Map<String, Object>> getSelfAndParentTranDef( String tab_name ) {

		StringBuffer sb = new StringBuffer();
		sb.append( "select *" );
		sb.append( " from " ).append( "TMS_COM_TAB" );
		sb.append( " where " ).append( "TAB_NAME" );
		sb.append( " in (" ).append( cutToIdsForSql( tab_name ) ).append( ")" );

		return tmsSimpleDao.queryForList( sb.toString() );
	}
	
	/*
	 * 交易表id是有规则的
	 * 所以可以把传入的交易主键 把txnid切分成数组
	 * 每一个都是一个交易主键,包括自己
	 */
	public String[] cutToIds(String txnid) {

		String[] txnids = new String[txnid.length() / 2 + 1];
		int offset = 0;

		for (int i = 0; i < txnids.length; i++) {
			txnids[i] = txnid.substring(0, 1 + offset);
			offset += 2;
		}

		return txnids;
	}

	/*
	 * make sql commond like 'a', 'b', 'c'
	 */
	public String cutToIdsForSql(String txnid) {

		String[] txnids = cutToIds(txnid);
		StringBuffer id_sb = new StringBuffer();
		for(int i = 0; i < txnids.length; i++){
			id_sb.append("'").append(txnids[i]).append("',");
		}
		id_sb.setCharAt(id_sb.lastIndexOf(","), ' ');
		return id_sb.toString();
	}
}
