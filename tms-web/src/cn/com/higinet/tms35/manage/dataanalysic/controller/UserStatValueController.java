package cn.com.higinet.tms35.manage.dataanalysic.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.rapid.web.model.Model;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.dataanalysic.service.UserStatValueService;
import cn.com.higinet.tms35.manage.tran.TransCommon;



@Controller("userStatValueController")
@RequestMapping("/tms35/dataanalysic")
public class UserStatValueController {
	@Autowired
	private SimpleDao tmsSimpleDao; 

	@Autowired
	private  UserStatValueService userStatValueService;
	
	@RequestMapping(value="/userStatValueList", method=RequestMethod.GET)
	public String statvalueListView(){
		return "tms35/dataanalysic/userstatvalue";
	}
	
	
	@RequestMapping(value="/getStatFunc1", method=RequestMethod.POST)
	public Model statfunc(@RequestParam String txnId){
		Model model = new Model();
		List<Map<String, Object>> stat_list = userStatValueService.queryTransStatFunc(txnId);
		model.setRow(stat_list);
		return model;
	}
		/*Model model = new Model();
		String stat_sql = "SELECT STAT_ID ID,STAT_TXN,STAT_NAME, stat_txn fid,STAT_DESC CODE_VALUE,'2' ftype,STAT_TXN FROM TMS_COM_STAT where STAT_VALID =1"; 
		// 查询统计
		List<Map<String,Object>> stat_list = tmsSimpleDao.queryForList(stat_sql);
		
		//if(stat_list == null || stat_list.size() == 0) {
			model.setRow(stat_list);
			return model;
		//}
		
		/*String txn_id="";
		Map<String,String> id_m = new HashMap<String, String>();
		for (Map<String, Object> map : stat_list) {
			String txnid = MapUtil.getString(map, "fid");
			String stat_txn = MapUtil.getString(map, "STAT_TXN");
			String stat_name = MapUtil.getString(map, "STAT_NAME");
			
			map.put("CODE_KEY",stat_txn+":"+stat_name);
			
			String[] id_arr = TransCommon.cutToIds(txnid);
			for (String id : id_arr) {
				id_m.put(id, id);
			}
		}
		
		Set<String> key = id_m.keySet();
		for (String s : key) {
			if(txn_id.length() > 0) {
				txn_id += ",";
			}
			txn_id += "'"+s+"'";
		}
		
		String txn_sql = "SELECT TAB_NAME ID,M.TAB_NAME CODE_KEY,m.parent_tab fid,m.tab_desc CODE_VALUE ,'1' ftype, TAB_NAME STAT_TXN FROM TMS_COM_TAB M WHERE M.TAB_NAME in ("+txn_id+") order by STAT_TXN";
		// 查询交易树
		List<Map<String,Object>> txn_list = tmsSimpleDao.queryForList(txn_sql);
		
		stat_list.addAll(txn_list);
		model.setRow(stat_list);
		return model;*/
	//}
   @RequestMapping(value="/statList", method=RequestMethod.POST) 
   	public Model statListActoin(@RequestParam Map<String, String> reqs){
	   Model model = new Model();
	   model.setPage(userStatValueService.statPage(reqs));
	   return model;
	   
   }
   /**
	* 方法描述:查询交易树
	* @param reqs
	* @return
	 */
	@RequestMapping(value="/transBranches1", method=RequestMethod.POST)
	public Model txnList(@RequestParam Map<String, String> reqs){
		Model model = new Model();
		
		List<Map<String, Object>> txn_list = userStatValueService.queryTransBranches();
		
		model.setRow(txn_list);
		return model;
	}
}
