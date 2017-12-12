package cn.com.higinet.tms35.manage.tran.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.com.higinet.rapid.web.model.Model;
import cn.com.higinet.tms35.manage.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.common.util.StringUtil;
import cn.com.higinet.tms35.manage.dp.service.TxnGroupService;
import cn.com.higinet.tms35.manage.tran.service.TransDefService;

/**
 * 交易模型控制器
 * @author yangk
 */
@Controller
@RequestMapping("/tms35/trandef")
public class TransDefController {

//	@Autowired
//	private DataSource dynamicDataSource;
	@Autowired
	private TransDefService transDefService;
	@Autowired
	private TxnGroupService txnGroupService;
	@Autowired
	private ObjectMapper objectMapper;
	
	/**
	 * 初始化交易模型树
	 * @return
	 */
	@RequestMapping(value="/init", method=RequestMethod.GET)
	public String initTranDefTree(
			@RequestParam Map<String, String> reqs){
		return "tms35/model/model_list";
	}
	
	/**
	 * 初始化交易模型树
	 * @return
	 * @throws SQLException 
	 */
	@RequestMapping(value="/query", method=RequestMethod.POST)
	public Model queryTranDefTree(
			HttpServletRequest request,
			@RequestParam Map<String, String> reqs) throws SQLException{
		
		Model model = new Model();
		model.setList(getTranMdlTreeList());
		return model;
	}
	/**
	 * 初始化规则模型树
	 * @return
	 * @throws SQLException 
	 */
	@RequestMapping(value="/queryRuleTree", method=RequestMethod.POST)
	public Model queryTranRuleTree(
			HttpServletRequest request,
			@RequestParam Map<String, String> reqs) throws SQLException{
		
		Model model = new Model();
		model.setList(getTranRuleTreeList());
		return model;
	}
	
