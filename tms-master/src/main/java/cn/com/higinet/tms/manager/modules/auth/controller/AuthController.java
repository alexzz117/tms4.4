/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.modules.auth.controller;

import java.sql.Timestamp;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import cn.com.higinet.tms.base.entity.common.RequestModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.modules.auth.exception.TmsMgrAuthDataSyncException;
import cn.com.higinet.tms.manager.modules.auth.exception.TmsMgrAuthDepException;
import cn.com.higinet.tms.manager.modules.auth.service.AuthService;
import cn.com.higinet.tms.manager.modules.mgr.util.MgrDateConvertUtil;

/**
 * 交易控制类
 * @author zhangfg
 * @version 1.0.0, 2012-9-6
 */
@RestController("authController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/auth")
public class AuthController extends ApplicationObjectSupport {
	
	@Autowired
	private AuthService authService;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * 跳转到授权中心模块首页：待授权模块列表页面
	 * @return
	 */
	/*@RequestMapping(value = "/modelList", method = RequestMethod.GET)
	public String centerView() {
		return "tms/auth/auth_center";
	}*/

	/**
	 * 显示待授权授权模块列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/modelList", method = RequestMethod.POST)
	public Model centerActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		Page<Map<String, Object>> page = new Page<Map<String, Object>>();
		page.setList( authService.showCenter( reqs ) );
		model.setPage( page );
		return model;
	}

	/**
	 * 跳转到待授权列表
	 * @return
	 */
	/*@RequestMapping(value = "/authDataList", method = RequestMethod.GET)
	public String dataListView() {
		return "tms/auth/auth_datalist";
	}*/

	/**
	 * 显示待授权列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/authList", method = RequestMethod.POST)
	public Model dataListActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.setPage( authService.dataList( reqs ) );
		return model;
	}

	/**
	 * 跳转到数据对比页面
	 * @return
	 */
	/*@RequestMapping(value = "/dataCompare", method = RequestMethod.GET)
	public String dataCompareView() {
		return "tms/auth/auth_dataCompare";
	}*/

	/**
	 * 获取需要对比的新旧数据
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/dataCompare", method = RequestMethod.POST)
	public Model dataCompareAction( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		Page page = new Page();
		page.setList( authService.getDataCompare( reqs ) );
		model.setPage( page );
		return model;
	}

	/**
	 * 跳转到授权信息详细页面
	 * @return
	 */
	/*@RequestMapping(value = "/authOperate", method = RequestMethod.GET)
	public String toAuthView() {
		return "tms/auth/auth_operate";
	}*/

	/**
	 * 更新授权信息
	 * @param regs
	 * @return
	 */
	@RequestMapping(value = "/modAuth", method = RequestMethod.POST)
	public Model updateAuthActoin( @RequestBody Map<String, String> reqs, HttpServletRequest request ) {
		Model model = new Model();
		try {
			authService.batchUpadteAuth( reqs );
		}
		catch( TmsMgrAuthDataSyncException e ) {
			model.addError( "认证数据同步失败，信息为：" + e.getMessage() );
		}
		catch( TmsMgrAuthDepException e ) {
			e.printStackTrace();
			model.addError( "存在授权依赖，请单独对被依赖的授权记录进行授权，信息为：" + e.getMessage() );
		}

		String authStatus = reqs.get( "AUTH_STATUS" );
		if( "1".equals( authStatus ) ) {
			authStatus = "是";
		}
		else {
			authStatus = "否";
		}
		String operateDataVal = reqs.get( "OPERATEDATA_VALUE" );
		String funcName = reqs.get( "FUNCNAME" );
		String operatename = reqs.get( "OPERATENAME" );
		String txnname = reqs.get( "TXNNAME" );
		if( null != operateDataVal && !operateDataVal.equals( "" ) && null != funcName && !funcName.equals( "" ) ) {
			String[] operateDataValArr = operateDataVal.split( "~" );
			String[] funcNameArr = funcName.split( "," );
			String[] operatenameArr = operatename.split( "," );
			String[] txnnameArr = txnname.split( "," );
			if( operateDataValArr.length > 0 && funcNameArr.length > 0 && operateDataValArr.length == funcNameArr.length ) {
				Map<String, List<Map<String, Object>>> formMap = new HashMap<String, List<Map<String, Object>>>();
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for( int i = 0; i < operateDataValArr.length; i++ ) {
					Map<String, Object> mapInfo = new HashMap<String, Object>();
					mapInfo.put( "OPERATEDATA_VALUE", operateDataValArr[i] );
					mapInfo.put( "FUNCNAME", funcNameArr[i] );
					mapInfo.put( "OPERATENAME", operatenameArr != null && operatenameArr.length >= i + 1 ? operatenameArr[i] : "" );
					mapInfo.put( "TXNNAME", txnnameArr != null && txnnameArr.length >= i + 1 ? txnnameArr[i] : "" );
					mapInfo.put( "AUTH_STATUS", authStatus );
					mapInfo.put( "AUTH_MSG", reqs.get( "AUTH_MSG" ) );
					list.add( mapInfo );
				}
				formMap.put( "del", list );

				try {
					request.setAttribute( "postData", objectMapper.writeValueAsString( formMap ) );
				}
				catch( Exception e ) {
					e.printStackTrace();
				}
			}

		}
		return model;
	}

