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
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.common.Constant;
import cn.com.higinet.tms.manager.common.Func;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;

/**
 * 鉴权拦截器
 * @author chenr
 * @version 2.0.0, 2011-6-30
 */
@Component
public class AuthorizationInterceptor extends HandlerInterceptorAdapter{
	
	private final static String GET = "GET";
	private final static String POST = "POST";
	
	@Autowired
	@Qualifier("cmcFunc")
	private Func func;
	
	/**
	 * 拦截请求处理
	 * @param request
	 * @param response
	 * @param handler
	 * @throws ServletException,IOException
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
	    throws ServletException, IOException {
		
		String self = (String) request.getAttribute("forward");
		if (!CmcStringUtil.isEmpty(self) && "self".equals(self)) {
			return true;
		}
		
		String contextPath = request.getContextPath();
		String uri = request.getRequestURI();
		if(contextPath.length() > 0)
			uri = uri.substring(contextPath.length());
		
		
		//注意防止"app//xxx"的非法路径绕过检查
		if(uri.indexOf("//") != -1){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        	return false;
		}
		
		//放过公用资源部分
		if(uri.startsWith(Constant.URI_STATIC_PREFIX)){
			return true;
		}
		//会话超时
		HttpSession session = request.getSession();
		Object o = session.getAttribute(Constant.SESSION_KEY_OPERATOR);
		if(Constant.URI_LOGIN.equals(uri)){
			return true; //登录界面放过
		}
		
		//会话超时
		if(null == o){
			sessionTimeout(contextPath, request, response);
			return false;
		}
		
		//需授权功能的权限检查
		if(func.isProtectedUri(uri)){
			
			String[] funcIds = func.getEnableFuncIdsByUri(uri);
			String[] funcIds0 = (String[])session.getAttribute(Constant.SESSION_KEY_FUNCIDS);
			if(funcIds.length == 0) return true;
			if(null == funcIds0) {
				forbidden(request, response);
				return false;
			}
			for(String funcId : funcIds){
				for(String funcId0 : funcIds0){
					if(funcId.equals(funcId0)){
						return true;
					}
				}
			}
			//未授权访问受保护URI
			forbidden(request, response);
			return false;
			
		}
		
		return true;
	}
	
	/**
	 * 会话超时
	 * @param contextPath
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void sessionTimeout(String contextPath, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String method = request.getMethod();
		//这里简要认为POST请求是数据请求， GET请求是视图类请求
		if(GET.equalsIgnoreCase(method)){
			response.sendRedirect(contextPath + Constant.URI_LOGIN);
		}else if(POST.equalsIgnoreCase(method)){
			//如果会话丢失，应该返回一个需要登录的标记
			//理论上解应该按照映射到的方法的具体返回类型来返回对应类型的消息
			Model model = new Model();
			model.addError("error.cmc.nosession");
			model.set("url", Constant.URI_LOGIN);
			ObjectMapper m = new ObjectMapper();
			m.writeValue(response.getOutputStream(), model.getModel());
		}else{
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	/**
	 * 禁止访问
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void forbidden(HttpServletRequest request, HttpServletResponse response) 
			throws IOException{
		String method = request.getMethod();
		//这里简要认为POST请求是数据请求， GET请求是视图类请求
		if(POST.equalsIgnoreCase(method)){
			//如果会话丢失，应该返回一个需要登录的标记
			//理论上解应该按照映射到的方法的具体返回类型来返回对应类型的消息
			Model model = new Model();
			model.addError("error.cmc.forbidden");
			ObjectMapper m = new ObjectMapper();
			m.writeValue(response.getOutputStream(), model.getModel());
		}else{
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
}
