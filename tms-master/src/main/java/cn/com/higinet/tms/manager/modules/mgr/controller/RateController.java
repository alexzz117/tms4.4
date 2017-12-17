package cn.com.higinet.tms.manager.modules.mgr.controller;

import java.lang.Thread.State;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.mgr.service.RateService;
import cn.com.higinet.tms.manager.modules.mgr.service.ServerService;

/**
 * 评级控制类
 * @author zlq
 * @author zhang.lei
 */

@RestController("rateController")
@RequestMapping("/tms/rate")
public class RateController {

	@Autowired
	private RateService rateService;

	@Autowired
	private ServerService serverService;

	/**
	 * 评级列表查询页面
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String rateListView() {
		return "tms35/mgr/rate/rate_list";
	}

	/**
	 * 评级列表查询交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model rateListActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		Page<Map<String, Object>> page = new Page<Map<String, Object>>();
		page.setList( rateService.rateList( reqs ) );
		model.setPage( page );
		return model;
	}

	/**
	 * 评级修改交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/mod", method = RequestMethod.POST)
	public Model rateModActoin( @RequestBody Map<String, String> reqs, HttpServletRequest request ) {
		Model model = new Model();
		request.setAttribute( "RS_NAME", reqs.get( "RS_NAME" ) );
		rateService.rateMod( reqs );
		return model;
	}

	/**
	 * 评级列表查询页面
	 * @return
	 */
	@RequestMapping(value = "/levelList", method = RequestMethod.GET)
	public String levelListView() {
		return "tms35/mgr/rate/level_list";
	}

	/**
	 * 评级列表历史查询页面
	 * @return
	 */
	@RequestMapping(value = "/ratehistorys", method = RequestMethod.GET)
	public String rateHistoryView() {
		return "tms35/mgr/rate/ratehistory";
	}

	/**
	 * 评级列表历史查询
	 * @return
	 */
	@RequestMapping(value = "/rateList", method = RequestMethod.POST)
	public Model rateList( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.setPage( rateService.rateHistoryList( reqs ) );
		return model;
	}

	/**
	 * 评级列表查询交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/levelList", method = RequestMethod.POST)
	public Model levelListActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.setPage( rateService.levelPage( reqs ) );
		return model;
	}

	/**
	 * 修改风险等级
	 */
	@RequestMapping(value = "/editRiskLevel", method = RequestMethod.POST)
	public Model editRiskLevel( @RequestBody Map<String, String> reqs, HttpServletRequest request ) {
		Model model = new Model();
		request.setAttribute( "RISKLEVEL", reqs.get( "RISKLEVEL" ) );
		rateService.updateRiskLevel( reqs );
		return model;
	}

	/**
	 * 评级列表查询交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/singleRate", method = RequestMethod.POST)
	public Model singleRateActoin( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		String signal = "single";
		rateService.singleRate( reqs, signal );
		return model;
	}

	Map<String, Thread> t_m1 = new HashMap<String, Thread>();

	/**
	 * 根据查询条件进行批量评级
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/queryRate", method = RequestMethod.POST)
	public Model queryRateActoin( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		String table_name = MapUtil.getString( reqs, "TABLE_NAME" );//TBL_USER
		Thread clientthread = t_m1.get( table_name );

		if( clientthread != null && !clientthread.getState().equals( State.TERMINATED ) ) {
			return model;
		}

		/* 获取可以评级的服务器*/
		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "SERVTYPE", "1" );
		List<Map<String, Object>> server_list = serverService.listServer( conds );
		if( server_list == null || server_list.size() == 0 ) {
			throw new TmsMgrServiceException( "没有可用的server" );
		}
		Map<Integer, Map<String, Object>> ser_map = new HashMap<Integer, Map<String, Object>>();
		for( int i = 0; i < server_list.size(); i++ ) {
			ser_map.put( i, server_list.get( i ) );
		}

		/*启动任务*/
		Task1 tast1 = new Task1( reqs, ser_map );
		clientthread = new Thread( tast1 );
		clientthread.start();
		t_m1.put( table_name, clientthread );
		return model;
	}

	class Task1 implements Runnable {
		Map<String, Object> reqs;
		Map<Integer, Map<String, Object>> ser_map;

		public Task1( Map<String, Object> reqs, Map<Integer, Map<String, Object>> ser_map ) {
			this.reqs = reqs;
			this.ser_map = ser_map;
		}

		public void run() {
			rateService.queryRate( reqs, ser_map );
		}
	}

	/**
	 *批量评级 
	 */
	@RequestMapping(value = "/batchRate", method = RequestMethod.POST)
	public Model batchRateAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		String signal = "single";
		rateService.singleRate( reqs, signal );

		return model;
	}

	/**
	 * 用户评级的规则的页面
	 */
	@RequestMapping(value = "/userRateRule", method = RequestMethod.GET)
	public String userRateRuleView() {
		return "tms35/mgr/rate/userRateRule";
	}

	Map<String, Thread> t_m = new HashMap<String, Thread>();

	/**
	 * 评级列表查询交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/allRate", method = RequestMethod.POST)
	public Model allRateActoin( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		String table_name = MapUtil.getString( reqs, "TABLE_NAME" );//TBL_USER
		Thread clientthread = t_m.get( table_name );

		if( clientthread != null && !clientthread.getState().equals( State.TERMINATED ) ) {
			return model;
		}

		/* 获取可以评级的服务器*/
		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "SERVTYPE", "1" );
		List<Map<String, Object>> server_list = serverService.listServer( conds );
		if( server_list == null || server_list.size() == 0 ) {
			throw new TmsMgrServiceException( "没有可用的server" );
		}
		Map<Integer, Map<String, Object>> ser_map = new HashMap<Integer, Map<String, Object>>();
		for( int i = 0; i < server_list.size(); i++ ) {
			ser_map.put( i, server_list.get( i ) );
		}

		/*启动任务*/
		Task tast = new Task( reqs, ser_map );
		clientthread = new Thread( tast );
		clientthread.start();
		t_m.put( table_name, clientthread );

		return model;
	}

	class Task implements Runnable {
		Map<String, Object> reqs;
		Map<Integer, Map<String, Object>> ser_map;

		public Task( Map<String, Object> reqs, Map<Integer, Map<String, Object>> ser_map ) {
			this.reqs = reqs;
			this.ser_map = ser_map;
		}

		public void run() {
			rateService.allRate( reqs, ser_map );
		}
	}

	/**
	 * 评级列表查询交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/rateCompletePert", method = RequestMethod.POST)
	public Model rateCompletePertActoin( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.set( "percent", rateService.completePercent( reqs ) );
		return model;
	}

	/**
	* 查询评级规则
	* @param reqs
	* @param request
	* @return
	*/
	@RequestMapping(value = "/queryRateRules", method = RequestMethod.POST)
	public Model alarmStrategyAction( @RequestBody Map<String, String> reqs, HttpServletRequest request ) {
		Model model = new Model();
		String txnCode = MapUtil.getString( reqs, "txncode" );
		String txnType = MapUtil.getString( reqs, "txntype" );
		model.set( "ruleList", rateService.getTransHitRuleList( txnCode, txnType ) );
		return model;
	}
}