	/**
	 * 转向授权日志列表页面
	 * @return
	 */
	/*@RequestMapping(value = "/toLog", method = RequestMethod.GET)
	public String toLogView() {
		return "tms/auth/auth_loglist";
	}*/

	/**
	 * 修改授权信息
	 * @param regs
	 * @return
	 */
	@RequestMapping(value = "/toLog", method = RequestMethod.POST)
	public Model toLogActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		try {
			model.setPage( authService.authLogList( reqs ) );
		}
		catch( Exception e ) {
			e.printStackTrace();
			model.addError( "查询授权日志失败！" + e );
		}
		return model;
	}

	@RequestMapping(value = "/listSubOperate", method = RequestMethod.GET)
	public String subOperateView() {
		return "tms/auth/auth_subdatalist";
	}

	@RequestMapping(value = "/listSubOperate", method = RequestMethod.POST)
	public Model subOperateAction( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.setPage( authService.subDataList( reqs ) );
		return model;
	}

	/*/////////////////////////////////////授权查看模块////////////////////////////////////////*/
	/**
	 * 授权查看主页面，授权查看模块
	 * @return
	 */
	@RequestMapping(value = "/authQuery", method = RequestMethod.GET)
	public String authQueryView() {
		return "tms/auth/auth_query";
	}

	@RequestMapping(value = "/authQuery", method = RequestMethod.POST)
	public Model authQueryAction() {
		Model model = new Model();
		List<Map<String, Object>> list = authService.listAuthQueryModel();
		Page<Map<String, Object>> page = new Page<Map<String, Object>>();
		page.setList( list );
		model.setPage( page );
		return model;
	}

	/**
	 * 转向授权信息列表页面
	 * @return
	 */
	/*@RequestMapping(value = "/dataquary", method = RequestMethod.GET)
	public String dataQueryView() {
		return "tms/auth/auth_dataquary";
	}*/

	/**
	 * 授权信息查询
	 * @param regs
	 * @return
	 */
	@RequestMapping(value = "/dataquary", method = RequestMethod.POST)
	public Model dataQueryActoin(@RequestBody RequestModel reqs, HttpServletRequest request ) {
		Model model = new Model();

		try {
			String[] time = request.getParameterValues( "PROPOSER_TIME" );
			String startTime = reqs.getString("startDate");
			String endTime = reqs.getString("endDate");
			if(StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)){
				reqs.put( "start_time", startTime);
				reqs.put( "end_time", endTime);
			}
//			if( time != null && time.length == 1 ) {
//				if( !CmcStringUtil.isBlank( time[0] ) ) {
//
//				}
//			}
//
//			if( time != null && time.length == 2 ) {
//				if( !CmcStringUtil.isBlank( time[0] ) ) {
//					reqs.put( "START_TIME", MgrDateConvertUtil.convert2Millisr( time[0], MgrDateConvertUtil.FORMATE1 ) + "" );
//				}
//				if( !CmcStringUtil.isBlank( time[1] ) ) {
//					reqs.put( "END_TIME", MgrDateConvertUtil.convert2Millisr( time[1], MgrDateConvertUtil.FORMATE1 ) + "" );
//				}
//			}
			model.setPage( authService.historyDataList( reqs ) );
		}
		catch( Exception e ) {
			model.addError( "查询授权信息失败！" + e );
		}
		return model;
	}
}
