/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import cn.com.higinet.tms.manager.common.DSContextHolder;
import cn.com.higinet.tms.manager.common.DSType;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;

/**
 * 双审授权拦截器
 * @author yangk
 * @author zhang.lei
 */
@Component
public class DualAuditInterceptor extends HandlerInterceptorAdapter {

	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	/**
	 * 拦截请求处理
	 * @param request
	 * @param response
	 * @param handler
	 * @throws ServletException,IOException
	 */
	public boolean preHandle( HttpServletRequest request, HttpServletResponse response, Object handler ) throws ServletException, IOException {
		
		String dblc = request.getRequestURL().toString();

		String self = (String) request.getAttribute( "forward" );
		if( !CmcStringUtil.isEmpty( self ) && "self".equals( self ) ) {
			return true;
		}

		if( dblc.indexOf( "/dualaudit" ) > -1 ) {
			log.debug( "数据源切换到临时库" );
			request.setAttribute( "readonly", "false" );
			DSContextHolder.setDSType( DSType.DS_TEMP );
		}
		else {
			log.debug( "数据源切换到正式库" );
			request.setAttribute( "readonly", "true" );
			DSContextHolder.setDSType( DSType.DS_OFFICIAL );

		}

		return true;
	}
}
