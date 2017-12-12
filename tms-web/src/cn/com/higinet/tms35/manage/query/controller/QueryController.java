package cn.com.higinet.tms35.manage.query.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

import cn.com.higinet.cmc.util.StringUtil;
import cn.com.higinet.rapid.base.dao.Page;
import cn.com.higinet.rapid.web.model.Model;
import cn.com.higinet.tms35.manage.common.DBConstant;
import cn.com.higinet.tms35.manage.common.util.CalendarUtil;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.exception.TmsMgrWebException;
import cn.com.higinet.tms35.manage.query.common.model.JsonDataProcess;
import cn.com.higinet.tms35.manage.query.service.QueryService;

@Controller("queryController")
@RequestMapping("/tms35/query")
public class QueryController {
	@Autowired
	private QueryService queryService;

	/**
	 * 自定义查询列表页View
	 * 
	 * @return
	 */
	@RequestMapping(value = "/tree", method = RequestMethod.GET)
	public String treeQueryView() {
		return "tms35/query/query_tree";
	}

	/**
	 * 自定义查询列表页Action
	 * 
	 * @param reqs
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/tree", method = RequestMethod.POST)
	public Model treeQueryAction(@RequestParam Map<String, String> reqs, HttpServletRequest request) {
		Model model = new Model();
		String context = request.getContextPath();
		List<Map<String, Object>> querylist = queryService.listQuery(reqs);
		List<Map<String, Object>> grouplist = queryService.listQueryGroup(reqs);
		List<Map<String, Object>> treeCon = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> map : grouplist) {
			Map<String, Object> treeMap = new HashMap<String, Object>();
			treeMap.put("id", MapUtil.getString(map, "GROUP_ID"));
			treeMap.put("qtype", "0");
			treeMap.put("text", MapUtil.getString(map, "GROUP_NAME"));
			treeMap.put("fid", MapUtil.getString(map, "PARENT_ID"));
			treeMap.put("onum", MapUtil.getInteger(map, "ORDERBY"));
			treeCon.add(treeMap);
		}
		for (Map<String, Object> map : querylist) {
			Map<String, Object> treeMap = new HashMap<String, Object>();
			treeMap.put("id", MapUtil.getString(map, DBConstant.TMS_COM_QUERY_QUERY_ID));
			treeMap.put("qtype", "1");
			treeMap.put("text", MapUtil.getString(map, DBConstant.TMS_COM_QUERY_QUERY_DESC));
			treeMap.put("fid", MapUtil.getString(map, "GROUP_ID"));
			treeMap.put("onum", MapUtil.getString(map, DBConstant.TMS_COM_QUERY_QUERY_ID));
			treeMap.put("qdata", MapUtil.getString(map, DBConstant.TMS_COM_QUERY_QUERY_DATA));
			String ctime = MapUtil.getString(map, DBConstant.TMS_COM_QUERY_CTIME);
			ctime = StringUtil.isBlank(ctime) ? "" : CalendarUtil.FORMAT14.format(CalendarUtil.parseStringToDate(ctime, CalendarUtil.FORMAT14));
			treeMap.put("ctime", ctime);
			treeCon.add(treeMap);
		}
		model.set("contextPath", context);
		model.setList(treeCon);
		return model;
	}

	/**
	 * 新增自定义查询action
	 * 
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Model addQueryGroupAction(@RequestParam Map<String, String> reqs) {
		Model model = new Model();
		String nodeType = reqs.get("tNodeType");
		long parentNodeId = MapUtil.getLong(reqs, "parentNodeId");
		String nodeName = reqs.get("tNodeName");
		Map<String, Object> result = null;
		if (nodeType.equals("0")) {// 分组
			Map<String, Object> group = new HashMap<String, Object>();
			group.put(DBConstant.TMS_COM_QUERY_GROUP_PARENT_ID, parentNodeId);
			group.put(DBConstant.TMS_COM_QUERY_GROUP_GROUP_NAME, nodeName);
			group.put(DBConstant.TMS_COM_QUERY_GROUP_ORDERBY, reqs.get("orderBy"));
			result = queryService.createQueryGroup(group);
		} else if (nodeType.equals("1")) {// 查询
			Map<String, Object> query = new HashMap<String, Object>();
			query.put(DBConstant.TMS_COM_QUERY_QUERY_DESC, nodeName);
			query.put(DBConstant.TMS_COM_QUERY_QUERY_DATA, reqs.get("queryData"));
			query.put(DBConstant.TMS_COM_QUERY_GROUP_ID, parentNodeId);
			result = queryService.createQuery(query);
			if (result.containsKey(DBConstant.TMS_COM_QUERY_CTIME)) {
				Object ctime = result.get(DBConstant.TMS_COM_QUERY_CTIME);
				ctime = CalendarUtil.parseTimeMillisToDateTime(((Date) ctime).getTime(), CalendarUtil.FORMAT7.toPattern());
				result.put(DBConstant.TMS_COM_QUERY_CTIME, ctime);
			}
		} else {
			model.addError("无此节点类型！");
		}
		model.set("resultMap", result);
		return model;
	}

	/**
	 * 更新自定义查询action
	 * 
	 * @return
	 */
	@RequestMapping(value = "/mod", method = RequestMethod.POST)
	public Model modQueryGroupAction(@RequestParam Map<String, String> reqs) {
		Model model = new Model();
		String nodeType = reqs.get("tNodeType");
		long nodeId = MapUtil.getLong(reqs, "nodeId");
		String nodeName = reqs.get("tNodeName");
		if (nodeType.equals("0")) {// 分组
			Map<String, Object> group = new HashMap<String, Object>();
			group.put(DBConstant.TMS_COM_QUERY_GROUP_GROUP_ID, nodeId);
			group.put(DBConstant.TMS_COM_QUERY_GROUP_GROUP_NAME, nodeName);
			group.put(DBConstant.TMS_COM_QUERY_GROUP_ORDERBY, reqs.get("orderBy"));
			queryService.updateQueryGroup(group);
		} else if (nodeType.equals("1")) {// 查询
			Map<String, Object> query = new HashMap<String, Object>();
			query.put(DBConstant.TMS_COM_QUERY_QUERY_ID, nodeId);
			query.put(DBConstant.TMS_COM_QUERY_QUERY_DESC, nodeName);
			query.put(DBConstant.TMS_COM_QUERY_QUERY_DATA, reqs.get("queryData"));
			queryService.updateQuery(query);
		} else {
			model.addError("无此节点类型！");
		}
		return model;
	}

