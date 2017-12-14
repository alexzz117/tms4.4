/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.com.higinet.tms.manager.common.CodeDict;

/**
 * 公用环境参数Controller类
 * @author zhang.lei
 */
@Controller("commonEnvController")
@RequestMapping("/s/common/env")
public class EnvController {

	private static final Logger logger = LoggerFactory.getLogger( EnvController.class );

	@Autowired
	CodeDict codeDict;

	@SuppressWarnings("unchecked")
	private void _env( HttpServletRequest request, HttpServletResponse response ) throws IOException {
		response.setContentType( "text/javascript" );
		response.setCharacterEncoding( "UTF-8" );
		response.setHeader( "Cache-Control", "no-cache" );

		PrintWriter out = response.getWriter();
		//TODO 缓存
		String context = request.getContextPath();
		out.println( "jcl.env.contextPath='" + context + "';" );
		//2011-5-10 加入 profile 功能
		//数据存放在session中
		HttpSession session = request.getSession( false );
		if( session != null ) {
			Object profileObject = session.getAttribute( "PROFILE" );
			Map<String, String> profile = new HashMap<String, String>();
			if( profileObject != null && profileObject instanceof Map ) {
				profile = (Map<String, String>) profileObject;
			}
			printProfile( out, profile );
		}

		out.flush();
	}

	private void _message( HttpServletRequest request, HttpServletResponse response ) throws IOException {
		response.setContentType( "text/javascript" );
		response.setCharacterEncoding( "UTF-8" );
		response.setHeader( "Cache-Control", "no-cache" );

		Map<String, Map<String, String>> codes = new HashMap<String, Map<String, String>>();
		if( codeDict != null ) {
			try {
				codes = codeDict.getAllCodes();
			}
			catch( Throwable e ) {
				logger.error( e.getMessage(), e );
			}
		}
		PrintWriter out = response.getWriter();
		printCode( out, codes );
		out.flush();
	}

	/**
	 * 系统环境
	 * @return
	 * @throws IOException 
	 */

	@RequestMapping("/env.js")
	public void env( HttpServletRequest request, HttpServletResponse response ) throws IOException {
		_env( request, response );
	}

	/**
	 * 将国际化字符发送到前端
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/message.js")
	public void message( HttpServletRequest request, HttpServletResponse response ) throws IOException {
		_message( request, response );
	}

	/**
	 * 为了分离静态请求和动态请求（一般服务器配置从后缀名上作区分）；
	 * 所以避免使用*.js 来生成动态的js内容而使用*_js的后缀，使请求看起来像Servlet请求
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/env_js")
	public void env2( HttpServletRequest request, HttpServletResponse response ) throws IOException {
		_env( request, response );
	}

	@RequestMapping("/message_js")
	public void message2( HttpServletRequest request, HttpServletResponse response ) throws IOException {
		_message( request, response );
	}

	private String keyValuePair( String key, String value ) {
		//TODO 对字符进行转义
		return "    '" + key + "': '" + value + "'";
	}

	private String keyValuePair( String key, Map<String, String> map ) {
		//TODO 对字符进行转义
		StringBuffer sb = new StringBuffer();
		for( String k : map.keySet() ) {
			if( sb.length() > 0 ) {
				sb.append( "," );
			}
			sb.append( "'" ).append( k ).append( "': '" ).append( map.get( k ) ).append( "'" );
		}
		return "    '" + key + "': {" + sb.toString() + "}";
	}

	/**
	 * 映射代码表到前端js
	 * @param out
	 */
	private void printCode( PrintWriter out, Map<String, Map<String, String>> codes ) {

		out.println( "$.extend(jcl.code, {" );
		boolean begin = true;
		for( String key : codes.keySet() ) {
			if( !begin ) {
				out.println( "," );
			}
			out.print( keyValuePair( key, codes.get( key ) ) );
			begin = false;
		}
		out.println();
		out.println( "});" );
	}

	private void printProfile( PrintWriter out, Map<String, String> profile ) {
		out.println( "jcl.env.profile={" );
		boolean begin = true;
		for( String key : profile.keySet() ) {
			if( !begin ) {
				out.println( "," );
			}
			out.print( keyValuePair( key, profile.get( key ) ) );
			begin = false;
		}
		out.println( "};" );
	}

}
