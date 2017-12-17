/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.manager.common.Constant;
import cn.com.higinet.tms.manager.common.DBConstant;
import cn.com.higinet.tms.manager.common.service.OperatorService;
import cn.com.higinet.tms.manager.common.service.SysService;
import cn.com.higinet.tms.manager.common.util.CmcMapUtil;

/**
 * 系统主控制类
 * <ul>
 * 	<li>/ ： 系统根路径</li>
 *  <li>/login ：系统登录</li>
 *  <li>/menu ：系统菜单</li>
 * </ul>
 * @author chenr
 * @version 2.0.0, 2011-6-30
 * 
 * @author zhang.lei
 */

@Controller("cmcSysController")
public class SysController {
	
	private static final Logger logger = LoggerFactory.getLogger( SysController.class );

	@Autowired
	@Qualifier("cmcSysService")
	private SysService cmcSysService;

	@Autowired
	@Qualifier("cmcOperatorService")
	private OperatorService cmcOperatorService;

	/**
	 * 主界面视图
	 * @return
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String mainView( HttpServletResponse response ) {
		//好像仅仅对IE有效果
		response.setHeader( "Cache-Control", "no-cache" );
		response.setHeader( "Pragma", "no-cache" );
		response.setDateHeader( "Expires", 0 );
		return "cmc/sys/main";
	}

	/**
	 * 登录视图
	 * @return
	 */
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String loginView( HttpServletResponse response ) {
		//好像仅仅对IE有效果
		response.setHeader( "Cache-Control", "no-cache" );
		response.setHeader( "Pragma", "no-cache" );
		response.setDateHeader( "Expires", 0 );
		return "cmc/sys/login";
	}

	/**
	 * 登录处理
	 * @param username
	 * @param password
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public Model loginAction( @RequestBody RequestModel modelMap, HttpSession session, HttpServletRequest request ) {

		String username = modelMap.getString( "username" );
		String password = modelMap.getString( "password" );

		//TODO MD5 应该放在后端，前端使用Base64编码到后端

		//MD5在前端加密，前端加密将使得密码加密失去意义
		//System.out.println("username:"+username+", password:"+password);
		//Object o = session.getAttribute(Constant.SESSION_KEY_OPERATOR);
		//TODO 防止重复登录

		Model model = new Model();
		Map<String, Object> operator = cmcSysService.getOperator( username, password );
		if( null == operator || operator.size() == 0 ) {
			model.addError( "用户名不存在!" );
		}
		else {
			String pwd = CmcMapUtil.getString( operator, DBConstant.CMC_OPERATOR_PASSWORD );
			if( !pwd.equals( password ) ) return model.addError( "密码错误!" );
			String flag = (String) operator.get( "flag" );
			if( !"1".equals( flag ) ) {
				model.addError( "用户已停用!" );
			}
			else {
				operator.remove( DBConstant.CMC_OPERATOR_PASSWORD );
				session.setAttribute( Constant.SESSION_KEY_OPERATOR, operator );
				//将操作员配置信息PROFILE放到session中
				Map<String, String> profile = new HashMap<String, String>();
				profile.put( "pagesize", "25" );
				profile.put( "maxpanel", "0" );
				String confStr = (String) operator.get( "CONF" );
				if( confStr != null && !confStr.equals( "" ) ) {
					String[] confs = confStr.split( ";" );
					String temp[] = new String[2];
					for( String conf : confs ) {
						temp = conf.split( ":" );
						profile.put( temp[0], temp[1] );
					}
				}
				profile.put( "username", (String) operator.get( DBConstant.CMC_OPERATOR_LOGIN_NAME ) );
				session.setAttribute( Constant.SESSION_KEY_PROFILE, profile );

				//将func编号缓存到会话中
				String operatorId = (String) operator.get( DBConstant.CMC_OPERATOR_OPERATOR_ID );
				String[] funcIds = cmcSysService.getOperatorFuncIds( operatorId );
				session.setAttribute( Constant.SESSION_KEY_FUNCIDS, funcIds );
				//登陆成功后，更新操作员最后登录时间
				operator.put( "LAST_LOGIN", new Date() );
				cmcOperatorService.updateOperator( operator );
				request.setAttribute( DBConstant.CMC_OPERATOR_LOGIN_NAME, (String) operator.get( DBConstant.CMC_OPERATOR_LOGIN_NAME ) );

				if( logger.isDebugEnabled() ) {
					String str = "";
					for( String s : funcIds ) {
						str += s + ",";
					}
					logger.debug( str.substring( 0, str.length() - 1 ) );
				}
			}
		}
		return model;
	}

	/**
	 * 注销登录
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public Model logoutAction( HttpSession session ) {
		Model model = new Model();
		session.removeAttribute( Constant.SESSION_KEY_OPERATOR );
		return model;
	}

	/**
	 * 注销登录
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/help", method = RequestMethod.GET)
	public String help( HttpSession session ) {
		return "cmc/sys/help";
	}

	/**
	 * 注销登录视图（直接跳转）
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logoutView( HttpServletRequest request ) {
		HttpSession session = request.getSession();
		Map<String, Object> opr = (Map<String, Object>) session.getAttribute( Constant.SESSION_KEY_OPERATOR );
		request.setAttribute( "LOGIN_NAME", opr.get( "LOGIN_NAME" ) );
		request.setAttribute( Constant.SESSION_KEY_OPERATOR, session.getAttribute( Constant.SESSION_KEY_OPERATOR ) );
		session.removeAttribute( Constant.SESSION_KEY_OPERATOR );
		return "redirect:/login";
	}

	/**
	 * 欢迎界面视图
	 * @return
	 */
	@RequestMapping(value = "/welcome")
	public String welcomeView() {
		return "cmc/sys/welcome";
	}