	/**
	 * 删除自定义查询action
	 * 
	 * @return
	 */
	@RequestMapping(value = "/del", method = RequestMethod.POST)
	public Model delQueryGroupAction(@RequestParam Map<String, String> reqs, HttpServletRequest request) {
		Model model = new Model();
		long nodeId = MapUtil.getLong(reqs, "nodeId");
		String nodeType = reqs.get("tNodeType");
		if (nodeType.equals("0")) {// 分组
			queryService.deleteQueryGroup(nodeId, request);
		} else if (nodeType.equals("1")) {// 查询
			queryService.deleteQuery(nodeId, request);
		}
		return model;
	}

	/**
	 * 自定义查询显示页View
	 * 
	 * @return
	 */
	@RequestMapping(value = "/show", method = RequestMethod.GET)
	public String showQueryView() {
		return "tms35/query/query_display";
	}

	/**
	 * 自定义查询显示页Action
	 * 
	 * @param reqs
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/show", method = RequestMethod.POST)
	public Model showQueryAction(@RequestParam Map<String, Object> reqs, HttpServletRequest request) {
		Model model = new Model();
		customQueryProcecess(model, reqs, request);
		model.set("unique", StringUtil.randomUUID());
		return model;
	}

	/**
	 * 动态生成前台js代码
	 * 
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/jcqView", method = RequestMethod.GET)
	public Model dynamicProducedJsCode(@RequestParam Map<String, Object> reqs, HttpServletRequest request) {
		Model model = new Model();
		String unique = (String) MapUtil.getObject(reqs, "unique");
		 //防止传入javascript	
		if (null != unique && unique.trim().length() > 0) {
			if(unique.trim().length()>36){
				return null;
			} 
			//如果前后不一样则包括了javascript代码
			if(!HtmlUtils.htmlEscape(unique).equals(unique)){
				return null;
			}
			
			unique.replace("script", "");
			
			if(unique.toLowerCase().indexOf("onload")>0){
				return null;
			}
			if(unique.toLowerCase().indexOf("alert")>0){
				return null;
			}
		}
		model.set("unique", unique);
		
		customQueryProcecess(model, reqs, request);
		
		model.setViewName("tms35/query/jcq_view");
		return model;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/result", method = RequestMethod.POST)
	public Model showQueryResultAction(@RequestParam Map<String, Object> reqs, HttpServletRequest request) {
		Model model = new Model();
		Object resultData = queryService.getCustomQueryResultDataList(request);
		if (resultData == null) {
			model.setPage(null);
		} else {
			model.setPage((Page<Map<String, Object>>) resultData);
		}
		return model;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/resultList", method = RequestMethod.POST)
	public Model showQueryResultListAction(@RequestParam Map<String, Object> reqs, HttpServletRequest request) {
		Model model = showQueryResultAction(reqs, request);
		if (model.get(Model.PAGE) == null) {
			model.setList(null);
		} else {
			model.setList(((Page<Map<String, Object>>) model.get(Model.PAGE)).getList());
		}
		model.setPage(null);
		return model;
	}

	@RequestMapping(value = "/export", method = RequestMethod.GET)
	public void exportListAction(@RequestParam Map<String, String> reqs, HttpServletRequest request, HttpServletResponse response) {
		String exportType = reqs.get("exportType");
		if (StringUtil.isBlank(exportType)) {
			exportType = "xlsx";
		}
		if (exportType.equalsIgnoreCase("csv")) {
			exportList2CSVAction(request, response);
		} else if (exportType.equalsIgnoreCase("txt")) {
			exportList2TXTAction(request, response);
		} else if (exportType.equalsIgnoreCase("xls")) {
			exportList2XLSAction(request, response);
		} else if (exportType.equalsIgnoreCase("xlsx")) {
			exportList2XLSXAction(request, response);
		} else {
			throw new TmsMgrWebException("当前不支持导出" + exportType + "类型文件.");
		}
	}

	/**
	 * 导出数据到csv
	 * 
	 * @param reqs
	 * @param response
	 */
	@RequestMapping(value = "/exportCSV", method = RequestMethod.GET)
	public void exportList2CSVAction(HttpServletRequest request, HttpServletResponse response) {
		exportCommonAction("csv", request, response);
	}

