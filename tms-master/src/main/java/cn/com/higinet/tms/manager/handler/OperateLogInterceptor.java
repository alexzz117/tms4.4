/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.handler;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.common.Constant;
import cn.com.higinet.tms.manager.common.DBConstant;
import cn.com.higinet.tms.manager.common.Func;
import cn.com.higinet.tms.manager.common.OperateLog;
import cn.com.higinet.tms.manager.common.OperateLogFactory;
import cn.com.higinet.tms.manager.common.service.AuthService;
import cn.com.higinet.tms.manager.common.util.AuthDataBusUtil;
import cn.com.higinet.tms.manager.common.util.CmcMapUtil;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;

/**
 * 日志拦截器
 * @author chenr
 * @version 2.0.0, 2011-6-30
 * @author zhang.lei
 */

@Component
public class OperateLogInterceptor extends HandlerInterceptorAdapter {

	private final Logger logger = LoggerFactory.getLogger( OperateLogInterceptor.class );

	@Autowired
	private AuthService cmcAuthService;

	@Autowired
	OperateLogFactory operateLogFactory;

	@Autowired
	Func cmcFunc;

	private String logId = "";

	/**
	 * 拦截在业务处理器处理请求前，如果需要记录日志，生成日志ID，为作为待授权信息的关联；
	 * 同时将需要拦截的模块的信息放到request中，供在拦截器的“处理后postHandle”方法使用
	 */
	@Override
	public boolean preHandle( HttpServletRequest request, HttpServletResponse response, Object handler ) {

		String self = (String) request.getAttribute( "forward" );
		if( !CmcStringUtil.isEmpty( self ) && "self".equals( self ) ) {
			return true;
		}

		String operateuri = request.getRequestURI().toString();
		String uri = operateuri.substring( request.getContextPath().length() );
		String method = request.getMethod().toUpperCase();

		Map<String, Object> funmap = cmcFunc.getFuncByUri( uri, method );

		if( !funmap.isEmpty() ) {
			String logId = Stringz.randomUUID().toUpperCase();
			this.logId = logId;
			AuthDataBusUtil.openDataBus();
			AuthDataBusUtil.put( DBConstant.CMC_OPERATE_LOG_LOG_ID, logId );
			AuthDataBusUtil.put( DBConstant.CMC_FUNC, funmap );
			//将当前操作员的ID放入数据总线中，在生成待授权信息时使用
			Map<String, String> operator = null;
			if( uri.contains( "logout" ) ) { //用户退出操作
				operator = (Map<String, String>) request.getAttribute( Constant.SESSION_KEY_OPERATOR );
			}
			else {
				operator = (Map<String, String>) request.getSession().getAttribute( Constant.SESSION_KEY_OPERATOR );
			}
			if( operator != null ) {
				AuthDataBusUtil.put( DBConstant.CMC_OPERATOR_OPERATOR_ID, operator.get( DBConstant.CMC_OPERATOR_OPERATOR_ID ) );
			}
		}
		else {
			this.logId = "";
			if( AuthDataBusUtil.get( DBConstant.CMC_FUNC ) != null ) {
				AuthDataBusUtil.remove( DBConstant.CMC_FUNC );
			}
		}

		return true;
	}

	//如果没有视图
	/**
	 * 拦截在业务处理器处理完请求后，进行处理
	 * @param request
	 * @param response
	 * @param handler
	 * @throws ServletException,IOException
	 */
	@Override
	public void postHandle( HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView ) throws Exception {
		//TODO 这里最好的做法是指定需要记录日志的模块(URI)
		//1.可以配置注解到Controller的方法
		//2.可以配置单独的配置文件
		//3.可以配置到数据库专门的配置表

		//记录完整URL
		//Request中的请求数据
		//Model中的响应结果
		//记录请求方的IP

		/*
		 * StringUtil.isEmpty(self)
		 * 不是双审uri或者是双审的第二次
		 */
		String self = (String) request.getAttribute( "forward" );
		String operateuri = request.getRequestURI().toString();

		if( CmcStringUtil.isEmpty( self ) && operateuri.indexOf( "dualaudit" ) > -1 ) {
		}
		else {
			try {
				if( request.getAttribute( Constant.SESSION_KEY_OPERATOR ) != null || (modelAndView != null && modelAndView instanceof Model) ) {

					String uri = operateuri.substring( request.getContextPath().length() );
					String method = request.getMethod().toUpperCase();
					OperateLog operateLog = operateLogFactory.newLog( uri, method, request );

					if( operateLog != null ) {
						if( modelAndView != null && modelAndView instanceof Model ) {
							Model model = (Model) modelAndView;
							if( "true".equals( model.get( "success" ).toString() ) ) {

								formatLog( request );
								operateLog.doLog( "", true );
							}
							else {
								operateLog.doLog( CmcStringUtil.objToString( model.get( "error" ) ), false );
							}
						}
						else {
							operateLog.doLog( "", true );
						}
					}
					AuthDataBusUtil.closeDataBus();
				}
				else {
					logger.debug( "a modelAndView return:" + modelAndView );
				}
			}
			catch( Exception e ) {
				logger.error( "OperateLogInterceptor error.", e );
			}
		}

		//处理授权日志
		dealAuthLog();
	}

