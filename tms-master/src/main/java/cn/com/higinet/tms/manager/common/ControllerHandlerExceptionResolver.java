/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import cn.com.higinet.tms.base.entity.common.Model;

/**
 * Controller层的异常处理类
 * @author chenr
 * @version 2.0.0, 2011-6-23
 * 
 * @modify by chenr, 2011-07-19, 
 * 在使用了cn.com.higinet.rapid.web.view.MutilViewResolver之后，
 * Model可以被一致的处理了，而不需要单独调用MessageConverter去转换
 * 
 * 
 * @see org.springframework.web.servlet.HandlerExceptionResolver
 * 
 */
public class ControllerHandlerExceptionResolver implements HandlerExceptionResolver {

	private final Logger logger = LoggerFactory.getLogger( this.getClass() );

	public ModelAndView resolveException( HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex ) {
		if( logger.isDebugEnabled() ) logger.debug( "contentType: " + request.getContentType() );
		Model model = new Model();
		if( ex instanceof SQLException || ex instanceof DataAccessException ) {
			model.addError( "数据库异常." );
		}
		else {
			model.addError( ex.getMessage() );
		}
		//model.set("errormsg", ex.toString());
		model.set( "errormsg", "操作异常，请联系系统管理员！" );
		//打印更多信息,帮助定位错误
		String msg = "uri: " + request.getRequestURI();
		msg = msg + ", handler: " + handler;
		logger.error( msg, ex );
		return model;
	}

}