	/**
	 * 导出数据到txt
	 * 
	 * @param reqs
	 * @param response
	 */
	@RequestMapping(value = "/exportTXT", method = RequestMethod.GET)
	public void exportList2TXTAction(HttpServletRequest request, HttpServletResponse response) {
		exportCommonAction("txt", request, response);
	}

	/**
	 * 导出数据到老版本的Excel
	 * 
	 * @param reqs
	 * @param response
	 */
	@RequestMapping(value = "/exportXLS", method = RequestMethod.GET)
	public void exportList2XLSAction(HttpServletRequest request, HttpServletResponse response) {
		exportCommonAction("xls", request, response);
	}

	/**
	 * 导出数据到新版本的Excel
	 * 
	 * @param reqs
	 * @param response
	 */
	@RequestMapping(value = "/exportXLSX", method = RequestMethod.GET)
	public void exportList2XLSXAction(HttpServletRequest request, HttpServletResponse response) {
		exportCommonAction("xlsx", request, response);
	}

	public void exportCommonAction(String expType, HttpServletRequest request, HttpServletResponse response) {
		queryService.exportList(expType, request, response);
	}

	private void customQueryProcecess(Model model, Map<String, Object> reqs, HttpServletRequest request) {
		JsonDataProcess process = queryService.getCustomQueryDataProcess(request.getParameterMap());
		model.set("custom", process.getCustom());
		model.set("fields", process.getFieldsMap());
	}
}