	/**
	 * 根据登录用户，获取菜单信息
	 * @param session
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/menu")
	public Model menu( HttpSession session ) {
		Model model = new Model();
		Map<String, Object> operator = (Map<String, Object>) session.getAttribute( Constant.SESSION_KEY_OPERATOR );
		String operatorId = (String) operator.get( DBConstant.CMC_OPERATOR_OPERATOR_ID );
		List<Map<String, Object>> menu = cmcSysService.getOperatorMenu( operatorId );
		List<Map<String, Object>> menu2 = new ArrayList<Map<String, Object>>();
		for( int i = 0; i < menu.size(); i++ ) {
			Map<String, Object> func = menu.get( i );
			Map<String, Object> func2 = new HashMap<String, Object>();

			func2.put( "id", func.get( "FUNC_ID" ) );
			func2.put( "text", func.get( "FUNC_NAME" ) );
			func2.put( "ftype", func.get( "FUNC_TYPE" ) );
			func2.put( "fid", func.get( "PARENT_ID" ) );
			func2.put( "onum", func.get( "ONUM" ) );

			//TODO 是否需要根据type转化配置部分到具体属性
			String conf = (String) func.get( "CONF" );
			if( conf != null && conf.length() > 0 ) {
				if( conf.indexOf( "," ) != -1 ) {
					conf = conf.substring( 0, conf.indexOf( "," ) );
				}
			}
			func2.put( "conf", conf );
			menu2.add( func2 );
		}
		model.setList( menu2 );
		return model;
	}

	/**
	 * 转向管理员修改密码视图
	 * 更新操作在OperatorController中
	 * @return
	 */
	@RequestMapping(value = "/profile/pwd/mod", method = RequestMethod.GET)
	public String modifyPwdView() {
		return "cmc/sys/pwd_mod";
	}

	/**
	 * 更新当前登录管理员密码
	 * @param reqs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/profile/pwd/update", method = RequestMethod.POST)
	public Model updatePwdAction( @RequestBody Map<String, String> reqs, HttpSession session, HttpServletRequest request ) {
		Model model = new Model();
		Map<String, Object> opr = (Map<String, Object>) session.getAttribute( Constant.SESSION_KEY_OPERATOR );
		Map<String, Object> operator = cmcOperatorService.getOperator( (String) opr.get( "OPERATOR_ID" ) );
		request.setAttribute( "LOGIN_NAME", opr.get( "LOGIN_NAME" ) );
		if( !operator.get( "PASSWORD" ).toString().equals( reqs.get( "old_password" ) ) ) {
			return model.addError( "旧密码输入错误" );
		}
		else {
			operator.put( "PASSWORD", reqs.get( "new_password" ) );
			operator.put( "LAST_PWD", new Date() );
			cmcOperatorService.updateOperator( operator );
		}
		return model;
	}

	/**
	 * 转向操作员配置页面
	 * @return
	 */
	@RequestMapping(value = "/profile/mod", method = RequestMethod.GET)
	public String modProfileView() {
		return "cmc/sys/profile_mod";
	}

	/**
	 * 获取操作员当前的配置
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/profile/get")
	public Model getProfileAction( HttpSession session ) {
		Model model = new Model();
		Map<String, String> operator = (Map<String, String>) session.getAttribute( Constant.SESSION_KEY_OPERATOR );
		String confStr = operator.get( "CONF" );
		Map<String, String> profile = new HashMap<String, String>();
		profile.put( "pagesize", "25" );
		profile.put( "maxpanel", "0" );

		if( confStr != null && !confStr.equals( "" ) ) {
			String[] confs = confStr.split( ";" );
			String temp[] = new String[2];
			for( String conf : confs ) {
				temp = conf.split( ":" );
				profile.put( temp[0], temp[1] );
			}
		}
		model.setRow( profile );
		return model;
	}

	/**
	 * 更新操作员配置
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/profile/mod", method = RequestMethod.POST)
	public Model modProfileAction( @RequestBody Map<String, String> reqs, HttpSession session, HttpServletRequest request ) {
		String pagesize = reqs.get( "pagesize" );
		String maxpanel = reqs.get( "maxpanel" );
		if( !pagesize.matches( "\\d{1,3}" ) ) {
			pagesize = "25";
		}
		if( !maxpanel.matches( "[0,1]" ) ) {
			maxpanel = "0";
		}
		Map<String, Object> opr = (Map<String, Object>) session.getAttribute( Constant.SESSION_KEY_OPERATOR );
		request.setAttribute( "LOGIN_NAME", opr.get( "LOGIN_NAME" ) );
		String conf = "pagesize:" + pagesize + ";maxpanel:" + maxpanel;
		opr.put( "CONF", conf );
		cmcOperatorService.updateOperator( opr );
		//刷新session中的操作员配置信息
		Map<String, String> profile = (Map<String, String>) session.getAttribute( Constant.SESSION_KEY_PROFILE );
		if( profile == null ) {
			profile = new HashMap<String, String>();
			session.setAttribute( Constant.SESSION_KEY_PROFILE, profile );
		}
		profile.put( "pagesize", pagesize );
		profile.put( "maxpanel", maxpanel );

		return Model.emptyModel();
	}
}