	/**
	 * 去交易模型树,渲染节点返回
	 * @return
	 */
	private List<Map<String, Object>> getTranRuleTreeList() {
		
		List<Map<String, Object>> tranMdlList = transDefService.getTranDefs();
		List<Map<String, Object>> tranRuleList = transDefService.getTranRules();
		tranMdlList.addAll(tranRuleList);
		List<Map<String, Object>> treeMdlList = new ArrayList<Map<String, Object>>();

		for(int i = 0; i <tranMdlList.size(); i++){
			
			Map<String, Object> tranMdlMap = tranMdlList.get(i);
			Map<String, Object> treeMdlMap = new HashMap<String, Object>();
			treeMdlMap.putAll(tranMdlMap);
			
			treeMdlMap.put("text", tranMdlMap.get(TMS_COM_TAB.TAB_DESC));
			treeMdlMap.put("ftype", "4");
			treeMdlMap.put("flag", "1");
			
			if (MapUtil.getString(tranMdlMap, TMS_COM_TAB.TAB_NAME).startsWith("T")) {
				treeMdlMap.put("isgroup", "1"); // 交易或交易组
			} else {
				treeMdlMap.put("isgroup", "0"); // 规则
			}
		
			treeMdlMap.put("enable", tranMdlMap.get(TMS_COM_TAB.IS_ENABLE)); // 1启用
			treeMdlMap.put("onum", tranMdlMap.get(TMS_COM_TAB.SHOW_ORDER));
			treeMdlMap.put("id", tranMdlMap.get(TMS_COM_TAB.TAB_NAME));
			treeMdlMap.put("fid", tranMdlMap.get(TMS_COM_TAB.PARENT_TAB));
			treeMdlMap.put("txnid", tranMdlMap.get(TMS_COM_TAB.TXNID));
			treeMdlMap.put("TAB_DISPOSAL", tranMdlMap.get(TMS_COM_TAB.TAB_DISPOSAL));
			treeMdlList.add(treeMdlMap);
		}
		
		return treeMdlList;
	}
	/**
	 * 去交易模型树,渲染节点返回
	 * @return
	 */
	private List<Map<String, Object>> getTranMdlTreeList() {
		
		List<Map<String, Object>> tranMdlList = transDefService.getTranDefs();
		List<Map<String, Object>> treeMdlList = new ArrayList<Map<String, Object>>();

		for(int i = 0; i <tranMdlList.size(); i++){
			
			Map<String, Object> tranMdlMap = tranMdlList.get(i);
			Map<String, Object> treeMdlMap = new HashMap<String, Object>();
			treeMdlMap.putAll(tranMdlMap);
			
			treeMdlMap.put("text", tranMdlMap.get(TMS_COM_TAB.TAB_DESC));
			treeMdlMap.put("ftype", "4");
			treeMdlMap.put("flag", "1");
			
			if (StringUtil.isNotEmpty(
					MapUtil.getString(tranMdlMap, TMS_COM_TAB.TXNID))) {
				treeMdlMap.put("isgroup", "0"); // 交易
			} else {
				treeMdlMap.put("isgroup", "1"); // 交易组
			}
		
			treeMdlMap.put("enable", tranMdlMap.get(TMS_COM_TAB.IS_ENABLE)); // 1启用
			treeMdlMap.put("onum", tranMdlMap.get(TMS_COM_TAB.SHOW_ORDER));
			treeMdlMap.put("id", tranMdlMap.get(TMS_COM_TAB.TAB_NAME));
			treeMdlMap.put("fid", tranMdlMap.get(TMS_COM_TAB.PARENT_TAB));
			treeMdlMap.put("txnid", tranMdlMap.get(TMS_COM_TAB.TXNID));
			treeMdlMap.put("TAB_DISPOSAL", tranMdlMap.get(TMS_COM_TAB.TAB_DISPOSAL));
			treeMdlList.add(treeMdlMap);
		}
		
		return treeMdlList;
	}
	/**
	 * 初始化交易模型树
	 * @return
	 * @throws SQLException 
	 */
	@RequestMapping(value="/queryTmp", method=RequestMethod.POST)
	public Model queryTranDefTreeTmp(
			HttpServletRequest request,
			@RequestParam Map<String, String> reqs) throws SQLException{
		
		Model model = new Model();
		model.setList(getTranMdlTreeList());
		return model;
	}
	
	
	/**
	 * 新建,保存交易模型
	 * @return
	 */
	@RequestMapping(value="/save", method=RequestMethod.POST)
	public Model saveTran(HttpServletRequest req,
			@RequestParam Map<String, Object> reqs){
		
		formatReq(reqs);
		Map<String, Object> p = new HashMap<String, Object>(reqs);
		Map<String, List<Map<String, Object>>> formMap = new HashMap<String, List<Map<String, Object>>>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		list.add(p);
		formMap.put(MapUtil.getString(reqs, "op"), list);
		
		try {
			req.setAttribute("postData", objectMapper.writeValueAsString(formMap));
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		transDefService.saveTranDef(formMap);
		
		Model model = new Model();
		model.setList(getTranMdlTreeList());
		return model;
	}
	
	/*
	 * 转换order 00004->4
	 * @param reqs
	 */
	private void formatReq (Map<String, Object> reqs) {
		String order = MapUtil.getString(reqs, "show_order");
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(order);
		if (m.matches()) {
			reqs.remove("show_order");
			reqs.put("show_order", Integer.parseInt(order));
		}
	}
	
	/**
	 * 删除交易模型
	 * @return
	 */
	@RequestMapping(value="/listChann", method=RequestMethod.POST)
	public Model listAllChannel(@RequestParam Map<String, String> reqs){
		List<Map<String,Object>> channellist = txnGroupService.listAllChannelWithOrder();
		Model model = new Model();
		model.set("channelList", channellist);
		return model;
	}
	
	/**
	 * 编辑交易模型时,load出节点数据
	 * @return
	 */
	@RequestMapping(value="/edit_prepare", method=RequestMethod.POST)
	public Model editPrepareTran(@RequestParam Map<String, String> reqs){
		
		Model model = new Model();
		model.set("infos", transDefService.getNodeFullInfos(MapUtil.getString(reqs, "tab_name")));
		model.set("channs", txnGroupService.listAllChannelWithOrder());
		
		return model;
	}
	
	@RequestMapping(value="/getAllTran",method=RequestMethod.POST)
	public Model queryAllTxns(){
		Model model = new Model();
		List<Map<String,Object>> tranList = transDefService.getAllTxn();
		model.setList(tranList);
		return model;
	}
	
}
