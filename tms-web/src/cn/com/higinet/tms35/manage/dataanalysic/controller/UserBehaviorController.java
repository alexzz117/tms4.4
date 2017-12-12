package cn.com.higinet.tms35.manage.dataanalysic.controller;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.com.higinet.rapid.web.model.Model;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.core.cache.ip_cache;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.dataanalysic.model.ReportModel;
import cn.com.higinet.tms35.manage.dataanalysic.service.UserBehaviorService;
import cn.com.higinet.tms35.manage.dataanalysic.service.impl.UserBehaviorServiceImpl;
import cn.com.higinet.tms35.manage.tran.TransCommon;



@Controller("userBehaviorController")
@RequestMapping("/tms35/dataanalysic")
public class UserBehaviorController {
	private static Logger logger = Logger.getLogger(UserBehaviorController.class);
	@Autowired
	UserBehaviorService userBehaviorService;
	
	@RequestMapping(value="/userBehavior", method=RequestMethod.GET)
	public String statvalueListView(){
		return "tms35/dataanalysic/userBehavior";
	}
	
	/**
	 * 查询用户行为习惯维度
	 * @param userIdArray
	 * @param weiDus
	 * @param txnId
	 * @return
	 */
	@RequestMapping(value="/queryWeiDus",method=RequestMethod.POST)
	public Model queryCodeList(@RequestParam String[] userIdArray,String[] weiDus,String txnId) {
		Model model = new Model();
		
		Map<String, List<Map<String, Object>>> weiDuListMap = userBehaviorService.queryWeiDus(userIdArray, weiDus, txnId);
		
		model.setRow(weiDuListMap);
		return model;
	}
	
	/**
	* 方法描述:查询交易统计
	* @param reqs
	* @return
	 */
	@RequestMapping(value="/statfunc", method=RequestMethod.POST)
	public Model statfunc(@RequestParam String txnId){
		Model model = new Model();
		List<Map<String, Object>> stat_list = userBehaviorService.queryTransStatFunc(txnId);
		
		model.setRow(stat_list);
		return model;
	}

	
	/**
	* 方法描述:查询交易树
	* @param reqs
	* @return
	 */
	@RequestMapping(value="/transBranches", method=RequestMethod.POST)
	public Model txnList(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		
		List<Map<String, Object>> txn_list = userBehaviorService.queryTransBranches();
		
		model.setRow(txn_list);
		return model;
	}
	
	/**
	 * 获取交易属性
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/txnFeature", method=RequestMethod.POST)
	public Model txnFeature(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		List<Map<String,Object>> txn_rule_list = userBehaviorService.queryTranpropertiesWeiDus(reqs);
		
		model.setRow(txn_rule_list);
		return model;
	}

	
	 /**
	  * 资金转账地域视图
	  * @return
	  */
	@RequestMapping(value="/rechargeRegion", method=RequestMethod.GET)
	public String rechargeRegion(){
		return "tms35/dataanalysic/rechargeRegionReport";
	}
	
	
	
	/**
	 * 资金转账地域视图
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/rechargeRegionpost", method=RequestMethod.POST)
	public Model rechargeRegion(@RequestParam Map<String, String> reqs){
		String isFirestQuery = MapUtil.getString(reqs, "isFirestQuery");//是否查询
		if("true".equals(isFirestQuery)){
			return null;
		}
		ReportModel model = new ReportModel();		
		List<Map<String, Object>> txn_list = userBehaviorService.queryRechargeRegion(reqs,model);	
		for (Map<String,Object> dataMap:txn_list)	
			logger.debug("txn_list>>>>>>>.."+dataMap);
		for (Map<String,Object> dataMap:(List<Map<String,Object>>)(model.getModel().get("geocoordmaplist")))		
			logger.debug("citymap>>>>>>>.."+dataMap);
		return model;
	}
	

	
	
	@RequestMapping(value="/phoneRechargeRegion", method=RequestMethod.GET)
	public String phoneRechargeRegion(){
		return "tms35/dataanalysic/phoneRechargeRegionReport";
	}
	
	
	/**
	* 方法描述:手机充值地域视图
	* @param reqs
	* @return
	 */
	@RequestMapping(value="/phoneRechargeRegionpost", method=RequestMethod.POST)
	public Model phoneRechargeRegion(@RequestParam Map<String, String> reqs){
		String isFirestQuery = MapUtil.getString(reqs, "isFirestQuery");//是否查询
		if("true".equals(isFirestQuery)){
			return null;
		}
		ReportModel model = new ReportModel();		
		List<Map<String, Object>> txn_list = userBehaviorService.queryPhoneRechargeRegion(reqs,model);	
		for (Map<String,Object> dataMap:txn_list)	
			logger.debug("txn_list>>>>>>>.."+dataMap);
		for (Map<String,Object> dataMap:(List<Map<String,Object>>)(model.getModel().get("geocoordmaplist")))	
			logger.debug("citymap>>>>>>>.."+dataMap);
		return model;
	}
	
	
	public static void main(String[] args) {
		String city = ip_cache.Instance().get_city(ip_cache.LOCATE_TYPE_MOBILE, str_tool.to_str("18664327415"));
		System.out.println(">>>>"+city);
		
	}

}