	/**
	 * 处理授权日志
	 */
	private void dealAuthLog() {
		if( CmcStringUtil.isEmpty( logId ) ) {
			return;
		}

		List<Map<String, Object>> authLogList = cmcAuthService.getAuthLogByLogId( logId );
		List<Map<String, Object>> logList = cmcAuthService.getLogByLogId( logId );

		if( authLogList == null || authLogList.size() == 0 || logList == null || logList.size() == 0 ) {
			return;
		}

		String[] updateSqls = new String[authLogList.size()];
		if( authLogList.size() == logList.size() ) {
			//			for (Map<String, Object> authLogMap : authLogList) {
			for( int i = 0; i < authLogList.size(); i++ ) {
				//				for (Map<String, Object> logMap : logList) {
				Map<String, Object> authLogMap = authLogList.get( i );
				Map<String, Object> logMap = logList.get( i );
				String authLogOrder = CmcMapUtil.getString( authLogMap, "LOG_ORDER" );
				String logOrder = CmcMapUtil.getString( logMap, "ORDER_ID" );
				System.out.println( "authLogOrder=" + authLogOrder + ",logOrder=" + logOrder );

				String sql = getUpdateSql( authLogMap, logMap, authLogOrder );
				updateSqls[i] = sql;
				//					if(authLogOrder.equals(logOrder)){
				//						String sql = getUpdateSql(authLogMap, logMap, authLogOrder);
				//						updateSqls[i++] = sql;
				//					}
				//				}
			}
		}
		else {
			System.err.println( "authLogList.size()=" + authLogList.size() + "logList.size()=" + logList.size() );
			System.err.println( "未能成功更新授权日志！！" );
		}

		cmcAuthService.batchUpdateAuthLog( updateSqls );

		//		String[] updateSqls = new String[authLogList.size()];
		//		if(authLogList.size()==1 && logList.size()==1){	//非批量操作
		//			Map<String, Object> authLogMap = authLogList.get(0);
		//			Map<String, Object> logMap = logList.get(0);
		//			
		//			String authLogOrder = MapUtil.getString(authLogMap, "LOG_ORDER");
		//			String sql = getUpdateSql(authLogMap, logMap, authLogOrder);
		//			updateSqls[0] = sql;
		//		} else {		//批量操作
		//			int i=0;
		//			for (Map<String, Object> authLogMap : authLogList) {
		//				for (Map<String, Object> logMap : logList) {
		//					String authLogOrder = MapUtil.getString(authLogMap, "LOG_ORDER");
		//					String logOrder = MapUtil.getString(logMap, "ORDER_ID");
		//					
		//					if(authLogOrder.equals(logOrder)){
		//						String sql = getUpdateSql(authLogMap, logMap, authLogOrder);
		//						updateSqls[i++] = sql;
		//					}
		//				}
		//			}
		//		}

		//		cmcAuthService.batchUpdateAuthLog(updateSqls);
	}

	/**
	 * @param authLogMap
	 * @param logMap
	 * @param authLogOrder
	 * @return
	 */
	private String getUpdateSql( Map<String, Object> authLogMap, Map<String, Object> logMap, String authLogOrder ) {
		String logKey = CmcMapUtil.getString( logMap, "PRIMARY_KEY_ID" );
		String authId = CmcMapUtil.getString( authLogMap, "AUTH_ID" );
		String sql = "UPDATE TMS_MGR_AUTHLOG SET LOG_ID = '" + logKey + "' WHERE AUTH_ID='" + authId + "' AND LOG_ORDER = '" + authLogOrder + "'";
		return sql;
	}

	@Autowired
	private ObjectMapper objectMapper = null;

	public void formatLog( HttpServletRequest request ) {

		String json = request.getParameter( "postData" );
		String operateuri = request.getRequestURI().toString();

		Map<String, List<Map<String, ?>>> formMap = null;
		try {
			formMap = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
		}
	}
}
